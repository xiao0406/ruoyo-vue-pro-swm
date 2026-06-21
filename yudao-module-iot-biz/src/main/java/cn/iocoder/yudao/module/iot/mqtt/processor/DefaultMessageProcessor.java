package cn.iocoder.yudao.module.iot.mqtt.processor;

import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;

/**
 * 默认MQTT消息处理器
 * 简单打印消息内容到控制台
 *
 * @author Shawn
 * @date 2025-07-21
 */
public class DefaultMessageProcessor implements MqttMessageProcessor {

    private static final Logger logger = LoggerFactory.getLogger(DefaultMessageProcessor.class);

    @Override
    public void process(String topic, MqttMessage message) throws Exception {
        String payload = new String(message.getPayload(), StandardCharsets.UTF_8);

        logger.info("收到MQTT消息 [默认处理器] - Topic: {}, QoS: {}, Payload: {}",
            topic, message.getQos(), payload);
    }
}
