package cn.iocoder.yudao.module.swm.dal.dataobject;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * 报警灯 DO
 * 表: swm_alarm_light
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@TableName("swm_alarm_light")
public class SwmAlarmLightDO extends SwmBaseDO {
    private String lightName;
    private String snCode;
    /** 是否启用报警（枚举 SwmEnums.EnableAlarmEnum） */
    private String enableAlarm;
}
