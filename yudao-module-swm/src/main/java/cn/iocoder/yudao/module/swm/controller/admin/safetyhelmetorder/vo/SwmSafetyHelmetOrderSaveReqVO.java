package cn.iocoder.yudao.module.swm.controller.admin.safetyhelmetorder.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;

@Schema(description = "安全帽订单新增/修改 Request VO")
@Data
public class SwmSafetyHelmetOrderSaveReqVO {

    @Schema(description = "安全帽订单编号")
    private String id;

    @Schema(description = "人员ID")
    private String personId;

    @Schema(description = "设备ID")
    private String deviceId;

    @Schema(description = "下单人")
    private String orderByPerson;

    @Schema(description = "下单时间")
    private LocalDateTime orderDate;

    @Schema(description = "安全帽型号")
    private String helmetModel;

    @Schema(description = "安全帽颜色")
    private String helmetColor;

    @Schema(description = "数量")
    private Integer quantity;

    @Schema(description = "订单状态")
    private String orderStatus;

    @Schema(description = "收货时间")
    private LocalDateTime receivedDate;

    @Schema(description = "收货签收")
    private String receivedSign;

    @Schema(description = "绑定时间")
    private LocalDateTime bindTime;

    @Schema(description = "解绑时间")
    private LocalDateTime unbindTime;

    @Schema(description = "绑定时长")
    private Integer bindDuration;

    @Schema(description = "使用状态")
    private String usageStatus;

    @Schema(description = "绑定人")
    private String binder;

}
