package cn.iocoder.yudao.module.swm.controller.admin.warning.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "预警管理 Response VO")
@Data
public class SwmWarningManagementRespVO {

    @Schema(description = "预警管理编号")
    private String id;

    @Schema(description = "人员姓名")
    private String personName;

    @Schema(description = "预警性质")
    private String warningType;

    @Schema(description = "预警内容")
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

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    @Schema(description = "创建者")
    private String creator;

    @Schema(description = "更新者")
    private String updater;

    @Schema(description = "状态")
    private String status;

    @Schema(description = "备注")
    private String remarks;

}
