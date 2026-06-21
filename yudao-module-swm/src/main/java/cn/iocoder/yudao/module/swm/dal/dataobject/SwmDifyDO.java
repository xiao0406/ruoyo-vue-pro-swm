package cn.iocoder.yudao.module.swm.dal.dataobject;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * AI Dify 配置 DO
 * 表: swm_dify
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@TableName("swm_dify")
public class SwmDifyDO extends SwmBaseDO {

    private String difyName;
    private String difyType;
    private String difyContent;
    private String difyStatus;
    private String reportDate;
    private BigDecimal safetyIndex;
    private BigDecimal workerIndex;
    private BigDecimal attendanceIndex;
    private String reportContent;
    private LocalDateTime reportTime;

}
