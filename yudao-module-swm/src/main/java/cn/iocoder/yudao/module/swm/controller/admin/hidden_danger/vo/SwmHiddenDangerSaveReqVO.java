package cn.iocoder.yudao.module.swm.controller.admin.hidden_danger.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;

@Schema(description = "隐患排查新增/修改 Request VO")
@Data
public class SwmHiddenDangerSaveReqVO {

    @Schema(description = "隐患编号")
    private String id;

    @Schema(description = "隐患名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "隐患名称不能为空")
    private String dangerName;

    @Schema(description = "隐患位置")
    private String location;

    @Schema(description = "关联巡检计划编号")
    private String inspectionPlanId;

    @Schema(description = "是否布设信标")
    private String isBeaconDeployed;

    @Schema(description = "是否已处置")
    private String isHandled;

}
