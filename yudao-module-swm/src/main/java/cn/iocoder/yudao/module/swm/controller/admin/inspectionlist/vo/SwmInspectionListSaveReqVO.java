package cn.iocoder.yudao.module.swm.controller.admin.inspectionlist.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;

@Schema(name = "巡检单新增/修改 Request VO")
@Data
public class SwmInspectionListSaveReqVO {
    @Schema(description = "主键") private String id;
    @Schema(description = "巡检计划ID") private String planId;
    @NotBlank(message = "计划名称不能为空") @Schema(description = "计划名称", requiredMode = Schema.RequiredMode.REQUIRED) private String planName;
    @Schema(description = "巡检类型") private String inspectionType;
    @Schema(description = "巡检人ID") private String inspectorId;
    @Schema(description = "开始时间") private LocalDateTime startTime;
    @Schema(description = "结束时间") private LocalDateTime endTime;
    @Schema(description = "附件路径") private String attachmentPath;
    @Schema(description = "巡检单状态") private String inspectionListStatus;
}
