package cn.iocoder.yudao.module.swm.controller.admin.sitemap.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;

@Schema(description = "站点地图新增/修改 Request VO")
@Data
public class SwmSiteMapManagementSaveReqVO {

    @Schema(description = "站点地图编号")
    private String id;

    @Schema(description = "地图名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "地图名称不能为空")
    private String mapName;

    @Schema(description = "项目ID")
    private String projectId;

    @Schema(description = "地图大小")
    private String mapSize;

    @Schema(description = "比例尺")
    private String scale;

    @Schema(description = "文件路径")
    private String filePath;

    @Schema(description = "绘图像素X")
    private Integer drawingPixelX;

    @Schema(description = "绘图像素Y")
    private Integer drawingPixelY;

    @Schema(description = "站点坐标X(m)")
    private BigDecimal siteCoordinateXM;

    @Schema(description = "站点坐标Y(m)")
    private BigDecimal siteCoordinateYM;

    @Schema(description = "父节点ID")
    private String parentId;

    @Schema(description = "地图类型")
    private String mapType;

    @Schema(description = "原点像素X")
    private Integer originPixelX;

    @Schema(description = "原点像素Y")
    private Integer originPixelY;

    @Schema(description = "排序")
    private Integer sortOrder;

    @Schema(description = "是否3D")
    private Integer is3d;

}
