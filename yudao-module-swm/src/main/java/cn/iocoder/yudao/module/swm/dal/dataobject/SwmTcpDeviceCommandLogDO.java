package cn.iocoder.yudao.module.swm.dal.dataobject;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * TCP 设备指令日志 DO（TDengine 专用）
 *
 * 迁移自 JeeSite: com.jeesite.modules.swm.entity.SwmTcpDeviceCommandLog
 * 此类不使用 SwmBaseDO，因为它映射到 TDengine 表而非 MySQL 表
 */
@Data
public class SwmTcpDeviceCommandLogDO {

    private LocalDateTime time;
    private String sendMessage;
    private String sendStatus;
    private String errorMessage;
    private String identityCard;
    private String personName;
    private String deviceId;

    // ===== 查询条件 =====
    @TableField(exist = false)
    private LocalDateTime startTime;
    @TableField(exist = false)
    private LocalDateTime endTime;
    @TableField(exist = false)
    private String sortOrder;

}
