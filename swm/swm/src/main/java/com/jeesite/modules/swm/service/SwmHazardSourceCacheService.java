package com.jeesite.modules.swm.service;


import com.jeesite.common.entity.Page;
import com.jeesite.common.mybatis.mapper.query.QueryType;
import com.jeesite.modules.cache.service.RedisService;
import com.jeesite.modules.swm.constant.SwmRedisConstant;
import com.jeesite.modules.swm.entity.SwmHazardSource;
import com.jeesite.modules.swm.entity.SwmHelmetDevice;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.stream.Collectors;


/*
 * 危险源缓存服务
 */
@Service
@Slf4j
public class SwmHazardSourceCacheService {

    @Autowired
    private RedisService redisService;
    @Autowired
    private SwmHazardSourceService swmHazardSourceService;
    @Autowired
    private SwmHelmetDeviceService swmHelmetDeviceService;

    /**
     * 程序启动时初始化危险源缓存服务
     * 延迟初始化，避免循环依赖问题
     *
     */
    @PostConstruct
    public void initHazardSourceCache() {
        try {
            log.info("开始初始化危险源缓存...");

            //查询危险源数据
            List<SwmHazardSource> list = swmHazardSourceService.findList(new SwmHazardSource());
            if (list == null || list.isEmpty()) {
                log.warn("未查询到危险源数据");
                return;
            }
            // 清除旧缓存
            redisService.del(SwmRedisConstant.HazardSource.Hazard_ISALARM_BEACON);

            //缓存数据
            Map<String, Set<String>> hazardDeviceMap = new HashMap<>();

            //先获取到所有的身份证，为后续查设备id做准备
            Set<String> identityCardSet = new HashSet<>();
            for (SwmHazardSource source : list) {
                String filterIdentityCard = source.getFilterIdentityCard();
                if (StringUtils.isNotBlank(filterIdentityCard)) {
                    identityCardSet.addAll(Arrays.asList(filterIdentityCard.split(",")));
                }
            }

            //通过人员身份证去查询设备id ,deviceId
            SwmHelmetDevice query = new SwmHelmetDevice();
            if (!identityCardSet.isEmpty()) {
                List<String> identityCards = new ArrayList<>(identityCardSet);
                query.getSqlMap().getWhere().and("assigned_person", QueryType.IN, identityCards);
            }
            query.setStatus(SwmHelmetDevice.STATUS_NORMAL);
            List<SwmHelmetDevice> swmHelmetDevices = swmHelmetDeviceService.findList(query);

            //身份证和设备id对应关系
            Map<String,String> deviceMap = new HashMap<>();
            for (SwmHelmetDevice swmHelmetDevice : swmHelmetDevices) {
                deviceMap.put(swmHelmetDevice.getAssignedPerson(), swmHelmetDevice.getDeviceId());
            }

            //遍历危险源，获取人员与设备对应关系放入reids里
            for (SwmHazardSource swmHazardSource : list) {
                String filterIdentityCard = swmHazardSource.getFilterIdentityCard();
                if (StringUtils.isBlank(filterIdentityCard)){
                    continue;
                }
                String[] split = filterIdentityCard.split(",");
                Set<String> deviceIds = new HashSet<>();
                for (String s : split) {
                    String deviceId = deviceMap.get(s);
                    if (deviceId != null) {
                        deviceIds.add(deviceId);
                    }
                }
                hazardDeviceMap.put(swmHazardSource.getId(), deviceIds);
            }
            for (Map.Entry<String, Set<String>> entry : hazardDeviceMap.entrySet()) {
                redisService.hset(SwmRedisConstant.HazardSource.Hazard_ISALARM_BEACON, entry.getKey(), entry.getValue());
            }


        } catch (Exception e) {
            log.error("初始化在职人员缓存失败", e);
        }
    }


}
