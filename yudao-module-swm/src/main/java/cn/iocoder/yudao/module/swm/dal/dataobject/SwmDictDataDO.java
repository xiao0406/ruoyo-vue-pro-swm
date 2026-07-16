package cn.iocoder.yudao.module.swm.dal.dataobject;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;

/**
 * 字典数据 DO
 * 表: swm_dict_data
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@TableName("swm_dict_data")
public class SwmDictDataDO extends TenantBaseDO {

    /** JeeSite dictionary tables use dict_code as their primary key. */
    @TableId("dict_code")
    private String id;

    /** API alias for the physical primary key; it is not a second column. */
    @TableField(exist = false)
    private String dictCode;

    private String dictLabel;
    private String dictValue;
    private String dictIcon;
    private String dictType;
    private String isSys;
    private String description;
    private String cssStyle;
    private String cssClass;

    @TableField(value = "create_date", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(value = "update_date", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableField(value = "create_by", fill = FieldFill.INSERT)
    private String creator;

    @TableField(value = "update_by", fill = FieldFill.INSERT_UPDATE)
    private String updater;

    @TableField(exist = false)
    private Boolean deleted;

    @TableLogic(value = "0", delval = "1")
    private String status;

    private String remarks;
}
