/**
 * 区域围栏数据实体类 - 对应TDengine表area_fence_data
 * @author Shawn
 * @date 2025-01-14
 */
package com.jeesite.modules.swm.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 区域围栏数据实体类
 */
public class AreaFenceData implements Serializable {

    private static final long serialVersionUID = 1L;

    private Date time; // 时间戳
    private BigDecimal x; // X坐标
    private BigDecimal y; // Y坐标
    private String areaName; // 区域名称
    private String areaId; // 区域ID
    private String deviceId; // 设备ID (TAG)
    private String idCard; // 身份证号 (TAG)

    public AreaFenceData() {
    }

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }

    public BigDecimal getX() {
        return x;
    }

    public void setX(BigDecimal x) {
        this.x = x;
    }

    public BigDecimal getY() {
        return y;
    }

    public void setY(BigDecimal y) {
        this.y = y;
    }

    public String getAreaName() {
        return areaName;
    }

    public void setAreaName(String areaName) {
        this.areaName = areaName;
    }

    public String getAreaId() {
        return areaId;
    }

    public void setAreaId(String areaId) {
        this.areaId = areaId;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getIdCard() {
        return idCard;
    }

    public void setIdCard(String idCard) {
        this.idCard = idCard;
    }
}