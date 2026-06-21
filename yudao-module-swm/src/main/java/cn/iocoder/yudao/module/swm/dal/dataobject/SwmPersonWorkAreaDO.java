package cn.iocoder.yudao.module.swm.dal.dataobject;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * 人员工作区域 DO
 * 表: swm_person_work_area
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@TableName("swm_person_work_area")
public class SwmPersonWorkAreaDO extends SwmBaseDO {
    private String identityCard;
    private String personName;
    private String areaId;
    private String areaName;
}
