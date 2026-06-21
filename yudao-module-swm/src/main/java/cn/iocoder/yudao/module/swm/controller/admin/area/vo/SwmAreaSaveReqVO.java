package cn.iocoder.yudao.module.swm.controller.admin.area.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;

@Schema(description = "区域管理新增/修改 Request VO")
@Data
public class SwmAreaSaveReqVO {

    @Schema(description = "区域编号")
    private String id;

    @Schema(description = "区域名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "区域名称不能为空")
    private String areaName;

    @Schema(description = "区域类型")
    private String areaType;

    @Schema(description = "区域颜色")
    private String areaColor;

    @Schema(description = "车间ID")
    private String workShop;

    @Schema(description = "语音提示")
    private String voicePrompt;

    @Schema(description = "文件路径")
    private String filePath;

    @Schema(description = "关联信标ID列表")
    private String bIds;

    @Schema(description = "是否大屏展示")
    private Boolean isScreenShow;

    @Schema(description = "备注")
    private String remarks;

}
