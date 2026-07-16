package cn.iocoder.yudao.module.iot.dal.dataobject;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;

/**
 * IoT module base entity.
 *
 * <p>Kept compatible with the migrated table columns while using RuoYi's
 * tenant base model instead of depending on SWM internal entities.</p>
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class IotBaseDO extends TenantBaseDO {

    @TableId
    private String id;

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
