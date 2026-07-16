package cn.iocoder.yudao.module.iot.dal.dataobject;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * 一键召回 DO（IoT 侧）
 * 表: iot_one_key_recall
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@TableName("iot_one_key_recall")
public class OneKeyRecallDO extends IotBaseDO {
    private String voiceText;
    private String templateName;
    private String templateContent;
    private String evacuationPlan;
    private Integer evacueeCount;
    private Integer recallSuccessCount;
    private Integer recallFailCount;
    private String evacueeList;
    private String pushMethod;
    private Integer recallFrequency;
    private Integer recallCount;
    private java.time.LocalDateTime recallTime;
    private String recallResult;
    private String deviceList;
}
