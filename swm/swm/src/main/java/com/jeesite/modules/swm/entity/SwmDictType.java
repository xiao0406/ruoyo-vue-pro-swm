package com.jeesite.modules.swm.entity;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.List;
import com.jeesite.common.collect.ListUtils;

import com.jeesite.common.entity.BaseEntity;
import com.jeesite.common.entity.DataEntity;
import com.jeesite.modules.sys.entity.User;
import io.swagger.annotations.*;
import com.jeesite.common.mybatis.annotation.Column;
import com.jeesite.common.mybatis.annotation.Table;
import com.jeesite.common.mybatis.mapper.query.QueryType;
import lombok.Data;

/**
 * 字典类型表Entity
 * @author wxy
 * @version 2026-04-24
 */
@Table(name="swm_dict_type", alias="a", label="字典类型表信息", columns={
		@Column(name="id", attrName="id", label="编号", isPK=true),
		@Column(name="dict_name", attrName="dictName", label="字典名称", queryType=QueryType.LIKE),
		@Column(name="dict_type", attrName="dictType", label="字典类型"),
		@Column(name="is_sys", attrName="isSys", label="是否系统字典"),
		@Column(includeEntity=DataEntity.class),
		@Column(includeEntity= BaseEntity.class),
	}, orderBy="a.update_date DESC"
)
@ApiModel(value = "SwmDictType对象", description = "字典类型表Entity")
@Data
public class SwmDictType extends DataEntity<SwmDictType> {
	
	private static final long serialVersionUID = 1L;
	@ApiModelProperty(value = "字典名称")
	private String dictName;
	@ApiModelProperty(value = "字典类型")
	private String dictType;
	@ApiModelProperty(value = "是否系统字典")
	private String isSys;
//	private List<SwmDictData> swmDictDataList = ListUtils.newArrayList();		// 子表列表
	

}