package com.jeesite.modules.swm.util;

import cn.hutool.core.util.IdUtil;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.nacos.common.utils.CollectionUtils;
import com.alibaba.nacos.common.utils.StringUtils;
import com.jeesite.common.lang.ObjectUtils;
import com.jeesite.modules.config.RabbitMqConfig;
import com.jeesite.modules.entity.SwmBeaconStation;
import com.jeesite.modules.swm.entity.SwmHelmetDevice;
import com.jeesite.modules.swm.entity.SwmPerson;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

/**
 * MQ消息发送通用工具类
 */
@Component
@Slf4j
public class MqSendUtil {
    @Autowired
    private RabbitTemplate rabbitTemplate;

    /**
     * 发送模块变更消息
     * @param exchange 交换机
     * @param routingKey 路由键
     * @param bizId 业务ID（设备ID/信标ID）
     * @param operateType 操作类型
     * @param changeData 变更数据
     * @param <T> 业务ID类型
     */
    public <T extends Serializable> void sendChangeMsg(String exchange, String routingKey, T bizId, String operateType, Map<String, Object> changeData) {
        BaseChangeMsg<T> msg = new BaseChangeMsg<>();
        msg.setMsgId(IdUtil.simpleUUID());
        msg.setBizId(bizId);
        msg.setOperateType(operateType);
        msg.setChangeData(changeData);
        msg.setOperateTime(System.currentTimeMillis());

        rabbitTemplate.convertAndSend(exchange, routingKey, msg, message -> {
            message.getMessageProperties().setExpiration("3600000"); // 1小时过期
            return message;
        });
    }

    // ========== 发送单条人员改变信息==========
    public void sendPersonSingleChangeMsg(String operateType, SwmPerson fullPerson) {
        // 构建基础消息体（仅封装操作元信息，核心数据直接用实体）
        BasePersonMsg baseMsg = new BasePersonMsg();
        baseMsg.setMsgId(IdUtil.simpleUUID());
        baseMsg.setOperateType(operateType);
        baseMsg.setOperateTime(System.currentTimeMillis());
        baseMsg.setPersonData(fullPerson); // 直接放SwmPerson实体，无需Map

        // 直接序列化实体（FastJSON会自动处理实体转JSON）
        String jsonStr = JSONObject.toJSONString(baseMsg);

        // 后续MQ发送逻辑不变
        MessageProperties properties = new MessageProperties();
        properties.setContentType(MessageProperties.CONTENT_TYPE_JSON);
        properties.setContentEncoding(StandardCharsets.UTF_8.name());
        properties.setHeader("msgType", "BasePersonMsg");

        Message message = new Message(jsonStr.getBytes(StandardCharsets.UTF_8), properties);
        rabbitTemplate.send(RabbitMqConfig.PERSON_CHANGE_EXCHANGE,
                RabbitMqConfig.PERSON_CHANGE_ROUTING_KEY, message);
    }

    /**
     * 批量删除人员 - 发送MQ消息（仅传ID列表）
     */
    public void sendBatchDeleteMqMessage(String operateType, List<String> deletePersonIds) {
        if (CollectionUtils.isEmpty(deletePersonIds)) {
            log.warn("批量删除MQ消息发送失败：待删除ID列表为空");
            return;
        }

        BatchPersonChangeMsg batchMsg = new BatchPersonChangeMsg();
        batchMsg.setMsgId(IdUtil.simpleUUID());
        batchMsg.setOperateType(operateType);
        batchMsg.setPersonIds(deletePersonIds);
        batchMsg.setTotal(deletePersonIds.size());
        batchMsg.setOperateTime(System.currentTimeMillis());

        // 发送MQ（序列化JSON字符串）
        String jsonStr = JSONObject.toJSONString(batchMsg);
        MessageProperties properties = new MessageProperties();
        properties.setContentType(MessageProperties.CONTENT_TYPE_JSON);
        properties.setHeader("msgType", "BatchPersonChangeMsg");

        Message message = new Message(jsonStr.getBytes(StandardCharsets.UTF_8), properties);
        rabbitTemplate.send(RabbitMqConfig.PERSON_CHANGE_EXCHANGE,
                RabbitMqConfig.PERSON_CHANGE_ROUTING_KEY, message);

        log.info("批量删除MQ消息发送成功，msgId：{}，删除ID数：{}",
                batchMsg.msgId, deletePersonIds.size());
    }

    // ==========发送人员批量信息 ==========
    public void sendPersonBatchChangeMsg(String operateType, List<SwmPerson> personList) {
        BatchPersonChangeMsg batchMsg = new BatchPersonChangeMsg();
        batchMsg.setMsgId(IdUtil.simpleUUID());
        batchMsg.setOperateType(operateType);
        batchMsg.setPersonList(personList);
        batchMsg.setTotal(personList.size());
        batchMsg.setOperateTime(System.currentTimeMillis());

        // 手动序列化为JSON字符串
        String jsonStr = JSONObject.toJSONString(batchMsg);

        // 构建消息属性
        MessageProperties properties = new MessageProperties();
        properties.setContentType(MessageProperties.CONTENT_TYPE_JSON);
        properties.setContentEncoding(StandardCharsets.UTF_8.name());
        properties.setHeader("msgType", "BatchPersonChangeMsg"); // 标记消息类型-人员批量消息

        // 构建并发送Message
        Message message = new Message(jsonStr.getBytes(StandardCharsets.UTF_8), properties);
        rabbitTemplate.send(
                RabbitMqConfig.PERSON_CHANGE_EXCHANGE,
                RabbitMqConfig.PERSON_CHANGE_ROUTING_KEY,
                message
        );
        log.info("人员批量消息==发送MQ消息成功，operateType:{}，总条数:{}", operateType, personList.size());
    }

    // ========== 发送单条设备改变信息==========
    public void sendDeviceSingleChangeMsg(String operateType, SwmHelmetDevice fullDevice) {
        if (ObjectUtils.isEmpty(fullDevice)) {
            log.warn("发送单条设备MQ消息发送失败：待处理设备D为空");
            return;
        }
        // 构建基础消息体（仅封装操作元信息，核心数据直接用实体）
        BaseDeviceMsg baseMsg = new BaseDeviceMsg();
        baseMsg.setMsgId(IdUtil.simpleUUID());
        baseMsg.setOperateType(operateType);
        baseMsg.setOperateTime(System.currentTimeMillis());
        baseMsg.setDeviceData(fullDevice);

        // 直接序列化实体（FastJSON会自动处理实体转JSON）
        String jsonStr = JSONObject.toJSONString(baseMsg);

        // 后续MQ发送逻辑不变
        MessageProperties properties = new MessageProperties();
        properties.setContentType(MessageProperties.CONTENT_TYPE_JSON);
        properties.setContentEncoding(StandardCharsets.UTF_8.name());
        properties.setHeader("msgType", "BaseDeviceMsg");

        Message message = new Message(jsonStr.getBytes(StandardCharsets.UTF_8), properties);
        rabbitTemplate.send(RabbitMqConfig.DEVICE_CHANGE_EXCHANGE,
                RabbitMqConfig.DEVICE_CHANGE_ROUTING_KEY, message);
    }

    /**
     * 单个删除设备 - 发送MQ消息
     */
    public void sendDeviceDeleteMqMessage(String operateType, String deviceId) {
        if (StringUtils.isEmpty(deviceId)) {
            log.warn("单个删除设备MQ消息发送失败：待删除ID为空");
            return;
        }

        BaseDeviceMsg baseMsg = new BaseDeviceMsg();
        baseMsg.setMsgId(IdUtil.simpleUUID());
        baseMsg.setOperateType(operateType);
        baseMsg.setOperateTime(System.currentTimeMillis());
        baseMsg.setDeviceId(deviceId);

        // 发送MQ（序列化JSON字符串）
        String jsonStr = JSONObject.toJSONString(baseMsg);
        MessageProperties properties = new MessageProperties();
        properties.setContentType(MessageProperties.CONTENT_TYPE_JSON);
        properties.setHeader("msgType", "BaseDeviceMsg");

        Message message = new Message(jsonStr.getBytes(StandardCharsets.UTF_8), properties);
        rabbitTemplate.send(RabbitMqConfig.DEVICE_CHANGE_EXCHANGE,
                RabbitMqConfig.DEVICE_CHANGE_ROUTING_KEY, message);

        log.info("批量删除MQ消息发送成功，msgId：{}", baseMsg.msgId);
    }

    // ==========发送设备批量信息 ==========
    public void sendDeviceBatchChangeMsg(String operateType, List<SwmHelmetDevice> deviceList) {
        if (CollectionUtils.isEmpty(deviceList)) {
            log.warn("设备批量信息发送MQ失败：待发送设备列表为空");
            return;
        }
        BatchDeviceChangeMsg batchMsg = new BatchDeviceChangeMsg();
        batchMsg.setMsgId(IdUtil.simpleUUID()); // 复用hutool的UUID生成
        batchMsg.setOperateType(operateType);
        batchMsg.setDeviceList(deviceList);
        batchMsg.setTotal(deviceList.size());
        batchMsg.setOperateTime(System.currentTimeMillis());

        // 核心：手动序列化为JSON字符串（保持原有风格）
        String jsonStr = JSONObject.toJSONString(batchMsg);

        // 构建消息属性，和人员消息保持一致
        MessageProperties properties = new MessageProperties();
        properties.setContentType(MessageProperties.CONTENT_TYPE_JSON);
        properties.setContentEncoding(StandardCharsets.UTF_8.name());
        properties.setHeader("msgType", "BatchDeviceChangeMsg"); // 标记消息类型-设备批量消息

        // 构建Message对象
        Message message = new Message(jsonStr.getBytes(StandardCharsets.UTF_8), properties);

        rabbitTemplate.send(
                RabbitMqConfig.DEVICE_CHANGE_EXCHANGE,
                RabbitMqConfig.DEVICE_CHANGE_ROUTING_KEY,
                message
        );
        log.info("设备==发送批量MQ消息成功，operateType:{}，设备总数:{}", operateType, deviceList.size());
    }


    // ========== 发送单条信标改变信息==========
    public void sendBeaconSingleChangeMsg(String operateType, SwmBeaconStation fullBeacon) {
        if (ObjectUtils.isEmpty(fullBeacon)) {
            log.warn("发送单条设备MQ消息发送失败：待处理信标为空");
            return;
        }
        // 构建基础消息体（仅封装操作元信息，核心数据直接用实体）
        BaseBeaconMsg baseMsg = new BaseBeaconMsg();
        baseMsg.setMsgId(IdUtil.simpleUUID());
        baseMsg.setOperateType(operateType);
        baseMsg.setOperateTime(System.currentTimeMillis());
        baseMsg.setBeaconData(fullBeacon);

        // 直接序列化实体（FastJSON会自动处理实体转JSON）
        String jsonStr = JSONObject.toJSONString(baseMsg);

        // 后续MQ发送逻辑不变
        MessageProperties properties = new MessageProperties();
        properties.setContentType(MessageProperties.CONTENT_TYPE_JSON);
        properties.setContentEncoding(StandardCharsets.UTF_8.name());
        properties.setHeader("msgType", "BaseBeaconMsg");

        Message message = new Message(jsonStr.getBytes(StandardCharsets.UTF_8), properties);
        rabbitTemplate.send(RabbitMqConfig.BEACON_CHANGE_EXCHANGE,
                RabbitMqConfig.BEACON_CHANGE_ROUTING_KEY, message);
    }

    /**
     * 单个删除信标 - 发送MQ消息
     */
    public void sendBeaconDeleteMqMessage(String operateType, String id) {
        if (StringUtils.isEmpty(id)) {
            log.warn("单个删除信标MQ消息发送失败：待删除ID为空");
            return;
        }

        BaseBeaconMsg baseMsg = new BaseBeaconMsg();
        baseMsg.setMsgId(IdUtil.simpleUUID());
        baseMsg.setOperateType(operateType);
        baseMsg.setOperateTime(System.currentTimeMillis());
        baseMsg.setBeaconId(id);

        // 发送MQ（序列化JSON字符串）
        String jsonStr = JSONObject.toJSONString(baseMsg);
        MessageProperties properties = new MessageProperties();
        properties.setContentType(MessageProperties.CONTENT_TYPE_JSON);
        properties.setHeader("msgType", "BaseBeaconMsg");

        Message message = new Message(jsonStr.getBytes(StandardCharsets.UTF_8), properties);
        rabbitTemplate.send(RabbitMqConfig.BEACON_CHANGE_EXCHANGE,
                RabbitMqConfig.BEACON_CHANGE_ROUTING_KEY, message);

        log.info("单个删除MQ消息发送成功，msgId：{}", baseMsg.msgId);
    }


    // ==========发送设备批量信息 ==========
    public void sendBeaconBatchChangeMsg(String operateType, List<SwmBeaconStation> beaconStationList) {
        if (CollectionUtils.isEmpty(beaconStationList)) {
            log.warn("信标批量信息发送MQ失败：待发送设备列表为空");
            return;
        }
        BatchBeaconChangeMsg batchMsg = new BatchBeaconChangeMsg();
        batchMsg.setMsgId(IdUtil.simpleUUID()); // 复用hutool的UUID生成
        batchMsg.setOperateType(operateType);
        batchMsg.setBeaconList(beaconStationList);
        batchMsg.setTotal(beaconStationList.size());
        batchMsg.setOperateTime(System.currentTimeMillis());

        // 核心：手动序列化为JSON字符串（保持原有风格）
        String jsonStr = JSONObject.toJSONString(batchMsg);

        // 构建消息属性，和人员消息保持一致
        MessageProperties properties = new MessageProperties();
        properties.setContentType(MessageProperties.CONTENT_TYPE_JSON);
        properties.setContentEncoding(StandardCharsets.UTF_8.name());
        properties.setHeader("msgType", "BatchBeaconChangeMsg"); // 标记消息类型-设备批量消息

        // 构建Message对象
        Message message = new Message(jsonStr.getBytes(StandardCharsets.UTF_8), properties);

        rabbitTemplate.send(
                RabbitMqConfig.BEACON_CHANGE_EXCHANGE,
                RabbitMqConfig.BEACON_CHANGE_ROUTING_KEY,
                message
        );
        log.info("信标==发送批量MQ消息成功，operateType:{}，设备总数:{}", operateType, beaconStationList.size());
    }

    // 人员单个消息体
    @Data
    public static class BasePersonMsg implements Serializable {
        private static final long serialVersionUID = 1L;
        private String msgId;          // 消息ID
        private String operateType;    // 操作类型（新增/修改/删除）
        private Long operateTime;      // 操作时间戳
        private SwmPerson personData;  // 核心：直接放人员实体，无需Map
    }


    // 人员批量消息实体
    @Data
    public static class BatchPersonChangeMsg implements Serializable {
        private static final long serialVersionUID = 1L;
        private String msgId;                // 批量消息唯一ID
        private String operateType;          // 批量操作类型
        private List<SwmPerson> personList; // 人员列表
        private List<String> personIds; // 人员id列表
        private int total;                   // 总条数
        private Long operateTime;            // 操作时间
    }

    // 设备单个消息体
    @Data
    public static class BaseDeviceMsg implements Serializable {
        private static final long serialVersionUID = 1L;
        private String msgId;          // 消息ID
        private String operateType;    // 操作类型（新增/修改/删除）
        private Long operateTime;      // 操作时间戳
        private SwmHelmetDevice deviceData;
        private String deviceId;
    }

    // 设备批量消息实体
    @Data
    public static class BatchDeviceChangeMsg implements Serializable{
        private static final long serialVersionUID = 1L;
        private String msgId;                // 批量消息唯一ID
        private String operateType;          // 批量操作类型
        private List<SwmHelmetDevice> deviceList; // 设备列表
        private List<String> deviceIdList; // 设备id列表
        private int total;                   // 总条数
        private Long operateTime;            // 操作时间
    }


    // 信标单个消息体
    @Data
    public static class BaseBeaconMsg implements Serializable {
        private static final long serialVersionUID = 1L;
        private String msgId;          // 消息ID
        private String operateType;    // 操作类型（新增/修改/删除）
        private Long operateTime;      // 操作时间戳
        private SwmBeaconStation beaconData;
        private String beaconId;
    }

    // 信标批量消息实体
    @Data
    public static class BatchBeaconChangeMsg implements Serializable{
        private static final long serialVersionUID = 1L;
        private String msgId;                // 批量消息唯一ID
        private String operateType;          // 批量操作类型
        private List<SwmBeaconStation> beaconList; // 设备列表
        private List<String> beaconIdList; // 信标id列表
        private int total;                   // 总条数
        private Long operateTime;            // 操作时间
    }


    /**
     * 通用消息实体
     */
    @Getter
    @Setter
    public static class BaseChangeMsg<T extends Serializable> implements Serializable {
        private static final long serialVersionUID = 1L;
        // Getter & Setter
        private String msgId;
        private T bizId;
        private String operateType;
        private Map<String, Object> changeData;
        private Long operateTime;
    }
}
