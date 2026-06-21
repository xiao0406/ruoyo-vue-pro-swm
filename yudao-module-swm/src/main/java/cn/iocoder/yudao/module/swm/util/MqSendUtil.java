package cn.iocoder.yudao.module.swm.util;

import cn.hutool.core.util.IdUtil;
import cn.iocoder.yudao.module.swm.mq.SwmMqConstants;
import cn.iocoder.yudao.module.swm.dal.dataobject.SwmBeaconStationDO;
import cn.iocoder.yudao.module.swm.dal.dataobject.SwmHelmetDeviceDO;
import cn.iocoder.yudao.module.swm.dal.dataobject.SwmPersonDO;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import jakarta.annotation.Resource;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Slf4j
@Component
public class MqSendUtil {
    @Resource
    private RabbitTemplate rabbitTemplate;

    public void sendMessage(String exchange, String routingKey, Object message) {
        rabbitTemplate.convertAndSend(exchange, routingKey, message);
    }

    // ==================== 人员批量变更 ====================
    public void sendPersonBatchChangeMsg(String operateType, List<SwmPersonDO> personList) {
        BatchPersonChangeMsg batchMsg = new BatchPersonChangeMsg();
        batchMsg.setMsgId(IdUtil.simpleUUID());
        batchMsg.setOperateType(operateType);
        batchMsg.setPersonList(personList);
        batchMsg.setPersonIds(null);
        batchMsg.setTotal(personList.size());
        batchMsg.setOperateTime(System.currentTimeMillis());

        String jsonStr = com.alibaba.fastjson.JSON.toJSONString(batchMsg);
        MessageProperties properties = new MessageProperties();
        properties.setContentType(MessageProperties.CONTENT_TYPE_JSON);
        properties.setContentEncoding(StandardCharsets.UTF_8.name());
        properties.setHeader("msgType", "BatchPersonChangeMsg");

        Message message = new Message(jsonStr.getBytes(StandardCharsets.UTF_8), properties);
        rabbitTemplate.send(SwmMqConstants.PERSON_CHANGE_EXCHANGE, SwmMqConstants.PERSON_CHANGE_ROUTING_KEY, message);
        log.info("人员批量消息发送MQ成功，operateType:{}，总条数:{}", operateType, personList.size());
    }

    // ==================== 设备批量变更 ====================
    public void sendDeviceBatchChangeMsg(String operateType, List<SwmHelmetDeviceDO> deviceList) {
        BatchDeviceChangeMsg batchMsg = new BatchDeviceChangeMsg();
        batchMsg.setMsgId(IdUtil.simpleUUID());
        batchMsg.setOperateType(operateType);
        batchMsg.setDeviceList(deviceList);
        batchMsg.setDeviceIdList(null);
        batchMsg.setTotal(deviceList.size());
        batchMsg.setOperateTime(System.currentTimeMillis());

        String jsonStr = com.alibaba.fastjson.JSON.toJSONString(batchMsg);
        MessageProperties properties = new MessageProperties();
        properties.setContentType(MessageProperties.CONTENT_TYPE_JSON);
        properties.setContentEncoding(StandardCharsets.UTF_8.name());
        properties.setHeader("msgType", "BatchDeviceChangeMsg");

        Message message = new Message(jsonStr.getBytes(StandardCharsets.UTF_8), properties);
        rabbitTemplate.send(SwmMqConstants.DEVICE_CHANGE_EXCHANGE, SwmMqConstants.DEVICE_CHANGE_ROUTING_KEY, message);
        log.info("设备批量消息发送MQ成功，operateType:{}，总条数:{}", operateType, deviceList.size());
    }

    // ==================== 信标批量变更 ====================
    public void sendBeaconBatchChangeMsg(String operateType, List<SwmBeaconStationDO> beaconList) {
        BatchBeaconChangeMsg batchMsg = new BatchBeaconChangeMsg();
        batchMsg.setMsgId(IdUtil.simpleUUID());
        batchMsg.setOperateType(operateType);
        batchMsg.setBeaconList(beaconList);
        batchMsg.setTotal(beaconList.size());
        batchMsg.setOperateTime(System.currentTimeMillis());

        String jsonStr = com.alibaba.fastjson.JSON.toJSONString(batchMsg);
        MessageProperties properties = new MessageProperties();
        properties.setContentType(MessageProperties.CONTENT_TYPE_JSON);
        properties.setContentEncoding(StandardCharsets.UTF_8.name());
        properties.setHeader("msgType", "BatchBeaconChangeMsg");

        Message message = new Message(jsonStr.getBytes(StandardCharsets.UTF_8), properties);
        rabbitTemplate.send(SwmMqConstants.BEACON_CHANGE_EXCHANGE, SwmMqConstants.BEACON_CHANGE_ROUTING_KEY, message);
        log.info("信标批量消息发送MQ成功，operateType:{}，总条数:{}", operateType, beaconList.size());
    }

    // ==================== 消息体定义 ====================

    @Data
    public static class BaseBeaconMsg implements Serializable {
        private static final long serialVersionUID = 1L;
        private String msgId;
        private String operateType;
        private Long operateTime;
        private SwmBeaconStationDO beaconData;
        private String beaconId;
    }

    @Data
    public static class BaseDeviceMsg implements Serializable {
        private static final long serialVersionUID = 1L;
        private String msgId;
        private String operateType;
        private Long operateTime;
        private SwmHelmetDeviceDO deviceData;
        private String deviceId;
    }

    @Data
    public static class BasePersonMsg implements Serializable {
        private static final long serialVersionUID = 1L;
        private String msgId;
        private String operateType;
        private Long operateTime;
        private SwmPersonDO personData;
    }

    @Data
    public static class BatchBeaconChangeMsg implements Serializable {
        private static final long serialVersionUID = 1L;
        private String msgId;
        private String operateType;
        private List<SwmBeaconStationDO> beaconList;
        private int total;
        private Long operateTime;
    }

    @Data
    public static class BatchDeviceChangeMsg implements Serializable {
        private static final long serialVersionUID = 1L;
        private String msgId;
        private String operateType;
        private List<SwmHelmetDeviceDO> deviceList;
        private List<String> deviceIdList;
        private int total;
        private Long operateTime;
    }

    @Data
    public static class BatchPersonChangeMsg implements Serializable {
        private static final long serialVersionUID = 1L;
        private String msgId;
        private String operateType;
        private List<SwmPersonDO> personList;
        private List<String> personIds;
        private int total;
        private Long operateTime;
    }
}
