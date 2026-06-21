package cn.iocoder.yudao.module.swm.controller.admin.beacon.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;

@Schema(description = "信标站点新增/修改 Request VO")
@Data
public class SwmBeaconStationSaveReqVO {

    @Schema(description = "信标站点编号")
    private String id;

    @Schema(description = "MAC地址", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "MAC地址不能为空")
    private String beaconId;

    @Schema(description = "设备名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "设备名称不能为空")
    private String deviceName;

    @Schema(description = "信标类型")
    private String beaconType;

    @Schema(description = "信标颜色")
    private String beaconColor;

    @Schema(description = "围栏类型")
    private String controlType;

    @Schema(description = "所在位置")
    private String location;

    @Schema(description = "所属区域")
    private String area;

    @Schema(description = "图中坐标")
    private String mapCoord;

    @Schema(description = "图纸像素X坐标")
    private Double pixelX;

    @Schema(description = "图纸像素Y坐标")
    private Double pixelY;

    @Schema(description = "实际地址X坐标")
    private Double realX;

    @Schema(description = "实际地址Y坐标")
    private Double realY;

    @Schema(description = "GPS坐标")
    private String gpsCoord;

    @Schema(description = "GPS经度")
    private Double gpsLongitude;

    @Schema(description = "GPS纬度")
    private Double gpsLatitude;

    @Schema(description = "信标状态")
    private String beaconStatus;

    @Schema(description = "部署状态")
    private String deployStatus;

    @Schema(description = "推流地址")
    private String streamUrl;

    @Schema(description = "major")
    private String major;

    @Schema(description = "minor")
    private String minor;

    @Schema(description = "楼层名称")
    private String floor;

    @Schema(description = "楼层ID")
    private String floorId;

    @Schema(description = "建筑ID")
    private String buildingId;

    @Schema(description = "建筑名称")
    private String building;

}
