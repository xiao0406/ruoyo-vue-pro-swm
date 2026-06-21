package cn.iocoder.yudao.module.swm.dal.dataobject;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * 头盔设备配置实体
 * @author Shawn
 * @date 2025-01-08
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@TableName("swm_helmet_device_config")
public class SwmHelmetDeviceConfigDO extends SwmBaseDO {

    /** 头盔编号 */
    private String deviceId;
    /** 服务器IP */
    private String serverIp;
    /** 端口Port */
    private String serverPort;
    /** 蓝牙扫描窗口 */
    private String bluetoothScanWindow;
    /** 每组时长 */
    private String groupDuration;
    /** 普通信标CS */
    private String normalBeaconCs;
    /** 特殊信标CS */
    private String specialBeaconCs;
    /** 定位模式 */
    private String locationMode;
    /** 深度休眠时长 */
    private String deepSleepDuration;
    /** 蓝牙扫描持续时间窗口 */
    private String bluetoothScanDuration;
    /** 发送间隔 */
    private String sendInterval;
    /** 危险源重新触发间隔 */
    private String hazardRetriggerInterval;
    /** 休眠唤醒时间 */
    private String sleepWakeupTime;
    /** 接收信标名称 */
    private String beaconFilterName;
    /** 脱帽报警时间间隔 */
    private String hatOffAlarmInterval;
}
