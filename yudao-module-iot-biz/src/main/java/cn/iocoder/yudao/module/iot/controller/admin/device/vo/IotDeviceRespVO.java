package cn.iocoder.yudao.module.iot.controller.admin.device.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(name = "IoT设备 Response VO")
@Data
public class IotDeviceRespVO {

    @Schema(description = "主键")
    private String id;
    @Schema(description = "设备编号")
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
    @Schema(description = "状态")
    private String status;
    @Schema(description = "SN码")
    private String snCode;
    @Schema(description = "绑定人员ID")
    private String bindPersonId;
    @Schema(description = "绑定人员姓名")
    private String bindPersonName;
    @Schema(description = "创建时间")
    private LocalDateTime createTime;
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
