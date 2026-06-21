package cn.iocoder.yudao.module.swm.controller.admin.hidden_danger.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import jakarta.validation.constraints.Size;

@Schema(description = "隐患排查分页查询 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class SwmHiddenDangerPageReqVO extends PageParam {

    @Schema(description = "隐患名称")
    @Size(max = 30, message = "隐患名称长度不能超过30个字符")
    private String dangerName;

    @Schema(description = "隐患位置")
    @Size(max = 100, message = "隐患位置长度不能超过100个字符")
    private String location;

    @Schema(description = "是否已处置")
    @Size(max = 30, message = "是否已处置长度不能超过30个字符")
    private String isHandled;

}
