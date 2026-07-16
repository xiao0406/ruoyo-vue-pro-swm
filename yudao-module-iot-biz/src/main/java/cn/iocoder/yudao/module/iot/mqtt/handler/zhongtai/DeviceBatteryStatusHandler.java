package cn.iocoder.yudao.module.iot.mqtt.handler.zhongtai;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.iocoder.yudao.module.iot.cache.DeviceTenantMappingCache;
import cn.iocoder.yudao.module.iot.cache.service.RedisService;
import cn.iocoder.yudao.module.iot.mqtt.constant.MqttConstants;
import cn.iocoder.yudao.module.iot.mqtt.handler.MqttBusinessHandler;
import cn.iocoder.yudao.module.swm.api.constant.SwmRedisKeyConstants;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Slf4j
@Component
public class DeviceBatteryStatusHandler implements MqttBusinessHandler {

    @Resource
    private RedisService redisService;
    @Resource
    private DeviceTenantMappingCache deviceTenantMappingCache;

    @Override
    public boolean canHandle(String topic, MqttMessage message) {
        return topic != null && topic.contains(MqttConstants.BATTERY);
    }

    @Override
    public void handle(String topic, MqttMessage message) {
        String payload = new String(message.getPayload(), StandardCharsets.UTF_8);
        JSONObject json = JSONUtil.parseObj(payload);
        String deviceId = json.getStr("deviceId", json.getStr("device_id"));
        if (deviceId == null || deviceId.isBlank()) {
            log.warn("Battery message skipped, deviceId is blank: {}", payload);
            return;
        }
        Object battery = json.get("battery");
        String tenantKey = deviceTenantMappingCache.getTenantKey(deviceId);
        redisService.hset(tenantKey + SwmRedisKeyConstants.RedisSwmKey.ZT_DEVICE_BATTERY, deviceId, battery);
    }
}
