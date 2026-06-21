package cn.iocoder.yudao.module.swm.dal.dataobject;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * 岗位档案 DO
 * 表: fms_position_archive
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@TableName("fms_position_archive")
public class FmsPositionArchiveDO extends SwmBaseDO {

    /** 岗位名称 */
    private String name;
    /** 岗位编码 */
    private String code;
    /** 所属部门 */
    private String department;
    /** 企业编码 */
    private String corpCode;

}
