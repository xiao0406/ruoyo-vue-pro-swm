package cn.iocoder.yudao.module.swm.service.cache;

import cn.iocoder.yudao.module.swm.dal.dataobject.SwmHelmetDeviceDO;
import cn.iocoder.yudao.module.swm.dal.mysql.SwmHelmetDeviceMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 设备 → 租户映射缓存
 * <p>
 * 旧名 DeviceTenantMappingCache，已将 tenantId 替换为 tenantId。
 * TDengine 库名由 {@link cn.iocoder.yudao.module.swm.enums.TenantDbEnum#getDbNameByTenantId(Long)} 解析。
 */
@Slf4j
@Component
public class DeviceTenantMappingCache {

    /**
     * deviceId -> tenantId
     */
    private final Map<String, Long> deviceTenantMap = new ConcurrentHashMap<>();

    @Resource
    private SwmHelmetDeviceMapper swmHelmetDeviceMapper;

    public void refreshCache() {
        List<SwmHelmetDeviceDO> devices = swmHelmetDeviceMapper.selectList(new LambdaQueryWrapper<>());
        deviceTenantMap.clear();
        for (SwmHelmetDeviceDO device : devices) {
            if (StringUtils.isBlank(device.getDeviceId()) || device.getTenantId() == null) {
                continue;
            }
            deviceTenantMap.put(device.getDeviceId(), device.getTenantId());
        }
        log.info("Device tenant mapping cache refreshed, size={}", deviceTenantMap.size());
    }

    /**
     * 根据 deviceId 获取 tenantId
     */
    public Long getTenantIdByDeviceId(String deviceId) {
        if (StringUtils.isBlank(deviceId)) {
            return null;
        }
        Long tenantId = deviceTenantMap.get(deviceId);
        if (tenantId != null) {
            return tenantId;
        }
        SwmHelmetDeviceDO device = swmHelmetDeviceMapper.selectOne(
                new LambdaQueryWrapper<SwmHelmetDeviceDO>()
                        .eq(SwmHelmetDeviceDO::getDeviceId, deviceId)
                        .last("LIMIT 1"));
        if (device == null || device.getTenantId() == null) {
            return null;
        }
        tenantId = device.getTenantId();
        deviceTenantMap.put(deviceId, tenantId);
        return tenantId;
    }

    public String getDbName(String deviceId) {
        Long tenantId = getTenantIdByDeviceId(deviceId);
        return tenantId == null ? null : cn.iocoder.yudao.module.swm.enums.TenantDbEnum.getDbNameByTenantId(tenantId);
    }

}
