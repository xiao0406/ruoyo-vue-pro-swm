/**
 * @author Shawn
 * @date 2023-05-30
 */
package com.jeesite.modules.swm.entity;

import com.jeesite.common.entity.DataEntity;
import com.jeesite.common.mybatis.annotation.Column;
import com.jeesite.common.mybatis.annotation.Table;
import com.jeesite.common.mybatis.mapper.query.QueryType;
import org.hibernate.validator.constraints.Length;

/**
 * 头盔设备管理实体类
 * 
 * @author Shawn
 */
@Table(name = "swm_helmet_device", alias = "a", columns = {
        @Column(name = "id", attrName = "id", label = "主键ID", isPK = true),
        @Column(name = "helmet_id", attrName = "helmetId", label = "头盔编号", queryType = QueryType.LIKE),
        @Column(name = "helmet_type", attrName = "helmetType", label = "头盔类型(1:便携式 2:头箍式)"),
        @Column(name = "battery_level", attrName = "batteryLevel", label = "头盔电量(0-100%)"),
        @Column(name = "ip", attrName = "ip", label = "IP地址"),
        @Column(name = "mac_address", attrName = "macAddress", label = "MAC地址"),
        @Column(name = "assigned_person", attrName = "assignedPerson", label = "绑定人员"),
        @Column(name = "assigned_workshop", attrName = "assignedWorkshop", label = "所属车间"),
        @Column(name = "assigned_process", attrName = "assignedProcess", label = "所属工序"),
        @Column(name = "assigned_team", attrName = "assignedTeam", label = "所属班组"),
        @Column(name = "motion_status", attrName = "motionStatus", label = "运动状态"),
        @Column(includeEntity = DataEntity.class)
}, orderBy = "a.update_date DESC")
public class SwmHelmetDevice extends DataEntity<SwmHelmetDevice> {

    private static final long serialVersionUID = 1L;

    /**
     * 头盔类型枚举
     */
    public static class HelmetTypeEnum {
        /** 便携式 */
        public static final Integer PORTABLE = 1;
        /** 头箍式 */
        public static final Integer HEADBAND = 2;

        /**
         * 获取头盔类型显示文本
         */
        public static String getText(Integer value) {
            if (value == null) {
                return "";
            }
            if (PORTABLE.equals(value)) {
                return "便携式";
            } else if (HEADBAND.equals(value)) {
                return "头箍式";
            }
            return "";
        }
    }

    /**
     * 运动状态枚举
     */
    public static class MotionStatusEnum {
        /** 静止 */
        public static final String STATIC = "0";
        /** 移动 */
        public static final String MOVING = "1";

        /**
         * 获取运动状态显示文本
         */
        public static String getText(String value) {
            if (STATIC.equals(value)) {
                return "静止";
            } else if (MOVING.equals(value)) {
                return "移动";
            }
            return "";
        }
    }

    private String helmetId; // 头盔编号
    private Integer helmetType; // 头盔类型(1:便携式 2:头箍式)
    private Integer batteryLevel; // 头盔电量 (0-100%)
    private String ip; // IP地址
    private String macAddress; // MAC地址
    private String assignedPerson; // 绑定人员
    private String assignedWorkshop;// 所属车间
    private String assignedProcess; // 所属工序
    private String assignedTeam; // 所属班组
    private String motionStatus; // 运动状态

    public SwmHelmetDevice() {
        super();
    }

    public SwmHelmetDevice(String id) {
        super(id);
    }

    @Length(min = 0, max = 50, message = "头盔编号长度不能超过50个字符")
    public String getHelmetId() {
        return helmetId;
    }

    public void setHelmetId(String helmetId) {
        this.helmetId = helmetId;
    }

    public Integer getHelmetType() {
        return helmetType;
    }

    /**
     * 获取头盔类型显示值
     */
    public String getHelmetTypeText() {
        return HelmetTypeEnum.getText(helmetType);
    }

    public void setHelmetType(Integer helmetType) {
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
}