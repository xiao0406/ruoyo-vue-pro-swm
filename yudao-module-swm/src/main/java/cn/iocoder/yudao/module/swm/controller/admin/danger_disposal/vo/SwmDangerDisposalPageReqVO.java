package cn.iocoder.yudao.module.swm.controller.admin.danger_disposal.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "隐患处置分页查询 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class SwmDangerDisposalPageReqVO extends PageParam {

    @Schema(description = "隐患名称")
    private String dangerName;

    @Schema(description = "位置")
    private String location;

    @Schema(description = "处置状态")
    private String disposalStatus;

}
