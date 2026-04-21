package com.jeesite.modules.swm.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.jeesite.common.lang.ObjectUtils;
import com.jeesite.modules.enums.CorpDbEnum;
import com.jeesite.modules.constant.SwmRedisConstant;
import com.jeesite.modules.swm.entity.SwmHelmetDevice;
import com.jeesite.modules.swm.service.SwmHelmetDeviceService;
import com.xxl.job.core.context.XxlJobHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Component
public class DeviceCorpMappingCache {

    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private SwmHelmetDeviceService helmetDeviceService;

    private static final String NULL_VALUE = "NULL";

    /**
     * 本地缓存（使用 Caffeine 防止OOM）
     */
    private volatile Cache<String, String> deviceDbCache = buildCache();
    private volatile Cache<String, String> deviceCorpCache = buildCache();

    private Cache<String, String> buildCache() {
        return Caffeine.newBuilder().maximumSize(200_000) // 最大20万，根据你实际设备量调整
                .expireAfterWrite(10, TimeUnit.MINUTES).build();
    }

    /**
     * 定时刷新（建议5~10分钟，不要1分钟）
     */
    public void refreshCache() {
        XxlJobHelper.log("开始刷新设备缓存...");

        Map<Object, Object> allDeviceCorp = redisTemplate.opsForHash().entries(SwmRedisConstant.RedisGlobalKey.DEVICE_TO_CORP);

        Cache<String, String> newDbCache = buildCache();
        Cache<String, String> newCorpCache = buildCache();

        allDeviceCorp.forEach((k, v) -> {
            String deviceId = (String) k;
            String corpCode = (String) v;

            if (NULL_VALUE.equals(corpCode)) {
                return;
            }

            String dbName = CorpDbEnum.getDbNameByCorpCode(corpCode);
            if (org.apache.commons.lang3.StringUtils.isNotBlank(dbName)) {
                newDbCache.put(deviceId, dbName);
                newCorpCache.put(deviceId, corpCode);
            }
        });

        //  原子替换（核心优化点）
        deviceDbCache = newDbCache;
        deviceCorpCache = newCorpCache;

        XxlJobHelper.log("刷新完成: dbCache={}, corpCache={}", allDeviceCorp.size(), allDeviceCorp.size());
    }

    /**
     * 获取数据库名
     */
    public String getDbName(String deviceId) {
        if (org.apache.commons.lang3.StringUtils.isBlank(deviceId)) {
            return null;
        }

        // 1. 本地缓存
        String dbName = deviceDbCache.getIfPresent(deviceId);
        if (org.apache.commons.lang3.StringUtils.isNotBlank(dbName)) {
            return dbName;
        }

        // 2. Redis
        String corpCode = (String) redisTemplate.opsForHash().get(SwmRedisConstant.RedisGlobalKey.DEVICE_TO_CORP, deviceId);

        if (NULL_VALUE.equals(corpCode)) {
            return null;
        }

        // 3. DB fallback（加保护）
        if (org.apache.commons.lang3.StringUtils.isBlank(corpCode)) {
            corpCode = loadFromDbAndCache(deviceId);
        }

        if (org.apache.commons.lang3.StringUtils.isBlank(corpCode)) {
            return null;
        }

        dbName = CorpDbEnum.getDbNameByCorpCode(corpCode);

        if (org.apache.commons.lang3.StringUtils.isNotBlank(dbName)) {
            deviceDbCache.put(deviceId, dbName);
            deviceCorpCache.put(deviceId, corpCode);
        }

        return dbName;
    }

    /**
     * 获取租户
     */
    public String getCorpCode(String deviceId) {
        if (org.apache.commons.lang3.StringUtils.isBlank(deviceId)) {
            return null;
        }

        // 1. 本地缓存
        String corpCode = deviceCorpCache.getIfPresent(deviceId);
        if (org.apache.commons.lang3.StringUtils.isNotBlank(corpCode)) {
            return corpCode;
        }

        // 2. Redis
        corpCode = (String) redisTemplate.opsForHash().get(SwmRedisConstant.RedisGlobalKey.DEVICE_TO_CORP, deviceId);

        if (NULL_VALUE.equals(corpCode)) {
            return null;
        }

        if (org.apache.commons.lang3.StringUtils.isNotBlank(corpCode)) {
            deviceCorpCache.put(deviceId, corpCode);
            return corpCode;
        }

        // 3. DB fallback
        return loadFromDbAndCache(deviceId);
    }

    /**
     * DB加载 + 缓存（防穿透）
     */
    private String loadFromDbAndCache(String deviceId) {
        try {
            SwmHelmetDevice device = helmetDeviceService.getByDeviceId(deviceId);

            if (device != null && StringUtils.isNotBlank(device.getCorpCode())) {
                String corpCode = device.getCorpCode();

                // Redis缓存
                redisTemplate.opsForHash().put(SwmRedisConstant.RedisGlobalKey.DEVICE_TO_CORP, deviceId, corpCode);

                deviceCorpCache.put(deviceId, corpCode);
                return corpCode;
            }

            //  防穿透：缓存NULL
            redisTemplate.opsForHash().put(SwmRedisConstant.RedisGlobalKey.DEVICE_TO_CORP, deviceId, NULL_VALUE);

        } catch (Exception e) {
            log.error("DB查询异常 deviceId={}", deviceId, e);
        }

        return null;
    }

    /**
     * 获取所有数据库名
     */
    public Set<String> getAllDbNames() {
        return deviceDbCache.asMap().values().stream().collect(Collectors.toSet());
    }

    public String getCorpCodeByIdCard(String idCard) {

        String deviceId = (String) redisTemplate.opsForHash().get(SwmRedisConstant.RedisGlobalKey.PERSON_DEVICE_MAP, String.valueOf(idCard));


        String corpCode = deviceCorpCache.getIfPresent(deviceId);
        if (corpCode != null && !corpCode.isEmpty()) {
            return corpCode;
        }

        // 缓存未命中，从 Redis 获取，还是没有则返回 ZJZK
        corpCode = (String) redisTemplate.opsForHash().get(SwmRedisConstant.RedisGlobalKey.DEVICE_TO_CORP, deviceId);
        if (StringUtils.isEmpty(corpCode)) {
            return CorpDbEnum.ZJZK.getCorpCode();
        }
        return corpCode;
    }
}



