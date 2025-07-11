/**
 * @author Shawn
 * @date 2025-05-21
 */
package com.jeesite.modules.swm.entity;

import com.jeesite.common.entity.DataEntity;
import com.jeesite.common.mybatis.annotation.Column;
import com.jeesite.common.mybatis.annotation.Table;
import com.jeesite.common.mybatis.mapper.query.QueryType;
import com.jeesite.modules.sys.utils.DictUtils;
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
        @Column(name = "person_name", attrName = "personName", label = "人员姓名"),
        @Column(name = "person_phone", attrName = "personPhone", label = "手机号码"),
        @Column(name = "assigned_workshop", attrName = "assignedWorkshop", label = "所属车间"),
        @Column(name = "assigned_process", attrName = "assignedProcess", label = "所属工序"),
        @Column(name = "assigned_team", attrName = "assignedTeam", label = "所属班组"),
        @Column(name = "motion_status", attrName = "motionStatus", label = "运动状态"),
        @Column(name = "bind_time", attrName = "bindTime", label = "绑定时间"),
        @Column(name = "unbind_time", attrName = "unbindTime", label = "解绑时间"),
        @Column(name = "bind_duration_days", attrName = "bindDurationDays", label = "绑定时长(天)"),
        @Column(name = "usage_status", attrName = "usageStatus", label = "使用状态(0-已解绑, 1-使用中)"),
        @Column(includeEntity = DataEntity.class)
}, orderBy = "a.update_date DESC")
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

    // 非数据库字段，用于查询条件
    private List<String> deviceIdList; // 设备ID列表，用于批量查询

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
}