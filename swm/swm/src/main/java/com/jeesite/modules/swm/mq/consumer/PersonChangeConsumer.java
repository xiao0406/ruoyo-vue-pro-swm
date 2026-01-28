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
import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.IOException;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class PersonChangeConsumer {
    private static final Logger logger = LoggerFactory.getLogger(PersonChangeConsumer.class);
//    private static final String THIRD_PARTY_PERSON_URL = "http://第三方服务器/notify/personChange";
    // 新增：批量接口（如果第三方支持，优先调用批量接口，效率更高）
    private static final String THIRD_PARTY_PERSON_BATCH_URL = "http://第三方服务器/notify/personBatchChange";

    // 第三方接口配置
    private static final String THIRD_PARTY_BASE_URL = "/api/Open/Personnel/";
    private static final String THIRD_PARTY_PERSON_URL = THIRD_PARTY_BASE_URL + "/api/Open/Personnel";
    private static final String THIRD_PARTY_PERSON_BIND_URL = THIRD_PARTY_BASE_URL + "/api/Open/Personnel/Bind";
    private static final String THIRD_PARTY_PERSON_DELETE_URL = THIRD_PARTY_BASE_URL + "/api/Open/Personnel";


    private static final int TIMEOUT = 5000;

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
                logger.error("消费消息失败，已确认（不重试），deliveryTag:{}，msg:{}", deliveryTag, jsonStr);
            }

        } catch (Exception e) {
            logger.error("消费消息异常，已确认（不重试），deliveryTag:{}", deliveryTag, e);
        }
    }

//    /**
//     * 处理单条人员变更消息
//     * 兼容场景：新增、编辑、绑定安全帽、处理离职
//     */
//    private boolean handleSingleMsg(MqSendUtil.BasePersonMsg msg) {
//        try {
//            JSONObject param = new JSONObject();
//            param.put("msgId", msg.getMsgId());
//            param.put("operateType", msg.getOperateType());
//            param.put("changeData", msg.getPersonData());
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
//            logger.error("处理单条消息失败，personData:{}", msg.getPersonData(), e);
//            return false;
//        }
//    }

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
                            (personData.getPhoneNumber()!= null && !personData.getPhoneNumber().trim().isEmpty())
                                    ? personData.getPhoneNumber().trim()
                                    : "");
                    requestParam.put("deviceId", StringUtils.isNotBlank(personData.getSafetyHelmetId()) ? personData.getSafetyHelmetId().trim() : "");
                    requestParam.put("identityCard", StringUtils.isNotBlank(personData.getIdentityCard()) ? personData.getIdentityCard().trim() : "");
                    // 发送POST请求
//                    result = sendPostRequest(THIRD_PARTY_PERSON_URL, requestParam);
                System.out.println("================================"+requestParam.toJSONString()+"=======================================");
                System.out.println("================================"+requestParam.toJSONString()+"=======================================");
                System.out.println("================================"+requestParam.toJSONString()+"=======================================");
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
                            (personData.getPhoneNumber()!= null && !personData.getPhoneNumber().trim().isEmpty())
                                    ? personData.getPhoneNumber().trim()
                                    : "");
                    requestParam.put("deviceId", StringUtils.isNotBlank(personData.getSafetyHelmetId()) ? personData.getSafetyHelmetId().trim() : "");
                    requestParam.put("identityCard", StringUtils.isNotBlank(personData.getIdentityCard()) ? personData.getIdentityCard().trim() : "");
                    // 发送PUT请求
//                    result = sendPutRequest(THIRD_PARTY_PERSON_URL, requestParam);
                    System.out.println("================================"+requestParam.toJSONString()+"=======================================");
                    System.out.println("================================"+requestParam.toJSONString()+"=======================================");
                    System.out.println("================================"+requestParam.toJSONString()+"=======================================");
                    break;

                case "PERSON_BIND_HELMET": // 绑定安全帽 - 调用POST /api/Open/Personnel/Bind
                    // 构造绑定请求参数
                    requestParam.put("deviceId", personData.getSafetyHelmetId()); // 安全帽设备ID
                    requestParam.put("syncId", personData.getId());     // 人员唯一标识
                    // 发送POST请求
//                    result = sendPostRequest(THIRD_PARTY_PERSON_BIND_URL, requestParam);
                    System.out.println("================================"+requestParam.toJSONString()+"=======================================");
                    System.out.println("================================"+requestParam.toJSONString()+"=======================================");
                    System.out.println("================================"+requestParam.toJSONString()+"=======================================");
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
            // 假设第三方返回code为"success"表示成功（根据实际第三方文档调整）
            boolean isSuccess = "success".equals(resultJson.getString("code"));
            if (!isSuccess) {
                logger.error("处理单条消息失败：第三方接口返回失败，msgId:{}, result:{}", msgId, result);
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
     * @param url    请求地址
     * @param param  请求参数（JSON）
     * @return 响应体字符串（null表示请求失败）
     */
    private String sendPostRequest(String url, JSONObject param) {
        try (HttpResponse response = HttpRequest.post(url)
                .body(param.toJSONString())          // 设置JSON请求体
                .contentType("application/json")     // 设置Content-Type为JSON
                .setReadTimeout(TIMEOUT)             // 读取超时
                .execute()) {                        // 执行请求

            logger.info("发送POST请求，url:{}, param:{}", url, param.toJSONString());
            if (response.isOk()) { // 状态码200-299
                return response.body();
            } else {
                logger.error("POST请求失败，url:{}, status:{}, error:{}", url, response.getStatus(), response.body());
                return null;
            }
        } catch (Exception e) {
            logger.error("发送POST请求异常，url:{}, param:{}", url, param.toJSONString(), e);
            return null;
        }
    }

    /**
     * 发送PUT请求（封装通用PUT逻辑）
     *
     * @param url    请求地址
     * @param param  请求参数（JSON）
     * @return 响应体字符串（null表示请求失败）
     */
    private String sendPutRequest(String url, JSONObject param) {
        try (HttpResponse response = HttpRequest.put(url)
                .body(param.toJSONString())          // 设置JSON请求体
                .contentType("application/json")     // 设置Content-Type为JSON
                .setReadTimeout(TIMEOUT)             // 读取超时
                .execute()) {                        // 执行请求

            logger.info("发送PUT请求，url:{}, param:{}", url, param.toJSONString());
            if (response.isOk()) { // 状态码200-299
                return response.body();
            } else {
                logger.error("PUT请求失败，url:{}, status:{}, error:{}", url, response.getStatus(), response.body());
                return null;
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
                    personIds != null ?  personIds.size() : 0);
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
        if (StringUtils.isBlank(THIRD_PARTY_PERSON_BATCH_URL)) {
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

            System.out.println("================================"+batchParam.toJSONString()+"=======================================");
            System.out.println("================================"+batchParam.toJSONString()+"=======================================");
            System.out.println("================================"+batchParam.toJSONString()+"=======================================");

            // 3. 调用第三方批量同步接口（POST + JSON格式）
            String result = null ;
//            String result = HttpRequest.post(THIRD_PARTY_PERSON_BATCH_URL)
//                    .body(batchParam.toJSONString())          // 设置JSON请求体
//                    .contentType("application/json")         // 严格匹配接口的Content-Type
//                    .setReadTimeout(TIMEOUT)                 // 读取超时
//                    .execute()
//                    .body();

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

    /**
     * 处理批量删除人员消息（单/批量统一调用DELETE接口，无降级逻辑）
     */
    private boolean handleBatchDelete(String batchMsgId, List<String> personIds) {
        // 1. 前置核心校验：batchMsgId非空 + 人员ID列表非空
        if (StringUtils.isBlank(batchMsgId)) {
            logger.error("批量删除失败：batchMsgId为空");
            return false;
        }
        if (CollectionUtils.isEmpty(personIds)) {
            logger.error("批量删除失败：人员ID列表为空，batchMsgId:{}", batchMsgId);
            return false;
        }

        try {
            // 2. 过滤空值：避免数组中有空字符串/null，保证请求体格式合法
            List<String> validSyncIds = personIds.stream()
                    .filter(StringUtils::isNotBlank)
                    .collect(Collectors.toList());

            // 二次校验：过滤后为空则返回失败
            if (CollectionUtils.isEmpty(validSyncIds)) {
                logger.error("批量删除失败：人员ID列表过滤空值后为空，batchMsgId:{}", batchMsgId);
                return false;
            }

            // 3. 构造请求体：纯JSON数组（["1","3","6"]），适配接口要求
            String requestBody = JSON.toJSONString(validSyncIds);
            logger.info("构造批量删除请求体，batchMsgId:{}, 请求体:{}", batchMsgId, requestBody);
            System.out.println("================================"+requestBody+"=======================================");
            System.out.println("================================"+requestBody+"=======================================");
            System.out.println("================================"+requestBody+"=======================================");

            // 4. 调用第三方DELETE接口（核心适配：JSON请求体 + application/json格式）
            String result = HttpRequest.delete(THIRD_PARTY_PERSON_DELETE_URL)
                    .body(requestBody) // DELETE请求设置JSON请求体（Hutool支持）
                    .contentType("application/json") // 必须指定JSON格式
                    .setReadTimeout(TIMEOUT)
                    .execute()
                    .body();

            // 5. 响应校验：判断接口返回是否成功
            boolean isSuccess = false;
            if (result != null) {
                try {
                    // 适配接口返回的JSON格式（取code字段判断）
                    String code = JSONObject.parseObject(result).getString("code");
                    isSuccess = "success".equals(code);
                } catch (Exception e) {
                    logger.warn("批量删除接口响应格式异常，无法解析code字段，batchMsgId:{}, result:{}", batchMsgId, result);
                    isSuccess = false;
                }
            }

            // 6. 日志输出结果
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
     * 处理批量导入、批量完成安全教育、全量同步人员信息
     */
//    private boolean handleBatchOperation(String batchMsgId, List<SwmPerson> personList, String operateType) {
//        // 优先调用第三方批量全量同步接口（效率最高）
//        if (StringUtils.isNotBlank(THIRD_PARTY_PERSON_BATCH_URL)) {
//            try {
//                JSONObject batchParam = new JSONObject();
//                batchParam.put("msgId", batchMsgId);
//                batchParam.put("operateType", operateType);
//                batchParam.put("changeData", personList);
//                batchParam.put("operateTime", System.currentTimeMillis());
//
//                System.out.println("================================"+batchParam.toJSONString()+"=======================================");
//                System.out.println("================================"+batchParam.toJSONString()+"=======================================");
//                System.out.println("================================"+batchParam.toJSONString()+"=======================================");
//
////              String result = HttpUtil.createPost(THIRD_PARTY_PERSON_URL)
////                    .body(batchParam.toJSONString()) // 替换 setBody -> body
////                    .contentType("application/json") // 必须设置 JSON 格式
////                    // 方式1：推荐（Hutool 5.x+）：分别设置连接超时和读取超时（更灵活）
////                    .setReadTimeout(TIMEOUT)       // 读取超时
////                    // 方式2（兼容旧版本）：一次性设置超时（连接+读取）
////                    // .timeout(THIRD_PARTY_TIMEOUT)
////                    .execute()
////                    .body();
////            return "success".equals(JSONObject.parseObject(result).getString("code"));
//            } catch (Exception e) {
//                logger.error("调用第三方批量操作接口异常，batchMsgId:{}", batchMsgId, e);
//            }
//        }
//        return true;
//    }


}