package cn.iocoder.yudao.module.iot.websocket.service;

import cn.iocoder.yudao.module.iot.websocket.endpoint.WebSocketEndpoint;
import cn.iocoder.yudao.module.iot.websocket.handler.WebSocketMessageHandler;
import cn.iocoder.yudao.module.iot.util.JsonResponseBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Date;

/**
 * WebSocket服务类
 * 提供给业务代码调用的WebSocket服务
 * 
 * @author Shawn
 * @date 2023-11-01
 */
@Service
public class WebSocketService {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketService.class);

    @Autowired
    private WebSocketMessageHandler webSocketMessageHandler;

    /**
     * 通过Handler方式发送消息给指定sessionId的客户端
     */
    public void sendMessageToSession(String sessionId, String message) {
        logger.info("通过Handler发送消息给会话[{}]: {}", sessionId, message);
        webSocketMessageHandler.sendMessageToSession(sessionId, message);
    }

    /**
     * 通过Handler方式发送消息给指定device_id的客户端
     * 
     * @author Shawn
     * @date 2024-07-05
     */
    public void sendMessageToDevice(String deviceId, String message) {
        logger.info("通过Handler发送消息给设备[{}]: {}", deviceId, message);
        webSocketMessageHandler.sendMessageToDevice(deviceId, message);
    }

    /**
     * 通过Handler方式广播消息给所有客户端
     */
    public void broadcastMessageViaHandler(String message) {
        logger.info("通过Handler广播消息: {}", message);
        webSocketMessageHandler.broadcastMessage(message);
    }

    /**
     * 通过Endpoint方式发送消息给指定用户
     */
    public void sendMessageToUser(String userId, String message) {
        logger.info("通过Endpoint发送消息给用户[{}]: {}", userId, message);
        // WebSocketEndpoint.sendMessageToUser(userId, message);
    }

    /**
     * 通过Endpoint方式广播消息给所有用户
     */
    public void broadcastMessageViaEndpoint(String message) {
        logger.info("通过Endpoint广播消息: {}", message);
        // WebSocketEndpoint.broadcastMessage(message);
    }

    /**
     * 获取当前在线用户数
     */
    public int getOnlineUserCount() {
        return WebSocketEndpoint.getOnlineCount();
    }

    /**
     * 发送本地录制控制指令给指定设备
     * 后台发送指令控制设备开启/关闭本地录制
     * 
     * @param deviceId        设备ID
     * @param localRecord     本地录制状态 (0:关闭, 1:开启)
     * @param recordVideoName 录制视频文件名
     * 
     * @author Shawn
     * @date 2025-05-27
     */
    public void sendLocalRecordControlCommand(String deviceId, String localRecord, String recordVideoName) {
        logger.info("发送本地录制控制指令给设备[{}]: localRecord={}, recordVideoName={}",
                deviceId, localRecord, recordVideoName);

        // 使用工具类构建指令JSON
        String commandJson = JsonResponseBuilder
                .buildServerPushSetSosHeightCommand(localRecord, recordVideoName);

        // 发送指令给指定设备
        webSocketMessageHandler.sendMessageToDevice(deviceId, commandJson);
    }

    /**
     * 发送查询本地视频列表指令给指定设备
     * 后台发送指令查询设备本地存储的视频列表
     * 
     * @param deviceId  设备ID
     * @param startTime 开始时间（格式：yyyyMMddHHmmss）
     * @param endTime   结束时间（格式：yyyyMMddHHmmss）
     * @param videoType 视频类型
     * 
     * @author Shawn
     * @date 2024-07-05
     */
    public void sendGetLocalVideoListCommand(String deviceId, String startTime, String endTime, String videoType) {
        logger.info("发送查询本地视频列表指令给设备[{}]: startTime={}, endTime={}, videoType={}",
                deviceId, startTime, endTime, videoType);

        // 使用工具类构建指令JSON
        String commandJson = JsonResponseBuilder
                .buildGetLocalVideoListCommand(startTime, endTime, videoType);

        // 发送指令给指定设备
        webSocketMessageHandler.sendMessageToDevice(deviceId, commandJson);
    }

    /**
     * 发送服务端选择文件上传指令给指定设备
     * 后台发送指令要求设备上传指定文件
     * 
     * @param deviceId   设备ID
     * @param talkbackId 对讲ID
     * @param fileList   文件列表
     * 
     * @author Shawn
     * @date 2024-06-09
     */
    public void sendServerPushUploadCommand(String deviceId, String talkbackId, List<String> fileList) {
        logger.info("发送服务端选择文件上传指令给设备[{}]: talkbackId={}, fileList={}",
                deviceId, talkbackId, fileList);

        // 使用工具类构建指令JSON
        String commandJson = JsonResponseBuilder
                .buildServerPushUploadCommand(talkbackId, fileList);

        // 发送指令给指定设备
        webSocketMessageHandler.sendMessageToDevice(deviceId, commandJson);
    }

    /**
     * 发送开启推流指令给指定设备
     * 9.1.1 后台发送指令控制设备开启推流
     * 
     * @param deviceId 设备ID
     * @param pushUrl  推流地址
     * 
     * @author Shawn
     * @date 2024-07-06
     */
    public void sendOpenPushStreamCommand(String deviceId, String pushUrl) {
        logger.info("发送开启推流指令给设备[{}]: pushUrl={}", deviceId, pushUrl);

        // 使用工具类构建指令JSON
        String commandJson = JsonResponseBuilder.buildServerPushOpenRtspCommand(pushUrl);

        // 发送指令给指定设备
        webSocketMessageHandler.sendMessageToDevice(deviceId, commandJson);
    }

    /**
     * 发送关闭推流指令给指定设备
     * 9.2 后台发送指令控制设备关闭推流
     * 向设备发送数据 {"cmd": "server_push_stop_rtsp"}
     *
     * @param deviceId 设备ID
     * @author Shawn
     * @date 2024-07-05
     */
    public void sendClosePushStreamCommand(String deviceId) {
        logger.info("发送关闭推流指令给设备[{}]", deviceId);
        String commandJson = JsonResponseBuilder.buildServerPushStopRtspCommand();
        webSocketMessageHandler.sendMessageToDevice(deviceId, commandJson);
    }

    /**
     * 获取WebSocket连接状态详情
     * 
     * @author Shawn
     * @date 2024-12-19
     */
    public Map<String, Object> getWebSocketStatusDetail() {
        Map<String, Object> statusDetail = new HashMap<>();

        // 获取连接统计信息
        statusDetail.put("totalConnections", webSocketMessageHandler.getTotalConnections());
        statusDetail.put("deviceConnections", webSocketMessageHandler.getDeviceConnections());
        statusDetail.put("activeConnections", webSocketMessageHandler.getActiveConnections());
        statusDetail.put("connectionDetails", webSocketMessageHandler.getConnectionDetails());

        return statusDetail;
    }

    /**
     * 强制清理指定设备的WebSocket连接
     * 
     * @param deviceId 设备ID
     * @return 清理结果
     * @author Shawn
     * @date 2025-01-04
     */
    public boolean forceCleanupDeviceConnection(String deviceId) {
        logger.info("强制清理设备[{}]的WebSocket连接", deviceId);
        return webSocketMessageHandler.forceCleanupDeviceConnection(deviceId);
    }

    /**
     * 手动触发无效连接清理
     * 
     * @return 清理统计信息
     * @author Shawn
     * @date 2025-01-04
     */
    public Map<String, Object> triggerConnectionCleanup() {
        logger.info("手动触发WebSocket连接清理");

        // 记录清理前的连接数
        int beforeTotalConnections = webSocketMessageHandler.getTotalConnections();
        int beforeDeviceConnections = webSocketMessageHandler.getDeviceConnections();

        // 触发清理（通过调用getConnectionDetails间接触发cleanupInvalidSessions）
        Map<String, Object> connectionDetails = webSocketMessageHandler.getConnectionDetails();

        // 记录清理后的连接数
        int afterTotalConnections = webSocketMessageHandler.getTotalConnections();
        int afterDeviceConnections = webSocketMessageHandler.getDeviceConnections();

        Map<String, Object> cleanupResult = new HashMap<>();
        cleanupResult.put("cleanupTime", new Date());

        // 创建清理前的统计信息
        Map<String, Object> beforeCleanup = new HashMap<>();
        beforeCleanup.put("totalConnections", beforeTotalConnections);
        beforeCleanup.put("deviceConnections", beforeDeviceConnections);
        cleanupResult.put("beforeCleanup", beforeCleanup);

        // 创建清理后的统计信息
        Map<String, Object> afterCleanup = new HashMap<>();
        afterCleanup.put("totalConnections", afterTotalConnections);
        afterCleanup.put("deviceConnections", afterDeviceConnections);
        cleanupResult.put("afterCleanup", afterCleanup);

        cleanupResult.put("cleanedConnections", beforeTotalConnections - afterTotalConnections);
        cleanupResult.put("cleanedDeviceConnections", beforeDeviceConnections - afterDeviceConnections);
        cleanupResult.put("connectionDetails", connectionDetails);

        logger.info("WebSocket连接清理完成，清理了{}个连接（其中{}个设备连接）",
                beforeTotalConnections - afterTotalConnections,
                beforeDeviceConnections - afterDeviceConnections);

        return cleanupResult;
    }
}