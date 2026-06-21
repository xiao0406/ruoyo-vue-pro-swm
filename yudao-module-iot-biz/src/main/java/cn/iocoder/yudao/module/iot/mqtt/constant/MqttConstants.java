package cn.iocoder.yudao.module.iot.mqtt.constant;

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
    String STATUS_CLIMB = "climb";
    String STATUS_CLIMB_HIG = "climbHig";
    String STATUS_BIND = "bind";

    // ========== 传感器子类型 ==========
    String SENSOR_BATTERY = "battery";
    String SENSOR_IBEACON = "ibeacon";

    // ========== 定位子类型 ==========
    String LOCATION_UWB = "uwb";
    String LOCATION_GPS = "gps";

    // ========== 告警子类型 ==========
    String ALARM_SOS = "SOS";
    String ALARM_UNBONNET = "Unbonnet";
    String ALARM_FALL = "Fall";
    String ALARM_JINDIAN = "JinDian";
    String ALARM_INOUT = "inout";
    String ALARM_BRAIN = "brain";
    String ALARM_BATTERY = "Battery";
    String ALARM_AI = "AiAlarm";
    String ALARM_RU_QIN = "ru_qin";

}
