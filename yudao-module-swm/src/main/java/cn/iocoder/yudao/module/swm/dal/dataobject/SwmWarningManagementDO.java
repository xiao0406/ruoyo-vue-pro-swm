package cn.iocoder.yudao.module.swm.dal.dataobject;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 预警管理 DO
 *
 * 迁移自 JeeSite: com.jeesite.modules.swm.entity.SwmWarningManagement
 * 表: swm_warning_management
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@TableName("swm_warning_management")
public class SwmWarningManagementDO extends SwmBaseDO {

    private String personName;
    /** 预警性质（枚举 SwmEnums.WarningTypeEnum） */
    private String warningType;
    private String warningContent;
    private LocalDateTime warningTime;
    private String alarmRecord;
    private LocalDateTime alarmTime;
    private String triggerReason;
    private String handler;
    private LocalDateTime handleTime;
    private String handleProcess;
    /** 处置状态（枚举 SwmEnums.HandleStatusEnum） */
    private String handleStatus;
    private String attachment;
    private Long disposalDuration;
    private String deviceId;
    private String idCard;
    private String frontAlarm;
    /** 告警类型（匹配 alarm_config.alarm_key） */
    private String type;
    private String x;
    private String y;
    private String hazardCategory;
    private String location;
    private String area;

    // ===== 非数据库字段 =====
    @TableField(exist = false)
    private String workGroupName;
    @TableField(exist = false)
    private boolean excludeSOS;
    @TableField(exist = false)
    private boolean excludeAttendance;
    @TableField(exist = false)
    private boolean excludeGateEntry;
    @TableField(exist = false)
    private Date beginAlarmTime;
    @TableField(exist = false)
    private Date endAlarmTime;
    @TableField(exist = false)
    private String personType;
    @TableField(exist = false)
    private List<String> warningContentList;
    @TableField(exist = false)
    private Date startDate;
    @TableField(exist = false)
    private Date endDate;
    @TableField(exist = false)
    private boolean isMasterDataAlarm;
    @TableField(exist = false)
    private Map<String, Object> extraData;

}
