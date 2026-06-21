package cn.iocoder.yudao.module.swm.dal.dataobject;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * 字典数据 DO
 * 表: swm_dict_data
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@TableName("swm_dict_data")
public class SwmDictDataDO extends SwmBaseDO {
    private String dictCode;
    private String dictLabel;
    private String dictValue;
    private String dictIcon;
    private String dictType;
    private String isSys;
    private String description;
    private String cssStyle;
    private String cssClass;
}
