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
import com.jeesite.modules.swm.util.MqSendUtil;
import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class DeviceChangeConsumer {

    private static final Logger logger = LoggerFactory.getLogger(DeviceChangeConsumer.class);
    // 第三方设备接口（和人员接口风格统一）

    private static final String THIRD_PARTY_DEVICE_BATCH_SYNC_URL = "你的第三方接口基础地址/api/Open/Device/SyncDevice";
    private static final String THIRD_PARTY_DEVICE_URL = "你的第三方接口基础地址/api/Open/Device";
    private static final String THIRD_PARTY_DEVICE_BATCH_URL = "http://第三方服务器/notify/deviceBatchChange";

    // 第三方接口配置
    private static final String THIRD_PARTY_BASE_URL = "/api/Open/Personnel";
    private static final String THIRD_PARTY_UNBIND_URL = THIRD_PARTY_BASE_URL + "/UnBind";
    private static final int TIMEOUT = 5000;

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
                logger.error("消费设备消息失败，已确认（不重试），deliveryTag:{}，msg:{}", deliveryTag, jsonStr);
            }

        } catch (Exception e) {
            logger.error("消费设备消息异常，已确认（不重试），deliveryTag:{}", deliveryTag, e);
        }
    }

    /**
     * 处理单条设备消息 新增、编辑、解绑 、删除
     */
//    private boolean handleSingleMsg(MqSendUtil.BaseDeviceMsg msg) {
//        try {
//            JSONObject param = new JSONObject();
//            param.put("msgId", msg.getMsgId());
//            param.put("operateType", msg.getOperateType());
//            if (msg.getOperateType().equals(SyncDataOperateTypeEnum.HELMET_DELETE.getCode())){
//                param.put("deviceId", msg.getDeviceId());
//            }
//            param.put("changeData", msg.getDeviceData());
//            param.put("operateTime", msg.getOperateTime());
//
//            System.out.println("================================"+param.toJSONString()+"=======================================");
//            System.out.println("================================"+param.toJSONString()+"=======================================");
//            System.out.println("================================"+param.toJSONString()+"=======================================");
//
////            String result = HttpUtil.createPost(THIRD_PARTY_PERSON_URL)
////                    .body(param.toJSONString()) // 替换 setBody -> body
////                    .contentType("application/json") // 必须设置 JSON 格式
////                    // 方式1：推荐（Hutool 5.x+）：分别设置连接超时和读取超时（更灵活）
////                    .setReadTimeout(TIMEOUT)       // 读取超时
////                    // 方式2（兼容旧版本）：一次性设置超时（连接+读取）
////                    // .timeout(THIRD_PARTY_TIMEOUT)
////                    .execute()
////                    .body();
////            return "success".equals(JSONObject.parseObject(result).getString("code"));
//            return true;
//        } catch (Exception e) {
//            logger.error("处理单条消息失败，personData:{}", msg.getDeviceData(), e);
//            return false;
//        }
//    }

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
                String syncId = (msg.getDeviceData() != null && StringUtils.isNotBlank(msg.getDeviceData().getDeviceId()))
                        ? msg.getDeviceData().getDeviceId().trim()
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
//                result = sendPutRequest(THIRD_PARTY_UNBIND_URL, requestParam);

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
//                result = HttpRequest.post(THIRD_PARTY_DEVICE_URL)
//                        .body(requestParam.toJSONString())
//                        .contentType("application/json")
//                        .setReadTimeout(TIMEOUT)
//                        .execute()
//                        .body();

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
//                result = HttpRequest.delete(THIRD_PARTY_DEVICE_URL)
//                        .form("deviceId", deviceId) // 自动拼接到query
//                        .setReadTimeout(TIMEOUT)
//                        .execute()
//                        .body();

                // ========== 分支4：更新设备（PUT + JSON格式） ==========
            } else if (SyncDataOperateTypeEnum.HELMET_EDIT.getCode().equals(operateType)) {
                // 校验deviceData和核心参数
                if (msg.getDeviceData() == null) {
                    logger.error("更新设备缺少deviceData，msgId:{}", msgId);
                    return false;
                }
                // 三目运算获取参数（空值处理）
                String deviceId = StringUtils.isNotBlank(msg.getDeviceData().getDeviceId())
                        ? msg.getDeviceData().getDeviceId().trim() : "";

                // 必传参数校验：deviceId不能为空
                if (deviceId.isEmpty()) {
                    logger.error("更新设备缺少必要参数deviceId，msgId:{}", msgId);
                    return false;
                }

                // 构造更新请求参数
                requestParam.put("deviceType", 5);
                requestParam.put("deviceId", deviceId);
                logger.info("构造更新设备请求参数，msgId:{}, param:{}", msgId, requestParam.toJSONString());
                System.out.println("================================"+requestParam.toJSONString()+"=======================================");
                System.out.println("================================"+requestParam.toJSONString()+"=======================================");
                System.out.println("================================"+requestParam.toJSONString()+"=======================================");
                System.out.println("================================"+requestParam.toJSONString()+"=======================================");
                // 发送PUT请求
//                result = HttpRequest.put(THIRD_PARTY_DEVICE_URL)
//                        .body(requestParam.toJSONString())
//                        .contentType("application/json")
//                        .setReadTimeout(TIMEOUT)
//                        .execute()
//                        .body();

                // ========== 分支5：不支持的操作类型 ==========
            } else {
                logger.warn("暂不支持的设备操作类型，msgId:{}, type:{}", msgId, operateType);
                return false;
            }

            // 调试打印请求参数
            System.out.println("================================"+requestParam.toJSONString()+"=======================================");

            // 响应校验：统一判断code=success
            boolean isSuccess = (result != null && "success".equals(JSONObject.parseObject(result).getString("code")));
            if (!isSuccess) {
                logger.error("设备消息处理失败，msgId:{}, result:{}", msgId, result);
            } else {
                logger.info("设备消息处理成功，msgId:{}", msgId);
            }
            return isSuccess;

        } catch (Exception e) {
            logger.error("处理设备消息异常，deviceData:{}", msg.getDeviceData(), e);
            return false;
        }
    }

    /**
     * 通用PUT请求封装（解绑接口用）
     */
    private String sendPutRequest(String url, JSONObject param) {
        try (HttpResponse res = HttpRequest.put(url)
                .body(param.toJSONString())
                .contentType("application/json")
                .setReadTimeout(TIMEOUT)
                .execute()) {
            return res.isOk() ? res.body() : null;
        } catch (Exception e) {
            logger.error("PUT请求异常，url:{}, param:{}", url, param.toJSONString(), e);
            return null;
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
                deviceObj.put("deviceType", 5);
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
            String result = null ;
//            String result = HttpRequest.post(THIRD_PARTY_DEVICE_BATCH_SYNC_URL)
//                    .body(requestBody)
//                    .contentType("application/json") // 严格匹配接口格式
//                    .setReadTimeout(TIMEOUT)
//                    .execute()
//                    .body();

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


}
