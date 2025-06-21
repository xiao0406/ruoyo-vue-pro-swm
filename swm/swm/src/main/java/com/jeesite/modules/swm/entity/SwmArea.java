/**
 * 区域管理实体类
 * @author Shawn
 * @version 2025-06-22
 */
package com.jeesite.modules.swm.entity;

import com.jeesite.common.entity.DataEntity;
import com.jeesite.common.mybatis.annotation.Column;
import com.jeesite.common.mybatis.annotation.Table;
import com.jeesite.common.mybatis.mapper.query.QueryType;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;

/**
 * 区域管理实体类
 */
@Table(name = "swm_area", alias = "a", columns = {
        @Column(name = "id", attrName = "id", label = "主键ID", isPK = true),
        @Column(name = "area_name", attrName = "areaName", label = "区域名称", queryType = QueryType.LIKE),
        @Column(name = "area_type", attrName = "areaType", label = "区域类型"),
        @Column(name = "work_shop", attrName = "workShop", label = "车间ID"),
        @Column(name = "voice_prompt", attrName = "voicePrompt", label = "语音提示"),
        @Column(name = "file_path", attrName = "filePath", label = "文件路径"),
        @Column(includeEntity = DataEntity.class)
}, orderBy = "a.update_date DESC")
public class SwmArea extends DataEntity<SwmArea> {

    private static final long serialVersionUID = 1L;

    private String areaName; // 区域名称
    private String areaType; // 区域类型
    private String workShop; // 车间ID
    private String voicePrompt; // 语音提示
    private String filePath; // 文件路径

    public SwmArea() {
        this(null);
    }

    public SwmArea(String id) {
        super(id);
    }

    @NotBlank(message = "区域名称不能为空")
    @Length(min = 0, max = 100, message = "区域名称长度不能超过 100 个字符")
    public String getAreaName() {
        return areaName;
    }

    public void setAreaName(String areaName) {
        this.areaName = areaName;
    }

    @Length(min = 0, max = 50, message = "区域类型长度不能超过 50 个字符")
    public String getAreaType() {
        return areaType;
    }

    public void setAreaType(String areaType) {
        this.areaType = areaType;
    }

    @Length(min = 0, max = 64, message = "车间ID长度不能超过 64 个字符")
    public String getWorkShop() {
        return workShop;
    }

    public void setWorkShop(String workShop) {
        this.workShop = workShop;
    }

    @Length(min = 0, max = 500, message = "语音提示长度不能超过 500 个字符")
    public String getVoicePrompt() {
        return voicePrompt;
    }

    public void setVoicePrompt(String voicePrompt) {
        this.voicePrompt = voicePrompt;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }
}