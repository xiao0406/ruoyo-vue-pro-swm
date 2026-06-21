package cn.iocoder.yudao.module.iot.mqtt.handler;

import org.eclipse.paho.client.mqttv3.MqttMessage;

/**
 * MQTT业务处理器接口
 * 用于处理不同类型的MQTT业务消息
 *
 * @author Shawn
 * @date 2025-07-23
 */
public interface MqttBusinessHandler {

    /**
     * 处理MQTT业务消息
     *
     * @param topic 消息主题
     * @param message MQTT消息对象
     * @throws Exception 处理异常
     */
    void handle(String topic, MqttMessage message) throws Exception;

    /**
     * 判断是否可以处理该消息
     *
     * @param topic 消息主题
     * @param message MQTT消息对象
     * @return 是否可以处理
     */
    boolean canHandle(String topic, MqttMessage message);
}
