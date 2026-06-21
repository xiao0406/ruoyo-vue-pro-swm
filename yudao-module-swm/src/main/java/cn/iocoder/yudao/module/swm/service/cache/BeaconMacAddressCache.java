package cn.iocoder.yudao.module.swm.service.cache;

import cn.iocoder.yudao.module.swm.dal.dataobject.SwmBeaconStationDO;
import cn.iocoder.yudao.module.swm.dal.mysql.SwmBeaconStationMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class BeaconMacAddressCache {

    private final Set<String> beaconMacAddressSet = ConcurrentHashMap.newKeySet();

    @Resource
    private SwmBeaconStationMapper swmBeaconStationMapper;

    public void refreshCache() {
        beaconMacAddressSet.clear();
        for (SwmBeaconStationDO station : swmBeaconStationMapper.selectList(new LambdaQueryWrapper<>())) {
            if (StringUtils.isNotBlank(station.getBeaconId())) {
                beaconMacAddressSet.add(station.getBeaconId());
            }
        }
        log.info("Beacon MAC cache refreshed, size={}", beaconMacAddressSet.size());
    }

    public boolean contains(String macAddress) {
        return StringUtils.isNotBlank(macAddress) && beaconMacAddressSet.contains(macAddress);
    }

    public Set<String> getAll() {
        return Collections.unmodifiableSet(beaconMacAddressSet);
    }

}
