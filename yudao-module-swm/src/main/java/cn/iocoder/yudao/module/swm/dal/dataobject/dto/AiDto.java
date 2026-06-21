package cn.iocoder.yudao.module.swm.dal.dataobject.dto;

import lombok.Data;

/**
 * AI 报表 DTO
 */
public class AiDto {

    @Data
    public static class RiskStatistics {
        private String tableName;
        private Long count;
    }

    @Data
    public static class WorkerFatigue {
        private String employeeId;
        private String employeeName;
        private String identityCard;
        private String team;
        private Double actualHours;
        private String attendanceDate;
    }
}
