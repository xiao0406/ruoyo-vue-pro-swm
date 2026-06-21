package cn.iocoder.yudao.module.swm.dal.dataobject;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * 信标颜色配置 DO
 * 表: swm_beacon_color_config
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@TableName("swm_beacon_color_config")
public class SwmBeaconColorConfigDO extends SwmBaseDO {
    private String name;
    private String color;
}
