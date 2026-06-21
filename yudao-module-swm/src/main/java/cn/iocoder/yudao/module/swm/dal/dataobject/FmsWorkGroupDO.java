package cn.iocoder.yudao.module.swm.dal.dataobject;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * 班组 DO
 * 表: fms_work_group
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@TableName("fms_work_group")
public class FmsWorkGroupDO extends SwmBaseDO {

    /** 班组名称 */
    private String name;
    /** 班组编码 */
    private String code;
    /** 所属车间 */
    private String workshop;
    /** 企业编码 */
    private String corpCode;

}
