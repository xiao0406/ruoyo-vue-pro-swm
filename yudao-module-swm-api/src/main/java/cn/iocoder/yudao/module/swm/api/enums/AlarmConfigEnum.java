package cn.iocoder.yudao.module.swm.api.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 告警配置枚举
 */
@Getter
@AllArgsConstructor
public enum AlarmConfigEnum {

    YJ("YJ", "预警"),
    DL("DL", "跌落"),
    TM("TM", "脱帽"),
    SOS("SOS", "求救"),
    UNBONNET("UNBONNET", "未戴帽");

    private final String code;
    private final String name;

}
