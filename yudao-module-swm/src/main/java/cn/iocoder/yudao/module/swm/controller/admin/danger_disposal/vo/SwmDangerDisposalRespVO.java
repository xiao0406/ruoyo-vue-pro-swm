package cn.iocoder.yudao.module.swm.controller.admin.danger_disposal.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "隐患处置 Response VO")
@Data
public class SwmDangerDisposalRespVO {

    @Schema(description = "隐患处置编号")
    private String id;

    @Schema(description = "隐患ID")
    private String hiddenDangerId;

    @Schema(description = "隐患名称")
    private String dangerName;

    @Schema(description = "位置")
    private String location;

    @Schema(description = "处置时间")
    private LocalDateTime disposalTime;

    @Schema(description = "处置方案")
    private String disposalPlan;

    @Schema(description = "处置内容")
    private String disposalContent;

    @Schema(description = "处置方式")
    private String disposalMethod;

    @Schema(description = "处置状态")
    private String disposalStatus;

    @Schema(description = "处置人")
    private String disposalUser;

    @Schema(description = "附件")
    private String attachment;

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
