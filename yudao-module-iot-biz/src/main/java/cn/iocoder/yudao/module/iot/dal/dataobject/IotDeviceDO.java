package cn.iocoder.yudao.module.iot.dal.dataobject;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * IoT 设备 DO
 * 表: iot_device
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@TableName("iot_device")
public class IotDeviceDO extends IotBaseDO {
    private String deviceId;
    private String deviceName;
    private String deviceType;
    private String macAddress;
    private String firmwareVersion;
    private String hardwareVersion;
    private String manufacturer;
    private String status;
    private String snCode;
    private String bindPersonId;
    private String bindPersonName;
}
