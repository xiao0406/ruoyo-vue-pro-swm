package com.jeesite.modules.swm.entity;

import com.jeesite.common.entity.BaseEntity;
import com.jeesite.common.entity.DataEntity;
import com.jeesite.common.mybatis.annotation.Column;
import com.jeesite.common.mybatis.annotation.Table;
import com.jeesite.common.mybatis.mapper.query.QueryType;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;
import java.math.BigDecimal;

/**
 * 考勤月统计表实体类
 * @author zwf
 * @version 2025-05-20
 */
@Table(name = "swm_attendance_summary", alias = "a", columns = {
        @Column(name = "id", attrName = "id", label = "主键ID", isPK = true),
        @Column(name = "employee_id", attrName = "employeeId", label = "员工ID"),
        @Column(name = "employee_name", attrName = "employeeName", label = "员工姓名", queryType = QueryType.LIKE),
        @Column(name = "identity_card", attrName = "identityCard", label = "身份证号", queryType = QueryType.LIKE),
        @Column(name = "department", attrName = "department", label = "所属车间", queryType = QueryType.LIKE),
        @Column(name = "work_process", attrName = "workProcess", label = "所属工序", queryType = QueryType.LIKE),
        @Column(name = "team", attrName = "team", label = "所属班组", queryType = QueryType.LIKE),
        @Column(name = "job_type", attrName = "jobType", label = "工种", queryType = QueryType.LIKE),
        @Column(name = "work_shift", attrName = "workShift", label = "所属班次", queryType = QueryType.LIKE),
        @Column(name = "month", attrName = "month", label = "统计月份(YYYY-MM)", queryType = QueryType.LIKE),
        @Column(name = "scheduled_days", attrName = "scheduledDays", label = "应出勤天数(天)"),
        @Column(name = "actual_days", attrName = "actualDays", label = "实际出勤天数(天)"),
        @Column(name = "actual_attendance_days", attrName = "actualAttendanceDays", label = "实际考勤天数(天)"),
        @Column(name = "attendance_rate", attrName = "attendanceRate", label = "出勤率"),
        @Column(name = "monthly_attendance_rate", attrName = "monthlyAttendanceRate", label = "本月考勤率"),
        @Column(name = "scheduled_hours", attrName = "scheduledHours", label = "应考勤时间(h)"),
        @Column(name = "actual_hours", attrName = "actualHours", label = "实际工作时间(h)"),
        @Column(name = "attendance_achievement_rate", attrName = "attendanceAchievementRate", label = "考勤达成率"),
        @Column(name = "idle_hours", attrName = "idleHours", label = "怠工时长(h)"),
        @Column(name = "efficiency", attrName = "efficiency", label = "功效"),
        @Column(includeEntity = DataEntity.class),
        @Column(includeEntity= BaseEntity.class),
}, orderBy = "a.create_date DESC")
public class SwmAttendanceSummary extends DataEntity<SwmAttendanceSummary> {

    private static final long serialVersionUID = 1L;

    private String employeeId;        // 员工ID
    private String employeeName;        // 员工姓名
    private String identityCard;       // 身份证号
    private String department;          // 所属车间
    private String workProcess;         // 所属工序
    private String team;                // 所属班组
    private String jobType;             // 工种
    private String workShift;           // 所属班次
    private String month;               // 统计月份(YYYY-MM)
    private BigDecimal scheduledDays;   // 应出勤天数(天)
    private BigDecimal actualDays;      // 实际出勤天数(天)
    private BigDecimal actualAttendanceDays;  // 实际考勤天数(天)
    private BigDecimal attendanceRate;  // 出勤率
    private BigDecimal monthlyAttendanceRate;  // 本月考勤率
    private BigDecimal scheduledHours;  // 应考勤时间(h)
    private BigDecimal actualHours;     // 实际工作时间(h)
    private BigDecimal attendanceAchievementRate;  // 考勤达成率
    private BigDecimal idleHours;       // 怠工时长(h)
    private BigDecimal efficiency;      // 功效

    public SwmAttendanceSummary() {
        this(null);
    }

    public SwmAttendanceSummary(String id) {
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

    @Length(min = 0, max = 100, message = "所属车间长度不能超过 100 个字符")
    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    @Length(min = 0, max = 100, message = "所属工序长度不能超过 100 个字符")
    public String getWorkProcess() {
        return workProcess;
    }

    public void setWorkProcess(String workProcess) {
        this.workProcess = workProcess;
    }

    @Length(min = 0, max = 100, message = "所属班组长度不能超过 100 个字符")
    public String getTeam() {
        return team;
    }

    public void setTeam(String team) {
        this.team = team;
    }

    @Length(min = 0, max = 100, message = "工种长度不能超过 100 个字符")
    public String getJobType() {
        return jobType;
    }

    public void setJobType(String jobType) {
        this.jobType = jobType;
    }

    @Length(min = 0, max = 50, message = "所属班次长度不能超过 50 个字符")
    public String getWorkShift() {
        return workShift;
    }

    public void setWorkShift(String workShift) {
        this.workShift = workShift;
    }

    @NotBlank(message = "统计月份不能为空")
    @Length(min = 0, max = 7, message = "统计月份长度不能超过 7 个字符")
    public String getMonth() {
        return month;
    }

    public void setMonth(String month) {
        this.month = month;
    }

    public BigDecimal getScheduledDays() {
        return scheduledDays;
    }

    public void setScheduledDays(BigDecimal scheduledDays) {
        this.scheduledDays = scheduledDays;
    }

    public BigDecimal getActualDays() {
        return actualDays;
    }

    public void setActualDays(BigDecimal actualDays) {
        this.actualDays = actualDays;
    }

    public BigDecimal getAttendanceRate() {
        return attendanceRate;
    }

    public void setAttendanceRate(BigDecimal attendanceRate) {
        this.attendanceRate = attendanceRate;
    }

    public BigDecimal getActualAttendanceDays() {
        return actualAttendanceDays;
    }

    public void setActualAttendanceDays(BigDecimal actualAttendanceDays) {
        this.actualAttendanceDays = actualAttendanceDays;
    }

    public BigDecimal getMonthlyAttendanceRate() {
        return monthlyAttendanceRate;
    }

    public void setMonthlyAttendanceRate(BigDecimal monthlyAttendanceRate) {
        this.monthlyAttendanceRate = monthlyAttendanceRate;
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

    public BigDecimal getAttendanceAchievementRate() {
        return attendanceAchievementRate;
    }

    public void setAttendanceAchievementRate(BigDecimal attendanceAchievementRate) {
        this.attendanceAchievementRate = attendanceAchievementRate;
    }

    public BigDecimal getIdleHours() {
        return idleHours;
    }

    public void setIdleHours(BigDecimal idleHours) {
        this.idleHours = idleHours;
    }

    public BigDecimal getEfficiency() {
        return efficiency;
    }

    public void setEfficiency(BigDecimal efficiency) {
        this.efficiency = efficiency;
    }
} 