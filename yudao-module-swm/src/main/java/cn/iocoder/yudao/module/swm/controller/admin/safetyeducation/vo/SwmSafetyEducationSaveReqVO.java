package cn.iocoder.yudao.module.swm.controller.admin.safetyeducation.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;

@Schema(name = "安全教育新增/修改 Request VO")
@Data
public class SwmSafetyEducationSaveReqVO {
    @Schema(description = "主键") private String id;
    @NotBlank(message = "主题不能为空") @Schema(description = "主题", requiredMode = Schema.RequiredMode.REQUIRED) private String theme;
    @Schema(description = "内容描述") private String contentDescription;
    @Schema(description = "安全教育类型") private String safetyEducationType;
    @Schema(description = "开始时间") private LocalDateTime startTime;
    @Schema(description = "参与对象") private String participants;
    @Schema(description = "参与对象名称") private String participantsName;
    @Schema(description = "状态") private String safetyStatus;
    @Schema(description = "参与类型") private String participationType;
    @Schema(description = "附件URL") private String attachmentUrl;
}
