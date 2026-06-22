package cn.iocoder.yudao.module.iot.job.task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import cn.iocoder.yudao.module.iot.cache.DeviceCorpMappingCache;
import cn.iocoder.yudao.module.iot.cache.service.RedisService;
import cn.iocoder.yudao.module.swm.api.constant.SwmRedisKeyConstants;
import cn.iocoder.yudao.module.system.dal.dataobject.tenant.TenantDO;
import cn.iocoder.yudao.module.system.service.tenant.TenantService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import org.springframework.util.CollectionUtils;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Set;


/**
 * redis定时任务
 */
@Component
public class RedisTask {
private static final Logger log = LoggerFactory.getLogger(RedisTask.class);

    @Resource
    private RedisService redisService;

    @Resource
    private DeviceCorpMappingCache deviceCorpMappingCache;
    @Resource
    private TenantService tenantService;

        /**
        * 定时清理过期设备（每分钟执行）
        */
        @XxlJob("cleanExpiredDevices")
        public void cleanExpiredDevices() {
            Date startTime = new Date();
            SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

            XxlJobHelper.log("========== 开始执行清理过期设备定时任务 ==========");
            XxlJobHelper.log("任务开始时间: " + dateTimeFormat.format(startTime));

            // 获取系统所有租户信息
            List<TenantDO> corpList = tenantService.getTenantIdList().stream()
                    .map(tenantService::getTenant)
                    .filter(tenant -> tenant != null)
                    .toList();
            if (CollectionUtils.isEmpty(corpList)) {
                XxlJobHelper.log("没有租户信息");
                return;
            }

            for (TenantDO tenant : corpList) {
                String corpCode = tenant.getId().toString();

                try {
                    Set<Object> deviceIds = redisService.sGet(corpCode + SwmRedisKeyConstants.IotKey.ONLINE_DEVICES_KEY);

                    // 注意：不能 return，否则其他租户无法处理
                    if (deviceIds == null || deviceIds.isEmpty()) {
                        XxlJobHelper.log("租户 {} 没有在线设备", corpCode);
                        continue;
                    }

                    for (Object deviceIdObj : deviceIds) {
                        String deviceId = String.valueOf(deviceIdObj);
                        String ttlKey = corpCode + SwmRedisKeyConstants.IotKey.DEVICE_TTL_KEY_PREFIX + deviceId;

                        if (!redisService.hasKey(ttlKey)) {
                            long removed = redisService.setRemove(
                                    corpCode + SwmRedisKeyConstants.IotKey.ONLINE_DEVICES_KEY,
                                    deviceId
                            );

                            log.info("租户 [{}] 设备 [{}] TTL 过期，已移出在线集合, 数量: {}", corpCode, deviceId, removed);
                            XxlJobHelper.log("租户 [{}] 设备 [{}] TTL 过期，已移出在线集合, 数量: {}", corpCode, deviceId, removed);
                        }
                    }
                } catch (Exception e) {
                    log.error("租户 [{}] 自动清理过期设备失败: {}", corpCode, e.getMessage(), e);
                    XxlJobHelper.log("租户 [{}] 自动清理过期设备失败: {}", corpCode, e.getMessage());
                    // 不 return，让下一个租户继续执行
                    continue;
                }
            }

            XxlJobHelper.log("========== 清理过期设备任务执行完毕 ==========");
        }



    @XxlJob("refreshCacheDeviceCorpMapping")
    public void refreshCacheDeviceCorpMapping() {
        try {
            deviceCorpMappingCache.refreshCache();
        } catch (Exception e) {
            log.error("刷新设备数据库和租户缓存失败", e);
        }
    }
}
