package cn.iocoder.yudao.module.iot.mqtt.processor;

import org.eclipse.paho.client.mqttv3.MqttMessage;

/**
 * MQTT消息处理器接口
 * 用于处理接收到的MQTT消息
 *
 * @author Shawn
 * @date 2025-07-21
 */
@FunctionalInterface
public interface MqttMessageProcessor {

    /**
     * 处理MQTT消息
     *
     * @param topic 消息主题
     * @param message MQTT消息对象
     * @throws Exception 处理异常
     */
    void process(String topic, MqttMessage message) throws Exception;
}
