/**
 * Copyright (c) 2013-Now http://jeesite.com All rights reserved.
 * No deletion without permission, or be held responsible to law.
 */
package com.jeesite.modules.swm.entity;

import com.jeesite.common.entity.BaseEntity;
import com.jeesite.common.entity.DataEntity;
import com.jeesite.common.mybatis.annotation.Column;
import com.jeesite.common.mybatis.annotation.Table;
import lombok.Data;

/**
 * 报警灯配置关联Entity
 * @author Shawn
 * @version 2025-01-19
 */
@Table(name="swm_alarm_light_config", alias="a", label="报警灯配置关联信息", columns={
	@Column(name="id", attrName="id", label="主键ID", isPK=true),
	@Column(name="light_id", attrName="lightId", label="报警灯ID"),
	@Column(name="alarm_config_id", attrName="alarmConfigId", label="报警配置ID"),
	@Column(name="voice_template_id", attrName="voiceTemplateId", label="语音模板ID"),
	@Column(includeEntity = DataEntity.class),
	@Column(includeEntity= BaseEntity.class),
}, orderBy="a.create_date ASC"
)
@Data
public class SwmAlarmLightConfig extends DataEntity<SwmAlarmLightConfig> {
	
	private static final long serialVersionUID = 1L;
	
	private String lightId;			// 报警灯ID
	private String alarmConfigId;	// 报警配置ID
	private String voiceTemplateId;	// 语音模板ID
	
	// 关联字段
	private String alarmName;		// 报警名称
	private String templateName;	// 模板名称
	
	public SwmAlarmLightConfig() {
		this(null);
	}
	
	public SwmAlarmLightConfig(String id){
		super(id);
	}
	
	public String getLightId() {
		return lightId;
	}
	
	public void setLightId(String lightId) {
		this.lightId = lightId;
	}
	
	public String getAlarmConfigId() {
		return alarmConfigId;
	}
	
	public void setAlarmConfigId(String alarmConfigId) {
		this.alarmConfigId = alarmConfigId;
	}
	
	public String getVoiceTemplateId() {
		return voiceTemplateId;
	}
	
	public void setVoiceTemplateId(String voiceTemplateId) {
		this.voiceTemplateId = voiceTemplateId;
	}
	
	public String getAlarmName() {
		return alarmName;
	}
	
	public void setAlarmName(String alarmName) {
		this.alarmName = alarmName;
	}
	
	public String getTemplateName() {
		return templateName;
	}
	
	public void setTemplateName(String templateName) {
		this.templateName = templateName;
	}
}