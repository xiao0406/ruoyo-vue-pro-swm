package com.jeesite.modules.swm.cache;


import com.jeesite.common.mybatis.mapper.query.QueryType;
import com.jeesite.modules.cache.service.RedisService;
import com.jeesite.modules.swm.constant.SwmRedisConstant;
import com.jeesite.modules.swm.entity.SwmHazardSource;
import com.jeesite.modules.swm.entity.SwmHelmetDevice;
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
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.stream.Collectors;


/*
 * 危险源缓存服务
 */
@Service
@Slf4j
public class SwmHazardSourceCache implements ApplicationListener<ApplicationReadyEvent> {

    @Autowired
    private RedisService redisService;
    @Autowired
    private SwmHazardSourceService swmHazardSourceService;
    @Autowired
    private SwmHelmetDeviceService swmHelmetDeviceService;
    @Autowired
    private UserService userService;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        initHazardSourceCache();
    }

    /**
     * 程序启动时初始化危险源缓存服务
     * 延迟初始化，避免循环依赖问题
     *
     */
//    @PostConstruct
    public void initHazardSourceCache() {

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
                log.info("开始初始化危险源缓存...");
                //这里是强制刷新缓存，避免缓存数据不一致
                SwmHazardSource swmHazardSource1 = new SwmHazardSource();
                int random = new Random().nextInt(1000);
                swmHazardSource1.getSqlMap().getWhere().and("1", QueryType.NE, random);
                //查询危险源数据
                List<SwmHazardSource> list = swmHazardSourceService.findList(swmHazardSource1);
                if (list == null || list.isEmpty()) {
                    log.warn("未查询到危险源数据");
                    return;
                }
                // 清除旧缓存
                redisService.del(corpCode+SwmRedisConstant.RedisSwmKey.Hazard_ISALARM_BEACON);

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
                query.setRandom(new Random().nextInt(1_000_000));
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
                    redisService.hset(corpCode+SwmRedisConstant.RedisSwmKey.Hazard_ISALARM_BEACON, entry.getKey(), entry.getValue());
                }


            } catch (Exception e) {
                log.error("初始化在职人员缓存失败", e);
            }finally {
                CorpUtils.setCurrentCorpCode(null,null);
            }
        }

    }


    /**
     * 获取缓存数据
     */

//    public Set<String> getHazardSourceCache(String  hazardId){
//        Set<String> devices = new HashSet<>();
//        // 从 Redis Hash 获取该危险源对应的设备集合
//        Object cacheObj = redisService.hget(SwmRedisConstant.RedisSwmKey.Hazard_ISALARM_BEACON, hazardId);
//
//        if (cacheObj instanceof Set) {
//            devices = (Set<String>) cacheObj;
//        }
//        return devices;
//    }

    /**
     * 插入缓存数据
     * @param hazardId
     */
    public void insertHazardSourceCache(String hazardId,String filterIdentityCard){
        String currentCorpCode = CorpUtils.getCurrentCorpCode();
        //分割为多个人员身份证
        String[] identityCard = filterIdentityCard.split(",");
        //通过人员身份证去查询设备id ,deviceId
        SwmHelmetDevice query = new SwmHelmetDevice();
        query.getSqlMap().getWhere().and("assigned_person", QueryType.IN, identityCard);
        query.setStatus(SwmHelmetDevice.STATUS_NORMAL);
        List<SwmHelmetDevice> list = swmHelmetDeviceService.findList(query);
        Set<String> deviceIds = list.stream().map(SwmHelmetDevice::getDeviceId).filter(StringUtils::isNotBlank).collect(Collectors.toSet());

        redisService.hset(currentCorpCode+SwmRedisConstant.RedisSwmKey.Hazard_ISALARM_BEACON, hazardId, deviceIds);
    }


}
