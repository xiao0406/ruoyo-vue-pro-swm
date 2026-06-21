package cn.iocoder.yudao.module.iot.websocket.processor;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import cn.iocoder.yudao.module.swm.api.constant.TdengineSuperTableConstants;
import cn.iocoder.yudao.module.iot.dal.dataobject.IotCoordinateRequestDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.BeaconStationDO;
import cn.iocoder.yudao.module.iot.service.ExternalCoordinateService;
import cn.iocoder.yudao.module.iot.cache.BeaconMacAddressCache;
import cn.iocoder.yudao.module.iot.dal.dataobject.BeaconStationDO;
import cn.iocoder.yudao.module.iot.service.ExternalCoordinateService;
import org.apache.commons.lang3.StringUtils;
import cn.iocoder.yudao.module.iot.service.HelmetRundeCaReportLocationTdEnginService;
import cn.iocoder.yudao.module.iot.service.LocationEngineService;
import cn.iocoder.yudao.module.iot.util.JsonResponseBuilder;
import cn.iocoder.yudao.module.iot.util.R;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.*;

/**
 * CA位置上报处理器
 *
 * @author Shawn
 * @date 2025-01-16
 */
@Component
public class CaReportLocationProcessor {

    private static final Logger logger = LoggerFactory.getLogger(CaReportLocationProcessor.class);

    private final ObjectMapper mapper = new ObjectMapper();

    @Value("${JIAI.url:http://58.240.212.6:8094}")
    private String jiaiBaseUrl;

    @Resource
    private HelmetRundeCaReportLocationTdEnginService helmetRundeCaReportLocationTdEnginService;

    @Resource
    private LocationEngineService locationEngineService;

    @Resource
    private cn.iocoder.yudao.module.iot.cache.BeaconMacAddressCache beaconMacAddressCache;

    @Resource
    private ExternalCoordinateService externalCoordinateService;

    /**
     * 处理CA位置上报请求
     *
     * @param session             WebSocket会话
     * @param jsonNode            JSON消息节点
     * @param originalPayload     原始消息载荷
     * @param sendMessageCallback 发送消息回调函数
     * @throws IOException IO异常
     * @author Shawn
     * @date 2025-01-16
     */
    public void process(WebSocketSession session,
            JsonNode jsonNode,
            String originalPayload,
            SendMessageCallback sendMessageCallback) throws IOException {

        String sessionId = session.getId();

        // 获取设备ID，优先从session属性中获取，如果没有则从消息中获取
        String deviceId = session.getAttributes().get("device_id") != null
                ? session.getAttributes().get("device_id").toString()
                : (jsonNode.has("user_id") ? jsonNode.get("user_id").asText() : "unknown");

        // 处理蓝牙信标定位 - Shawn 2025-01-31
        Map<String, Object> enhancedDataMap = processBluetoothBeaconLocation(deviceId, jsonNode);

        try {
            // 保存安全帽设备数据到TDengine时序数据库，使用增强后的数据
            saveHelmetDataToTDengineWithEnhancedData(deviceId, enhancedDataMap);
        } catch (Exception e) {
            logger.error("保存安全帽设备数据到TDengine失败, deviceId: {}, 错误: {}", deviceId, e.getMessage(), e);
        }

        // 使用工具类构建位置上报响应JSON
        String responseJson = JsonResponseBuilder.buildCaReportLocationResponse();
        sendMessageCallback.sendMessage(session, responseJson);
        logger.info("已发送ca_report_location响应, sessionId: {}, deviceId: {}", sessionId, deviceId);
    }

    /**
     * 处理蓝牙信标定位
     * 从bt_signal_info中提取beacon数据，调用定位引擎接口获取坐标
     *
     * @param deviceId 设备ID
     * @param jsonNode 原始消息JSON节点
     * @return 增强后的数据Map（包含定位引擎返回的坐标）
     * @author Shawn
     * @date 2025-01-31
     */
    private Map<String, Object> processBluetoothBeaconLocation(String deviceId, JsonNode jsonNode) {
        // 先将原始JSON转换为Map
        Map<String, Object> dataMap = new HashMap<>();
        Iterator<Map.Entry<String, JsonNode>> fields = jsonNode.fields();
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> field = fields.next();
            String key = field.getKey();
            JsonNode value = field.getValue();
            Object convertedValue = convertJsonNodeValue(value);
            if (convertedValue != null) {
                dataMap.put(key, convertedValue);
            }
        }

        try {
            // 检查是否包含bt_signal_info字段
            if (!jsonNode.has("bt_signal_info")) {
                logger.debug("消息中不包含bt_signal_info字段, deviceId: {}", deviceId);
                return dataMap;
            }

            String btSignalInfo = jsonNode.get("bt_signal_info").asText();
            if (btSignalInfo == null || btSignalInfo.trim().isEmpty() || "null".equals(btSignalInfo)) {
                logger.debug("bt_signal_info为空, deviceId: {}", deviceId);
                return dataMap;
            }

            // 解析bt_signal_info JSON
            JsonNode btInfoNode = mapper.readTree(btSignalInfo);
            if (!btInfoNode.has("dataList") || !btInfoNode.get("dataList").isArray()) {
                logger.debug("bt_signal_info中不包含dataList数组, deviceId: {}", deviceId);
                return dataMap;
            }

            JsonNode dataListNode = btInfoNode.get("dataList");
            if (dataListNode.size() == 0) {
                logger.debug("bt_signal_info的dataList为空, deviceId: {}", deviceId);
                return dataMap;
            }

            // 提取beacon数据
            List<Map<String, Object>> beaconDataList = new ArrayList<>();
            for (JsonNode beaconNode : dataListNode) {
                Map<String, Object> beaconData = new HashMap<>();
                if (beaconNode.has("mac")) {
                    beaconData.put("MAC", beaconNode.get("mac").asText());
                }
                if (beaconNode.has("rssi")) {
                    beaconData.put("RSSI", String.valueOf(beaconNode.get("rssi").asInt()));
                }
                // 设置默认值
                beaconData.put("DATA", "");
                beaconData.put("BATTERY", "4"); // 默认电量

                if (beaconData.containsKey("MAC") && beaconData.containsKey("RSSI")) {
                    beaconDataList.add(beaconData);
                }
            }

            if (beaconDataList.isEmpty()) {
                logger.debug("未从bt_signal_info中提取到有效的beacon数据, deviceId: {}", deviceId);
                return dataMap;
            }

            logger.info("从设备[{}]提取到{}个beacon数据", deviceId, beaconDataList.size());

            // 调用定位引擎接口
            Map<String, Object> locationResult = callLocationEngine(deviceId, beaconDataList);
            if (locationResult != null) {
                // 新增：检查是否有有效坐标（x或y大于0），如果有则保存到external_coordinate_data表 - Shawn
                boolean hasValidCoordinate = false;
                Double xValue = null;
                Double yValue = null;

                if (locationResult.containsKey("x") && locationResult.containsKey("y")) {
                    try {
                        xValue = Double.parseDouble(locationResult.get("x").toString());
                        yValue = Double.parseDouble(locationResult.get("y").toString());
                        hasValidCoordinate = (xValue > 0 || yValue > 0);
                    } catch (Exception e) {
                        logger.debug("坐标值解析失败: {}", e.getMessage());
                    }
                }

                // 只要 x 或 y 有一个大于0就保存到 external_coordinate_data 表
                if (hasValidCoordinate) {
                    try {
                        saveChatLocationDataToExternalTable(deviceId, locationResult);
                        logger.info("有效坐标保存到{}表成功, deviceId: {}, x: {}, y: {}",
                                TdengineSuperTableConstants.EXTERNAL_COORDINATE_DATA,deviceId, xValue, yValue);
                    } catch (Exception e) {
                        logger.error("保存坐标数据到{}表失败, deviceId: {}, 错误: {}",
                                TdengineSuperTableConstants.EXTERNAL_COORDINATE_DATA,deviceId, e.getMessage(), e);
                    }
                } else {
                    logger.info("设备 {} 的坐标无效或不满足条件，跳过保存到{}表, x: {}, y: {}",
                        deviceId,TdengineSuperTableConstants.EXTERNAL_COORDINATE_DATA, xValue, yValue);
                }

                // 为helmet_runde_ca_report_location表添加请求和响应字段
                if (locationResult.containsKey("location_engine_request_body")) {
                    dataMap.put("location_engine_request_body", locationResult.get("location_engine_request_body"));
                }
                if (locationResult.containsKey("http_response_body")) {
                    dataMap.put("engine_http_response", locationResult.get("http_response_body"));
                }
            } else {
                logger.warn("设备[{}]定位引擎调用返回null，将保存原始GPS数据", deviceId);
                // 不向dataMap添加任何字段，保持原有逻辑
            }

        } catch (Exception e) {
            logger.error("处理蓝牙信标定位失败, deviceId: {}, 将保存原始数据", deviceId, e);
            // 不向dataMap添加任何字段，保持原有逻辑
        }

        return dataMap;
    }

    /**
     * 调用定位引擎接口
     *
     * @param deviceId       设备ID
     * @param beaconDataList beacon数据列表
     * @return 定位引擎返回的结果
     * @author Shawn
     * @date 2025-01-31
     */
    private Map<String, Object> callLocationEngine(String deviceId, List<Map<String, Object>> beaconDataList) {
        try {
            // 过滤beacon数据，只保留MAC地址在缓存中的数据
            List<Map<String, Object>> filteredBeaconDataList = filterValidBeaconData(deviceId, beaconDataList);

            // 如果过滤后没有有效的beacon数据，直接返回
            if (filteredBeaconDataList.isEmpty()) {
                logger.warn("设备 {} 过滤后没有有效的beacon数据，跳过定位引擎调用", deviceId);
                Map<String, Object> result = new HashMap<>();
                result.put("engine_status", "no_valid_beacons");
                result.put("http_status_code", 0);
                result.put("http_response_body", "没有有效的beacon数据");
                result.put("original_beacon_count", beaconDataList.size());
                result.put("filtered_beacon_count", 0);
                result.put("location_engine_request_body", "未生成请求体（无有效beacon数据）");
                return result;
            }

            // 调用统一的LocationEngineService，享受引擎选择和历史坐标查询功能
            logger.info("WebSocket通道调用定位引擎服务, deviceId: {}, 过滤后beacon数量: {}",
                    deviceId, filteredBeaconDataList.size());

            // WebSocket消息使用当前时间作为扫描时间，传递null
            Map<String, Object> result = locationEngineService.callLocationEngine(deviceId, filteredBeaconDataList, null);

            // 为了保持与原有逻辑的兼容性，确保返回结果包含必要的字段
            if (result != null) {
                // 确保包含原始和过滤后的beacon数量
                result.put("original_beacon_count", beaconDataList.size());
                result.put("filtered_beacon_count", filteredBeaconDataList.size());

                logger.info("WebSocket通道定位引擎调用完成, deviceId: {}, 引擎类型: {}, 状态: {}",
                        deviceId, result.get("engine_type"), result.get("engine_status"));
            }

            return result;

        } catch (Exception e) {
            logger.error("WebSocket通道调用定位引擎发生异常, deviceId: {}, 异常类型: {}, 错误信息: {}",
                    deviceId, e.getClass().getSimpleName(), e.getMessage(), e);

            // 即使发生异常也要返回错误信息
            Map<String, Object> result = new HashMap<>();
            result.put("engine_status", "exception");
            result.put("exception_type", e.getClass().getSimpleName());
            result.put("exception_message", e.getMessage());
            result.put("http_status_code", 0);
            result.put("http_response_body", "");
            result.put("original_beacon_count", beaconDataList != null ? beaconDataList.size() : 0);
            result.put("filtered_beacon_count", 0);
            result.put("location_engine_request_body", "异常导致未能生成请求体");
            return result;
        }
    }

    /**
     * 过滤有效的beacon数据，只保留MAC地址在缓存中的数据
     *
     * @param deviceId       设备ID
     * @param beaconDataList 原始beacon数据列表
     * @return 过滤后的beacon数据列表
     * @author Shawn
     * @date 2025-01-31
     */
    private List<Map<String, Object>> filterValidBeaconData(String deviceId, List<Map<String, Object>> beaconDataList) {
        List<Map<String, Object>> filteredList = new ArrayList<>();

        if (beaconDataList == null || beaconDataList.isEmpty()) {
            logger.debug("设备 {} 的beacon数据列表为空", deviceId);
            return filteredList;
        }

        int originalCount = beaconDataList.size();
        int validCount = 0;
        int invalidCount = 0;

        for (Map<String, Object> beaconData : beaconDataList) {
            if (beaconData == null) {
                continue;
            }

            // 尝试从beacon数据中提取MAC地址
            String macAddress = extractMacAddressFromBeaconData(beaconData);

            if (macAddress != null) {
                // 检查MAC地址是否在缓存中，并填充楼层/建筑上下文
                // @author Shawn @date 2026-04-09
                BeaconStationDO station = beaconMacAddressCache.getBeaconStation(macAddress);
                if (station != null) {
                    if (StringUtils.isNotBlank(station.getFloorId())) {
                        beaconData.put("floorId", station.getFloorId());
                    }
                    if (StringUtils.isNotBlank(station.getFloor())) {
                        beaconData.put("floor", station.getFloor());
                    }
                    if (StringUtils.isNotBlank(station.getBuildingId())) {
                        beaconData.put("buildingId", station.getBuildingId());
                    }
                    if (StringUtils.isNotBlank(station.getBuilding())) {
                        beaconData.put("building", station.getBuilding());
                    }
                    filteredList.add(beaconData);
                    validCount++;
                    logger.info("设备 {} 的beacon MAC地址 {} 在缓存中，保留数据", deviceId, macAddress);
                } else {
                    invalidCount++;
                    logger.info("设备 {} 的beacon MAC地址 {} 不在缓存中，过滤掉数据", deviceId, macAddress);
                }
            } else {
                invalidCount++;
                logger.info("设备 {} 的beacon数据中无法提取有效MAC地址，过滤掉数据: {}", deviceId, beaconData);
            }
        }

        logger.info("设备 {} beacon数据过滤完成，原始数量: {}, 有效数量: {}, 无效数量: {}",
                deviceId, originalCount, validCount, invalidCount);

        return filteredList;
    }

    /**
     * 从beacon数据中提取MAC地址
     * 支持多种可能的字段名称和格式
     *
     * @param beaconData beacon数据
     * @return MAC地址
     * @author Shawn
     * @date 2025-01-31
     */
    private String extractMacAddressFromBeaconData(Map<String, Object> beaconData) {
        if (beaconData == null || beaconData.isEmpty()) {
            return null;
        }

        // 可能的MAC地址字段名称
        String[] macFieldNames = { "mac", "MAC", "macAddress", "mac_address", "address", "addr", "beacon_id",
                "beaconId" };

        for (String fieldName : macFieldNames) {
            Object macValue = beaconData.get(fieldName);
            if (macValue != null) {
                String macStr = macValue.toString().trim();
                if (!macStr.isEmpty()) {
                    // 尝试格式化MAC地址
                    String formattedMac = formatMacAddressForCheck(macStr);
                    if (formattedMac != null) {
                        logger.debug("从字段 {} 提取到MAC地址: {} -> {}", fieldName, macStr, formattedMac);
                        return formattedMac;
                    }
                }
            }
        }

        // 如果没有找到，记录一下beacon数据的内容用于调试
        logger.debug("无法从beacon数据中提取MAC地址，数据内容: {}", beaconData);
        return null;
    }

    /**
     * 格式化MAC地址为标准格式用于检查
     * 从 80ECCCD09BFD 或其他格式转换为 80:EC:CC:D0:9B:FD
     *
     * @param originalMac 原始MAC地址
     * @return 标准格式的MAC地址，如果格式化失败返回null
     * @author Shawn
     * @date 2025-01-31
     */
    private String formatMacAddressForCheck(String originalMac) {
        if (originalMac == null || originalMac.trim().isEmpty()) {
            return null;
        }

        try {
            // 移除可能存在的分隔符和空格
            String cleanMac = originalMac.replaceAll("[:\\-\\s]", "").toUpperCase();

            // 检查长度是否为12（MAC地址应该是12个十六进制字符）
            if (cleanMac.length() != 12) {
                logger.debug("MAC地址长度不正确，应为12个字符: {}", originalMac);
                return null;
            }

            // 检查是否都是十六进制字符
            if (!cleanMac.matches("^[0-9A-F]{12}$")) {
                logger.debug("MAC地址包含非十六进制字符: {}", originalMac);
                return null;
            }

            // 格式化为标准MAC地址格式
            StringBuilder formatted = new StringBuilder();
            for (int i = 0; i < cleanMac.length(); i += 2) {
                if (i > 0) {
                    formatted.append(":");
                }
                formatted.append(cleanMac.substring(i, i + 2));
            }

            return formatted.toString();

        } catch (Exception e) {
            logger.debug("格式化MAC地址时发生异常: {}, 原始MAC: {}", e.getMessage(), originalMac);
            return null;
        }
    }

    /**
     * 保存安全帽设备数据到TDengine时序数据库（使用增强数据）
     *
     * @param deviceId        设备ID
     * @param enhancedDataMap 增强后的数据Map
     * @author Shawn
     * @date 2025-01-31
     */
    private void saveHelmetDataToTDengineWithEnhancedData(String deviceId, Map<String, Object> enhancedDataMap) {
        try {
            // 使用HelmetDataService保存数据
            R<cn.hutool.json.JSONObject> result = helmetRundeCaReportLocationTdEnginService.saveHelmetData(deviceId,
                    enhancedDataMap);

            if (result.getCode() != R.SUCCESS) {
                logger.error("安全帽设备数据保存失败, deviceId: {}, 错误信息: {}", deviceId, result.getMsg());
            } else {
                logger.info("安全帽设备数据保存成功, deviceId: {}", deviceId);
            }

        } catch (Exception e) {
            logger.error("保存安全帽设备数据异常, deviceId: {}", deviceId, e);
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
     * 保存Chat定位引擎数据到external_coordinate_data表
     *
     * @param deviceId       原生设备ID
     * @param locationResult 定位引擎返回结果
     * @author Shawn
     * @date 2025-07-23
     */
    private void saveChatLocationDataToExternalTable(String deviceId, Map<String, Object> locationResult) {
        try {
            // 使用原生的deviceId作为elderId
            if (deviceId == null || deviceId.trim().isEmpty()) {
                logger.warn("原生deviceId为空，跳过保存到external_coordinate_data表");
                return;
            }

            // 创建IotCoordinateRequest对象
            IotCoordinateRequestDO coordinateRequest = new IotCoordinateRequestDO();
            coordinateRequest.setElderId(deviceId);

            // 设置坐标信息
            if (locationResult.containsKey("x")) {
                Double x = Double.parseDouble(locationResult.get("x").toString());
                coordinateRequest.setX(x);
                coordinateRequest.setOriginalX(x);
                coordinateRequest.setScaleX(x);
            }
            if (locationResult.containsKey("y")) {
                Double y = Double.parseDouble(locationResult.get("y").toString());
                coordinateRequest.setY(y);
                coordinateRequest.setOriginalY(y);
                coordinateRequest.setScaleY(y);
            }

            // 设置其他字段
            if (locationResult.containsKey("address")) {
                coordinateRequest.setAddress(locationResult.get("address").toString());
            }
            if (locationResult.containsKey("mapId") && locationResult.get("mapId") != null) {
                coordinateRequest.setMapId(locationResult.get("mapId").toString());
            }
            if (locationResult.containsKey("orgCd")) {
                coordinateRequest.setOrgCd(locationResult.get("orgCd").toString());
            }
            if (locationResult.containsKey("type")) {
                coordinateRequest.setType(locationResult.get("type").toString());
            }
            if (locationResult.containsKey("appId")) {
                coordinateRequest.setAppId(locationResult.get("appId").toString());
            }
            if (locationResult.containsKey("warningId")) {
                coordinateRequest.setWarningId(Integer.parseInt(locationResult.get("warningId").toString()));
            }
            if (locationResult.containsKey("time")) {
                coordinateRequest.setTimeStr(locationResult.get("time").toString());
            }
            if (locationResult.containsKey("nearestBeacon")) {
                coordinateRequest.setNearestBeacon(locationResult.get("nearestBeacon").toString());
            }
            if (locationResult.containsKey("usedBeacons")) {
                coordinateRequest.setUsedBeacons(locationResult.get("usedBeacons").toString());
            }

            // 调用ExternalCoordinateService保存数据
            R<Map<String, Object>> saveResult = externalCoordinateService.saveCoordinateData(coordinateRequest);

            if (saveResult.getCode() == R.SUCCESS) {
                logger.info("Chat定位数据保存到{}表成功, deviceId: {}",TdengineSuperTableConstants.EXTERNAL_COORDINATE_DATA, deviceId);
            } else {
                logger.error("Chat定位数据保存到{}表失败, deviceId: {}, 错误: {}",
                        TdengineSuperTableConstants.EXTERNAL_COORDINATE_DATA,deviceId, saveResult.getMsg());
            }

        } catch (Exception e) {
            logger.error("保存Chat定位数据到external_coordinate_data表异常", e);
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
