package com.jeesite.modules.job.task;


import com.jeesite.modules.swm.cache.DeviceCorpMappingCache;
import com.jeesite.modules.swm.constant.SwmRedisConstant;
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
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;


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

        XxlJobHelper.log("定时生成设备租户的映射关系===================================");
        //获取系统所有租户信息
        List<User> corpList = userService.findCorpList(new User());
        if (CollectionUtils.isEmpty(corpList)) {
            XxlJobHelper.log("没有租户信息");
            return;
        }

        List<SwmHelmetDevice> deviceListAll = new ArrayList<>();

        //查询每个租户的设备信息
        for (User user : corpList) {
            String corpCode = user.getCorpCode();
            String corpName = user.getCorpName();
            //设置当前线程的租户信息
            CorpUtils.setCurrentCorpCode(corpCode, corpName);
            SwmHelmetDevice device = new SwmHelmetDevice();
            // 传随机值，使 SQL 每次不同，目的是取消一级缓存
            device.setRandom(new Random().nextInt(1_000_000));
            List<SwmHelmetDevice> deviceList = deviceService.findDeviceCorpMapping(device);

            if (CollectionUtils.isEmpty(deviceList)) {
                XxlJobHelper.log("租户 {} 没有设备", corpCode);
                continue;
            }
            deviceListAll.addAll(deviceList);
        }

        // 写入 Redis：设备 -> 租户
        for (SwmHelmetDevice d : deviceListAll) {
            String deviceId = d.getDeviceId();
            String corpCode = d.getCorpCode();
            redisTemplate.opsForHash().put(SwmRedisConstant.RedisGlobalKey.DEVICE_TO_CORP, deviceId, corpCode);
        }

        XxlJobHelper.log("设备租户映射关系生成完成");

        // 刷新缓存
        deviceCorpMappingCache.refreshCache();

    }
}
