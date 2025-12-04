package com.jeesite.modules.swm.entity;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

import com.jeesite.common.entity.BaseEntity;
import com.jeesite.common.entity.DataEntity;
import com.jeesite.common.mybatis.annotation.Column;
import com.jeesite.common.mybatis.annotation.Table;
import com.jeesite.common.mybatis.mapper.query.QueryType;

/**
 * 信标颜色配置表实体类
 * 
 * @author zwf
 * @version 2025-05-23
 */
@Table(name = "swm_beacon_color_config", alias = "a", label = "信标颜色配置表", columns = {
        @Column(name = "id", attrName = "id", label = "主键ID", isPK = true),
        @Column(name = "name", attrName = "name", label = "名称", queryType = QueryType.LIKE),
        @Column(name = "color", attrName = "color", label = "颜色"),
        @Column(includeEntity = DataEntity.class),
        @Column(includeEntity= BaseEntity.class),
}, orderBy = "a.update_date DESC")
public class SwmBeaconColorConfig extends DataEntity<SwmBeaconColorConfig> {
    
    private static final long serialVersionUID = 1L;

    private String name;        // 名称
    private String color;       // 颜色
    
    public SwmBeaconColorConfig() {
        this(null);
    }
    
    public SwmBeaconColorConfig(String id) {
        super(id);
    }
    
    @NotBlank(message = "名称不能为空")
    @Size(min = 0, max = 100, message = "名称长度不能超过 100 个字符")
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    @Size(min = 0, max = 50, message = "颜色长度不能超过 50 个字符")
    public String getColor() {
        return color;
    }
    
    public void setColor(String color) {
        this.color = color;
    }
} 