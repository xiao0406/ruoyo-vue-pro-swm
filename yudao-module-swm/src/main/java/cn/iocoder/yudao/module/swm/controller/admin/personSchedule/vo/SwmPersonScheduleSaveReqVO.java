package cn.iocoder.yudao.module.swm.controller.admin.personSchedule.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;

@Schema(description = "人员排班新增/修改 Request VO")
@Data
public class SwmPersonScheduleSaveReqVO {

    @Schema(description = "排班编号")
    private String id;

    @Schema(description = "人员姓名")
    private String personName;

    @Schema(description = "月份", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "月份不能为空")
    private String month;

    @Schema(description = "班次")
    private String classes;

    @Schema(description = "身份证号")
    private String idCard;

    @Schema(description = "员工ID")
    private String employeeId;

    @Schema(description = "备注")
    private String remarks;

}
