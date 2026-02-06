package com.jeesite.modules.swm.entity;

import com.jeesite.common.entity.BaseEntity;
import com.jeesite.common.entity.DataEntity;
import com.jeesite.common.mybatis.annotation.Table;
import com.jeesite.common.mybatis.annotation.Column;
import lombok.Data;

/**
 * 头盔设备配置实体
 * @author Shawn
 * @date 2025-01-08
 */
@Table(name = "swm_helmet_device_config", alias = "a", columns = {
        @Column(name = "id", attrName = "id", label = "主键ID", isPK = true),
        @Column(name = "device_id", attrName = "deviceId", label = "头盔编号"),
        @Column(name = "server_ip", attrName = "serverIp", label = "服务器IP"),
        @Column(name = "server_port", attrName = "serverPort", label = "端口Port"),
        @Column(name = "bluetooth_scan_window", attrName = "bluetoothScanWindow", label = "蓝牙扫描窗口"),
        @Column(name = "group_duration", attrName = "groupDuration", label = "每组时长"),
        @Column(name = "normal_beacon_cs", attrName = "normalBeaconCs", label = "普通信标CS"),
        @Column(name = "special_beacon_cs", attrName = "specialBeaconCs", label = "特殊信标CS"),
        @Column(name = "location_mode", attrName = "locationMode", label = "定位模式"),
        @Column(name = "deep_sleep_duration", attrName = "deepSleepDuration", label = "深度休眠时长"),
        @Column(name = "bluetooth_scan_duration", attrName = "bluetoothScanDuration", label = "蓝牙扫描持续时间窗口"),
        @Column(name = "send_interval", attrName = "sendInterval", label = "发送间隔"),
        @Column(name = "hazard_retrigger_interval", attrName = "hazardRetriggerInterval", label = "危险源重新触发间隔"),
        @Column(name = "sleep_wakeup_time", attrName = "sleepWakeupTime", label = "休眠唤醒时间"),
        @Column(name = "beacon_filter_name", attrName = "beaconFilterName", label = "接收信标名称"),
        @Column(name = "hat_off_alarm_interval", attrName = "hatOffAlarmInterval", label = "脱帽报警时间间隔"),
        @Column(includeEntity = DataEntity.class),
        @Column(includeEntity= BaseEntity.class),
}, orderBy = "a.update_date DESC")
@Data
public class SwmHelmetDeviceConfig extends DataEntity<SwmHelmetDeviceConfig> {
    
    private static final long serialVersionUID = 1L;
    
    private String deviceId;
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

    
    public SwmHelmetDeviceConfig() {
        this(null);
    }
    
    public SwmHelmetDeviceConfig(String id) {
        super(id);
    }
    
    public String getDeviceId() {
        return deviceId;
    }
    
    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }
    
    public String getServerIp() {
        return serverIp;
    }
    
    public void setServerIp(String serverIp) {
        this.serverIp = serverIp;
    }
    
    public String getServerPort() {
        return serverPort;
    }
    
    public void setServerPort(String serverPort) {
        this.serverPort = serverPort;
    }
    
    public Integer getBluetoothScanWindow() {
        return bluetoothScanWindow;
    }
    
    public void setBluetoothScanWindow(Integer bluetoothScanWindow) {
        this.bluetoothScanWindow = bluetoothScanWindow;
    }
    
    public Integer getGroupDuration() {
        return groupDuration;
    }
    
    public void setGroupDuration(Integer groupDuration) {
        this.groupDuration = groupDuration;
    }
    
    public Integer getNormalBeaconCs() {
        return normalBeaconCs;
    }
    
    public void setNormalBeaconCs(Integer normalBeaconCs) {
        this.normalBeaconCs = normalBeaconCs;
    }
    
    public Integer getSpecialBeaconCs() {
        return specialBeaconCs;
    }
    
    public void setSpecialBeaconCs(Integer specialBeaconCs) {
        this.specialBeaconCs = specialBeaconCs;
    }
    
    public String getLocationMode() {
        return locationMode;
    }
    
    public void setLocationMode(String locationMode) {
        this.locationMode = locationMode;
    }
    
    public Integer getDeepSleepDuration() {
        return deepSleepDuration;
    }
    
    public void setDeepSleepDuration(Integer deepSleepDuration) {
        this.deepSleepDuration = deepSleepDuration;
    }
    
    public Integer getBluetoothScanDuration() {
        return bluetoothScanDuration;
    }
    
    public void setBluetoothScanDuration(Integer bluetoothScanDuration) {
        this.bluetoothScanDuration = bluetoothScanDuration;
    }
    
    public Integer getSendInterval() {
        return sendInterval;
    }
    
    public void setSendInterval(Integer sendInterval) {
        this.sendInterval = sendInterval;
    }
    
    public Integer getHazardRetriggerInterval() {
        return hazardRetriggerInterval;
    }
    
    public void setHazardRetriggerInterval(Integer hazardRetriggerInterval) {
        this.hazardRetriggerInterval = hazardRetriggerInterval;
    }
    
    public Integer getSleepWakeupTime() {
        return sleepWakeupTime;
    }
    
    public void setSleepWakeupTime(Integer sleepWakeupTime) {
        this.sleepWakeupTime = sleepWakeupTime;
    }
    
    public String getBeaconFilterName() {
        return beaconFilterName;
    }
    
    public void setBeaconFilterName(String beaconFilterName) {
        this.beaconFilterName = beaconFilterName;
    }
}