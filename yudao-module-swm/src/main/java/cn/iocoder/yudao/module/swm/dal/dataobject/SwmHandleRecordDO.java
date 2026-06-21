package cn.iocoder.yudao.module.swm.dal.dataobject;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;

/**
 * 处置记录 DO
 * 表: swm_handle_record
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@TableName("swm_handle_record")
public class SwmHandleRecordDO extends SwmBaseDO {
    private String recordName;
    private String warningId;
    private String warningRecord;
    private LocalDateTime alarmTime;
    private String handler;
    private LocalDateTime handleTime;
    private String handleProcess;
    /** 处置状态（枚举 SwmEnums.HandleStatusEnum） */
    private String handleStatus;
    private String attachment;
}
