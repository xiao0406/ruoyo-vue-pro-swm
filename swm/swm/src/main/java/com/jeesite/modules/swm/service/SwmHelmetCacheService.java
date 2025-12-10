package com.jeesite.modules.swm.service;

import com.jeesite.modules.cache.service.RedisService;
import com.jeesite.modules.swm.constant.SwmRedisConstant;
import com.jeesite.modules.swm.entity.SwmHelmetDevice;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * 头盔设备Redis缓存服务（简化版）
 * 只缓存device_id和assigned_person的映射关系
 * 
 * @author Shawn
 */
@Service
@Slf4j
public class SwmHelmetCacheService {

    @Autowired
    private RedisService redisService;

    /**
     * 系统启动时，将设备ID和分配人员的映射关系加载到Redis
     * 
     * @param deviceList 设备列表
     */
    public void initHelmetCache(java.util.List<SwmHelmetDevice> deviceList) {
        log.info("开始初始化头盔设备映射关系缓存，共{}条记录", deviceList.size());

        try {
            Map<String, String> devicePersonMap = new HashMap<>();
            Map<String, String> personDeviceMap = new HashMap<>();

            for (SwmHelmetDevice device : deviceList) {
                // 只缓存设备ID和分配人员的映射关系
                if (device.getDeviceId() != null && device.getAssignedPerson() != null
                        && !device.getAssignedPerson().trim().isEmpty()) {
                    devicePersonMap.put(device.getDeviceId(), device.getAssignedPerson());
                    personDeviceMap.put(device.getAssignedPerson(), device.getDeviceId());
                }
            }

            // 批量缓存映射关系（永不过期）
            if (!devicePersonMap.isEmpty()) {
                Map<String, Object> devicePersonMapObj = new HashMap<>(devicePersonMap);
                redisService.del(SwmRedisConstant.Helmet.DEVICE_PERSON_MAP);
                redisService.hmset(SwmRedisConstant.Helmet.DEVICE_PERSON_MAP, devicePersonMapObj);
                log.info("已缓存设备到人员映射关系，共{}条", devicePersonMap.size());
            }

            if (!personDeviceMap.isEmpty()) {
                Map<String, Object> personDeviceMapObj = new HashMap<>(personDeviceMap);
                redisService.del(SwmRedisConstant.Helmet.PERSON_DEVICE_MAP);
                redisService.hmset(SwmRedisConstant.Helmet.PERSON_DEVICE_MAP, personDeviceMapObj);
                log.info("已缓存人员到设备映射关系，共{}条", personDeviceMap.size());
            }

            log.info("头盔设备映射关系缓存初始化完成");

        } catch (Exception e) {
            log.error("初始化头盔设备映射关系缓存失败", e);
        }
    }

    /**
     * 更新设备分配关系的缓存
     * 
     * @param deviceId       设备ID
     * @param assignedPerson 分配的人员身份证号（可为null表示解绑）
     */
    public void updateDevicePersonMapping(String deviceId, String assignedPerson) {
        if (deviceId == null || deviceId.trim().isEmpty()) {
            return;
        }

        try {
            // 获取当前映射关系
            String currentPerson = (String) redisService.hget(SwmRedisConstant.Helmet.DEVICE_PERSON_MAP, deviceId);

            // 清除旧的映射关系
            if (currentPerson != null && !currentPerson.trim().isEmpty()) {
                redisService.hdel(SwmRedisConstant.Helmet.PERSON_DEVICE_MAP, currentPerson);
                log.debug("已清除旧的人员映射：{} -> {}", currentPerson, deviceId);
            }

            // 设置新的映射关系
            if (assignedPerson != null && !assignedPerson.trim().isEmpty()) {
                // 检查该人员是否已绑定其他设备
                String existingDevice = (String) redisService.hget(SwmRedisConstant.Helmet.PERSON_DEVICE_MAP,
                        assignedPerson);
                if (existingDevice != null && !existingDevice.equals(deviceId)) {
                    // 清除该人员的旧设备绑定
                    redisService.hdel(SwmRedisConstant.Helmet.DEVICE_PERSON_MAP, existingDevice);
                    log.debug("清除人员旧设备绑定：{} -> {}", assignedPerson, existingDevice);
                }

                // 建立新的双向映射（永不过期）
                redisService.hset(SwmRedisConstant.Helmet.DEVICE_PERSON_MAP, deviceId, assignedPerson);
                redisService.hset(SwmRedisConstant.Helmet.PERSON_DEVICE_MAP, assignedPerson, deviceId);
                log.info("已更新设备分配关系：{} -> {}", deviceId, assignedPerson);
            } else {
                // 解绑操作，清除设备映射
                redisService.hdel(SwmRedisConstant.Helmet.DEVICE_PERSON_MAP, deviceId);
                log.info("已解绑设备：{}", deviceId);
            }
        } catch (Exception e) {
            log.error("更新设备分配关系缓存失败，设备ID：{}，人员：{}", deviceId, assignedPerson, e);
        }
    }

    /**
     * 从缓存中获取设备分配的人员，如果缓存中没有则查询数据库
     * 
     * @param deviceId      设备ID
     * @param deviceService 设备服务（用于数据库查询）
     * @return 分配的人员身份证号
     */
    public String getAssignedPersonFromCache(String deviceId, SwmHelmetDeviceService deviceService) {
        if (deviceId == null || deviceId.trim().isEmpty()) {
            return null;
        }

        try {
            // 先从缓存中查找
            String cachedPerson = (String) redisService.hget(SwmRedisConstant.Helmet.DEVICE_PERSON_MAP, deviceId);
            if (cachedPerson != null) {
                log.debug("从缓存中获取设备分配人员：{} -> {}", deviceId, cachedPerson);
                return cachedPerson;
            }

            // 缓存中没有，查询数据库
            SwmHelmetDevice device = deviceService.getByDeviceIdFromDB(deviceId);
            if (device != null && device.getAssignedPerson() != null && !device.getAssignedPerson().trim().isEmpty()) {
                // 更新缓存
                updateDevicePersonMapping(deviceId, device.getAssignedPerson());
                log.debug("从数据库查询并缓存设备分配人员：{} -> {}", deviceId, device.getAssignedPerson());
                return device.getAssignedPerson();
            }

        } catch (Exception e) {
            log.error("获取设备分配人员失败，设备ID：{}", deviceId, e);
        }

        return null;
    }

    /**
     * 从缓存中获取人员分配的设备，如果缓存中没有则查询数据库
     * 
     * @param personId      人员身份证号
     * @param deviceService 设备服务（用于数据库查询）
     * @return 设备ID
     */
    public String getAssignedDeviceFromCache(String personId, SwmHelmetDeviceService deviceService) {
        if (personId == null || personId.trim().isEmpty()) {
            return null;
        }

        try {
            // 先从缓存中查找
            String cachedDevice = (String) redisService.hget(SwmRedisConstant.Helmet.PERSON_DEVICE_MAP, personId);
            if (cachedDevice != null) {
                log.debug("从缓存中获取人员分配设备：{} -> {}", personId, cachedDevice);
                return cachedDevice;
            }

            // 缓存中没有，查询数据库
            java.util.List<SwmHelmetDevice> devices = deviceService.findByAssignedPerson(personId);
            if (devices != null && !devices.isEmpty()) {
                SwmHelmetDevice device = devices.get(0); // 假设一个人只能分配一个设备
                // 更新缓存
                updateDevicePersonMapping(device.getDeviceId(), personId);
                log.debug("从数据库查询并缓存人员分配设备：{} -> {}", personId, device.getDeviceId());
                return device.getDeviceId();
            }

        } catch (Exception e) {
            log.error("获取人员分配设备失败，人员ID：{}", personId, e);
        }

        return null;
    }

    /**
     * 获取所有设备到人员的映射关系
     * 
     * @return 映射关系Map
     */
    public Map<String, String> getAllDevicePersonMappings() {
        try {
            Map<Object, Object> mappings = redisService.hmget(SwmRedisConstant.Helmet.DEVICE_PERSON_MAP);
            Map<String, String> result = new HashMap<>();
            if (mappings != null) {
                for (Map.Entry<Object, Object> entry : mappings.entrySet()) {
                    result.put(String.valueOf(entry.getKey()), String.valueOf(entry.getValue()));
                }
            }
            return result;
        } catch (Exception e) {
            log.error("获取所有设备人员映射关系失败", e);
            return new HashMap<>();
        }
    }

    /**
     * 获取所有人员到设备的映射关系
     * 
     * @return 映射关系Map
     */
    public Map<String, String> getAllPersonDeviceMappings() {
        try {
            Map<Object, Object> mappings = redisService.hmget(SwmRedisConstant.Helmet.PERSON_DEVICE_MAP);
            Map<String, String> result = new HashMap<>();
            if (mappings != null) {
                for (Map.Entry<Object, Object> entry : mappings.entrySet()) {
                    result.put(String.valueOf(entry.getKey()), String.valueOf(entry.getValue()));
                }
            }
            return result;
        } catch (Exception e) {
            log.error("获取所有人员设备映射关系失败", e);
            return new HashMap<>();
        }
    }

    /**
     * 清除指定设备的缓存
     * 
     * @param deviceId 设备ID
     */
    public void clearDeviceCache(String deviceId) {
        if (deviceId == null || deviceId.trim().isEmpty()) {
            return;
        }

        try {
            // 获取当前分配的人员
            String assignedPerson = (String) redisService.hget(SwmRedisConstant.Helmet.DEVICE_PERSON_MAP, deviceId);

            // 清除映射关系
            redisService.hdel(SwmRedisConstant.Helmet.DEVICE_PERSON_MAP, deviceId);
            if (assignedPerson != null && !assignedPerson.trim().isEmpty()) {
                redisService.hdel(SwmRedisConstant.Helmet.PERSON_DEVICE_MAP, assignedPerson);
            }

            log.info("已清除设备缓存：{}", deviceId);
        } catch (Exception e) {
            log.error("清除设备缓存失败，设备ID：{}", deviceId, e);
        }
    }

    /**
     * 清除所有头盔相关缓存
     */
    public void clearAllHelmetCache() {
        try {
            // 清除映射关系缓存
            redisService.del(SwmRedisConstant.Helmet.DEVICE_PERSON_MAP);
            redisService.del(SwmRedisConstant.Helmet.PERSON_DEVICE_MAP);

            log.info("已清除所有头盔设备映射关系缓存");
        } catch (Exception e) {
            log.error("清除所有头盔设备映射关系缓存失败", e);
        }
    }

    /**
     * 检查Redis连接状态
     * 
     * @return true表示连接正常
     */
    public boolean isRedisAvailable() {
        try {
            redisService.set("SWM:HEALTH_CHECK", "OK", 10);
            String result = (String) redisService.get("SWM:HEALTH_CHECK");
            redisService.del("SWM:HEALTH_CHECK");
            return "OK".equals(result);
        } catch (Exception e) {
            log.error("Redis连接检查失败", e);
            return false;
        }
    }
}