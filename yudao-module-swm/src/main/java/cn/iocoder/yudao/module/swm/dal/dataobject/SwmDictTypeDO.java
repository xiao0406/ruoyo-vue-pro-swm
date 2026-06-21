package cn.iocoder.yudao.module.swm.dal.dataobject;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * 字典类型 DO
 * 表: swm_dict_type
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@TableName("swm_dict_type")
public class SwmDictTypeDO extends SwmBaseDO {
    private String dictName;
    private String dictType;
    private String isSys;
}
