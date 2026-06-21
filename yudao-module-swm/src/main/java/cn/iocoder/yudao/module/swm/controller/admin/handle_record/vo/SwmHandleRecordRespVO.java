package cn.iocoder.yudao.module.swm.controller.admin.handle_record.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "处置记录 Response VO")
@Data
public class SwmHandleRecordRespVO {

    @Schema(description = "主键")
    private String id;

    @Schema(description = "处置记录名称")
    private String recordName;

    @Schema(description = "预警ID")
    private String warningId;

    @Schema(description = "预警记录")
    private String warningRecord;

    @Schema(description = "报警时间")
    private LocalDateTime alarmTime;

    @Schema(description = "处置人")
    private String handler;

    @Schema(description = "处置时间")
    private LocalDateTime handleTime;

    @Schema(description = "处置过程")
    private String handleProcess;

    @Schema(description = "处置状态")
    private String handleStatus;

    @Schema(description = "附件")
    private String attachment;

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
