package cn.iocoder.yudao.module.swm.controller.admin.area.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "区域管理 Response VO")
@Data
public class SwmAreaRespVO {

    @Schema(description = "主键")
    private String id;

    @Schema(description = "区域名称")
    private String areaName;

    @Schema(description = "区域类型")
    private String areaType;

    @Schema(description = "区域颜色")
    private String areaColor;

    @Schema(description = "车间ID")
    private String workShop;

    @Schema(description = "语音提示")
    private String voicePrompt;

    @Schema(description = "文件路径")
    private String filePath;

    @Schema(description = "关联信标ID列表")
    private String bIds;

    @Schema(description = "是否大屏展示")
    private Boolean isScreenShow;

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
