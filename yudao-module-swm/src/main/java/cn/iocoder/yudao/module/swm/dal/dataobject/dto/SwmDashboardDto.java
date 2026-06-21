package cn.iocoder.yudao.module.swm.dal.dataobject.dto;

import lombok.Data;
import java.math.BigDecimal;

/**
 * 仪表盘统计 DTO
 */
public class SwmDashboardDto {

    @Data
    public static class IdleHoursRankingDto {
        private String employeeId;
        private String employeeName;
        private String identityCard;
        private String team;
        private BigDecimal totalIdleHours;
    }

    @Data
    public static class ManagementOnDutyDto {
        private String employeeId;
        private String employeeName;
        private String identityCard;
        private String team;
        private BigDecimal totalHours;
    }

    @Data
    public static class TeamAttendanceAnalysis {
        private String name;
        private Long count;
    }

    @Data
    public static class NoAttendancePerson {
        private String employeeId;
        private String employeeName;
        private String identityCard;
        private String team;
        private String personType;
        private String organization;
        private String workShop;
        private Integer absentDays;
    }
}
