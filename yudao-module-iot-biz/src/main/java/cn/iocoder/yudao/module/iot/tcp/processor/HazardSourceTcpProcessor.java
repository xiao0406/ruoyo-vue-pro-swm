package cn.iocoder.yudao.module.iot.tcp.processor;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import cn.iocoder.yudao.module.iot.cache.DeviceTenantMappingCache;
import cn.iocoder.yudao.module.iot.cache.service.RedisService;
import cn.iocoder.yudao.module.iot.mqtt.handler.zhongtai.DeviceSoSAlarmHandler;
import cn.iocoder.yudao.module.iot.service.HelmetSosTdEngineService;
import cn.iocoder.yudao.module.iot.service.SwmWarningManagementService;
import cn.iocoder.yudao.module.iot.service.VoiceAlarmService;
import cn.iocoder.yudao.module.iot.tcp.model.TcpMessageData;
import cn.iocoder.yudao.module.iot.tcp.service.TcpBeaconLocationService;
import cn.iocoder.yudao.module.iot.util.R;
import cn.iocoder.yudao.module.swm.api.constant.SwmRedisKeyConstants;
import io.netty.channel.ChannelHandlerContext;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * TCP handler for hazard-source alarms.
 *
 * <p>The JeeSite version mixed Redis matching, TDengine writes, SWM warning writes, JDBC template
 * lookups and voice dispatch. This migrated version keeps the observable alarm flow while replacing
 * corp-prefixed caches with tenant-prefixed caches and IOT-local service boundaries.
 */
@Slf4j
@Component
public class HazardSourceTcpProcessor {

    private static final int HAZARD_SOURCE_ALARM_BIT_MASK = 32;
    private static final String WARNING_TYPE = "8";

    @Resource
    private RedisService redisService;
    @Resource
    private DeviceTenantMappingCache deviceTenantMappingCache;
    @Resource
    private TcpBeaconLocationService tcpBeaconLocationService;
    @Resource
    private HelmetSosTdEngineService helmetSosTdEngineService;
    @Resource
    private SwmWarningManagementService swmWarningManagementService;
    @Resource
    private VoiceAlarmService voiceAlarmService;
    @Resource
    private DeviceSoSAlarmHandler deviceSoSAlarmHandler;

    public boolean canProcess(TcpMessageData messageData) {
        if (messageData == null || messageData.getAlarmValue() == null) {
            return false;
        }
        Integer alarmValue = messageData.getAlarmValue();
        return alarmValue != 0 || (alarmValue & HAZARD_SOURCE_ALARM_BIT_MASK) != 0;
    }

    public void process(TcpMessageData messageData, ChannelHandlerContext ctx) {
        if (messageData == null || StrUtil.isBlank(messageData.getDeviceId())) {
            return;
        }

        String deviceId = messageData.getDeviceId();
        List<Map<String, Object>> matchedHazards = getMatchedHazardSources(messageData);
        if (matchedHazards.isEmpty()) {
            log.debug("No hazard source matched for deviceId={}", deviceId);
            return;
        }
        if (hasDisabledHazardAlarmDevice(deviceId, matchedHazards)) {
            return;
        }

        String btSignalInfo = buildBtSignalInfo(messageData, matchedHazards);
        String location = StrUtil.blankToDefault(tcpBeaconLocationService.getLocationFromBeacons(messageData), "");
        String areaName = StrUtil.blankToDefault(tcpBeaconLocationService.getAreaNameFromBeacons(messageData), "");
        List<String> hazardNames = matchedHazards.stream()
                .map(hazard -> StrUtil.toString(hazard.get("hazard_name")))
                .filter(StrUtil::isNotBlank)
                .collect(Collectors.toList());

        Map<String, Object> tdData = new LinkedHashMap<>();
        tdData.put("device_id", deviceId);
        tdData.put("type", Integer.parseInt(WARNING_TYPE));
        tdData.put("act", "ca_sos");
        tdData.put("sos_time", messageData.getScanTimestamp() == null ? System.currentTimeMillis() : messageData.getScanTimestamp());
        tdData.put("bt_signal_info", btSignalInfo);
        R<?> tdResult = helmetSosTdEngineService.saveHelmetSosData(deviceId, tdData);
        if (tdResult.getCode() == R.FAIL) {
            log.warn("Hazard-source TDengine save failed, deviceId={}, msg={}", deviceId, tdResult.getMsg());
        }

        String idCard = StrUtil.toStringOrNull(redisService.hget(SwmRedisKeyConstants.GlobalKey.DEVICE_PERSON_MAP, deviceId));
        String warningContent = hazardNames.isEmpty()
                ? "Hazard source alarm"
                : "Detected worker near hazard source: " + String.join(", ", hazardNames);
        swmWarningManagementService.saveCustomWarningToSwmWarningManagement(deviceId, idCard,
                warningContent, WARNING_TYPE, "1", "iot", DateUtil.now(), true,
                "", areaName, String.valueOf(messageData.getLng()), String.valueOf(messageData.getLat()));
        sendVoiceAlarms(deviceId, matchedHazards, messageData);

        log.info("Processed hazard-source alarm, deviceId={}, hazards={}, location={}, areaName={}",
                deviceId, hazardNames, location, areaName);
    }

    public boolean hasHazardSourceAlarmFromRawMessage(String rawMessage) {
        Integer alarmValue = parseAlarmValue(rawMessage);
        return alarmValue != null && (alarmValue != 0 || (alarmValue & HAZARD_SOURCE_ALARM_BIT_MASK) != 0);
    }

    private List<Map<String, Object>> getMatchedHazardSources(TcpMessageData messageData) {
        List<Map<String, Object>> matchedHazards = new ArrayList<>();
        Set<String> processedHazardIds = new HashSet<>();
        List<Map<String, Object>> beacons = messageData.getBluetoothBeacons();
        if (beacons == null || beacons.isEmpty()) {
            return matchedHazards;
        }

        String tenantKey = deviceTenantMappingCache.getTenantKey(messageData.getDeviceId());
        String redisKey = tenantKey + SwmRedisKeyConstants.SwmKey.MAC_TO_HAZARD_INFO;
        for (Map<String, Object> beacon : beacons) {
            String mac = StrUtil.toStringOrNull(beacon.get("MAC"));
            if (StrUtil.isBlank(mac)) {
                continue;
            }

            Object cacheValue = redisService.hget(redisKey, mac.toUpperCase());
            Map<String, Object> hazard = toHazardMap(cacheValue);
            String hazardId = hazard == null ? null : StrUtil.toStringOrNull(hazard.get("id"));
            if (StrUtil.isBlank(hazardId) || processedHazardIds.contains(hazardId)) {
                continue;
            }
            processedHazardIds.add(hazardId);
            matchedHazards.add(hazard);
        }
        return matchedHazards;
    }

    private boolean hasDisabledHazardAlarmDevice(String deviceId, List<Map<String, Object>> matchedHazards) {
        String tenantKey = deviceTenantMappingCache.getTenantKey(deviceId);
        String redisKey = tenantKey + SwmRedisKeyConstants.SwmKey.HAZARD_IS_ALARM_BEACON;
        for (Map<String, Object> hazard : matchedHazards) {
            String hazardId = StrUtil.toStringOrNull(hazard.get("id"));
            Object disabledDevices = redisService.hget(redisKey, hazardId);
            if (disabledDevices instanceof Set<?> set && set.contains(deviceId)) {
                log.info("Device is disabled for hazard-source alarm, deviceId={}, hazardId={}", deviceId, hazardId);
                return true;
            }
        }
        return false;
    }

    private Map<String, Object> toHazardMap(Object cacheValue) {
        if (cacheValue instanceof SwmHazardSource hazardSource) {
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("id", hazardSource.getId());
            result.put("hazard_name", hazardSource.getHazardName());
            result.put("hazard_category", hazardSource.getHazardCategory());
            result.put("location", hazardSource.getLocation());
            result.put("voice_template_id", hazardSource.getVoiceTemplateId());
            return result;
        }
        if (cacheValue instanceof Map<?, ?> map) {
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("id", firstPresent(map, "id", "hazardId"));
            result.put("hazard_name", firstPresent(map, "hazard_name", "hazardName"));
            result.put("hazard_category", firstPresent(map, "hazard_category", "hazardCategory"));
            result.put("location", map.get("location"));
            result.put("voice_template_id", firstPresent(map, "voice_template_id", "voiceTemplateId"));
            return result;
        }
        return null;
    }

    private Object firstPresent(Map<?, ?> map, String first, String second) {
        Object value = map.get(first);
        return value == null ? map.get(second) : value;
    }

    private String buildBtSignalInfo(TcpMessageData messageData, List<Map<String, Object>> matchedHazards) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("rawMessage", messageData.getRawMessage());
        payload.put("alarmValue", messageData.getAlarmValue());
        payload.put("beacons", messageData.getBluetoothBeacons());
        payload.put("hazards", matchedHazards);
        return JSONUtil.toJsonStr(payload);
    }

    private void sendVoiceAlarms(String deviceId, List<Map<String, Object>> matchedHazards, TcpMessageData messageData) {
        Set<String> sentTemplates = new HashSet<>();
        for (Map<String, Object> hazard : matchedHazards) {
            String templateId = StrUtil.toStringOrNull(hazard.get("voice_template_id"));
            if (StrUtil.isNotBlank(templateId) && !sentTemplates.add(templateId)) {
                continue;
            }
            String hazardName = StrUtil.blankToDefault(StrUtil.toStringOrNull(hazard.get("hazard_name")), "hazard source");
            String voiceText = "Please stay away from " + hazardName;
            if (messageData.isZTDevice()) {
                deviceSoSAlarmHandler.sendVoiceCommand(deviceId, voiceText);
            } else {
                voiceAlarmService.sendVoiceAlarm(deviceId, voiceText);
            }
        }
    }

    private Integer parseAlarmValue(String rawMessage) {
        if (rawMessage == null || !rawMessage.startsWith("$") || !rawMessage.endsWith("#")) {
            return null;
        }
        String[] parts = rawMessage.substring(1, rawMessage.length() - 1).split(",");
        if (parts.length < 14 || !"S".equals(parts[1])) {
            return null;
        }
        String alarm = parts[13].trim();
        try {
            if (alarm.matches("[0-9A-Fa-f]+") && alarm.length() <= 4) {
                return Integer.parseInt(alarm, 16);
            }
            return Integer.parseInt(alarm);
        } catch (NumberFormatException ex) {
            log.warn("Cannot parse hazard-source alarm value from raw tcp message: {}", rawMessage, ex);
            return null;
        }
    }
}
