package cn.iocoder.yudao.module.swm.controller.admin.attendanceSummary.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "考勤汇总分页查询 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class SwmAttendanceSummaryPageReqVO extends PageParam {

    @Schema(description = "员工ID")
    private String employeeId;

    @Schema(description = "员工姓名")
    private String employeeName;

    @Schema(description = "身份证号")
    private String identityCard;

    @Schema(description = "部门")
    private String department;

    @Schema(description = "班组")
    private String team;

    @Schema(description = "工种")
    private String jobType;

    @Schema(description = "月份")
    private String month;

}
