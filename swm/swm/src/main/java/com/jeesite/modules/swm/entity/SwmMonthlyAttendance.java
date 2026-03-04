package com.jeesite.modules.swm.entity;

import com.jeesite.common.entity.DataEntity;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;
import java.math.BigDecimal;
import java.util.Date;

public class SwmMonthlyAttendance extends DataEntity<SwmMonthlyAttendance> {

    private String monthly;
    private String employeeId; // 员工ID
    private String employeeName; // 员工姓名
    private String identityCard; // 身份证号
    private String phoneNumber; // 手机号
    private String team; // 班组
    private String jobType; // 工种
    private BigDecimal attendanceDay; //出勤天数/天
    private String monthlyAttendanceRate; //月出勤率
    private BigDecimal validAttendanceDays; //有效考勤天数
    private BigDecimal actualHours; //本月有效考勤时长
    private BigDecimal idleHours; //本月怠工时长

    private Date currentMonth; //月份

    private Date stratDate; //开始时间
    private Date endDate; //结束时间

    private String personType; //人员类型

    private String timeRange;

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

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getTeam() {
        return team;
    }

    public void setTeam(String team) {
        this.team = team;
    }

    public String getJobType() {
        return jobType;
    }

    public void setJobType(String jobType) {
        this.jobType = jobType;
    }

    public BigDecimal getAttendanceDay() {
        return attendanceDay;
    }

    public void setAttendanceDay(BigDecimal attendanceDay) {
        this.attendanceDay = attendanceDay;
    }

    public String getMonthlyAttendanceRate() {
        return monthlyAttendanceRate;
    }

    public void setMonthlyAttendanceRate(String monthlyAttendanceRate) {
        this.monthlyAttendanceRate = monthlyAttendanceRate;
    }

    public BigDecimal getValidAttendanceDays() {
        return validAttendanceDays;
    }

    public void setValidAttendanceDays(BigDecimal validAttendanceDays) {
        this.validAttendanceDays = validAttendanceDays;
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

    public Date getCurrentMonth() {
        return currentMonth;
    }

    public void setCurrentMonth(Date currentMonth) {
        this.currentMonth = currentMonth;
    }

    public String getMonthly() {
        return monthly;
    }

    public void setMonthly(String monthly) {
        this.monthly = monthly;
    }

    public Date getStratDate() {
        return stratDate;
    }

    public void setStratDate(Date stratDate) {
        this.stratDate = stratDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public String getPersonType() {
        return personType;
    }

    public void setPersonType(String personType) {
        this.personType = personType;
    }

    public String getTimeRange() {
        return timeRange;
    }

    public void setTimeRange(String timeRange) {
        this.timeRange = timeRange;
    }
}
