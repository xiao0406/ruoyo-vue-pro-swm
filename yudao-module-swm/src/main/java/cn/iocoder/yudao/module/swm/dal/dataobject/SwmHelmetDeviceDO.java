package cn.iocoder.yudao.module.swm.dal.dataobject;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 安全帽设备 DO
 *
 * 迁移自 JeeSite: com.jeesite.modules.swm.entity.SwmHelmetDevice
 * 表: swm_helmet_device
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@TableName("swm_helmet_device")
public class SwmHelmetDeviceDO extends SwmBaseDO {

    /** 头盔编号 */
    private String deviceId;
    /** 头盔类型（枚举 SwmEnums.HelmetTypeEnum） */
    private String helmetType;
    /** 头盔电量(0-100%) */
    private Integer batteryLevel;
    /** IP地址 */
    private String ip;
    /** MAC地址 */
    private String macAddress;
    /** 绑定人员 */
    private String assignedPerson;
    /** 人员姓名 */
    private String personName;
    /** 手机号码 */
    private String personPhone;
    /** 所属车间 */
    private String assignedWorkshop;
    /** 所属工序 */
    private String assignedProcess;
    /** 所属班组 */
    private String assignedTeam;
    /** 运动状态（枚举 SwmEnums.MotionStatusEnum） */
    private String motionStatus;
    /** 绑定时间 */
    private LocalDateTime bindTime;
    /** 解绑时间 */
    private LocalDateTime unbindTime;
    /** 绑定时长(天) */
    private Integer bindDurationDays;
    /** 使用状态（枚举 SwmEnums.HelmetUsageStatusEnum） */
    private String usageStatus;
    /** 服务器IP */
    private String serverIp;
    /** 端口Port */
    private String serverPort;
    /** 蓝牙扫描窗口(秒) */
    private Integer bluetoothScanWindow;
    /** 每组时长(秒) */
    private Integer groupDuration;
    /** 普通信标CS */
    private Integer normalBeaconCs;
    /** 特殊信标CS */
    private Integer specialBeaconCs;
    /** 定位模式（枚举 SwmEnums.LocationModeEnum） */
    private String locationMode;
    /** 深度休眠时长(分钟) */
    private Integer deepSleepDuration;
    /** 蓝牙扫描持续时间窗口(0.1秒为单位) */
    private Integer bluetoothScanDuration;
    /** 发送间隔(秒) */
    private Integer sendInterval;
    /** 危险源重新触发间隔(秒) */
    private Integer hazardRetriggerInterval;
    /** 休眠唤醒时间(秒) */
    private Integer sleepWakeupTime;
    /** 接收信标(名称) */
    private String beaconFilterName;
    /** 脱帽报警时间间隔 */
    private String hatOffAlarmInterval;
    /** 设备来源(0-科利特, 1-中泰) */
    private String deviceSource;
    /** IMEI */
    private String imei;
    /** 帽子颜色 */
    private String deviceColor;

    // ===== 非数据库字段 =====

    @TableField(exist = false)
    private List<String> deviceIdList;
    @TableField(exist = false)
    private String powerOnStatus;
    @TableField(exist = false)
    private List<String> deviceOnlist;
    @TableField(exist = false)
    private String oldDeviceId;
    @TableField(exist = false)
    private String personId;

}
