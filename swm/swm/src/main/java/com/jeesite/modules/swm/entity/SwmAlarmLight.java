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
 * 报警灯设备Entity
 * 
 * @author Shawn
 * @version 2025-01-19
 */
@Table(name = "swm_alarm_light", alias = "a", label = "报警灯设备信息", columns = {
		@Column(name = "id", attrName = "id", label = "主键ID", isPK = true),
		@Column(name = "light_name", attrName = "lightName", label = "报警灯名称"),
		@Column(name = "sn_code", attrName = "snCode", label = "报警灯SN码"),
		@Column(name = "enable_alarm", attrName = "enableAlarm", label = "是否报警"),
		@Column(includeEntity = DataEntity.class),
		@Column(includeEntity= BaseEntity.class),
}, orderBy = "a.update_date DESC")
@Data
public class SwmAlarmLight extends DataEntity<SwmAlarmLight> {

	private static final long serialVersionUID = 1L;

	private String lightName; // 报警灯名称
	private String snCode; // 报警灯SN码
	private String enableAlarm; // 是否报警（0否 1是）

	// 扩展字段
	private Integer configCount; // 配置数量
	private String alarmTypeNames; // 报警类型名称（逗号分隔）
	private String alarmTypeName; // 报警类型名称（搜索用）

	public SwmAlarmLight() {
		this(null);
	}

	public SwmAlarmLight(String id) {
		super(id);
	}

	public String getLightName() {
		return lightName;
	}

	public void setLightName(String lightName) {
		this.lightName = lightName;
	}

	public String getSnCode() {
		return snCode;
	}

	public void setSnCode(String snCode) {
		this.snCode = snCode;
	}

	public String getEnableAlarm() {
		return enableAlarm;
	}

	public void setEnableAlarm(String enableAlarm) {
		this.enableAlarm = enableAlarm;
	}

	public Integer getConfigCount() {
		return configCount;
	}

	public void setConfigCount(Integer configCount) {
		this.configCount = configCount;
	}

	public String getAlarmTypeNames() {
		return alarmTypeNames;
	}

	public void setAlarmTypeNames(String alarmTypeNames) {
		this.alarmTypeNames = alarmTypeNames;
	}

	public String getAlarmTypeName() {
		return alarmTypeName;
	}

	public void setAlarmTypeName(String alarmTypeName) {
		this.alarmTypeName = alarmTypeName;
	}

	/**
	 * 是否报警枚举
	 */
	public static class EnableAlarmEnum {
		public static final String YES = "1"; // 是
		public static final String NO = "0"; // 否

		public static String getText(String value) {
			if (YES.equals(value)) {
				return "是";
			} else if (NO.equals(value)) {
				return "否";
			}
			return "";
		}
	}

	/**
	 * 获取是否报警显示文本
	 */
	public String getEnableAlarmText() {
		return EnableAlarmEnum.getText(enableAlarm);
	}
}