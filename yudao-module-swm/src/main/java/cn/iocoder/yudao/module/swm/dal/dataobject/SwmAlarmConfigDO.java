package cn.iocoder.yudao.module.swm.dal.dataobject;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * 告警配置 DO
 *
 * 迁移自 JeeSite: com.jeesite.modules.swm.entity.SwmAlarmConfig
 * 表: swm_alarm_config
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@TableName("swm_alarm_config")
public class SwmAlarmConfigDO extends SwmBaseDO {

    /** 报警名称 */
    private String alarmName;
    /** 报警唯一标识key */
    private String alarmKey;
    /** 是否报警 */
    private Integer enableAlarm;
    /** 是否弹窗确认 */
    private Integer needConfirm;
    /** 弹窗位置 */
    private String dialogPosition;
    /** 是否推送中建通（1是，0否） */
    private Integer isSendZjt;

}
