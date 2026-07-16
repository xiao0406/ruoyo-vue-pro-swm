package cn.iocoder.yudao.module.iot.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.iocoder.yudao.module.iot.cache.DeviceTenantMappingCache;
import cn.iocoder.yudao.module.iot.cache.service.RedisService;
import cn.iocoder.yudao.module.iot.service.SwmWarningManagementService;
import cn.iocoder.yudao.module.iot.util.R;
import cn.iocoder.yudao.module.swm.api.warning.SwmWarningApi;
import cn.iocoder.yudao.module.swm.api.warning.dto.SwmWarningCreateReqDTO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * RuoYi-side warning bridge.
 *
 * <p>The original JeeSite service wrote directly into SWM tables and triggered several side effects.
 * This migrated boundary calls the public SWM API and keeps Redis events as a replayable fallback.
 */
@Slf4j
@Service
@Validated
public class SwmWarningManagementServiceImpl implements SwmWarningManagementService {

    private static final String WARNING_EVENT_HASH_PREFIX = "iot:swm_warning:event:";
    private static final String ATTENDANCE_EVENT_HASH_PREFIX = "iot:swm_attendance:event:";

    @Resource
    private RedisService redisService;
    @Resource
    private DeviceTenantMappingCache deviceTenantMappingCache;
    @Autowired(required = false)
    private SwmWarningApi swmWarningApi;

    @Override
    public R<JSONObject> saveCustomWarningToSwmWarningManagement(String deviceId, String idCard,
                                                                 String warningContent, String warningType,
                                                                 String warningLevel, String warningSource,
                                                                 String warningTime, boolean autoHandle,
                                                                 String areaId, String areaName,
                                                                 String lng, String lat) {
        if (StrUtil.isBlank(deviceId)) {
            return R.fail("deviceId cannot be blank");
        }

        JSONObject event = new JSONObject();
        event.set("tenantId", deviceTenantMappingCache.getTenantId(deviceId));
        event.set("deviceId", deviceId);
        event.set("idCard", StrUtil.blankToDefault(idCard, ""));
        event.set("warningContent", warningContent);
        event.set("warningType", warningType);
        event.set("warningLevel", warningLevel);
        event.set("warningSource", warningSource);
        event.set("warningTime", warningTime);
        event.set("autoHandle", autoHandle);
        event.set("areaId", StrUtil.blankToDefault(areaId, ""));
        event.set("areaName", StrUtil.blankToDefault(areaName, ""));
        event.set("lng", StrUtil.blankToDefault(lng, ""));
        event.set("lat", StrUtil.blankToDefault(lat, ""));
        event.set("createdAt", System.currentTimeMillis());

        if (swmWarningApi != null) {
            try {
                SwmWarningCreateReqDTO reqDTO = buildCreateReqDTO(deviceId, idCard, warningContent, warningType,
                        warningLevel, warningSource, warningTime, autoHandle, areaName, lng, lat);
                String warningId = swmWarningApi.createWarning(reqDTO);
                event.set("swmWarningId", warningId);
                event.set("persisted", true);
                redisService.hset(WARNING_EVENT_HASH_PREFIX + deviceId, event.getStr("createdAt"), event);
                log.info("Created SWM warning through public API, id={}, deviceId={}, type={}",
                        warningId, deviceId, warningType);
                return R.ok(event);
            } catch (Exception ex) {
                event.set("persisted", false);
                event.set("error", ex.getMessage());
                log.warn("Create SWM warning through public API failed, fallback to Redis event, deviceId={}", deviceId, ex);
            }
        } else {
            event.set("persisted", false);
            event.set("error", "SwmWarningApi bean is not available");
        }

        // Store one hash per device so SWM can replay failed events without IOT importing SWM internals.
        redisService.hset(WARNING_EVENT_HASH_PREFIX + deviceId, event.getStr("createdAt"), event);
        log.info("Saved fallback SWM warning event, deviceId={}, type={}, content={}",
                deviceId, warningType, warningContent);
        return R.ok(event);
    }

    @Override
    public R<JSONObject> saveCustomWarningToSwmWarningManagement(String deviceId, String idCard,
                                                                 String warningContent, String warningType,
                                                                 String warningLevel, String warningSource,
                                                                 String warningTime) {
        return saveCustomWarningToSwmWarningManagement(deviceId, idCard, warningContent, warningType,
                warningLevel, warningSource, warningTime, true, "", "", "", "");
    }

    @Override
    public R<JSONObject> updateAttendanceRecord(String deviceId, String idCard) {
        JSONObject event = new JSONObject();
        event.set("tenantId", deviceTenantMappingCache.getTenantId(deviceId));
        event.set("deviceId", deviceId);
        event.set("idCard", StrUtil.blankToDefault(idCard, ""));
        event.set("createdAt", System.currentTimeMillis());
        redisService.hset(ATTENDANCE_EVENT_HASH_PREFIX + deviceId, event.getStr("createdAt"), event);
        return R.ok(event);
    }

    private SwmWarningCreateReqDTO buildCreateReqDTO(String deviceId, String idCard,
                                                     String warningContent, String warningType,
                                                     String warningLevel, String warningSource,
                                                     String warningTime, boolean autoHandle,
                                                     String areaName, String lng, String lat) {
        SwmWarningCreateReqDTO reqDTO = new SwmWarningCreateReqDTO();
        reqDTO.setDeviceId(deviceId);
        reqDTO.setIdCard(StrUtil.blankToDefault(idCard, ""));
        reqDTO.setWarningType(StrUtil.blankToDefault(warningLevel, "1"));
        reqDTO.setWarningContent(StrUtil.blankToDefault(warningContent, "IOT warning"));
        reqDTO.setWarningLevel(warningLevel);
        reqDTO.setWarningSource(warningSource);
        reqDTO.setWarningTime(parseWarningTime(warningTime));
        reqDTO.setAlarmTime(reqDTO.getWarningTime());
        reqDTO.setAlarmRecord(warningSource);
        reqDTO.setTriggerReason(warningContent);
        reqDTO.setHandleStatus(autoHandle ? "0" : "1");
        reqDTO.setType(warningType);
        reqDTO.setX(lng);
        reqDTO.setY(lat);
        reqDTO.setLocation(areaName);
        reqDTO.setArea(areaName);
        reqDTO.setAutoHandle(autoHandle);
        return reqDTO;
    }

    private LocalDateTime parseWarningTime(String warningTime) {
        if (StrUtil.isBlank(warningTime)) {
            return LocalDateTime.now();
        }
        try {
            return LocalDateTime.parse(warningTime, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        } catch (DateTimeParseException ignored) {
            try {
                return LocalDateTime.parse(warningTime);
            } catch (DateTimeParseException ex) {
                return LocalDateTime.now();
            }
        }
    }
}
