package com.jeesite.modules.swm.entity;

import com.jeesite.common.utils.excel.annotation.ExcelField;
import com.jeesite.common.utils.excel.annotation.ExcelFields;
import io.swagger.annotations.ApiModelProperty;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 日考勤统计表导出实体类
 *
 * @author zwf
 * @version 2025-07-05
 */
public class SwmDailyAttendanceExportEntity {

    private String employeeId;
    private String employeeName;
    private String personType;
    private Date attendanceDate;
    private String workTimeRange;
    private Date clockInTime;
    private Date clockOutTime;
    private BigDecimal scheduledHours;
    private BigDecimal actualHours;
    private BigDecimal idleHours;
    private BigDecimal effectiveWorkHours;
    private BigDecimal dailyEfficiency;
    private BigDecimal dailyAchievementRate;
    private String attendanceNormal;
    private String currentPosition;
    private String remarks;
    @ApiModelProperty(value = "开机状态  0-开机，1-关机")
    private String powerOnStatus;

    @ExcelFields({
            @ExcelField(title = "员工ID", attrName = "employeeId", align = ExcelField.Align.CENTER, sort = 10),
            @ExcelField(title = "员工姓名", attrName = "employeeName", align = ExcelField.Align.CENTER, sort = 20),
            @ExcelField(title = "人员类型", attrName = "personType", align = ExcelField.Align.CENTER, sort = 30, dictType = "person_type_enum"),
            @ExcelField(title = "考勤日期", attrName = "attendanceDate", align = ExcelField.Align.CENTER, sort = 40, dataFormat = "yyyy-MM-dd"),
            @ExcelField(title = "应考勤时间范围", attrName = "workTimeRange", align = ExcelField.Align.CENTER, sort = 50),
            @ExcelField(title = "上班打卡时间", attrName = "clockInTime", align = ExcelField.Align.CENTER, sort = 60, dataFormat = "HH:mm:ss"),
            @ExcelField(title = "下班打卡时间", attrName = "clockOutTime", align = ExcelField.Align.CENTER, sort = 70, dataFormat = "HH:mm:ss"),
            @ExcelField(title = "应考勤时长(h)", attrName = "scheduledHours", align = ExcelField.Align.CENTER, sort = 80),
            @ExcelField(title = "实际考勤时长(h)", attrName = "actualHours", align = ExcelField.Align.CENTER, sort = 90),
            @ExcelField(title = "怠工时长(h)", attrName = "idleHours", align = ExcelField.Align.CENTER, sort = 100),
            @ExcelField(title = "实际工作时长(h)", attrName = "effectiveWorkHours", align = ExcelField.Align.CENTER, sort = 110),
            @ExcelField(title = "今日功效", attrName = "dailyEfficiency", align = ExcelField.Align.CENTER, sort = 120),
            @ExcelField(title = "今日达成率", attrName = "dailyAchievementRate", align = ExcelField.Align.CENTER, sort = 130),
            @ExcelField(title = "考勤状态", attrName = "attendanceNormal", align = ExcelField.Align.CENTER, sort = 140, dictType = "swm_attendance_status"),
            @ExcelField(title = "当前位置", attrName = "currentPosition", align = ExcelField.Align.CENTER, sort = 150, dictType = "swm_current_position"),
            @ExcelField(title = "备注", attrName = "remarks", align = ExcelField.Align.LEFT, sort = 160),
            @ExcelField(title = "开机状态", attrName = "powerOnStatus", align = ExcelField.Align.LEFT, sort = 170)
    })
    public SwmDailyAttendanceExportEntity() {
    }

    public SwmDailyAttendanceExportEntity(String employeeId, String employeeName, String personType,
            Date attendanceDate, String workTimeRange, Date clockInTime, Date clockOutTime,
            BigDecimal scheduledHours, BigDecimal actualHours, BigDecimal idleHours,
            BigDecimal effectiveWorkHours, BigDecimal dailyEfficiency, BigDecimal dailyAchievementRate,
            String attendanceNormal, String currentPosition, String remarks,String powerOnStatus) {
        this.employeeId = employeeId;
        this.employeeName = employeeName;
        this.personType = personType;
        this.attendanceDate = attendanceDate;
        this.workTimeRange = workTimeRange;
        this.clockInTime = clockInTime;
        this.clockOutTime = clockOutTime;
        this.scheduledHours = scheduledHours;
        this.actualHours = actualHours;
        this.idleHours = idleHours;
        this.effectiveWorkHours = effectiveWorkHours;
        this.dailyEfficiency = dailyEfficiency;
        this.dailyAchievementRate = dailyAchievementRate;
        this.attendanceNormal = attendanceNormal;
        this.currentPosition = currentPosition;
        this.remarks = remarks;
        this.powerOnStatus = powerOnStatus;
    }

    // Getter and Setter methods
    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }

    public String getEmployeeName() {
        return employeeName;
    }

    public void setEmployeeName(String employeeName) {
        this.employeeName = employeeName;
    }

    public String getPersonType() {
        return personType;
    }

    public void setPersonType(String personType) {
        this.personType = personType;
    }

    public Date getAttendanceDate() {
        return attendanceDate;
    }

    public void setAttendanceDate(Date attendanceDate) {
        this.attendanceDate = attendanceDate;
    }

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

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public String getPowerOnStatus() {
        return powerOnStatus;
    }

    public void setPowerOnStatus(String powerOnStatus) {
        this.powerOnStatus = powerOnStatus;
    }
}
