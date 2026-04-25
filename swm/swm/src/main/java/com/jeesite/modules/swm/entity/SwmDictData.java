package com.jeesite.modules.swm.entity;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import com.jeesite.common.entity.BaseEntity;
import com.jeesite.common.entity.Extend;

import com.jeesite.common.entity.DataEntity;
import com.jeesite.common.entity.TreeEntity;
import io.swagger.annotations.*;
import com.jeesite.common.mybatis.annotation.Column;
import com.jeesite.common.mybatis.annotation.Table;

/**
 * 字典数据表Entity
 * @author wxy
 * @version 2026-04-24
 */
@Table(name="swm_dict_data", alias="a", label="字典数据表信息", columns={
		@Column(name="dict_code", attrName="dictCode", label="字典编码", isPK=true),
		@Column(includeEntity=TreeEntity.class),
		@Column(name="dict_label", attrName="dictLabel", label="字典标签", isTreeName=true),
		@Column(name="dict_value", attrName="dictValue", label="字典键值"),
		@Column(name="dict_icon", attrName="dictIcon", label="字典图标"),
		@Column(name="dict_type", attrName="dictType", label="字典类型"),
		@Column(name="is_sys", attrName="isSys", label="系统内置", comment="系统内置（1是 0否）"),
		@Column(name="description", attrName="description", label="字典描述"),
		@Column(name="css_style", attrName="cssStyle", label="css样式", comment="css样式（如：color:red)"),
		@Column(name="css_class", attrName="cssClass", label="css类名", comment="css类名（如：red）"),
		@Column(includeEntity=DataEntity.class),
		@Column(includeEntity=BaseEntity.class),
		@Column(includeEntity=Extend.class, attrName="extend"),
	}, orderBy="a.tree_sorts, a.dict_code"
)
@ApiModel(value = "SwmDictData对象", description = "字典数据表Entity")
public class SwmDictData extends TreeEntity<SwmDictData> {
	
	private static final long serialVersionUID = 1L;
	@ApiModelProperty(value = "字典编码")
	private String dictCode;
	@ApiModelProperty(value = "字典标签")
	private String dictLabel;
	@ApiModelProperty(value = "字典键值")
	private String dictValue;
	@ApiModelProperty(value = "字典图标")
	private String dictIcon;
	@ApiModelProperty(value = "字典类型")
	private String dictType;
	@ApiModelProperty(value = "系统内置（1是 0否）")
	private String isSys;
	@ApiModelProperty(value = "字典描述")
	private String description;
	@ApiModelProperty(value = "css样式（如：color:red)")
	private String cssStyle;
	@ApiModelProperty(value = "css类名（如：red）")
	private String cssClass;
	@ApiModelProperty(value = "扩展字段")
	private Extend extend;
	
	public SwmDictData() {
		this(null);
	}
	
	public SwmDictData(String id){
		super(id);
	}
	
	@Override
	public SwmDictData getParent() {
		return parent;
	}

	@Override
	public void setParent(SwmDictData parent) {
		this.parent = parent;
	}
	
	public String getDictCode() {
		return dictCode;
	}

	public void setDictCode(String dictCode) {
		this.dictCode = dictCode;
	}
	
	@NotBlank(message="字典标签不能为空")
	@Size(min=0, max=100, message="字典标签长度不能超过 100 个字符")
	public String getDictLabel() {
		return dictLabel;
	}

	public void setDictLabel(String dictLabel) {
		this.dictLabel = dictLabel;
	}
	
	@NotBlank(message="字典键值不能为空")
	@Size(min=0, max=100, message="字典键值长度不能超过 100 个字符")
	public String getDictValue() {
		return dictValue;
	}

	public void setDictValue(String dictValue) {
		this.dictValue = dictValue;
	}
	
	@Size(min=0, max=100, message="字典图标长度不能超过 100 个字符")
	public String getDictIcon() {
		return dictIcon;
	}

	public void setDictIcon(String dictIcon) {
		this.dictIcon = dictIcon;
	}
	
	@NotBlank(message="字典类型不能为空")
	@Size(min=0, max=100, message="字典类型长度不能超过 100 个字符")
	public String getDictType() {
		return dictType;
	}

	public void setDictType(String dictType) {
		this.dictType = dictType;
	}
	
	@NotBlank(message="系统内置不能为空")
	@Size(min=0, max=1, message="系统内置长度不能超过 1 个字符")
	public String getIsSys() {
		return isSys;
	}

	public void setIsSys(String isSys) {
		this.isSys = isSys;
	}
	
	@Size(min=0, max=500, message="字典描述长度不能超过 500 个字符")
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
	
	@Size(min=0, max=500, message="css样式长度不能超过 500 个字符")
	public String getCssStyle() {
		return cssStyle;
	}

	public void setCssStyle(String cssStyle) {
		this.cssStyle = cssStyle;
	}
	
	@Size(min=0, max=500, message="css类名长度不能超过 500 个字符")
	public String getCssClass() {
		return cssClass;
	}

	public void setCssClass(String cssClass) {
		this.cssClass = cssClass;
	}
	
	public Extend getExtend() {
		return extend;
	}

	public void setExtend(Extend extend) {
		this.extend = extend;
	}
	
}