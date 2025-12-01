/**
 * 一键召回记录表实体类
 * @author zwf
 * @date 2024-05-30
 */
package com.jeesite.modules.swm.entity;

import com.jeesite.common.entity.DataEntity;
import com.jeesite.common.mybatis.annotation.Column;
import com.jeesite.common.mybatis.annotation.Table;
import com.jeesite.common.mybatis.mapper.query.QueryType;
import com.jeesite.modules.sys.utils.DictUtils;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 一键召回记录表实体类
 * 
 * @author zwf
 */
@Table(name = "swm_oneclick_recall", alias = "a", columns = {
        @Column(name = "id", attrName = "id", label = "主键ID", isPK = true),
        @Column(name = "voice_text", attrName = "voiceText", label = "语音文字内容"),
        @Column(name = "template_name", attrName = "templateName", label = "语音模板名称", queryType = QueryType.LIKE),
        @Column(name = "template_content", attrName = "templateContent", label = "语音模板内容"),
        @Column(name = "evacuation_plan", attrName = "evacuationPlan", label = "撤离方案"),
        @Column(name = "evacuee_count", attrName = "evacueeCount", label = "撤离人员数量"),
        @Column(name = "recall_success_count", attrName = "recallSuccessCount", label = "成功人员数量"),
        @Column(name = "recall_fail_count", attrName = "recallFailCount", label = "失败人员数量"),
        @Column(name = "evacuee_list", attrName = "evacueeList", label = "撤离人员名单"),
        @Column(name = "push_method", attrName = "pushMethod", label = "推送方式"),
        @Column(name = "recall_frequency", attrName = "recallFrequency", label = "推送频率"),
        @Column(name = "recall_count", attrName = "recallCount", label = "推送次数"),
        @Column(name = "recall_time", attrName = "recallTime", label = "召回时间"),
        @Column(name = "recall_result", attrName = "recallResult", label = "召回结果"),
        @Column(name = "device_list", attrName = "deviceList", label = "推送列表"),
        @Column(includeEntity = DataEntity.class)
}, orderBy = "a.recall_time DESC")
public class SwmOneClickRecall extends DataEntity<SwmOneClickRecall> {

    private static final long serialVersionUID = 1L;
    
    /**
     * 召回结果枚举
     */
    public static class RecallResultEnum {
        /** 进行中 */
        public static final String IN_PROGRESS = "0";
        /** 成功 */
        public static final String SUCCESS = "1";
        /** 失败 */
        public static final String FAILED = "2";
        
        /**
         * 获取召回结果显示文本
         */
        public static String getText(String value) {
            return DictUtils.getDictLabel("recall_result_enum", value, "");
        }
    }

    /**
     * 撤离方案枚举
     */
    public static class EvacuationPlanEnum {
        /** 全体撤离 */
        public static final String ALL = "1";
        /** 按车间撤离 */
        public static final String BY_WORKSHOP = "2";
        /** 按班组撤离 */
        public static final String BY_TEAM = "3";
        /** 按区域撤离 */
        public static final String BY_AREA = "4";
        /** 按人员撤离 */
        public static final String BY_PERSON = "5";
        /** 按人员类型撤离 */
        public static final String BY_PERSON_TYPE = "6";

        /**
         * 获取撤离方案显示文本
         */
        public static String getText(String value) {
            return DictUtils.getDictLabel("evacuation_plan_enum", value, "");
        }
    }

    /**
     * 推送方式枚举
     */
    public static class PushMethodEnum {
        /** 设备 */
        public static final String DEVICE = "1";
        /** 短信 */
        public static final String SMS = "2";

        /**
         * 获取推送方式显示文本
         */
        public static String getText(String value) {
            return DictUtils.getDictLabel("push_method_enum", value, "");
        }
    }

    private String voiceText;
    private String templateName;     // 语音模板名称
    private String templateContent;  // 语音模板内容
    private String evacuationPlan;   // 撤离方案
    private Integer evacueeCount;    // 撤离人员数量
    private Integer recallSuccessCount;    // 成功人员数量
    private Integer recallFailCount;    // 失败人员数量
    private String evacueeList;      // 撤离人员名单(JSON格式)
    private String pushMethod;
    private Integer recallFrequency;
    private Integer recallCount;
    private Date recallTime;         // 召回时间
    private String recallResult;     // 召回结果
    private String deviceList;
    
    public SwmOneClickRecall() {
        this(null);
    }

    public SwmOneClickRecall(String id) {
        super(id);
    }

    public Integer getRecallSuccessCount() {
        return recallSuccessCount;
    }

    public void setRecallSuccessCount(Integer recallSuccessCount) {
        this.recallSuccessCount = recallSuccessCount;
    }

    public Integer getRecallFailCount() {
        return recallFailCount;
    }

    public void setRecallFailCount(Integer recallFailCount) {
        this.recallFailCount = recallFailCount;
    }

    public String getVoiceText() {
        return voiceText;
    }

    public void setVoiceText(String voiceText) {
        this.voiceText = voiceText;
    }

    public String getDeviceList() {
        return deviceList;
    }

    public void setDeviceList(String deviceList) {
        this.deviceList = deviceList;
    }

    public String getPushMethod() {
        return pushMethod;
    }

    public void setPushMethod(String pushMethod) {
        this.pushMethod = pushMethod;
    }

    public Integer getRecallFrequency() {
        return recallFrequency;
    }

    public void setRecallFrequency(Integer recallFrequency) {
        this.recallFrequency = recallFrequency;
    }

    public Integer getRecallCount() {
        return recallCount;
    }

    public void setRecallCount(Integer recallCount) {
        this.recallCount = recallCount;
    }

    @NotBlank(message = "语音模板名称不能为空")
    @Length(min = 0, max = 100, message = "语音模板名称不能超过100个字符")
    public String getTemplateName() {
        return templateName;
    }

    public void setTemplateName(String templateName) {
        this.templateName = templateName;
    }

    public String getTemplateContent() {
        return templateContent;
    }

    public void setTemplateContent(String templateContent) {
        this.templateContent = templateContent;
    }
    
    @NotBlank(message = "撤离方案不能为空")
    @Length(min = 0, max = 20, message = "撤离方案不能超过20个字符")
    public String getEvacuationPlan() {
        return evacuationPlan;
    }

    public void setEvacuationPlan(String evacuationPlan) {
        this.evacuationPlan = evacuationPlan;
    }

    /**
     * 获取撤离方案显示文本
     */
    public String getEvacuationPlanText() {
        return EvacuationPlanEnum.getText(evacuationPlan);
    }
    
    public Integer getEvacueeCount() {
        return evacueeCount;
    }

    public void setEvacueeCount(Integer evacueeCount) {
        this.evacueeCount = evacueeCount;
    }
    
    public String getEvacueeList() {
        return evacueeList;
    }

    public void setEvacueeList(String evacueeList) {
        this.evacueeList = evacueeList;
    }
    
    public Date getRecallTime() {
        return recallTime;
    }

    public void setRecallTime(Date recallTime) {
        this.recallTime = recallTime;
    }
    
    @Length(min = 0, max = 20, message = "召回结果不能超过20个字符")
    public String getRecallResult() {
        return recallResult;
    }

    public void setRecallResult(String recallResult) {
        this.recallResult = recallResult;
    }
    
    /**
     * 获取召回结果显示文本
     */
    public String getRecallResultText() {
        return RecallResultEnum.getText(recallResult);
    }
} 