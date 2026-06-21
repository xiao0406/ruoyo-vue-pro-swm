package cn.iocoder.yudao.module.swm.controller.admin.mediafile.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "媒体文件 Response VO")
@Data
public class SwmMediaFileRespVO {

    @Schema(description = "主键")
    private String id;

    @Schema(description = "文件名称")
    private String fileName;

    @Schema(description = "文件类型")
    private String fileType;

    @Schema(description = "文件大小")
    private Long fileSize;

    @Schema(description = "文件URL")
    private String fileUrl;

    @Schema(description = "缩略图URL")
    private String thumbnailUrl;

    @Schema(description = "上传人")
    private String uploadBy;

    @Schema(description = "上传人姓名")
    private String uploadByName;

    @Schema(description = "上传时间")
    private LocalDateTime uploadTime;

    @Schema(description = "标签")
    private String tags;

    @Schema(description = "业务类型")
    private String businessType;

    @Schema(description = "业务ID")
    private String businessId;

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
