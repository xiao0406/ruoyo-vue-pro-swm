package com.jeesite.modules.swm.cache;


import com.alibaba.cloud.commons.lang.StringUtils;
import com.jeesite.modules.cache.service.RedisService;
import com.jeesite.modules.config.TenantContext;
import com.jeesite.modules.constant.SwmRedisConstant;
import com.jeesite.modules.entity.SwmBeaconStation;
import com.jeesite.modules.swm.service.SwmBeaconStationService;
import com.jeesite.modules.sys.entity.User;
import com.jeesite.modules.sys.service.UserService;
import com.jeesite.modules.sys.utils.CorpUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Lazy;
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
    @Lazy
    private SwmBeaconStationService stationService;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        initAreaCache();
    }


    public void initAreaCache() {

        log.info("开始初始化设备缓存（全局）...");


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

            //清理缓存
            redisService.del(corpCode + SwmRedisConstant.RedisSwmKey.MAJOR_MINOR_TO_MAC);
            redisService.del(corpCode + SwmRedisConstant.RedisSwmKey.BEACON_MAC_CACHE_KEY);

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
                    if (StringUtils.isNotBlank(beaconStation.getMajor())){
                        redisService.hset(corpCode +SwmRedisConstant.RedisSwmKey.MAJOR_MINOR_TO_MAC, redisKey, beaconStation.getBeaconId());
                        log.info("写入缓存 key={},value={}", redisKey,beaconStation.getBeaconId());
                    }

                    //添加信标详细信息
                    String macInfoKey = corpCode + SwmRedisConstant.RedisSwmKey.BEACON_MAC_CACHE_KEY;
                    redisService.hset(macInfoKey, beaconStation.getBeaconId(), beaconStation);
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

    /**
     * 新增方法
     */
    public void insertBeacon(SwmBeaconStation station) {
        String corpCode = TenantContext.get();
        redisService.hset(corpCode + SwmRedisConstant.RedisSwmKey.BEACON_MAC_CACHE_KEY, station.getBeaconId(),station);
    }

    /**
     * 删除方法
     */
    public void deleteBeacon(SwmBeaconStation station) {
        String corpCode = TenantContext.get();
        redisService.hdel(corpCode +SwmRedisConstant.RedisSwmKey.MAJOR_MINOR_TO_MAC, station.getMajor() + "_" + station.getMinor());
        redisService.hdel(corpCode +SwmRedisConstant.RedisSwmKey.BEACON_MAC_CACHE_KEY, station.getBeaconId());
    }
}
