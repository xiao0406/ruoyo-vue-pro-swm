package cn.iocoder.yudao.module.iot.mqtt.handler.zhongtai;

import cn.hutool.json.JSONObject;
import cn.iocoder.yudao.module.iot.cache.DeviceTenantMappingCache;
import cn.iocoder.yudao.module.iot.cache.service.RedisService;
import cn.iocoder.yudao.module.iot.mqtt.constant.MqttConstants;
import cn.iocoder.yudao.module.iot.mqtt.handler.MqttBusinessHandler;
import cn.iocoder.yudao.module.iot.service.VoiceAlarmService;
import cn.iocoder.yudao.module.swm.api.constant.SwmRedisKeyConstants;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class DeviceSoSAlarmHandler implements MqttBusinessHandler {

    @Resource
    private RedisService redisService;
    @Resource
    private DeviceTenantMappingCache deviceTenantMappingCache;
    @Resource
    private VoiceAlarmService voiceAlarmService;

    @Override
    public boolean canHandle(String topic, MqttMessage message) {
        return topic != null && topic.contains(MqttConstants.SOS);
    }

    @Override
    public void handle(String topic, MqttMessage message) {
        log.info("Received SOS alarm message on topic {}", topic);
    }

    public JSONObject sendVoiceCommand(String deviceId, String voiceText) {
        boolean success = voiceAlarmService.sendVoiceAlarm(deviceId, voiceText);
        JSONObject result = new JSONObject();
        result.set("success", success);
        result.set("deviceId", deviceId);
        result.set("message", success ? "voice command sent" : "voice command send failed");
        return result;
    }

    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> getIBeaconDataToRedis(String deviceId) {
        try {
            String tenantKey = deviceTenantMappingCache.getTenantKey(deviceId);
            Object value = redisService.hget(tenantKey + SwmRedisKeyConstants.SwmKey.ZT_DEVICE_MESSAGE_DATA, deviceId);
            if (value instanceof cn.iocoder.yudao.module.iot.tcp.model.TcpMessageData messageData) {
                return messageData.getBluetoothBeacons();
            }
            if (value instanceof Map<?, ?> map) {
                Object beacons = map.get("bluetoothBeacons");
                if (beacons instanceof List<?>) {
                    return (List<Map<String, Object>>) beacons;
                }
            }
        } catch (Exception ex) {
            log.warn("Failed to load iBeacon data from redis, deviceId={}", deviceId, ex);
        }
        return Collections.emptyList();
    }
}
