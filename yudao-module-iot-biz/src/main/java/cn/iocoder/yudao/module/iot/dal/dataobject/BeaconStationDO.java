package cn.iocoder.yudao.module.iot.dal.dataobject;

import com.baomidou.mybatisplus.annotation.TableName;
import cn.iocoder.yudao.module.swm.dal.dataobject.SwmBaseDO;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Beacon 基站 DO
 * 表: iot_beacon_station
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@TableName("iot_beacon_station")
public class BeaconStationDO extends SwmBaseDO {
    /** 信标编号 */
    private String beaconId;
    /** 信标类型 */
    private String beaconType;
    /** 设备名称 */
    private String deviceName;
    /** 所在位置 */
    private String location;
    /** 所属区域 */
    private String area;
    /** 楼层ID */
    private String floorId;
    /** 楼层 */
    private String floor;
    /** 建筑ID */
    private String buildingId;
    /** 建筑 */
    private String building;
    /** MAC地址 */
    private String mac;
    /** 信标状态 */
    private String beaconStatus;
}
