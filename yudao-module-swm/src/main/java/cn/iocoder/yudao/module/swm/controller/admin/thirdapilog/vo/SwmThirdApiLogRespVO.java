package cn.iocoder.yudao.module.swm.controller.admin.thirdapilog.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "第三方API日志 Response VO")
@Data
public class SwmThirdApiLogRespVO {

    @Schema(description = "主键")
    private String id;

    @Schema(description = "业务类型")
    private String businessType;

    @Schema(description = "HTTP方法")
    private String httpMethod;

    @Schema(description = "请求URL")
    private String requestUrl;

    @Schema(description = "请求参数")
    private String requestParam;

    @Schema(description = "响应参数")
    private String responseParam;

    @Schema(description = "请求时间")
    private LocalDateTime requestTime;

    @Schema(description = "响应时间")
    private LocalDateTime responseTime;

    @Schema(description = "耗时(ms)")
    private Long duration;

    @Schema(description = "HTTP状态码")
    private Integer httpStatus;

    @Schema(description = "执行状态")
    private String executeStatus;

    @Schema(description = "异常信息")
    private String exceptionInfo;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    @Schema(description = "创建者")
    private String creator;

    @Schema(description = "更新者")
    private String updater;

    @Schema(description = "状态")
    private String status;

    @Schema(description = "备注")
    private String remarks;

}
