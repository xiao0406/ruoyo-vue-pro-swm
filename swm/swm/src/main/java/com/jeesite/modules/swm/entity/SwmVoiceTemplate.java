/**
 * 语音模板表实体类
 * @author zwf
 * @date 2024-05-29
 */
package com.jeesite.modules.swm.entity;

import com.jeesite.common.entity.BaseEntity;
import com.jeesite.common.entity.DataEntity;
import com.jeesite.common.mybatis.annotation.Column;
import com.jeesite.common.mybatis.annotation.Table;
import com.jeesite.common.mybatis.mapper.query.QueryType;
import com.jeesite.modules.sys.utils.DictUtils;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;

/**
 * 语音模板表实体类
 * 
 * @author zwf
 */
@Table(name = "swm_voice_template", alias = "a", columns = {
        @Column(name = "id", attrName = "id", label = "主键ID", isPK = true),
        @Column(name = "template_name", attrName = "templateName", label = "模板名称", queryType = QueryType.LIKE),
        @Column(name = "template_code", attrName = "templateCode", label = "模板代码", isUpdateForce = true),
        @Column(name = "content", attrName = "content", label = "语音内容"),
        @Column(name = "voice_text", attrName = "voiceText", label = "语音文字"),
        @Column(name = "language", attrName = "language", label = "语言"),
        @Column(name = "push_method", attrName = "pushMethod", label = "推送方式"),
        @Column(name = "push_frequency", attrName = "pushFrequency", label = "推送频次"),
        @Column(includeEntity = DataEntity.class),
        @Column(includeEntity= BaseEntity.class),
}, orderBy = "a.update_date DESC")
public class SwmVoiceTemplate extends DataEntity<SwmVoiceTemplate> {

    private static final long serialVersionUID = 1L;

    private String templateName; // 模板名称
    private String templateCode; // 模板代码
    private String content; // 语音内容
    private String voiceText; // 语音文字
    private String language; // 语言
    private String pushMethod; // 推送方式
    private String pushFrequency; // 推送频次

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
    
    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getVoiceText() {
        return voiceText;
    }

    public void setVoiceText(String voiceText) {
        this.voiceText = voiceText;
    }

    @Length(min = 0, max = 20, message = "语言不能超过20个字符")
    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    @Length(min = 0, max = 100, message = "推送方式不能超过100个字符")
    public String getPushMethod() {
        return pushMethod;
    }

    public void setPushMethod(String pushMethod) {
        this.pushMethod = pushMethod;
    }

    @Length(min = 0, max = 50, message = "推送频次不能超过50个字符")
    public String getPushFrequency() {
        return pushFrequency;
    }

    public void setPushFrequency(String pushFrequency) {
        this.pushFrequency = pushFrequency;
    }

    /**
     * 获取模板代码对应的字典文本
     * @return 模板代码文本
     */
    public String getTemplateCodeText() {
        return DictUtils.getDictLabel("voice_template_type", templateCode, "");
    }
}