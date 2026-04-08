package com.jeesite.modules.swm.cache;


import com.alibaba.cloud.commons.lang.StringUtils;
import com.jeesite.modules.cache.service.RedisService;
import com.jeesite.modules.config.TenantContext;
import com.jeesite.modules.constant.SwmRedisConstant;
import com.jeesite.modules.swm.entity.SwmBeaconStation;
import com.jeesite.modules.swm.entity.SwmHelmetDevice;
import com.jeesite.modules.swm.service.SwmBeaconStationService;
import com.jeesite.modules.sys.entity.User;
import com.jeesite.modules.sys.service.UserService;
import com.jeesite.modules.sys.utils.CorpUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Random;

/*
 * 信标相关缓存
 */
@Service
@Slf4j
public class SwmBeaconStationCache implements ApplicationListener<ApplicationReadyEvent> {


    @Autowired
    private RedisService redisService;
    @Autowired
    private UserService userService;
    @Autowired
    private SwmBeaconStationService stationService;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        initAreaCache();
    }


    public void initAreaCache() {

        log.info("开始初始化设备缓存（全局）...");

        //清理缓存
        redisService.del(SwmRedisConstant.RedisGlobalKey.MAJOR_MINOR_TO_MAC);

        List<User> corpList = userService.findCorpList(new User());
        if (CollectionUtils.isEmpty(corpList)) {
            log.warn("没有租户信息");
            return;
        }

        for (User user : corpList) {

            String corpCode = user.getCorpCode();
            String corpName = user.getCorpName();

            CorpUtils.setCurrentCorpCode(corpCode, corpName);
            TenantContext.set(corpCode);

            log.info("处理租户：{}", corpCode);

            try {

                SwmBeaconStation station = new SwmBeaconStation();
                station.setRandom(new Random().nextInt(1_000_000));

                List<SwmBeaconStation> stationList = stationService.findMacList(station);

                if (CollectionUtils.isEmpty(stationList)) {
                    log.warn("未查询到设备 source={}", stationList);
                    continue;
                }
                //MAJOR和MINOR作为唯一值，value作为mac地址
                for (SwmBeaconStation beaconStation : stationList) {
                    String redisKey = beaconStation.getMajor() + "_" + beaconStation.getMinor();
                    if (StringUtils.isNotBlank(redisKey)){
                        redisService.hset(SwmRedisConstant.RedisGlobalKey.MAJOR_MINOR_TO_MAC, redisKey, beaconStation.getBeaconId());
                        log.info("写入缓存 key={},value={}", redisKey,beaconStation.getBeaconId());
                    }
                }
            } catch (Exception e) {
                log.error("租户处理失败: {}", corpCode, e);
            } finally {
                CorpUtils.removeCurrentCorpCode(null);
                TenantContext.clear();
            }
        }

        log.info("设备缓存初始化完成");
    }
}
