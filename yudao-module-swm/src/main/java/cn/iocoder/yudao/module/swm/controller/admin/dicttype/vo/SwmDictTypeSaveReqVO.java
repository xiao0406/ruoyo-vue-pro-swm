package cn.iocoder.yudao.module.swm.controller.admin.dicttype.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;

@Schema(description = "字典类型新增/修改 Request VO")
@Data
public class SwmDictTypeSaveReqVO {

    @Schema(description = "字典类型编号")
    private String id;

    @Schema(description = "字典名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "字典名称不能为空")
    private String dictName;

    @Schema(description = "字典类型", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "字典类型不能为空")
    private String dictType;

    @Schema(description = "是否系统字典")
    private String isSys;

}
