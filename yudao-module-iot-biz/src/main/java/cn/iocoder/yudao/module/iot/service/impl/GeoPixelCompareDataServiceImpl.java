package cn.iocoder.yudao.module.iot.service.impl;

import cn.iocoder.yudao.module.iot.service.GeoPixelCompareDataService;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

/**
 * GeoPixel 比对数据服务实现
 */
@Service
@Validated
public class GeoPixelCompareDataServiceImpl implements GeoPixelCompareDataService {

    @Override
    public boolean hasRecentBleSourceForTcp(String beaconMac, long timestamp) {
        // TODO: 实现 BLE 数据源判断逻辑
        return false;
    }
}
