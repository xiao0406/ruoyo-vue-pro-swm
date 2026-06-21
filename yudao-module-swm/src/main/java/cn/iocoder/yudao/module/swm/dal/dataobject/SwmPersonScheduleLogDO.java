package cn.iocoder.yudao.module.swm.dal.dataobject;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;

/**
 * 排班日志 DO
 * 表: swm_person_schedule_log
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@TableName("swm_person_schedule_log")
public class SwmPersonScheduleLogDO extends SwmBaseDO {
    private String operateUser;
    private LocalDateTime operateTime;
    private String targetClasses;
    private String personId;
    private String operateDesc;
    private String beforeClasses;
}
