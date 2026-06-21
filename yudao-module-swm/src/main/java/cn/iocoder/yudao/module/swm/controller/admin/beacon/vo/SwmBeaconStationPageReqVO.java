package cn.iocoder.yudao.module.swm.controller.admin.beacon.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "信标站点分页查询 VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class SwmBeaconStationPageReqVO extends PageParam {

    @Schema(description = "MAC地址")
    private String beaconId;

    @Schema(description = "设备名称")
    private String deviceName;

    @Schema(description = "信标类型")
    private String beaconType;

    @Schema(description = "所属区域")
    private String area;

    @Schema(description = "所在位置")
    private String location;

    @Schema(description = "信标状态")
    private String beaconStatus;

    @Schema(description = "部署状态")
    private String deployStatus;

    @Schema(description = "建筑名称")
    private String building;

}
