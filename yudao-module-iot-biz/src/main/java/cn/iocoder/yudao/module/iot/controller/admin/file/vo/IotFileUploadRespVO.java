package cn.iocoder.yudao.module.iot.controller.admin.file.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(name = "IoT文件上传 Response VO")
@Data
public class IotFileUploadRespVO {

    @Schema(description = "主键")
    private String id;
    @Schema(description = "文件名")
    private String fileName;
    @Schema(description = "文件URL")
    private String fileUrl;
    @Schema(description = "文件类型")
    private String fileType;
    @Schema(description = "文件大小")
    private Long fileSize;
    @Schema(description = "设备ID")
    private String deviceId;
    @Schema(description = "人员ID")
    private String personId;
    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
