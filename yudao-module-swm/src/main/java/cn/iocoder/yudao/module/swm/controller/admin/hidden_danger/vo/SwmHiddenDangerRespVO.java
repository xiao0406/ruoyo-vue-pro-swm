package cn.iocoder.yudao.module.swm.controller.admin.hidden_danger.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "隐患排查 Response VO")
@Data
public class SwmHiddenDangerRespVO {

    @Schema(description = "隐患编号")
    private String id;

    @Schema(description = "隐患名称")
    private String dangerName;

    @Schema(description = "隐患位置")
    private String location;

    @Schema(description = "关联巡检计划编号")
    private String inspectionPlanId;

    @Schema(description = "是否布设信标")
    private String isBeaconDeployed;

    @Schema(description = "是否已处置")
    private String isHandled;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    @Schema(description = "创建者")
    private String creator;

    @Schema(description = "更新者")
    private String updater;

    @Schema(description = "状态")
    private String status;

    @Schema(description = "备注")
    private String remarks;

}
