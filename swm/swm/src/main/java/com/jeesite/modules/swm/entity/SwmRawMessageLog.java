/**
 * @author Shawn
 * @date 2025-09-20
 */
package com.jeesite.modules.swm.entity;

import com.jeesite.common.entity.BaseEntity;
import java.util.Date;

/**
 * TDengine原始消息日志实体类
 */
public class SwmRawMessageLog extends BaseEntity<SwmRawMessageLog> {

    private static final long serialVersionUID = 1L;

    // TDengine表字段
    private Date time;                  // 时间戳
    private String sessionId;           // 会话ID
    private Long messageTime;           // 消息时间
    private String messageContent;      // 消息内容
    private Integer originalLength;     // 原始长度
    private Integer isTruncated;        // 是否截断
    private String deviceId;            // 设备ID

    // 查询条件字段
    private Date startTime;             // 查询开始时间
    private Date endTime;               // 查询结束时间
    private String sortOrder;           // 排序方式：DESC（降序）或 ASC（升序）

    public SwmRawMessageLog() {
        this(null);
    }

    public SwmRawMessageLog(String id) {
        super(id);
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