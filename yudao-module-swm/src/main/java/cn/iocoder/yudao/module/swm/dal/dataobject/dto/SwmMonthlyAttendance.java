package cn.iocoder.yudao.module.swm.dal.dataobject.dto;

import lombok.Data;
import java.math.BigDecimal;

/**
 * 月度考勤统计 DTO
 */
@Data
public class SwmMonthlyAttendance {
    private String employeeId;
    private String employeeName;
    private String identityCard;
    private String team;
    private String jobType;
    private String personType;
    private Integer totalDays;
    private Integer attendanceDays;
    private Integer absentDays;
    private BigDecimal totalActualHours;
    private BigDecimal totalScheduledHours;
    private BigDecimal totalIdleHours;
    private BigDecimal totalEffectiveWorkHours;
    private BigDecimal avgEfficiency;
    private BigDecimal avgAchievementRate;
}
