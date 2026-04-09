package com.jeesite.modules.swm.mq.consumer;

import com.alibaba.cloud.commons.lang.StringUtils;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.nacos.common.utils.CollectionUtils;
import com.jeesite.modules.config.RabbitMqConfig;
import com.jeesite.modules.enums.SyncDataOperateTypeEnum;
import com.jeesite.modules.entity.SwmBeaconStation;
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

@Component
public class BeaconChangeConsumer {
    private static final Logger logger = LoggerFactory.getLogger(BeaconChangeConsumer.class);

    // 第三方信标接口配置
    private static final String THIRD_PARTY_BEACON_URL = "http://第三方服务器/notify/beaconChange";
    private static final String THIRD_PARTY_BEACON_BATCH_URL = "http://第三方服务器/notify/beaconBatchChange";
    private static final int TIMEOUT = 5000;

    /**
     * 处理信标MQ消息（单条+批量）
     */
    @RabbitListener(queues = RabbitMqConfig.BEACON_CHANGE_QUEUE)
    public void consumeBeaconMsg(Message message, Channel channel) throws IOException {
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        boolean isSuccess = false;

        try {
            // 1. 读取并解析MQ消息
            String jsonStr = new String(message.getBody(), StandardCharsets.UTF_8);
            String msgType = (String) message.getMessageProperties().getHeader("msgType");
            logger.info("接收到信标JSON消息，msgType:{}，内容:{}", msgType, jsonStr);

            // 2. 根据消息类型处理
            if ("BaseBeaconMsg".equals(msgType)) {
                // 单条信标消息（新增/编辑/删除）
                MqSendUtil.BaseBeaconMsg singleMsg = JSONObject.parseObject(jsonStr, MqSendUtil.BaseBeaconMsg.class);
                isSuccess = handleSingleMsg(singleMsg);
            } else if ("BatchBeaconChangeMsg".equals(msgType)) {
                // 批量信标消息（导入/全量同步）
                MqSendUtil.BatchBeaconChangeMsg batchMsg = JSONObject.parseObject(jsonStr, MqSendUtil.BatchBeaconChangeMsg.class);
                isSuccess = handleBatchMsg(batchMsg);
            } else {
                logger.warn("未知的信标消息类型：{}，消息内容：{}", msgType, jsonStr);
                isSuccess = true; // 未知类型标记成功，避免重复消费
            }

            // 3. MQ消息确认
            if (isSuccess) {
                channel.basicAck(deliveryTag, false);
                logger.info("信标消息消费成功，deliveryTag:{}", deliveryTag);
            } else {
                logger.error("信标消息消费失败，已确认（不重试），deliveryTag:{}，msg:{}", deliveryTag, jsonStr);
            }

        } catch (Exception e) {
            // 异常时确认消息，避免进入死循环
            channel.basicAck(deliveryTag, false);
            logger.error("信标消息消费异常，已确认（不重试），deliveryTag:{}", deliveryTag, e);
        }
    }

    /**
     * 处理单条信标消息（新增/编辑/删除）
     */
    private boolean handleSingleMsg(MqSendUtil.BaseBeaconMsg msg) {
        try {
            JSONObject param = new JSONObject();
            param.put("msgId", msg.getMsgId());
            if (msg.getOperateType().equals(SyncDataOperateTypeEnum.HELMET_DELETE.getCode())){
                param.put("beaconId", msg.getBeaconId());
            }
            param.put("operateType", msg.getOperateType());
            param.put("changeData", msg.getBeaconData());
            param.put("operateTime", msg.getOperateTime());

            System.out.println("================================"+param.toJSONString()+"=======================================");
            System.out.println("================================"+param.toJSONString()+"=======================================");
            System.out.println("================================"+param.toJSONString()+"=======================================");

//            String result = HttpUtil.createPost(THIRD_PARTY_PERSON_URL)
//                    .body(param.toJSONString()) // 替换 setBody -> body
//                    .contentType("application/json") // 必须设置 JSON 格式
//                    // 方式1：推荐（Hutool 5.x+）：分别设置连接超时和读取超时（更灵活）
//                    .setReadTimeout(TIMEOUT)       // 读取超时
//                    // 方式2（兼容旧版本）：一次性设置超时（连接+读取）
//                    // .timeout(THIRD_PARTY_TIMEOUT)
//                    .execute()
//                    .body();
//            return "success".equals(JSONObject.parseObject(result).getString("code"));

            return true;
        } catch (Exception e) {
            logger.error("处理单条信标消息失败，beaconId:{}", msg.getBeaconData(), e);
            return false;
        }
    }

    /**
     * 处理批量信标消息
     * 支持场景：BEACON_IMPORT（导入）、BEACON_FULL_SYNC（全量同步）
     */
    private boolean handleBatchMsg(MqSendUtil.BatchBeaconChangeMsg batchMsg) {
        // 1. 第一层：batchMsg本身为空 → 直接返回成功
        if (batchMsg == null || CollectionUtils.isEmpty(batchMsg.getBeaconList())) {
            logger.warn("批量信标消息为空，无需处理，batchMsgId:{}", batchMsg != null ? batchMsg.getMsgId() : "null");
            return true;
        }

        String batchMsgId = batchMsg.getMsgId();
        String operateType = batchMsg.getOperateType();
        List<SwmBeaconStation> beaconList = batchMsg.getBeaconList();

        // 2. 第二层：核心元信息为空 → 返回成功（避免重复消费）
        if (StringUtils.isBlank(batchMsgId) || StringUtils.isBlank(operateType)) {
            logger.warn("批量消息缺少核心字段：msgId={}, operateType={}", batchMsgId, operateType);
            return true;
        }

        // 3. 第三层：按操作类型差异化校验业务数据
        boolean isEmptyData = false;
        isEmptyData = CollectionUtils.isEmpty(beaconList);

        // 4. 业务数据为空 → 返回成功
        if (isEmptyData) {
            logger.warn("批量消息无有效业务数据，batchMsgId:{}, operateType:{}, beaconListSize:{}",
                    batchMsgId, operateType,
                    beaconList != null ? beaconList.size() : 0);
            return true;
        }

        // 5. 有效数据 → 处理业务逻辑
        int totalCount = beaconList.size();
        logger.info("开始处理批量消息，batchMsgId:{}，operateType:{}，总条数:{}", batchMsgId, operateType, totalCount);

        boolean isSuccess = false;
        try {
            isSuccess = handleBatchOperation(batchMsgId, beaconList,operateType);
        } catch (Exception e) {
            logger.error("处理批量消息时发生异常，batchMsgId:{}，operateType:{}", batchMsgId, operateType, e);
        }

        return isSuccess;
    }


    // ===================== 子方法：全量同步信标 =====================
    private boolean handleBatchOperation(String batchMsgId, List<SwmBeaconStation> beaconList, String operateType) {
        // 优先调用第三方批量全量同步接口
        if (StringUtils.isNotBlank(THIRD_PARTY_BEACON_BATCH_URL)) {
            try {
                JSONObject batchParam = new JSONObject();
                batchParam.put("msgId", batchMsgId);
                batchParam.put("operateType", operateType);
                batchParam.put("changeData", beaconList);
                batchParam.put("operateTime", System.currentTimeMillis());

                System.out.println("================================"+batchParam.toJSONString()+"=======================================");
                System.out.println("================================"+batchParam.toJSONString()+"=======================================");
                System.out.println("================================"+batchParam.toJSONString()+"=======================================");

//              String result = HttpUtil.createPost(THIRD_PARTY_PERSON_URL)
//                    .body(batchParam.toJSONString()) // 替换 setBody -> body
//                    .contentType("application/json") // 必须设置 JSON 格式
//                    // 方式1：推荐（Hutool 5.x+）：分别设置连接超时和读取超时（更灵活）
//                    .setReadTimeout(TIMEOUT)       // 读取超时
//                    // 方式2（兼容旧版本）：一次性设置超时（连接+读取）
//                    // .timeout(THIRD_PARTY_TIMEOUT)
//                    .execute()
//                    .body();
//            return "success".equals(JSONObject.parseObject(result).getString("code"));
            } catch (Exception e) {
                logger.error("调用第三方全量同步信标接口异常，batchMsgId:{}", batchMsgId, e);
            }
        }
        return true;
    }
}
