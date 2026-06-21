package cn.iocoder.yudao.module.swm.dal.dataobject;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;

/**
 * 巡检单 DO
 *
 * 迁移自 JeeSite: com.jeesite.modules.swm.entity.SwmInspectionList
 * 表: swm_inspection_list
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@TableName("swm_inspection_list")
public class SwmInspectionListDO extends SwmBaseDO {

    private String planId;
    private String planName;
    private String inspectionType;
    private String inspectorId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String attachmentPath;
    /** 巡检单状态（枚举 SwmEnums.InspectionListStatusEnum） */
    private String inspectionListStatus;

    // ===== 非数据库字段 =====
    /** 巡检人名称（通过 JOIN 查询获取） */
    @TableField(exist = false)
    private String inspector;
    @TableField(exist = false)
    private String planCode;

}
