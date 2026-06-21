package cn.iocoder.yudao.module.iot.websocket.processor;

import com.fasterxml.jackson.databind.JsonNode;
import cn.iocoder.yudao.module.iot.service.HelmetSipSosTdEngineService;
import cn.iocoder.yudao.module.iot.util.JsonResponseBuilder;
import cn.iocoder.yudao.module.iot.util.R;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * CaSipSos消息处理器
 * 处理SIP SOS 消息，保存数据到TDengine并回复响应
 *
 * @author Shawn
 * @date 2025-01-31
 */
@Component
public class CaSipSosProcessor {

    private static final Logger logger = LoggerFactory.getLogger(CaSipSosProcessor.class);

    @Resource
    private HelmetSipSosTdEngineService helmetSipSosTdEngineService;

    /**
     * 处理ca_sip_sos消息
     * SIP安全帽报警消息处理
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
        String deviceId = jsonNode.has("device_id") ? jsonNode.get("device_id").asText() : "unknown";
        String xPoint = jsonNode.has("x_point") ? jsonNode.get("x_point").asText() : "";
        String yPoint = jsonNode.has("y_point") ? jsonNode.get("y_point").asText() : "";
        String type = jsonNode.has("type") ? jsonNode.get("type").asText() : "";
        String stepCount = jsonNode.has("stepCount") ? jsonNode.get("stepCount").asText() : "";
        String stepCountTime = jsonNode.has("stepCountTime") ? jsonNode.get("stepCountTime").asText() : "";

        // 保存SIP SOS信息到TDengine超级表
        try {
            saveHelmetSipSosDataToTDengine(deviceId, jsonNode);
        } catch (Exception e) {
            logger.error("保存安全帽设备SIP SOS数据到TDengine失败, deviceId: {}, 错误: {}",
                    deviceId, e.getMessage(), e);
        }

        // 使用工具类构建SIP SOS响应JSON
        String responseJson = JsonResponseBuilder.buildCaSipSosResponse();
        sendMessageCallback.sendMessage(session, responseJson);

        logger.info("已发送ca_sip_sos响应, sessionId: {}, deviceId: {}, 位置: [{}, {}], 类型: {}, 计步: {}, 计步时间: {}",
                sessionId, deviceId, xPoint, yPoint, type, stepCount, stepCountTime);
    }

    /**
     * 保存安全帽设备SIP SOS数据到TDengine时序数据库（使用超级表）
     *
     * @param deviceId 设备ID
     * @param jsonNode SIP SOS数据JSON节点
     * @author Shawn
     * @date 2025-06-02
     */
    private void saveHelmetSipSosDataToTDengine(String deviceId, JsonNode jsonNode) {
        try {
            // 将JSON数据转换为Map
            Map<String, Object> sipSosDataMap = new HashMap<>();

            // 遍历JSON节点，提取所有字段（除了act字段）
            Iterator<Map.Entry<String, JsonNode>> fields = jsonNode.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> field = fields.next();
                String key = field.getKey();
                JsonNode value = field.getValue();

                // 根据JSON节点类型转换值
                Object convertedValue = convertJsonNodeValue(value);
                if (convertedValue != null) {
                    sipSosDataMap.put(key, convertedValue);
                }
            }

            // 使用HelmetSipSosTdEngineService保存SIP SOS数据
            R<cn.hutool.json.JSONObject> result = helmetSipSosTdEngineService.saveHelmetSipSosData(deviceId,
                    sipSosDataMap);

            if (result.getCode() != R.SUCCESS) {
                logger.error("安全帽设备SIP SOS数据保存失败, deviceId: {}, 错误信息: {}", deviceId, result.getMsg());
            } else {
                logger.info("安全帽设备SIP SOS数据保存成功, deviceId: {}", deviceId);
            }

        } catch (Exception e) {
            logger.error("保存安全帽设备SIP SOS数据异常, deviceId: {}", deviceId, e);
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
