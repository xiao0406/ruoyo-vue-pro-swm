package cn.iocoder.yudao.module.swm.controller.admin.beaconcolor.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;

@Schema(description = "信标颜色配置新增/修改 Request VO")
@Data
public class SwmBeaconColorConfigSaveReqVO {

    @Schema(description = "信标颜色配置编号")
    private String id;

    @Schema(description = "名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "名称不能为空")
    private String name;

    @Schema(description = "颜色")
    private String color;

}
