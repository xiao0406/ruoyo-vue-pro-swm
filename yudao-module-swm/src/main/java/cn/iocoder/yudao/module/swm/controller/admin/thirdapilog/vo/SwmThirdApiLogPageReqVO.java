package cn.iocoder.yudao.module.swm.controller.admin.thirdapilog.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "第三方API日志分页查询 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class SwmThirdApiLogPageReqVO extends PageParam {

    @Schema(description = "业务类型")
    private String businessType;

    @Schema(description = "请求URL")
    private String requestUrl;

    @Schema(description = "HTTP方法")
    private String httpMethod;

    @Schema(description = "执行状态")
    private String executeStatus;

}
