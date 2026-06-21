package cn.iocoder.yudao.module.swm.controller.admin.voice_template.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;

@Schema(description = "语音模板新增/修改 Request VO")
@Data
public class SwmVoiceTemplateSaveReqVO {

    @Schema(description = "语音模板编号")
    private String id;

    @Schema(description = "模板名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "模板名称不能为空")
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

}
