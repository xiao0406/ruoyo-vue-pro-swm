package com.jeesite.modules.swm.entity;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

import com.jeesite.common.entity.BaseEntity;
import com.jeesite.common.entity.DataEntity;
import com.jeesite.common.mybatis.annotation.Column;
import com.jeesite.common.mybatis.annotation.Table;
import com.jeesite.common.mybatis.mapper.query.QueryType;

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
    
    @NotBlank(message = "文件路径不能为空")
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
} 