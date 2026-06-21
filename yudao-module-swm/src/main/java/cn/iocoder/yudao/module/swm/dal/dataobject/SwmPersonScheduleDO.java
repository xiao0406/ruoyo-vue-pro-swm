package cn.iocoder.yudao.module.swm.dal.dataobject;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * 人员排班 DO
 *
 * 迁移自 JeeSite: com.jeesite.modules.swm.entity.SwmPersonSchedule
 * 表: swm_person_schedule
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@TableName("swm_person_schedule")
public class SwmPersonScheduleDO extends SwmBaseDO {

    private String personName;
    private String month;
    /** 班次（枚举 SwmEnums.ShiftTypeEnum） */
    private String classes;
    private String idCard;
    private String employeeId;

    // ===== 非数据库字段 =====
    @TableField(exist = false)
    private String classesText;
    @TableField(exist = false)
    private String workGroupName;
    @TableField(exist = false)
    private String personId;
    @TableField(exist = false)
    private String organization;
    @TableField(exist = false)
    private String workshop;
    @TableField(exist = false)
    private String process;
    @TableField(exist = false)
    private String deviceId;
    @TableField(exist = false)
    private String personType;

}
