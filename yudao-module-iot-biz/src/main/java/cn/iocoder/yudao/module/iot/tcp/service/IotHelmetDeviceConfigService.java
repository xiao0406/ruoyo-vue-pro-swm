package cn.iocoder.yudao.module.iot.tcp.service;

import cn.iocoder.yudao.module.iot.cache.service.RedisService;
import cn.iocoder.yudao.module.iot.tcp.processor.SwmHelmetDeviceConfig;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

@Service
public class IotHelmetDeviceConfigService {

    @Resource
    private RedisService redisService;

    @SuppressWarnings("unchecked")
    public <T> T getByDeviceId(String deviceId) {
        if (deviceId == null || deviceId.isBlank()) {
            return null;
        }

        String key = IotHelmetDeviceService.HELMET_DEVICE_PARAM_KEY + ":" + deviceId;
        SwmHelmetDeviceConfig config = new SwmHelmetDeviceConfig();
        config.setDeviceId(deviceId);
        config.setServerIp(asString(redisService.hget(key, "serverIp")));
        config.setServerPort(asString(redisService.hget(key, "serverPort")));
        config.setBluetoothScanWindow(asString(redisService.hget(key, "bluetoothScanWindow")));
        config.setGroupDuration(asString(redisService.hget(key, "groupDuration")));
        config.setNormalBeaconCs(asString(redisService.hget(key, "normalBeaconCs")));
        config.setSpecialBeaconCs(asString(redisService.hget(key, "specialBeaconCs")));
        config.setLocationMode(asString(redisService.hget(key, "locationMode")));
        config.setDeepSleepDuration(asString(redisService.hget(key, "deepSleepDuration")));
        config.setBluetoothScanDuration(asString(redisService.hget(key, "bluetoothScanDuration")));
        config.setSendInterval(asString(redisService.hget(key, "sendInterval")));
        config.setHazardRetriggerInterval(asString(redisService.hget(key, "hazardRetriggerInterval")));
        config.setSleepWakeupTime(asString(redisService.hget(key, "sleepWakeupTime")));
        config.setBeaconFilterName(asString(redisService.hget(key, "beaconFilterName")));
        config.setHatOffAlarmInterval(asString(redisService.hget(key, "hatOffAlarmInterval")));
        return (T) config;
    }

    private String asString(Object value) {
        return value == null ? null : String.valueOf(value);
    }
}
