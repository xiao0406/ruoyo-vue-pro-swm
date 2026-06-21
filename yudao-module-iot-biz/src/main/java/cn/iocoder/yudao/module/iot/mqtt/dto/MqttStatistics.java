package cn.iocoder.yudao.module.iot.mqtt.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * MQTT统计信息响应对象
 *
 * @author Shawn
 * @date 2025-07-21
 */
@Data
public class MqttStatistics {

    /**
     * 总接收消息数
     */
    private long totalMessagesReceived;

    /**
     * 总发送消息数
     */
    private long totalMessagesSent;

    /**
     * 活跃订阅数
     */
    private int activeSubscriptions;

    /**
     * 连接运行时间（秒）
     */
    private long connectionUptime;

    /**
     * 各主题消息计数
     */
    private Map<String, Long> topicMessageCounts;

    /**
     * 各处理器类型消息计数
     */
    private Map<String, Long> processorMessageCounts;

    /**
     * 统计开始时间
     */
    private LocalDateTime statisticsStartTime;

    /**
     * 统计更新时间
     */
    private LocalDateTime lastUpdateTime;

    /**
     * 平均每分钟接收消息数
     */
    private double averageMessagesPerMinute;

    /**
     * 平均每分钟发送消息数
     */
    private double averageSentPerMinute;
}
