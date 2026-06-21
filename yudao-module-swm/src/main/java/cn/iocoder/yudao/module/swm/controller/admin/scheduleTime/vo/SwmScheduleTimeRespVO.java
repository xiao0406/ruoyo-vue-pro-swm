package cn.iocoder.yudao.module.swm.controller.admin.scheduleTime.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "排班时间 Response VO")
@Data
public class SwmScheduleTimeRespVO {

    @Schema(description = "主键")
    private String id;

    @Schema(description = "班次类型")
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

    @Schema(description = "租户编号")
    private Long tenantId;

}
