package cn.iocoder.yudao.module.swm.mq;

/**
 * SWM 模块 RabbitMQ 常量
 *
 * 包含队列名、交换机名、路由键等 MQ 配置常量。
 * 迁移自 JeeSite: com.jeesite.modules.swm.mq.SwmQueueKey + com.jeesite.modules.config.RabbitMqConfig
 */
public interface SwmMqConstants {

    // ========== 一键召回队列 ==========
    String SWM_RECALL_MESSAGE_PUSH = "SWM_RECALL_MESSAGE_PUSH";

    // ========== 人员变更队列 ==========
    String PERSON_CHANGE_QUEUE = "swm.person.change.queue";
    String PERSON_CHANGE_EXCHANGE = "swm.person.change.exchange";
    String PERSON_CHANGE_ROUTING_KEY = "swm.person.change.routing.key";
    String PERSON_CHANGE_DLQ_QUEUE = "swm.person.change.dlq.queue";
    String PERSON_CHANGE_DLQ_EXCHANGE = "swm.person.change.dlq.exchange";
    String PERSON_CHANGE_DLQ_ROUTING_KEY = "swm.person.change.dlq.routing.key";

    // ========== 设备变更队列 ==========
    String DEVICE_CHANGE_QUEUE = "swm.device.change.queue";
    String DEVICE_CHANGE_EXCHANGE = "swm.device.change.exchange";
    String DEVICE_CHANGE_ROUTING_KEY = "swm.device.change.routing.key";
    String DEVICE_CHANGE_DLQ_QUEUE = "swm.device.change.dlq.queue";
    String DEVICE_CHANGE_DLQ_EXCHANGE = "swm.device.change.dlq.exchange";
    String DEVICE_CHANGE_DLQ_ROUTING_KEY = "swm.device.change.dlq.routing.key";

    // ========== 信标变更队列 ==========
    String BEACON_CHANGE_QUEUE = "swm.beacon.change.queue";
    String BEACON_CHANGE_EXCHANGE = "swm.beacon.change.exchange";
    String BEACON_CHANGE_ROUTING_KEY = "swm.beacon.change.routing.key";
    String BEACON_CHANGE_DLQ_QUEUE = "swm.beacon.change.dlq.queue";
    String BEACON_CHANGE_DLQ_EXCHANGE = "swm.beacon.change.dlq.exchange";
    String BEACON_CHANGE_DLQ_ROUTING_KEY = "swm.beacon.change.dlq.routing.key";

}
