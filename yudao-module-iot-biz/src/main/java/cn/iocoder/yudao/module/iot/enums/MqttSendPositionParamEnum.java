package cn.iocoder.yudao.module.iot.enums;

/**
 * MQTT 发送位置参数枚举
 *
 * 迁移自 JeeSite: com.jeesite.modules.enums.MqttSendPositionParamEnum
 */
public enum MqttSendPositionParamEnum {

    POSITION_PARAM_ENUM("/device/report/message", 2, false),
    POSITION_PARAM_ENUM_POSITION("/device/position/message", 2, false),
    PARAM_ENUM_ALARM("/electronic/fence/message", 2, false);

    private final String topic;
    private final int qos;
    private final boolean retained;

    MqttSendPositionParamEnum(String topic, int qos, boolean retained) {
        this.topic = topic;
        this.qos = qos;
        this.retained = retained;
    }

    public String getTopic() { return topic; }
    public int getQos() { return qos; }
    public boolean isRetained() { return retained; }

}
