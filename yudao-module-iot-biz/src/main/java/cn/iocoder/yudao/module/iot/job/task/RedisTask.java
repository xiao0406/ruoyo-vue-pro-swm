package cn.iocoder.yudao.module.iot.job.task;

import cn.iocoder.yudao.framework.common.biz.system.tenant.TenantCommonApi;
import cn.iocoder.yudao.module.iot.cache.DeviceTenantMappingCache;
import cn.iocoder.yudao.module.iot.cache.service.RedisService;
import cn.iocoder.yudao.module.swm.api.constant.SwmRedisKeyConstants;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Set;

@Slf4j
@Component
public class RedisTask {

    @Resource
    private RedisService redisService;
    @Resource
    private DeviceTenantMappingCache deviceTenantMappingCache;
    @Resource
    private TenantCommonApi tenantApi;

    @XxlJob("cleanExpiredDevices")
    public void cleanExpiredDevices() {
        Date startTime = new Date();
        SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        XxlJobHelper.log("Start cleaning expired IoT device online keys: {}", dateTimeFormat.format(startTime));

        List<Long> tenantIds = tenantApi.getTenantIdList();
        if (CollectionUtils.isEmpty(tenantIds)) {
            XxlJobHelper.log("No tenants found, skip cleanup");
            return;
        }

        for (Long tenantId : tenantIds) {
            String tenantKey = String.valueOf(tenantId);
            try {
                Set<Object> deviceIds = redisService.sGet(tenantKey + SwmRedisKeyConstants.IotKey.ONLINE_DEVICES_KEY);
                if (deviceIds == null || deviceIds.isEmpty()) {
                    XxlJobHelper.log("Tenant {} has no online devices", tenantKey);
                    continue;
                }

                for (Object deviceIdObj : deviceIds) {
                    String deviceId = String.valueOf(deviceIdObj);
                    String ttlKey = tenantKey + SwmRedisKeyConstants.IotKey.DEVICE_TTL_KEY_PREFIX + deviceId;
                    if (!redisService.hasKey(ttlKey)) {
                        long removed = redisService.setRemove(
                                tenantKey + SwmRedisKeyConstants.IotKey.ONLINE_DEVICES_KEY, deviceId);
                        log.info("Tenant [{}] device [{}] TTL expired, removed: {}", tenantKey, deviceId, removed);
                        XxlJobHelper.log("Tenant [{}] device [{}] TTL expired, removed: {}", tenantKey, deviceId, removed);
                    }
                }
            } catch (Exception e) {
                log.error("Tenant [{}] cleanup expired online devices failed: {}", tenantKey, e.getMessage(), e);
                XxlJobHelper.log("Tenant [{}] cleanup failed: {}", tenantKey, e.getMessage());
            }
        }

        XxlJobHelper.log("Finished cleaning expired IoT device online keys");
    }

    @XxlJob("refreshCacheDeviceTenantMapping")
    public void refreshCacheDeviceTenantMapping() {
        try {
            deviceTenantMappingCache.refreshCache();
        } catch (Exception e) {
            log.error("Refresh device tenant mapping cache failed", e);
        }
    }
}
