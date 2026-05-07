/**
 * @author Shawn
 * @date 2025-05-21
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
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.validator.constraints.Length;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Date;
import java.util.List;

/**
 * 头盔设备管理实体类
 * 
 * @author Shawn
 */
@Table(name = "swm_helmet_device", alias = "a", columns = {
        @Column(name = "id", attrName = "id", label = "主键ID", isPK = true),
        @Column(name = "device_id", attrName = "deviceId", label = "头盔编号", queryType = QueryType.LIKE),
        @Column(name = "helmet_type", attrName = "helmetType", label = "头盔类型(1:便携式 2:头箍式)"),
        @Column(name = "battery_level", attrName = "batteryLevel", label = "头盔电量(0-100%)"),
        @Column(name = "ip", attrName = "ip", label = "IP地址"),
        @Column(name = "mac_address", attrName = "macAddress", label = "MAC地址"),
        @Column(name = "assigned_person", attrName = "assignedPerson", label = "绑定人员"),
        @Column(name = "person_name", attrName = "personName", label = "人员姓名", queryType = QueryType.LIKE),
        @Column(name = "person_phone", attrName = "personPhone", label = "手机号码"),
        @Column(name = "assigned_workshop", attrName = "assignedWorkshop", label = "所属车间"),
        @Column(name = "assigned_process", attrName = "assignedProcess", label = "所属工序"),
        @Column(name = "assigned_team", attrName = "assignedTeam", label = "所属班组"),
        @Column(name = "motion_status", attrName = "motionStatus", label = "运动状态"),
        @Column(name = "bind_time", attrName = "bindTime", label = "绑定时间"),
        @Column(name = "unbind_time", attrName = "unbindTime", label = "解绑时间"),
        @Column(name = "bind_duration_days", attrName = "bindDurationDays", label = "绑定时长(天)"),
        @Column(name = "usage_status", attrName = "usageStatus", label = "使用状态(0-已解绑, 1-使用中)"),
        // 设备参数配置字段
        @Column(name = "server_ip", attrName = "serverIp", label = "服务器IP"),
        @Column(name = "server_port", attrName = "serverPort", label = "端口Port"),
        @Column(name = "bluetooth_scan_window", attrName = "bluetoothScanWindow", label = "蓝牙扫描窗口(秒)"),
        @Column(name = "group_duration", attrName = "groupDuration", label = "每组时长(秒)"),
        @Column(name = "normal_beacon_cs", attrName = "normalBeaconCs", label = "普通信标CS"),
        @Column(name = "special_beacon_cs", attrName = "specialBeaconCs", label = "特殊信标CS"),
        @Column(name = "location_mode", attrName = "locationMode", label = "定位模式"),
        @Column(name = "deep_sleep_duration", attrName = "deepSleepDuration", label = "深度休眠时长(分钟)"),
        @Column(name = "bluetooth_scan_duration", attrName = "bluetoothScanDuration", label = "蓝牙扫描持续时间窗口(0.1秒为单位)"),
        @Column(name = "send_interval", attrName = "sendInterval", label = "发送间隔(秒)"),
        @Column(name = "hazard_retrigger_interval", attrName = "hazardRetriggerInterval", label = "危险源重新触发间隔(秒)"),
        @Column(name = "sleep_wakeup_time", attrName = "sleepWakeupTime", label = "休眠唤醒时间(秒)"),
        @Column(name = "beacon_filter_name", attrName = "beaconFilterName", label = "接收信标(名称)"),
        @Column(name = "hat_off_alarm_interval", attrName = "hatOffAlarmInterval", label = "脱帽报警时间间隔"),
        @Column(name = "device_source", attrName = "deviceSource", label = "设备来源（字典：swm_device_source  ，0-科利特，1-中泰）"),
        @Column(name = "imei", attrName = "imei", label = "IMEI", queryType = QueryType.LIKE),
        @Column(name = "device_color", attrName = "deviceColor", label = "帽子颜色", queryType = QueryType.LIKE),
        @Column(includeEntity = DataEntity.class),
        @Column(includeEntity= BaseEntity.class),
}, orderBy = "a.update_date DESC")
@Data
public class SwmHelmetDevice extends DataEntity<SwmHelmetDevice> {

    private static final long serialVersionUID = 1L;

    /**
     * 运动状态枚举
     */
    public static class MotionStatusEnum {
        /** 静止 */
        public static final String STATIC = "0";
        /** 运动 */
        public static final String MOVING = "1";
        /** 充电 */
        public static final String CHARGING = "2";

        /**
         * 获取运动状态显示文本
         */
        public static String getText(String value) {
            return DictUtils.getDictLabel("motion_status_enum", value, "");
        }
    }

    /**
     * 头盔类型枚举
     */
    public static class HelmetTypeEnum {
        /** 便携式 */
        public static final String PORTABLE = "1";
        /** 头箍式 */
        public static final String HEADBAND = "2";

        /**
         * 获取头盔类型显示文本
         */
        public static String getText(String value) {
            return DictUtils.getDictLabel("helmet_type_enum", value, "");
        }
    }

    /**
     * 使用状态枚举
     */
    public static class UsageStatusEnum {
        /** 已解绑 */
        public static final String UNBINDED = "0";
        /** 使用中 */
        public static final String IN_USE = "1";

        /**
         * 获取使用状态显示文本
         */
        public static String getText(String value) {
            return DictUtils.getDictLabel("usage_status_enum", value, "");
        }
    }

    /**
     * 定位模式枚举
     */
    public static class LocationModeEnum {
        /** GPS/北斗模式 */
        public static final String GPS_BEIDOU = "1";
        /** GPS/北斗+BT模式 */
        public static final String GPS_BEIDOU_BT = "2";
        /** BT+GPS/北斗模式 */
        public static final String BT_GPS_BEIDOU = "3";
        /** BT模式 */
        public static final String BT = "4";

        /**
         * 获取定位模式显示文本
         */
        public static String getText(String value) {
            if (value == null) {
                return "";
            }
            switch (value) {
                case GPS_BEIDOU:
                    return "GPS/北斗模式";
                case GPS_BEIDOU_BT:
                    return "GPS/北斗+BT模式";
                case BT_GPS_BEIDOU:
                    return "BT+GPS/北斗模式";
                case BT:
                    return "BT模式";
                default:
                    return "";
            }
        }
    }

    private String deviceId; // 头盔编号
    private String helmetType; // 头盔类型(1:便携式 2:头箍式)
    private Integer batteryLevel; // 头盔电量 (0-100%)
    private String ip; // IP地址
    private String macAddress; // MAC地址
    private String assignedPerson; // 绑定人员
    private String personName; // 人员姓名
    private String personPhone; // 手机号码
    private String assignedWorkshop;// 所属车间
    private String assignedProcess; // 所属工序
    private String assignedTeam; // 所属班组
    private String motionStatus; // 运动状态
    private Date bindTime; // 绑定时间
    private Date unbindTime; // 解绑时间
    private Integer bindDurationDays; // 绑定时长(天)
    private String usageStatus; // 使用状态(0-已解绑, 1-使用中)

    // 设备参数配置字段
    private String serverIp; // 服务器IP
    private String serverPort; // 端口Port
    private Integer bluetoothScanWindow; // 蓝牙扫描窗口(秒)
    private Integer groupDuration; // 每组时长(秒)
    private Integer normalBeaconCs; // 普通信标CS
    private Integer specialBeaconCs; // 特殊信标CS
    private String locationMode; // 定位模式(1:GPS/北斗 2:GPS/北斗+BT 3:BT+GPS/北斗 4:BT)
    private Integer deepSleepDuration; // 深度休眠时长(分钟)
    private Integer bluetoothScanDuration; // 蓝牙扫描持续时间窗口(0.1秒为单位)
    private Integer sendInterval; // 发送间隔(秒)
    private Integer hazardRetriggerInterval; // 危险源重新触发间隔(秒)
    private Integer sleepWakeupTime; // 休眠唤醒时间(秒)
    private String beaconFilterName; // 接收信标(名称)
    private String deviceSource; //设备来源（字典：swm_device_source  ，0-科利特，1-中泰）

    // 非数据库字段，用于查询条件
    private List<String> deviceIdList; // 设备ID列表，用于批量查询

    @ApiModelProperty(value = "开机状态  0-开机，1-关机")
    private String powerOnStatus;
    //在线人数
    private List<String> deviceOnlist;
    private String hatOffAlarmInterval;
    private String imei;
    private String deviceColor;


    /**
     * 随机值，目的取消一级缓存
     */
    private Integer random;

    /**
     * 旧设备ID - 非数据库字段，仅用于数据传递
     */
    @ApiModelProperty(value = "旧设备ID（仅用于数据传递）")
    private String oldDeviceId;

    /**
     * 绑定人员主键id - 非数据库字段，仅用于数据传递
     */
    @ApiModelProperty(value = "绑定人员主键id（仅用于数据传递）")
    private String personId;


    @ExcelFields({
            @ExcelField(title="设备编号", attrName = "deviceId", align = ExcelField.Align.CENTER, sort = 10),
            @ExcelField(title="设备类型", attrName = "helmetType",align = ExcelField.Align.CENTER,dictType = "helmet_type_enum", sort = 20),
            @ExcelField(title="电量", attrName = "batteryLevel",align = ExcelField.Align.CENTER, sort = 30),
            @ExcelField(title="IP", attrName = "ip",align = ExcelField.Align.CENTER, sort = 40),
            @ExcelField(title="MAC地址", attrName = "macAddress",align = ExcelField.Align.CENTER, sort = 50),
            @ExcelField(title="人员姓名", attrName = "personName",align = ExcelField.Align.CENTER, sort = 60),
            @ExcelField(title="手机号码", attrName = "personPhone",align = ExcelField.Align.CENTER, sort = 70),
            @ExcelField(title="所属车间", attrName = "assignedWorkshop",align = ExcelField.Align.CENTER, sort = 80),
            @ExcelField(title="所属班组", attrName = "assignedTeam",align = ExcelField.Align.CENTER, sort = 90),
            @ExcelField(title="运动状态", attrName = "motionStatus",align = ExcelField.Align.CENTER, dictType = "motion_status_enum",sort = 100),
    })

    public SwmHelmetDevice() {
        super();
    }

    public SwmHelmetDevice(String id) {
        super(id);
    }

    @Length(min = 0, max = 50, message = "头盔编号长度不能超过50个字符")
    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getHelmetType() {
        return helmetType;
    }

    /**
     * 获取头盔类型显示值
     */
    public String getHelmetTypeText() {
        return HelmetTypeEnum.getText(helmetType);
    }

    public void setHelmetType(String helmetType) {
        this.helmetType = helmetType;
    }

    public Integer getBatteryLevel() {
        return batteryLevel;
    }

    public void setBatteryLevel(Integer batteryLevel) {
        this.batteryLevel = batteryLevel;
    }

    @Length(min = 0, max = 20, message = "IP地址长度不能超过20个字符")
    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    @Length(min = 0, max = 50, message = "MAC地址长度不能超过50个字符")
    public String getMacAddress() {
        return macAddress;
    }

    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }

    @Length(min = 0, max = 100, message = "绑定人员长度不能超过100个字符")
    public String getAssignedPerson() {
        return assignedPerson;
    }

    public void setAssignedPerson(String assignedPerson) {
        this.assignedPerson = assignedPerson;
    }

    @Length(min = 0, max = 100, message = "人员姓名长度不能超过100个字符")
    public String getPersonName() {
        return personName;
    }

    public void setPersonName(String personName) {
        this.personName = personName;
    }

    @Length(min = 0, max = 20, message = "手机号码长度不能超过20个字符")
    public String getPersonPhone() {
        return personPhone;
    }

    public void setPersonPhone(String personPhone) {
        this.personPhone = personPhone;
    }

    @Length(min = 0, max = 100, message = "所属车间长度不能超过100个字符")
    public String getAssignedWorkshop() {
        return assignedWorkshop;
    }

    public void setAssignedWorkshop(String assignedWorkshop) {
        this.assignedWorkshop = assignedWorkshop;
    }

    @Length(min = 0, max = 100, message = "所属工序长度不能超过100个字符")
    public String getAssignedProcess() {
        return assignedProcess;
    }

    public void setAssignedProcess(String assignedProcess) {
        this.assignedProcess = assignedProcess;
    }

    @Length(min = 0, max = 100, message = "所属班组长度不能超过100个字符")
    public String getAssignedTeam() {
        return assignedTeam;
    }

    public void setAssignedTeam(String assignedTeam) {
        this.assignedTeam = assignedTeam;
    }

    @Length(min = 0, max = 20, message = "运动状态长度不能超过20个字符")
    public String getMotionStatus() {
        return motionStatus;
    }

    /**
     * 获取运动状态显示值
     */
    public String getMotionStatusText() {
        return MotionStatusEnum.getText(motionStatus);
    }

    public void setMotionStatus(String motionStatus) {
        this.motionStatus = motionStatus;
    }

    public Date getBindTime() {
        return bindTime;
    }

    public void setBindTime(Date bindTime) {
        this.bindTime = bindTime;
    }

    public Date getUnbindTime() {
        return unbindTime;
    }

    public void setUnbindTime(Date unbindTime) {
        this.unbindTime = unbindTime;
    }

    public Integer getBindDurationDays() {
        return bindDurationDays;
    }

    public void setBindDurationDays(Integer bindDurationDays) {
        this.bindDurationDays = bindDurationDays;
    }

    public String getUsageStatus() {
        return usageStatus;
    }

    /**
     * 获取使用状态显示值
     */
    public String getUsageStatusText() {
        return UsageStatusEnum.getText(usageStatus);
    }

    public void setUsageStatus(String usageStatus) {
        this.usageStatus = usageStatus;
    }

    public List<String> getDeviceIdList() {
        return deviceIdList;
    }

    public void setDeviceIdList(List<String> deviceIdList) {
        this.deviceIdList = deviceIdList;
    }

    // 设备参数配置字段的 getter/setter 方法

    @Length(min = 0, max = 20, message = "服务器IP长度不能超过20个字符")
    public String getServerIp() {
        return serverIp;
    }

    public void setServerIp(String serverIp) {
        this.serverIp = serverIp;
    }

    @Length(min = 0, max = 10, message = "端口Port长度不能超过10个字符")
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

    @Length(min = 0, max = 2, message = "定位模式长度不能超过2个字符")
    public String getLocationMode() {
        return locationMode;
    }

    /**
     * 获取定位模式显示值
     */
    public String getLocationModeText() {
        return LocationModeEnum.getText(locationMode);
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

    @Length(min = 0, max = 10, message = "接收信标名称长度不能超过10个字符")
    public String getBeaconFilterName() {
        return beaconFilterName;
    }

    public void setBeaconFilterName(String beaconFilterName) {
        this.beaconFilterName = beaconFilterName;
    }

    /**
     * 获取旧设备ID（非数据库字段）
     */
    public String getOldDeviceId() {
        return oldDeviceId;
    }

    /**
     * 设置旧设备ID（非数据库字段）
     */
    public void setOldDeviceId(String oldDeviceId) {
        this.oldDeviceId = oldDeviceId;
    }

    public String getPersonId() {
        return personId;
    }

    public void setPersonId(String personId) {
        this.personId = personId;
    }

}