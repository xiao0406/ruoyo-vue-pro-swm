package cn.iocoder.yudao.module.swm.controller.admin.beaconcolor.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "信标颜色配置分页查询 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class SwmBeaconColorConfigPageReqVO extends PageParam {

    @Schema(description = "名称")
    private String name;

    @Schema(description = "颜色")
    private String color;

}
