/**
 * 考勤检查结果实体类
 * @author Shawn
 * @date 2025-01-14
 */
package com.jeesite.modules.swm.entity;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 考勤检查结果实体类
 */
public class AttendanceCheckResult implements Serializable {

    private static final long serialVersionUID = 1L;

    private String idCard; // 身份证号
    private String employeeName; // 员工姓名
    private String checkDate; // 检查日期
    private String workShift; // 班次 (早班/中班/晚班)
    private Boolean isLate; // 是否迟到
    private Boolean isEarlyLeave; // 是否早退
    private Boolean isAbsent; // 是否旷工
    private BigDecimal idleHours; // 怠工时长（小时）
    private String attendanceStatus; // 考勤状态描述

    public AttendanceCheckResult() {
    }

    public String getIdCard() {
        return idCard;
    }

    public void setIdCard(String idCard) {
        this.idCard = idCard;
    }

    public String getEmployeeName() {
        return employeeName;
    }

    public void setEmployeeName(String employeeName) {
        this.employeeName = employeeName;
    }

    public String getCheckDate() {
        return checkDate;
    }

    public void setCheckDate(String checkDate) {
        this.checkDate = checkDate;
    }

    public String getWorkShift() {
        return workShift;
    }

    public void setWorkShift(String workShift) {
        this.workShift = workShift;
    }

    public Boolean getIsLate() {
        return isLate;
    }

    public void setIsLate(Boolean isLate) {
        this.isLate = isLate;
    }

    public Boolean getIsEarlyLeave() {
        return isEarlyLeave;
    }

    public void setIsEarlyLeave(Boolean isEarlyLeave) {
        this.isEarlyLeave = isEarlyLeave;
    }

    public Boolean getIsAbsent() {
        return isAbsent;
    }

    public void setIsAbsent(Boolean isAbsent) {
        this.isAbsent = isAbsent;
    }

    public BigDecimal getIdleHours() {
        return idleHours;
    }

    public void setIdleHours(BigDecimal idleHours) {
        this.idleHours = idleHours;
    }

    public String getAttendanceStatus() {
        return attendanceStatus;
    }

    public void setAttendanceStatus(String attendanceStatus) {
        this.attendanceStatus = attendanceStatus;
    }
}