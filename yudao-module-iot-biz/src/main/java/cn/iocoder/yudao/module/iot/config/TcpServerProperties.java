package cn.iocoder.yudao.module.iot.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * TCP服务器配置属性类
 */
@ConfigurationProperties(prefix = "iot.tcp")
public class TcpServerProperties {

    /**
     * 是否启用TCP服务器
     */
    private boolean enabled = true;

    /**
     * TCP服务器端口
     */
    private int port = 8992;

    /**
     * Boss线程数
     */
    private int bossThread = 1;

    /**
     * Worker线程数
     * 默认 16（原为 4）。1000+ TCP 设备需要足够的 EventLoop 线程
     * 来处理 I/O 事件，避免设备间互相影响。
     */
    private int workerThread = 16;

    /**
     * SO_BACKLOG参数
     */
    private int soBacklog = 1024;

    /**
     * SO_KEEPALIVE参数
     */
    private boolean soKeepAlive = true;

    /**
     * TCP_NODELAY参数
     */
    private boolean tcpNoDelay = true;

    /**
     * 心跳检测间隔（毫秒）
     */
    private long heartbeatInterval = 90000;

    /**
     * 连接超时时间（毫秒）
     */
    private long connectTimeout = 60000;

    /**
     * 接收缓冲区大小
     */
    private int receiveBufferSize = 1024 * 64;

    /**
     * 发送缓冲区大小
     */
    private int sendBufferSize = 1024 * 64;

    /**
     * 最大帧长度
     */
    private int maxFrameLength = 1024 * 10;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getBossThread() {
        return bossThread;
    }

    public void setBossThread(int bossThread) {
        this.bossThread = bossThread;
    }

    public int getWorkerThread() {
        return workerThread;
    }

    public void setWorkerThread(int workerThread) {
        this.workerThread = workerThread;
    }

    public int getSoBacklog() {
        return soBacklog;
    }

    public void setSoBacklog(int soBacklog) {
        this.soBacklog = soBacklog;
    }

    public boolean isSoKeepAlive() {
        return soKeepAlive;
    }

    public void setSoKeepAlive(boolean soKeepAlive) {
        this.soKeepAlive = soKeepAlive;
    }

    public boolean isTcpNoDelay() {
        return tcpNoDelay;
    }

    public void setTcpNoDelay(boolean tcpNoDelay) {
        this.tcpNoDelay = tcpNoDelay;
    }

    public long getHeartbeatInterval() {
        return heartbeatInterval;
    }

    public void setHeartbeatInterval(long heartbeatInterval) {
        this.heartbeatInterval = heartbeatInterval;
    }

    public long getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(long connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public int getReceiveBufferSize() {
        return receiveBufferSize;
    }

    public void setReceiveBufferSize(int receiveBufferSize) {
        this.receiveBufferSize = receiveBufferSize;
    }

    public int getSendBufferSize() {
        return sendBufferSize;
    }

    public void setSendBufferSize(int sendBufferSize) {
        this.sendBufferSize = sendBufferSize;
    }

    public int getMaxFrameLength() {
        return maxFrameLength;
    }

    public void setMaxFrameLength(int maxFrameLength) {
        this.maxFrameLength = maxFrameLength;
    }
}
