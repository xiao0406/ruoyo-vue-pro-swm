package cn.iocoder.yudao.module.swm.dal.dataobject;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;

/**
 * SWM 模块基础实体对象
 *
 * 适配 JeeSite 数据库表结构：
 * 1. 审计字段名映射：create_by/create_date/update_by/update_date
 * 2. 逻辑删除：使用 status 字段（'0'=正常, '1'=删除）
 * 3. 租户：继承 TenantBaseDO 的 tenantId 字段（数据库列 tenant_id）
 *    legacy enterprise fields are migrated to tenant_id
 *
 * @author swm
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class SwmBaseDO extends TenantBaseDO {

    /**
     * 主键 ID
     * JeeSite 使用 varchar(64)，部分为雪花 ID、部分为 UUID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private String id;

    /**
     * 创建时间
     * 覆盖 BaseDO 的 createTime，映射到 JeeSite 的 create_date 列
     */
    @TableField(value = "create_date", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 最后更新时间
     * 覆盖 BaseDO 的 updateTime，映射到 JeeSite 的 update_date 列
     */
    @TableField(value = "update_date", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 创建者
     * 覆盖 BaseDO 的 creator，映射到 JeeSite 的 create_by 列
     */
    @TableField(value = "create_by", fill = FieldFill.INSERT)
    private String creator;

    /**
     * 更新者
     * 覆盖 BaseDO 的 updater，映射到 JeeSite 的 update_by 列
     */
    @TableField(value = "update_by", fill = FieldFill.INSERT_UPDATE)
    private String updater;

    /**
     * 逻辑删除字段
     * 覆盖 BaseDO 的 deleted，设置 exist = false 避免 SQL 中出现 deleted 列
     * SWM 使用 status 字段进行逻辑删除（见下方 status 字段）
     */
    @TableField(exist = false)
    private Boolean deleted;

    /**
     * 状态（JeeSite 逻辑删除字段）
     * '0' = 正常/未删除，'1' = 已删除
     * JeeSite 查询条件: WHERE status = '0'
     */
    @TableLogic(value = "0", delval = "1")
    private String status;

    /**
     * 备注
     */
    private String remarks;

}
