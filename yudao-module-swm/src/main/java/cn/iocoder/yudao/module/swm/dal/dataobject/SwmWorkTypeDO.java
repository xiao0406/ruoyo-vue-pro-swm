package cn.iocoder.yudao.module.swm.dal.dataobject;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * 工种管理 DO
 * 表: swm_work_type
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@TableName("swm_work_type")
public class SwmWorkTypeDO extends SwmBaseDO {
    private String workType;
    private String workTypeCode;
    private String description;
}
