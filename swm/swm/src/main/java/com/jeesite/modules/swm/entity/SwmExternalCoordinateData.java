/**
 * @author Shawn
 * @date 2025-09-20
 */
package com.jeesite.modules.swm.entity;

import com.jeesite.common.entity.BaseEntity;

import java.util.Date;

/**
 * TDengine外部坐标数据实体类
 */
public class SwmExternalCoordinateData extends BaseEntity<SwmExternalCoordinateData> {

    private static final long serialVersionUID = 1L;

    private Date time;
    private String address;
    private Integer mapId;
    private Double x;
    private Double y;
    private String orgCd;
    private String timeStr;
    private String type;
    private String appId;
    private Integer warningId;
    private Double originalX;
    private Double originalY;
    private Double scaleX;
    private Double scaleY;
    private String nearestBeacon;
    private String usedBeacons;
    private String elderId;
    private String idCard;

    private Date startTime;
    private Date endTime;
    private String sortOrder;

    public SwmExternalCoordinateData() {
        this(null);
    }

    public SwmExternalCoordinateData(String id) {
        super(id);
    }

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Integer getMapId() {
        return mapId;
    }

    public void setMapId(Integer mapId) {
        this.mapId = mapId;
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

    public String getOrgCd() {
        return orgCd;
    }

    public void setOrgCd(String orgCd) {
        this.orgCd = orgCd;
    }

    public String getTimeStr() {
        return timeStr;
    }

    public void setTimeStr(String timeStr) {
        this.timeStr = timeStr;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public Integer getWarningId() {
        return warningId;
    }

    public void setWarningId(Integer warningId) {
        this.warningId = warningId;
    }

    public Double getOriginalX() {
        return originalX;
    }

    public void setOriginalX(Double originalX) {
        this.originalX = originalX;
    }

    public Double getOriginalY() {
        return originalY;
    }

    public void setOriginalY(Double originalY) {
        this.originalY = originalY;
    }

    public Double getScaleX() {
        return scaleX;
    }

    public void setScaleX(Double scaleX) {
        this.scaleX = scaleX;
    }

    public Double getScaleY() {
        return scaleY;
    }

    public void setScaleY(Double scaleY) {
        this.scaleY = scaleY;
    }

    public String getNearestBeacon() {
        return nearestBeacon;
    }

    public void setNearestBeacon(String nearestBeacon) {
        this.nearestBeacon = nearestBeacon;
    }

    public String getUsedBeacons() {
        return usedBeacons;
    }

    public void setUsedBeacons(String usedBeacons) {
        this.usedBeacons = usedBeacons;
    }

    public String getElderId() {
        return elderId;
    }

    public void setElderId(String elderId) {
        this.elderId = elderId;
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
