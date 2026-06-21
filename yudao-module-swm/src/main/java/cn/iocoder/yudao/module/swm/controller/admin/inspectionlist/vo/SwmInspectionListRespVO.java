package cn.iocoder.yudao.module.swm.controller.admin.inspectionlist.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.LocalDateTime;

@Schema(name = "巡检单 Response VO")
@Data
public class SwmInspectionListRespVO {
    @Schema(description = "主键") private String id;
    @Schema(description = "巡检计划ID") private String planId;
    @Schema(description = "计划名称") private String planName;
    @Schema(description = "巡检类型") private String inspectionType;
    @Schema(description = "巡检人ID") private String inspectorId;
    @Schema(description = "巡检人名称") private String inspector;
    @Schema(description = "开始时间") private LocalDateTime startTime;
    @Schema(description = "结束时间") private LocalDateTime endTime;
    @Schema(description = "附件路径") private String attachmentPath;
    @Schema(description = "巡检单状态") private String inspectionListStatus;
    @Schema(description = "创建时间") private LocalDateTime createTime;
    @Schema(description = "更新时间") private LocalDateTime updateTime;
}
