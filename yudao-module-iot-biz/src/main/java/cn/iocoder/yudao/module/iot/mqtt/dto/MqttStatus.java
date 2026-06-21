package cn.iocoder.yudao.module.iot.mqtt.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * MQTT状态响应对象
 *
 * @author Shawn
 * @date 2025-07-21
 */
@Data
public class MqttStatus {

    /**
     * 是否已连接
     */
    private boolean connected;

    /**
     * Broker地址
     */
    private String brokerUrl;

    /**
     * 客户端ID
     */
    private String clientId;

    /**
     * 订阅数量
     */
    private int subscriptionCount;

    /**
     * 连接时间
     */
    private LocalDateTime connectTime;

    /**
     * 运行时间（秒）
     */
    private long uptime;

    /**
     * 重连次数
     */
    private int reconnectAttempts;

    /**
     * 最后重连时间
     */
    private LocalDateTime lastReconnectTime;

    /**
     * 发布成功次数
     */
    private long publishSuccessCount;

    /**
     * 发布失败次数
     */
    private long publishFailureCount;

    /**
     * 接收消息总数
     */
    private long totalMessagesReceived;
}
