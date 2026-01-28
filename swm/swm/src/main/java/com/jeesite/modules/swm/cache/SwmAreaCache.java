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
import com.jeesite.modules.sys.entity.User;
import com.jeesite.modules.sys.service.UserService;
import com.jeesite.modules.sys.utils.CorpUtils;
import com.xxl.job.core.context.XxlJobHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
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
    @Autowired
    private UserService userService;

    /**
     * 程序启动时初始化危险源缓存服务
     * 延迟初始化，避免循环依赖问题
     *
     */
    @PostConstruct
    public void initAreaCache() {


        //获取系统所有租户信息
        List<User> corpList = userService.findCorpList(new User());
        if (CollectionUtils.isEmpty(corpList)) {
            XxlJobHelper.log("没有租户信息");
            return;
        }

        for (User user : corpList) {
            String corpCode = user.getCorpCode();
            String corpName = user.getCorpName();
            //设置当前线程的租户信息
            CorpUtils.setCurrentCorpCode(corpCode, corpName);
            XxlJobHelper.log("开始处理租户：{} ========================", corpCode);

            try {
                log.info("开始初始化工作区域缓存...");
                //查询工作区域数据
                SwmArea swmArea = new SwmArea();
                swmArea.setAreaType(SwmArea.STATUS_NORMAL);
                int random = new Random().nextInt(1000);
                swmArea.setRandom( random);
                List<SwmArea> list = swmAreaService.findList(swmArea);
                if (list == null || list.isEmpty()) {
                    log.warn("未查询到工作区数据");
                    continue;
                }
                // 清除旧缓存
                redisService.del(corpCode+ SwmRedisConstant.RedisSwmKey.AREA_CACHE);
                List<String> ids = list.stream().map(SwmArea::getId).collect(Collectors.toList());
                redisService.set(corpCode+ SwmRedisConstant.RedisSwmKey.AREA_CACHE, ids);
            } catch (Exception e) {
                log.error("初始化工作缓存失败", e);
            }finally {
                CorpUtils.setCurrentCorpCode(null,null);
            }
        }
    }

    /**
     * 插入缓存数据
     * @param
     */

    public void insertAreaCache(String areaName){
            String currentCorpCode = CorpUtils.getCurrentCorpCode();
            Object object = redisService.get(currentCorpCode+SwmRedisConstant.RedisSwmKey.AREA_CACHE);
            if (object != null) {
                List<String> list = (List<String>) object;
                list.add(areaName);
                redisService.set(currentCorpCode+SwmRedisConstant.RedisSwmKey.AREA_CACHE, list);
            }
    }
}
