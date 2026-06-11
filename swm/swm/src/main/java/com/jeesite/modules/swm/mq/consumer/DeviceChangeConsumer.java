package com.jeesite.modules.swm.mq.consumer;


import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.alibaba.cloud.commons.lang.StringUtils;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.nacos.common.utils.CollectionUtils;
import com.jeesite.modules.config.RabbitMqConfig;
import com.jeesite.modules.enums.SyncDataOperateTypeEnum;
import com.jeesite.modules.swm.entity.SwmHelmetDevice;
import com.jeesite.modules.swm.entity.SwmThirdApiLog;
import com.jeesite.modules.swm.service.SwmThirdApiLogService;
import com.jeesite.modules.swm.util.MqSendUtil;
import com.jeesite.modules.swm.util.SignatureUtil;
import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class DeviceChangeConsumer {

    private static final Logger logger = LoggerFactory.getLogger(DeviceChangeConsumer.class);

    @Autowired
    private SwmThirdApiLogService swmThirdApiLogService;

    private final ThreadLocal<String> lastApiResponseBody = new ThreadLocal<>();
    // 第三方设备接口（和人员接口风格统一）

    @Value("${third-party.base-url:https://lbsapi.vgomap.com}")
    private String thirdPartyBaseUrl;

    @Value("${third-party.device.base-url:/api/Open/Device}")
    private String thirdPartyDeviceBasePath;

    @Value("${third-party.device.batch-sync-url:/api/Open/Device/SyncDevice}")
    private String thirdPartyDeviceBatchSyncPath;

    @Value("${third-party.device.unbind-url:/api/Open/Personnel/UnBind}")
    private String thirdPartyDeviceUnbindPath;

    @Value("${third-party.app-key:fBPMJTYsQ8ndmNVz6SxzmZz7rdTVdkEf}")
    private String appKey;

    @Value("${third-party.app-secret:XAt3NFSxQUpkH7UaATAStTYK7XB8JFct}")
    private String appSecret;

    @Value("${third-party.request.timeout:5000}")
    private int timeout;

    // 设备类型固定值（安全帽设备类型为5，抽成常量便于维护）
    private static final int DEVICE_TYPE_SAFETY_HELMET = 5;

    /**
     * 处理设备消息（完全复用人员消费端的结构：单条+批量解析、手动ACK、异常处理）
     */
    @RabbitListener(queues = RabbitMqConfig.DEVICE_CHANGE_QUEUE)
    public void consumeDeviceMsg(Message message, Channel channel) throws IOException {
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        boolean isSuccess = false;

        try {
            // 1. 读取JSON字符串
            String jsonStr = new String(message.getBody(), StandardCharsets.UTF_8);
            String msgType = (String) message.getMessageProperties().getHeader("msgType");
            logger.info("接收到设备JSON消息，msgType:{}，内容:{}", msgType, jsonStr);

            // 2. 根据msgType判断消息类型，手动反序列化
            if ("BaseDeviceMsg".equals(msgType)) {
                // 处理单条设备消息
                MqSendUtil.BaseDeviceMsg singleMsg = JSONObject.parseObject(jsonStr, MqSendUtil.BaseDeviceMsg.class);
                isSuccess = handleSingleMsg(singleMsg);
            } else if ("BatchDeviceChangeMsg".equals(msgType)) {
                // 处理批量设备消息
                MqSendUtil.BatchDeviceChangeMsg batchMsg = JSONObject.parseObject(jsonStr, MqSendUtil.BatchDeviceChangeMsg.class);
                isSuccess = handleBatchMsg(batchMsg);
            } else {
                logger.warn("未知的设备msgType：{}，消息内容：{}", msgType, jsonStr);
                isSuccess = true; // 标记成功，避免重复消费
            }

            // 3. 消息确认
            if (isSuccess) {
                channel.basicAck(deliveryTag, false);
                logger.info("消费设备消息成功，deliveryTag:{}", deliveryTag);
            } else {
                // 失败时：拒绝并重新入队（关键修改）
                channel.basicNack(deliveryTag, false, false);
                logger.error("消费消息失败，已拒绝并重新入队，deliveryTag:{}，msg:{}", deliveryTag, jsonStr);
            }

        } catch (Exception e) {
            logger.error("消费消息异常，拒绝并重新入队，deliveryTag:{}", deliveryTag, e);
            // 异常时：确保执行Nack（捕获IO异常，避免二次报错）
            try {
                channel.basicNack(deliveryTag, false, false);
            } catch (IOException ioEx) {
                logger.error("执行basicNack失败，deliveryTag:{}", deliveryTag, ioEx);
            }
        }
    }


    /**
     * 处理单条设备消息（完整支持：新增/删除/更新/解绑设备）
     */
    private boolean handleSingleMsg(MqSendUtil.BaseDeviceMsg msg) {
        // 前置空校验：核心字段为空直接返回失败
        if (msg == null || msg.getOperateType() == null) {
            logger.error("设备消息核心字段为空，msgId:{}", msg == null ? "未知" : msg.getMsgId());
            return false;
        }

        try {
            String operateType = msg.getOperateType();
            String msgId = msg.getMsgId();
            logger.info("开始处理设备消息，msgId:{}, operateType:{}", msgId, operateType);

            JSONObject requestParam = new JSONObject();
            String result = null;

            // ========== 分支1：解绑安全帽（原有逻辑，保留） ==========
            if (SyncDataOperateTypeEnum.HELMET_UNBIND.getCode().equals(operateType)) {
                // 解绑安全帽：调用PUT /api/Open/Personnel/UnBind，仅需syncId
                String syncId = (msg.getDeviceData() != null && StringUtils.isNotBlank(msg.getDeviceData().getPersonId()))
                        ? msg.getDeviceData().getPersonId().trim()
                        : "";

                if (syncId.isEmpty()) {
                    logger.error("解绑操作缺少必要参数syncId，msgId:{}", msgId);
                    return false;
                }

                requestParam.put("syncId", syncId);
                logger.info("构造解绑请求参数，msgId:{}, param:{}", msgId, requestParam.toJSONString());
                System.out.println("================================"+requestParam.toJSONString()+"=======================================");
                System.out.println("================================"+requestParam.toJSONString()+"=======================================");
                System.out.println("================================"+requestParam.toJSONString()+"=======================================");
                String requestUrl = thirdPartyBaseUrl + thirdPartyDeviceUnbindPath;
                Date requestTime = new Date();
                result = sendPutRequest(requestUrl, requestParam);
                saveThirdApiLog("device", "PUT", requestUrl, requestParam.toJSONString(), getApiResponseParam(result),
                        requestTime, new Date(), result != null ? 200 : null, null);

                // ========== 分支2：新增设备（POST + JSON格式） ==========
            } else if (SyncDataOperateTypeEnum.HELMET_ADD.getCode().equals(operateType)) {
                // 校验deviceData和核心参数
                if (msg.getDeviceData() == null) {
                    logger.error("新增设备缺少deviceData，msgId:{}", msgId);
                    return false;
                }
                // 三目运算获取参数（空值处理）
                String deviceId = StringUtils.isNotBlank(msg.getDeviceData().getDeviceId())
                        ? msg.getDeviceData().getDeviceId().trim() : "";

                // 必传参数校验：deviceId不能为空
                if (deviceId.isEmpty()) {
                    logger.error("新增设备缺少必要参数deviceId，msgId:{}", msgId);
                    return false;
                }

                // 构造新增请求参数
                requestParam.put("deviceType", 5);
                requestParam.put("deviceId", deviceId);
                logger.info("构造新增设备请求参数，msgId:{}, param:{}", msgId, requestParam.toJSONString());
                System.out.println("================================"+requestParam.toJSONString()+"=======================================");
                System.out.println("================================"+requestParam.toJSONString()+"=======================================");
                System.out.println("================================"+requestParam.toJSONString()+"=======================================");
                System.out.println("================================"+requestParam.toJSONString()+"=======================================");
                // 发送POST请求
                String requestUrl = thirdPartyBaseUrl + thirdPartyDeviceBasePath;
                Date requestTime = new Date();
                result = sendPostRequest(requestUrl, requestParam);
                saveThirdApiLog("device", "POST", requestUrl, requestParam.toJSONString(), getApiResponseParam(result),
                        requestTime, new Date(), result != null ? 200 : null, null);
                // ========== 分支3：删除设备（DELETE + x-www-form-urlencoded） ==========
            } else if (SyncDataOperateTypeEnum.HELMET_DELETE.getCode().equals(operateType)) {
                // 获取deviceId（非必传，但空则提示）
                String deviceId = StringUtils.isNotBlank(msg.getDeviceId())
                        ? msg.getDeviceId().trim() : "";

                logger.info("构造删除设备请求参数，msgId:{}, deviceId:{}", msgId, deviceId);
                System.out.println("================================"+deviceId+"=======================================");
                System.out.println("================================"+deviceId+"=======================================");
                System.out.println("================================"+deviceId+"=======================================");
                System.out.println("================================"+deviceId+"=======================================");
                // 发送DELETE请求：query参数传deviceId（x-www-form-urlencoded格式）
                String requestUrl = thirdPartyBaseUrl + thirdPartyDeviceBasePath;
                Date requestTime = new Date();
                result = sendDeleteRequest(requestUrl, deviceId);
                saveThirdApiLog("device", "DELETE", requestUrl, deviceId, getApiResponseParam(result),
                        requestTime, new Date(), result != null ? 200 : null, null);

                // ========== 分支4：更新设备（PUT + JSON格式） ==========
            } else if (SyncDataOperateTypeEnum.HELMET_EDIT.getCode().equals(operateType)) {
                // 校验deviceData和核心参数
                if (msg.getDeviceData() == null) {
                    logger.error("更新设备缺少deviceData，msgId:{}", msgId);
                    return false;
                }
                // 三目运算获取设备id
                String deviceId = StringUtils.isNotBlank(msg.getDeviceData().getDeviceId())
                        ? msg.getDeviceData().getDeviceId().trim() : "";

                // 三目运算获取原设备id
                String oldDeviceId = (msg.getDeviceData() != null && StringUtils.isNotBlank(msg.getDeviceData().getOldDeviceId()))
                        ? msg.getDeviceData().getOldDeviceId().trim()
                        : "";

                // 必传参数校验：deviceId不能为空
                if (deviceId.isEmpty()) {
                    logger.error("更新设备缺少必要参数deviceId，msgId:{}", msgId);
                    return false;
                }

                // 构造更新请求参数
                requestParam.put("deviceType", 5);
                requestParam.put("deviceId", deviceId);
                requestParam.put("oldDeviceId", oldDeviceId);
                logger.info("构造更新设备请求参数，msgId:{}, param:{}", msgId, requestParam.toJSONString());
                System.out.println("================================"+requestParam.toJSONString()+"=======================================");
                System.out.println("================================"+requestParam.toJSONString()+"=======================================");
                System.out.println("================================"+requestParam.toJSONString()+"=======================================");
                System.out.println("================================"+requestParam.toJSONString()+"=======================================");
                // 发送PUT请求
                String requestUrl = thirdPartyBaseUrl + thirdPartyDeviceBasePath;
                Date requestTime = new Date();
                result = sendPutRequest(requestUrl, requestParam);
                saveThirdApiLog("device", "PUT", requestUrl, requestParam.toJSONString(), getApiResponseParam(result),
                        requestTime, new Date(), result != null ? 200 : null, null);
            } else {
                // ========== 分支5：不支持的操作类型 ==========
                logger.warn("暂不支持的设备操作类型，msgId:{}, type:{}", msgId, operateType);
                return false;
            }

            // 统一响应校验（和人员端一致：isSuccess+code=200）
            if (result == null) {
                logger.error("处理设备消息失败：第三方接口无响应，msgId:{}", msgId);
                return false;
            }
            JSONObject resultJson = JSONObject.parseObject(result);
            boolean isSuccess = resultJson.getBoolean("isSuccess") != null
                    && resultJson.getBoolean("isSuccess")
                    && resultJson.getIntValue("code") == 200;

            if (!isSuccess) {
                String errMsg = resultJson.getString("message") != null ? resultJson.getString("message") : "无错误信息";
                logger.error("处理设备消息失败：第三方接口返回失败，msgId:{}, code:{}, message:{}",
                        msgId, resultJson.getIntValue("code"), errMsg);
            } else {
                logger.info("处理设备消息成功，msgId:{}", msgId);
            }
            return isSuccess;

        } catch (Exception e) {
            logger.error("处理设备消息异常，deviceData:{}", msg.getDeviceData(), e);
            return false;
        }
    }

    /**
     * 处理批量设备消息 导入、批量全量同步
     */
    private boolean handleBatchMsg(MqSendUtil.BatchDeviceChangeMsg batchMsg) {
        // 1. 第一层：batchMsg本身为空 → 直接返回成功（原有逻辑）
        if (batchMsg == null) {
            logger.warn("批量消息反序列化失败：batchMsg为null");
            return true;
        }

        String batchMsgId = batchMsg.getMsgId();
        String operateType = batchMsg.getOperateType();
        List<SwmHelmetDevice> deviceList = batchMsg.getDeviceList();

        // 2. 第二层：核心元信息为空 → 返回成功（避免重复消费）（原有逻辑）
        if (StringUtils.isBlank(batchMsgId) || StringUtils.isBlank(operateType)) {
            logger.warn("批量消息缺少核心字段：msgId={}, operateType={}", batchMsgId, operateType);
            return true;
        }

        // 3. 第三层：按操作类型差异化校验业务数据（原有逻辑）
        boolean isEmptyData = CollectionUtils.isEmpty(deviceList);

        // 4. 业务数据为空 → 返回成功（原有逻辑）
        if (isEmptyData) {
            logger.warn("批量消息无有效业务数据，batchMsgId:{}, operateType:{}, deviceListSize:{}",
                    batchMsgId, operateType,
                    deviceList != null ? deviceList.size() : 0);
            return true;
        }

        // 5. 有效数据 → 处理业务逻辑（原有逻辑+调用扩展的批量处理方法）
        int totalCount = deviceList.size();
        logger.info("开始处理批量消息，batchMsgId:{}，operateType:{}，总条数:{}", batchMsgId, operateType, totalCount);

        boolean isSuccess = false;
        try {
            isSuccess = handleBatchOperation(batchMsgId, deviceList, operateType);
        } catch (Exception e) {
            logger.error("处理批量消息时发生异常，batchMsgId:{}，operateType:{}", batchMsgId, operateType, e);
        }
        return isSuccess;
    }



    private boolean handleBatchOperation(String batchMsgId, List<SwmHelmetDevice> deviceList, String operateType) {
        try {
            // 1. 过滤无效数据：剔除null/deviceId为空的设备，保证请求体合法
            List<SwmHelmetDevice> validDeviceList = deviceList.stream()
                    .filter(device -> device != null && StringUtils.isNotBlank(device.getDeviceId()))
                    .collect(Collectors.toList());

            // 二次校验：过滤后无有效数据则返回失败
            if (CollectionUtils.isEmpty(validDeviceList)) {
                logger.error("批量同步设备失败：过滤空值后无有效设备数据，batchMsgId:{}", batchMsgId);
                return false;
            }

            // 2. 构造接口要求的请求体：{"datas": [{"deviceType":5,"deviceId":"xxx"}]}
            JSONObject requestParam = new JSONObject();
            JSONArray datasArray = new JSONArray();

            for (SwmHelmetDevice device : validDeviceList) {
                JSONObject deviceObj = new JSONObject();
                // deviceType：默认值5（匹配接口示例），空则补默认值
                deviceObj.put("deviceType", DEVICE_TYPE_SAFETY_HELMET);
                // deviceId：已过滤空值，直接赋值
                deviceObj.put("deviceId", device.getDeviceId().trim());
                datasArray.add(deviceObj);
            }
            requestParam.put("datas", datasArray);

            // 调试打印请求参数
            String requestBody = requestParam.toJSONString();
            logger.info("构造批量同步设备请求体，batchMsgId:{}, 请求体:{}", batchMsgId, requestBody);
            System.out.println("------------------"+operateType+"---------------========="+requestBody+"=======================================");
            System.out.println("------------------"+operateType+"---------------========="+requestBody+"=======================================");
            System.out.println("------------------"+operateType+"---------------========="+requestBody+"=======================================");
            System.out.println("------------------"+operateType+"---------------========="+requestBody+"=======================================");

            // 3. 调用第三方批量同步接口（POST + JSON格式）
            String requestUrl = thirdPartyBaseUrl + thirdPartyDeviceBatchSyncPath;
            Date requestTime = new Date();
            String result = sendPostRequest(requestUrl, requestParam);
            saveThirdApiLog("device", "POST", requestUrl, requestBody, getApiResponseParam(result),
                    requestTime, new Date(), result != null ? 200 : null, null);

            // 4. 响应校验：统一判断code=success
            boolean isSuccess = (result != null && "success".equals(JSONObject.parseObject(result).getString("code")));
            if (isSuccess) {
                logger.info("批量同步设备成功，batchMsgId:{}, 有效设备数:{}", batchMsgId, validDeviceList.size());
            } else {
                logger.error("批量同步设备失败，batchMsgId:{}, result:{}", batchMsgId, result);
            }
            return isSuccess;

        } catch (Exception e) {
            logger.error("调用批量同步设备接口异常，batchMsgId:{}, deviceListSize:{}", batchMsgId, deviceList.size(), e);
            return false;
        }
    }

    /**
     * 通用POST请求封装（含签名，和人员端完全一致）
     */
    private String sendPostRequest(String url, JSONObject param) {
        try {
            long timestamp = System.currentTimeMillis();
            String nonce = UUID.randomUUID().toString().replace("-", "");
            // 构造待签名字符串（和人员端签名规则一致）
            String signContent = appKey + timestamp + appSecret + nonce;
            String sign = SignatureUtil.sha256(signContent);

            try (HttpResponse response = HttpRequest.post(url)
                    .body(param.toJSONString())
                    .contentType("application/json")
                    .header("AppId", appKey)
                    .header("Timestamp", String.valueOf(timestamp))
                    .header("Nonce", nonce)
                    .header("Sign", sign)
                    .setReadTimeout(timeout)
                    .execute()) {

                logger.info("发送设备POST请求，url:{}, param:{}, headers:{AppId, Timestamp, Nonce, Sign}",
                        url, param.toJSONString());
                if (response.isOk()) {
                    String body = response.body();
                    lastApiResponseBody.set(body);
                    if (SignatureUtil.isBusinessSuccess(body)) {
                        return body;
                    } else {
                        logger.error("设备POST请求业务失败，url:{}, response:{}", url, SignatureUtil.getErrorMessage(body));
                        return null;
                    }
                } else {
                    logger.error("设备POST请求HTTP失败，url:{}, status:{}, response:{}",
                            url, response.getStatus(), SignatureUtil.getErrorMessage(response.body()));
                    return null;
                }
            }
        } catch (Exception e) {
            logger.error("发送设备POST请求异常，url:{}, param:{}", url, param.toJSONString(), e);
            return null;
        }
    }

    /**
     * 通用PUT请求封装（含签名，和人员端完全一致）
     */
    private String sendPutRequest(String url, JSONObject param) {
        try {
            long timestamp = System.currentTimeMillis();
            String nonce = UUID.randomUUID().toString().replace("-", "");
            String signContent = appKey + timestamp + appSecret + nonce;
            String sign = SignatureUtil.sha256(signContent);

            try (HttpResponse response = HttpRequest.put(url)
                    .body(param.toJSONString())
                    .contentType("application/json")
                    .header("AppId", appKey)
                    .header("Timestamp", String.valueOf(timestamp))
                    .header("Nonce", nonce)
                    .header("Sign", sign)
                    .setReadTimeout(timeout)
                    .execute()) {

                logger.info("发送设备PUT请求，url:{}, param:{}, headers:{AppId, Timestamp, Nonce, Sign}",
                        url, param.toJSONString());
                if (response.isOk()) {
                    String body = response.body();
                    lastApiResponseBody.set(body);
                    if (SignatureUtil.isBusinessSuccess(body)) {
                        logger.info("设备PUT请求成功，url:{}, HTTP状态:{}, 响应体:{}",
                                url, response.getStatus(), body);
                        return body;
                    } else {
                        logger.error("设备PUT请求业务失败，url:{}, response:{}", url, SignatureUtil.getErrorMessage(body));
                        return null;
                    }
                } else {
                    logger.error("设备PUT请求HTTP失败，url:{}, status:{}, response:{}",
                            url, response.getStatus(), SignatureUtil.getErrorMessage(response.body()));
                    return null;
                }
            }
        } catch (Exception e) {
            logger.error("发送设备PUT请求异常，url:{}, param:{}", url, param.toJSONString(), e);
            return null;
        }
    }

    /**
     * 通用DELETE请求封装（含签名，和人员端完全一致）
     */
    private String sendDeleteRequest(String url, String deviceId) {
        try {
            long timestamp = System.currentTimeMillis();
            String nonce = UUID.randomUUID().toString().replace("-", "");
            String signContent = appKey + timestamp + appSecret + nonce;
            String sign = SignatureUtil.sha256(signContent);

            try (HttpResponse response = HttpRequest.delete(url)
                    .form("body", deviceId)
                    .contentType("application/json")
                    .header("AppId", appKey)
                    .header("Timestamp", String.valueOf(timestamp))
                    .header("Nonce", nonce)
                    .header("Sign", sign)
                    .setReadTimeout(timeout)
                    .execute()) {

                logger.info("发送设备DELETE请求，url:{}, bodyLength:{}", url, deviceId.length());
                if (response.isOk()) {
                    String resBody = response.body();
                    lastApiResponseBody.set(resBody);
                    if (SignatureUtil.isBusinessSuccess(resBody)) {
                        return resBody;
                    } else {
                        logger.error("设备DELETE请求业务失败，url:{}, response:{}", url, SignatureUtil.getErrorMessage(resBody));
                        return null;
                    }
                } else {
                    logger.error("设备DELETE请求HTTP失败，url:{}, status:{}, response:{}",
                            url, response.getStatus(), SignatureUtil.getErrorMessage(response.body()));
                    return null;
                }
            }
        } catch (Exception e) {
            logger.error("发送设备DELETE请求异常，url:{}, body:{}", url, deviceId, e);
            return null;
        }
    }

    private void saveThirdApiLog(String businessType, String httpMethod, String requestUrl, String requestParam,
                                 String responseParam, Date requestTime, Date responseTime,
                                 Integer httpStatus, String exceptionInfo) {
        try {
            SwmThirdApiLog apiLog = new SwmThirdApiLog();
            apiLog.setBusinessType(businessType);
            apiLog.setHttpMethod(httpMethod);
            apiLog.setRequestUrl(requestUrl);
            apiLog.setRequestParam(requestParam);
            apiLog.setResponseParam(responseParam);
            apiLog.setRequestTime(requestTime);
            apiLog.setResponseTime(responseTime);
            apiLog.setDuration(responseTime.getTime() - requestTime.getTime());
            apiLog.setHttpStatus(httpStatus);
            apiLog.setExceptionInfo(exceptionInfo);
            apiLog.setExecuteStatus(httpStatus != null && httpStatus >= 200 && httpStatus < 300
                    && responseParam != null && !"NOT_EXECUTED".equals(responseParam) ? "1" : "0");
            swmThirdApiLogService.saveLog(apiLog);
        } catch (Exception e) {
            logger.error("保存第三方接口调用日志失败，url:{}", requestUrl, e);
        }
    }

    private String getApiResponseParam(String result) {
        String responseParam = result != null ? result : lastApiResponseBody.get();
        lastApiResponseBody.remove();
        return responseParam;
    }


}
