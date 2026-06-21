package cn.iocoder.yudao.module.iot.websocket.handler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Date;

/**
 * WebSocket消息处理器
 *
 * @author Shawn
 * @date 2023-11-01
 */
@Component
public class WebSocketMessageHandler implements WebSocketHandler {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketMessageHandler.class);

    // 存储会话的集合
    private static final Map<String, WebSocketSession> SESSIONS = new ConcurrentHashMap<>();

    // 存储deviceId到session的映射关系
    private static final Map<String, WebSocketSession> DEVICE_SESSIONS = new ConcurrentHashMap<>();

    // 存储会话的最后活跃时间
    private static final Map<String, Long> SESSION_LAST_ACTIVE = new ConcurrentHashMap<>();

    private final ObjectMapper mapper = new ObjectMapper();
    private final Map<String, WebSocketMessageProcessor> messageProcessors = new HashMap<>();

    @Resource
    private cn.iocoder.yudao.module.swm.service.UnknownMessageTdEngineService unknownMessageTdEngineService;

    @Resource
    private cn.iocoder.yudao.module.swm.service.RawMessageTdEngineService rawMessageTdEngineService;

    @Resource
    private cn.iocoder.yudao.module.iot.websocket.processor.CaLoginProcessor caLoginProcessor;

    @Resource
    private cn.iocoder.yudao.module.iot.websocket.processor.CaReportLocationProcessor caReportLocationProcessor;

    @Resource
    private cn.iocoder.yudao.module.iot.websocket.processor.PingProcessor pingProcessor;

    @Resource
    private cn.iocoder.yudao.module.iot.websocket.processor.CaSosProcessor caSosProcessor;

    @Resource
    private cn.iocoder.yudao.module.iot.websocket.processor.CaSendingMessageProcessor caSendingMessageProcessor;

    @Resource
    private cn.iocoder.yudao.module.iot.websocket.processor.ToggleLocalRecordProcessor toggleLocalRecordProcessor;

    @Resource
    private cn.iocoder.yudao.module.iot.websocket.processor.CaUploadPhotoProcessor caUploadPhotoProcessor;

    @Resource
    private cn.iocoder.yudao.module.iot.websocket.processor.PushToClientProcessor pushToClientProcessor;

    @Resource
    private cn.iocoder.yudao.module.iot.websocket.processor.CaSipSosProcessor caSipSosProcessor;

    /**
     * 构造函数，注册所有消息处理器
     */
    public WebSocketMessageHandler() {
        // 注册消息处理器
        registerProcessor("ca_login", this::processCaLogin);
        registerProcessor("ca_report_location", this::processCaReportLocation);
        registerProcessor("ca_sos", this::processCaSos);
        registerProcessor("ca_sip_sos", this::processCaSipSos);
        registerProcessor("ca_sending_message", this::processCaSendingMessage);
        registerProcessor("toggle_localrecord", this::processToggleLocalRecord);
        registerProcessor("ca_upload_photo", this::processCaUploadPhoto);
        registerProcessor("push_to_client", this::processPushToClient);
        registerProcessor("ping", this::processPing); // 保留ping处理器，但不主动发送
        // 可以在这里继续注册更多的处理器
    }

    /**
     * 注册消息处理器
     */
    private void registerProcessor(String actionType, WebSocketMessageProcessor processor) {
        messageProcessors.put(actionType, processor);
    }

    /**
     * 连接建立后触发
     */
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String sessionId = session.getId();
        SESSIONS.put(sessionId, session);
        SESSION_LAST_ACTIVE.put(sessionId, System.currentTimeMillis());

        // 记录连接详细信息
        String remoteAddress = session.getRemoteAddress() != null ? session.getRemoteAddress().toString() : "unknown";
        logger.info("WebSocket连接建立, sessionId: {}, 远程地址: {}, 当前连接数: {}",
                sessionId, remoteAddress, SESSIONS.size());

        // 发送连接成功消息
        sendMessage(session, "连接已建立，sessionId: " + sessionId);
    }

    /**
     * 收到消息时触发
     */
    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
        String sessionId = session.getId();
        String payload = message.getPayload().toString();

        // 更新最后活跃时间
        SESSION_LAST_ACTIVE.put(sessionId, System.currentTimeMillis());

        // 获取device_id，如果不存在则显示"未知"
        String deviceId = session.getAttributes().get("device_id") != null
                ? session.getAttributes().get("device_id").toString()
                : "未知";
        logger.info("收到WebSocket消息, sessionId: {}, deviceId: {}, 消息内容: {}", sessionId, deviceId, payload);

        // 保存原始消息到TDengine超级表
        try {
            // 调用原始消息保存服务
            rawMessageTdEngineService.saveRawMessageData(deviceId, sessionId, payload);
        } catch (Exception e) {
            // 捕获异常但不影响后续处理
            logger.error("保存原始消息到TDengine失败, sessionId: {}, deviceId: {}, 错误: {}",
                    sessionId, deviceId, e.getMessage(), e);
        }

        try {
            // 预处理payload，去除换行符和其他控制字符
            String cleanedPayload = payload
                    .replaceAll("\\r\\n|\\r|\\n", "") // 去除换行符
                    .replaceAll("\\s+", " ") // 将多个空格替换为单个空格
                    .trim(); // 去除首尾空格

            // 使用Jackson解析JSON消息
            JsonNode jsonNode = mapper.readTree(cleanedPayload);

            // 检查是否包含qj_to_device_id字段
            if (jsonNode.has("qj_to_device_id")) {
                String toDeviceId = jsonNode.get("qj_to_device_id").asText();
                // 获取目标设备的WebSocket会话
                WebSocketSession targetSession = getSessionByDeviceId(toDeviceId);

                if (targetSession != null && targetSession.isOpen()) {
                    // 创建不包含qj_to_device_id的新消息对象
                    ObjectMapper objectMapper = new ObjectMapper();
                    JsonNode rootNode = objectMapper.readTree(cleanedPayload);
                    ((com.fasterxml.jackson.databind.node.ObjectNode) rootNode).remove("qj_to_device_id");
                    String forwardPayload = rootNode.toString();

                    // 转发消息给目标设备
                    sendMessage(targetSession, forwardPayload);
                    logger.info("已转发消息至设备 {}, 消息内容: {}", toDeviceId, forwardPayload);

                    // 向发送者回复转发成功的消息，包含目标设备ID
                    String responseJson = String.format(
                            "{\"status\":true,\"msg\":\"消息已转发到设备[%s]\",\"msg_code\":\"ForwardSucc\",\"to_device_id\":\"%s\"}",
                            toDeviceId, toDeviceId);
                    sendMessage(session, responseJson);
                    return;
                } else {
                    // 目标设备不在线，发送失败消息，包含目标设备ID
                    String responseJson = String.format(
                            "{\"status\":false,\"msg\":\"目标设备[%s]不在线\",\"msg_code\":\"ForwardFail\",\"to_device_id\":\"%s\"}",
                            toDeviceId, toDeviceId);
                    sendMessage(session, responseJson);
                    logger.warn("消息转发失败，目标设备 {} 不在线", toDeviceId);
                    return;
                }
            }

            // 获取动作类型
            String actionType = jsonNode.has("act") ? jsonNode.get("act").asText() : null;

            if (actionType != null && messageProcessors.containsKey(actionType)) {
                // 使用对应的处理器处理消息
                messageProcessors.get(actionType).process(session, jsonNode, cleanedPayload);
            } else {
                // 未知动作类型，保存到时序数据库并原样返回
                processUnknownMessage(session, cleanedPayload);
                sendMessage(session, cleanedPayload);
            }
        } catch (com.fasterxml.jackson.core.JsonParseException jpe) {
            // JSON解析异常，记录详细错误信息并保存到时序数据库
            logger.error("JSON解析失败, sessionId: {}, deviceId: {}, 原始消息: {}, 错误: {}",
                    sessionId, deviceId, payload, jpe.getMessage());

            // 保存JSON解析异常消息到时序数据库
            processUnknownMessage(session, payload);

            // 尝试发送错误响应给客户端
            String errorResponse = String.format(
                    "{\"status\":false,\"msg\":\"JSON格式错误: %s\",\"msg_code\":\"JsonParseError\"}",
                    jpe.getMessage().replaceAll("\"", "'"));
            sendMessage(session, errorResponse);
        } catch (Exception e) {
            // 其他异常，保存到时序数据库并原样返回
            logger.error("处理WebSocket消息时发生异常, sessionId: {}, deviceId: {}, 消息: {}, 错误: {}",
                    sessionId, deviceId, payload, e.getMessage(), e);

            // 保存异常消息到时序数据库
            processUnknownMessage(session, payload);

            sendMessage(session, payload);
        }
    }

    /**
     * 处理ping消息（心跳）
     * 只响应客户端发送的ping，不主动发送
     *
     * @author Shawn
     * @date 2024-12-19
     */
    private void processPing(WebSocketSession session, JsonNode jsonNode, String originalPayload)
            throws IOException {
        // 调用专门的Ping处理器
        pingProcessor.process(session, jsonNode, originalPayload, SESSION_LAST_ACTIVE, this::sendMessage);
    }

    /**
     * 处理未知消息类型
     * 保存到时序数据库用于日志记录
     *
     * @param session        WebSocket会话
     * @param messageContent 消息内容
     * @author Shawn
     * @date 2025-01-31
     */
    private void processUnknownMessage(WebSocketSession session, String messageContent) {
        String sessionId = session.getId();

        // 获取设备ID，如果不存在则为null
        String deviceId = session.getAttributes().get("device_id") != null
                ? session.getAttributes().get("device_id").toString()
                : null;

        logger.info("处理未知消息类型, sessionId: {}, deviceId: {}, 消息长度: {}",
                sessionId, deviceId, messageContent.length());

        try {
            // 保存未知消息到时序数据库
            cn.iocoder.yudao.module.swm.util.R<cn.hutool.json.JSONObject> result = unknownMessageTdEngineService
                    .saveUnknownMessageData(deviceId, sessionId, messageContent);

            if (result.getCode() != cn.iocoder.yudao.module.swm.util.R.SUCCESS) {
                logger.error("保存未知消息到时序数据库失败, sessionId: {}, deviceId: {}, 错误: {}",
                        sessionId, deviceId, result.getMsg());
            } else {
                logger.info("未知消息已保存到时序数据库, sessionId: {}, deviceId: {}", sessionId, deviceId);
            }
        } catch (Exception e) {
            logger.error("保存未知消息到时序数据库异常, sessionId: {}, deviceId: {}", sessionId, deviceId, e);
        }
    }

    /**
     * 处理ca_login消息
     * 2.设备登录
     */
    private void processCaLogin(WebSocketSession session, JsonNode jsonNode, String originalPayload)
            throws IOException {
        // 调用专门的CA登录处理器
        caLoginProcessor.process(session, jsonNode, originalPayload,
                DEVICE_SESSIONS, SESSIONS, SESSION_LAST_ACTIVE,
                this::sendMessage);
    }

    /**
     * 处理ca_report_location消息
     * 3.上报实时数据
     *
     * @author Shawn
     * @date 2025-05-31
     */
    private void processCaReportLocation(WebSocketSession session, JsonNode jsonNode, String originalPayload)
            throws IOException {
        // 调用专门的CA位置上报处理器
        caReportLocationProcessor.process(session, jsonNode, originalPayload, this::sendMessage);
    }

    /**
     * 处理ca_sos消息
     *
     * @author Shawn
     * @date 2024-07-31
     */
    private void processCaSos(WebSocketSession session, JsonNode jsonNode, String originalPayload)
            throws IOException {
        // 调用专门的CaSos处理器
        caSosProcessor.process(session, jsonNode, originalPayload, this::sendMessage);
    }

    /**
     * 处理ca_sending_message消息
     * 高端型号设备发送录音广播到后台
     * 设备通过长连接通知后台发送广播
     *
     * @author Shawn
     * @date 2024-12-19 根据message字段查询文件URL并替换响应中的URL
     */
    private void processCaSendingMessage(WebSocketSession session, JsonNode jsonNode, String originalPayload)
            throws IOException {
        // 调用专门的CaSendingMessage处理器
        caSendingMessageProcessor.process(session, jsonNode, originalPayload, this::sendMessage);
    }

    /**
     * 处理toggle_localrecord消息
     * 设备上报当前本地录制状态
     *
     * @author Shawn
     */
    private void processToggleLocalRecord(WebSocketSession session, JsonNode jsonNode, String originalPayload)
            throws IOException {
        // 调用专门的ToggleLocalRecord处理器
        toggleLocalRecordProcessor.process(session, jsonNode, originalPayload, this::sendMessage);
    }

    /**
     * 处理ca_upload_photo消息
     * 根据图片URL更新数据库中的经纬度和设备ID
     *
     * @author Shawn
     * @date 2025-05-27
     */
    private void processCaUploadPhoto(WebSocketSession session, JsonNode jsonNode, String originalPayload)
            throws IOException {
        // 调用专门的CaUploadPhoto处理器
        caUploadPhotoProcessor.process(session, jsonNode, originalPayload, this::sendMessage);
    }

    /**
     * 处理push_to_client消息
     * 8.2 设备端收到指令后上报本地视频记录
     * 8.4.2 设备发送开始上传报文
     *
     * @author Shawn
     * @date 2025-05-28
     */
    private void processPushToClient(WebSocketSession session, JsonNode jsonNode, String originalPayload)
            throws IOException {
        // 调用专门的PushToClient处理器
        pushToClientProcessor.process(session, jsonNode, originalPayload, this::sendMessage);
    }

    /**
     * 广播消息给所有连接的客户端
     */
    public void broadcastMessage(String message) {
        SESSIONS.values().forEach(session -> {
            sendMessage(session, message);
        });
    }

    /**
     * 发送消息给指定sessionId的客户端
     */
    public void sendMessageToSession(String sessionId, String message) {
        WebSocketSession session = SESSIONS.get(sessionId);
        if (session != null) {
            sendMessage(session, message);
        } else {
            logger.warn("未找到sessionId为{}的WebSocket连接", sessionId);
        }
    }

    /**
     * 根据device_id获取对应的WebSocketSession
     *
     * @author Shawn
     * @date 2024-07-05
     */
    public WebSocketSession getSessionByDeviceId(String deviceId) {
        // 直接从映射Map中获取，时间复杂度O(1)
        return DEVICE_SESSIONS.get(deviceId);
    }

    /**
     * 发送消息给指定device_id的客户端
     *
     * @author Shawn
     * @date 2024-07-05
     */
    public void sendMessageToDevice(String deviceId, String message) {
        WebSocketSession session = getSessionByDeviceId(deviceId);
        if (session != null) {
            sendMessage(session, message);
            logger.info("已发送消息给设备[{}]", deviceId);
        } else {
            logger.warn("未找到device_id为{}的WebSocket连接", deviceId);
        }
    }

    /**
     * 发送消息给指定的WebSocketSession
     */
    private void sendMessage(WebSocketSession session, String message) {
        if (session != null && session.isOpen()) {
            try {
                session.sendMessage(new TextMessage(message));
            } catch (IOException e) {
                logger.error("发送WebSocket消息失败", e);
            }
        }
    }

    /**
     * 连接关闭时触发
     */
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
        String sessionId = session.getId();
        // 获取关闭连接的deviceId
        Object deviceId = session.getAttributes().get("device_id");

        // 从SESSIONS中移除
        SESSIONS.remove(sessionId);
        // 从活跃时间记录中移除
        SESSION_LAST_ACTIVE.remove(sessionId);

        // 如果有deviceId，从DEVICE_SESSIONS中移除
        if (deviceId != null) {
            DEVICE_SESSIONS.remove(deviceId.toString());
        }

        // 记录详细的关闭信息
        String deviceIdStr = deviceId != null ? deviceId.toString() : "未知";
        logger.info("WebSocket连接关闭, sessionId: {}, deviceId: {}, 关闭状态: {}, 关闭原因: {}, 当前连接数: {}",
                sessionId, deviceIdStr, closeStatus.getCode(), closeStatus.getReason(), SESSIONS.size());
    }

    /**
     * 传输错误时触发
     */
    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        String sessionId = session.getId();
        // 获取出错连接的deviceId
        Object deviceId = session.getAttributes().get("device_id");
        String deviceIdStr = deviceId != null ? deviceId.toString() : "未知";

        logger.error("WebSocket传输错误, sessionId: {}, deviceId: {}, 错误信息: {}",
                sessionId, deviceIdStr, exception.getMessage(), exception);
    }

    /**
     * 是否支持部分消息
     * 返回true以支持大消息传输
     *
     * @author Shawn
     * @date 2025-06-10
     */
    @Override
    public boolean supportsPartialMessages() {
        return true;
    }

    /**
     * 获取总连接数
     *
     * @author Shawn
     * @date 2024-12-19
     */
    public int getTotalConnections() {
        // 清理无效连接
        cleanupInvalidSessions();
        return SESSIONS.size();
    }

    /**
     * 获取设备连接数
     *
     * @author Shawn
     * @date 2024-12-19
     */
    public int getDeviceConnections() {
        // 清理无效连接
        cleanupInvalidSessions();
        return DEVICE_SESSIONS.size();
    }

    /**
     * 获取活跃连接数
     *
     * @author Shawn
     * @date 2024-12-19
     */
    public int getActiveConnections() {
        // 清理无效连接
        cleanupInvalidSessions();

        // 永久连接模式：所有打开的连接都视为活跃连接
        return SESSIONS.size();
    }

    /**
     * 清理无效的WebSocket连接
     * 被动清理，不主动发送心跳
     *
     * @author Shawn
     * @date 2024-12-19
     */
    private void cleanupInvalidSessions() {
        try {
            // 清理已关闭的连接
            SESSIONS.entrySet().removeIf(entry -> {
                String sessionId = entry.getKey();
                WebSocketSession session = entry.getValue();

                if (!session.isOpen()) {
                    logger.info("清理已关闭的WebSocket连接: {}", sessionId);

                    // 从设备映射中移除
                    Object deviceId = session.getAttributes().get("device_id");
                    if (deviceId != null) {
                        DEVICE_SESSIONS.remove(deviceId.toString());
                    }
                    SESSION_LAST_ACTIVE.remove(sessionId);
                    return true;
                }
                return false;
            });

            // 清理过期的session - Shawn 2025-01-04
            cleanupExpiredSessions();

        } catch (Exception e) {
            logger.error("清理无效连接时发生错误", e);
        }
    }

    /**
     * 清理过期的WebSocket会话
     * 永久连接模式：不再基于时间清理会话，只清理已关闭的连接
     *
     * @author Shawn
     * @date 2025-01-04
     */
    private void cleanupExpiredSessions() {
        try {
            // 永久连接模式：不进行基于时间的过期清理
            // 只在连接实际关闭时进行清理（在cleanupInvalidSessions中处理）
            logger.debug("永久连接模式：跳过基于时间的会话过期清理");

        } catch (Exception e) {
            logger.error("清理过期session时发生错误", e);
        }
    }

    /**
     * 强制清理指定设备的连接
     * 提供外部调用接口，用于手动清理特定设备的连接
     *
     * @param deviceId 设备ID
     * @return 是否成功清理
     * @author Shawn
     * @date 2025-01-04
     */
    public boolean forceCleanupDeviceConnection(String deviceId) {
        if (deviceId == null || deviceId.trim().isEmpty()) {
            return false;
        }

        try {
            WebSocketSession session = DEVICE_SESSIONS.get(deviceId);
            if (session != null) {
                String sessionId = session.getId();
                logger.info("强制清理设备[{}]的连接, sessionId: {}", deviceId, sessionId);

                // 从映射中移除
                DEVICE_SESSIONS.remove(deviceId);
                SESSIONS.remove(sessionId);
                SESSION_LAST_ACTIVE.remove(sessionId);

                // 关闭连接
                if (session.isOpen()) {
                    session.close();
                }

                return true;
            }
            return false;
        } catch (Exception e) {
            logger.error("强制清理设备[{}]连接时发生错误: {}", deviceId, e.getMessage(), e);
            return false;
        }
    }

    /**
     * 获取连接详情
     *
     * @author Shawn
     * @date 2024-12-19
     */
    public Map<String, Object> getConnectionDetails() {
        // 先清理无效连接
        cleanupInvalidSessions();

        Map<String, Object> details = new HashMap<>();
        long currentTime = System.currentTimeMillis();

        List<Map<String, Object>> connections = new ArrayList<>();

        for (Map.Entry<String, WebSocketSession> entry : SESSIONS.entrySet()) {
            String sessionId = entry.getKey();
            WebSocketSession session = entry.getValue();

            Map<String, Object> connectionInfo = new HashMap<>();
            connectionInfo.put("sessionId", sessionId);
            connectionInfo.put("isOpen", session.isOpen());
            connectionInfo.put("remoteAddress",
                    session.getRemoteAddress() != null ? session.getRemoteAddress().toString() : "unknown");

            Object deviceId = session.getAttributes().get("device_id");
            connectionInfo.put("deviceId", deviceId != null ? deviceId.toString() : "未设置");

            Long lastActive = SESSION_LAST_ACTIVE.get(sessionId);
            if (lastActive != null) {
                connectionInfo.put("lastActiveTime", new Date(lastActive));
                connectionInfo.put("idleTimeMinutes", (currentTime - lastActive) / 60000);
            } else {
                connectionInfo.put("lastActiveTime", "未知");
                connectionInfo.put("idleTimeMinutes", 0);
            }

            connections.add(connectionInfo);
        }

        details.put("connections", connections);
        details.put("timestamp", new Date(currentTime));
        details.put("cleanupInfo", "永久连接模式 - 只清理已关闭连接，不进行基于时间的过期清理");

        return details;
    }

    /**
     * WebSocket消息处理器接口
     * 4.报警推送
     */
    @FunctionalInterface
    private interface WebSocketMessageProcessor {
        void process(WebSocketSession session, JsonNode jsonNode, String originalPayload) throws IOException;
    }

    /**
     * 处理ca_sip_sos消息
     * SIP安全帽报警消息处理
     *
     * @author Shawn
     * @date 2025-06-02
     */
    private void processCaSipSos(WebSocketSession session, JsonNode jsonNode, String originalPayload)
            throws IOException {
        // 调用专门的CaSipSos处理器
        caSipSosProcessor.process(session, jsonNode, originalPayload, this::sendMessage);
    }
}
