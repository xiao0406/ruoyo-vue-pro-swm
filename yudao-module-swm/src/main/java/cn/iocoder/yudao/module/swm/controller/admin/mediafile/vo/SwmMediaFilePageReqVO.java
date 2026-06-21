package cn.iocoder.yudao.module.swm.controller.admin.mediafile.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "媒体文件分页查询 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class SwmMediaFilePageReqVO extends PageParam {

    @Schema(description = "文件名称")
    private String fileName;

    @Schema(description = "文件类型")
    private String fileType;

    @Schema(description = "业务类型")
    private String businessType;

    @Schema(description = "上传人")
    private String uploadBy;

}
