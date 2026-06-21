package cn.iocoder.yudao.module.swm.controller.admin.scheduleTime.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;

@Schema(description = "排班时间新增/修改 Request VO")
@Data
public class SwmScheduleTimeSaveReqVO {

    @Schema(description = "排班时间编号")
    private String id;

    @Schema(description = "班次类型", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "班次类型不能为空")
    private String shiftType;

    @Schema(description = "上班时间")
    private String startTime;

    @Schema(description = "午休结束时间")
    private String noonEndTime;

    @Schema(description = "下午开始时间")
    private String afterStartTime;

    @Schema(description = "下班时间")
    private String endTime;

    @Schema(description = "休息时长")
    private Double restTime;

    @Schema(description = "休息天数")
    private String restDays;

    @Schema(description = "备注")
    private String remarks;

}
