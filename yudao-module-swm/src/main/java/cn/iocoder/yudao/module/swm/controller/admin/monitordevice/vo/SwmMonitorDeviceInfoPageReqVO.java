package cn.iocoder.yudao.module.swm.controller.admin.monitordevice.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "监控设备分页查询 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class SwmMonitorDeviceInfoPageReqVO extends PageParam {

    @Schema(description = "设备名称")
    private String name;

    @Schema(description = "设备编码")
    private String code;

    @Schema(description = "设备类型")
    private String deviceType;

    @Schema(description = "IP地址")
    private String ip;

}
