package cn.iocoder.yudao.module.iot.controller.admin.device.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Schema(name = "IoT设备分页查询 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class IotDevicePageReqVO extends PageParam {

    @Schema(description = "设备编号", example = "DEV001")
    private String deviceId;

    @Schema(description = "设备名称")
    private String deviceName;

    @Schema(description = "设备类型")
    private String deviceType;

    @Schema(description = "绑定人员姓名")
    private String bindPersonName;
}
