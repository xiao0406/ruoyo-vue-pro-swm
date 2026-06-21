package cn.iocoder.yudao.module.swm.controller.admin.sitemap.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "站点地图分页查询 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class SwmSiteMapManagementPageReqVO extends PageParam {

    @Schema(description = "地图名称")
    private String mapName;

    @Schema(description = "项目ID")
    private String projectId;

    @Schema(description = "地图类型")
    private String mapType;

    @Schema(description = "父节点ID")
    private String parentId;

}
