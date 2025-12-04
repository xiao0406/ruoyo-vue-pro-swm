package com.jeesite.modules.swm.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.jeesite.common.entity.BaseEntity;
import com.jeesite.common.entity.DataEntity;
import com.jeesite.common.mybatis.annotation.Column;
import com.jeesite.common.mybatis.annotation.Table;
import com.jeesite.common.mybatis.mapper.query.QueryType;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;
import java.util.Date;

/**
 * 处置记录实体类
 * 
 * @author zwf
 * @version 2025-05-16
 */
@Table(name = "swm_handle_record", alias = "a", columns = {
        @Column(name = "id", attrName = "id", label = "主键ID", isPK = true),
        @Column(name = "record_name", attrName = "recordName", label = "处置记录名称", queryType = QueryType.LIKE),
        @Column(name = "warning_id", attrName = "warningId", label = "关联预警ID"),
        @Column(name = "warning_record", attrName = "warningRecord", label = "预警记录", queryType = QueryType.LIKE),
        @Column(name = "alarm_time", attrName = "alarmTime", label = "预警/报警时间"),
        @Column(name = "handler", attrName = "handler", label = "处置人", queryType = QueryType.LIKE),
        @Column(name = "handle_time", attrName = "handleTime", label = "处置时间"),
        @Column(name = "handle_process", attrName = "handleProcess", label = "处置过程"),
        @Column(name = "handle_status", attrName = "handleStatus", label = "处置状态"),
        @Column(name = "attachment", attrName = "attachment", label = "附件路径", comment = "附件路径"),
        @Column(includeEntity = DataEntity.class),
        @Column(includeEntity= BaseEntity.class),
}, orderBy = "a.create_date DESC")
public class SwmHandleRecord extends DataEntity<SwmHandleRecord> {

    private static final long serialVersionUID = 1L;

    private String recordName; // 处置记录名称
    private String warningId; // 关联预警ID
    private String warningRecord; // 预警记录
    private Date alarmTime; // 预警/报警时间
    private String handler; // 处置人
    private Date handleTime; // 处置时间
    private String handleProcess; // 处置过程
    private String handleStatus; // 处置状态
    private String attachment; // 附件路径
    private String warningMan; // 预警人

    // 用于显示的属性，不对应数据库字段
    private String handleStatusText; // 处置状态显示文本
    private String warningContent; // 预警内容（从关联的预警记录中获取）

    // 查询条件字段
    private Date beginAlarmTime; // 查询开始预警/报警时间
    private Date endAlarmTime; // 查询结束预警/报警时间
    private Date beginHandleTime; // 查询开始处置时间
    private Date endHandleTime; // 查询结束处置时间

    /**
     * 处置状态枚举
     */
    public static class HandleStatusEnum {
        public static final String UNHANDLED = "0"; // 未处置
        public static final String HANDLED = "1"; // 已完成
        public static final String DRAFT = "2"; // 草稿

        public static String getText(String value) {
            if (UNHANDLED.equals(value)) {
                return "未处置";
            } else if (HANDLED.equals(value)) {
                return "已完成";
            } else if (DRAFT.equals(value)) {
                return "草稿";
            }
            return "未知";
        }
    }

    public SwmHandleRecord() {
        this(null);
        // 创建新实例时才设置默认的处置状态，用于查询时不设置默认值
        // this.handleStatus = HandleStatusEnum.UNHANDLED;
    }

    public SwmHandleRecord(String id) {
        super(id);
    }

    /**
     * 创建一个新记录（用于保存）
     */
    public static SwmHandleRecord createNewRecord() {
        SwmHandleRecord record = new SwmHandleRecord();
        record.setHandleStatus(HandleStatusEnum.UNHANDLED);
        return record;
    }

    @NotBlank(message = "处置记录名称不能为空")
    @Length(min = 0, max = 100, message = "处置记录名称长度不能超过 100 个字符")
    public String getRecordName() {
        return recordName;
    }

    public void setRecordName(String recordName) {
        this.recordName = recordName;
    }

    @Length(min = 0, max = 64, message = "关联预警ID长度不能超过 64 个字符")
    public String getWarningId() {
        return warningId;
    }

    public void setWarningId(String warningId) {
        this.warningId = warningId;
    }

    @Length(min = 0, max = 200, message = "预警记录长度不能超过 200 个字符")
    public String getWarningRecord() {
        return warningRecord;
    }

    public void setWarningRecord(String warningRecord) {
        this.warningRecord = warningRecord;
    }

    public Date getAlarmTime() {
        return alarmTime;
    }

    public void setAlarmTime(Date alarmTime) {
        this.alarmTime = alarmTime;
    }

    @Length(min = 0, max = 100, message = "处置人长度不能超过 100 个字符")
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

    @Length(min = 0, max = 2, message = "处置状态长度不能超过 2 个字符")
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

    // 查询条件的getter/setter

    @JsonIgnore
    public Date getBeginAlarmTime() {
        return beginAlarmTime;
    }

    public void setBeginAlarmTime(Date beginAlarmTime) {
        this.beginAlarmTime = beginAlarmTime;
    }

    @JsonIgnore
    public Date getEndAlarmTime() {
        return endAlarmTime;
    }

    public void setEndAlarmTime(Date endAlarmTime) {
        this.endAlarmTime = endAlarmTime;
    }

    @JsonIgnore
    public Date getBeginHandleTime() {
        return beginHandleTime;
    }

    public void setBeginHandleTime(Date beginHandleTime) {
        this.beginHandleTime = beginHandleTime;
    }

    @JsonIgnore
    public Date getEndHandleTime() {
        return endHandleTime;
    }

    public void setEndHandleTime(Date endHandleTime) {
        this.endHandleTime = endHandleTime;
    }

    // mediumtext类型，无需长度限制
    public String getAttachment() {
        return attachment;
    }

    public void setAttachment(String attachment) {
        this.attachment = attachment;
    }

    public String getWarningMan() {
        return warningMan;
    }

    public void setWarningMan(String warningMan) {
        this.warningMan = warningMan;
    }

    public String getWarningContent() {
        return warningContent;
    }

    public void setWarningContent(String warningContent) {
        this.warningContent = warningContent;
    }
}
