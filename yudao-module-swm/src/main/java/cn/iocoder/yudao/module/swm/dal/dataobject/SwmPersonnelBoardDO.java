package cn.iocoder.yudao.module.swm.dal.dataobject;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.math.BigDecimal;
import java.util.List;

/**
 * 人员看板 DO
 *
 * 迁移自 JeeSite: com.jeesite.modules.swm.entity.SwmPersonnelBoard
 * 表: swm_personnel_board
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@TableName("swm_personnel_board")
public class SwmPersonnelBoardDO extends SwmBaseDO {

    private String name;
    private String organization;
    private String workshop;
    private String process;
    private String team;
    /** 工作状态（枚举 SwmEnums.WorkStatusEnum） */
    private String workStatus;
    private String deviceId;
    /** 安全帽状态（枚举 SwmEnums.HelmetStatusEnum） */
    private String helmetStatus;
    /** 人员状态（枚举 SwmEnums.PersonnelStatusEnum） */
    private String personnelStatus;
    private Integer attendanceCount;
    private BigDecimal workingHours;
    private BigDecimal idleHours;
    /**
     * 当前 swm_personnel_board 表没有 id_card 字段，保留该属性仅用于接口兼容。
     */
    @TableField(exist = false)
    private String idCard;

    // ===== 非数据库字段 =====
    @TableField(exist = false)
    private String timeType;
    @TableField(exist = false)
    private String timeValue;
    @TableField(exist = false)
    private List<String> deviceIds;
    @TableField(exist = false)
    private Integer leisureCount;
    @TableField(exist = false)
    private Integer leisureDurationMin;

}
