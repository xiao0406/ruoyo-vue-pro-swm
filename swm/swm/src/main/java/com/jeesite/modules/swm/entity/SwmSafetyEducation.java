package com.jeesite.modules.swm.entity;

import com.jeesite.common.entity.BaseEntity;
import com.jeesite.common.entity.DataEntity;
import com.jeesite.common.mybatis.annotation.Column;
import com.jeesite.common.mybatis.annotation.Table;
import com.jeesite.common.mybatis.mapper.query.QueryType;
import com.jeesite.modules.sys.utils.DictUtils;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;
import java.util.Date;

/**
 * 安全教育实体类
 * 
 * @author zwf
 * @version 2025-05-14
 * 
 */
@Table(name = "swm_safety_education", alias = "a", label = "安全教育管理表", columns = {
        @Column(name = "id", attrName = "id", label = "主键ID", isPK = true),
        @Column(name = "theme", attrName = "theme", label = "主题", queryType = QueryType.LIKE),
        @Column(name = "content_description", attrName = "contentDescription", label = "内容描述"),
        @Column(name = "safety_education_type", attrName = "safetyEducationType", label = "安全教育类型"),
        @Column(name = "start_time", attrName = "startTime", label = "开始时间"),
        @Column(name = "participants", attrName = "participants", label = "参与对象", queryType = QueryType.LIKE),
        @Column(name = "participants_name", attrName = "participantsName", label = "参与对象名称", queryType = QueryType.LIKE),
        @Column(name = "safety_status", attrName = "safetyStatus", label = "状态"),
        @Column(name = "participation_type", attrName = "participationType", label = "参与类型"),
        @Column(name = "attachment_url", attrName = "attachmentUrl", label = "附件URL"),
        @Column(name = "create_time", attrName = "createTime", label = "创建时间", isUpdateForce = true),
        @Column(name = "update_time", attrName = "updateTime", label = "更新时间", isUpdateForce = true),
        @Column(name = "status", attrName = "status", label = "状态：0-正常，1-删除"),
        @Column(includeEntity= BaseEntity.class),
}, orderBy = "a.update_time DESC")
public class SwmSafetyEducation extends DataEntity<SwmSafetyEducation> {

    private static final long serialVersionUID = 1L;

    /**
     * 安全教育状态枚举
     * 
     * @author Shawn
     * @date 2025-05-21
     */
    public static class StatusEnum {
        /** 未开始 */
        public static final String NOT_STARTED = "0";
        /** 已完成 */
        public static final String COMPLETED = "1";

        /**
         * 获取状态显示文本
         */
        public static String getText(String value) {
            return DictUtils.getDictLabel("education_status_enum", value, "");
        }
    }

    /**
     * 安全教育类型枚举
     * 
     * @author Shawn
     * @date 2025-05-21
     */
    public static class EducationTypeEnum {
        /** 人员入职安全教育 */
        public static final String ENTRY = "1";
        /** 周安全教育 */
        public static final String WEEKLY = "2";
        /** 月度教育 */
        public static final String MONTHLY = "3";
        /** 季度教育 */
        public static final String QUARTERLY = "4";
        /** 专题教育 */
        public static final String SPECIAL = "5";

        /**
         * 获取教育类型显示文本
         */
        public static String getText(String value) {
            return DictUtils.getDictLabel("education_type_enum", value, "");
        }
    }

    /**
     * 参与类型枚举
     * 
     * @author Shawn
     * @date 2025-05-21
     */
    public static class ParticipationTypeEnum {
        /** 班组 */
        public static final String TEAM = "1";
        /** 产线 */
        public static final String PROCESS = "2";
        /** 车间 */
        public static final String WORKSHOP = "3";

        /**
         * 获取参与类型显示文本
         */
        public static String getText(String value) {
            return DictUtils.getDictLabel("education_participation_type_enum", value, "");
        }
    }

    private String theme; // 主题
    private String contentDescription; // 内容描述
    private String safetyEducationType; // 安全教育类型
    private Date startTime; // 开始时间
    private String participants; // 参与对象
    private String participantsName; // 参与对象名称
    private String safetyStatus; // 状态
    private String participationType; // 参与类型
    private String attachmentUrl; // 附件URL
    private Date createTime; // 创建时间
    private Date updateTime; // 更新时间
    private String status; // 状态：0-正常，1-删除

    // 用于显示的属性，不对应数据库字段
    private String safetyEducationTypeText; // 安全教育类型显示文本
    private String participationTypeText; // 参与类型显示文本
    private String safetyStatusText; // 安全状态显示文本

    public SwmSafetyEducation() {
        this(null);
    }

    public SwmSafetyEducation(String id) {
        super(id);
        // 取消DataEntity默认的状态过滤
        this.safetyStatus = null;
        // 默认状态为正常
        this.status = String.valueOf(0);
    }

    @NotBlank(message = "主题不能为空")
    @Length(min = 0, max = 200, message = "主题长度不能超过 200 个字符")
    public String getTheme() {
        return theme;
    }

    public void setTheme(String theme) {
        this.theme = theme;
    }

    @Length(min = 0, max = 4000, message = "内容描述长度不能超过 4000 个字符")
    public String getContentDescription() {
        return contentDescription;
    }

    public void setContentDescription(String contentDescription) {
        this.contentDescription = contentDescription;
    }

    @Length(min = 0, max = 50, message = "安全教育类型长度不能超过 50 个字符")
    public String getSafetyEducationType() {
        return safetyEducationType;
    }

    public void setSafetyEducationType(String safetyEducationType) {
        this.safetyEducationType = safetyEducationType;
    }

    /**
     * 获取安全教育类型显示文本
     */
    public String getSafetyEducationTypeText() {
        if (this.safetyEducationTypeText == null && this.safetyEducationType != null) {
            this.safetyEducationTypeText = EducationTypeEnum.getText(this.safetyEducationType);
        }
        return this.safetyEducationTypeText;
    }

    public void setSafetyEducationTypeText(String safetyEducationTypeText) {
        this.safetyEducationTypeText = safetyEducationTypeText;
    }

    @NotBlank(message = "参与类型不能为空")
    @Length(min = 0, max = 50, message = "参与类型长度不能超过 50 个字符")
    public String getParticipationType() {
        return participationType;
    }

    public void setParticipationType(String participationType) {
        this.participationType = participationType;
    }

    /**
     * 获取参与类型显示文本
     */
    public String getParticipationTypeText() {
        if (this.participationTypeText == null && this.participationType != null) {
            this.participationTypeText = ParticipationTypeEnum.getText(this.participationType);
        }
        return this.participationTypeText;
    }

    public void setParticipationTypeText(String participationTypeText) {
        this.participationTypeText = participationTypeText;
    }

    /**
     * 获取安全状态显示文本
     */
    public String getSafetyStatusText() {
        if (this.safetyStatusText == null && this.safetyStatus != null) {
            this.safetyStatusText = StatusEnum.getText(this.safetyStatus);
        }
        return this.safetyStatusText;
    }

    public void setSafetyStatusText(String safetyStatusText) {
        this.safetyStatusText = safetyStatusText;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    @Length(min = 0, max = 50000, message = "参与对象长度不能超过 50000 个字符")
    public String getParticipants() {
        return participants;
    }

    public void setParticipants(String participants) {
        this.participants = participants;
    }

    @Length(min = 0, max = 50000, message = "参与对象名称长度不能超过 50000 个字符")
    public String getParticipantsName() {
        return participantsName;
    }

    public void setParticipantsName(String participantsName) {
        this.participantsName = participantsName;
    }

    @Length(min = 0, max = 20, message = "状态长度不能超过 20 个字符")
    public String getSafetyStatus() {
        return safetyStatus;
    }

    public void setSafetyStatus(String safetyStatus) {
        this.safetyStatus = safetyStatus;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    @Length(min = 0, max = 4000, message = "附件URL长度不能超过 4000 个字符")
    public String getAttachmentUrl() {
        return attachmentUrl;
    }

    public void setAttachmentUrl(String attachmentUrl) {
        this.attachmentUrl = attachmentUrl;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = String.valueOf(status);
    }
}