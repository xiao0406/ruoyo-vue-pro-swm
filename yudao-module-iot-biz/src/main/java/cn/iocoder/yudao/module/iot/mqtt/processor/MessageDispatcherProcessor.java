package cn.iocoder.yudao.module.iot.mqtt.processor;

import cn.iocoder.yudao.module.iot.mqtt.handler.weigou.DevicePositionMessageHandler;
import cn.iocoder.yudao.module.iot.mqtt.handler.weigou.FenceAlarmMessageHandler;
import cn.iocoder.yudao.module.iot.mqtt.handler.HelmetAttendanceHandler;
import jakarta.annotation.Resource;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * MQTT消息分发处理器
 * 根据消息内容特征将消息路由到不同的业务处理器
 *
 * @author Shawn
 * @date 2025-07-23
 */
@Component
@ConditionalOnProperty(prefix = "iot.mqtt", name = "enabled", havingValue = "true")
public class MessageDispatcherProcessor implements MqttMessageProcessor {

    private static final Logger logger = LoggerFactory.getLogger(MessageDispatcherProcessor.class);

    @Resource
    private HelmetAttendanceHandler helmetAttendanceHandler;

    @Resource
    private DevicePositionMessageHandler devicePositionMessageHandler;

    @Resource
    private FenceAlarmMessageHandler fenceAlarmMessageHandler;

    @Override
    public void process(String topic, MqttMessage message) throws Exception {
        try {

            // 1. 检查是否为头盔考勤消息
            if (helmetAttendanceHandler.canHandle(topic, message)) {
                helmetAttendanceHandler.handle(topic, message);
                return;
            }

            // ================维构定位数据======================================
            // 2. 处理定位数据 这里注释掉 不进行处理维构转发过来的定位数据
            if (devicePositionMessageHandler.canHandle(topic, message)) {
                devicePositionMessageHandler.handle(topic, message);
                return;
             }
            // 3. 处理告警数据
             if (fenceAlarmMessageHandler.canHandle(topic, message)) {
                fenceAlarmMessageHandler.handle(topic, message);
                return;
            }

        } catch (Exception e) {
            logger.error("MQTT消息分发处理异常: topic={}, error={}", topic, e.getMessage(), e);
            throw e;
        }
    }
}
