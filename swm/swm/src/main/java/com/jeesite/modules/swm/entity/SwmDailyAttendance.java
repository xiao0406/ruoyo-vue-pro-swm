package com.jeesite.modules.swm.entity;

import com.jeesite.common.entity.DataEntity;
import com.jeesite.common.mybatis.annotation.Column;
import com.jeesite.common.mybatis.annotation.Table;
import com.jeesite.common.mybatis.mapper.query.QueryType;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 日考勤统计表实体类
 * 
 * @author zwf
 * @version 2025-05-20
 */
@Table(name = "swm_daily_attendance", alias = "a", columns = {
        @Column(name = "id", attrName = "id", label = "主键ID", isPK = true),
        @Column(name = "employee_id", attrName = "employeeId", label = "员工ID"),
        @Column(name = "employee_name", attrName = "employeeName", label = "员工姓名", queryType = QueryType.LIKE),
        @Column(name = "attendance_date", attrName = "attendanceDate", label = "考勤日期"),
        @Column(name = "work_time_range", attrName = "workTimeRange", label = "应考勤时间范围", queryType = QueryType.LIKE),
        @Column(name = "clock_in_time", attrName = "clockInTime", label = "上班打卡时间"),
        @Column(name = "clock_out_time", attrName = "clockOutTime", label = "下班打卡时间"),
        @Column(name = "scheduled_hours", attrName = "scheduledHours", label = "应考勤时长(h)"),
        @Column(name = "actual_hours", attrName = "actualHours", label = "实际考勤时长(h)"),
        @Column(name = "idle_hours", attrName = "idleHours", label = "怠工时长(h)"),
        @Column(name = "effective_work_hours", attrName = "effectiveWorkHours", label = "实际工作时长(h)"),
        @Column(name = "daily_efficiency", attrName = "dailyEfficiency", label = "今日功效"),
        @Column(name = "daily_achievement_rate", attrName = "dailyAchievementRate", label = "今日达成率"),
        @Column(name = "attendance_normal", attrName = "attendanceNormal", label = "考勤是否正常(0正常 1异常)"),
        @Column(name = "current_position", attrName = "currentPosition", label = "当前位置(0工作区 1休息区)"),
        @Column(includeEntity = DataEntity.class)
}, orderBy = "a.attendance_date DESC")
public class SwmDailyAttendance extends DataEntity<SwmDailyAttendance> {

    private static final long serialVersionUID = 1L;

    private String employeeId; // 员工ID
    private String employeeName; // 员工姓名
    private Date attendanceDate; // 考勤日期
    private String workTimeRange; // 应考勤时间范围(如08:00-17:00)
    private Date clockInTime; // 上班打卡时间
    private Date clockOutTime; // 下班打卡时间
    private BigDecimal scheduledHours; // 应考勤时长(h)
    private BigDecimal actualHours; // 实际考勤时长(h)
    private BigDecimal idleHours; // 怠工时长(h)
    private BigDecimal effectiveWorkHours; // 实际工作时长(h)
    private BigDecimal dailyEfficiency; // 今日功效
    private BigDecimal dailyAchievementRate; // 今日达成率
    private String attendanceNormal; // 考勤是否正常(0正常 1异常)
    private String currentPosition; // 当前位置(0工作区 1休息区)

    // 查询条件字段
    private Date beginAttendanceDate; // 查询开始考勤日期
    private Date endAttendanceDate; // 查询结束考勤日期

    public SwmDailyAttendance() {
        this(null);
    }

    public SwmDailyAttendance(String id) {
        super(id);
    }

    @Length(min = 0, max = 64, message = "员工ID长度不能超过 64 个字符")
    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }

    @NotBlank(message = "员工姓名不能为空")
    @Length(min = 0, max = 50, message = "员工姓名长度不能超过 50 个字符")
    public String getEmployeeName() {
        return employeeName;
    }

    public void setEmployeeName(String employeeName) {
        this.employeeName = employeeName;
    }

    @NotNull(message = "考勤日期不能为空")
    public Date getAttendanceDate() {
        return attendanceDate;
    }

    public void setAttendanceDate(Date attendanceDate) {
        this.attendanceDate = attendanceDate;
    }

    @Length(min = 0, max = 50, message = "应考勤时间范围长度不能超过 50 个字符")
    public String getWorkTimeRange() {
        return workTimeRange;
    }

    public void setWorkTimeRange(String workTimeRange) {
        this.workTimeRange = workTimeRange;
    }

    public Date getClockInTime() {
        return clockInTime;
    }

    public void setClockInTime(Date clockInTime) {
        this.clockInTime = clockInTime;
    }

    public Date getClockOutTime() {
        return clockOutTime;
    }

    public void setClockOutTime(Date clockOutTime) {
        this.clockOutTime = clockOutTime;
    }

    public BigDecimal getScheduledHours() {
        return scheduledHours;
    }

    public void setScheduledHours(BigDecimal scheduledHours) {
        this.scheduledHours = scheduledHours;
    }

    public BigDecimal getActualHours() {
        return actualHours;
    }

    public void setActualHours(BigDecimal actualHours) {
        this.actualHours = actualHours;
    }

    public BigDecimal getIdleHours() {
        return idleHours;
    }

    public void setIdleHours(BigDecimal idleHours) {
        this.idleHours = idleHours;
    }

    public BigDecimal getEffectiveWorkHours() {
        return effectiveWorkHours;
    }

    public void setEffectiveWorkHours(BigDecimal effectiveWorkHours) {
        this.effectiveWorkHours = effectiveWorkHours;
    }

    public BigDecimal getDailyEfficiency() {
        return dailyEfficiency;
    }

    public void setDailyEfficiency(BigDecimal dailyEfficiency) {
        this.dailyEfficiency = dailyEfficiency;
    }

    public BigDecimal getDailyAchievementRate() {
        return dailyAchievementRate;
    }

    public void setDailyAchievementRate(BigDecimal dailyAchievementRate) {
        this.dailyAchievementRate = dailyAchievementRate;
    }

    public Date getBeginAttendanceDate() {
        return beginAttendanceDate;
    }

    public void setBeginAttendanceDate(Date beginAttendanceDate) {
        this.beginAttendanceDate = beginAttendanceDate;
    }

    public Date getEndAttendanceDate() {
        return endAttendanceDate;
    }

    public void setEndAttendanceDate(Date endAttendanceDate) {
        this.endAttendanceDate = endAttendanceDate;
    }

    public String getAttendanceNormal() {
        return attendanceNormal;
    }

    public void setAttendanceNormal(String attendanceNormal) {
        this.attendanceNormal = attendanceNormal;
    }

    public String getCurrentPosition() {
        return currentPosition;
    }

    public void setCurrentPosition(String currentPosition) {
        this.currentPosition = currentPosition;
    }
}
