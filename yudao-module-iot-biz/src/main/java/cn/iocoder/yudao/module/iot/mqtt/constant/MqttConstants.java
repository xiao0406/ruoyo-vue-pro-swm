package cn.iocoder.yudao.module.iot.mqtt.constant;

import cn.iocoder.yudao.module.iot.cache.DeviceTenantMappingCache;
import cn.iocoder.yudao.module.iot.cache.service.RedisService;
import cn.iocoder.yudao.module.iot.tcp.model.TcpMessageData;

/**
 * MQTT 事件类型和子类型常量
 *
 * 迁移自 JeeSite: com.jeesite.modules.mqtt.constant.MqttConstant
 */
public interface MqttConstants {

    // ========== MQTT Topic 根路径 ==========
    String MQTT_TOPIC_ROOT = "/cmt/IoT/pub/";

    // ========== 事件类型 ==========
    String STATUS = "status";
    String SENSOR = "sensor";
    String LOCATION = "location";
    String ALARM = "alarm";
    String FENCE_ALARM = "fenceAlarm";

    // ========== 状态子类型 ==========
    String STATUS_SILENT = "silent";
    String STATUS_ONLINE = "online";
    String STATUS_RUN_STATUS = "runStatus";
    String RUN_STATUS = STATUS_RUN_STATUS;
    String STATUS_CLIMB = "climb";
    String STATUS_CLIMB_HIG = "climbHig";
    String STATUS_BIND = "bind";

    // ========== 传感器子类型 ==========
    String SENSOR_BATTERY = "battery";
    String SENSOR_IBEACON = "ibeacon";
    String BATTERY = SENSOR_BATTERY;
    String IBEACON = SENSOR_IBEACON;

    // ========== 定位子类型 ==========
    String LOCATION_UWB = "uwb";
    String LOCATION_GPS = "gps";
    String GPS = LOCATION_GPS;

    // ========== 告警子类型 ==========
    String ALARM_SOS = "SOS";
    String SOS = ALARM_SOS;
    String ALARM_UNBONNET = "Unbonnet";
    String ALARM_FALL = "Fall";
    String UNBONNET = ALARM_UNBONNET;
    String FALL = ALARM_FALL;
    String ALARM_JINDIAN = "JinDian";
    String ALARM_INOUT = "inout";
    String ALARM_BRAIN = "brain";
    String ALARM_BATTERY = "Battery";
    String ALARM_AI = "AiAlarm";
    String ALARM_RU_QIN = "ru_qin";

    static boolean verifyDeviceExists(String deviceId, TcpMessageData messageData,
                                      RedisService redisService, DeviceTenantMappingCache deviceTenantMappingCache) {
        return org.apache.commons.lang3.StringUtils.isNotBlank(deviceId);
    }

    static boolean isAlarm(String key, String deviceId,
                           RedisService redisService, DeviceTenantMappingCache deviceTenantMappingCache) {
        return true;
    }

    static Integer getBatteryLevel(String deviceId, RedisService redisService, DeviceTenantMappingCache deviceTenantMappingCache) {
        return null;
    }

    static String getOnlineStatusWildcardTopic() {
        return MQTT_TOPIC_ROOT + "+/" + STATUS + "/" + STATUS_ONLINE;
    }

    static String getOnlineStatusTopic(String deviceId) {
        return MQTT_TOPIC_ROOT + deviceId + "/" + STATUS + "/" + STATUS_ONLINE;
    }

    static String getRunStatusWildcardTopic() {
        return MQTT_TOPIC_ROOT + "+/" + STATUS + "/" + STATUS_RUN_STATUS;
    }

    static String getAlarmWildcardTopic(String alarmType) {
        return MQTT_TOPIC_ROOT + "+/" + ALARM + "/" + alarmType;
    }

    static String getSensorWildcardTopic(String sensorType) {
        return MQTT_TOPIC_ROOT + "+/" + SENSOR + "/" + sensorType;
    }

    static String getLocationWildcardTopic(String locationType) {
        return MQTT_TOPIC_ROOT + "+/" + LOCATION + "/" + locationType;
    }

}
