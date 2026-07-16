package cn.iocoder.yudao.module.iot.dal.dataobject;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * 坐标请求 DO
 * 表: iot_coordinate_request
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@TableName("iot_coordinate_request")
public class IotCoordinateRequestDO extends IotBaseDO {
    private String elderId;
    private Double x;
    private Double originalX;
    private Double scaleX;
    private Double y;
    private Double originalY;
    private Double scaleY;
    private String address;
    private String mapId;
    private String orgCd;
    private String type;
    private String appId;
    private Integer warningId;
    private String timeStr;
    private String nearestBeacon;
    private String usedBeacons;
}
