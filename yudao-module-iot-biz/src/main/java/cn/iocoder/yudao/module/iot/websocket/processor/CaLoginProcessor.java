package cn.iocoder.yudao.module.iot.websocket.processor;

import com.fasterxml.jackson.databind.JsonNode;
import cn.iocoder.yudao.module.iot.service.HelmetLoginTdEngineService;
import cn.iocoder.yudao.module.iot.service.IotDeviceService;
import cn.iocoder.yudao.module.iot.dao.IotDeviceDao;
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
import java.util.concurrent.ConcurrentHashMap;

/**
 * CA登录处理器
 *
 * @author Shawn
 * @date 2025-01-16
 */
@Component
public class CaLoginProcessor {

    private static final Logger logger = LoggerFactory.getLogger(CaLoginProcessor.class);

    @Resource
    private HelmetLoginTdEngineService helmetLoginTdEngineService;

    @Resource
    private IotDeviceService iotDeviceService;

    @Resource
    private IotDeviceDao iotDeviceDao;

    /**
     * 处理CA登录请求
     *
     * @param session             WebSocket会话
     * @param jsonNode            JSON消息节点
     * @param originalPayload     原始消息载荷
     * @param deviceSessions      设备会话映射
     * @param sessions            会话映射
     * @param sessionLastActive   会话最后活跃时间映射
     * @param sendMessageCallback 发送消息回调函数
     * @throws IOException IO异常
     * @author Shawn
     * @date 2025-01-16
     */
    public void process(WebSocketSession session,
            JsonNode jsonNode,
            String originalPayload,
            Map<String, WebSocketSession> deviceSessions,
            Map<String, WebSocketSession> sessions,
            Map<String, Long> sessionLastActive,
            SendMessageCallback sendMessageCallback) throws IOException {

        String sessionId = session.getId();
        // 获取device_id
        String deviceId = jsonNode.has("device_id") ? jsonNode.get("device_id").asText() : "unknown";

        // 处理设备重新登录的情况 - Shawn 2025-01-04
        handleDeviceRelogin(deviceId, session, deviceSessions, sessions, sessionLastActive);

        // 将device_id保存到session中
        session.getAttributes().put("device_id", deviceId);
        // 将deviceId和session的映射关系存入Map
        deviceSessions.put(deviceId, session);

        // 保存或更新设备信息到数据库
        saveOrUpdateDeviceInfo(deviceId, sessionId);

        // 保存登录数据到TDengine超级表
        try {
            saveHelmetLoginDataToTDengine(deviceId, sessionId, jsonNode);
        } catch (Exception e) {
            logger.error("保存安全帽设备登录数据到TDengine失败, deviceId: {}, sessionId: {}, 错误: {}",
                    deviceId, sessionId, e.getMessage(), e);
        }

        // 使用工具类构建响应JSON
        String responseJson = JsonResponseBuilder.buildCaLoginResponse(deviceId);
        sendMessageCallback.sendMessage(session, responseJson);
        logger.info("已发送ca_login响应, sessionId: {}, device_id: {}", sessionId, deviceId);
    }

    /**
     * 处理设备重新登录的情况
     * 如果设备已存在连接，先清理旧的连接映射
     *
     * @param deviceId          设备ID
     * @param newSession        新的WebSocket会话
     * @param deviceSessions    设备会话映射
     * @param sessions          会话映射
     * @param sessionLastActive 会话最后活跃时间映射
     * @author Shawn
     * @date 2025-01-04
     */
    private void handleDeviceRelogin(String deviceId,
            WebSocketSession newSession,
            Map<String, WebSocketSession> deviceSessions,
            Map<String, WebSocketSession> sessions,
            Map<String, Long> sessionLastActive) {
        if (deviceId == null || "unknown".equals(deviceId)) {
            return;
        }

        // 检查是否已存在该设备的连接
        WebSocketSession oldSession = deviceSessions.get(deviceId);
        if (oldSession != null && !oldSession.equals(newSession)) {
            logger.info("检测到设备[{}]重新登录，清理旧连接", deviceId);

            try {
                // 从SESSIONS中移除旧session（如果存在）
                String oldSessionId = oldSession.getId();
                sessions.remove(oldSessionId);
                sessionLastActive.remove(oldSessionId);

                // 如果旧连接仍然打开，主动关闭它
                if (oldSession.isOpen()) {
                    oldSession.close();
                    logger.info("已主动关闭设备[{}]的旧连接, oldSessionId: {}", deviceId, oldSessionId);
                }
            } catch (Exception e) {
                logger.error("清理设备[{}]旧连接时发生错误: {}", deviceId, e.getMessage(), e);
            }
        }
    }

    /**
     * 保存安全帽设备登录数据到TDengine时序数据库（使用超级表）
     *
     * @param deviceId  设备ID
     * @param sessionId 会话ID
     * @param jsonNode  登录数据JSON节点
     * @author Shawn
     * @date 2025-01-31
     */
    private void saveHelmetLoginDataToTDengine(String deviceId, String sessionId, JsonNode jsonNode) {
        try {
            // 将JSON数据转换为Map
            Map<String, Object> loginDataMap = new HashMap<>();

            // 遍历JSON节点，提取所有字段（除了act字段）
            Iterator<Map.Entry<String, JsonNode>> fields = jsonNode.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> field = fields.next();
                String key = field.getKey();
                JsonNode value = field.getValue();

                // 根据JSON节点类型转换值
                Object convertedValue = convertJsonNodeValue(value);
                if (convertedValue != null) {
                    loginDataMap.put(key, convertedValue);
                }
            }

            // 使用HelmetLoginTdEngineService保存登录数据
            R<cn.hutool.json.JSONObject> result = helmetLoginTdEngineService.saveHelmetLoginData(deviceId, sessionId,
                    loginDataMap);

            if (result.getCode() != R.SUCCESS) {
                logger.error("安全帽设备登录数据保存失败, deviceId: {}, sessionId: {}, 错误信息: {}",
                        deviceId, sessionId, result.getMsg());
            } else {
                logger.info("安全帽设备登录数据保存成功, deviceId: {}, sessionId: {}", deviceId, sessionId);
            }

        } catch (Exception e) {
            logger.error("保存安全帽设备登录数据异常, deviceId: {}, sessionId: {}", deviceId, sessionId, e);
        }
    }

    /**
     * 根据JSON节点类型转换值
     *
     * @param jsonNode JSON节点
     * @return 转换后的值
     * @author Shawn
     * @date 2025-01-31
     */
    private Object convertJsonNodeValue(JsonNode jsonNode) {
        if (jsonNode == null || jsonNode.isNull()) {
            return null;
        }

        if (jsonNode.isBoolean()) {
            return jsonNode.asBoolean();
        } else if (jsonNode.isInt()) {
            return jsonNode.asInt();
        } else if (jsonNode.isLong()) {
            return jsonNode.asLong();
        } else if (jsonNode.isDouble()) {
            return jsonNode.asDouble();
        } else if (jsonNode.isTextual()) {
            return jsonNode.asText();
        } else if (jsonNode.isArray() || jsonNode.isObject()) {
            return jsonNode.toString();
        }

        return jsonNode.asText();
    }

    /**
     * 保存或更新设备信息到数据库
     *
     * @param deviceId  设备ID
     * @param sessionId 会话ID
     * @author Shawn
     * @date 2025-01-16
     */
    private void saveOrUpdateDeviceInfo(String deviceId, String sessionId) {
        try {
            if (deviceId == null || deviceId.trim().isEmpty() || "unknown".equals(deviceId)) {
                logger.warn("设备ID为空或未知，跳过数据库操作");
                return;
            }

            // 查询是否已存在该设备
            cn.iocoder.yudao.module.iot.entity.IotDevice existingDevice = iotDeviceService.getByDeviceId(deviceId);

            if (existingDevice != null) {
                // 使用DAO直接更新，避免实体层的自动处理
                int updateCount = iotDeviceDao.updateSessionId(deviceId, sessionId);
                if (updateCount > 0) {
                    logger.info("更新设备session_id成功，deviceId: {}, sessionId: {}", deviceId, sessionId);
                } else {
                    logger.warn("更新设备session_id失败，可能设备不存在，deviceId: {}", deviceId);
                }
            } else {
                // 新增设备信息 - 使用DAO直接插入
                cn.iocoder.yudao.module.iot.entity.IotDevice newDevice = new cn.iocoder.yudao.module.iot.entity.IotDevice();
                newDevice.setId(cn.iocoder.yudao.framework.common.idgen.IdGen.nextId()); // 生成ID
                newDevice.setDeviceId(deviceId);
                newDevice.setSessionId(sessionId);
                newDevice.setStatus("0"); // 默认状态
                newDevice.setRemarks("WebSocket登录新增设备 - " + new java.util.Date());

                int insertCount = iotDeviceDao.insertDevice(newDevice);
                if (insertCount > 0) {
                    logger.info("新增设备成功，deviceId: {}, sessionId: {}", deviceId, sessionId);
                } else {
                    logger.warn("新增设备失败，deviceId: {}", deviceId);
                }
            }
        } catch (Exception e) {
            logger.error("保存或更新设备信息失败，deviceId: {}, sessionId: {}", deviceId, sessionId, e);
        }
    }

    /**
     * 发送消息回调接口
     */
    @FunctionalInterface
    public interface SendMessageCallback {
        void sendMessage(WebSocketSession session, String message) throws IOException;
    }
}
