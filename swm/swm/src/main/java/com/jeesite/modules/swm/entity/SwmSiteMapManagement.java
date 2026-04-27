package com.jeesite.modules.swm.entity;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

import com.jeesite.common.entity.BaseEntity;
import com.jeesite.common.entity.DataEntity;
import com.jeesite.common.mybatis.annotation.Column;
import com.jeesite.common.mybatis.annotation.Table;
import com.jeesite.common.mybatis.mapper.query.QueryType;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * 场地底图管理表实体类
 * 
 * @author zwf
 * @version 2025-05-30
 */
@Table(name = "swm_site_map_management", alias = "a", label = "场地底图管理表", columns = {
        @Column(name = "id", attrName = "id", label = "主键ID", isPK = true),
        @Column(name = "map_name", attrName = "mapName", label = "底图名称", queryType = QueryType.LIKE),
        @Column(name = "project_id", attrName = "projectId", label = "所属项目"),
        @Column(name = "map_size", attrName = "mapSize", label = "底图尺寸"),
        @Column(name = "scale", attrName = "scale", label = "比例尺"),
        @Column(name = "file_path", attrName = "filePath", label = "文件路径"),
        @Column(name = "drawing_pixel_x", attrName = "drawingPixelX", label = "图纸X像素坐标"),
        @Column(name = "drawing_pixel_y", attrName = "drawingPixelY", label = "图纸Y像素坐标"),
        @Column(name = "site_coordinate_x_m", attrName = "siteCoordinateXM", label = "场地X坐标(米)"),
        @Column(name = "site_coordinate_y_m", attrName = "siteCoordinateYM", label = "场地Y坐标(米)"),
        @Column(name = "parent_id", attrName = "parentId", label = "父节点ID"),
        @Column(name = "map_type", attrName = "mapType", label = "图纸类型"),
        @Column(name = "origin_pixel_x", attrName = "originPixelX", label = "起始X像素偏移"),
        @Column(name = "origin_pixel_y", attrName = "originPixelY", label = "起始Y像素偏移"),
        @Column(name = "sort_order", attrName = "sortOrder", label = "排序值"),
		@Column(name = "is3d", attrName = "is3d", label = "是否是3D"),
        @Column(includeEntity = DataEntity.class),
        @Column(includeEntity= BaseEntity.class),
}, orderBy = "a.update_date DESC")
public class SwmSiteMapManagement extends DataEntity<SwmSiteMapManagement> {
    
    private static final long serialVersionUID = 1L;

    private String mapName;     // 底图名称
    private String projectId;   // 所属项目
    private String mapSize;     // 底图尺寸
    private String scale;       // 比例尺
    private String filePath;    // 文件路径
    private Integer drawingPixelX;  // 图纸X像素坐标
    private Integer drawingPixelY;  // 图纸Y像素坐标
    private java.math.BigDecimal siteCoordinateXM;  // 场地X坐标(米)
    private java.math.BigDecimal siteCoordinateYM;  // 场地Y坐标(米)

    // ===== 树形结构新增字段 @author Shawn @date 2026-04-02 =====
    private String parentId;        // 父节点ID，顶层填'0'
    private String mapType;         // 图纸类型：factory=厂区，building=建筑，floor=楼层
    private Integer originPixelX;   // 图纸起始X像素偏移量
    private Integer originPixelY;   // 图纸起始Y像素偏移量
    private Integer sortOrder;      // 排序值，越小越靠前

    // 辅助字段，不存数据库，用于告诉前端是否有子节点
    private transient Boolean hasChildren;
    
    private Integer is3d;  // 是否是3D


    public Integer getIs3d() {
        return is3d;
    }

    public void setIs3d(Integer is3d) {
        this.is3d = is3d;
    }
    public SwmSiteMapManagement() {
        this(null);
    }
    
    public SwmSiteMapManagement(String id) {
        super(id);
    }
    
    @NotBlank(message = "底图名称不能为空")
    @Size(min = 0, max = 100, message = "底图名称长度不能超过 100 个字符")
    public String getMapName() {
        return mapName;
    }
    
    public void setMapName(String mapName) {
        this.mapName = mapName;
    }
    
    @Size(min = 0, max = 64, message = "所属项目长度不能超过 64 个字符")
    public String getProjectId() {
        return projectId;
    }
    
    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }
    
    @Size(min = 0, max = 50, message = "底图尺寸长度不能超过 50 个字符")
    public String getMapSize() {
        return mapSize;
    }
    
    public void setMapSize(String mapSize) {
        this.mapSize = mapSize;
    }
    
    @Size(min = 0, max = 20, message = "比例尺长度不能超过 20 个字符")
    public String getScale() {
        return scale;
    }
    
    public void setScale(String scale) {
        this.scale = scale;
    }
    
    // 厂区/建筑节点可能没有图纸文件，不再强制非空
    public String getFilePath() {
        return filePath;
    }
    
    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }
    
    public Integer getDrawingPixelX() {
        return drawingPixelX;
    }
    
    public void setDrawingPixelX(Integer drawingPixelX) {
        this.drawingPixelX = drawingPixelX;
    }
    
    public Integer getDrawingPixelY() {
        return drawingPixelY;
    }
    
    public void setDrawingPixelY(Integer drawingPixelY) {
        this.drawingPixelY = drawingPixelY;
    }
    
    public java.math.BigDecimal getSiteCoordinateXM() {
        return siteCoordinateXM;
    }
    
    public void setSiteCoordinateXM(java.math.BigDecimal siteCoordinateXM) {
        this.siteCoordinateXM = siteCoordinateXM;
    }
    
    public java.math.BigDecimal getSiteCoordinateYM() {
        return siteCoordinateYM;
    }
    
    public void setSiteCoordinateYM(java.math.BigDecimal siteCoordinateYM) {
        this.siteCoordinateYM = siteCoordinateYM;
    }

    // ===== 树形结构新增字段 getter/setter @author Shawn @date 2026-04-02 =====

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    @Size(min = 0, max = 20, message = "图纸类型长度不能超过 20 个字符")
    public String getMapType() {
        return mapType;
    }

    public void setMapType(String mapType) {
        this.mapType = mapType;
    }

    public Integer getOriginPixelX() {
        return originPixelX;
    }

    public void setOriginPixelX(Integer originPixelX) {
        this.originPixelX = originPixelX;
    }

    public Integer getOriginPixelY() {
        return originPixelY;
    }

    public void setOriginPixelY(Integer originPixelY) {
        this.originPixelY = originPixelY;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

    public Boolean getHasChildren() {
        return hasChildren;
    }

    public void setHasChildren(Boolean hasChildren) {
        this.hasChildren = hasChildren;
    }
}