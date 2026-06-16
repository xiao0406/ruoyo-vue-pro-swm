package com.jeesite.modules.swm.entity;

import com.jeesite.common.entity.BaseEntity;
import com.jeesite.common.entity.DataEntity;
import com.jeesite.common.mybatis.annotation.Column;
import com.jeesite.common.mybatis.annotation.Table;
import com.jeesite.common.mybatis.mapper.query.QueryType;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

/**
 * 人员工作区域绑定实体。
 */
@Table(name = "swm_person_work_area", alias = "a", columns = {
        @Column(name = "id", attrName = "id", label = "主键", isPK = true),
        @Column(name = "identity_card", attrName = "identityCard", label = "身份证号码", queryType = QueryType.LIKE),
        @Column(name = "person_name", attrName = "personName", label = "人员姓名", queryType = QueryType.LIKE),
        @Column(name = "area_id", attrName = "areaId", label = "区域ID"),
        @Column(name = "area_name", attrName = "areaName", label = "区域名称", queryType = QueryType.LIKE),
        @Column(includeEntity = DataEntity.class),
        @Column(includeEntity = BaseEntity.class),
}, orderBy = "a.update_date DESC")
@Data
public class SwmPersonWorkArea extends DataEntity<SwmPersonWorkArea> {

    private static final long serialVersionUID = 1L;

    private String identityCard;
    private String personId;
    private String personName;
    private String areaId;
    private String areaName;
    private String identityCards;
    private String areaIds;
    private String bindIds;
    private String company;
    private String department;
    private String prodLine;
    private String team;
    private String jobType;

    public SwmPersonWorkArea() {
        this(null);
    }

    public SwmPersonWorkArea(String id) {
        super(id);
    }

    @Length(min = 0, max = 32, message = "身份证号码长度不能超过32个字符")
    public String getIdentityCard() {
        return identityCard;
    }

    public void setIdentityCard(String identityCard) {
        this.identityCard = identityCard;
    }

    public String getPersonId() {
        return personId;
    }

    public void setPersonId(String personId) {
        this.personId = personId;
    }

    @Length(min = 0, max = 100, message = "人员姓名长度不能超过100个字符")
    public String getPersonName() {
        return personName;
    }

    public void setPersonName(String personName) {
        this.personName = personName;
    }

    @Length(min = 0, max = 64, message = "区域ID长度不能超过64个字符")
    public String getAreaId() {
        return areaId;
    }

    public void setAreaId(String areaId) {
        this.areaId = areaId;
    }

    @Length(min = 0, max = 100, message = "区域名称长度不能超过100个字符")
    public String getAreaName() {
        return areaName;
    }

    public void setAreaName(String areaName) {
        this.areaName = areaName;
    }

    public String getIdentityCards() {
        return identityCards;
    }

    public void setIdentityCards(String identityCards) {
        this.identityCards = identityCards;
    }

    public String getAreaIds() {
        return areaIds;
    }

    public void setAreaIds(String areaIds) {
        this.areaIds = areaIds;
    }

    public String getBindIds() {
        return bindIds;
    }

    public void setBindIds(String bindIds) {
        this.bindIds = bindIds;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getProdLine() {
        return prodLine;
    }

    public void setProdLine(String prodLine) {
        this.prodLine = prodLine;
    }

    public String getTeam() {
        return team;
    }

    public void setTeam(String team) {
        this.team = team;
    }

    public String getJobType() {
        return jobType;
    }

    public void setJobType(String jobType) {
        this.jobType = jobType;
    }
}
