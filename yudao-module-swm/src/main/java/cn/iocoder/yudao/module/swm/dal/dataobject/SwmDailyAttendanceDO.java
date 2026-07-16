package cn.iocoder.yudao.module.swm.dal.dataobject;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@TableName("swm_daily_attendance")
public class SwmDailyAttendanceDO extends SwmBaseDO {

    private String employeeId;
    private String employeeName;
    private String identityCard;
    private String deviceId;
    private String personType;
    private LocalDate attendanceDate;
    private String workTimeRange;
    private String classes;
    private LocalDateTime clockInTime;
    private LocalDateTime clockInDate;
    private LocalDateTime noonEndTime;
    private LocalDateTime noonEndDate;
    private LocalDateTime afterStartTime;
    private LocalDateTime afterStartDate;
    private LocalDateTime clockOutTime;
    private LocalDateTime clockOutDate;
    private LocalDateTime clockStartTime;
    private LocalDateTime clockEndTime;
    private BigDecimal scheduledHours;
    private BigDecimal restTime;
    private BigDecimal actualHours;
    private BigDecimal idleHours;
    private BigDecimal effectiveWorkHours;
    private BigDecimal dailyEfficiency;
    private BigDecimal dailyAchievementRate;
    private String attendanceNormal;
    private String currentPosition;
    private Boolean pendingClockOutCompensate;

    @TableField(exist = false)
    private LocalDate beginAttendanceDate;
    @TableField(exist = false)
    private LocalDate endAttendanceDate;
    @TableField(exist = false)
    private String attendanceStatus;
    @TableField(exist = false)
    private String department;
    @TableField(exist = false)
    private String team;
    @TableField(exist = false)
    private String powerOnStatus;
    @TableField(exist = false)
    private String company;
    @TableField(exist = false)
    private String prodLine;
    @TableField(exist = false)
    private String phoneNumber;
    @TableField(exist = false)
    private String jobType;
    @TableField(exist = false)
    private List<String> idCards;
}
