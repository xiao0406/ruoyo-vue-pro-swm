package cn.iocoder.yudao.module.iot.tcp.model;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * TCP消息数据模型
 * 用于存储解析后的TCP消息数据
 *
 * @author Shawn
 * @date 2026-03-25
 */
@Data
public class TcpMessageData {

    /**
     * 原始消息内容
     */
    private String rawMessage;

    /**
     * 数据长度（如：13B）
     */
    private String dataLength;

    /**
     * 命令类型（如：S）
     */
    private String commandType;

    /**
     * 设备编号
     */
    private String deviceId;

    /**
     * 纬度方向（N/S）
     */
    private String northSouth;

    /**
     * 纬度原始值（ddmm.mmmm）
     */
    private String latitude;

    /**
     * 经度方向（E/W）
     */
    private String eastWest;

    /**
     * 经度原始值（dddmm.mmmm）
     */
    private String longitude;

    /**
     * 使用的数据库信息
     */
    private String dbName;

    /**
     * GSM信号强度
     */
    private Integer gsmSignalStrength;

    /**
     * 报警值
     */
    private Integer alarmValue;

    /**
     * 状态值
     */
    private Integer statusValue;

    /**
     * 三轴加速度 [X, Y, Z]
     */
    private int[] acceleration;

    /**
     * 蓝牙信标数据列表
     * 每个Map包含：MAC, RSSI, TIME
     */
    private List<Map<String, Object>> bluetoothBeacons;

    /**
     * 电池电压
     */
    private Integer batteryVoltage;

    /**
     * 电池电量（百分比）
     */
    private Integer batteryLevel;

    /**
     * 扫描时间差（秒）
     * 从蓝牙信标数据中提取的时间差
     */
    private Integer scanTimeDiff;

    /**
     * 计算得到的扫描时间戳
     */
    private Long scanTimestamp;

    /**
     * 协议中的GPS/北斗 UTC原始时间
     */
    private String gpsUtcRaw;

    /**
     * 协议中的GPS/北斗 UTC时间戳（毫秒）
     */
    private Long gpsUtcTimestamp;

    /**
     * 是否是中泰的设备，默认false
     */
    private boolean isZTDevice = false;

    /**
     * 中泰-纬度
     */
    double lat;
    /**
     * 中泰-经度
     */
    double lng;

    // 构造函数
    public TcpMessageData() {
    }

    public TcpMessageData(String rawMessage) {
        this.rawMessage = rawMessage;
    }

    // Getter和Setter方法
    public String getRawMessage() {
        return rawMessage;
    }

    public void setRawMessage(String rawMessage) {
        this.rawMessage = rawMessage;
    }

    public String getDataLength() {
        return dataLength;
    }

    public void setDataLength(String dataLength) {
        this.dataLength = dataLength;
    }

    public String getCommandType() {
        return commandType;
    }

    public void setCommandType(String commandType) {
        this.commandType = commandType;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getNorthSouth() {
        return northSouth;
    }

    public void setNorthSouth(String northSouth) {
        this.northSouth = northSouth;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getEastWest() {
        return eastWest;
    }

    public void setEastWest(String eastWest) {
        this.eastWest = eastWest;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public Integer getGsmSignalStrength() {
        return gsmSignalStrength;
    }

    public void setGsmSignalStrength(Integer gsmSignalStrength) {
        this.gsmSignalStrength = gsmSignalStrength;
    }

    public Integer getAlarmValue() {
        return alarmValue;
    }

    public void setAlarmValue(Integer alarmValue) {
        this.alarmValue = alarmValue;
    }

    public Integer getStatusValue() {
        return statusValue;
    }

    public void setStatusValue(Integer statusValue) {
        this.statusValue = statusValue;
    }

    public int[] getAcceleration() {
        return acceleration;
    }

    public void setAcceleration(int[] acceleration) {
        this.acceleration = acceleration;
    }

    public List<Map<String, Object>> getBluetoothBeacons() {
        return bluetoothBeacons;
    }

    public void setBluetoothBeacons(List<Map<String, Object>> bluetoothBeacons) {
        this.bluetoothBeacons = bluetoothBeacons;
    }

    public Integer getBatteryVoltage() {
        return batteryVoltage;
    }

    public void setBatteryVoltage(Integer batteryVoltage) {
        this.batteryVoltage = batteryVoltage;
    }

    public Integer getBatteryLevel() {
        return batteryLevel;
    }

    public void setBatteryLevel(Integer batteryLevel) {
        this.batteryLevel = batteryLevel;
    }

    public Integer getScanTimeDiff() {
        return scanTimeDiff;
    }

    public void setScanTimeDiff(Integer scanTimeDiff) {
        this.scanTimeDiff = scanTimeDiff;
    }

    public Long getScanTimestamp() {
        return scanTimestamp;
    }

    public String getDbName() {
        return dbName;
    }

    public void setDbName(String dbName) {
        this.dbName = dbName;
    }

    public void setScanTimestamp(Long scanTimestamp) {
        this.scanTimestamp = scanTimestamp;
    }

    public String getGpsUtcRaw() {
        return gpsUtcRaw;
    }

    public void setGpsUtcRaw(String gpsUtcRaw) {
        this.gpsUtcRaw = gpsUtcRaw;
    }

    public Long getGpsUtcTimestamp() {
        return gpsUtcTimestamp;
    }

    public void setGpsUtcTimestamp(Long gpsUtcTimestamp) {
        this.gpsUtcTimestamp = gpsUtcTimestamp;
    }

    @Override
    public String toString() {
        return "TcpMessageData{" +
                "deviceId='" + deviceId + '\'' +
                ", commandType='" + commandType + '\'' +
                ", northSouth='" + northSouth + '\'' +
                ", latitude='" + latitude + '\'' +
                ", eastWest='" + eastWest + '\'' +
                ", longitude='" + longitude + '\'' +
                ", alarmValue=" + alarmValue +
                ", gsmSignalStrength=" + gsmSignalStrength +
                ", gpsUtcRaw='" + gpsUtcRaw + '\'' +
                ", batteryLevel=" + batteryLevel +
                ", bluetoothBeacons=" + (bluetoothBeacons != null ? bluetoothBeacons.size() : 0) + " beacons" +
                '}';
    }
}
