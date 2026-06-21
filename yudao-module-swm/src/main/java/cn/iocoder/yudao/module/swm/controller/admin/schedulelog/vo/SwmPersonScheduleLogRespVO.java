package cn.iocoder.yudao.module.swm.controller.admin.schedulelog.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "排班日志 Response VO")
@Data
public class SwmPersonScheduleLogRespVO {

    @Schema(description = "主键")
    private String id;

    @Schema(description = "操作人")
    private String operateUser;

    @Schema(description = "操作时间")
    private LocalDateTime operateTime;

    @Schema(description = "目标班次")
    private String targetClasses;

    @Schema(description = "人员ID")
    private String personId;

    @Schema(description = "操作描述")
    private String operateDesc;

    @Schema(description = "变更前班次")
    private String beforeClasses;

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
