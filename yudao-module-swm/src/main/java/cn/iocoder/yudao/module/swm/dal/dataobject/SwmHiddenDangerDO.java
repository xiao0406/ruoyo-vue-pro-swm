package cn.iocoder.yudao.module.swm.dal.dataobject;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * 隐患排查 DO
 *
 * 迁移自 JeeSite: com.jeesite.modules.swm.entity.SwmHiddenDanger
 * 表: swm_hidden_danger
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@TableName("swm_hidden_danger")
public class SwmHiddenDangerDO extends SwmBaseDO {

    private String dangerName;
    private String location;
    private String inspectionPlanId;
    /** 是否布设信标 */
    private String isBeaconDeployed;
    /** 是否已处置 */
    private String isHandled;

}
