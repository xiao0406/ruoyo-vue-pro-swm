package cn.iocoder.yudao.module.swm.dal.dataobject;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;

/**
 * 一键召回 DO
 *
 * 迁移自 JeeSite: com.jeesite.modules.swm.entity.SwmOneClickRecall
 * 表: swm_oneclick_recall
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@TableName("swm_oneclick_recall")
public class SwmOneClickRecallDO extends SwmBaseDO {

    private String voiceText;
    private String templateName;
    private String templateContent;
    /** 撤离方案（枚举 SwmEnums.EvacuationPlanEnum） */
    private String evacuationPlan;
    private Integer evacueeCount;
    private Integer recallSuccessCount;
    private Integer recallFailCount;
    private String evacueeList;
    /** 推送方式（枚举 SwmEnums.PushMethodEnum） */
    private String pushMethod;
    private Integer recallFrequency;
    private Integer recallCount;
    private LocalDateTime recallTime;
    /** 召回结果（枚举 SwmEnums.RecallResultEnum） */
    private String recallResult;
    private String deviceList;

    // ===== 非数据库字段 =====
    @TableField(exist = false)
    private String kltDeviceList;
    @TableField(exist = false)
    private String zTDeviceList;

}
