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
    public static class IdleHoursRankingDto extends DataEntity<IdleHoursRankingDto> {

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

    /**
     * 管理人员在岗情况”，列表类型：姓名、手机号、安全帽使用时长（月）、车间在岗时长（月）
     */
    @Data
    public static class ManagementOnDutyDto extends DataEntity<ManagementOnDutyDto> {

        @ApiModelProperty(value = "姓名")
        private String employeeName;

        @ApiModelProperty(value = "手机号")
        private String phoneNumber;

        @ApiModelProperty(value = "怠工时长")
        private BigDecimal idleHours;

        @ApiModelProperty(value = "使用时长（工作区+怠工时长）")
        private BigDecimal useHours;
        @ApiModelProperty(value = "车间在岗时长（工作区考勤时长）")
        private BigDecimal workDepartmentHours;
        private Date startDate;
        private Date endDate;
    }
}
