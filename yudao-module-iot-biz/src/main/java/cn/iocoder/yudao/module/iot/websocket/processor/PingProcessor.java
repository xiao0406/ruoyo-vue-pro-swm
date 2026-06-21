package cn.iocoder.yudao.module.iot.websocket.processor;

import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.Map;

/**
 * Ping消息处理器
 * 处理心跳消息，更新会话活跃时间并回复pong
 *
 * @author Shawn
 * @date 2025-01-31
 */
@Component
public class PingProcessor {

    private static final Logger logger = LoggerFactory.getLogger(PingProcessor.class);

    /**
     * 处理ping消息
     *
     * @param session             WebSocket会话
     * @param jsonNode            JSON消息节点
     * @param originalPayload     原始消息负载
     * @param sessionLastActive   会话最后活跃时间映射
     * @param sendMessageCallback 发送消息回调接口
     * @throws IOException IO异常
     */
    public void process(WebSocketSession session, JsonNode jsonNode, String originalPayload,
            Map<String, Long> sessionLastActive, SendMessageCallback sendMessageCallback) throws IOException {

        String sessionId = session.getId();

        // 更新最后活跃时间
        sessionLastActive.put(sessionId, System.currentTimeMillis());

        // 回复pong消息
        String pongResponse = "{\"cmd\":\"pong\",\"timestamp\":" + System.currentTimeMillis() + "}";
        sendMessageCallback.sendMessage(session, pongResponse);

        logger.debug("已回复心跳pong消息, sessionId: {}", sessionId);
    }

    /**
     * 发送消息回调接口
     */
    @FunctionalInterface
    public interface SendMessageCallback {
        void sendMessage(WebSocketSession session, String message) throws IOException;
    }
}
