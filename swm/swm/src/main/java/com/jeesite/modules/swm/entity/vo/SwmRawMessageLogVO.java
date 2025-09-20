/**
 * @author Shawn
 * @date 2025-09-20
 */
package com.jeesite.modules.swm.entity.vo;

import java.util.Date;

/**
 * TDengine原始消息日志前端展示VO
 */
public class SwmRawMessageLogVO {

    private Date time;                  // 时间戳
    private String sessionId;           // 会话ID
    private Long messageTime;           // 消息时间
    private String messageContent;      // 消息内容
    private Integer originalLength;     // 原始长度
    private Integer isTruncated;        // 是否截断
    private String deviceId;            // 设备ID

    // 格式化显示字段
    private String timeText;            // 时间格式化文本
    private String isTruncatedText;     // 是否截断文本

    public SwmRawMessageLogVO() {
    }

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public Long getMessageTime() {
        return messageTime;
    }

    public void setMessageTime(Long messageTime) {
        this.messageTime = messageTime;
    }

    public String getMessageContent() {
        return messageContent;
    }

    public void setMessageContent(String messageContent) {
        this.messageContent = messageContent;
    }

    public Integer getOriginalLength() {
        return originalLength;
    }

    public void setOriginalLength(Integer originalLength) {
        this.originalLength = originalLength;
    }

    public Integer getIsTruncated() {
        return isTruncated;
    }

    public void setIsTruncated(Integer isTruncated) {
        this.isTruncated = isTruncated;
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

    public String getIsTruncatedText() {
        return isTruncatedText;
    }

    public void setIsTruncatedText(String isTruncatedText) {
        this.isTruncatedText = isTruncatedText;
    }
}