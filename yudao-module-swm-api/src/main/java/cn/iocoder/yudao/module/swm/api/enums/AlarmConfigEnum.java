package cn.iocoder.yudao.module.swm.api.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Alarm configuration types shared by SWM and IOT.
 */
@Getter
@AllArgsConstructor
public enum AlarmConfigEnum {

    YJ("YJ", "warning"),
    DL("DL", "fall"),
    TM("TM", "helmet_off"),
    SOS("SOS", "sos"),
    UNBONNET("UNBONNET", "unbonnet"),
    CSJ("CSJ", "long_static"),
    WX("WX", "hazard_source");

    private final String code;
    private final String name;

}
