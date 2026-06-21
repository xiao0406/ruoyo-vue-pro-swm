package cn.iocoder.yudao.module.swm.controller.admin.safetyhelmetorder.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "安全帽订单 Response VO")
@Data
public class SwmSafetyHelmetOrderRespVO {

    @Schema(description = "主键")
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
