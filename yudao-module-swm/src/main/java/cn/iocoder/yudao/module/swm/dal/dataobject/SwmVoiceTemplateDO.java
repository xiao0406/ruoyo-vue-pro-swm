package cn.iocoder.yudao.module.swm.dal.dataobject;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * 语音模板 DO
 * 表: swm_voice_template
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@TableName("swm_voice_template")
public class SwmVoiceTemplateDO extends SwmBaseDO {
    private String templateName;
    private String templateCode;
    private String content;
    private String voiceText;
    private String language;
    private String pushMethod;
    private String pushFrequency;
    private String templateId;
}
