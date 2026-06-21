package cn.iocoder.yudao.module.swm.controller.admin.inspectionplan.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.LocalDateTime;

@Schema(name = "巡检计划 Response VO")
@Data
public class SwmInspectionPlanRespVO {
    @Schema(description = "主键") private String id;
    @Schema(description = "巡检计划编号") private String planCode;
    @Schema(description = "计划名称") private String planName;
    @Schema(description = "巡检频次（天数）") private Integer frequencyDays;
    @Schema(description = "巡检类型") private String inspectionType;
    @Schema(description = "巡检负责人ID") private String responsiblePersonId;
    @Schema(description = "巡检负责人名称") private String responsiblePerson;
    @Schema(description = "首次巡检时间") private LocalDateTime firstInspectionTime;
    @Schema(description = "关联危险源ID") private String hazardSourceId;
    @Schema(description = "关联危险源名称") private String hazardSourceName;
    @Schema(description = "巡检状态") private String planStatus;
    @Schema(description = "创建时间") private LocalDateTime createTime;
    @Schema(description = "更新时间") private LocalDateTime updateTime;
}
