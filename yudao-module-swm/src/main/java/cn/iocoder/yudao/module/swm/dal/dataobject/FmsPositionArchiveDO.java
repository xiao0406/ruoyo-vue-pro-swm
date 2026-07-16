package cn.iocoder.yudao.module.swm.dal.dataobject;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@TableName("fms_position_archive")
public class FmsPositionArchiveDO extends SwmBaseDO {

    private String name;
    private String code;
    private String department;
}
