package com.jeesite.modules.swm.entity;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

import com.jeesite.common.entity.BaseEntity;
import com.jeesite.common.entity.DataEntity;
import com.jeesite.common.mybatis.annotation.Column;
import com.jeesite.common.mybatis.annotation.Table;
import com.jeesite.common.mybatis.mapper.query.QueryType;

/**
 * 安全帽子项颜色表实体类
 * 
 * @author zwf
 * @version 2025-05-20
 */
@Table(name = "swm_helmet_subitem", alias = "a", label = "安全帽子项颜色表", columns = {
        @Column(name = "id", attrName = "id", label = "主键ID", isPK = true),
        @Column(name = "parent_id", attrName = "parentId", label = "关联主表ID"),
        @Column(name = "subitem_name", attrName = "subitemName", label = "子项名称", queryType = QueryType.LIKE),
        @Column(name = "color", attrName = "color", label = "颜色代码"),
        @Column(name = "key", attrName = "key", label = "关键字段"),
        @Column(includeEntity = DataEntity.class),
        @Column(includeEntity= BaseEntity.class),
}, orderBy = "a.update_date DESC")
public class SwmHelmetSubitem extends DataEntity<SwmHelmetSubitem> {

    private static final long serialVersionUID = 1L;

    private String parentId; // 关联主表ID
    private String subitemName; // 子项名称
    private String color; // 颜色代码
    private String key; // 关键字段（用于保存车间ID等）

    // 非数据库字段
    private SwmHelmetConfig helmetConfig; // 关联的安全帽配置

    public SwmHelmetSubitem() {
        this(null);
    }

    public SwmHelmetSubitem(String id) {
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

    @NotBlank(message = "关联主表ID不能为空")
    @Size(min = 0, max = 64, message = "关联主表ID长度不能超过 64 个字符")
    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    @NotBlank(message = "子项名称不能为空")
    @Size(min = 0, max = 100, message = "子项名称长度不能超过 100 个字符")
    public String getSubitemName() {
        return subitemName;
    }

    public void setSubitemName(String subitemName) {
        this.subitemName = subitemName;
    }

    @NotBlank(message = "颜色代码不能为空")
    @Size(min = 0, max = 50, message = "颜色代码长度不能超过 50 个字符")
    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    @Size(min = 0, max = 64, message = "关键字段长度不能超过 64 个字符")
    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public SwmHelmetConfig getHelmetConfig() {
        return helmetConfig;
    }

    public void setHelmetConfig(SwmHelmetConfig helmetConfig) {
        this.helmetConfig = helmetConfig;
    }
}