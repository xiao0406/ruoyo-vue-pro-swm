package cn.iocoder.yudao.module.swm.controller.admin.voice_template.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "语音模板 Response VO")
@Data
public class SwmVoiceTemplateRespVO {

    @Schema(description = "主键")
    private String id;

    @Schema(description = "模板名称")
    private String templateName;

    @Schema(description = "模板编码")
    private String templateCode;

    @Schema(description = "模板内容")
    private String content;

    @Schema(description = "语音文本")
    private String voiceText;

    @Schema(description = "语言")
    private String language;

    @Schema(description = "推送方式")
    private String pushMethod;

    @Schema(description = "推送频率")
    private String pushFrequency;

    @Schema(description = "模板ID")
    private String templateId;

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
