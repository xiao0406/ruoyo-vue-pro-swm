/**
 * @author Shawn
 * @date 2025/06/26
 */
package com.jeesite.modules.swm.entity;

import javax.validation.constraints.NotBlank;

import com.jeesite.common.entity.BaseEntity;
import org.hibernate.validator.constraints.Length;
import java.util.Date;

import com.jeesite.common.entity.DataEntity;
import com.jeesite.common.mybatis.annotation.Column;
import com.jeesite.common.mybatis.annotation.Table;
import com.jeesite.common.mybatis.mapper.query.QueryType;

/**
 * 定时任务调度日志表Entity
 * 
 * @author Shawn
 * @version 2025-06-26
 */
@Table(name = "swm_job_log", alias = "a", columns = {
        @Column(name = "id", attrName = "id", label = "ID", isPK = true),
        @Column(name = "job_name", attrName = "jobName", label = "任务名称", queryType = QueryType.LIKE),
        @Column(name = "job_param", attrName = "jobParam", label = "任务参数", queryType = QueryType.LIKE),
        @Column(name = "start_time", attrName = "startTime", label = "执行开始时间"),
        @Column(name = "end_time", attrName = "endTime", label = "执行结束时间"),
        @Column(name = "duration", attrName = "duration", label = "执行耗时（毫秒）"),
        @Column(name = "execute_status", attrName = "executeStatus", label = "执行结果（0 - 成功，1 - 失败）"),
        @Column(name = "exception_info", attrName = "exceptionInfo", label = "异常信息"),
        @Column(includeEntity = DataEntity.class),
        @Column(includeEntity= BaseEntity.class),
}, orderBy = "a.update_date DESC")
public class SwmJobLog extends DataEntity<SwmJobLog> {

    private static final long serialVersionUID = 1L;
    private String jobName; // 任务名称
    private String jobParam; // 任务参数
    private Date startTime; // 执行开始时间
    private Date endTime; // 执行结束时间
    private Long duration; // 执行耗时（毫秒）
    private String executeStatus; // 执行结果（0 - 成功，1 - 失败）
    private String exceptionInfo; // 异常信息

    public SwmJobLog() {
        this(null);
    }

    public SwmJobLog(String id) {
        super(id);
    }

    @NotBlank(message = "任务名称不能为空")
    @Length(min = 0, max = 255, message = "任务名称长度不能超过 255 个字符")
    public String getJobName() {
        return jobName;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    @Length(min = 0, max = 500, message = "任务参数长度不能超过 500 个字符")
    public String getJobParam() {
        return jobParam;
    }

    public void setJobParam(String jobParam) {
        this.jobParam = jobParam;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public Long getDuration() {
        return duration;
    }

    public void setDuration(Long duration) {
        this.duration = duration;
    }

    @NotBlank(message = "执行结果（0 - 成功，1 - 失败）不能为空")
    @Length(min = 0, max = 1, message = "执行结果（0 - 成功，1 - 失败）长度不能超过 1 个字符")
    public String getExecuteStatus() {
        return executeStatus;
    }

    public void setExecuteStatus(String executeStatus) {
        this.executeStatus = executeStatus;
    }

    public String getExceptionInfo() {
        return exceptionInfo;
    }

    public void setExceptionInfo(String exceptionInfo) {
        this.exceptionInfo = exceptionInfo;
    }

}