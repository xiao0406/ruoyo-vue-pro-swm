package cn.iocoder.yudao.module.swm.dal.dataobject;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 安全帽配置 DO
 * 表: swm_helmet_config
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("swm_helmet_config")
public class SwmHelmetConfigDO extends TenantBaseDO {

    private String id;
    /** 配置名称 */
    private String configName;
    /** 配置类型 */
    private String configType;
    /** 配置值 */
    private String configValue;
    /** 状态 */
    private String status;
    /** 备注 */
    private String remarks;
}
