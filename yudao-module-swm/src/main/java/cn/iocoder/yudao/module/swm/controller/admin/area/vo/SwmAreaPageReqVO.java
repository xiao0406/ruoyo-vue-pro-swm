package cn.iocoder.yudao.module.swm.controller.admin.area.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "区域管理分页查询 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class SwmAreaPageReqVO extends PageParam {

    @Schema(description = "区域名称")
    private String areaName;

    @Schema(description = "区域类型")
    private String areaType;

    @Schema(description = "车间ID")
    private String workShop;

    @Schema(description = "是否大屏展示")
    private Boolean isScreenShow;

}
