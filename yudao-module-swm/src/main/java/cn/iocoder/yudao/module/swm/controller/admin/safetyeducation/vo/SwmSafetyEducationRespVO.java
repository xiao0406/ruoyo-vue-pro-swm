package cn.iocoder.yudao.module.swm.controller.admin.safetyeducation.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.LocalDateTime;

@Schema(name = "安全教育 Response VO")
@Data
public class SwmSafetyEducationRespVO {
    @Schema(description = "主键") private String id;
    @Schema(description = "主题") private String theme;
    @Schema(description = "内容描述") private String contentDescription;
    @Schema(description = "安全教育类型") private String safetyEducationType;
    @Schema(description = "开始时间") private LocalDateTime startTime;
    @Schema(description = "参与对象") private String participants;
    @Schema(description = "参与对象名称") private String participantsName;
    @Schema(description = "状态") private String safetyStatus;
    @Schema(description = "参与类型") private String participationType;
    @Schema(description = "附件URL") private String attachmentUrl;
    @Schema(description = "创建时间") private LocalDateTime createTime;
    @Schema(description = "更新时间") private LocalDateTime updateTime;
}
