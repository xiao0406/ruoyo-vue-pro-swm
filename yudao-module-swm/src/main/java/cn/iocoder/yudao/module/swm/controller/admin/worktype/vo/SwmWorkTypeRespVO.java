package cn.iocoder.yudao.module.swm.controller.admin.worktype.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "工种管理 Response VO")
@Data
public class SwmWorkTypeRespVO {

    @Schema(description = "主键")
    private String id;

    @Schema(description = "工种名称")
    private String workType;

    @Schema(description = "工种编码")
    private String workTypeCode;

    @Schema(description = "描述")
    private String description;

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
