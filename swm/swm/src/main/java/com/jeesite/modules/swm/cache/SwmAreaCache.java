package com.jeesite.modules.swm.cache;


import com.jeesite.common.mybatis.mapper.query.QueryType;
import com.jeesite.modules.cache.service.RedisService;
import com.jeesite.modules.swm.constant.SwmRedisConstant;
import com.jeesite.modules.swm.entity.SwmArea;
import com.jeesite.modules.swm.entity.SwmHazardSource;
import com.jeesite.modules.swm.entity.SwmHelmetDevice;
import com.jeesite.modules.swm.service.SwmAreaService;
import com.jeesite.modules.swm.service.SwmHazardSourceService;
import com.jeesite.modules.swm.service.SwmHelmetDeviceService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.stream.Collectors;

/*
 * 工作区域缓存服务
 */
@Service
@Slf4j
public class SwmAreaCache {

    @Autowired
    private RedisService redisService;
    @Autowired
    private SwmAreaService swmAreaService;


    /**
     * 程序启动时初始化危险源缓存服务
     * 延迟初始化，避免循环依赖问题
     *
     */
    @PostConstruct
    public void initAreaCache() {
        try {
            log.info("开始初始化工作区域缓存...");

            //查询工作区域数据
            SwmArea swmArea = new SwmArea();
            swmArea.setAreaType(SwmArea.STATUS_NORMAL);
            List<SwmArea> list = swmAreaService.findList(swmArea);
            if (list == null || list.isEmpty()) {
                log.warn("未查询到工作区数据");
                return;
            }
            // 清除旧缓存
            redisService.del(SwmRedisConstant.AreaCache.AREA_CACHE);
            List<String> areaNames = list.stream().map(SwmArea::getAreaName).collect(Collectors.toList());
            redisService.set(SwmRedisConstant.AreaCache.AREA_CACHE, areaNames);
        } catch (Exception e) {
            log.error("初始化工作缓存失败", e);
        }
    }

    /**
     * 插入缓存数据
     * @param
     */
    public void insertAreaCache(String areaName){
        Object object = redisService.get(SwmRedisConstant.AreaCache.AREA_CACHE);
        if (object != null) {
            List<String> list = (List<String>) object;
            list.add(areaName);
            redisService.set(SwmRedisConstant.AreaCache.AREA_CACHE, list);
        }
    }
}
