package cn.iocoder.yudao.module.iot.cache;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.module.iot.cache.service.RedisService;
import cn.iocoder.yudao.module.iot.dal.dataobject.BeaconStationDO;
import cn.iocoder.yudao.module.iot.tcp.service.SwmBeaconStation;
import cn.iocoder.yudao.module.swm.api.constant.SwmRedisKeyConstants;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

/**
 * Beacon MAC address cache.
 */
@Component
public class BeaconMacAddressCache {

    @Resource
    private RedisService redisService;
    @Resource
    private DeviceTenantMappingCache deviceTenantMappingCache;

    /**
     * Resolve beacon information from the tenant-scoped SWM beacon Redis cache.
     */
    public BeaconStationDO getBeaconStation(String macAddress) {
        if (StrUtil.isBlank(macAddress)) {
            return null;
        }
        String tenantKey = deviceTenantMappingCache.getTenantKey(null);
        Object value = redisService.hget(tenantKey + SwmRedisKeyConstants.SwmKey.BEACON_MAC_CACHE_KEY,
                macAddress.toUpperCase());
        if (value instanceof BeaconStationDO beaconStation) {
            return beaconStation;
        }
        if (value instanceof SwmBeaconStation swmBeaconStation) {
            return toBeaconStationDO(swmBeaconStation);
        }
        return null;
    }

    private BeaconStationDO toBeaconStationDO(SwmBeaconStation source) {
        BeaconStationDO target = new BeaconStationDO();
        target.setId(source.getId());
        target.setTenantId(source.getTenantId());
        target.setBeaconId(source.getBeaconId());
        target.setDeviceName(source.getDeviceName());
        target.setBeaconType(source.getBeaconType());
        target.setLocation(source.getLocation());
        target.setArea(source.getArea());
        target.setMac(source.getBeaconId());
        return target;
    }
}
