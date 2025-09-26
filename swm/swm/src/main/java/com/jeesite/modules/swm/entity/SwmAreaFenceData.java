package com.jeesite.modules.swm.entity;

import com.jeesite.common.entity.BaseEntity;

import java.util.Date;

/**
 * TDengine区域围栏数据实体类
 */
public class SwmAreaFenceData extends BaseEntity<SwmAreaFenceData> {

    private static final long serialVersionUID = 1L;

    private Date time;
    private Double x;
    private Double y;
    private String areaName;
    private String areaId;
    private String remarks;
    private String areaType;
    private String deviceId;
    private String idCard;

    private Date startTime;
    private Date endTime;
    private String sortOrder;

    public SwmAreaFenceData() {
        this(null);
    }

    public SwmAreaFenceData(String id) {
        super(id);
    }

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }

    public Double getX() {
        return x;
    }

    public void setX(Double x) {
        this.x = x;
    }

    public Double getY() {
        return y;
    }

    public void setY(Double y) {
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

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public String getAreaType() {
        return areaType;
    }

    public void setAreaType(String areaType) {
        this.areaType = areaType;
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

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public String getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(String sortOrder) {
        this.sortOrder = sortOrder;
    }
}
