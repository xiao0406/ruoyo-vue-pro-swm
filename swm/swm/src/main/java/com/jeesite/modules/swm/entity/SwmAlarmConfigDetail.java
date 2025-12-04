package com.jeesite.modules.swm.entity;

import javax.validation.constraints.Size;

import com.jeesite.common.entity.BaseEntity;
import com.jeesite.common.entity.DataEntity;
import com.jeesite.common.mybatis.annotation.Column;
import com.jeesite.common.mybatis.annotation.Table;
import com.jeesite.common.mybatis.mapper.query.QueryType;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * swm_alarm_config_detailEntity
 * @author gjl
 * @version 2025-08-11
 */
@Table(name="swm_alarm_config_detail", alias="a", label="swm_alarm_config_detail信息", columns={
		@Column(name="id", attrName="id", label="主键", isPK=true),
		@Column(name="roles", attrName="roles", label="推送角色", comment="推送角色（多个用逗号分隔）"),
		@Column(name="users", attrName="users", label="推送人员", comment="推送人员（多个用逗号分隔）"),
		@Column(includeEntity=DataEntity.class),
		@Column(name="main_id", attrName="mainId", label="主表id"),
		@Column(includeEntity= BaseEntity.class),
	}, orderBy="a.update_date DESC"
)
@ApiModel(value = "SwmAlarmConfigDetail对象", description = "swm_alarm_config_detailEntity")
public class SwmAlarmConfigDetail extends DataEntity<SwmAlarmConfigDetail> {
	
	private static final long serialVersionUID = 1L;
	@ApiModelProperty(value = "推送角色（多个用逗号分隔）")
	private String roles;
	@ApiModelProperty(value = "推送人员（多个用逗号分隔）")
	private String users;
	@ApiModelProperty(value = "主表id")
	private String mainId;
	@ApiModelProperty(value = "主表key")
	private String mainKey;

	public String getMainKey() {
		return mainKey;
	}

	public void setMainKey(String mainKey) {
		this.mainKey = mainKey;
	}

	public SwmAlarmConfigDetail() {
		this(null);
	}
	
	public SwmAlarmConfigDetail(String id){
		super(id);
	}

	public String getRoles() {
		return roles;
	}

	public void setRoles(String roles) {
		this.roles = roles;
	}
	
	public String getUsers() {
		return users;
	}

	public void setUsers(String users) {
		this.users = users;
	}
	
	@Size(min=0, max=64, message="主表id长度不能超过 64 个字符")
	public String getMainId() {
		return mainId;
	}

	public void setMainId(String mainId) {
		this.mainId = mainId;
	}
	
}