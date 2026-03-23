package com.jeesite.modules.entity;

import javax.validation.constraints.Size;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.jeesite.common.entity.BaseEntity;

import com.jeesite.common.entity.DataEntity;
import io.swagger.annotations.*;
import com.jeesite.common.mybatis.annotation.Column;
import com.jeesite.common.mybatis.annotation.Table;
import lombok.Data;

/**
 * 视频培训记录（人员）Entity
 * @author wxy
 * @version 2026-01-19
 */
@Table(name="swm_safety_person_training", alias="a", label="视频培训记录（人员）信息", columns={
		@Column(name="id", attrName="id", label="主键id", isPK=true),
		@Column(name="safety_manage_id", attrName="safetyManageId", label="swm_safety_manage主键id"),
		@Column(name="identity_card", attrName="identityCard", label="身份证号码"),
		@Column(name="phone_number", attrName="phoneNumber", label="手机号码"),
		@Column(name="complete_status", attrName="completeStatus", label="完成状态，字典，swm_complete_status"),
		@Column(name="complete_date", attrName="completeDate", label="完成时间", isUpdateForce=true),
		@Column(name="progress", attrName="progress", label="视频进度"),
		@Column(includeEntity=DataEntity.class),
		@Column(includeEntity=BaseEntity.class),
	}, orderBy="a.update_date DESC"
)
@ApiModel(value = "SwmSafetyPersonTraining对象", description = "视频培训记录（人员）Entity")
@Data
public class SwmSafetyPersonTraining extends DataEntity<SwmSafetyPersonTraining> {
	
	private static final long serialVersionUID = 1L;
	@ApiModelProperty(value = "swm_safety_manage主键id")
	private String safetyManageId;
	@ApiModelProperty(value = "身份证号码")
	private String identityCard;
	@ApiModelProperty(value = "手机号码")
	private String phoneNumber;
	@ApiModelProperty(value = "完成状态")
	private String completeStatus;
	@ApiModelProperty(value = "完成时间")
	private Date completeDate;
	@ApiModelProperty(value = "视频进度")
	private String progress;
	@ApiModelProperty(value = "视频标题")
	private String title;
	@ApiModelProperty(value = "推送日期")
	@JsonFormat(pattern = "yyyy-MM-dd")
	private Date pushDate;
	@ApiModelProperty(value = "工种")
	private String jobType;
	@ApiModelProperty(value = "姓名")
	private String personName;
	@ApiModelProperty(value = "视频分类")
	private String type;
	@ApiModelProperty(value = "视频封面url地址")
	private String coverUrl;
	@ApiModelProperty(value = "文件url地址")
	private String fileUrl;
	@ApiModelProperty(value = "视频时长")
	private String duration;

	private String pushDateStr;



	private String company; // 所属单位
	private String department; // 所属车间
	private String prodLine; // 产线
	private String team; // 所属班组
	
	public SwmSafetyPersonTraining() {
		this(null);
	}
	
	public SwmSafetyPersonTraining(String id){
		super(id);
	}
	
	@Size(min=0, max=64, message="swm_safety_manage主键id长度不能超过 64 个字符")
	public String getSafetyManageId() {
		return safetyManageId;
	}

	public void setSafetyManageId(String safetyManageId) {
		this.safetyManageId = safetyManageId;
	}
	
	@Size(min=0, max=64, message="身份证号码长度不能超过 64 个字符")
	public String getIdentityCard() {
		return identityCard;
	}

	public void setIdentityCard(String identityCard) {
		this.identityCard = identityCard;
	}
	
	@Size(min=0, max=64, message="手机号码长度不能超过 64 个字符")
	public String getPhoneNumber() {
		return phoneNumber;
	}

	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}
	
	@Size(min=0, max=20, message="完成状态长度不能超过 20 个字符")
	public String getCompleteStatus() {
		return completeStatus;
	}

	public void setCompleteStatus(String completeStatus) {
		this.completeStatus = completeStatus;
	}
	
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	public Date getCompleteDate() {
		return completeDate;
	}

	public void setCompleteDate(Date completeDate) {
		this.completeDate = completeDate;
	}
	
	@Size(min=0, max=63, message="视频进度长度不能超过 63 个字符")
	public String getProgress() {
		return progress;
	}

	public void setProgress(String progress) {
		this.progress = progress;
	}
	
}