package cn.iocoder.yudao.module.swm.dal.dataobject;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.math.BigDecimal;

/**
 * 考勤汇总 DO
 * 表: swm_attendance_summary
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@TableName("swm_attendance_summary")
public class SwmAttendanceSummaryDO extends SwmBaseDO {
    private String employeeId;
    private String employeeName;
    private String identityCard;
    private String department;
    private String workProcess;
    private String team;
    private String jobType;
    private String workShift;
    private String month;
    private BigDecimal scheduledDays;
    private BigDecimal actualDays;
    private BigDecimal actualAttendanceDays;
    private BigDecimal attendanceRate;
    private BigDecimal monthlyAttendanceRate;
    private BigDecimal scheduledHours;
    private BigDecimal actualHours;
    private BigDecimal attendanceAchievementRate;
    private BigDecimal idleHours;
    private BigDecimal efficiency;

    // ===== 兼容字段（JeeSite 迁移过渡） =====
    /** 企业编码 */
    private String corpCode;
    /** 企业名称 */
    private String corpName;
}
