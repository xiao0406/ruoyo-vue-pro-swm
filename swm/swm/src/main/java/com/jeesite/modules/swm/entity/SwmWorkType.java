package com.jeesite.modules.swm.entity;

import com.jeesite.common.entity.BaseEntity;
import com.jeesite.common.entity.DataEntity;
import com.jeesite.common.mybatis.annotation.Column;
import com.jeesite.common.mybatis.annotation.Table;
import com.jeesite.common.mybatis.mapper.query.QueryType;

/**
 * 工种表实体类
 * 
 * @author Shawn
 * @version 2025-07-04
 */
@Table(name = "swm_work_type", alias = "a", columns = {
        @Column(name = "id", attrName = "id", label = "主键ID", isPK = true),
        @Column(name = "work_type", attrName = "workType", label = "工种名称", queryType = QueryType.LIKE),
        @Column(name = "work_type_code", attrName = "workTypeCode", label = "工种编码", queryType = QueryType.LIKE),
        @Column(name = "description", attrName = "description", label = "工种描述"),
        @Column(includeEntity = DataEntity.class),
        @Column(includeEntity= BaseEntity.class),
}, orderBy = "a.work_type_code ASC")
public class SwmWorkType extends DataEntity<SwmWorkType> {

    private static final long serialVersionUID = 1L;

    private String workType; // 工种名称
    private String workTypeCode; // 工种编码
    private String description; // 工种描述

    public SwmWorkType() {
        this(null);
    }

    public SwmWorkType(String id) {
        super(id);
    }

    public String getWorkType() {
        return workType;
    }

    public void setWorkType(String workType) {
        this.workType = workType;
    }

    public String getWorkTypeCode() {
        return workTypeCode;
    }

    public void setWorkTypeCode(String workTypeCode) {
        this.workTypeCode = workTypeCode;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
