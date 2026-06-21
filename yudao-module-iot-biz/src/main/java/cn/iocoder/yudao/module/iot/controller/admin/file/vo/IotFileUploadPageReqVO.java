package cn.iocoder.yudao.module.iot.controller.admin.file.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Schema(name = "IoT文件上传分页查询 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class IotFileUploadPageReqVO extends PageParam {

    @Schema(description = "文件名")
    private String fileName;
    @Schema(description = "文件类型")
    private String fileType;
    @Schema(description = "设备ID")
    private String deviceId;
}
