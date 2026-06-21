package cn.iocoder.yudao.module.iot.mqtt.dto;

import lombok.Data;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

/**
 * MQTT订阅请求对象
 *
 * @author Shawn
 * @date 2025-07-21
 */
@Data
public class SubscriptionRequest {

    /**
     * 订阅主题
     */
    @NotBlank(message = "主题不能为空")
    private String topic;

    /**
     * QoS级别 (0, 1, 2)
     */
    @Min(value = 0, message = "QoS必须在0-2之间")
    @Max(value = 2, message = "QoS必须在0-2之间")
    private int qos = 1;

    /**
     * 处理器类型
     */
    @NotBlank(message = "处理器类型不能为空")
    private String processorType;

    public SubscriptionRequest() {
    }

    public SubscriptionRequest(String topic, int qos, String processorType) {
        this.topic = topic;
        this.qos = qos;
        this.processorType = processorType;
    }
}
