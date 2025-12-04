package com.jeesite.modules.swm.entity;

import com.jeesite.common.entity.BaseEntity;
import com.jeesite.common.entity.DataEntity;
import com.jeesite.common.mybatis.annotation.Column;
import com.jeesite.common.mybatis.annotation.Table;
import com.jeesite.common.mybatis.mapper.query.QueryType;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
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
        @Column(name = "identity_card", attrName = "identityCard", label = "身份证号"),
        @Column(name = "device_id", attrName = "deviceId", label = "绑定设备号"),
        @Column(name = "person_type", attrName = "personType", label = "人员类型"),
        @Column(name = "attendance_date", attrName = "attendanceDate", label = "考勤日期"),
        @Column(name = "work_time_range", attrName = "workTimeRange", label = "应考勤时间范围", queryType = QueryType.LIKE),
        @Column(name = "classes", attrName = "classes", label = "班次"),
        @Column(name = "clock_in_time", attrName = "clockInTime", label = "上班打卡时间"),
        @Column(name = "clock_in_date", attrName = "clockInDate", label = "上班打卡完整时间"),
        @Column(name = "clock_out_time", attrName = "clockOutTime", label = "下班打卡时间"),
        @Column(name = "clock_out_date", attrName = "clockOutDate", label = "下班打卡完整时间"),
        @Column(name = "clock_start_time", attrName = "clockStartTime", label = "打卡开始时间"),
        @Column(name = "clock_end_time", attrName = "clockEndTime", label = "打卡结束时间"),
        @Column(name = "scheduled_hours", attrName = "scheduledHours", label = "应考勤时长(h)"),
        @Column(name = "rest_time", attrName = "restTime", label = "休息时长(h)"),
        @Column(name = "actual_hours", attrName = "actualHours", label = "实际考勤时长(h)"),
        @Column(name = "idle_hours", attrName = "idleHours", label = "怠工时长(h)"),
        @Column(name = "effective_work_hours", attrName = "effectiveWorkHours", label = "实际工作时长(h)"),
        @Column(name = "daily_efficiency", attrName = "dailyEfficiency", label = "今日功效"),
        @Column(name = "daily_achievement_rate", attrName = "dailyAchievementRate", label = "今日达成率"),
        @Column(name = "attendance_normal", attrName = "attendanceNormal", label = "考勤是否正常(0正常 1异常)"),
        @Column(name = "current_position", attrName = "currentPosition", label = "当前位置(0工作区 1休息区)"),
        @Column(name = "pending_clock_out_compensate", attrName = "pendingClockOutCompensate", label = "当天该员工是否已经触发过补偿并且未恢复"),
        @Column(includeEntity = DataEntity.class),
        @Column(includeEntity= BaseEntity.class),
}, orderBy = "a.attendance_date DESC")
@Data
public class SwmDailyAttendance extends DataEntity<SwmDailyAttendance> {

    private static final long serialVersionUID = 1L;

    private String employeeId; // 员工ID
    private String employeeName; // 员工姓名
    private String identityCard; // 身份证号
    private String deviceId; // 绑定设备号
    private String personType; // 人员类型
    private Date attendanceDate; // 考勤日期
    private String workTimeRange; // 应考勤时间范围(如08:00-17:00)
    private String classes; // 班次(早班/中班/晚班)
    private Date clockInTime; // 上班打卡时间
    private Date clockInDate; // 上班打卡完整时间
    private Date clockOutTime; // 下班打卡时间
    private Date clockOutDate; // 下班打卡完整时间
    private Date clockStartTime; // 打卡开始时间
    private Date clockEndTime; // 打卡结束时间
    private BigDecimal scheduledHours; // 应考勤时长(h)
    private BigDecimal restTime; // 休息时长(h)
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

    /** 当天该员工是否已经触发过补偿并且未恢复， true -已经补卡了 */
    private boolean pendingClockOutCompensate;


    @ApiModelProperty(value = "所属车间")
    private String department;
    @ApiModelProperty(value = "所属车间")
    private String departmentName;
    @ApiModelProperty(value = "所属班组")
    private String team;
    @ApiModelProperty(value = "所属班组")
    private String teamName;

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

    @Length(min = 0, max = 20, message = "身份证号长度不能超过 20 个字符")
    public String getIdentityCard() {
        return identityCard;
    }

    public void setIdentityCard(String identityCard) {
        this.identityCard = identityCard;
    }

    @Length(min = 0, max = 50, message = "设备号长度不能超过50个字符")
    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    @Length(min = 0, max = 100, message = "人员类型不能超过100个字符")
    public String getPersonType() {
        return personType;
    }

    public void setPersonType(String personType) {
        this.personType = personType;
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

    public Date getClockInDate() {
        return clockInDate;
    }

    public void setClockInDate(Date clockInDate) {
        this.clockInDate = clockInDate;
    }

    @Length(min = 0, max = 20, message = "班次长度不能超过 20 个字符")
    public String getClasses() {
        return classes;
    }

    public void setClasses(String classes) {
        this.classes = classes;
    }

    public Date getClockOutTime() {
        return clockOutTime;
    }

    public void setClockOutTime(Date clockOutTime) {
        this.clockOutTime = clockOutTime;
    }

    public Date getClockOutDate() {
        return clockOutDate;
    }

    public void setClockOutDate(Date clockOutDate) {
        this.clockOutDate = clockOutDate;
    }

    public BigDecimal getScheduledHours() {
        return scheduledHours;
    }

    public void setScheduledHours(BigDecimal scheduledHours) {
        this.scheduledHours = scheduledHours;
    }

    public BigDecimal getRestTime() {
        return restTime;
    }

    public void setRestTime(BigDecimal restTime) {
        this.restTime = restTime;
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

    public Date getClockStartTime() {
        return clockStartTime;
    }

    public void setClockStartTime(Date clockStartTime) {
        this.clockStartTime = clockStartTime;
    }

    public Date getClockEndTime() {
        return clockEndTime;
    }

    public void setClockEndTime(Date clockEndTime) {
        this.clockEndTime = clockEndTime;
    }

    // 判断是否为有效未打卡（非休息状态且未打卡）
    public boolean isEffectiveAbsence() {
        return "0".equals(status) && clockInTime == null;
    }

    public boolean isPendingClockOutCompensate() {
        return pendingClockOutCompensate;
    }

    public void setPendingClockOutCompensate(boolean pendingClockOutCompensate) {
        this.pendingClockOutCompensate = pendingClockOutCompensate;
    }
}
