package cn.iocoder.yudao.module.swm.dal.dataobject;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * 信标站点 DO
 *
 * 迁移自 JeeSite: com.jeesite.modules.entity.SwmBeaconStation
 * 表: swm_beacon_station
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@TableName("swm_beacon_station")
public class SwmBeaconStationDO extends SwmBaseDO {

    /** MAC地址 */
    private String beaconId;
    /** 设备名称 */
    private String deviceName;
    /** 信标类型（枚举 SwmEnums.BeaconTypeEnum） */
    private String beaconType;
    /** 信标颜色 */
    private String beaconColor;
    /** 围栏类型 */
    private String controlType;
    /** 所在位置 */
    private String location;
    /** 所属区域 */
    private String area;
    /** 图中坐标 */
    private String mapCoord;
    /** 图纸像素X坐标 */
    private Double pixelX;
    /** 图纸像素Y坐标 */
    private Double pixelY;
    /** 实际地址X坐标 */
    private Double realX;
    /** 实际地址Y坐标 */
    private Double realY;
    /** GPS坐标 */
    private String gpsCoord;
    /** GPS经度 */
    private Double gpsLongitude;
    /** GPS纬度 */
    private Double gpsLatitude;
    /** 信标状态（枚举 SwmEnums.BeaconStatusEnum） */
    private String beaconStatus;
    /** 部署状态（枚举 SwmEnums.DeployStatusEnum） */
    private String deployStatus;
    /** 推流地址 */
    private String streamUrl;
    /** major */
    private String major;
    /** minor */
    private String minor;
    /** 楼层名称 */
    private String floor;
    /** 楼层ID */
    private String floorId;
    /** 建筑ID */
    private String buildingId;
    /** 建筑名称 */
    private String building;

}
