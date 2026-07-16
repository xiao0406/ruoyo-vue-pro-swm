package cn.iocoder.yudao.module.iot.tcp.processor;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import cn.iocoder.yudao.module.iot.cache.DeviceTenantMappingCache;
import cn.iocoder.yudao.module.iot.cache.service.RedisService;
import cn.iocoder.yudao.module.iot.mqtt.handler.zhongtai.DeviceSoSAlarmHandler;
import cn.iocoder.yudao.module.iot.service.HelmetSosTdEngineService;
import cn.iocoder.yudao.module.iot.service.SwmWarningManagementService;
import cn.iocoder.yudao.module.iot.service.TDengineService;
import cn.iocoder.yudao.module.iot.service.VoiceAlarmService;
import cn.iocoder.yudao.module.iot.tcp.model.TcpMessageData;
import cn.iocoder.yudao.module.iot.tcp.service.TcpBeaconLocationService;
import cn.iocoder.yudao.module.iot.util.R;
import cn.iocoder.yudao.module.swm.api.constant.SwmRedisKeyConstants;
import io.netty.channel.ChannelHandlerContext;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * TCP handler for helmet-off alarms.
 *
 * <p>This keeps the JeeSite protocol behavior but replaces corp switching with RuoYi tenantId
 * resolution and IOT-local service boundaries.
 */
@Slf4j
@Component
public class HelmetOffTcpProcessor {

    private static final int HELMET_OFF_ALARM_BIT_MASK = 128;
    private static final String WARNING_TYPE = "1";
    private static final String WARNING_CONTENT = "CA alarm";
    private static final String VOICE_CONTENT = "Please wear your safety helmet";

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
    private TDengineService tdengineService;
    @Resource
    private VoiceAlarmService voiceAlarmService;
    @Resource
    private DeviceSoSAlarmHandler deviceSoSAlarmHandler;

    public boolean canProcess(TcpMessageData messageData) {
        return messageData != null
                && messageData.getAlarmValue() != null
                && (messageData.getAlarmValue() & HELMET_OFF_ALARM_BIT_MASK) != 0;
    }

    public void process(TcpMessageData messageData, ChannelHandlerContext ctx) {
        if (messageData == null || StrUtil.isBlank(messageData.getDeviceId())) {
            return;
        }
        String deviceId = messageData.getDeviceId();
        String idCard = StrUtil.toStringOrNull(redisService.hget(SwmRedisKeyConstants.GlobalKey.DEVICE_PERSON_MAP, deviceId));
        if (StrUtil.isBlank(idCard)) {
            log.warn("Skip helmet-off warning because device is not bound to a person, deviceId={}", deviceId);
            return;
        }
        if (hasRecentWarning(deviceId, idCard, WARNING_TYPE)) {
            return;
        }

        Map<String, Object> area = tcpBeaconLocationService.getAreaNameFromBeaconsFilter(messageData);
        if (isAreaBlocked(deviceId, area)) {
            return;
        }

        Map<String, Object> tdData = new LinkedHashMap<>();
        tdData.put("device_id", deviceId);
        tdData.put("type", Integer.parseInt(WARNING_TYPE));
        tdData.put("act", "ca_sos");
        tdData.put("sos_time", System.currentTimeMillis());
        tdData.put("bt_signal_info", JSONUtil.toJsonStr(messageData.getBluetoothBeacons()));
        R<?> tdResult = helmetSosTdEngineService.saveHelmetSosData(deviceId, tdData);
        if (tdResult.getCode() == R.FAIL) {
            log.warn("Helmet-off TDengine save failed, deviceId={}, msg={}", deviceId, tdResult.getMsg());
        }

        String areaId = area == null ? "" : StrUtil.toString(area.get("id"));
        String areaName = area == null ? "" : StrUtil.toString(area.get("areaName"));
        swmWarningManagementService.saveCustomWarningToSwmWarningManagement(deviceId, idCard,
                WARNING_CONTENT, WARNING_TYPE, "1", "iot", DateUtil.now(), true,
                areaId, areaName, String.valueOf(messageData.getLng()), String.valueOf(messageData.getLat()));
        sendVoice(messageData, VOICE_CONTENT);
    }

    public boolean hasHelmetOffAlarmFromRawMessage(String rawMessage) {
        Integer alarmValue = parseAlarmValue(rawMessage);
        return alarmValue != null && (alarmValue & HELMET_OFF_ALARM_BIT_MASK) != 0;
    }

    private boolean hasRecentWarning(String deviceId, String idCard, String warningType) {
        String dbName = deviceTenantMappingCache.getDbName(deviceId);
        if (StrUtil.isBlank(dbName)) {
            return false;
        }
        long twoMinutesAgo = System.currentTimeMillis() - 2 * 60 * 1000L;
        String sql = String.format(
                "select count(*) count from %s.swm_warning_management where device_id='%s' and id_card='%s' and warning_type='%s' and warning_time >= %d",
                dbName, escapeSql(deviceId), escapeSql(idCard), escapeSql(warningType), twoMinutesAgo);
        return tdengineService.selectCount(sql) > 0;
    }

    private boolean isAreaBlocked(String deviceId, Map<String, Object> area) {
        String tenantKey = deviceTenantMappingCache.getTenantKey(deviceId);
        String areaCacheKey = tenantKey + SwmRedisKeyConstants.SwmKey.AREA_CACHE;
        return redisService.hasKey(areaCacheKey) && (area == null || StrUtil.isBlank(StrUtil.toString(area.get("id"))));
    }

    private void sendVoice(TcpMessageData messageData, String content) {
        if (messageData.isZTDevice()) {
            deviceSoSAlarmHandler.sendVoiceCommand(messageData.getDeviceId(), content);
            return;
        }
        voiceAlarmService.sendVoiceAlarm(messageData.getDeviceId(), content);
    }

    private Integer parseAlarmValue(String rawMessage) {
        if (rawMessage == null || !rawMessage.startsWith("$") || !rawMessage.endsWith("#")) {
            return null;
        }

        String[] parts = rawMessage.substring(1, rawMessage.length() - 1).split(",");
        if (parts.length < 14 || !"S".equals(parts[1])) {
            return null;
        }

        String alarmField = parts[13].trim();
        try {
            if (alarmField.matches("[0-9A-Fa-f]+") && alarmField.length() <= 4) {
                return Integer.parseInt(alarmField, 16);
            }
            return Integer.parseInt(alarmField);
        } catch (NumberFormatException ex) {
            log.warn("Cannot parse helmet-off alarm value from raw tcp message: {}", rawMessage, ex);
            return null;
        }
    }

    private String escapeSql(String value) {
        return StrUtil.blankToDefault(value, "").replace("'", "''");
    }
}
