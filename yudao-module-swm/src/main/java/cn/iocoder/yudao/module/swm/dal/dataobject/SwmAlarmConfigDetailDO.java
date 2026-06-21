package cn.iocoder.yudao.module.swm.dal.dataobject;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * 告警配置详情 DO
 * 表: swm_alarm_config_detail
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@TableName("swm_alarm_config_detail")
public class SwmAlarmConfigDetailDO extends SwmBaseDO {
    private String roles;
    private String users;
    private String mainId;
}
