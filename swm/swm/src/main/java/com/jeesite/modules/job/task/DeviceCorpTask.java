package com.jeesite.modules.job.task;


import com.jeesite.modules.config.TenantContext;
import com.jeesite.modules.swm.cache.DeviceCorpMappingCache;
import com.jeesite.modules.constant.SwmRedisConstant;
import com.jeesite.modules.swm.entity.SwmHelmetDevice;
import com.jeesite.modules.swm.service.SwmHelmetDeviceService;
import com.jeesite.modules.sys.entity.User;
import com.jeesite.modules.sys.service.UserService;
import com.jeesite.modules.sys.utils.CorpUtils;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.*;


/**
 * 维护设备公司信息
 */

@Slf4j
@Component
public class DeviceCorpTask {

    @Autowired
    private SwmHelmetDeviceService deviceService;
    @Autowired
    private UserService userService;
    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private DeviceCorpMappingCache deviceCorpMappingCache;

    @XxlJob("deviceCorpMapping")
    @Transactional(rollbackFor = Exception.class)
    public void deviceCorpMapping() {

        List<User> corpList = userService.findCorpList(new User());
        if (CollectionUtils.isEmpty(corpList)) {
            XxlJobHelper.log("没有租户信息");
            return;
        }

        List<SwmHelmetDevice> deviceListAll = new ArrayList<>();

        for (User user : corpList) {
            try {
                String corpCode = user.getCorpCode();
                String corpName = user.getCorpName();

                // 设置当前线程租户
                CorpUtils.setCurrentCorpCode(corpCode, corpName);
                TenantContext.set(corpCode);

                SwmHelmetDevice device = new SwmHelmetDevice();
                device.setRandom(new Random().nextInt(1_000_000));  // 防止一级缓存

                List<SwmHelmetDevice> deviceList = deviceService.findDeviceCorpMapping(device);

                if (CollectionUtils.isEmpty(deviceList)) {
                    XxlJobHelper.log("租户 {} 没有设备", corpCode);
                    continue;
                }
                deviceListAll.addAll(deviceList);
            } catch (Exception e) {
                XxlJobHelper.log("租户 {} 获取设备列表异常", user.getCorpCode(), e);
            } finally {
                CorpUtils.removeCurrentCorpCode(null);
                TenantContext.clear();
            }

            // 构建一个临时 key
            String tempKey = SwmRedisConstant.RedisGlobalKey.DEVICE_TO_CORP + "_TMP";

            // 先写入临时 key
            Map<String, String> map = new HashMap<>();
            for (SwmHelmetDevice d : deviceListAll) {
                map.put(d.getDeviceId(), d.getCorpCode());
            }
            redisTemplate.opsForHash().putAll(tempKey, map);

            // 原子替换旧 key
            redisTemplate.execute((RedisCallback<Object>) connection -> {
                byte[] temp = tempKey.getBytes(StandardCharsets.UTF_8);
                byte[] real = SwmRedisConstant.RedisGlobalKey.DEVICE_TO_CORP.getBytes(StandardCharsets.UTF_8);

                connection.rename(temp, real); // 原子操作
                return null;
            });

            XxlJobHelper.log("设备租户映射关系生成完成");
            // 刷新缓存
            deviceCorpMappingCache.refreshCache();
        }
    }

}
