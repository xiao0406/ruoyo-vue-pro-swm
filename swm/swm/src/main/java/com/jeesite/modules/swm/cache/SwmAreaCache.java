package com.jeesite.modules.swm.cache;


import com.jeesite.modules.cache.service.RedisService;
import com.jeesite.modules.config.TenantContext;
import com.jeesite.modules.constant.SwmRedisConstant;
import com.jeesite.modules.entity.SwmArea;
import com.jeesite.modules.swm.service.SwmAreaService;
import com.jeesite.modules.sys.entity.User;
import com.jeesite.modules.sys.service.UserService;
import com.jeesite.modules.sys.utils.CorpUtils;
import com.xxl.job.core.context.XxlJobHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/*
 * 工作区域缓存服务
 */
@Service
@Slf4j
public class SwmAreaCache implements ApplicationListener<ApplicationReadyEvent> {

    @Autowired
    private RedisService redisService;
    @Autowired
    private SwmAreaService swmAreaService;
    @Autowired
    private UserService userService;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        initAreaCache();
    }

    /**
     * 程序启动时初始化危险源缓存服务
     * 延迟初始化，避免循环依赖问题
     *
     */
//    @PostConstruct
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
//            CorpUtils.setCurrentCorpCode(corpCode, corpName);
            TenantContext.set(corpCode);
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

                //设置缓存
                String areaCacheKey = corpCode+ SwmRedisConstant.RedisSwmKey.AREA_ID_CACHE_KEY;
                redisService.del(areaCacheKey);
                for (SwmArea area : list) {
                    redisService.hset(areaCacheKey, area.getId(), area);
                }


            } catch (Exception e) {
                log.error("初始化工作缓存失败", e);
            }finally {
//                CorpUtils.removeCurrentCorpCode(null);
                TenantContext.clear();
            }
        }
    }

    /**
     * 插入区域缓存（优化健壮版）
     */
    public void insertAreaCache(SwmArea swmArea) {
        if (swmArea == null || swmArea.getId() == null || swmArea.getAreaName() == null) {
            log.warn("[区域缓存] 参数为空，不执行缓存");
            return;
        }

        String currentCorpCode = CorpUtils.getCurrentCorpCode();
        String areaCacheKey = currentCorpCode + SwmRedisConstant.RedisSwmKey.AREA_CACHE;
        String areaIdCacheKey = currentCorpCode + SwmRedisConstant.RedisSwmKey.AREA_ID_CACHE_KEY;

        try {
            // ====================== 1. 维护区域名称列表缓存 ======================
            //生产区域才添加
            if (SwmArea.STATUS_NORMAL.equals(swmArea.getAreaType())){
                List<String> areaNameList;
                Object cacheObj = redisService.get(areaCacheKey);

                // 缓存存在 → 强转使用；不存在 → 新建集合
                if (cacheObj instanceof List<?>) {
                    areaNameList = (List<String>) cacheObj;
                } else {
                    areaNameList = new ArrayList<>();
                }

                // 避免重复添加
                if (!areaNameList.contains(swmArea.getAreaName())) {
                    areaNameList.add(swmArea.getAreaName());
                    // 重新覆盖缓存
                    redisService.set(areaCacheKey, areaNameList);
                }
            }

            // ====================== 2. 维护区域ID哈希缓存 ======================
            redisService.hset(areaIdCacheKey, swmArea.getId(), swmArea);

            log.info("[区域缓存] 插入成功，区域ID：{}，区域名称：{}",
                    swmArea.getId(), swmArea.getAreaName());

        } catch (Exception e) {
            log.error("[区域缓存] 插入缓存异常，区域ID：{}，异常：", swmArea.getId(), e);
        }
    }

    /**
     * 删除区域缓存数据
     */
    public void delete(SwmArea swmArea) {
        // 空值安全校验
        if (swmArea == null || swmArea.getId() == null || swmArea.getAreaName() == null) {
            log.warn("[删除区域缓存] 参数为空，跳过执行");
            return;
        }

        String areaName = swmArea.getAreaName();
        String corpCode = CorpUtils.getCurrentCorpCode();
        String areaCacheKey = corpCode + SwmRedisConstant.RedisSwmKey.AREA_CACHE;
        String areaIdCacheKey = corpCode + SwmRedisConstant.RedisSwmKey.AREA_ID_CACHE_KEY;

        try {
            // 1. 删除区域名称列表中的名称
            Object cacheObj = redisService.get(areaCacheKey);
            if (cacheObj instanceof List<?>) {
                List<String> areaNameList = (List<String>) cacheObj;
                // 移除并判断是否需要更新回Redis
                if (areaNameList.remove(areaName)) {
                    redisService.set(areaCacheKey, areaNameList);
                }
            }

            // 2. 删除哈希结构里的区域对象
            redisService.hdel(areaIdCacheKey, swmArea.getId());

            log.info("[删除区域缓存] 执行成功，区域ID：{}，区域名：{}", swmArea.getId(), areaName);

        } catch (Exception e) {
            log.error("[删除区域缓存] 执行异常，区域ID：{}", swmArea.getId(), e);
        }
    }
}
