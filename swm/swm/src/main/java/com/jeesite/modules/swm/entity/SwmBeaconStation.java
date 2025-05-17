/**
 * @author Shawn
 * @date 2025-05-14
 */
package com.jeesite.modules.swm.entity;

import com.jeesite.common.entity.DataEntity;
import com.jeesite.common.mybatis.annotation.Column;
import com.jeesite.common.mybatis.annotation.Table;
import com.jeesite.common.mybatis.mapper.query.QueryType;
import com.jeesite.modules.sys.utils.DictUtils;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;

/**
 * 信标基站管理实体类
 * 
 * @author Shawn
 */
@Table(name = "swm_beacon_station", alias = "a", columns = {
        @Column(name = "id", attrName = "id", label = "主键ID", isPK = true),
        @Column(name = "beacon_id", attrName = "beaconId", label = "信标编号", queryType = QueryType.LIKE),
        @Column(name = "beacon_type", attrName = "beaconType", label = "信标类型"),
        @Column(name = "control_type", attrName = "controlType", label = "围栏类型"),
        @Column(name = "location", attrName = "location", label = "所在位置", queryType = QueryType.LIKE),
        @Column(name = "map_coord", attrName = "mapCoord", label = "图中坐标"),
        @Column(name = "gps_coord", attrName = "gpsCoord", label = "GPS坐标"),
        @Column(name = "beacon_status", attrName = "beaconStatus", label = "信标状态"),
        @Column(name = "deploy_status", attrName = "deployStatus", label = "部署状态"),
        @Column(includeEntity = DataEntity.class)
}, orderBy = "a.update_date DESC")
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

    private String beaconId; // 信标编号
    private String beaconType; // 信标类型
    private String controlType; // 围栏类型
    private String location; // 所在位置
    private String mapCoord; // 图中坐标
    private String gpsCoord; // GPS坐标
    private String beaconStatus; // 信标状态
    private String deployStatus; // 部署状态

    public SwmBeaconStation() {
        super();
    }

    public SwmBeaconStation(String id) {
        super(id);
    }

    @NotBlank(message = "信标编号不能为空")
    @Length(min = 0, max = 50, message = "信标编号不能超过50个字符")
    public String getBeaconId() {
        return beaconId;
    }

    public void setBeaconId(String beaconId) {
        this.beaconId = beaconId;
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
}