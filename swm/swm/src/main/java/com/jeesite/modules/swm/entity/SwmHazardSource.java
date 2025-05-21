/**
 * 危险源信息实体类
 * @author Shawn
 * @version 2025-05-21
 */
package com.jeesite.modules.swm.entity;

import java.util.Date;
import com.jeesite.common.entity.DataEntity;
import com.jeesite.common.mybatis.annotation.Column;
import com.jeesite.common.mybatis.annotation.Table;
import com.jeesite.common.mybatis.mapper.query.QueryType;
import org.hibernate.validator.constraints.Length;
import javax.validation.constraints.NotBlank;
import com.fasterxml.jackson.annotation.JsonFormat;

@Table(name = "swm_hazard_source", alias = "a", columns = {
        @Column(name = "id", attrName = "id", label = "主键", isPK = true),
        @Column(name = "hazard_name", attrName = "hazardName", label = "危险源名称", queryType = QueryType.LIKE),
        @Column(name = "hazard_category", attrName = "hazardCategory", label = "危险源类别"),
        @Column(includeEntity = DataEntity.class),
        @Column(name = "location", attrName = "location", label = "位置", queryType = QueryType.LIKE),
        @Column(name = "beacon_identifier", attrName = "beaconIdentifier", label = "所属信标", queryType = QueryType.LIKE),
        @Column(name = "is_patrol_included", attrName = "isPatrolIncluded", label = "是否加入巡检"),
        @Column(name = "patrol_record_summary", attrName = "patrolRecordSummary", label = "巡检记录摘要"),
        @Column(name = "registration_time", attrName = "registrationTime", label = "登记时间"),
        @Column(name = "hazard_status", attrName = "hazardStatus", label = "危险源状态")
}, orderBy = "a.create_date DESC")
public class SwmHazardSource extends DataEntity<SwmHazardSource> {

    private static final long serialVersionUID = 1L;

    private String hazardName; // 危险源名称
    private String hazardCategory; // 危险源类别
    private String location; // 位置
    private String beaconIdentifier; // 所属信标
    private String isPatrolIncluded; // 是否加入巡检
    private String patrolRecordSummary; // 巡检记录摘要
    private Date registrationTime; // 登记时间
    private String hazardStatus; // 危险源状态

    // 非持久化的文本展示字段，不再由数据库JOIN查询获取，而是在Controller中手动设置
    private String hazardCategoryText; // 危险源类别文本
    private String isPatrolIncludedText; // 是否加入巡检文本
    private String hazardStatusText; // 危险源状态文本

    public SwmHazardSource() {
        this(null);
    }

    public SwmHazardSource(String id) {
        super(id);
    }

    @NotBlank(message = "危险源名称不能为空")
    @Length(min = 0, max = 255, message = "危险源名称长度不能超过 255 个字符")
    public String getHazardName() {
        return hazardName;
    }

    public void setHazardName(String hazardName) {
        this.hazardName = hazardName;
    }

    public String getHazardCategory() {
        return hazardCategory;
    }

    public void setHazardCategory(String hazardCategory) {
        this.hazardCategory = hazardCategory;
    }

    public String getHazardCategoryText() {
        return hazardCategoryText;
    }

    public void setHazardCategoryText(String hazardCategoryText) {
        this.hazardCategoryText = hazardCategoryText;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getBeaconIdentifier() {
        return beaconIdentifier;
    }

    public void setBeaconIdentifier(String beaconIdentifier) {
        this.beaconIdentifier = beaconIdentifier;
    }

    public String getIsPatrolIncluded() {
        return isPatrolIncluded;
    }

    public void setIsPatrolIncluded(String isPatrolIncluded) {
        this.isPatrolIncluded = isPatrolIncluded;
    }

    public String getIsPatrolIncludedText() {
        return isPatrolIncludedText;
    }

    public void setIsPatrolIncludedText(String isPatrolIncludedText) {
        this.isPatrolIncludedText = isPatrolIncludedText;
    }

    public String getPatrolRecordSummary() {
        return patrolRecordSummary;
    }

    public void setPatrolRecordSummary(String patrolRecordSummary) {
        this.patrolRecordSummary = patrolRecordSummary;
    }

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    public Date getRegistrationTime() {
        return registrationTime;
    }

    public void setRegistrationTime(Date registrationTime) {
        this.registrationTime = registrationTime;
    }

    public String getHazardStatus() {
        return hazardStatus;
    }

    public void setHazardStatus(String hazardStatus) {
        this.hazardStatus = hazardStatus;
    }

    public String getHazardStatusText() {
        return hazardStatusText;
    }

    public void setHazardStatusText(String hazardStatusText) {
        this.hazardStatusText = hazardStatusText;
    }
}