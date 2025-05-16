package com.jeesite.modules.swm.entity;

import com.jeesite.common.entity.DataEntity;
import com.jeesite.common.mybatis.annotation.Column;
import com.jeesite.common.mybatis.annotation.Table;
import com.jeesite.common.mybatis.mapper.query.QueryType;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;
import java.util.Date;

/**
 * 预警管理表实体类
 * 
 * @author auto create
 * @version 2025-05-16
 */
@Table(name = "swm_warning_management", alias = "a", columns = {
        @Column(name = "id", attrName = "id", label = "主键ID", isPK = true),
        @Column(name = "person_name", attrName = "personName", label = "人员名称", queryType = QueryType.LIKE),
        @Column(name = "warning_type", attrName = "warningType", label = "预警类型"),
        @Column(name = "warning_content", attrName = "warningContent", label = "预警内容"),
        @Column(name = "warning_time", attrName = "warningTime", label = "预警时间"),
        @Column(name = "alarm_record", attrName = "alarmRecord", label = "报警记录"),
        @Column(name = "alarm_time", attrName = "alarmTime", label = "报警时间"),
        @Column(name = "trigger_reason", attrName = "triggerReason", label = "触发原因"),
        @Column(name = "handler", attrName = "handler", label = "处置人"),
        @Column(name = "handle_time", attrName = "handleTime", label = "处置时间"),
        @Column(name = "handle_process", attrName = "handleProcess", label = "处置过程"),
        @Column(name = "handle_status", attrName = "handleStatus", label = "处置状态"),
        @Column(name = "attachment", attrName = "attachment", label = "附件路径"),
        @Column(includeEntity = DataEntity.class)
}, orderBy = "a.warning_time DESC")
public class SwmWarningManagement extends DataEntity<SwmWarningManagement> {

    private static final long serialVersionUID = 1L;
    
    /**
     * 预警类型枚举
     */
    public static class WarningTypeEnum {
        /** 主动预警 */
        public static final String ACTIVE = "1";
        /** 被动预警 */
        public static final String PASSIVE = "2";
        
        /**
         * 获取预警类型显示文本
         */
        public static String getText(String value) {
            if (ACTIVE.equals(value)) {
                return "主动预警";
            } else if (PASSIVE.equals(value)) {
                return "被动预警";
            }
            return "";
        }
    }

    /**
     * 处置状态枚举
     */
    public static class HandleStatusEnum {
        /** 未处置 */
        public static final String UNHANDLED = "0";
        /** 已处置 */
        public static final String HANDLED = "1";
        
        /**
         * 获取处置状态显示文本
         */
        public static String getText(String value) {
            if (UNHANDLED.equals(value)) {
                return "未处置";
            } else if (HANDLED.equals(value)) {
                return "已处置";
            }
            return "";
        }
    }

    private String personName;     // 人员名称
    private String warningType;    // 预警类型
    private String warningContent; // 预警内容
    private Date warningTime;      // 预警时间
    private String alarmRecord;    // 报警记录
    private Date alarmTime;        // 报警时间
    private String triggerReason;  // 触发原因
    private String handler;        // 处置人
    private Date handleTime;       // 处置时间
    private String handleProcess;  // 处置过程
    private String handleStatus;   // 处置状态
    private String attachment;     // 附件路径
    
    // 用于显示的属性，不对应数据库字段
    private String warningTypeText;     // 预警类型显示文本
    private String handleStatusText;    // 处置状态显示文本

    public SwmWarningManagement() {
        this(null);
        this.handleStatus = HandleStatusEnum.UNHANDLED; // 默认未处置
    }

    public SwmWarningManagement(String id) {
        super(id);
    }

    @Length(min = 0, max = 100, message = "人员名称不能超过100个字符")
    public String getPersonName() {
        return personName;
    }

    public void setPersonName(String personName) {
        this.personName = personName;
    }

    @Length(min = 0, max = 20, message = "预警类型不能超过20个字符")
    public String getWarningType() {
        return warningType;
    }

    public void setWarningType(String warningType) {
        this.warningType = warningType;
    }
    
    /**
     * 获取预警类型显示文本
     */
    public String getWarningTypeText() {
        if (this.warningTypeText == null && this.warningType != null) {
            this.warningTypeText = WarningTypeEnum.getText(this.warningType);
        }
        return this.warningTypeText;
    }
    
    public void setWarningTypeText(String warningTypeText) {
        this.warningTypeText = warningTypeText;
    }

    @Length(min = 0, max = 50, message = "预警内容不能超过50个字符")
    public String getWarningContent() {
        return warningContent;
    }

    public void setWarningContent(String warningContent) {
        this.warningContent = warningContent;
    }

    public Date getWarningTime() {
        return warningTime;
    }

    public void setWarningTime(Date warningTime) {
        this.warningTime = warningTime;
    }

    @Length(min = 0, max = 200, message = "报警记录不能超过200个字符")
    public String getAlarmRecord() {
        return alarmRecord;
    }

    public void setAlarmRecord(String alarmRecord) {
        this.alarmRecord = alarmRecord;
    }

    public Date getAlarmTime() {
        return alarmTime;
    }

    public void setAlarmTime(Date alarmTime) {
        this.alarmTime = alarmTime;
    }

    public String getTriggerReason() {
        return triggerReason;
    }

    public void setTriggerReason(String triggerReason) {
        this.triggerReason = triggerReason;
    }

    @Length(min = 0, max = 100, message = "处置人不能超过100个字符")
    public String getHandler() {
        return handler;
    }

    public void setHandler(String handler) {
        this.handler = handler;
    }

    public Date getHandleTime() {
        return handleTime;
    }

    public void setHandleTime(Date handleTime) {
        this.handleTime = handleTime;
    }

    public String getHandleProcess() {
        return handleProcess;
    }

    public void setHandleProcess(String handleProcess) {
        this.handleProcess = handleProcess;
    }
    
    @Length(min = 0, max = 2, message = "处置状态不能超过2个字符")
    public String getHandleStatus() {
        return handleStatus;
    }

    public void setHandleStatus(String handleStatus) {
        this.handleStatus = handleStatus;
    }
    
    /**
     * 获取处置状态显示文本
     */
    public String getHandleStatusText() {
        if (this.handleStatusText == null && this.handleStatus != null) {
            this.handleStatusText = HandleStatusEnum.getText(this.handleStatus);
        }
        return this.handleStatusText;
    }
    
    public void setHandleStatusText(String handleStatusText) {
        this.handleStatusText = handleStatusText;
    }

    @Length(min = 0, max = 500, message = "附件路径不能超过500个字符")
    public String getAttachment() {
        return attachment;
    }

    public void setAttachment(String attachment) {
        this.attachment = attachment;
    }
} 