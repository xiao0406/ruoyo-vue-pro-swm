package cn.iocoder.yudao.module.swm.controller.admin.danger_disposal.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;

@Schema(description = "隐患处置新增/修改 Request VO")
@Data
public class SwmDangerDisposalSaveReqVO {

    @Schema(description = "隐患处置编号")
    private String id;

    @Schema(description = "隐患ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "隐患ID不能为空")
    private String hiddenDangerId;

    @Schema(description = "隐患名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "隐患名称不能为空")
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

}
