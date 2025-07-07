package com.jeesite.modules.swm.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.jeesite.common.entity.DataEntity;
import com.jeesite.common.mybatis.annotation.Column;
import com.jeesite.common.mybatis.annotation.Table;
import com.jeesite.common.mybatis.mapper.query.QueryType;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Date;

/**
 * 巡检计划实体类
 *
 * @author Shawn
 * @version 2025-05-21
 */
@Table(name = "swm_inspection_plan", alias = "a", columns = {
        @Column(name = "id", attrName = "id", label = "主键ID", isPK = true),
        @Column(name = "plan_name", attrName = "planName", label = "计划名称", queryType = QueryType.LIKE),
        @Column(name = "frequency_days", attrName = "frequencyDays", label = "巡检频次（天数）"),
        @Column(name = "inspection_type", attrName = "inspectionType", label = "巡检类型"),
        @Column(name = "responsible_person_id", attrName = "responsiblePersonId", label = "巡检负责人id"),
        @Column(name = "responsible_person", attrName = "responsiblePerson", label = "巡检负责人名称"),
        @Column(name = "first_inspection_time", attrName = "firstInspectionTime", label = "首次巡检时间"),
        @Column(includeEntity = DataEntity.class)
}, orderBy = "a.create_date DESC")
public class SwmInspectionPlan extends DataEntity<SwmInspectionPlan> {

    private static final long serialVersionUID = 1L;

    private String planName; // 计划名称
    private Integer frequencyDays; // 巡检频次（天数）
    private String inspectionType; // 巡检类型
    private String responsiblePersonId; // 巡检负责人id
    private String responsiblePerson; // 巡检负责人
    private Date firstInspectionTime; // 首次巡检时间
    private String inspectionTypeText; // 巡检类型文本

    public SwmInspectionPlan() {
        this(null);
    }

    public SwmInspectionPlan(String id) {
        super(id);
    }

    @NotBlank(message = "计划名称不能为空")
    @Size(min = 0, max = 255, message = "计划名称长度不能超过 255 个字符")
    public String getPlanName() {
        return planName;
    }

    public void setPlanName(String planName) {
        this.planName = planName;
    }

    @NotNull(message = "巡检频次不能为空")
    public Integer getFrequencyDays() {
        return frequencyDays;
    }

    public void setFrequencyDays(Integer frequencyDays) {
        this.frequencyDays = frequencyDays;
    }

    @NotBlank(message = "巡检类型不能为空")
    @Size(min = 0, max = 100, message = "巡检类型长度不能超过 100 个字符")
    public String getInspectionType() {
        return inspectionType;
    }

    public void setInspectionType(String inspectionType) {
        this.inspectionType = inspectionType;
    }

    @NotBlank(message = "巡检负责人ID不能为空")
    @Size(min = 0, max = 64, message = "巡检负责人ID长度不能超过 64 个字符")
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

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @NotNull(message = "首次巡检时间不能为空")
    public Date getFirstInspectionTime() {
        return firstInspectionTime;
    }

    public void setFirstInspectionTime(Date firstInspectionTime) {
        this.firstInspectionTime = firstInspectionTime;
    }

    public String getInspectionTypeText() {
        return inspectionTypeText;
    }

    public void setInspectionTypeText(String inspectionTypeText) {
        this.inspectionTypeText = inspectionTypeText;
    }
}
