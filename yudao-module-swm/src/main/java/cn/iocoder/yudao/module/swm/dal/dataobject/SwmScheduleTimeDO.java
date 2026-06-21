package cn.iocoder.yudao.module.swm.dal.dataobject;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * 排班时间段 DO
 * 表: swm_schedule_time
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@TableName("swm_schedule_time")
public class SwmScheduleTimeDO extends SwmBaseDO {
    /** 班次类型（枚举 SwmEnums.ShiftTypeEnum） */
    private String shiftType;
    private String startTime;
    private String noonEndTime;
    private String afterStartTime;
    private String endTime;
    private Double restTime;
    private String restDays;
}
