package cn.iocoder.yudao.module.swm.dal.dataobject;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;

/**
 * 原始消息日志 DO（TDengine 专用，不做 MySQL 映射）
 *
 * 迁移自 JeeSite: com.jeesite.modules.swm.entity.SwmRawMessageLog
 * 此类不使用 SwmBaseDO，因为它映射到 TDengine 表而非 MySQL 表
 */
@Data
public class SwmRawMessageLogDO {

    private LocalDateTime time;
    private String sessionId;
    private Long messageTime;
    private String messageContent;
    private Integer originalLength;
    private Integer isTruncated;
    private String deviceId;

    // ===== 查询条件 =====
    @TableField(exist = false)
    private LocalDateTime startTime;
    @TableField(exist = false)
    private LocalDateTime endTime;
    @TableField(exist = false)
    private String sortOrder;

}
