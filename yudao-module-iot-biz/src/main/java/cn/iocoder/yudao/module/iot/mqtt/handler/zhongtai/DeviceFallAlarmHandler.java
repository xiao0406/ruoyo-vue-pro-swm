package cn.iocoder.yudao.module.iot.mqtt.handler.zhongtai;

import cn.iocoder.yudao.module.iot.mqtt.constant.MqttConstants;
import cn.iocoder.yudao.module.iot.mqtt.handler.MqttBusinessHandler;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class DeviceFallAlarmHandler implements MqttBusinessHandler {

    @Override
    public boolean canHandle(String topic, MqttMessage message) {
        return topic != null && topic.contains(MqttConstants.FALL);
    }

    @Override
    public void handle(String topic, MqttMessage message) {
        log.info("Received fall alarm message on topic {}", topic);
    }
}
