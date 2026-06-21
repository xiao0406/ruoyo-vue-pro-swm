package cn.iocoder.yudao.module.swm.controller.admin.warning.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;

@Schema(description = "预警管理新增/修改 Request VO")
@Data
public class SwmWarningManagementSaveReqVO {

    @Schema(description = "预警管理编号")
    private String id;

    @Schema(description = "人员姓名")
    private String personName;

    @Schema(description = "预警性质", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "预警性质不能为空")
    private String warningType;

    @Schema(description = "预警内容", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "预警内容不能为空")
    private String warningContent;

    @Schema(description = "预警时间")
    private LocalDateTime warningTime;

    @Schema(description = "告警记录")
    private String alarmRecord;

    @Schema(description = "告警时间")
    private LocalDateTime alarmTime;

    @Schema(description = "触发原因")
    private String triggerReason;

    @Schema(description = "处理人")
    private String handler;

    @Schema(description = "处理时间")
    private LocalDateTime handleTime;

    @Schema(description = "处理过程")
    private String handleProcess;

    @Schema(description = "处置状态")
    private String handleStatus;

    @Schema(description = "附件")
    private String attachment;

    @Schema(description = "处置时长")
    private Long disposalDuration;

    @Schema(description = "设备编号")
    private String deviceId;

    @Schema(description = "ID卡号")
    private String idCard;

    @Schema(description = "前置告警")
    private String frontAlarm;

    @Schema(description = "告警类型")
    private String type;

    @Schema(description = "X坐标")
    private String x;

    @Schema(description = "Y坐标")
    private String y;

    @Schema(description = "危险源分类")
    private String hazardCategory;

    @Schema(description = "位置")
    private String location;

    @Schema(description = "区域")
    private String area;

}
