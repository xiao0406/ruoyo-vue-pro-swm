package com.jeesite.modules.entity;

import javax.validation.constraints.Size;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.jeesite.common.entity.BaseEntity;

import com.jeesite.common.entity.DataEntity;
import io.swagger.annotations.*;
import com.jeesite.common.mybatis.annotation.Column;
import com.jeesite.common.mybatis.annotation.Table;
import com.jeesite.common.mybatis.mapper.query.QueryType;
import lombok.Data;

/**
 * 安全教育视频管理Entity
 * @author wxy
 * @version 2026-01-19
 */
@Table(name="swm_safety_file_manage", alias="a", label="安全教育视频管理信息", columns={
		@Column(name="id", attrName="id", label="主键", isPK=true),
		@Column(name="cover_url", attrName="coverUrl", label="视频封面url地址"),
		@Column(name="title", attrName="title", label="视频标题", queryType=QueryType.LIKE),
		@Column(name="type", attrName="type", label="视频分类,字典：swm_safety_type"),
		@Column(name="job_type", attrName="jobType", label="工种"),
		@Column(name="duration", attrName="duration", label="视频时长"),
		@Column(name="push_date", attrName="pushDate", label="推送日期", isUpdateForce=true),
		@Column(name="push_status", attrName="pushStatus", label="推送状态，字典swm_push_status"),
		@Column(name="file_url", attrName="fileUrl", label="文件url地址"),
		@Column(includeEntity=DataEntity.class),
		@Column(includeEntity=BaseEntity.class),
	}, orderBy="a.update_date DESC"
)
@Data
@ApiModel(value = "SwmSafetyFileManage对象", description = "安全教育视频管理Entity")
public class SwmSafetyFileManage extends DataEntity<SwmSafetyFileManage> {
	
	private static final long serialVersionUID = 1L;
	@ApiModelProperty(value = "视频封面url地址")
	private String coverUrl;
	@ApiModelProperty(value = "视频标题")
	private String title;
	@ApiModelProperty(value = "视频分类")
	private String type;
	@ApiModelProperty(value = "工种")
	private String jobType;
	@ApiModelProperty(value = "视频时长")
	private String duration;
	@ApiModelProperty(value = "推送日期")
	@JsonFormat(pattern = "yyyy-MM-dd")
	private Date pushDate;
	@ApiModelProperty(value = "推送状态")
	private String pushStatus;
	@ApiModelProperty(value = "文件url地址")
	private String fileUrl;

	@ApiModelProperty(value = "随机值，目的取消一级缓存")
	private Integer random;
	@ApiModelProperty(value = "开始时间")
	private Date startTime;
	@ApiModelProperty(value = "结束时间")
	private Date endTime;
	
	public SwmSafetyFileManage() {
		this(null);
	}
	
	public SwmSafetyFileManage(String id){
		super(id);
	}
	

}