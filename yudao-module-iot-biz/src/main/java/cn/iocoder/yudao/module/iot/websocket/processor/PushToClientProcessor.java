package cn.iocoder.yudao.module.iot.websocket.processor;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import cn.iocoder.yudao.module.iot.service.HelmetPushToClientTdEngineService;
import cn.iocoder.yudao.module.iot.util.R;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * PushToClient消息处理器
 * 处理推送到客户端消息，保存数据到TDengine
 * 
 * @author Shawn
 * @date 2025-01-31
 */
@Component
public class PushToClientProcessor {

    private static final Logger logger = LoggerFactory.getLogger(PushToClientProcessor.class);

    @Autowired
    private HelmetPushToClientTdEngineService helmetPushToClientTdEngineService;

    private final ObjectMapper mapper = new ObjectMapper();

    /**
     * 处理push_to_client消息
     * 
     * @param session             WebSocket会话
     * @param jsonNode            JSON消息节点
     * @param originalPayload     原始消息负载
     * @param sendMessageCallback 发送消息回调接口
     * @throws IOException IO异常
     */
    public void process(WebSocketSession session, JsonNode jsonNode, String originalPayload,
            PingProcessor.SendMessageCallback sendMessageCallback) throws IOException {

        String sessionId = session.getId();
        String deviceId = session.getAttributes().get("device_id") != null
                ? session.getAttributes().get("device_id").toString()
                : "unknown";
        String talkbackId = jsonNode.has("talkback_id") ? jsonNode.get("talkback_id").asText() : "";

        // 保存推送到客户端数据到TDengine超级表
        try {
            saveHelmetPushToClientDataToTDengine(deviceId, jsonNode);
        } catch (Exception e) {
            logger.error("保存安全帽设备推送到客户端数据到TDengine失败, deviceId: {}, 错误: {}",
                    deviceId, e.getMessage(), e);
        }

        // 检查是否为"开始上传"消息
        boolean isStartUpload = isStartUploadMessage(jsonNode);

        // 处理"开始上传"消息 - 8.4.2 设备发送开始上传报文
        if (isStartUpload) {
            handleStartUploadMessage(session, sessionId, deviceId, talkbackId, sendMessageCallback);
            return;
        }

        // 处理普通推送消息
        handleNormalPushMessage(sessionId, deviceId, talkbackId, jsonNode);
    }

    /**
     * 检查是否为开始上传消息
     * 
     * @param jsonNode JSON节点
     * @return 是否为开始上传消息
     */
    private boolean isStartUploadMessage(JsonNode jsonNode) {
        return jsonNode.has("res") && jsonNode.get("res").has("msg") &&
                "开始上传".equals(jsonNode.get("res").get("msg").asText());
    }

    /**
     * 处理开始上传消息
     * 
     * @param session             WebSocket会话
     * @param sessionId           会话ID
     * @param deviceId            设备ID
     * @param talkbackId          对讲ID
     * @param sendMessageCallback 发送消息回调接口
     * @throws IOException IO异常
     */
    private void handleStartUploadMessage(WebSocketSession session, String sessionId, String deviceId,
            String talkbackId, PingProcessor.SendMessageCallback sendMessageCallback) throws IOException {

        logger.info("收到\"act\": \"push_to_client\" - 开始上传消息, sessionId: {}, deviceId: {}, talkbackId: {}",
                sessionId, deviceId, talkbackId);

        // 构建响应
        Map<String, Object> response = new HashMap<>();
        response.put("status", true);
        response.put("msg", "推送成功");
        response.put("msg_code", "PushSucc");

        // 发送响应
        sendMessageCallback.sendMessage(session, mapper.writeValueAsString(response));
    }

    /**
     * 处理普通推送消息
     * 
     * @param sessionId  会话ID
     * @param deviceId   设备ID
     * @param talkbackId 对讲ID
     * @param jsonNode   JSON节点
     */
    private void handleNormalPushMessage(String sessionId, String deviceId, String talkbackId, JsonNode jsonNode) {
        // 检查是否有结果数据
        boolean hasData = false;
        String fileInfo = "";

        if (jsonNode.has("res") && jsonNode.get("res").has("status")) {
            boolean status = jsonNode.get("res").get("status").asBoolean();
            String msg = jsonNode.get("res").has("msg") ? jsonNode.get("res").get("msg").asText() : "";

            if (status && jsonNode.get("res").has("data") && jsonNode.get("res").get("data").isArray()
                    && jsonNode.get("res").get("data").size() > 0) {
                hasData = true;
                JsonNode dataNode = jsonNode.get("res").get("data").get(0);
                String fileMd5 = dataNode.has("file_md5") ? dataNode.get("file_md5").asText() : "";
                String fileName = dataNode.has("file_name") ? dataNode.get("file_name").asText() : "";
                fileInfo = String.format("file_md5: %s, file_name: %s", fileMd5, fileName);
            }

            // 记录日志
            if (hasData) {
                logger.info(
                        "收到\"act\": \"push_to_client\"消息, sessionId: {}, deviceId: {}, talkbackId: {}, 状态: {}, 消息: {}, 文件信息: {}",
                        sessionId, deviceId, talkbackId, status, msg, fileInfo);
            } else {
                logger.info(
                        "收到\"act\": \"push_to_client\"消息, sessionId: {}, deviceId: {}, talkbackId: {}, 状态: {}, 消息: {}",
                        sessionId, deviceId, talkbackId, status, msg);
            }
        } else {
            logger.info("收到\"act\": \"push_to_client\"消息, sessionId: {}, deviceId: {}, talkbackId: {}, 但格式不完整",
                    sessionId, deviceId, talkbackId);
        }

        // 无需返回结果给前端（原视频记录上报情况）
    }

    /**
     * 保存安全帽设备推送到客户端数据到TDengine时序数据库（使用超级表）
     * 
     * @param deviceId 设备ID
     * @param jsonNode 推送数据JSON节点
     * @author Shawn
     * @date 2025-01-31
     */
    private void saveHelmetPushToClientDataToTDengine(String deviceId, JsonNode jsonNode) {
        try {
            // 将JSON数据转换为Map
            Map<String, Object> pushDataMap = new HashMap<>();

            // 遍历JSON节点，提取所有字段（除了act字段）
            Iterator<Map.Entry<String, JsonNode>> fields = jsonNode.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> field = fields.next();
                String key = field.getKey();
                JsonNode value = field.getValue();

                // 根据JSON节点类型转换值
                Object convertedValue = convertJsonNodeValue(value);
                if (convertedValue != null) {
                    pushDataMap.put(key, convertedValue);
                }
            }

            // 使用HelmetPushToClientTdEngineService保存推送到客户端数据
            R<cn.hutool.json.JSONObject> result = helmetPushToClientTdEngineService.saveHelmetPushToClientData(deviceId,
                    pushDataMap);

            if (result.getCode() != R.SUCCESS) {
                logger.error("安全帽设备推送到客户端数据保存失败, deviceId: {}, 错误信息: {}", deviceId, result.getMsg());
            } else {
                logger.info("安全帽设备推送到客户端数据保存成功, deviceId: {}", deviceId);
            }

        } catch (Exception e) {
            logger.error("保存安全帽设备推送到客户端数据异常, deviceId: {}", deviceId, e);
        }
    }

    /**
     * 转换JSON节点值为合适的Java对象
     * 
     * @param jsonNode JSON节点
     * @return 转换后的值
     * @author Shawn
     * @date 2025-01-31
     */
    private Object convertJsonNodeValue(JsonNode jsonNode) {
        if (jsonNode.isNull()) {
            return null;
        } else if (jsonNode.isBoolean()) {
            return jsonNode.asBoolean();
        } else if (jsonNode.isInt()) {
            return jsonNode.asInt();
        } else if (jsonNode.isLong()) {
            return jsonNode.asLong();
        } else if (jsonNode.isDouble() || jsonNode.isFloat()) {
            return jsonNode.asDouble();
        } else if (jsonNode.isTextual()) {
            return jsonNode.asText();
        } else {
            // 对于复杂对象，转换为字符串
            return jsonNode.toString();
        }
    }
}