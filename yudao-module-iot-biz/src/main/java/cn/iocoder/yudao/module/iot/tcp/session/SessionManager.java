package cn.iocoder.yudao.module.iot.tcp.session;

import cn.iocoder.yudao.module.iot.config.SpringContextHolder;
// TODO: Replace RedisUtil with Yudao's RedisService (cn.iocoder.yudao.framework.common.util.RedisUtils)
import cn.iocoder.yudao.module.iot.tcp.util.RedisUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 设备会话管理器
 */
public class SessionManager {

    private static final Logger logger = LoggerFactory.getLogger(SessionManager.class);

    /**
     * TCP指令日志记录器（通过setter注入）
     */
    private cn.iocoder.yudao.module.iot.tcp.util.TcpCommandLogAsyncRecorder commandLogRecorder;

    /**
     * 会话存储 - 按会话ID索引
     */
    private final Map<String, DeviceSession> sessionMap = new ConcurrentHashMap<>();

    /**
     * 设备ID到会话ID的映射
     */
    private final Map<String, String> deviceToSessionMap = new ConcurrentHashMap<>();

    /**
     * 定时清理任务执行器
     */
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    /**
     * 会话超时时间（分钟）
     */
    private static final long SESSION_TIMEOUT_MINUTES = 30;

    //  手动获取 Spring 容器中的 RedisTemplate Bean
    private RedisUtil redisUtil;
    private RedisUtil getRedisUtil() {
        if (redisUtil == null) {
            redisUtil = SpringContextHolder.getBean(RedisUtil.class);
        }
        return redisUtil;
    }




    public SessionManager() {
        logger.info("SessionManager 正在初始化...");
        // 启动定时清理任务，每5分钟执行一次
        scheduler.scheduleAtFixedRate(this::cleanupInactiveSessions, 5, 5, TimeUnit.MINUTES);
        logger.info("SessionManager 初始化完成");
    }

    /**
     * 添加会话
     */
    public void addSession(String sessionId, DeviceSession session) {
        session.setSessionId(sessionId);
        sessionMap.put(sessionId, session);

        // 如果设备ID不为空，建立设备ID到会话ID的映射
        if (session.getDeviceId() != null) {
            deviceToSessionMap.put(session.getDeviceId(), sessionId);
            //  设置设备在线状态，带自动过期
            this.getRedisUtil().setDeviceOnline(session.getDeviceId());
        }

        logger.info("添加设备会话：{}", session);
    }

    /**
     * 移除会话
     */
    public void removeSession(String sessionId) {
        DeviceSession session = sessionMap.remove(sessionId);
        if (session != null) {
            // 移除设备ID映射
            if (session.getDeviceId() != null) {
                deviceToSessionMap.remove(session.getDeviceId());
                //  从Redis删除在线状态
                this.getRedisUtil().removeDeviceOnline(session.getDeviceId());
            }
            logger.info("移除设备会话：{}", session);
        }
    }

    /**
     * 心跳续期（设备上报心跳时调用）
     */
    public void refreshOnlineStatus(String deviceId) {
        if (deviceId != null) {
            this.getRedisUtil().refreshDeviceOnline(deviceId);
        }
    }

    /**
     * 根据会话ID获取会话
     */
    public DeviceSession getSession(String sessionId) {
        return sessionMap.get(sessionId);
    }

    /**
     * 根据设备ID获取会话
     */
    public DeviceSession getSessionByDeviceId(String deviceId) {
        String sessionId = deviceToSessionMap.get(deviceId);
        return sessionId != null ? sessionMap.get(sessionId) : null;
    }

    /**
     * 获取所有会话
     */
    public Collection<DeviceSession> getAllSessions() {
        return new ArrayList<>(sessionMap.values());
    }

    /**
     * 获取活跃会话列表
     */
    public List<DeviceSession> getActiveSessions() {
        List<DeviceSession> activeSessions = new ArrayList<>();
        for (DeviceSession session : sessionMap.values()) {
            if (session.isActive()) {
                activeSessions.add(session);
            }
        }
        return activeSessions;
    }

    /**
     * 获取会话总数
     */
    public int getSessionCount() {
        return sessionMap.size();
    }

    /**
     * 获取活跃会话数
     */
    public int getActiveSessionCount() {
        return (int) sessionMap.values().stream()
                .filter(DeviceSession::isActive)
                .count();
    }

    /**
     * 关闭所有会话
     */
    public void closeAllSessions() {
        logger.info("关闭所有设备会话，当前会话数：{}", sessionMap.size());

        for (DeviceSession session : sessionMap.values()) {
            try {
                session.close();
            } catch (Exception e) {
                logger.error("关闭会话时发生异常：{}", e.getMessage());
            }
        }
        sessionMap.clear();
        deviceToSessionMap.clear();
    }

    /**
     * 向指定设备发送消息
     */
    public boolean sendMessageToDevice(String deviceId, String message) {
        DeviceSession session = getSessionByDeviceId(deviceId);
        if (session != null && session.isActive()) {
            try {
                session.sendMessage(message);

                // 记录成功日志
                if (commandLogRecorder != null) {
                    commandLogRecorder.saveAsync(deviceId, message);
                }

                return true;
            } catch (Exception e) {
                logger.error("发送消息到设备失败, deviceId: {}", deviceId, e);

                // 记录失败日志
                if (commandLogRecorder != null) {
                    commandLogRecorder.saveAsync(deviceId, message, "发送失败: " + e.getMessage());
                }

                return false;
            }
        }

        // 设备不在线或会话不活跃，记录失败日志
        if (commandLogRecorder != null) {
            commandLogRecorder.saveAsync(deviceId, message, "设备不在线或会话不活跃");
        }

        return false;
    }

    /**
     * 广播消息给所有活跃设备
     */
    public int broadcastMessage(String message) {
        int sentCount = 0;
        for (DeviceSession session : sessionMap.values()) {
            if (session.isActive()) {
                try {
                    session.sendMessage(message);

                    // 记录广播成功日志
                    if (commandLogRecorder != null) {
                        commandLogRecorder.saveAsync(session.getDeviceId(), message);
                    }

                    sentCount++;
                } catch (Exception e) {
                    logger.error("广播消息失败，设备：{}，错误：{}", session.getDeviceId(), e.getMessage());

                    // 记录广播失败日志
                    if (commandLogRecorder != null) {
                        commandLogRecorder.saveAsync(session.getDeviceId(), message,
                                "广播失败: " + e.getMessage());
                    }
                }
            }
        }
        return sentCount;
    }

    /**
     * 更新设备ID映射
     */
    public void updateDeviceMapping(String sessionId, String deviceId) {
        DeviceSession session = sessionMap.get(sessionId);
        if (session != null) {
            String oldDeviceId = session.getDeviceId();
            // 设备ID没变，不需要重复删重建 Redis
            if (deviceId != null && deviceId.equals(oldDeviceId)) {
                return;
            }

            // 移除旧的设备ID映射
            if (oldDeviceId != null) {
                deviceToSessionMap.remove(oldDeviceId);
                // 从Redis在线设备集合中移除旧设备
                this.getRedisUtil().removeDeviceOnline(oldDeviceId);
            }

            // 更新设备ID
            session.setDeviceId(deviceId);

            // 建立新的设备ID映射
            if (deviceId != null) {
                deviceToSessionMap.put(deviceId, sessionId);
                // 将新设备添加到Redis在线设备集合中
                this.getRedisUtil().setDeviceOnline(deviceId);
            }

            logger.info("更新设备ID映射：会话[{}] -> 设备[{}]", sessionId, deviceId);
        }
    }

    /**
     * 清理非活跃会话
     */
    private void cleanupInactiveSessions() {
        try {
            LocalDateTime cutoffTime = LocalDateTime.now().minusMinutes(SESSION_TIMEOUT_MINUTES);
            List<String> sessionsToRemove = new ArrayList<>();

            for (Map.Entry<String, DeviceSession> entry : sessionMap.entrySet()) {
                DeviceSession session = entry.getValue();

                // 检查会话是否超时或连接已断开
                if (!session.isActive() ||
                        (session.getLastActiveTime() != null && session.getLastActiveTime().isBefore(cutoffTime))) {
                    logger.info("会话已超时或已断开，将关闭会话：sessionId={}, deviceId={}, lastActiveTime={}, cutoffTime={}",
                            session.getSessionId(), session.getDeviceId(),null,null);
                    sessionsToRemove.add(entry.getKey());
                }
            }

            // 移除非活跃会话
            for (String sessionId : sessionsToRemove) {
                removeSession(sessionId);
            }

            if (!sessionsToRemove.isEmpty()) {
                logger.info("清理非活跃会话数量：{}，当前活跃会话数：{}",
                        sessionsToRemove.size(), getActiveSessionCount());
            }

        } catch (Exception e) {
            logger.error("清理非活跃会话时发生异常", e);
        }
    }

    /**
     * 设置TCP指令日志记录器
     *
     * @param commandLogRecorder 日志记录器
     */
    public void setCommandLogRecorder(cn.iocoder.yudao.module.iot.tcp.util.TcpCommandLogAsyncRecorder commandLogRecorder) {
        this.commandLogRecorder = commandLogRecorder;
        logger.info("SessionManager 已设置 TcpCommandLogAsyncRecorder");
    }

    /**
     * 销毁资源
     */
    public void destroy() {
        closeAllSessions();
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }


}
