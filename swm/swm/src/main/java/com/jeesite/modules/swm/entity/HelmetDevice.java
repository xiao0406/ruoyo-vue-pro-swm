package com.jeesite.modules.swm.entity;

import com.jeesite.common.entity.DataEntity;
import com.jeesite.common.mybatis.annotation.Table;

/**
 * 安全帽设备实体类
 * 
 * @author Shawn
 * @date 2025-01-31
 */
@Table(name = "swm_helmet_device", alias = "a", label = "安全帽设备信息")
public class HelmetDevice extends DataEntity<HelmetDevice> {

    private static final long serialVersionUID = 1L;

    private String deviceId; // 设备ID
    private String assignedPerson; // 绑定人员（身份证ID）
    private Integer usageStatus; // 使用状态

    public HelmetDevice() {
        this(null);
    }

    public HelmetDevice(String id) {
        super(id);
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getAssignedPerson() {
        return assignedPerson;
    }

    public void setAssignedPerson(String assignedPerson) {
        this.assignedPerson = assignedPerson;
    }

    public Integer getUsageStatus() {
        return usageStatus;
    }

    public void setUsageStatus(Integer usageStatus) {
        this.usageStatus = usageStatus;
    }
}