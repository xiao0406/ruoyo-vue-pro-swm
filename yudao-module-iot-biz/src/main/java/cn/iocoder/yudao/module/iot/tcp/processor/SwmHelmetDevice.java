package cn.iocoder.yudao.module.iot.tcp.processor;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.Date;

@Data
public class SwmHelmetDevice {

    public static final String HELMET_TYPE_INTEGRATED = "0";

    private String id;
    private Long tenantId;
    private String deviceId;
    private String helmetType;
    private Integer batteryLevel;
    private String ip;
    private String macAddress;
    private String assignedPerson;
    private String personName;
    private String personPhone;
    private String assignedWorkshop;
    private String assignedProcess;
    private String assignedTeam;
    private String motionStatus;
    private LocalDateTime bindTime;
    private LocalDateTime unbindTime;
    private Integer bindDurationDays;
    private String usageStatus;
    private String serverIp;
    private String serverPort;
    private Integer bluetoothScanWindow;
    private Integer groupDuration;
    private Integer normalBeaconCs;
    private Integer specialBeaconCs;
    private String locationMode;
    private Integer deepSleepDuration;
    private Integer bluetoothScanDuration;
    private Integer sendInterval;
    private Integer hazardRetriggerInterval;
    private Integer sleepWakeupTime;
    private String beaconFilterName;
    private String hatOffAlarmInterval;
    private String deviceSource;
    private String imei;
    private String deviceColor;
    private String status;
    private String remarks;
    private Date createDate;
    private Date updateDate;

    public void setUsageStatus(Integer usageStatus) {
        this.usageStatus = usageStatus == null ? null : String.valueOf(usageStatus);
    }
}
