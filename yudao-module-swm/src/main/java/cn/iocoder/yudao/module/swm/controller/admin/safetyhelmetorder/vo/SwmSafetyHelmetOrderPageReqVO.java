package cn.iocoder.yudao.module.swm.controller.admin.safetyhelmetorder.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "安全帽订单分页查询 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class SwmSafetyHelmetOrderPageReqVO extends PageParam {

    @Schema(description = "人员ID")
    private String personId;

    @Schema(description = "设备ID")
    private String deviceId;

    @Schema(description = "订单状态")
    private String orderStatus;

    @Schema(description = "安全帽型号")
    private String helmetModel;

}
