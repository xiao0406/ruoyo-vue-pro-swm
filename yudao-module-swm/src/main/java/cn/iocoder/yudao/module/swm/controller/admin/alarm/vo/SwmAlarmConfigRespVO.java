package cn.iocoder.yudao.module.swm.controller.admin.alarm.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "告警配置 Response VO")
@Data
public class SwmAlarmConfigRespVO {

    @Schema(description = "主键")
    private String id;

    @Schema(description = "报警名称")
    private String alarmName;

    @Schema(description = "报警唯一标识key")
    private String alarmKey;

    @Schema(description = "是否报警")
    private Integer enableAlarm;

    @Schema(description = "是否弹窗确认")
    private Integer needConfirm;

    @Schema(description = "弹窗位置")
    private String dialogPosition;

    @Schema(description = "是否推送中建通")
    private Integer isSendZjt;

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
