package com.jeesite.modules.swm.entity;

import com.jeesite.common.utils.excel.annotation.ExcelField;
import com.jeesite.common.utils.excel.annotation.ExcelFields;

import java.math.BigDecimal;
import java.util.Date;

public class SwmMonthlyAttendanceExportEntity {

    private Date currentMonth; //月份
    private String employeeName; // 员工姓名
    private String phoneNumber; // 手机号
    private String team; // 班组
    private String jobType; // 工种
    private BigDecimal attendanceDay; //出勤天数/天
    private String monthlyAttendanceRate; //月出勤率
    private BigDecimal validAttendanceDays; //有效考勤天数
    private BigDecimal actualHours; //本月有效考勤时长
    private BigDecimal idleHours; //本月怠工时长

    @ExcelFields({
            @ExcelField(title = "月份", attrName = "currentMonth", align = ExcelField.Align.CENTER, sort = 10, dataFormat = "yyyy-MM"),
            @ExcelField(title = "员工姓名", attrName = "employeeName", align = ExcelField.Align.CENTER, sort = 20),
            @ExcelField(title = "手机号", attrName = "phoneNumber", align = ExcelField.Align.CENTER, sort = 30),
            @ExcelField(title = "班组", attrName = "team", align = ExcelField.Align.CENTER, sort = 40),
            @ExcelField(title = "工种", attrName = "jobType", align = ExcelField.Align.CENTER, sort = 50),
            @ExcelField(title = "出勤天数/天", attrName = "attendanceDay", align = ExcelField.Align.CENTER, sort = 60),
            @ExcelField(title = "月出勤率", attrName = "monthlyAttendanceRate", align = ExcelField.Align.CENTER, sort = 70),
            @ExcelField(title = "有效考勤天数", attrName = "validAttendanceDays", align = ExcelField.Align.CENTER, sort = 80),
            @ExcelField(title = "本月有效考勤时长(h)", attrName = "actualHours", align = ExcelField.Align.CENTER, sort = 90),
            @ExcelField(title = "本月怠工时长(h)", attrName = "idleHours", align = ExcelField.Align.CENTER, sort = 100)
    })
    public SwmMonthlyAttendanceExportEntity() {
    }

    public SwmMonthlyAttendanceExportEntity(Date currentMonth, String employeeName,
                                            String phoneNumber, String team,
                                            String jobType, BigDecimal attendanceDay,
                                            String monthlyAttendanceRate, BigDecimal validAttendanceDays,
                                            BigDecimal actualHours, BigDecimal idleHours) {
        this.currentMonth = currentMonth;
        this.employeeName = employeeName;
        this.phoneNumber = phoneNumber;
        this.team = team;
        this.jobType = jobType;
        this.attendanceDay = attendanceDay;
        this.monthlyAttendanceRate = monthlyAttendanceRate;
        this.validAttendanceDays = validAttendanceDays;
        this.actualHours = actualHours;
        this.idleHours = idleHours;
    }

    public Date getCurrentMonth() {
        return currentMonth;
    }

    public void setCurrentMonth(Date currentMonth) {
        this.currentMonth = currentMonth;
    }

    public String getEmployeeName() {
        return employeeName;
    }

    public void setEmployeeName(String employeeName) {
        this.employeeName = employeeName;
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
}
