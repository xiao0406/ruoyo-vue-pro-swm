package cn.iocoder.yudao.module.iot.tcp.util;

import cn.iocoder.yudao.module.iot.cache.DeviceTenantMappingCache;
import cn.iocoder.yudao.module.iot.cache.service.RedisService;
import cn.iocoder.yudao.module.swm.api.constant.SwmRedisKeyConstants;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class RedisUtil {

    private static final long ONLINE_EXPIRE_SECONDS = 2 * 60;

    @Resource
    private RedisService redisService;
    @Resource
    private DeviceTenantMappingCache deviceTenantMappingCache;

    public void setDeviceOnline(String deviceId) {
        setDeviceOnline(deviceId, ONLINE_EXPIRE_SECONDS);
    }

    public void setZTDeviceOnline(String deviceId, long time) {
        setDeviceOnline(deviceId, time);
    }

    public void refreshDeviceOnline(String deviceId) {
        refreshDeviceOnlineByTime(deviceId, ONLINE_EXPIRE_SECONDS);
    }

    public void refreshDeviceOnlineByTime(String deviceId, long time) {
        try {
            String ttlKey = buildTtlKey(deviceId);
            if (redisService.hasKey(ttlKey)) {
                redisService.expire(ttlKey, time);
                log.debug("Refreshed online TTL for deviceId={}", deviceId);
                return;
            }
            setDeviceOnline(deviceId, time);
        } catch (Exception ex) {
            log.error("Failed to refresh online TTL for deviceId={}", deviceId, ex);
        }
    }

    public void removeDeviceOnline(String deviceId) {
        try {
            String tenantKey = deviceTenantMappingCache.getTenantKey(deviceId);
            redisService.setRemove(tenantKey + SwmRedisKeyConstants.IotKey.ONLINE_DEVICES_KEY, deviceId);
            redisService.del(buildTtlKey(deviceId));
            log.debug("Removed online state for deviceId={}", deviceId);
        } catch (Exception ex) {
            log.error("Failed to remove online state for deviceId={}", deviceId, ex);
        }
    }

    private void setDeviceOnline(String deviceId, long expireSeconds) {
        try {
            String tenantKey = deviceTenantMappingCache.getTenantKey(deviceId);
            redisService.sSet(tenantKey + SwmRedisKeyConstants.IotKey.ONLINE_DEVICES_KEY, deviceId);
            redisService.set(buildTtlKey(deviceId), "1", expireSeconds);
            log.debug("Set device online, deviceId={}, expireSeconds={}", deviceId, expireSeconds);
        } catch (Exception ex) {
            log.error("Failed to set device online, deviceId={}", deviceId, ex);
        }
    }

    private String buildTtlKey(String deviceId) {
        return deviceTenantMappingCache.getTenantKey(deviceId)
                + SwmRedisKeyConstants.IotKey.DEVICE_TTL_KEY_PREFIX
                + deviceId;
    }
}
