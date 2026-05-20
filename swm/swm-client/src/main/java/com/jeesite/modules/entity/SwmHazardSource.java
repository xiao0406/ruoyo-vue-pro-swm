/**
 * 危险源信息实体类
 * @author Shawn
 * @version 2025-05-21
 */
package com.jeesite.modules.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.jeesite.common.entity.BaseEntity;
import com.jeesite.common.entity.DataEntity;
import com.jeesite.common.mybatis.annotation.Column;
import com.jeesite.common.mybatis.annotation.Table;
import com.jeesite.common.mybatis.mapper.query.QueryType;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;
import java.util.Date;
import java.util.List;

/**
 * 危险源管理
 *
 * @author Shawn
 * @version 2025-05-21
 */
@Table(name = "swm_hazard_source", alias = "a", columns = {
        @Column(name = "id", attrName = "id", label = "主键", isPK = true),
        @Column(name = "hazard_name", attrName = "hazardName", label = "危险源名称", queryType = QueryType.LIKE),
        @Column(name = "hazard_category", attrName = "hazardCategory", label = "危险源类别"),
        @Column(includeEntity = DataEntity.class),
        @Column(name = "location", attrName = "location", label = "位置", queryType = QueryType.LIKE),
        @Column(name = "beacon_identifier", attrName = "beaconIdentifier", label = "所属信标，多个ID用逗号分隔", queryType = QueryType.LIKE),
        @Column(name = "is_patrol_included", attrName = "isPatrolIncluded", label = "是否加入巡检"),
        @Column(name = "patrol_record_summary", attrName = "patrolRecordSummary", label = "巡检记录摘要"),
        @Column(name = "registration_time", attrName = "registrationTime", label = "登记时间"),
        @Column(name = "hazard_status", attrName = "hazardStatus", label = "危险源状态"),
        @Column(name = "frequency_days", attrName = "frequencyDays", label = "巡检频次（天数）"),
        @Column(name = "responsible_person_id", attrName = "responsiblePersonId", label = "巡检负责人id"),
        @Column(name = "responsible_person", attrName = "responsiblePerson", label = "巡检负责人"),
        @Column(name = "first_inspection_time", attrName = "firstInspectionTime", label = "首次巡检时间"),
        @Column(name = "voice_template_id", attrName = "voiceTemplateId", label = "语音模板ID"),
        @Column(name = "beacon_tag", attrName = "beaconTag", label = "信标标记", queryType = QueryType.LIKE),
        @Column(name = "is_draft", attrName = "isDraft", label = "是否草稿状态"),
        @Column(name = "filter_identity_card", attrName = "filterIdentityCard", label = "白名单人员（这里的人员不收到报警信号）"),
        @Column(name = "filter_personnel", attrName = "filterPersonnel", label = "白名单人员（这里的人员不收到报警信号）"),
        @Column(includeEntity= BaseEntity.class),
}, orderBy = "a.create_date DESC")
public class SwmHazardSource extends DataEntity<SwmHazardSource> {

    private static final long serialVersionUID = 1L;

    /**
     * 危险源状态枚举
     */
    @Data
    public static class HazardSourceStatusEnum {
        /** 待处理 */
        public static final String WAIT = "0";
        /** 处理中 */
        public static final String IN_PROGRESS = "1";
        /** 已关闭 */
        public static final String COMPLETED = "2";
        /** 已忽略 */
        public static final String CANCELLED = "3";
    }

    private String hazardName; // 危险源名称
    private String hazardCategory; // 危险源类别
    private String location; // 位置
    private String beaconIdentifier; // 所属信标，多个ID用逗号分隔
    private String isPatrolIncluded; // 是否加入巡检
    private String patrolRecordSummary; // 巡检记录摘要
    private Date registrationTime; // 登记时间
    private String hazardStatus; // 危险源状态
    private Integer frequencyDays; // 巡检频次（天数）
    private String responsiblePersonId; // 巡检负责人id
    private String responsiblePerson; // 巡检负责人
    private Date firstInspectionTime; // 首次巡检时间
    private String voiceTemplateId; // 语音模板ID
    private String beaconTag; // 信标标记
    private String isDraft; // 是否草稿状态 0:否 1:是
    private String filterIdentityCard; // 白名单人员（这里的人员不收到报警信号）
    private String filterPersonnel; // 白名单人员（这里的人员不收到报警信号）

    // 非持久化字段
    private String[] beaconIdentifiers; // 所属信标数组，用于前端多选
    private String beaconIdentifierText; // 所属信标显示文本
    private List<String> beaconIdentifierSearchList; // 用于多信标搜索的列表

    // 非持久化的文本展示字段，不再由数据库JOIN查询获取，而是在Controller中手动设置
    private String hazardCategoryText; // 危险源类别文本
    private String isPatrolIncludedText; // 是否加入巡检文本
    private String hazardStatusText; // 危险源状态文本
    private String voiceTemplateText; // 语音模板名称文本

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
        // 如果beaconIdentifiers已设置，则将其转换为逗号分隔的字符串
        if (beaconIdentifiers != null && beaconIdentifiers.length > 0) {
            return String.join(",", beaconIdentifiers);
        }
        return beaconIdentifier;
    }

    public void setBeaconIdentifier(String beaconIdentifier) {
        this.beaconIdentifier = beaconIdentifier;
        // 如果设置了beaconIdentifier，同时更新beaconIdentifiers数组
        if (beaconIdentifier != null && !beaconIdentifier.isEmpty()) {
            this.beaconIdentifiers = beaconIdentifier.split(",");
        } else {
            this.beaconIdentifiers = new String[0];
        }
    }

    public String[] getBeaconIdentifiers() {
        // 如果beaconIdentifiers为null但beaconIdentifier不为空，则初始化
        if (beaconIdentifiers == null && beaconIdentifier != null && !beaconIdentifier.isEmpty()) {
            beaconIdentifiers = beaconIdentifier.split(",");
        }
        return beaconIdentifiers == null ? new String[0] : beaconIdentifiers;
    }

    public void setBeaconIdentifiers(String[] beaconIdentifiers) {
        this.beaconIdentifiers = beaconIdentifiers;
        // 同步更新beaconIdentifier字段
        if (beaconIdentifiers != null && beaconIdentifiers.length > 0) {
            this.beaconIdentifier = String.join(",", beaconIdentifiers);
        } else {
            this.beaconIdentifier = "";
        }
    }

    public String getBeaconIdentifierText() {
        return beaconIdentifierText;
    }

    public void setBeaconIdentifierText(String beaconIdentifierText) {
        this.beaconIdentifierText = beaconIdentifierText;
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

    public Integer getFrequencyDays() {
        return frequencyDays;
    }

    public void setFrequencyDays(Integer frequencyDays) {
        this.frequencyDays = frequencyDays;
    }

    public String getResponsiblePersonId() {
        return responsiblePersonId;
    }

    public void setResponsiblePersonId(String responsiblePersonId) {
        this.responsiblePersonId = responsiblePersonId;
    }

    public String getResponsiblePerson() {
        return responsiblePerson;
    }

    public void setResponsiblePerson(String responsiblePerson) {
        this.responsiblePerson = responsiblePerson;
    }

    public Date getFirstInspectionTime() {
        return firstInspectionTime;
    }

    public void setFirstInspectionTime(Date firstInspectionTime) {
        this.firstInspectionTime = firstInspectionTime;
    }

    @Length(min = 0, max = 64, message = "语音模板ID长度不能超过 64 个字符")
    public String getVoiceTemplateId() {
        return voiceTemplateId;
    }

    public void setVoiceTemplateId(String voiceTemplateId) {
        this.voiceTemplateId = voiceTemplateId;
    }

    public String getVoiceTemplateText() {
        return voiceTemplateText;
    }

    public void setVoiceTemplateText(String voiceTemplateText) {
        this.voiceTemplateText = voiceTemplateText;
    }

    @Length(min = 0, max = 200, message = "信标标记长度不能超过 200 个字符")
    public String getBeaconTag() {
        return beaconTag;
    }

    public void setBeaconTag(String beaconTag) {
        this.beaconTag = beaconTag;
    }

    public String getHazardStatusText() {
        return hazardStatusText;
    }

    public void setHazardStatusText(String hazardStatusText) {
        this.hazardStatusText = hazardStatusText;
    }

    public String getIsDraft() {
        return isDraft;
    }

    public void setIsDraft(String isDraft) {
        this.isDraft = isDraft;
    }

    public List<String> getBeaconIdentifierSearchList() {
        return beaconIdentifierSearchList;
    }

    public void setBeaconIdentifierSearchList(List<String> beaconIdentifierSearchList) {
        this.beaconIdentifierSearchList = beaconIdentifierSearchList;
    }

    public String getFilterIdentityCard() {
        return filterIdentityCard;
    }

    public void setFilterIdentityCard(String filterIdentityCard) {
        this.filterIdentityCard = filterIdentityCard;
    }

    public String getFilterPersonnel() {
        return filterPersonnel;
    }

    public void setFilterPersonnel(String filterPersonnel) {
        this.filterPersonnel = filterPersonnel;
    }
}
