package cn.iocoder.yudao.module.iot.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.module.iot.cache.service.RedisService;
import cn.iocoder.yudao.module.iot.service.GeoPixelCompareDataService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.lang.reflect.Method;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * Geo/BLE pixel comparison decision service.
 */
@Service
@Validated
public class GeoPixelCompareDataServiceImpl implements GeoPixelCompareDataService {

    private static final String DECISION_HASH_PREFIX = "iot:geo_pixel_compare:";
    private static final long RECENT_BLE_WINDOW_MILLIS = 60_000L;

    @Resource
    private RedisService redisService;

    @Override
    public boolean hasRecentBleSourceForTcp(String deviceId, long timestamp) {
        if (StrUtil.isBlank(deviceId)) {
            return false;
        }
        long baseTime = timestamp <= 0 ? System.currentTimeMillis() : timestamp;
        return getRecentDecisionRecordsForTcp(deviceId, 20).stream()
                .filter(record -> record.getBusinessTimestamp() != null)
                .filter(record -> Math.abs(baseTime - record.getBusinessTimestamp()) <= RECENT_BLE_WINDOW_MILLIS)
                .anyMatch(record -> "BLE".equalsIgnoreCase(record.getDecisionSource())
                        || "BLE_FALLBACK".equalsIgnoreCase(record.getDecisionSource()));
    }

    @Override
    public List<RecentDecisionRecord> getRecentDecisionRecordsForTcp(String deviceId, int limit) {
        if (StrUtil.isBlank(deviceId)) {
            return List.of();
        }
        return redisService.hmget(DECISION_HASH_PREFIX + deviceId).values().stream()
                .filter(RecentDecisionRecord.class::isInstance)
                .map(RecentDecisionRecord.class::cast)
                .sorted(Comparator.comparing(RecentDecisionRecord::getBusinessTimestamp,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(Math.max(limit, 0))
                .toList();
    }

    @Override
    public void saveCompareDataForTcp(Object messageData, Object bleRawLocationResult,
                                      Object mapPixelMatchResult, String finalPixelSource,
                                      Long businessTimestamp) {
        String deviceId = readStringProperty(messageData, "getDeviceId");
        if (StrUtil.isBlank(deviceId)) {
            return;
        }
        long now = System.currentTimeMillis();
        RecentDecisionRecord record = new RecentDecisionRecord();
        record.setDecisionSource(finalPixelSource);
        record.setTimestamp(now);
        record.setBusinessTimestamp(businessTimestamp == null ? now : businessTimestamp);
        redisService.hset(DECISION_HASH_PREFIX + deviceId, String.valueOf(record.getBusinessTimestamp()), record, 3600);
    }

    @Override
    public void saveCompareDataForTcp(Object messageData, Object helmetData,
                                      Object mapPixelMatchResult, boolean indoorCandidate,
                                      int beaconCount, String finalPixelSource,
                                      Double finalX, Double finalY, String reason) {
        saveCompareDataForTcp(messageData, helmetData, mapPixelMatchResult, finalPixelSource, null);
    }

    private String readStringProperty(Object target, String methodName) {
        if (target == null) {
            return null;
        }
        try {
            Method method = target.getClass().getMethod(methodName);
            Object value = method.invoke(target);
            return value == null ? null : String.valueOf(value);
        } catch (Exception ignored) {
            if (target instanceof Map<?, ?> map) {
                Object value = map.get("deviceId");
                return value == null ? null : String.valueOf(value);
            }
            return null;
        }
    }
}
