package cn.iocoder.yudao.module.swm.dal.dataobject;

import lombok.Data;
import java.util.Map;

/**
 * TDengine 设备数据传输对象
 */
@Data
public class DeviceDataDTO {
    private String deviceNum;
    private String deviceId;
    private String personName;
    private String identityCard;
    private String x;
    private String y;
    private String time;
    private String battery;
    private String major;
    private String minor;
    private String rssi;
    private Map<String, Object> data;
}
