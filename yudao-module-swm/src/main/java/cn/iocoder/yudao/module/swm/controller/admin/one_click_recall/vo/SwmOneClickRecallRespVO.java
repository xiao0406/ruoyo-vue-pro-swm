package cn.iocoder.yudao.module.swm.controller.admin.one_click_recall.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "一键召回 Response VO")
@Data
public class SwmOneClickRecallRespVO {

    @Schema(description = "主键")
    private String id;

    @Schema(description = "语音文本")
    private String voiceText;

    @Schema(description = "模板名称")
    private String templateName;

    @Schema(description = "模板内容")
    private String templateContent;

    @Schema(description = "撤离方案")
    private String evacuationPlan;

    @Schema(description = "疏散人数")
    private Integer evacueeCount;

    @Schema(description = "召回成功数")
    private Integer recallSuccessCount;

    @Schema(description = "召回失败数")
    private Integer recallFailCount;

    @Schema(description = "疏散人员列表")
    private String evacueeList;

    @Schema(description = "推送方式")
    private String pushMethod;

    @Schema(description = "召回频率")
    private Integer recallFrequency;

    @Schema(description = "召回次数")
    private Integer recallCount;

    @Schema(description = "召回时间")
    private LocalDateTime recallTime;

    @Schema(description = "召回结果")
    private String recallResult;

    @Schema(description = "设备列表")
    private String deviceList;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    @Schema(description = "创建者")
    private String creator;

    @Schema(description = "更新者")
    private String updater;

    @Schema(description = "状态")
    private String status;

    @Schema(description = "备注")
    private String remarks;

}
