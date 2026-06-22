package cn.iocoder.yudao.module.iot.service.impl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.iocoder.yudao.module.iot.service.LocationEngineService;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 定位引擎服务实现
 */
@Service
@Validated
public class LocationEngineServiceImpl implements LocationEngineService {

    @Override
    public Object getCoordinate(Object... args) {
        // TODO: 实现定位引擎逻辑
        return null;
    }

    @Override
    public Map<String, Object> callLocationEngine(String deviceId, List<Map<String, Object>> beaconDataList, Long scanTimestamp) {
        return callLocationEngine(deviceId, beaconDataList, scanTimestamp, null);
    }

    @Override
    public Map<String, Object> callLocationEngine(String deviceId, List<Map<String, Object>> beaconDataList, Long scanTimestamp, String rawMessage) {
        // TODO: 实现定位引擎调用逻辑
        log.warn("callLocationEngine 尚未实现, deviceId={}", deviceId);
        Map<String, Object> result = new HashMap<>();
        result.put("engine_status", "not_implemented");
        return result;
    }

    @Override
    public String getEngineType() {
        return "jiai";
    }

    @Override
    public boolean isChatEngineAvailable() {
        return false;
    }

    @Override
    public boolean isHistoryEnabled() {
        return false;
    }

    @Override
    public int getHistorySeconds() {
        return 5;
    }
}
