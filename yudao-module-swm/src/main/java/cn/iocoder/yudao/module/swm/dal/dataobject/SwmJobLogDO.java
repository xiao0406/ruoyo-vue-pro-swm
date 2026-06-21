package cn.iocoder.yudao.module.swm.dal.dataobject;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;

/**
 * 定时任务日志 DO
 * 表: swm_job_log
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@TableName("swm_job_log")
public class SwmJobLogDO extends SwmBaseDO {
    private String jobName;
    private String jobParam;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Long duration;
    private String executeStatus;
    private String exceptionInfo;
}
