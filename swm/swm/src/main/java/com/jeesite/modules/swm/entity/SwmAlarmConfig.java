package com.jeesite.modules.swm.entity;

import com.jeesite.common.entity.BaseEntity;
import com.jeesite.common.entity.DataEntity;
import com.jeesite.common.mybatis.annotation.Column;
import com.jeesite.common.mybatis.annotation.Table;
import com.jeesite.common.mybatis.mapper.query.QueryType;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

/**
 * 预警报警配置表实体类
 *
 * @author zwf
 * @version 2025-06-18
 */
@Table(name = "swm_alarm_config", alias = "a", columns = {
        @Column(name = "id", attrName = "id", label = "主键ID", isPK = true),
        @Column(name = "alarm_name", attrName = "alarmName", label = "报警名称", queryType = QueryType.LIKE),
        @Column(name = "alarm_key", attrName = "alarmKey", label = "报警唯一标识key"),
        @Column(name = "enable_alarm", attrName = "enableAlarm", label = "是否报警"),
        @Column(name = "need_confirm", attrName = "needConfirm", label = "是否弹窗确认"),
        @Column(name = "dialog_position", attrName = "dialogPosition", label = "弹窗位置"),
        @Column(name = "is_send_zjt", attrName = "isSendZjt", label = "是否推送中建通（1是，0否）"),
        @Column(includeEntity = DataEntity.class),
        @Column(includeEntity= BaseEntity.class),
}, orderBy = "a.update_date DESC")
@Data
public class SwmAlarmConfig extends DataEntity<SwmAlarmConfig> {

    private static final long serialVersionUID = 1L;

    private String alarmName; // 报警名称
    private String alarmKey; // 报警唯一标识key
    private Integer enableAlarm; // 是否报警（0否 1是）
    private Integer needConfirm; // 是否弹窗确认（0否 1是）
    private String dialogPosition; // 弹窗位置
    /**是否推送中建通（1是，0否）*/
    private Integer isSendZjt;

    /**
     * 随机值，目的取消一级缓存
     */
    private Integer random;

    public Integer getIsSendZjt() {
        return isSendZjt;
    }

    public void setIsSendZjt(Integer isSendZjt) {
        this.isSendZjt = isSendZjt;
    }

    public SwmAlarmConfig() {
        this(null);
    }

    public SwmAlarmConfig(String id) {
        super(id);
    }

    @Length(min = 1, max = 100, message = "报警名称不能为空且不能超过100个字符")
    public String getAlarmName() {
        return alarmName;
    }

    public void setAlarmName(String alarmName) {
        this.alarmName = alarmName;
    }

    @Length(min = 1, max = 100, message = "报警唯一标识key不能为空且不能超过100个字符")
    public String getAlarmKey() {
        return alarmKey;
    }

    public void setAlarmKey(String alarmKey) {
        this.alarmKey = alarmKey;
    }

    public Integer getEnableAlarm() {
        return enableAlarm;
    }

    public void setEnableAlarm(Integer enableAlarm) {
        this.enableAlarm = enableAlarm;
    }

    public Integer getNeedConfirm() {
        return needConfirm;
    }

    public void setNeedConfirm(Integer needConfirm) {
        this.needConfirm = needConfirm;
    }

    @Length(min = 0, max = 50, message = "弹窗位置不能超过50个字符")
    public String getDialogPosition() {
        return dialogPosition;
    }

    public void setDialogPosition(String dialogPosition) {
        this.dialogPosition = dialogPosition;
    }
} 