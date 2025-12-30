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

    /**
     * 修改“班组出勤率分析”（倒排）：应出勤人数、实际出勤人数、日出勤率、月出勤率
     */
    @Data
    public static class TeamAttendanceAnalysis extends DataEntity<TeamAttendanceAnalysis> {

        @ApiModelProperty(value = "班组名称")
        private String teamName;
        @ApiModelProperty(value = "车间名称")
        private String departmentName;

        @ApiModelProperty(value = "应出勤人数")
        private Integer shouldAttendance;
        @ApiModelProperty(value = "实际出勤人数")
        private Integer actualAttendance;
        @ApiModelProperty(value = "日出勤率")
        private BigDecimal dailyAttendanceRate;
        @ApiModelProperty(value = "月出勤率")
        private BigDecimal monthlyAttendanceRate;

        //打卡
        private Date clockInDate;
        private String employeeId;

        private Date startDate;
        private Date endDate;
        private String attendanceDate;
    }

    /**
     * 新增“长时间未出勤人员”清单，列表类型：姓名、班组、手机号、累计未出勤时间
     */
    @Data
    public static class NoAttendancePerson extends DataEntity<NoAttendancePerson> {

        @ApiModelProperty(value = "班组名称")
        private String teamName;
        @ApiModelProperty(value = "人员名称")
        private String employeeName;
        @ApiModelProperty(value = "手机号")
        private String phoneNumber;
        @ApiModelProperty(value = "累计未出勤时间")
        private Integer absentDays;

        //上班卡
        private Date clockInDate;
        //下班卡
        private Date clockOutDate;


        private Date startDate;
        private Date endDate;
    }
}
