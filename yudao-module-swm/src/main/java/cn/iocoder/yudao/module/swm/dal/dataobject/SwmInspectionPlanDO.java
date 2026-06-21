package cn.iocoder.yudao.module.swm.dal.dataobject;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;

/**
 * 巡检计划 DO
 *
 * 迁移自 JeeSite: com.jeesite.modules.swm.entity.SwmInspectionPlan
 * 表: swm_inspection_plan
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@TableName("swm_inspection_plan")
public class SwmInspectionPlanDO extends SwmBaseDO {

    private String planCode;
    private String planName;
    private Integer frequencyDays;
    private String inspectionType;
    private String responsiblePersonId;
    private String responsiblePerson;
    private LocalDateTime firstInspectionTime;
    private String hazardSourceId;
    private String hazardSourceName;
    /** 巡检状态（枚举 SwmEnums.PlanStatusEnum） */
    private String planStatus;

    // ===== 非数据库字段 =====
    @TableField(exist = false)
    private String[] hazardSourceIds;
    @TableField(exist = false)
    private String[] hazardSourceNames;

}
