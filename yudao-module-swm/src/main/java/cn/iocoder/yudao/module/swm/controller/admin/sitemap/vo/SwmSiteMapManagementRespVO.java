package cn.iocoder.yudao.module.swm.controller.admin.sitemap.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "站点地图 Response VO")
@Data
public class SwmSiteMapManagementRespVO {

    @Schema(description = "主键")
    private String id;

    @Schema(description = "地图名称")
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
