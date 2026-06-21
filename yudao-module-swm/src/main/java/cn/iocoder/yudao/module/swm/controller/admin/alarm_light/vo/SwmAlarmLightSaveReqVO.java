package cn.iocoder.yudao.module.swm.controller.admin.alarm_light.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;

@Schema(description = "报警灯新增/修改 Request VO")
@Data
public class SwmAlarmLightSaveReqVO {

    @Schema(description = "报警灯编号")
    private String id;

    @Schema(description = "报警灯名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "报警灯名称不能为空")
    private String lightName;

    @Schema(description = "SN编码")
    private String snCode;

    @Schema(description = "是否启用报警")
    private String enableAlarm;

}
