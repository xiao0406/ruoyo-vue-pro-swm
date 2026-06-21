package cn.iocoder.yudao.module.swm.dal.dataobject;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.math.BigDecimal;

/**
 * 站点地图管理 DO
 * 表: swm_site_map_management
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@TableName("swm_site_map_management")
public class SwmSiteMapManagementDO extends SwmBaseDO {
    private String mapName;
    private String projectId;
    private String mapSize;
    private String scale;
    private String filePath;
    private Integer drawingPixelX;
    private Integer drawingPixelY;
    private BigDecimal siteCoordinateXM;
    private BigDecimal siteCoordinateYM;
    private String parentId;
    private String mapType;
    private Integer originPixelX;
    private Integer originPixelY;
    private Integer sortOrder;
    private Integer is3d;

    @TableField(exist = false)
    private boolean hasChildren;
}
