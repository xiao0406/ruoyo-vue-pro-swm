package cn.iocoder.yudao.module.iot.tcp.processor;

import lombok.Data;

@Data
public class SwmHelmetDeviceConfig {
    private String deviceId;
    private String serverIp;
    private String serverPort;
    private String bluetoothScanWindow;
    private String groupDuration;
    private String normalBeaconCs;
    private String specialBeaconCs;
    private String locationMode;
    private String deepSleepDuration;
    private String bluetoothScanDuration;
    private String sendInterval;
    private String hazardRetriggerInterval;
    private String sleepWakeupTime;
    private String beaconFilterName;
    private String hatOffAlarmInterval;
}
