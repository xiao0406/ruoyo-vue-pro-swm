package cn.iocoder.yudao.module.swm.controller.admin.dictdata.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;

@Schema(description = "字典数据新增/修改 Request VO")
@Data
public class SwmDictDataSaveReqVO {

    @Schema(description = "字典数据编号")
    private String id;

    @Schema(description = "字典编码")
    private String dictCode;

    @Schema(description = "字典标签", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "字典标签不能为空")
    private String dictLabel;

    @Schema(description = "字典值", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "字典值不能为空")
    private String dictValue;

    @Schema(description = "字典图标")
    private String dictIcon;

    @Schema(description = "字典类型", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "字典类型不能为空")
    private String dictType;

    @Schema(description = "是否系统字典")
    private String isSys;

    @Schema(description = "描述")
    private String description;

    @Schema(description = "CSS样式")
    private String cssStyle;

    @Schema(description = "CSS类名")
    private String cssClass;

}
