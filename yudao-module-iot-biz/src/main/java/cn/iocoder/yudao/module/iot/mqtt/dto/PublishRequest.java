package cn.iocoder.yudao.module.iot.mqtt.dto;

import lombok.Data;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

/**
 * MQTT发布请求对象
 *
 * @author Shawn
 * @date 2025-07-21
 */
@Data
public class PublishRequest {

    /**
     * 发布主题
     */
    @NotBlank(message = "主题不能为空")
    private String topic;

    /**
     * 消息内容
     */
    @NotBlank(message = "消息内容不能为空")
    private String payload;

    /**
     * QoS级别 (0, 1, 2)
     */
    @Min(value = 0, message = "QoS必须在0-2之间")
    @Max(value = 2, message = "QoS必须在0-2之间")
    private int qos = 1;

    /**
     * 是否保留消息
     */
    private boolean retained = false;

    public PublishRequest() {
    }

    public PublishRequest(String topic, String payload) {
        this.topic = topic;
        this.payload = payload;
    }

    public PublishRequest(String topic, String payload, int qos) {
        this.topic = topic;
        this.payload = payload;
        this.qos = qos;
    }

    public PublishRequest(String topic, String payload, int qos, boolean retained) {
        this.topic = topic;
        this.payload = payload;
        this.qos = qos;
        this.retained = retained;
    }
}
