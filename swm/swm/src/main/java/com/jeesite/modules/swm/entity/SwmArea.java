/**
 * 区域管理实体类
 * @author Shawn
 * @version 2025-06-22
 */
package com.jeesite.modules.swm.entity;

import com.jeesite.common.entity.DataEntity;
import com.jeesite.common.mybatis.annotation.Column;
import com.jeesite.common.mybatis.annotation.Table;
import com.jeesite.common.mybatis.mapper.query.QueryType;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;

/**
 * 区域管理实体类
 */
@Table(name = "swm_area", alias = "a", columns = {
        @Column(name = "id", attrName = "id", label = "主键ID", isPK = true),
        @Column(name = "area_name", attrName = "areaName", label = "区域名称", queryType = QueryType.LIKE),
        @Column(name = "work_shop", attrName = "workShop", label = "车间ID"),
        @Column(includeEntity = DataEntity.class)
}, orderBy = "a.update_date DESC")
public class SwmArea extends DataEntity<SwmArea> {

    private static final long serialVersionUID = 1L;

    private String areaName; // 区域名称
    private String workShop; // 车间ID

    public SwmArea() {
        this(null);
    }

    public SwmArea(String id) {
        super(id);
    }

    @NotBlank(message = "区域名称不能为空")
    @Length(min = 0, max = 100, message = "区域名称长度不能超过 100 个字符")
    public String getAreaName() {
        return areaName;
    }

    public void setAreaName(String areaName) {
        this.areaName = areaName;
    }

    @Length(min = 0, max = 64, message = "车间ID长度不能超过 64 个字符")
    public String getWorkShop() {
        return workShop;
    }

    public void setWorkShop(String workShop) {
        this.workShop = workShop;
    }
}