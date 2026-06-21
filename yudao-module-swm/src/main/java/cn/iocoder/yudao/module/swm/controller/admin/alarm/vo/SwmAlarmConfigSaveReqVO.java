package cn.iocoder.yudao.module.swm.controller.admin.alarm.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;

@Schema(description = "告警配置新增/修改 Request VO")
@Data
public class SwmAlarmConfigSaveReqVO {

    @Schema(description = "告警配置编号")
    private String id;

    @Schema(description = "报警名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "报警名称不能为空")
    private String alarmName;

    @Schema(description = "报警唯一标识key", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "报警唯一标识key不能为空")
    private String alarmKey;

    @Schema(description = "是否报警")
    private Integer enableAlarm;

    @Schema(description = "是否弹窗确认")
    private Integer needConfirm;

    @Schema(description = "弹窗位置")
    private String dialogPosition;

    @Schema(description = "是否推送中建通")
    private Integer isSendZjt;

}
