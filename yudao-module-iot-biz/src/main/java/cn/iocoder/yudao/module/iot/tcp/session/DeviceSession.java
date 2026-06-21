package cn.iocoder.yudao.module.iot.tcp.session;

import io.netty.channel.Channel;

import java.time.LocalDateTime;

/**
 * 设备会话实体类
 */
public class DeviceSession {

    /**
     * 会话ID
     */
    private String sessionId;

    /**
     * 设备ID
     */
    private String deviceId;

    /**
     * Netty通道
     */
    private Channel channel;

    /**
     * 客户端地址
     */
    private String clientAddress;

    /**
     * 连接时间
     */
    private LocalDateTime connectTime;

    /**
     * 最后活跃时间
     */
    private LocalDateTime lastActiveTime;

    /**
     * 最后接收的消息
     */
    private String lastMessage;

    /**
     * 消息计数
     */
    private long messageCount = 0;

    /**
     * 设备注册检查标志
     * true-已检查过设备注册，false-未检查
     */
    private boolean deviceRegistrationChecked = false;

    /**
     * 首次连接MAC地址获取指令下发标志
     * true-已下发获取MAC地址指令，false-未下发
     */
    private boolean firstConnectMacCommandSent = false;

    /**
     * 是否已认证
     */
    private boolean authenticated = false;

    /**
     * 设备类型
     */
    private String deviceType;

    /**
     * 设备版本
     */
    private String deviceVersion;

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public Channel getChannel() {
        return channel;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    public String getClientAddress() {
        return clientAddress;
    }

    public void setClientAddress(String clientAddress) {
        this.clientAddress = clientAddress;
    }

    public LocalDateTime getConnectTime() {
        return connectTime;
    }

    public void setConnectTime(LocalDateTime connectTime) {
        this.connectTime = connectTime;
    }

    public LocalDateTime getLastActiveTime() {
        return lastActiveTime;
    }

    public void setLastActiveTime(LocalDateTime lastActiveTime) {
        this.lastActiveTime = lastActiveTime;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public long getMessageCount() {
        return messageCount;
    }

    public void setMessageCount(long messageCount) {
        this.messageCount = messageCount;
    }

    /**
     * 增加消息计数
     */
    public void incrementMessageCount() {
        this.messageCount++;
    }

    public boolean isAuthenticated() {
        return authenticated;
    }

    public void setAuthenticated(boolean authenticated) {
        this.authenticated = authenticated;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    public String getDeviceVersion() {
        return deviceVersion;
    }

    public void setDeviceVersion(String deviceVersion) {
        this.deviceVersion = deviceVersion;
    }

    /**
     * 检查通道是否活跃
     */
    public boolean isActive() {
        return channel != null && channel.isActive();
    }

    /**
     * 发送消息给设备
     */
    public void sendMessage(String message) {
        if (isActive()) {
            channel.writeAndFlush(message);
        }
    }

    /**
     * 获取设备注册检查状态
     *
     * @return true-已检查过设备注册，false-未检查
     */
    public boolean isDeviceRegistrationChecked() {
        return deviceRegistrationChecked;
    }

    /**
     * 设置设备注册检查状态
     *
     * @param deviceRegistrationChecked true-已检查过设备注册，false-未检查
     */
    public void setDeviceRegistrationChecked(boolean deviceRegistrationChecked) {
        this.deviceRegistrationChecked = deviceRegistrationChecked;
    }

    /**
     * 获取首次连接MAC地址指令下发状态
     *
     * @return true-已下发获取MAC地址指令，false-未下发
     */
    public boolean isFirstConnectMacCommandSent() {
        return firstConnectMacCommandSent;
    }

    /**
     * 设置首次连接MAC地址指令下发状态
     *
     * @param firstConnectMacCommandSent true-已下发获取MAC地址指令，false-未下发
     */
    public void setFirstConnectMacCommandSent(boolean firstConnectMacCommandSent) {
        this.firstConnectMacCommandSent = firstConnectMacCommandSent;
    }

    /**
     * 关闭连接
     */
    public void close() {
        if (channel != null) {
            channel.close();
        }
    }

    @Override
    public String toString() {
        return "DeviceSession{" +
                "sessionId='" + sessionId + '\'' +
                ", deviceId='" + deviceId + '\'' +
                ", clientAddress='" + clientAddress + '\'' +
                ", connectTime=" + connectTime +
                ", lastActiveTime=" + lastActiveTime +
                ", messageCount=" + messageCount +
                ", authenticated=" + authenticated +
                ", active=" + isActive() +
                '}';
    }
}
