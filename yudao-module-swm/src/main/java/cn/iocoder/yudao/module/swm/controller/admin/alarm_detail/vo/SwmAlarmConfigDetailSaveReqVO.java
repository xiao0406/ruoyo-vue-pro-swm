package cn.iocoder.yudao.module.swm.controller.admin.alarm_detail.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;

@Schema(description = "告警配置详情新增/修改 Request VO")
@Data
public class SwmAlarmConfigDetailSaveReqVO {

    @Schema(description = "主键")
    private String id;

    @Schema(description = "角色")
    private String roles;

    @Schema(description = "用户")
    private String users;

    @Schema(description = "主表ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "主表ID不能为空")
    private String mainId;

}
