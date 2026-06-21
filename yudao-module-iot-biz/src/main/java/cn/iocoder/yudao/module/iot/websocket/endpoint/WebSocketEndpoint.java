package cn.iocoder.yudao.module.iot.websocket.endpoint;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import cn.iocoder.yudao.module.iot.util.JsonResponseBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import jakarta.websocket.OnClose;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 基于注解的WebSocket端点
 * 支持用户标识，可用于一对一通信
 *
 * @author Shawn
 * @date 2023-11-01
 */
@Component
@ServerEndpoint("/iot/websocket/endpoint/{userId}")
public class WebSocketEndpoint {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketEndpoint.class);

    // 记录当前连接数
    private static final AtomicInteger ONLINE_COUNT = new AtomicInteger(0);

    // 存储用户会话，key为userId，value为session
    private static final Map<String, Session> USER_SESSIONS = new ConcurrentHashMap<>();

    /**
     * 连接建立时调用
     */
    @OnOpen
    public void onOpen(Session session, @PathParam("userId") String userId) {
        USER_SESSIONS.put(userId, session);
        int onlineCount = ONLINE_COUNT.incrementAndGet();
        logger.info("有新用户[{}]连接，当前在线人数为: {}", userId, onlineCount);

        // 发送连接成功消息
        sendMessage(session, "连接成功，您的用户ID是: " + userId);
    }

    /**
     * 连接关闭时调用
     */
    @OnClose
    public void onClose(@PathParam("userId") String userId) {
        USER_SESSIONS.remove(userId);
        int onlineCount = ONLINE_COUNT.decrementAndGet();
        logger.info("用户[{}]退出，当前在线人数为: {}", userId, onlineCount);
    }

    /**
     * 收到客户端消息时调用
     */
    @OnMessage
    public void onMessage(String message, Session session, @PathParam("userId") String userId) {
        logger.info("收到用户[{}]的消息: {}", userId, message);

        try {
            // 使用Jackson解析JSON消息
            ObjectMapper mapper = new ObjectMapper();
            JsonNode jsonNode = mapper.readTree(message);

            // 检查是否是ca_login消息
            if (jsonNode.has("act") && "ca_login".equals(jsonNode.get("act").asText())) {
                // 获取device_id
                String deviceId = jsonNode.has("device_id") ? jsonNode.get("device_id").asText() : "unknown";
                // 使用工具类构建响应JSON
                String responseJson = JsonResponseBuilder.buildCaLoginResponse(deviceId);
                sendMessage(session, responseJson);
                logger.info("已发送ca_login响应给用户[{}]", userId);
            } else {
                // 不是ca_login，原样返回
                sendMessage(session, message);
            }
        } catch (Exception e) {
            // 不是JSON，原样返回
            sendMessage(session, message);
        }
    }

    /**
     * 获取当前在线人数
     */
    public static int getOnlineCount() {
        return ONLINE_COUNT.get();
    }

    /**
     * 发送消息给指定Session
     *
     * @author Shawn
     * @date 2024-07-04
     */
    private void sendMessage(Session session, String message) {
        try {
            if (session != null && session.isOpen()) {
                session.getBasicRemote().sendText(message);
            }
        } catch (IOException e) {
            logger.error("发送WebSocket消息失败", e);
        }
    }
}
