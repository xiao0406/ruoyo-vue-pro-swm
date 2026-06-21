package cn.iocoder.yudao.module.iot.websocket.processor;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import cn.iocoder.yudao.module.iot.dal.dataobject.IotFileUploadDO;
import cn.iocoder.yudao.module.iot.service.HelmetUploadPhotoTdEngineService;
import cn.iocoder.yudao.module.iot.service.IotFileUploadService;
import cn.iocoder.yudao.module.iot.util.R;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;
import java.util.Date;

/**
 * CaUploadPhoto消息处理器
 * 处理照片上传消息，更新数据库中的经纬度和设备ID
 * 
 * @author Shawn
 * @date 2025-01-31
 */
@Component
public class CaUploadPhotoProcessor {

    private static final Logger logger = LoggerFactory.getLogger(CaUploadPhotoProcessor.class);

    @Autowired
    private IotFileUploadService iotFileUploadService;

    @Autowired
    private HelmetUploadPhotoTdEngineService helmetUploadPhotoTdEngineService;

    private final ObjectMapper mapper = new ObjectMapper();

    /**
     * 处理ca_upload_photo消息
     * 根据图片URL更新数据库中的经纬度和设备ID
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
        String deviceId = jsonNode.has("device_id") ? jsonNode.get("device_id").asText() : "";
        String imageUrl = jsonNode.has("image_url") ? jsonNode.get("image_url").asText() : "";
        String xPointStr = jsonNode.has("x_point") ? jsonNode.get("x_point").asText() : "";
        String yPointStr = jsonNode.has("y_point") ? jsonNode.get("y_point").asText() : "";

        logger.info("处理照片上传，sessionId: {}, deviceId: {}, imageUrl: {}, 位置: [{}, {}]",
                sessionId, deviceId, imageUrl, xPointStr, yPointStr);

        // 保存照片上传数据到TDengine超级表
        try {
            saveHelmetUploadPhotoDataToTDengine(deviceId, jsonNode);
        } catch (Exception e) {
            logger.error("保存安全帽设备照片上传数据到TDengine失败, deviceId: {}, 错误: {}",
                    deviceId, e.getMessage(), e);
        }

        Map<String, Object> result = processPhotoUpload(sessionId, deviceId, imageUrl, xPointStr, yPointStr);
        sendMessageCallback.sendMessage(session, mapper.writeValueAsString(result));
    }

    /**
     * 处理照片上传逻辑
     * 
     * @param sessionId 会话ID
     * @param deviceId  设备ID
     * @param imageUrl  图片URL
     * @param xPointStr X坐标字符串
     * @param yPointStr Y坐标字符串
     * @return 处理结果
     */
    private Map<String, Object> processPhotoUpload(String sessionId, String deviceId, String imageUrl,
            String xPointStr, String yPointStr) {

        Map<String, Object> result = new HashMap<>();

        try {
            if (imageUrl == null || imageUrl.trim().isEmpty()) {
                result.put("status", false);
                result.put("msg", "图片URL不能为空");
                result.put("data", new ArrayList<>());
                result.put("msg_code", "AddFail");
                return result;
            }

            // 使用安全截断方法处理经纬度
            BigDecimal xPoint = safeParseAndTruncate(xPointStr, 3, 8, new BigDecimal("999.99999999"));
            BigDecimal yPoint = safeParseAndTruncate(yPointStr, 3, 8, new BigDecimal("999.99999999"));
            if ((xPointStr != null && !xPointStr.trim().isEmpty() && xPoint == null) ||
                    (yPointStr != null && !yPointStr.trim().isEmpty() && yPoint == null)) {
                logger.warn("经纬度格式错误，xPoint: {}, yPoint: {}", xPointStr, yPointStr);
                result.put("status", false);
                result.put("msg", "经纬度格式错误");
                result.put("data", new ArrayList<>());
                result.put("msg_code", "AddFail");
                return result;
            }

            // 从URL中提取文件名
            String fileName = extractFileName(imageUrl);

            // 查询数据库中是否存在该图片名称的记录
            IotFileUploadDO queryEntity = new IotFileUploadDO();
            queryEntity.setFileName(fileName);
            List<IotFileUploadDO> existingRecords = iotFileUploadService.findList(queryEntity);

            if (existingRecords.isEmpty()) {
                logger.warn("未找到图片名称为 {} 的记录，原URL: {}", fileName, imageUrl);
                result.put("status", false);
                result.put("msg", "未找到对应的图片记录: " + fileName);
                result.put("data", new ArrayList<>());
                result.put("msg_code", "AddFail");
                return result;
            }

            // 更新第一条匹配的记录
            IotFileUploadDO fileUpload = existingRecords.get(0);
            fileUpload.setDeviceId(deviceId);
            fileUpload.setXPoint(xPoint);
            fileUpload.setYPoint(yPoint);
            fileUpload.setUpdateBy("system");
            fileUpload.setUpdateDate(new Date());
            fileUpload.setRemarks("WebSocket照片上传更新 - " + new Date());

            try {
                // 构建响应
                result.put("status", true);
                result.put("msg", "添加成功！");
                result.put("data", new ArrayList<>());
                result.put("msg_code", "AddSucc");

                // 将完整的响应内容保存到数据库记录中，方便后续跟踪运维
                String responseJson = new ObjectMapper().writeValueAsString(result);
                fileUpload.setResponse(responseJson);

                // 保存更新
                iotFileUploadService.save(fileUpload);
                logger.info("照片位置信息更新成功，记录ID: {}", fileUpload.getId());

                result.put("status", true);
                result.put("msg", "更新成功");
                result.put("data", Arrays.asList(fileUpload));
                result.put("msg_code", "UpdateSucc");
            } catch (Exception e) {
                logger.error("更新图片记录失败，fileName: {}, 错误: {}", fileName, e.getMessage(), e);
                result.put("status", false);
                result.put("msg", "数据库更新失败: " + e.getMessage());
                result.put("data", new ArrayList<>());
                result.put("msg_code", "UpdateFail");
            }

        } catch (Exception e) {
            logger.error("处理照片上传时发生未知错误, sessionId: {}, 错误: {}", sessionId, e.getMessage(), e);
            result.put("status", false);
            result.put("msg", "系统错误: " + e.getMessage());
            result.put("data", new ArrayList<>());
            result.put("msg_code", "SystemError");
        }

        return result;
    }

    /**
     * 从URL中提取文件名
     * 
     * @param imageUrl 图片URL
     * @return 文件名
     */
    private String extractFileName(String imageUrl) {
        String fileName = imageUrl;
        if (imageUrl.contains("/")) {
            // 如果是URL格式，提取最后一个/后面的文件名
            fileName = imageUrl.substring(imageUrl.lastIndexOf("/") + 1);
            logger.info("从URL中提取文件名，原URL: {}, 提取的文件名: {}", imageUrl, fileName);
        }
        return fileName;
    }

    /**
     * 经纬度安全解析与截断
     * 
     * @author Shawn
     * @date 2024-12-19
     */
    private BigDecimal safeParseAndTruncate(String value, int intDigits, int fracDigits, BigDecimal maxAbs) {
        if (value == null || value.trim().isEmpty())
            return null;
        try {
            BigDecimal bd = new BigDecimal(value.trim());
            // 截断小数位
            bd = bd.setScale(fracDigits, BigDecimal.ROUND_DOWN);
            // 限制绝对值
            if (bd.abs().compareTo(maxAbs) > 0) {
                bd = bd.signum() >= 0 ? maxAbs : maxAbs.negate();
            }
            return bd;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 保存安全帽设备照片上传数据到TDengine时序数据库（使用超级表）
     * 
     * @param deviceId 设备ID
     * @param jsonNode 照片上传数据JSON节点
     * @author Shawn
     * @date 2025-01-31
     */
    private void saveHelmetUploadPhotoDataToTDengine(String deviceId, JsonNode jsonNode) {
        try {
            // 将JSON数据转换为Map
            Map<String, Object> photoDataMap = new HashMap<>();

            // 遍历JSON节点，提取所有字段（除了act字段）
            Iterator<Map.Entry<String, JsonNode>> fields = jsonNode.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> field = fields.next();
                String key = field.getKey();
                JsonNode value = field.getValue();

                // 根据JSON节点类型转换值
                Object convertedValue = convertJsonNodeValue(value);
                if (convertedValue != null) {
                    photoDataMap.put(key, convertedValue);
                }
            }

            // 使用HelmetUploadPhotoTdEngineService保存照片上传数据
            R<cn.hutool.json.JSONObject> result = helmetUploadPhotoTdEngineService.saveHelmetUploadPhotoData(deviceId,
                    photoDataMap);

            if (result.getCode() != R.SUCCESS) {
                logger.error("安全帽设备照片上传数据保存失败, deviceId: {}, 错误信息: {}", deviceId, result.getMsg());
            } else {
                logger.info("安全帽设备照片上传数据保存成功, deviceId: {}", deviceId);
            }

        } catch (Exception e) {
            logger.error("保存安全帽设备照片上传数据异常, deviceId: {}", deviceId, e);
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