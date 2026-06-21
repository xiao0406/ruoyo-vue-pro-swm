package cn.iocoder.yudao.module.swm.api.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * MQTT 发送位置参数枚举
 */
@Getter
@AllArgsConstructor
public enum MqttSendPositionParamEnum {

    POSITION_PARAM_ENUM("device/position", 1, false);

    private final String topic;
    private final int qos;
    private final boolean retained;

}
