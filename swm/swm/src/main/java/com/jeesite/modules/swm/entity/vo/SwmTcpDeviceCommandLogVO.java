/**
 * @author Shawn
 * @date 2025-10-02
 */
package com.jeesite.modules.swm.entity.vo;

import java.util.Date;

/**
 * TDengine 下发指令到设备日志前端展示VO
 */
public class SwmTcpDeviceCommandLogVO {

    private Date time;                  // 时间戳
    private String sendMessage;         // 发送消息
    private String sendStatus;          // 发送状态
    private String errorMessage;        // 错误消息
    private String identityCard;        // 身份证号
    private String personName;          // 人员姓名
    private String deviceId;            // 设备ID

    // 格式化显示字段
    private String timeText;            // 时间格式化文本

    public SwmTcpDeviceCommandLogVO() {
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

    public String getTimeText() {
        return timeText;
    }

    public void setTimeText(String timeText) {
        this.timeText = timeText;
    }
}
