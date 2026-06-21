package cn.iocoder.yudao.module.swm.dal.dataobject;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;

/**
 * 安全教育 DO
 *
 * 迁移自 JeeSite: com.jeesite.modules.swm.entity.SwmSafetyEducation
 * 表: swm_safety_education
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@TableName("swm_safety_education")
public class SwmSafetyEducationDO extends SwmBaseDO {

    private String theme;
    private String contentDescription;
    /** 安全教育类型（枚举 SwmEnums.EducationTypeEnum） */
    private String safetyEducationType;
    private LocalDateTime startTime;
    private String participants;
    private String participantsName;
    /** 状态（枚举 SwmEnums.SafetyEducationStatusEnum） */
    private String safetyStatus;
    /** 参与类型（枚举 SwmEnums.ParticipationTypeEnum） */
    private String participationType;
    private String attachmentUrl;

}
