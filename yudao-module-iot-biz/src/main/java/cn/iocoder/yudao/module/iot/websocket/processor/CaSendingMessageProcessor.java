package cn.iocoder.yudao.module.iot.websocket.processor;

import com.fasterxml.jackson.databind.JsonNode;
import cn.iocoder.yudao.module.iot.dal.dataobject.IotFileUploadDO;
import cn.iocoder.yudao.module.iot.service.HelmetSendingMessageTdEngineService;
import cn.iocoder.yudao.module.iot.service.IotFileUploadService;
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
 * CaSendingMessage消息处理器
 * 处理录音广播消息，查询文件URL，保存数据到TDengine并回复响应
 *
 * @author Shawn
 * @date 2025-01-31
 */
@Component
public class CaSendingMessageProcessor {

    private static final Logger logger = LoggerFactory.getLogger(CaSendingMessageProcessor.class);

    @Resource
    private IotFileUploadService iotFileUploadService;

    @Resource
    private HelmetSendingMessageTdEngineService helmetSendingMessageTdEngineService;

    /**
     * 处理ca_sending_message消息
     * 高端型号设备发送录音广播到后台
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
                : (jsonNode.has("device_id") ? jsonNode.get("device_id").asText() : "unknown");
        String message = jsonNode.has("message") ? jsonNode.get("message").asText() : "";

        // 保存录音广播消息数据到TDengine超级表
        try {
            saveHelmetSendingMessageDataToTDengine(deviceId, jsonNode);
        } catch (Exception e) {
            logger.error("保存安全帽设备录音广播消息数据到TDengine失败, deviceId: {}, 错误: {}",
                    deviceId, e.getMessage(), e);
        }

        // 根据message字段查询iot_file_upload表获取URL
        String responseJson = buildResponseWithFileUrl(sessionId, message);
        sendMessageCallback.sendMessage(session, responseJson);

        logger.info("已发送server_push_ma_broadcast响应, sessionId: {}, message: {}",
                sessionId, message);
    }

    /**
     * 根据message字段构建响应，查询文件URL
     *
     * @param sessionId 会话ID
     * @param message   消息内容
     * @return 响应JSON字符串
     * @author Shawn
     * @date 2024-12-19
     */
    private String buildResponseWithFileUrl(String sessionId, String message) {
        try {
            if (message != null && !message.trim().isEmpty()) {
                // 通过message值作为objectName查询文件记录
                IotFileUploadDO fileRecord = iotFileUploadService.findByObjectName(message.trim());
                if (fileRecord != null && fileRecord.getFileUrl() != null && !fileRecord.getFileUrl().trim().isEmpty()) {
                    // 找到文件记录，使用其URL构建响应
                    logger.info("根据message[{}]查询到文件URL: {}, sessionId: {}",
                            message, fileRecord.getFileUrl(), sessionId);
                    return JsonResponseBuilder.buildServerPushMaBroadcastResponse(fileRecord.getFileUrl());
                } else {
                    // 未找到文件记录，使用默认响应
                    logger.warn("未根据message[{}]查询到文件记录，使用默认URL, sessionId: {}",
                            message, sessionId);
                    return JsonResponseBuilder.buildServerPushMaBroadcastResponse();
                }
            } else {
                // message为空，使用默认响应
                logger.warn("message字段为空，使用默认URL, sessionId: {}", sessionId);
                return JsonResponseBuilder.buildServerPushMaBroadcastResponse();
            }
        } catch (Exception e) {
            // 查询出错，使用默认响应
            logger.error("查询文件记录时发生错误，使用默认URL, sessionId: {}, 错误: {}",
                    sessionId, e.getMessage(), e);
            return JsonResponseBuilder.buildServerPushMaBroadcastResponse();
        }
    }

    /**
     * 保存安全帽设备录音广播消息数据到TDengine时序数据库（使用超级表）
     *
     * @param deviceId 设备ID
     * @param jsonNode 消息数据JSON节点
     * @author Shawn
     * @date 2025-01-31
     */
    private void saveHelmetSendingMessageDataToTDengine(String deviceId, JsonNode jsonNode) {
        try {
            // 将JSON数据转换为Map
            Map<String, Object> messageDataMap = new HashMap<>();

            // 遍历JSON节点，提取所有字段（除了act字段）
            Iterator<Map.Entry<String, JsonNode>> fields = jsonNode.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> field = fields.next();
                String key = field.getKey();
                JsonNode value = field.getValue();

                // 根据JSON节点类型转换值
                Object convertedValue = convertJsonNodeValue(value);
                if (convertedValue != null) {
                    messageDataMap.put(key, convertedValue);
                }
            }

            // 使用HelmetSendingMessageTdEngineService保存录音广播消息数据
            R<cn.hutool.json.JSONObject> result = helmetSendingMessageTdEngineService
                    .saveHelmetSendingMessageData(deviceId, messageDataMap);

            if (result.getCode() != R.SUCCESS) {
                logger.error("安全帽设备录音广播消息数据保存失败, deviceId: {}, 错误信息: {}", deviceId, result.getMsg());
            } else {
                logger.info("安全帽设备录音广播消息数据保存成功, deviceId: {}", deviceId);
            }

        } catch (Exception e) {
            logger.error("保存安全帽设备录音广播消息数据异常, deviceId: {}", deviceId, e);
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
