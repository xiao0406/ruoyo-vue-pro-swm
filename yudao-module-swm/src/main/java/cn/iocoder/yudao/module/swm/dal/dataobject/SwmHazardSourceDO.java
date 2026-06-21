package cn.iocoder.yudao.module.swm.dal.dataobject;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 危险源管理 DO
 *
 * 迁移自 JeeSite: com.jeesite.modules.entity.SwmHazardSource
 * 表: swm_hazard_source
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@TableName("swm_hazard_source")
public class SwmHazardSourceDO extends SwmBaseDO {

    private String hazardName;
    private String hazardCategory;
    private String location;
    private String beaconIdentifier;
    private String isPatrolIncluded;
    private String patrolRecordSummary;
    private LocalDateTime registrationTime;
    private String hazardStatus;
    private Integer frequencyDays;
    private String responsiblePersonId;
    private String responsiblePerson;
    private LocalDateTime firstInspectionTime;
    private String voiceTemplateId;
    private String beaconTag;
    private String isDraft;
    private String filterIdentityCard;
    private String filterPersonnel;

    // ===== 非数据库字段 =====
    @TableField(exist = false)
    private String[] beaconIdentifiers;
    @TableField(exist = false)
    private String beaconIdentifierText;
    @TableField(exist = false)
    private List<String> beaconIdentifierSearchList;

}
