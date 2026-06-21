package cn.iocoder.yudao.module.swm.dal.dataobject;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * 报警灯配置 DO
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SwmAlarmLightConfigDO extends TenantBaseDO {

    private String id;
    /** 报警灯ID */
    private String lightId;
    /** 报警配置ID */
    private String alarmConfigId;
    /** 语音模板ID */
    private String voiceTemplateId;
    /** 状态 */
    private String status;
    /** 备注 */
    private String remarks;

    // 关联查询字段
    /** 报警名称（关联查询） */
    private String alarmName;
    /** 模板名称（关联查询） */
    private String templateName;
}
