package cn.iocoder.yudao.module.swm.dal.dataobject;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;

/**
 * 隐患处置 DO
 * 表: swm_danger_disposal
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@TableName("swm_danger_disposal")
public class SwmDangerDisposalDO extends SwmBaseDO {
    private String hiddenDangerId;
    private String dangerName;
    private String location;
    private LocalDateTime disposalTime;
    private String disposalPlan;
    private String disposalContent;
    private String disposalMethod;
    private String disposalStatus;
    private String disposalUser;
    private String attachment;
}
