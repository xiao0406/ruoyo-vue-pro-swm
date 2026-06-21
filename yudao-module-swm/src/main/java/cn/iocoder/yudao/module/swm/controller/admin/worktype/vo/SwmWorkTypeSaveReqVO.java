package cn.iocoder.yudao.module.swm.controller.admin.worktype.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;

@Schema(description = "工种管理新增/修改 Request VO")
@Data
public class SwmWorkTypeSaveReqVO {

    @Schema(description = "工种编号")
    private String id;

    @Schema(description = "工种名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "工种名称不能为空")
    private String workType;

    @Schema(description = "工种编码")
    private String workTypeCode;

    @Schema(description = "描述")
    private String description;

}
