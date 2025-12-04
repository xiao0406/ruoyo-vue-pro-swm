package com.jeesite.modules.swm.cache;

import com.alibaba.cloud.commons.lang.StringUtils;
import com.jeesite.common.lang.ObjectUtils;
import com.jeesite.modules.constant.RedisConstant;
import com.jeesite.modules.enums.CorpDbEnum;
import com.jeesite.modules.swm.constant.SwmRedisConstant;
import com.jeesite.modules.swm.entity.HelmetDevice;
import com.jeesite.modules.swm.entity.SwmHelmetDevice;
import com.jeesite.modules.swm.service.SwmHelmetDeviceService;
import com.xxl.job.core.context.XxlJobHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class DeviceCorpMappingCache {

    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private SwmHelmetDeviceService helmetDeviceService;

    /**
     * 本地缓存：deviceId -> dbName
     */
    private final Map<String, String> deviceDbCache = new ConcurrentHashMap<>();

    // deviceId -> corpCode
    private final Map<String, String> deviceCorpCache = new ConcurrentHashMap<>();


    /**
     * 定时刷新整个缓存，每分钟刷新一次
     */
    public void refreshCache() {
        XxlJobHelper.log("开始刷新设备数据库和租户本地缓存...");

        // 从 Redis 获取全部 device -> corpCode 映射
        Map<Object, Object> allDeviceCorp = redisTemplate.opsForHash().entries(SwmRedisConstant.RedisGlobalKey.DEVICE_TO_CORP);

        Map<String, String> newDeviceDbCache = new ConcurrentHashMap<>();
        Map<String, String> newDeviceCorpCache = new ConcurrentHashMap<>();

        allDeviceCorp.forEach((deviceIdObj, corpCodeObj) -> {
            String deviceId = (String) deviceIdObj;
            String corpCode = (String) corpCodeObj;

            String dbName = CorpDbEnum.getDbNameByCorpCode(corpCode);
            if (dbName != null) {
                newDeviceDbCache.put(deviceId, dbName);
                newDeviceCorpCache.put(deviceId, corpCode);
            }
        });

        deviceDbCache.clear();
        deviceDbCache.putAll(newDeviceDbCache);

        deviceCorpCache.clear();
        deviceCorpCache.putAll(newDeviceCorpCache);

        XxlJobHelper.log("刷新完成，本地缓存大小: dbCache={}, corpCache={}", deviceDbCache.size(), deviceCorpCache.size());
    }




    /**
     * 获取数据库名
     *
     * @param deviceId 设备ID
     * @return 数据库名，如果不存在返回 null
     */
    public String getDbName(String deviceId) {
        String dbName = deviceDbCache.get(deviceId);
        if (dbName != null) {
            return dbName;
        }

        // 缓存未命中，从 Redis 获取
        String corpCode = (String) redisTemplate.opsForHash().get(SwmRedisConstant.RedisGlobalKey.DEVICE_TO_CORP, deviceId);
        if (corpCode == null) {
            SwmHelmetDevice byDeviceId = helmetDeviceService.getByDeviceId(deviceId);
            if (ObjectUtils.isNotEmpty(byDeviceId)){
                if (StringUtils.isNotEmpty(byDeviceId.getCorpCode())){
                    //添加缓存
                    redisTemplate.opsForHash().put(SwmRedisConstant.RedisGlobalKey.DEVICE_TO_CORP, deviceId, corpCode);
                    String dbNameByCorpCode = CorpDbEnum.getDbNameByCorpCode(corpCode);
                    return dbNameByCorpCode;
                }
            }
            //这里没有租户信息，就给个默认的
            return CorpDbEnum.ZJZK.getDbName();
        }

        dbName = CorpDbEnum.getDbNameByCorpCode(corpCode);
        if (dbName == null) {
            log.warn("未找到租户数据库映射: {}", corpCode);
            return null;
        }

        // 更新本地缓存
        deviceDbCache.put(deviceId, dbName);
        return dbName;
    }



    public String getCorpCode(String deviceId) {
        String corpCode = deviceCorpCache.get(deviceId);
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

    public String getCorpCodeByIdCard(String idCard) {

        String deviceId = (String) redisTemplate.opsForHash().get(SwmRedisConstant.RedisGlobalKey.PERSON_DEVICE_MAP, String.valueOf(idCard));

        String corpCode = deviceCorpCache.get(deviceId);
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



    /**
     * 获取当前本地缓存中所有设备对应的数据库名
     *
     * @return Map<deviceId, dbName>
     */
    public Set<String> getAllDeviceDbMapping() {
        // 如果使用 ConcurrentHashMap 作为缓存，直接返回副本，避免外部修改
        HashMap<String, String> stringStringHashMap = new HashMap<>(deviceDbCache);
        Set<String> dbNames = new HashSet<>(stringStringHashMap.values()); // 去重
        return dbNames;
    }
}



