package cn.iocoder.yudao.module.swm.dal.dataobject;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * 产线 DO
 * 表: fms_prod_line
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@TableName("fms_prod_line")
public class FmsProdLineDO extends SwmBaseDO {

    /** 产线名称 */
    private String name;
    /** 产线编码 */
    private String code;
    /** 所属车间 */
    private String workshop;
    /** 企业编码 */
    private String corpCode;

}
