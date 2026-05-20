package com.jeesite.modules.swm.cache;


import com.jeesite.modules.cache.service.RedisService;
import com.jeesite.modules.config.TenantContext;
import com.jeesite.modules.constant.SwmRedisConstant;
import com.jeesite.modules.swm.entity.SwmHelmetDevice;
import com.jeesite.modules.swm.service.SwmHelmetDeviceService;
import com.jeesite.modules.sys.entity.User;
import com.jeesite.modules.sys.service.UserService;
import com.jeesite.modules.sys.utils.CorpUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Random;

/*
 * 设备品牌缓存服务
 */
@Service
@Slf4j
public class DeviceSourchCache implements ApplicationListener<ApplicationReadyEvent> {

    @Autowired
    private UserService userService;

    @Autowired
    @Lazy
    private SwmHelmetDeviceService deviceService;

    @Autowired
    private RedisService redisService;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        initAreaCache();
    }

    public void initAreaCache() {

        log.info("开始初始化设备缓存（全局）...");

        //清理缓存
        redisService.del(SwmRedisConstant.RedisGlobalKey.KLT_DEVICE_ID_ALL,
                SwmRedisConstant.RedisGlobalKey.ZT_DEVICE_ID_ALL);

        List<User> corpList = userService.findCorpList(new User());
        if (CollectionUtils.isEmpty(corpList)) {
            log.warn("没有租户信息");
            return;
        }

        for (User user : corpList) {

            String corpCode = user.getCorpCode();
            String corpName = user.getCorpName();

//            CorpUtils.setCurrentCorpCode(corpCode, corpName);
            TenantContext.set(corpCode);

            log.info("处理租户：{}", corpCode);

            try {
                // 科利特
                addDevice("0", SwmRedisConstant.RedisGlobalKey.KLT_DEVICE_ID_ALL);

                // 中泰（你之前写成2，这里按你字典确认）
                addDevice("1", SwmRedisConstant.RedisGlobalKey.ZT_DEVICE_ID_ALL);

            } catch (Exception e) {
                log.error("租户处理失败: {}", corpCode, e);
            } finally {
//                CorpUtils.removeCurrentCorpCode(null);
                TenantContext.clear();
            }
        }

        log.info("设备缓存初始化完成");
    }

    /**
     * 查询 + 写入 Redis（累加，不覆盖）
     */
    private void addDevice(String deviceSource, String redisKey) {
        SwmHelmetDevice device = new SwmHelmetDevice();
        device.setDeviceSource(deviceSource);
        device.setRandom(new Random().nextInt(1_000_000));

        List<String> deviceIds = deviceService.findDeviceIdsByDeviceSource(device);

        if (CollectionUtils.isEmpty(deviceIds)) {
            log.warn("未查询到设备 source={}", deviceSource);
            return;
        }
        // 写入 Set（自动去重）
        redisService.sSet(redisKey, deviceIds.toArray());
        log.info("写入缓存 key={} 数量={}", redisKey, deviceIds.size());
    }


    /**
     * 追加单个设备
     */
    public void appendDeviceId( String deviceSource,String deviceId) {
        // 加入 Set（自动去重）
        if ("0".equals(deviceSource)){
            redisService.sSet(SwmRedisConstant.RedisGlobalKey.KLT_DEVICE_ID_ALL, deviceId);
        }else if ("1".equals(deviceSource)){
            redisService.sSet(SwmRedisConstant.RedisGlobalKey.ZT_DEVICE_ID_ALL, deviceId);
        }
    }

    /**
     * 删除单个设备
     */
    public void removeDeviceId( String deviceSource,String deviceId) {
        // 从 Set 删除
        if ("0".equals(deviceSource)){
            redisService.setRemove(SwmRedisConstant.RedisGlobalKey.KLT_DEVICE_ID_ALL, deviceId);
        }else if ("1".equals(deviceSource)){
            redisService.setRemove(SwmRedisConstant.RedisGlobalKey.ZT_DEVICE_ID_ALL, deviceId);
        }
    }


}
