/**
 * @author Shawn
 * @date 2025-05-14
 */
package com.jeesite.modules.swm.entity;

import com.jeesite.common.entity.BaseEntity;
import com.jeesite.common.entity.DataEntity;
import com.jeesite.common.mybatis.annotation.Column;
import com.jeesite.common.mybatis.annotation.Table;
import com.jeesite.common.mybatis.mapper.query.QueryType;
import com.jeesite.common.utils.excel.annotation.ExcelField;
import com.jeesite.common.utils.excel.annotation.ExcelFields;
import com.jeesite.modules.sys.utils.DictUtils;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;

/**
 * 信标基站管理实体类
 * 
 * @author Shawn
 */
@Table(name = "swm_beacon_station", alias = "a", columns = {
        @Column(name = "id", attrName = "id", label = "主键ID", isPK = true),
        @Column(name = "beacon_id", attrName = "beaconId", label = "MAC地址", queryType = QueryType.LIKE),
        @Column(name = "device_name", attrName = "deviceName", label = "设备名称", queryType = QueryType.LIKE),
        @Column(name = "beacon_type", attrName = "beaconType", label = "信标类型"),
        @Column(name = "beacon_color", attrName = "beaconColor", label = "信标颜色"),
        @Column(name = "control_type", attrName = "controlType", label = "围栏类型"),
        @Column(name = "location", attrName = "location", label = "所在位置", queryType = QueryType.LIKE),
        @Column(name = "area", attrName = "area", label = "所属区域", queryType = QueryType.LIKE),
        @Column(name = "map_coord", attrName = "mapCoord", label = "图中坐标"),
        @Column(name = "pixel_x", attrName = "pixelX", label = "图纸像素X坐标"),
        @Column(name = "pixel_y", attrName = "pixelY", label = "图纸像素Y坐标"),
        @Column(name = "real_x", attrName = "realX", label = "实际地址X坐标"),
        @Column(name = "real_y", attrName = "realY", label = "实际地址Y坐标"),
        @Column(name = "gps_coord", attrName = "gpsCoord", label = "GPS坐标"),
        @Column(name = "gps_longitude", attrName = "gpsLongitude", label = "GPS经度"),
        @Column(name = "gps_latitude", attrName = "gpsLatitude", label = "GPS纬度"),
        @Column(name = "beacon_status", attrName = "beaconStatus", label = "信标状态"),
        @Column(name = "deploy_status", attrName = "deployStatus", label = "部署状态"),
        @Column(name = "stream_url", attrName = "streamUrl", label = "推流地址"),
        @Column(name = "floor", attrName = "floor", label = "楼层名称"),
        // 楼层/建筑关联字段 @author Shawn @date 2026-04-07
        @Column(name = "floor_id",    attrName = "floorId",    label = "楼层ID"),
        @Column(name = "building_id", attrName = "buildingId", label = "建筑ID"),
        @Column(name = "building",    attrName = "building",   label = "建筑名称"),
        @Column(includeEntity = DataEntity.class),
        @Column(includeEntity= BaseEntity.class),
}, orderBy = "a.update_date DESC")
@Data
public class SwmBeaconStation extends DataEntity<SwmBeaconStation> {

    private static final long serialVersionUID = 1L;

    /**
     * 信标类型枚举
     */
    public static class BeaconTypeEnum {
        /** 常规信标 */
        public static final String CONVENTION = "1";
        /** 电子围栏 */
        public static final String FENCE = "2";
        /** 危险源信标 */
        public static final String DANGEROUS_SOURCE = "3";
        /** 摄像头 */
        public static final String CAMERA = "4";

        /**
         * 获取信标类型显示文本
         */
        public static String getText(String value) {
            return DictUtils.getDictLabel("beacon_type_enum", value, "");
        }
    }

    /**
     * 信标状态枚举
     */
    public static class BeaconStatusEnum {
        /** 离线 */
        public static final String OFFLINE = "2";
        /** 在线 */
        public static final String ONLINE = "1";

        /**
         * 获取信标状态显示文本
         */
        public static String getText(String value) {
            return DictUtils.getDictLabel("beacon_status_enum", value, "");
        }
    }

    /**
     * 部署状态枚举
     */
    public static class DeployStatusEnum {
        /** 未部署 */
        public static final String NOT_DEPLOYED = "0";
        /** 已部署 */
        public static final String DEPLOYED = "1";

        /**
         * 获取部署状态显示文本
         */
        public static String getText(String value) {
            return DictUtils.getDictLabel("deploy_status_enum", value, "");
        }
    }

    private String beaconId; // MAC地址
    private String deviceName; // 设备名称
    private String beaconType; // 信标类型
    private String beaconColor; // 信标颜色
    private String controlType; // 围栏类型
    private String location; // 所在位置
    private String area; // 所属区域
    private String mapCoord; // 图中坐标
    private Double pixelX; // 图纸像素X坐标
    private Double pixelY; // 图纸像素Y坐标
    private Double realX; // 实际地址X坐标
    private Double realY; // 实际地址Y坐标
    private String gpsCoord; // GPS坐标
    private Double gpsLongitude; // GPS经度
    private Double gpsLatitude; // GPS纬度
    private String beaconStatus; // 信标状态
    private String deployStatus; // 部署状态
    private String streamUrl; // 推流地址
    private String floor;      // 楼层名称
    // 楼层/建筑关联字段，方案A：前端上传冗余存储 @author Shawn @date 2026-04-07
    private String floorId;    // 楼层ID，关联 swm_site_map_management floor 节点
    private String buildingId; // 建筑ID，关联 swm_site_map_management building 节点
    private String building;   // 建筑名称（冗余存储）

    public SwmBeaconStation() {
        super();
    }

    @ExcelFields({
            @ExcelField(title = "MAC地址", attrName = "beaconId", align = ExcelField.Align.CENTER, sort = 10,width = 256*30),
            @ExcelField(title = "所属区域", attrName = "area", align = ExcelField.Align.CENTER, sort = 20,width = 256*30),
    })

    public SwmBeaconStation(String id) {
        super(id);
    }

    @NotBlank(message = "MAC地址不能为空")
    @Length(min = 0, max = 50, message = "MAC地址不能超过50个字符")
    public String getBeaconId() {
        return beaconId;
    }

    public void setBeaconId(String beaconId) {
        this.beaconId = beaconId;
    }

    @Length(min = 0, max = 100, message = "设备名称不能超过100个字符")
    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    @Length(min = 0, max = 50, message = "信标类型不能超过50个字符")
    public String getBeaconType() {
        return beaconType;
    }

    public void setBeaconType(String beaconType) {
        this.beaconType = beaconType;
    }

    public String getBeaconTypeText() {
        return BeaconTypeEnum.getText(this.beaconType);
    }

    @Length(min = 0, max = 50, message = "信标颜色不能超过50个字符")
    public String getBeaconColor() {
        return beaconColor;
    }

    public void setBeaconColor(String beaconColor) {
        this.beaconColor = beaconColor;
    }

    @Length(min = 0, max = 50, message = "围栏类型不能超过50个字符")
    public String getControlType() {
        return controlType;
    }

    public void setControlType(String controlType) {
        this.controlType = controlType;
    }

    @Length(min = 0, max = 255, message = "所在位置不能超过255个字符")
    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    @Length(min = 0, max = 100, message = "所属区域不能超过100个字符")
    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area;
    }

    @Length(min = 0, max = 50, message = "图中坐标不能超过50个字符")
    public String getMapCoord() {
        return mapCoord;
    }

    public void setMapCoord(String mapCoord) {
        this.mapCoord = mapCoord;
    }

    @Length(min = 0, max = 100, message = "GPS坐标不能超过100个字符")
    public String getGpsCoord() {
        return gpsCoord;
    }

    public void setGpsCoord(String gpsCoord) {
        this.gpsCoord = gpsCoord;
    }

    @Length(min = 0, max = 20, message = "信标状态不能超过20个字符")
    public String getBeaconStatus() {
        return beaconStatus;
    }

    public void setBeaconStatus(String beaconStatus) {
        this.beaconStatus = beaconStatus;
    }

    public String getBeaconStatusText() {
        return BeaconStatusEnum.getText(this.beaconStatus);
    }

    @Length(min = 0, max = 20, message = "部署状态不能超过20个字符")
    public String getDeployStatus() {
        return deployStatus;
    }

    public void setDeployStatus(String deployStatus) {
        this.deployStatus = deployStatus;
    }

    public String getDeployStatusText() {
        return DeployStatusEnum.getText(this.deployStatus);
    }

    @Length(min = 0, max = 255, message = "推流地址不能超过255个字符")
    public String getStreamUrl() {
        return streamUrl;
    }

    public void setStreamUrl(String streamUrl) {
        this.streamUrl = streamUrl;
    }

    public Double getPixelX() {
        return pixelX;
    }

    public void setPixelX(Double pixelX) {
        this.pixelX = pixelX;
    }

    public Double getPixelY() {
        return pixelY;
    }

    public void setPixelY(Double pixelY) {
        this.pixelY = pixelY;
    }

    public Double getGpsLongitude() {
        return gpsLongitude;
    }

    public void setGpsLongitude(Double gpsLongitude) {
        this.gpsLongitude = gpsLongitude;
    }

    public Double getGpsLatitude() {
        return gpsLatitude;
    }

    public void setGpsLatitude(Double gpsLatitude) {
        this.gpsLatitude = gpsLatitude;
    }

    public Double getRealX() {
        return realX;
    }

    public void setRealX(Double realX) {
        this.realX = realX;
    }

    public Double getRealY() {
        return realY;
    }

    public void setRealY(Double realY) {
        this.realY = realY;
    }
}