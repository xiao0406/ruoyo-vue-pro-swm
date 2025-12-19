package com.jeesite.modules.swm.entity.dto;

import com.jeesite.common.entity.DataEntity;
import com.jeesite.modules.swm.entity.SwmPerson;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 劳务看板dto
 */
@Data
public class SwmDashboardDto {

    /**
     * “人员休闲区停留时长”排名（姓名、班组、工种、月怠工时长）
     */
    @Data
    public class IdleHoursRankingDto extends DataEntity<IdleHoursRankingDto> {

        @ApiModelProperty(value = "姓名")
        private String employeeName;

        @ApiModelProperty(value = "所属车间")
        private String departmentName;

        @ApiModelProperty(value = "所属工种")
        private String jobType;

        @ApiModelProperty(value = "怠工时长")
        private BigDecimal idleHours;

        @ApiModelProperty(value = "月怠工时长")
        private BigDecimal monthIdleHours;

        private Date startDate;
        private Date endDate;


    }
}
