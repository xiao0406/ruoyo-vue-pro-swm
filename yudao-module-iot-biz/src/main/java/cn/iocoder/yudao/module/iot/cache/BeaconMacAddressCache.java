package cn.iocoder.yudao.module.iot.cache;

import cn.iocoder.yudao.module.iot.dal.dataobject.BeaconStationDO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Beacon MAC 地址缓存
 */
@Component
@Slf4j
public class BeaconMacAddressCache {

    /**
     * 根据MAC地址获取信标信息
     */
    public BeaconStationDO getBeaconStation(String macAddress) {
        // TODO: 实现从Redis缓存获取信标信息
        log.warn("getBeaconStation 尚未实现, mac={}", macAddress);
        return null;
    }
}
