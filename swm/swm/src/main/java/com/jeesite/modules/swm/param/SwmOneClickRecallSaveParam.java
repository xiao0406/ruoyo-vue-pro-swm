package com.jeesite.modules.swm.param;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class SwmOneClickRecallSaveParam implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotBlank(message = "语音模板名称不能为空")
    @Length(min = 0, max = 100, message = "语音模板名称不能超过100个字符")
    private String templateName;     // 语音模板名称

    @NotBlank(message = "语音模板内容不能为空")
    private String templateContent;  // 语音模板内容

    @NotBlank(message = "撤离方案不能为空")
    private String evacuationPlan;
    @NotBlank(message = "推送方式不能为空")
    private String pushMethod;
    @NotNull(message = "推送频率不能为空")
    private Integer recallFrequency;
    @NotNull(message = "推送频率次数不能为空")
    private Integer recallCount;

    private String evacueeList;      // 撤离人员名单(JSON格式)

    private String recallResult;     // 召回结果

    private List<String> selectedTargets; // 撤离方案不为全员时，需要传。选中的树形节点

    private List<Map<String, Object>> originalTreeData; // 撤离方案不为全员时，需要传。原始树形数据
} 