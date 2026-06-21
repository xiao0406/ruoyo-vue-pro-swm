package cn.iocoder.yudao.module.iot.tcp.util;

import cn.iocoder.yudao.module.swm.service.cache.DeviceCorpMappingCache;
import cn.iocoder.yudao.module.swm.service.RedisService;
import cn.iocoder.yudao.module.swm.api.constant.SwmRedisKeyConstants;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class RedisUtil {
    // TODO: This class needs to be replaced with Yudao's RedisUtils

    @Resource
    private RedisService redisService;
    @Resource
    private DeviceCorpMappingCache deviceCorpMappingCache;

    /** 在线状态过期时间（秒） */
    private static final long ONLINE_EXPIRE_SECONDS = 2 * 60;

    /**
     * SMEMBERS iot:devices:online
     * KEYS iot:device:ttl:*
     */

    /**
     * 设备上线
     */
    public void setDeviceOnline(String deviceId) {
        try {

            String corpCode = deviceCorpMappingCache.getCorpCode(deviceId);
            // 1 加入总在线集合
            redisService.sSet(corpCode+ SwmRedisKeyConstants.IotKey.ONLINE_DEVICES_KEY, deviceId);

            // 2️ 设置单独TTL key（自动过期）
            redisService.set(corpCode + SwmRedisKeyConstants.IotKey.DEVICE_TTL_KEY_PREFIX + deviceId, "1", ONLINE_EXPIRE_SECONDS);

        } catch (Exception e) {
            log.error("设置设备在线状态失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 设备上线，中泰专用，不设置过期时间，拿设备推过来的离线作为判定条件
     */
    public void setZTDeviceOnline(String deviceId,long time ) {
        try {

            String corpCode = deviceCorpMappingCache.getCorpCode(deviceId);
            // 1 加入总在线集合
            redisService.sSet(corpCode+ SwmRedisKeyConstants.IotKey.ONLINE_DEVICES_KEY, deviceId);

            // 2️ 设置单独TTL key（自动过期）
            redisService.set(corpCode + SwmRedisKeyConstants.IotKey.DEVICE_TTL_KEY_PREFIX + deviceId, "1", time);

        } catch (Exception e) {
            log.error("设置设备在线状态失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 心跳续期
     */
    public void refreshDeviceOnline(String deviceId) {
        try {

            String corpCode = deviceCorpMappingCache.getCorpCode(deviceId);

            String ttlKey = corpCode+ SwmRedisKeyConstants.IotKey.DEVICE_TTL_KEY_PREFIX + deviceId;
            if (redisService.hasKey(ttlKey)) {
                redisService.expire(ttlKey, ONLINE_EXPIRE_SECONDS);
                log.info("设备 [{}] TTL 已续期", deviceId);
            } else {
                // 若 TTL 已过期，则重新注册
                setDeviceOnline(deviceId);
            }
        } catch (Exception e) {
            log.error("刷新设备在线状态失败: {}", e.getMessage(), e);
        }
    }


    /**
     * 心跳续期(指定时间)
     */
    public void refreshDeviceOnlineByTime(String deviceId, long time) {
        try {

            String corpCode = deviceCorpMappingCache.getCorpCode(deviceId);
            String ttlKey = corpCode+ SwmRedisKeyConstants.IotKey.DEVICE_TTL_KEY_PREFIX + deviceId;
            if (redisService.hasKey(ttlKey)) {
                redisService.expire(ttlKey, time);
                log.info("设备 [{}] TTL 已续期", deviceId);
            } else {
                // 若 TTL 已过期，则重新注册
                setDeviceOnline(deviceId);
            }
        } catch (Exception e) {
            log.error("刷新设备在线状态失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 设备下线
     */
    public void removeDeviceOnline(String deviceId) {
        try {

            String corpCode = deviceCorpMappingCache.getCorpCode(deviceId);

            redisService.setRemove(corpCode + SwmRedisKeyConstants.IotKey.ONLINE_DEVICES_KEY, deviceId);
            redisService.del(corpCode + SwmRedisKeyConstants.IotKey.DEVICE_TTL_KEY_PREFIX + deviceId);
            log.info("设备 [{}] 已标记为下线", deviceId);
        } catch (Exception e) {
            log.error("移除设备在线状态失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 获取所有在线设备 ID
     */
//    public Set<Object> getAllOnlineDevices() {
//        try {
//            return redisService.sGet(RedisConstant.RedisIotKey.ONLINE_DEVICES_KEY);
//        } catch (Exception e) {
//            log.error("获取在线设备列表失败: {}", e.getMessage(), e);
//            return null;
//        }
//    }


//    /**
//     * 定时清理过期设备（每分钟执行）
//     */
//    @Scheduled(fixedRate = 60000)
//    public void cleanExpiredDevices() {
//        log.info("开始执行自动清理过期设备任务");
//        try {
//            Set<Object> deviceIds = redisService.sGet(ONLINE_DEVICES_KEY);
//            if (deviceIds == null || deviceIds.isEmpty()) {
//                return;
//            }
//
//            for (Object deviceIdObj : deviceIds) {
//                String deviceId = String.valueOf(deviceIdObj);
//                String ttlKey = DEVICE_TTL_KEY_PREFIX + deviceId;
//                if (!redisService.hasKey(ttlKey)) {
//                    long removed = redisService.setRemove(ONLINE_DEVICES_KEY, deviceId);
//                    log.info("设备 [{}] TTL 过期，已自动移出在线集合, 移除数量: {}", deviceId, removed);
//                }
//            }
//        } catch (Exception e) {
//            log.error("自动清理过期设备失败: {}", e.getMessage(), e);
//        }
//    }
}
