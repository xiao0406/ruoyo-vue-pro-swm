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

/**
 * 每日考勤 DO
 *
 * 迁移自 JeeSite: com.jeesite.modules.swm.entity.SwmDailyAttendance
 * 表: swm_daily_attendance
 */
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
    /** 考勤是否正常(0正常 1异常) */
    private String attendanceNormal;
    /** 当前位置(0工作区 1休息区) */
    private String currentPosition;
    /** 当天该员工是否已触发过补偿且未恢复 */
    private Boolean pendingClockOutCompensate;

    // ===== 非数据库字段（查询用） =====
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
    /** 身份证号列表（查询用，IN 条件） */
    @TableField(exist = false)
    private List<String> idCards;

    // ===== 兼容字段（JeeSite 迁移过渡） =====
    /** 企业编码（兼容 JeeSite 老逻辑，来自 tenantId 映射） */
    private String corpCode;
    /** 企业名称（兼容 JeeSite 老逻辑） */
    private String corpName;

}
