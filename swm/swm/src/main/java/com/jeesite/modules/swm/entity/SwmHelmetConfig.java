package com.jeesite.modules.swm.entity;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

import com.jeesite.common.entity.BaseEntity;
import com.jeesite.common.entity.DataEntity;
import com.jeesite.common.mybatis.annotation.Column;
import com.jeesite.common.mybatis.annotation.Table;
import com.jeesite.common.mybatis.mapper.query.QueryType;

/**
 * 安全帽主配置表实体类
 * 
 * @author zwf
 * @version 2025-05-20
 */
@Table(name = "swm_helmet_config", alias = "a", label = "安全帽主配置表", columns = {
        @Column(name = "id", attrName = "id", label = "主键ID", isPK = true),
        @Column(name = "name", attrName = "name", label = "安全帽类型名称", queryType = QueryType.LIKE),
        @Column(name = "subitem_count", attrName = "subitemCount", label = "子项数量"),
        @Column(includeEntity = DataEntity.class),
        @Column(includeEntity= BaseEntity.class),
}, orderBy = "a.update_date DESC")
public class SwmHelmetConfig extends DataEntity<SwmHelmetConfig> {
    
    private static final long serialVersionUID = 1L;

    
    private String name;        // 安全帽类型名称
    private Integer subitemCount;  // 子项数量
    
    public SwmHelmetConfig() {
        this(null);
    }
    
    public SwmHelmetConfig(String id) {
        super(id);
    }
    
    @Override
    public String getId() {
        return super.getId();
    }
    
    @Override
    public void setId(String id) {
        super.setId(id);
    }
    
    @NotBlank(message = "安全帽类型名称不能为空")
    @Size(min = 0, max = 100, message = "安全帽类型名称长度不能超过 100 个字符")
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public Integer getSubitemCount() {
        return subitemCount;
    }
    
    public void setSubitemCount(Integer subitemCount) {
        this.subitemCount = subitemCount;
    }
} 