/**
 * @author Shawn
 * @date 2025-10-02
 */
package com.jeesite.modules.swm.entity;

import com.jeesite.common.entity.BaseEntity;
import java.util.Date;

/**
 * TDengine 下发指令到设备日志实体类
 */
public class SwmTcpDeviceCommandLog extends BaseEntity<SwmTcpDeviceCommandLog> {

    private static final long serialVersionUID = 1L;

    // TDengine表字段
    private Date time;                  // 时间戳
    private String sendMessage;         // 发送消息
    private String sendStatus;          // 发送状态
    private String errorMessage;        // 错误消息
    private String identityCard;        // 身份证号
    private String personName;          // 人员姓名
    private String deviceId;            // 设备ID (TAG)

    // 查询条件字段
    private Date startTime;             // 查询开始时间
    private Date endTime;               // 查询结束时间
    private String sortOrder;           // 排序方式：DESC（降序）或 ASC（升序）

    public SwmTcpDeviceCommandLog() {
        this(null);
    }

    public SwmTcpDeviceCommandLog(String id) {
        super(id);
    }

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }

    public String getSendMessage() {
        return sendMessage;
    }

    public void setSendMessage(String sendMessage) {
        this.sendMessage = sendMessage;
    }

    public String getSendStatus() {
        return sendStatus;
    }

    public void setSendStatus(String sendStatus) {
        this.sendStatus = sendStatus;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getIdentityCard() {
        return identityCard;
    }

    public void setIdentityCard(String identityCard) {
        this.identityCard = identityCard;
    }

    public String getPersonName() {
        return personName;
    }

    public void setPersonName(String personName) {
        this.personName = personName;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
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
