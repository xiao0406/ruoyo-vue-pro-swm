package cn.iocoder.yudao.module.swm.controller.admin.hazard.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "危险源分页查询 VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class SwmHazardSourcePageReqVO extends PageParam {

    @Schema(description = "危险源名称")
    private String hazardName;

    @Schema(description = "危险源类别")
    private String hazardCategory;

    @Schema(description = "危险源状态")
    private String hazardStatus;

    @Schema(description = "所在位置")
    private String location;

    @Schema(description = "责任人")
    private String responsiblePerson;
}
