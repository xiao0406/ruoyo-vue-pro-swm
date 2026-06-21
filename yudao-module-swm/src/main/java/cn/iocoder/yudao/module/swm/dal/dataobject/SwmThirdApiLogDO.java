package cn.iocoder.yudao.module.swm.dal.dataobject;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;

/**
 * 第三方 API 日志 DO
 * 表: swm_third_api_log
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@TableName("swm_third_api_log")
public class SwmThirdApiLogDO extends SwmBaseDO {
    private String businessType;
    private String httpMethod;
    private String requestUrl;
    private String requestParam;
    private String responseParam;
    private LocalDateTime requestTime;
    private LocalDateTime responseTime;
    private Long duration;
    private Integer httpStatus;
    private String executeStatus;
    private String exceptionInfo;
}
