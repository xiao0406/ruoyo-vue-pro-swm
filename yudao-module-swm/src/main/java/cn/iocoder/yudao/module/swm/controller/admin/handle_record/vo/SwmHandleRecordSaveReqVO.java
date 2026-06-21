package cn.iocoder.yudao.module.swm.controller.admin.handle_record.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;

@Schema(description = "处置记录新增/修改 Request VO")
@Data
public class SwmHandleRecordSaveReqVO {

    @Schema(description = "处置记录编号")
    private String id;

    @Schema(description = "处置记录名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "处置记录名称不能为空")
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

}
