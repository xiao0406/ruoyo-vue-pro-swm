package cn.iocoder.yudao.module.iot.controller.admin.device.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;

@Schema(name = "IoT设备新增/修改 Request VO")
@Data
public class IotDeviceSaveReqVO {

    @Schema(description = "主键", example = "abc123")
    private String id;

    @NotBlank(message = "设备编号不能为空")
    @Schema(description = "设备编号", requiredMode = Schema.RequiredMode.REQUIRED)
    private String deviceId;

    @Schema(description = "设备名称")
    private String deviceName;

    @Schema(description = "设备类型")
    private String deviceType;

    @Schema(description = "MAC地址")
    private String macAddress;

    @Schema(description = "固件版本")
    private String firmwareVersion;

    @Schema(description = "硬件版本")
    private String hardwareVersion;

    @Schema(description = "制造商")
    private String manufacturer;

    @Schema(description = "SN码")
    private String snCode;

    @Schema(description = "绑定人员ID")
    private String bindPersonId;

    @Schema(description = "绑定人员姓名")
    private String bindPersonName;
}
