package cn.iocoder.yudao.module.iot.mqtt.entity;

import cn.iocoder.yudao.module.iot.mqtt.processor.MqttMessageProcessor;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.concurrent.atomic.LongAdder;

/**
 * MQTT订阅信息实体类
 *
 * @author Shawn
 * @date 2025-07-21
 */
@Data
public class MqttSubscription {

    /**
     * 订阅主题
     */
    private String topic;

    /**
     * QoS级别
     */
    private int qos;

    /**
     * 处理器类型
     */
    private String processorType;

    /**
     * 消息处理器实例
     */
    private transient MqttMessageProcessor processor;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 订阅来源
     */
    private SubscriptionSource source;

    /**
     * 绑定的Broker地址，null表示所有可订阅的Broker
     */
    private String brokerUrl;

    /**
     * 接收消息数量（并发安全）
     */
    private final transient LongAdder messageCounter = new LongAdder();

    /**
     * 最后接收消息时间
     */
    private LocalDateTime lastMessageTime;

    /**
     * 最后重新订阅时间
     */
    private LocalDateTime lastResubscribeTime;

    /**
     * 重试次数
     */
    private int retryCount = 0;

    /**
     * 是否活跃
     */
    private boolean active = true;

    public MqttSubscription() {
    }

    public MqttSubscription(String topic, int qos, MqttMessageProcessor processor,
                           LocalDateTime createTime, SubscriptionSource source) {
        this.topic = topic;
        this.qos = qos;
        this.processor = processor;
        this.createTime = createTime;
        this.source = source;
        this.processorType = getProcessorTypeName(processor);
    }

    /**
     * 增加消息计数
     */
    public void incrementMessageCount() {
        this.messageCounter.increment();
        this.lastMessageTime = LocalDateTime.now();
    }

    public long getMessageCount() {
        return this.messageCounter.sum();
    }

    /**
     * 增加重试计数
     */
    public void incrementRetryCount() {
        this.retryCount++;
    }

    /**
     * 判断是否为配置驱动的订阅
     */
    public boolean isConfigDriven() {
        return source == SubscriptionSource.CONFIG;
    }

    /**
     * 判断是否为API驱动的订阅
     */
    public boolean isApiDriven() {
        return source == SubscriptionSource.API;
    }

    /**
     * 获取处理器类型名称
     */
    private String getProcessorTypeName(MqttMessageProcessor processor) {
        if (processor == null) {
            return "unknown";
        }

        String className = processor.getClass().getSimpleName();
        if (className.endsWith("MessageProcessor")) {
            className = className.substring(0, className.length() - "MessageProcessor".length());
        }
        return className.toLowerCase();
    }
}
