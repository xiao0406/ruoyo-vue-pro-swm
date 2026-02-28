package com.jeesite.modules.swm.mq.consumer;


import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.alibaba.cloud.commons.lang.StringUtils;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.nacos.common.utils.CollectionUtils;
import com.jeesite.modules.config.RabbitMqConfig;
import com.jeesite.modules.enums.SyncDataOperateTypeEnum;
import com.jeesite.modules.swm.entity.SwmPerson;
import com.jeesite.modules.swm.util.MqSendUtil;
import com.jeesite.modules.swm.util.SignatureUtil;
import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class PersonChangeConsumer {
    private static final Logger logger = LoggerFactory.getLogger(PersonChangeConsumer.class);

    // ====================== 配置项（从配置文件读取，避免硬编码） ======================
    @Value("${third-party.base-url:https://lbsapi.vgomap.com}")
    private String thirdPartyBaseUrl;

    @Value("${third-party.person.base-url:/api/Open/Personnel}")
    private String thirdPartyPersonBasePath;

    @Value("${third-party.person.bind-url:/api/Open/Personnel/Bind}")
    private String thirdPartyPersonBindPath;

    @Value("${third-party.person.batch-delete-url:/api/Open/Personnel/BatchDelete}")
    private String thirdPartyPersonBatchDeletePath;

    @Value("${third-party.person.batch-sync-url:/api/Open/Personnel/Sync}")
    private String thirdPartyPersonBatchSyncPath;

    @Value("${third-party.app-key:fBPMJTYsQ8ndmNVz6SxzmZz7rdTVdkEf}")
    private String appKey;

    @Value("${third-party.app-secret:XAt3NFSxQUpkH7UaATAStTYK7XB8JFct}")
    private String appSecret;

    @Value("${third-party.request.timeout:5000}")
    private int timeout;

    /**
     * 处理消息
     */
    @RabbitListener(queues = RabbitMqConfig.PERSON_CHANGE_QUEUE)
    public void consumePersonMsg(Message message, Channel channel) throws IOException {
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        boolean isSuccess = false;

        try {
            // 1. 读取JSON字符串（核心：手动解析）
            String jsonStr = new String(message.getBody(), StandardCharsets.UTF_8);
            String msgType = (String) message.getMessageProperties().getHeader("msgType");
            logger.info("接收到JSON消息，msgType:{}，内容:{}", msgType, jsonStr);

            // 2. 根据msgType判断消息类型，手动反序列化
            if ("BasePersonMsg".equals(msgType)) {
                // 单条消息
                MqSendUtil.BasePersonMsg singlePersonMsg = JSONObject.parseObject(jsonStr, MqSendUtil.BasePersonMsg.class);
                isSuccess = handleSingleMsg(singlePersonMsg);
            } else if ("BatchPersonChangeMsg".equals(msgType)) {
                // 批量消息
                MqSendUtil.BatchPersonChangeMsg batchMsg = JSONObject.parseObject(jsonStr, MqSendUtil.BatchPersonChangeMsg.class);
                isSuccess = handleBatchMsg(batchMsg);
            } else {
                logger.warn("未知的msgType：{}，消息内容：{}", msgType, jsonStr);
                isSuccess = true; // 标记成功，避免重复消费
            }

            // 3. 消息确认
            if (isSuccess) {
                channel.basicAck(deliveryTag, false);
                logger.info("消费消息成功，deliveryTag:{}", deliveryTag);
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
     * 处理单条人员变更消息
     * 兼容场景：新增、编辑、绑定安全帽
     *
     * @param msg MQ人员消息体
     * @return 处理结果：true-成功，false-失败
     */
    private boolean handleSingleMsg(MqSendUtil.BasePersonMsg msg) {
        // 前置校验：消息或核心字段为空直接返回失败
        if (msg == null || msg.getOperateType() == null || msg.getPersonData() == null) {
            logger.error("处理单条消息失败：消息或核心字段为空，msgId:{}", msg.getMsgId());
            return false;
        }

        try {
            String operateType = msg.getOperateType();
            SwmPerson personData = msg.getPersonData();// 人员核心数据（包含syncId、name、gender等）
            String msgId = msg.getMsgId();
            logger.info("开始处理人员变更消息，msgId:{}, operateType:{}", msgId, operateType);

            // 声明响应结果和请求参数
            String result = null;
            JSONObject requestParam = new JSONObject();

            // 根据操作类型分支处理
            switch (operateType) {
                case "PERSON_ADD": // 新增人员 - 调用POST /api/Open/Personnel
                    // 构造创建人员请求参数
                    requestParam.put("syncId", personData.getId()); // 第三方要求的唯一标识
                    requestParam.put("name", personData.getName());     // 姓名
                    String addGenderStr = personData.getGender();
                    int addGenderCode = 0; // 默认未知性别
                    if (addGenderStr != null && !addGenderStr.trim().isEmpty()) {
                        String genderTrim = addGenderStr.trim();
                        if ("男".equals(genderTrim)) {
                            addGenderCode = 1;
                        } else if ("女".equals(genderTrim)) {
                            addGenderCode = 2;
                        } else {
                            logger.warn("msgId:{} 性别值异常：{}，使用默认值0", msgId, addGenderStr);
                        }
                    } else {
                        logger.warn("msgId:{} 性别字段为空，使用默认值0", msgId);
                    }
                    requestParam.put("gender", addGenderCode);// 性别（0/1等，按第三方要求）
                    requestParam.put("phoneNumber",
                            (personData.getPhoneNumber() != null && !personData.getPhoneNumber().trim().isEmpty())
                                    ? personData.getPhoneNumber().trim()
                                    : "");
                    requestParam.put("deviceId", StringUtils.isNotBlank(personData.getSafetyHelmetId()) ? personData.getSafetyHelmetId().trim() : "");
                    requestParam.put("identityCard", StringUtils.isNotBlank(personData.getIdentityCard()) ? personData.getIdentityCard().trim() : "");
                    // 发送POST请求

                    System.out.println("================================" + requestParam.toJSONString() + "=======================================");
                    System.out.println("================================" + requestParam.toJSONString() + "=======================================");
                    System.out.println("================================" + requestParam.toJSONString() + "=======================================");

                    result = sendPostRequest(thirdPartyBaseUrl+thirdPartyPersonBasePath, requestParam);
                    break;

                case "PERSON_EDIT": // 编辑人员 - 调用PUT /api/Open/Personnel
                    // 构造更新人员请求参数（和创建一致，第三方要求相同）
                    requestParam.put("syncId", personData.getId()); // 第三方要求的唯一标识
                    requestParam.put("name", personData.getName());     // 姓名
                    String editGenderStr = personData.getGender();
                    int editGenderCode = 0; // 默认未知性别
                    if (editGenderStr != null && !editGenderStr.trim().isEmpty()) {
                        String editGenderTrim = editGenderStr.trim();
                        if ("男".equals(editGenderTrim)) {
                            editGenderCode = 1;
                        } else if ("女".equals(editGenderTrim)) {
                            editGenderCode = 2;
                        } else {
                            logger.warn("msgId:{} 性别值异常：{}，使用默认值0", msgId, editGenderStr);
                        }
                    } else {
                        logger.warn("msgId:{} 性别字段为空，使用默认值0", msgId);
                    }
                    requestParam.put("gender", editGenderCode);// 性别（0/1等，按第三方要求）
                    requestParam.put("phoneNumber",
                            (personData.getPhoneNumber() != null && !personData.getPhoneNumber().trim().isEmpty())
                                    ? personData.getPhoneNumber().trim()
                                    : "");
                    requestParam.put("deviceId", StringUtils.isNotBlank(personData.getSafetyHelmetId()) ? personData.getSafetyHelmetId().trim() : "");
                    requestParam.put("identityCard", StringUtils.isNotBlank(personData.getIdentityCard()) ? personData.getIdentityCard().trim() : "");
                    // 发送PUT请求
                    System.out.println("================================" + requestParam.toJSONString() + "=======================================");
                    System.out.println("================================" + requestParam.toJSONString() + "=======================================");
                    System.out.println("================================" + requestParam.toJSONString() + "=======================================");
                    result = sendPutRequest(thirdPartyBaseUrl+thirdPartyPersonBasePath, requestParam);
                    break;

                case "PERSON_BIND_HELMET": // 绑定安全帽 - 调用POST /api/Open/Personnel/Bind
                    // 构造绑定请求参数
                    requestParam.put("deviceId", personData.getSafetyHelmetId()); // 安全帽设备ID
                    requestParam.put("syncId", personData.getId());     // 人员唯一标识
                    // 发送POST请求
                    result = sendPostRequest(thirdPartyBaseUrl+thirdPartyPersonBindPath, requestParam);
                    System.out.println("================================" + requestParam.toJSONString() + "=======================================");
                    System.out.println("================================" + requestParam.toJSONString() + "=======================================");
                    System.out.println("================================" + requestParam.toJSONString() + "=======================================");
                    break;

                default: // 未知操作类型
                    logger.error("处理单条消息失败：未知的操作类型，msgId:{}, operateType:{}", msgId, operateType);
                    return false;
            }

            // 校验第三方响应结果
            if (result == null) {
                logger.error("处理单条消息失败：第三方接口无响应，msgId:{}", msgId);
                return false;
            }
            JSONObject resultJson = JSONObject.parseObject(result);
            // ✅ 校验真正成功的字段：isSuccess == true 且 code == 200（可选）
            boolean isSuccess = resultJson.getBoolean("isSuccess") != null
                    && resultJson.getBoolean("isSuccess")
                    && (resultJson.getIntValue("code") == 200); // 可根据业务放宽为只要 isSuccess=true

            if (!isSuccess) {
                String message = resultJson.getString("message");
                logger.error("处理单条消息失败：第三方接口返回失败，msgId:{}, code:{}, message:{}",
                        msgId, resultJson.getIntValue("code"), message != null ? message : "无错误信息");
            } else {
                logger.info("处理单条消息成功，msgId:{}", msgId);
            }

            return isSuccess;

        } catch (Exception e) {
            logger.error("处理单条消息异常，personData:{}", msg.getPersonData(), e);
            return false;
        }
    }

    /**
     * 发送POST请求（封装通用POST逻辑）
     *
     * @param url   请求地址
     * @param param 请求参数（JSON）
     * @return 响应体字符串（null表示请求失败）
     */
    private String sendPostRequest(String url, JSONObject param) {
        try {
            // 🔑 步骤1：生成签名所需参数（时间戳、随机串等）
            long timestamp = System.currentTimeMillis();
            String nonce = UUID.randomUUID().toString().replace("-", ""); // 防重放

            // 🔐 步骤2：构造待签名字符串（按第三方要求拼接，示例为：appKey+timestamp+nonce+body）
            String signContent = appKey + timestamp + appSecret + nonce;

            System.out.println("=============signContent===================" + signContent + "=======================================");

            // 3. 计算SHA256签名（纯哈希，无额外密钥）
            String sign = SignatureUtil.sha256(signContent);

            System.out.println("=============sign===================" + sign + "=======================================");
            // 📡 步骤3：构建带签名的请求
            try (HttpResponse response = HttpRequest.post(url)
                    .body(param.toJSONString())
                    .contentType("application/json")
                    .header("AppId", appKey)        // 必选：应用标识
                    .header("Timestamp", String.valueOf(timestamp)) // 必选：时间戳
                    .header("Nonce", nonce)            // 可选：防重放随机数
                    .header("Sign", sign)         // 必选：签名值
                    .setReadTimeout(timeout)
                    .execute()) {

                logger.info("发送POST请求，url:{}, param:{}, headers:{{X-App-Key, X-Timestamp, X-Signature}}",
                        url, param.toJSONString());
                if (response.isOk()) {
                    String body = response.body();
                    // ✅ 新增：校验业务层是否成功
                    if (SignatureUtil.isBusinessSuccess(body)) {
                        return body;
                    } else {
                        logger.error("POST请求业务失败，url:{}, response:{}", url, SignatureUtil.getErrorMessage(body));
                        return null;
                    }
                } else {
                    logger.error("POST请求HTTP失败，url:{}, status:{}, response:{}",
                            url, response.getStatus(), SignatureUtil.getErrorMessage(response.body()));
                    return null;
                }

            }
        } catch (Exception e) {
            logger.error("发送POST请求异常，url:{}, param:{}", url, param.toJSONString(), e);
            return null;
        }
    }


    /**
     * 发送PUT请求（封装通用PUT逻辑，含签名）
     *
     * @param url   请求地址
     * @param param 请求参数（JSON）
     * @return 响应体字符串（null表示请求失败）
     */
    private String sendPutRequest(String url, JSONObject param) {
        try {
            // 🔑 步骤1：生成签名所需参数（时间戳、随机串等）
            long timestamp = System.currentTimeMillis();
            String nonce = UUID.randomUUID().toString().replace("-", ""); // 防重放

            // 🔐 步骤2：构造待签名字符串（注意：此处仍存在安全风险，建议后续改为 AppKey+Timestamp+Nonce+Body）
            String signContent = appKey + timestamp + appSecret + nonce;

            System.out.println("=============signContent===================" + signContent + "=======================================");

            // 3. 计算SHA256签名
            String sign = SignatureUtil.sha256(signContent);
            System.out.println("=============sign===================" + sign + "=======================================");

            // 📡 步骤3：构建带签名的PUT请求
            try (HttpResponse response = HttpRequest.put(url)
                    .body(param.toJSONString())
                    .contentType("application/json")
                    .header("AppId", appKey)        // 必选：应用标识
                    .header("Timestamp", String.valueOf(timestamp)) // 必选：时间戳
                    .header("Nonce", nonce)            // 可选：防重放随机数
                    .header("Sign", sign)              // 必选：签名值
                    .setReadTimeout(timeout)
                    .execute()) {

                logger.info("发送PUT请求，url:{}, param:{}, headers:{{AppId, Timestamp, Nonce, Sign}}",
                        url, param.toJSONString());
                if (response.isOk()) {
                    String body = response.body();
                    // ✅ 新增：校验业务层是否成功
                    if (SignatureUtil.isBusinessSuccess(body)) {
                        return body;
                    } else {
                        logger.error("PUT请求业务失败，url:{}, response:{}", url, SignatureUtil.getErrorMessage(body));
                        return null;
                    }
                } else {
                    logger.error("PUT请求HTTP失败，url:{}, status:{}, response:{}",
                            url, response.getStatus(), SignatureUtil.getErrorMessage(response.body()));
                    return null;
                }

            }
        } catch (Exception e) {
            logger.error("发送PUT请求异常，url:{}, param:{}", url, param.toJSONString(), e);
            return null;
        }
    }


    /**
     * 处理批量人员变更消息
     * 支持场景：批量删除、批量导入、批量完成安全教育、全量同步人员信息
     */
    private boolean handleBatchMsg(MqSendUtil.BatchPersonChangeMsg batchMsg) {
        // 1. 第一层：batchMsg本身为空 → 直接返回成功
        if (batchMsg == null) {
            logger.warn("批量消息反序列化失败：batchMsg为null");
            return true;
        }

        String batchMsgId = batchMsg.getMsgId();
        String operateType = batchMsg.getOperateType();
        List<SwmPerson> personList = batchMsg.getPersonList();
        List<String> personIds = batchMsg.getPersonIds();

        // 2. 第二层：核心元信息为空 → 返回成功（避免重复消费）
        if (StringUtils.isBlank(batchMsgId) || StringUtils.isBlank(operateType)) {
            logger.warn("批量消息缺少核心字段：msgId={}, operateType={}", batchMsgId, operateType);
            return true;
        }

        // 3. 第三层：按操作类型差异化校验业务数据
        boolean isEmptyData = false;
        if (SyncDataOperateTypeEnum.PERSON_BATCH_DELETE.getCode().equals(operateType)) {
            // 批量删除：仅校验personIdList是否为空
            isEmptyData = CollectionUtils.isEmpty(personIds);
        } else {
            // 批量导入/安全教育/同步：仅校验personList是否为空
            isEmptyData = CollectionUtils.isEmpty(personList);
        }

        // 4. 业务数据为空 → 返回成功
        if (isEmptyData) {
            logger.warn("批量消息无有效业务数据，batchMsgId:{}, operateType:{}, personListSize:{}, personIdListSize:{}",
                    batchMsgId, operateType,
                    personList != null ? personList.size() : 0,
                    personIds != null ? personIds.size() : 0);
            return true;
        }

        // 5. 有效数据 → 处理业务逻辑
        int totalCount = SyncDataOperateTypeEnum.PERSON_BATCH_DELETE.getCode().equals(operateType)
                ? personIds.size() : personList.size();
        logger.info("开始处理批量消息，batchMsgId:{}，operateType:{}，总条数:{}", batchMsgId, operateType, totalCount);

        boolean isSuccess = false;
        try {
            if (SyncDataOperateTypeEnum.PERSON_BATCH_DELETE.getCode().equals(operateType)) {
                isSuccess = handleBatchDelete(batchMsgId, personIds);
            } else {
                isSuccess = handleBatchOperation(batchMsgId, personList, operateType);
            }
        } catch (Exception e) {
            logger.error("处理批量消息时发生异常，batchMsgId:{}，operateType:{}", batchMsgId, operateType, e);
        }
        return isSuccess;
    }

    /**
     * 处理批量同步人员消息（适配第三方批量同步接口）
     */
    private boolean handleBatchOperation(String batchMsgId, List<SwmPerson> personList, String operateType) {
        // 1. 前置核心校验
        if (StringUtils.isBlank(batchMsgId)) {
            logger.error("批量同步失败：batchMsgId为空");
            return false;
        }
        if (personList == null || personList.isEmpty()) {
            logger.error("批量同步失败：人员列表为空，batchMsgId:{}", batchMsgId);
            return false;
        }
        if (StringUtils.isBlank(thirdPartyBaseUrl+thirdPartyPersonBatchSyncPath)) {
            logger.error("批量同步失败：第三方批量接口地址未配置，batchMsgId:{}", batchMsgId);
            return false;
        }

        try {
            // 2. 构造第三方要求的请求参数（核心：datas数组）
            JSONObject batchParam = new JSONObject();
            JSONArray datasArray = new JSONArray(); // 对应接口的datas数组

            // 遍历人员列表，转换为接口要求的格式
            for (SwmPerson person : personList) {
                if (person == null) {
                    logger.warn("批量同步：跳过空人员对象，batchMsgId:{}", batchMsgId);
                    continue;
                }

                JSONObject personObj = new JSONObject();
                // syncId：必传字段，空则跳过当前人员
                String syncId = person.getId();
                if (StringUtils.isBlank(syncId)) {
                    logger.warn("批量同步：人员syncId为空，跳过，batchMsgId:{}", batchMsgId);
                    continue;
                }
                personObj.put("syncId", syncId);

                // 姓名：空则传空字符串
                personObj.put("name", StringUtils.isNotBlank(person.getName()) ? person.getName().trim() : "");

                // 性别：汉字转数字（男→1，女→2，其他/空→0），三目简化
                String genderStr = person.getGender();
                int genderCode = (StringUtils.isNotBlank(genderStr))
                        ? ("男".equals(genderStr.trim()) ? 1 : ("女".equals(genderStr.trim()) ? 2 : 0))
                        : 0;
                personObj.put("gender", genderCode);

                // 手机号：空则传空字符串，非空去空格
                personObj.put("phoneNumber", StringUtils.isNotBlank(person.getPhoneNumber()) ? person.getPhoneNumber().trim() : "");

                // 设备id deviceId
                personObj.put("deviceId", StringUtils.isNotBlank(person.getSafetyHelmetId()) ? person.getSafetyHelmetId().trim() : "");

                // 身份证号码 identityCard
                personObj.put("identityCard", StringUtils.isNotBlank(person.getIdentityCard()) ? person.getIdentityCard().trim() : "");

                datasArray.add(personObj);
            }

            // 校验转换后的datas数组是否为空
            if (datasArray.isEmpty()) {
                logger.error("批量同步失败：转换后datas数组为空，batchMsgId:{}", batchMsgId);
                return false;
            }

            // 最终请求参数（仅保留接口要求的datas字段）
            batchParam.put("datas", datasArray);
            logger.info("构造批量同步请求参数，batchMsgId:{}, datas:{}", batchMsgId, batchParam.toJSONString());

            System.out.println("================================" + batchParam.toJSONString() + "=======================================");
            System.out.println("================================" + batchParam.toJSONString() + "=======================================");
            System.out.println("================================" + batchParam.toJSONString() + "=======================================");

            // 3. 调用第三方批量同步接口（POST + JSON格式）
            String result = sendPostRequest(thirdPartyBaseUrl+thirdPartyPersonBatchSyncPath, batchParam);

            // 4. 响应校验（简洁版）
            boolean isSuccess = (result != null && "success".equals(JSONObject.parseObject(result).getString("code")));
            if (isSuccess) {
                logger.info("批量同步人员信息成功，batchMsgId:{}", batchMsgId);
            } else {
                logger.error("批量同步人员信息失败，batchMsgId:{}, result:{}", batchMsgId, result);
            }
            return isSuccess;

        } catch (Exception e) {
            logger.error("调用批量同步接口异常，batchMsgId:{}, personList:{}", batchMsgId, personList, e);
            return false;
        }
    }

    private boolean handleBatchDelete(String batchMsgId, List<String> personIds) {
        // 1. 前置校验
        if (StringUtils.isBlank(batchMsgId)) {
            logger.error("批量删除失败：batchMsgId为空");
            return false;
        }
        if (CollectionUtils.isEmpty(personIds)) {
            logger.error("批量删除失败：人员ID列表为空，batchMsgId:{}", batchMsgId);
            return false;
        }

        try {
            // 2. 过滤空值
            List<String> validSyncIds = personIds.stream()
                    .filter(StringUtils::isNotBlank)
                    .collect(Collectors.toList());
            if (CollectionUtils.isEmpty(validSyncIds)) {
                logger.error("批量删除失败：人员ID列表过滤空值后为空，batchMsgId:{}", batchMsgId);
                return false;
            }

            // 3. 构造请求体
            String requestBody = JSON.toJSONString(validSyncIds);
            logger.info("构造批量删除请求体，batchMsgId:{}, 请求体:{}", batchMsgId, requestBody);
            System.out.println("================================" + requestBody + "=======================================");

            // 4. 调用通用 DELETE 方法（已含签名）
            String result = sendDeleteRequest(thirdPartyBaseUrl+thirdPartyPersonBatchDeletePath, requestBody);

            // 5. 响应校验
            boolean isSuccess = result != null && "success".equals(JSONObject.parseObject(result).getString("code"));
            if (isSuccess) {
                logger.info("批量删除人员成功，batchMsgId:{}, syncIds:{}", batchMsgId, validSyncIds);
            } else {
                logger.error("批量删除人员失败，batchMsgId:{}, syncIds:{}, result:{}", batchMsgId, validSyncIds, result);
            }
            return isSuccess;

        } catch (Exception e) {
            logger.error("调用批量删除接口异常，batchMsgId:{}, personIds:{}", batchMsgId, personIds, e);
            return false;
        }
    }


    /**
     * 发送DELETE请求（封装通用DELETE逻辑，含签名）
     *
     * @param url  请求地址
     * @param body 请求体（JSON字符串，如 ["id1","id2"]）
     * @return 响应体字符串（null 表示失败）
     */
    private String sendDeleteRequest(String url, String body) {
        try {
            long timestamp = System.currentTimeMillis();
            String nonce = UUID.randomUUID().toString().replace("-", "");

            // 构造待签名字符串（注意：此处仍建议后续改为含 body 的拼接）
            String signContent = appKey + timestamp + appSecret + nonce;
            System.out.println("=============signContent (DELETE)===================" + signContent + "=======================================");

            String sign = SignatureUtil.sha256(signContent);
            System.out.println("=============sign (DELETE)===================" + sign + "=======================================");

            try (HttpResponse response = HttpRequest.delete(url)
                    .body(body)
                    .contentType("application/json")
                    .header("AppId", appKey)
                    .header("Timestamp", String.valueOf(timestamp))
                    .header("Nonce", nonce)
                    .header("Sign", sign)
                    .setReadTimeout(timeout)
                    .execute()) {

                logger.info("发送DELETE请求，url:{}, bodyLength:{}", url, body.length());
                if (response.isOk()) {
                    String message = response.body();
                    // ✅ 新增：校验业务层是否成功
                    if (SignatureUtil.isBusinessSuccess(message)) {
                        return body;
                    } else {
                        logger.error("请求业务失败，url:{}, response:{}", url, SignatureUtil.getErrorMessage(message));
                        return null;
                    }
                } else {
                    logger.error("POST请求HTTP失败，url:{}, status:{}, response:{}",
                            url, response.getStatus(), SignatureUtil.getErrorMessage(response.body()));
                    return null;
                }

            }
        } catch (Exception e) {
            logger.error("发送DELETE请求异常，url:{}, body:{}", url, body, e);
            return null;
        }
    }


}