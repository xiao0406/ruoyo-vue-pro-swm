package cn.iocoder.yudao.module.iot.mqtt.handler.weigou;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.iocoder.yudao.module.iot.cache.DeviceTenantMappingCache;
import cn.iocoder.yudao.module.iot.cache.service.RedisService;
import cn.iocoder.yudao.module.iot.enums.MqttSendPositionParamEnum;
import cn.iocoder.yudao.module.iot.mqtt.handler.MqttBusinessHandler;
import cn.iocoder.yudao.module.iot.service.HelmetSosTdEngineService;
import cn.iocoder.yudao.module.iot.service.SwmWarningManagementService;
import cn.iocoder.yudao.module.iot.service.TcpDeviceCommandLogService;
import cn.iocoder.yudao.module.iot.util.DictUtils;
import cn.iocoder.yudao.module.iot.util.R;
import cn.iocoder.yudao.module.swm.api.constant.SwmRedisKeyConstants;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@ConditionalOnProperty(prefix = "iot.mqtt", name = "enabled", havingValue = "true")
public class FenceAlarmMessageHandler implements MqttBusinessHandler {

    private static final String LOG_PREFIX = "MQTT electronic fence alarm";
    private static final String DICT_TYPE = "klt_xiafa_yy_cs";
    private static final String DEFAULT_VOICE_TEXT = "电子围栏告警";

    @Resource
    private SwmWarningManagementService swmWarningManagementService;
    @Resource
    private TcpDeviceCommandLogService tcpDeviceCommandLogService;
    @Resource
    private HelmetSosTdEngineService helmetSosTdEngineService;
    @Resource
    private DeviceTenantMappingCache deviceTenantMappingCache;
    @Resource
    private RedisService redisService;

    @Override
    public boolean canHandle(String topic, MqttMessage message) {
        return topic != null && topic.contains(MqttSendPositionParamEnum.PARAM_ENUM_ALARM.name());
    }

    @Override
    public void handle(String topic, MqttMessage message) throws Exception {
        long startTime = System.currentTimeMillis();
        String payload = new String(message.getPayload(), StandardCharsets.UTF_8);
        MqttAlarmDTO alarmDTO = null;
        try {
            log.info("{} received: topic={}, payload={}", LOG_PREFIX, topic, payload);
            alarmDTO = JSONUtil.toBean(payload, MqttAlarmDTO.class);
            if (alarmDTO == null || StrUtil.isBlank(alarmDTO.getDeviceId())) {
                log.warn("{} skipped because deviceId is blank: {}", LOG_PREFIX, payload);
                return;
            }

            String alarmKey = getAlarmKeyCode(alarmDTO.getAlarmType());
            if (StrUtil.isBlank(alarmKey)) {
                log.warn("{} skipped unsupported alarmType={}", LOG_PREFIX, alarmDTO.getAlarmType());
                return;
            }
            if (!isAlarm(alarmKey, alarmDTO.getDeviceId())) {
                log.info("{} disabled: deviceId={}, alarmKey={}", LOG_PREFIX, alarmDTO.getDeviceId(), alarmKey);
                return;
            }

            alarmDTO.setAlarmType(alarmKey);
            processAlarm(alarmDTO);
            log.info("{} handled: deviceId={}, alarmKey={}, cost={}ms",
                    LOG_PREFIX, alarmDTO.getDeviceId(), alarmKey, System.currentTimeMillis() - startTime);
        } catch (Exception e) {
            log.error("{} failed: topic={}, payload={}, error={}", LOG_PREFIX, topic, payload, e.getMessage(), e);
            if (alarmDTO != null) {
                saveErrorLog(alarmDTO, e.getMessage());
            }
            throw e;
        }
    }

    private void processAlarm(MqttAlarmDTO alarmDTO) {
        String deviceId = alarmDTO.getDeviceId();
        String alarmType = alarmDTO.getAlarmType();
        String location = buildLocation(alarmDTO);
        String areaName = getAreaNameByFloorId(alarmDTO.getFloorId());

        saveElectronicFenceDataToTDengine(deviceId, alarmType);
        saveWarningToSwmWarningManagement(alarmDTO, location, areaName);
        sendVoiceCommandForFence(deviceId, alarmType);
    }

    private void saveElectronicFenceDataToTDengine(String deviceId, String alarmType) {
        Map<String, Object> electronicFenceData = new HashMap<>();
        electronicFenceData.put("device_id", deviceId);
        electronicFenceData.put("type", alarmType);
        electronicFenceData.put("act", "ca_sos");
        electronicFenceData.put("sos_time", System.currentTimeMillis());
        electronicFenceData.put("bt_signal_info", "");

        R<JSONObject> result = helmetSosTdEngineService.saveHelmetSosData(deviceId, electronicFenceData);
        if (result.getCode() != R.SUCCESS) {
            log.error("{} TDengine save failed: deviceId={}, error={}", LOG_PREFIX, deviceId, result.getMsg());
        }
    }

    private String buildLocation(MqttAlarmDTO alarmDTO) {
        if (alarmDTO.getLng() == null || alarmDTO.getLat() == null) {
            return "未知位置";
        }
        return String.format("lng=%s, lat=%s", alarmDTO.getLng(), alarmDTO.getLat());
    }

    private String getAreaNameByFloorId(String floorId) {
        return StrUtil.isBlank(floorId) ? "" : "未知区域";
    }

    private void saveWarningToSwmWarningManagement(MqttAlarmDTO alarmDTO, String location, String areaName) {
        try {
            String warningContent = getWarningContentFromDict(alarmDTO.getAlarmType());
            String triggerReason = buildTriggerReason(alarmDTO, warningContent);
            String alarmRecord = alarmDTO.getAlarmType() + "-" + getAlarmTypeDesc(alarmDTO.getAlarmType()) + " alarm";
            String warningType = "1";

            R<JSONObject> result = swmWarningManagementService.saveCustomWarningToSwmWarningManagement(
                    alarmDTO.getDeviceId(),
                    alarmDTO.getIdentityCard(),
                    warningContent,
                    triggerReason,
                    alarmRecord,
                    warningType,
                    alarmDTO.getAlarmType(),
                    false,
                    null,
                    "",
                    location,
                    areaName
            );
            if (result.getCode() != R.SUCCESS) {
                log.error("{} warning save failed: deviceId={}, error={}",
                        LOG_PREFIX, alarmDTO.getDeviceId(), result.getMsg());
            }
        } catch (Exception e) {
            log.error("{} warning save exception: deviceId={}, error={}",
                    LOG_PREFIX, alarmDTO.getDeviceId(), e.getMessage(), e);
        }
    }

    private void sendVoiceCommandForFence(String deviceId, String alarmType) {
        String voiceText = DictUtils.getDictLabel(DICT_TYPE, alarmType, DEFAULT_VOICE_TEXT);
        String voiceCommand = buildVoiceCommand(voiceText);
        try {
            cn.iocoder.yudao.module.iot.tcp.session.SessionManager sessionManager =
                    cn.iocoder.yudao.module.iot.tcp.server.NettyTcpServer.getInstance().getSessionManager();
            boolean success = sessionManager.sendMessageToDevice(deviceId, voiceCommand);
            if (!success) {
                saveCommandLog(deviceId, voiceCommand, "device offline");
            }
        } catch (Exception e) {
            saveCommandLog(deviceId, voiceCommand, e.getMessage());
            throw e;
        }
    }

    private String buildVoiceCommand(String text) {
        String dataSection = "PS,0,TTSD," + text + ",#";
        int byteLength = dataSection.getBytes(StandardCharsets.UTF_8).length;
        return "$" + Integer.toHexString(byteLength).toUpperCase() + "," + dataSection + "\n";
    }

    private void saveCommandLog(String deviceId, String sendMessage, String errorMessage) {
        try {
            tcpDeviceCommandLogService.saveTcpCommandLog(deviceId, sendMessage, errorMessage);
        } catch (Exception e) {
            log.error("{} command log save failed: deviceId={}, error={}", LOG_PREFIX, deviceId, e.getMessage());
        }
    }

    private String buildTriggerReason(MqttAlarmDTO alarmDTO, String warningContent) {
        String formattedTime = DateUtil.format(DateUtil.date(), "yyyy-MM-dd HH:mm:ss");
        return String.format("%s at %s %s triggered %s",
                StrUtil.isNotBlank(alarmDTO.getName()) ? alarmDTO.getName() : "unknown person",
                formattedTime,
                buildLocation(alarmDTO),
                warningContent);
    }

    private String getWarningContentFromDict(String alarmType) {
        return DictUtils.getDictLabel("warning_content_enum", alarmType, getAlarmTypeDesc(alarmType));
    }

    private String getAlarmTypeDesc(String alarmType) {
        if (alarmType == null) {
            return "unknown alarm";
        }
        return switch (alarmType) {
            case "0" -> "enter fence";
            case "1" -> "leave fence";
            case "2" -> "linger";
            case "3" -> "approach";
            case "4" -> "cross border";
            case "5" -> "stay";
            case "6" -> "timeout";
            case "7" -> "low battery";
            case "8" -> "hazard";
            case "12" -> "SOS";
            case "13" -> "fall";
            default -> "unknown alarm";
        };
    }

    private boolean isAlarm(String alarmKey, String deviceId) {
        String tenantKey = deviceTenantMappingCache.getTenantKey(deviceId);
        if (StrUtil.isBlank(tenantKey)) {
            return true;
        }
        Object enabled = redisService.hget(tenantKey + SwmRedisKeyConstants.SwmKey.ALARM_CONFIG, alarmKey);
        if (enabled == null) {
            return true;
        }
        return !"0".equals(String.valueOf(enabled));
    }

    private String getAlarmKeyCode(String alarmType) {
        return StrUtil.blankToDefault(alarmType, null);
    }

    private void saveErrorLog(MqttAlarmDTO alarmDTO, String errorMsg) {
        log.error("{} error: deviceId={}, alarmType={}, error={}",
                LOG_PREFIX, alarmDTO.getDeviceId(), alarmDTO.getAlarmType(), errorMsg);
    }

    @Data
    public static class MqttAlarmDTO {
        private String deviceId;
        private String alarmType;
        private String identityCard;
        private String name;
        private String floorId;
        private Double lng;
        private Double lat;
    }
}
