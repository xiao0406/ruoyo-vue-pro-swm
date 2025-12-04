package com.jeesite.modules.swm.entity;

import java.math.BigDecimal;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import javax.validation.constraints.Size;
import com.jeesite.common.entity.BaseEntity;

import com.jeesite.common.entity.DataEntity;
import io.swagger.annotations.*;
import com.jeesite.common.mybatis.annotation.Column;
import com.jeesite.common.mybatis.annotation.Table;
import com.jeesite.common.mybatis.mapper.query.QueryType;
import lombok.Data;

/**
 * 安全帽：ai日报表Entity
 * @author wxy
 * @version 2025-11-27
 */
@Table(name="swm_dify", alias="a", label="安全帽：ai日报表信息", columns={
		@Column(name="id", attrName="id", label="唯一标识", isPK=true),
		@Column(name="date", attrName="date", label="请求时间", isUpdateForce=true),
		@Column(name="project_name", attrName="projectName", label="项目名称", queryType=QueryType.LIKE),
		@Column(name="safety_index", attrName="safetyIndex", label="安全合规指数", isUpdateForce=true),
		@Column(name="worker_index", attrName="workerIndex", label="人员活动指数", isUpdateForce=true),
		@Column(name="team_actual_hours", attrName="teamActualHours", label="班组的有效作业时长", isUpdateForce=true),
		@Column(name="text", attrName="text", label="文本"),
		@Column(includeEntity=DataEntity.class),
		@Column(name="manufacture", attrName="manufacture", label="制造单位"),
		@Column(includeEntity= BaseEntity.class),
	}, orderBy="a.update_date DESC"
)
@ApiModel(value = "SwmDify对象", description = "安全帽：ai日报表Entity")
@Data
public class SwmDify extends DataEntity<SwmDify> {
	
	private static final long serialVersionUID = 1L;
	@ApiModelProperty(value = "请求时间")
	private Date date;
	@ApiModelProperty(value = "项目名称")
	private String projectName;
	@ApiModelProperty(value = "安全合规指数")
	private Long safetyIndex;
	@ApiModelProperty(value = "人员活动指数")
	private Long workerIndex;
	@ApiModelProperty(value = "班组的有效作业时长")
	private Long teamActualHours;
	@ApiModelProperty(value = "文本")
	private String text;
	@ApiModelProperty(value = "制造单位")
	private String manufacture;

	private Date startDate;
	private Date endDate;
	
}