package cn.iocoder.yudao.module.swm.controller.admin.mediafile.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;

@Schema(description = "媒体文件新增/修改 Request VO")
@Data
public class SwmMediaFileSaveReqVO {

    @Schema(description = "媒体文件编号")
    private String id;

    @Schema(description = "文件名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "文件名称不能为空")
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

    @Schema(description = "标签")
    private String tags;

    @Schema(description = "业务类型")
    private String businessType;

    @Schema(description = "业务ID")
    private String businessId;

}
