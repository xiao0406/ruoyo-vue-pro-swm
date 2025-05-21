package com.jeesite.modules.swm.entity;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.jeesite.common.entity.DataEntity;
import com.jeesite.common.mybatis.annotation.Column;
import com.jeesite.common.mybatis.annotation.Table;
import com.jeesite.common.mybatis.mapper.query.QueryType;

/**
 * 巡检列表实体类
 * 
 * @author Shawn
 * @version 2025-05-21
 */
@Table(name = "swm_inspection_list", alias = "a", columns = {
        @Column(name = "id", attrName = "id", label = "主键ID", isPK = true),
        @Column(name = "plan_name", attrName = "planName", label = "计划名称", queryType = QueryType.LIKE),
        @Column(name = "inspection_type", attrName = "inspectionType", label = "巡检类型"),
        @Column(name = "inspector_id", attrName = "inspectorId", label = "巡检人"),
        @Column(name = "start_time", attrName = "startTime", label = "开始时间"),
        @Column(name = "end_time", attrName = "endTime", label = "结束时间"),
        @Column(name = "attachment_path", attrName = "attachmentPath", label = "附件路径"),
        @Column(name = "inspection_list_status", attrName = "inspectionListStatus", label = "巡检列表状态"),
        @Column(includeEntity = DataEntity.class)
}, orderBy = "a.create_date DESC")
public class SwmInspectionList extends DataEntity<SwmInspectionList> {

    private static final long serialVersionUID = 1L;

    private String planName; // 计划名称
    private String inspectionType; // 巡检类型
    private String inspectorId; // 巡检人
    private Date startTime; // 开始时间
    private Date endTime; // 结束时间
    private String attachmentPath; // 附件路径
    private String inspectionListStatus; // 巡检列表状态

    // 额外字段，非数据库字段
    private String inspectionTypeText; // 巡检类型文本
    private String inspectionListStatusText; // 巡检列表状态文本

    public SwmInspectionList() {
        this(null);
    }

    public SwmInspectionList(String id) {
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

    @Size(min = 0, max = 100, message = "巡检类型长度不能超过 100 个字符")
    public String getInspectionType() {
        return inspectionType;
    }

    public void setInspectionType(String inspectionType) {
        this.inspectionType = inspectionType;
    }

    @Size(min = 0, max = 100, message = "巡检人长度不能超过 100 个字符")
    public String getInspectorId() {
        return inspectorId;
    }

    public void setInspectorId(String inspectorId) {
        this.inspectorId = inspectorId;
    }

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    @Size(min = 0, max = 1000, message = "附件路径长度不能超过 1000 个字符")
    public String getAttachmentPath() {
        return attachmentPath;
    }

    public void setAttachmentPath(String attachmentPath) {
        this.attachmentPath = attachmentPath;
    }

    @NotBlank(message = "巡检列表状态不能为空")
    @Size(min = 0, max = 2, message = "巡检列表状态长度不能超过 2 个字符")
    public String getInspectionListStatus() {
        return inspectionListStatus;
    }

    public void setInspectionListStatus(String inspectionListStatus) {
        this.inspectionListStatus = inspectionListStatus;
    }

    public String getInspectionTypeText() {
        return inspectionTypeText;
    }

    public void setInspectionTypeText(String inspectionTypeText) {
        this.inspectionTypeText = inspectionTypeText;
    }

    public String getInspectionListStatusText() {
        return inspectionListStatusText;
    }

    public void setInspectionListStatusText(String inspectionListStatusText) {
        this.inspectionListStatusText = inspectionListStatusText;
    }
}