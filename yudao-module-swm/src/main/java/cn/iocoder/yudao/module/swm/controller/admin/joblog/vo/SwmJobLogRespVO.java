package cn.iocoder.yudao.module.swm.controller.admin.joblog.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "任务日志 Response VO")
@Data
public class SwmJobLogRespVO {

    @Schema(description = "主键")
    private String id;

    @Schema(description = "任务名称")
    private String jobName;

    @Schema(description = "任务参数")
    private String jobParam;

    @Schema(description = "开始时间")
    private LocalDateTime startTime;

    @Schema(description = "结束时间")
    private LocalDateTime endTime;

    @Schema(description = "耗时(ms)")
    private Long duration;

    @Schema(description = "执行状态")
    private String executeStatus;

    @Schema(description = "异常信息")
    private String exceptionInfo;

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
