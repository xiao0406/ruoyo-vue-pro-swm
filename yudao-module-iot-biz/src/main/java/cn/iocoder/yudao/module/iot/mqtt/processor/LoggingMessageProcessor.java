package cn.iocoder.yudao.module.iot.mqtt.processor;

import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

/**
 * 日志记录MQTT消息处理器
 * 详细记录消息信息到日志
 *
 * @author Shawn
 * @date 2025-07-21
 */
public class LoggingMessageProcessor implements MqttMessageProcessor {

    private static final Logger logger = LoggerFactory.getLogger(LoggingMessageProcessor.class);

    @Override
    public void process(String topic, MqttMessage message) throws Exception {
        String payload = new String(message.getPayload(), StandardCharsets.UTF_8);

        logger.info("=== MQTT消息详细信息 ===");
        logger.info("主题: {}", topic);
        logger.info("QoS: {}", message.getQos());
        logger.info("保留标志: {}", message.isRetained());
        logger.info("消息ID: {}", message.getId());
        logger.info("消息长度: {} bytes", message.getPayload().length);
        logger.info("消息内容: {}", payload);
        logger.info("接收时间: {}", LocalDateTime.now());
        logger.info("========================");
    }
}
