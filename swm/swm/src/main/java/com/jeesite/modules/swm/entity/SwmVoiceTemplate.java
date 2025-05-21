/**
 * 语音模板表实体类
 * @author auto
 * @date 2024-05-29
 */
package com.jeesite.modules.swm.entity;

import com.jeesite.common.entity.DataEntity;
import com.jeesite.common.mybatis.annotation.Column;
import com.jeesite.common.mybatis.annotation.Table;
import com.jeesite.common.mybatis.mapper.query.QueryType;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;

/**
 * 语音模板表实体类
 * 
 * @author auto
 */
@Table(name = "swm_voice_template", alias = "a", columns = {
        @Column(name = "id", attrName = "id", label = "主键ID", isPK = true),
        @Column(name = "template_name", attrName = "templateName", label = "模板名称", queryType = QueryType.LIKE),
        @Column(name = "template_code", attrName = "templateCode", label = "模板代码", isUpdateForce = true),
        @Column(name = "content", attrName = "content", label = "语音内容"),
        @Column(name = "language", attrName = "language", label = "语言"),
        @Column(name = "status", attrName = "status", label = "状态"),
        @Column(includeEntity = DataEntity.class)
}, orderBy = "a.update_date DESC")
public class SwmVoiceTemplate extends DataEntity<SwmVoiceTemplate> {

    private static final long serialVersionUID = 1L;
    
    private String templateName;    // 模板名称
    private String templateCode;    // 模板代码
    private String content;         // 语音内容
    private String language;        // 语言
    
    public SwmVoiceTemplate() {
        this(null);
    }

    public SwmVoiceTemplate(String id) {
        super(id);
    }
    
    @NotBlank(message = "模板名称不能为空")
    @Length(min = 0, max = 100, message = "模板名称不能超过100个字符")
    public String getTemplateName() {
        return templateName;
    }

    public void setTemplateName(String templateName) {
        this.templateName = templateName;
    }
    
    @NotBlank(message = "模板代码不能为空")
    @Length(min = 0, max = 50, message = "模板代码不能超过50个字符")
    public String getTemplateCode() {
        return templateCode;
    }

    public void setTemplateCode(String templateCode) {
        this.templateCode = templateCode;
    }
    
    @NotBlank(message = "语音内容不能为空")
    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
    
    @Length(min = 0, max = 20, message = "语言不能超过20个字符")
    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }
} 