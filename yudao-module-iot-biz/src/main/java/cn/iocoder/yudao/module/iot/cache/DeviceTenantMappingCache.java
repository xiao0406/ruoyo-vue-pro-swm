package cn.iocoder.yudao.module.iot.cache;

import cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder;
import cn.iocoder.yudao.module.swm.api.enums.TenantDbEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Device tenant mapping cache.
 */
@Slf4j
@Component
public class DeviceTenantMappingCache {

    public void refreshCache() {
        log.info("Refresh device tenant mapping cache");
    }

    public Long getTenantId(String deviceId) {
        return TenantContextHolder.getTenantId();
    }

    public String getTenantKey(String deviceId) {
        Long tenantId = getTenantId(deviceId);
        return tenantId == null ? "" : String.valueOf(tenantId);
    }

    public String getDbName(String deviceId) {
        Long tenantId = getTenantId(deviceId);
        if (tenantId == null) {
            log.warn("Cannot resolve TDengine database because tenantId is empty, deviceId={}", deviceId);
            return null;
        }
        // TDengine databases are still project-specific, so map RuoYi tenantId to the migrated DB name.
        return TenantDbEnum.getDbNameByTenantId(tenantId);
    }
}
