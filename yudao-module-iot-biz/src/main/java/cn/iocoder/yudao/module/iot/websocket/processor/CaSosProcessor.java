package cn.iocoder.yudao.module.iot.websocket.processor;

import cn.hutool.json.JSONObject;
import com.fasterxml.jackson.databind.JsonNode;
import cn.iocoder.yudao.module.iot.dal.dao.HelmetDeviceDao;
import cn.iocoder.yudao.module.iot.service.HelmetSosTdEngineService;
import cn.iocoder.yudao.module.iot.service.SwmWarningManagementService;
import cn.iocoder.yudao.module.iot.util.JsonResponseBuilder;
import cn.iocoder.yudao.module.iot.websocket.service.WebSocketService;
import cn.iocoder.yudao.module.iot.util.R;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * CaSos消息处理器
 * 处理安全帽SOS报警消息，保存数据到TDengine并回复响应
 * 
 * @author Shawn
 * @date 2025-01-31
 */
@Component
public class CaSosProcessor {

    private static final Logger logger = LoggerFactory.getLogger(CaSosProcessor.class);

    @Autowired
    private HelmetSosTdEngineService helmetSosTdEngineService;

    @Autowired
    private SwmWarningManagementService swmWarningManagementService;

    @Autowired
    private HelmetDeviceDao helmetDeviceDao;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private WebSocketService webSocketService;

    /**
     * 处理ca_sos消息
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

        // 保存SOS信息到TDengine超级表
        try {
            saveHelmetSosDataToTDengine(deviceId, jsonNode);

            // 根据报警类型保存到swm_warning_management超级表
            saveWarningToSwmWarningManagement(deviceId, type, xPoint, yPoint, jsonNode);

            // 处理ZKF或EWF开头的bt_signal_info name的考勤打卡功能
            String btSignalInfo = jsonNode.has("bt_signal_info") ? jsonNode.get("bt_signal_info").asText() : null;
            if (shouldHandleAttendance(btSignalInfo)) {
                handleAttendanceClocking(deviceId, btSignalInfo);
            }

        } catch (Exception e) {
            logger.error("保存安全帽设备SOS数据到TDengine失败, deviceId: {}, 错误: {}",
                    deviceId, e.getMessage(), e);
        }

        // 使用工具类构建SOS响应JSON
        String responseJson = JsonResponseBuilder.buildCaSosResponse();
        sendMessageCallback.sendMessage(session, responseJson);
        logger.info("已发送ca_sos响应, sessionId: {}, deviceId: {}, 位置: [{}, {}], 类型: {}",
                sessionId, deviceId, xPoint, yPoint, type);
    }

    /**
     * 保存安全帽设备SOS数据到TDengine时序数据库（使用超级表）
     * 
     * @param deviceId 设备ID
     * @param jsonNode SOS数据JSON节点
     * @author Shawn
     * @date 2025-01-31
     */
    private void saveHelmetSosDataToTDengine(String deviceId, JsonNode jsonNode) {
        try {
            // 将JSON数据转换为Map
            Map<String, Object> sosDataMap = new HashMap<>();

            // 遍历JSON节点，提取所有字段（除了act字段）
            Iterator<Map.Entry<String, JsonNode>> fields = jsonNode.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> field = fields.next();
                String key = field.getKey();
                JsonNode value = field.getValue();

                // 根据JSON节点类型转换值
                Object convertedValue = convertJsonNodeValue(value);
                if (convertedValue != null) {
                    sosDataMap.put(key, convertedValue);
                }
            }

            // 使用HelmetSosTdEngineService保存SOS数据
            R<JSONObject> result = helmetSosTdEngineService.saveHelmetSosData(deviceId, sosDataMap);

            if (result.getCode() != R.SUCCESS) {
                logger.error("安全帽设备SOS数据保存失败, deviceId: {}, 错误信息: {}", deviceId, result.getMsg());
            } else {
                logger.info("安全帽设备SOS数据保存成功, deviceId: {}", deviceId);
            }

        } catch (Exception e) {
            logger.error("保存安全帽设备SOS数据异常, deviceId: {}", deviceId, e);
        }
    }

    /**
     * 根据报警类型将报警信息写入swm_warning_management超级表
     * 
     * @param deviceId 设备ID
     * @param type     报警类型: 1=脱帽报警, 4=跌落(撞击)报警, 6=静默报警, 11=近电报警
     * @param xPoint   纬度
     * @param yPoint   经度
     * @param jsonNode JSON消息节点（用于获取bt_signal_info）
     * @author wn
     * @date 2027-07-16
     */
    private void saveWarningToSwmWarningManagement(String deviceId, String type, String xPoint, String yPoint,
            JsonNode jsonNode) {
        if (StringUtils.isBlank(deviceId) || StringUtils.isBlank(type)) {
            logger.warn("报警信息不完整，无法写入预警管理表, deviceId: {}, type: {}", deviceId, type);
            return;
        }

        try {
            // 获取bt_signal_info
            String btSignalInfo = jsonNode.has("bt_signal_info") ? jsonNode.get("bt_signal_info").asText() : null;

            // 判断是否需要跳过插入，检查bt_signal_info中的设备名称
            if (shouldSkipWarningInsert(btSignalInfo)) {
                logger.info("检测到需要过滤的设备名称，跳过插入swm_warning_management表, deviceId: {}, bt_signal_info: {}",
                        deviceId, btSignalInfo);
                return;
            }

            // 查询设备绑定的身份证
            String idCard = helmetDeviceDao.getAssignedPersonByDeviceId(deviceId);

            // 判断是否是危险源报警
            boolean isDangerBeaconAlert = false;
            String dangerBeaconName = "";
            String hazardCategory = null; // 新增危险源类别

            // 检查是否有bt_signal_info且包含危险源信标(beacon_type=3)
            if (StringUtils.isNotBlank(btSignalInfo)) {
                Map<String, String> dangerBeaconResult = checkDangerBeacon(btSignalInfo);
                isDangerBeaconAlert = dangerBeaconResult != null && dangerBeaconResult.containsKey("isDanger")
                        && Boolean.parseBoolean(dangerBeaconResult.get("isDanger"));

                if (isDangerBeaconAlert) {
                    if (dangerBeaconResult.containsKey("beaconName")) {
                        dangerBeaconName = dangerBeaconResult.get("beaconName");
                    }

                    // 获取hazard_category
                    if (dangerBeaconResult.containsKey("hazardCategory")) {
                        hazardCategory = dangerBeaconResult.get("hazardCategory");
                        logger.info("获取到危险源类别: {}", hazardCategory);
                    }
                }
            }

            // 根据报警类型构造不同的预警信息
            String warningContent = "";
            String triggerReason = "";
            String alarmRecord = "";
            // 添加一个标志位，表示是否已经添加了位置信息
            boolean locationAdded = false;

            // 判断是危险源报警的情况：1. type为8; 或者 2. 检测到危险源信标(beacon_type=3)
            boolean isHazardAlert = "8".equals(type) && isDangerBeaconAlert;

            if (isHazardAlert) {
                // 如果是危险源报警
                warningContent = "危险源报警";

                // 如果有具体的信标名称，添加到触发原因中
                if (StringUtils.isNotBlank(dangerBeaconName)) {
                    triggerReason = "检测到工人进入危险区域：" + dangerBeaconName;
                    // 标记已经添加了位置信息
                    locationAdded = true;
                } else {
                    triggerReason = "检测到工人可能进入危险区域";
                }

                alarmRecord = "安全帽危险源报警记录";
                // 将type强制设置为8（危险源报警）
                type = "8";
            } else {
                switch (type) {
                    case "1":
                        warningContent = "脱帽报警";
                        triggerReason = "工人未正确佩戴安全帽";
                        alarmRecord = "安全帽脱帽报警记录";
                        break;
                    case "4":
                        warningContent = "跌落报警";
                        triggerReason = "检测到工人可能发生跌落或撞击";
                        alarmRecord = "安全帽跌落(撞击)报警记录";
                        break;
                    case "6":
                        warningContent = "静默报警";
                        triggerReason = "工人长时间未移动，可能发生意外";
                        alarmRecord = "安全帽静默报警记录";
                        break;
                    case "11":
                        warningContent = "近电报警";
                        triggerReason = "工人接近高压电设备，存在安全隐患";
                        alarmRecord = "安全帽近电报警记录";
                        break;
                    case "7":
                        warningContent = "一键SOS报警";
                        triggerReason = "工人手动触发SOS求救信号";
                        alarmRecord = "安全帽一键SOS报警记录";
                        break;
                    default:
                        Map<String, String> warningInfoFromBtSignal = getWarningInfoFromBtSignal(btSignalInfo);
                        if (!warningInfoFromBtSignal.isEmpty()) {
                            warningContent = warningInfoFromBtSignal.get("warningContent");
                        } else {
                            warningContent = "安全帽报警";
                            triggerReason = "安全帽发出报警信号，类型=" + type;
                            alarmRecord = "安全帽SOS报警记录";
                        }
                        break;
                }
            }

            // 添加位置信息
            // if (StringUtils.isNotBlank(xPoint) && StringUtils.isNotBlank(yPoint)) {
            // triggerReason = triggerReason + "，位置：[" + xPoint + ", " + yPoint + "]";
            // }

            // @author Shawn
            // @date 2025/06/23 注释掉位置信息添加，避免在触发原因中显示坐标

            // 保存预警信息到超级表，传递报警类型和bt_signal_info
            R<JSONObject> result = saveCaSosWarningToSwmWarningManagement(deviceId, idCard, warningContent,
                    triggerReason, alarmRecord, type, locationAdded, hazardCategory, btSignalInfo);

            if (result.getCode() != R.SUCCESS) {
                logger.error("保存{}报警信息到swm_warning_management超级表失败, deviceId: {}, 错误信息: {}",
                        warningContent, deviceId, result.getMsg());
            } else {
                logger.info("成功保存{}报警信息到swm_warning_management超级表, deviceId: {}, 类型: {}",
                        warningContent, deviceId, type);
            }

            // 特殊处理：当type等于8时，查询语音模板并发送消息给当前设备
            if ("8".equals(type)) {
                handleType8VoiceTemplate(deviceId, btSignalInfo);
            }
        } catch (Exception e) {
            logger.error("保存报警信息到swm_warning_management超级表异常, deviceId: {}, type: {}", deviceId, type, e);
        }
    }

    /**
     * 判断是否应该跳过插入swm_warning_management表
     * 根据bt_signal_info中设备名称判断：
     * - 如果设备名称以"EW1"开头，跳过插入
     * - 如果设备名称以"EWF"开头，跳过插入
     * 
     * @param btSignalInfo 蓝牙信号信息JSON字符串
     * @return 如果应该跳过插入返回true，否则返回false
     * @author zwf
     * @date 2025-07-22
     */
    private boolean shouldSkipWarningInsert(String btSignalInfo) {
        if (StringUtils.isBlank(btSignalInfo)) {
            return false; // 没有蓝牙信号信息，不跳过
        }

        try {
            // 解析JSON
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            JsonNode btSignalNode = mapper.readTree(btSignalInfo);

            // 检查是否有dataList字段
            if (!btSignalNode.has("dataList")) {
                return false;
            }

            JsonNode dataListNode = btSignalNode.get("dataList");
            if (!dataListNode.isArray()) {
                return false;
            }

            // 遍历dataList中的每个设备
//            for (JsonNode dataNode : dataListNode) {
//                if (dataNode.has("name")) {
//                    String name = dataNode.get("name").asText();
//                    if (StringUtils.isNotBlank(name)) {
//                        // 检查name是否以EW1或EWF开头
//                        if (name.startsWith("EW1") || name.startsWith("EWF")) {
//                            logger.info("检测到需要过滤的设备名称: {}", name);
//                            return true; // 找到匹配的设备名，应该跳过插入
//                        }
//                    }
//                }
//            }

            return false; // 没有找到需要过滤的设备名

        } catch (Exception e) {
            logger.error("解析bt_signal_info异常，无法判断是否跳过插入: {}", btSignalInfo, e);
            return false; // 出现异常时不跳过插入
        }
    }

    /**
     * 保存CaSos报警信息到swm_warning_management超级表
     * 
     * @param deviceId       设备ID
     * @param idCard         身份证ID
     * @param warningContent 警告内容
     * @param triggerReason  触发原因
     * @param alarmRecord    警报记录
     * @param type           报警类型
     * @param locationAdded  是否已经添加了位置信息
     * @param hazardCategory 危险源类别
     * @return 保存结果
     * @author wn
     * @date 2027-07-16
     */
    private R<JSONObject> saveCaSosWarningToSwmWarningManagement(
            String deviceId, String idCard, String warningContent, String triggerReason, String alarmRecord,
            String type, boolean locationAdded, String hazardCategory, String btSignalInfo) {
        try {
            // 根据报警类型决定warningType值
            // 当type=8(危险源报警)时使用warningType=2(被动报警)
            // 其他情况使用warningType=1(主动报警)
            String warningType = "8".equals(type) ? "2" : "1";

            // 从bt_signal_info获取location
            String location = getLocationFromBtSignalInfo(btSignalInfo);
            
            // 从bt_signal_info获取areaName
            String areaName = getAreaNameFromBtSignalInfo(btSignalInfo);
            
            logger.info("报警类型: {}, 使用warningType: {}, 是否已添加位置信息: {}, 危险源类别: {}, 从bt_signal_info获取的location: {}, areaName: {}",
                    type, warningType, locationAdded, hazardCategory, location, areaName);

            // 传递原始type值和bt_signal_info、location、areaName到服务层
            R<JSONObject> result = swmWarningManagementService.saveCustomWarningToSwmWarningManagement(
                    deviceId, idCard, warningContent, triggerReason, alarmRecord, warningType, type, locationAdded,
                    hazardCategory, btSignalInfo, location, areaName);
            return result;
        } catch (Exception e) {
            logger.error("保存CaSos报警信息到swm_warning_management超级表异常", e);
            return R.fail("保存预警信息异常: " + e.getMessage());
        }
    }

    /**
     * 处理type=8时的语音模板查询并发送消息给指定设备
     * 基于数据库配置，从swm_hazard_source表中查询匹配的beacon_tag
     * 
     * @param deviceId     设备ID
     * @param btSignalInfo 蓝牙信号信息JSON字符串
     * @author Shawn
     * @date 2025/06/22
     */
    private void handleType8VoiceTemplate(String deviceId, String btSignalInfo) {
        try {
            logger.info("开始处理type=8语音模板查询并发送消息给设备: {}, bt_signal_info: {}", deviceId, btSignalInfo);

            if (StringUtils.isBlank(btSignalInfo)) {
                logger.warn("bt_signal_info为空，无法处理语音模板");
                return;
            }

            // 解析JSON
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            JsonNode btSignalNode = mapper.readTree(btSignalInfo);

            // 检查是否有dataList字段
            if (!btSignalNode.has("dataList")) {
                logger.warn("bt_signal_info中没有dataList字段");
                return;
            }

            JsonNode dataListNode = btSignalNode.get("dataList");
            if (!dataListNode.isArray()) {
                logger.warn("bt_signal_info中的dataList不是数组格式");
                return;
            }

            // 查询所有危险源配置
            String sql = "SELECT beacon_tag, voice_template_id, hazard_name FROM swm_hazard_source WHERE status = '0' AND beacon_tag IS NOT NULL AND voice_template_id IS NOT NULL";
            java.util.List<java.util.Map<String, Object>> hazardSources = jdbcTemplate.queryForList(sql);

            // 用于记录已经处理过的模板ID，避免重复发送
            java.util.Set<String> processedTemplateIds = new java.util.HashSet<>();

            // 遍历dataList中的每个MAC地址信息
            for (JsonNode dataNode : dataListNode) {
                if (dataNode.has("name")) {
                    String name = dataNode.get("name").asText();
                    if (StringUtils.isNotBlank(name)) {
                        logger.info("检查MAC name: {}", name);

                        String templateId = null;
                        String conditionType = null;

                        // 硬编码处理EW1,ZK1
                        if (name.startsWith("ZK1") || name.startsWith("EW1")) {
                            templateId = "1935324575087489024";
                            conditionType = "ZK1/EW1";
                        }

                        // 如果硬编码匹配到条件且未处理过该模板ID
                        if (StringUtils.isNotBlank(templateId) && !processedTemplateIds.contains(templateId)) {
                            logger.info("硬编码匹配到{}前缀，准备发送模板ID: {}", conditionType, templateId);

                            // 发送对应的语音模板消息
                            sendVoiceTemplateMessage(deviceId, templateId, conditionType);

                            // 记录已处理的模板ID
                            processedTemplateIds.add(templateId);
                        } else if (templateId == null) {
                            // 如果硬编码没有匹配，则检查是否匹配数据库中的配置
                            for (java.util.Map<String, Object> hazardSource : hazardSources) {
                                String beaconTag = (String) hazardSource.get("beacon_tag");
                                String voiceTemplateId = (String) hazardSource.get("voice_template_id");
                                String hazardName = (String) hazardSource.get("hazard_name");

                                if (StringUtils.isNotBlank(beaconTag)) {
                                    // beacon_tag用逗号分隔，支持多个前缀
                                    String[] tags = beaconTag.split(",");
                                    for (String tag : tags) {
                                        tag = tag.trim();
                                        if (StringUtils.isNotBlank(tag) && name.startsWith(tag)) {
                                            // 匹配到前缀且未处理过该模板ID
                                            if (StringUtils.isNotBlank(voiceTemplateId)
                                                    && !processedTemplateIds.contains(voiceTemplateId)) {
                                                logger.info("数据库配置匹配到{}前缀，危险源: {}, 准备发送模板ID: {}", tag, hazardName,
                                                        voiceTemplateId);

                                                // 发送对应的语音模板消息
                                                sendVoiceTemplateMessage(deviceId, voiceTemplateId,
                                                        tag + "(" + hazardName + ")");

                                                // 记录已处理的模板ID
                                                processedTemplateIds.add(voiceTemplateId);
                                            }
                                            break; // 找到匹配的前缀就跳出内层循环
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (processedTemplateIds.isEmpty()) {
                logger.warn("未在bt_signal_info中找到匹配的MAC name前缀");
            } else {
                logger.info("type=8语音模板处理完成，共发送了{}个模板消息给设备: {}", processedTemplateIds.size(), deviceId);
            }

        } catch (Exception e) {
            logger.error("处理type=8语音模板查询并发送消息给设备异常, deviceId: {}, btSignalInfo: {}", deviceId, btSignalInfo, e);
        }
    }

    /**
     * 发送语音模板消息给指定设备
     * 
     * @param deviceId      设备ID
     * @param templateId    模板ID
     * @param conditionType 匹配的条件类型（用于日志记录）
     * @author Shawn
     * @date 2025-01-31
     */
    private void sendVoiceTemplateMessage(String deviceId, String templateId, String conditionType) {
        try {
            // 查询语音模板内容
            String sql = "SELECT content FROM swm_voice_template WHERE id = ?";
            String templateContent = jdbcTemplate.queryForObject(sql, String.class, templateId);

            if (StringUtils.isNotBlank(templateContent)) {
                logger.info("查询到语音模板内容, 条件: {}, ID: {}, 内容: {}", conditionType, templateId, templateContent);

                // 构建消息，参照一键召回的方式
                String message = buildBroadcastMessage(templateContent.trim());

                // 通过WebSocket服务发送消息给指定设备
                webSocketService.sendMessageToDevice(deviceId, message);

                logger.info("{}语音模板消息发送成功给设备: {}, 模板ID: {}, 消息内容: {}", conditionType, deviceId, templateId, message);
            } else {
                logger.warn("未查询到语音模板内容，条件: {}, ID: {}", conditionType, templateId);
            }
        } catch (Exception e) {
            logger.error("发送语音模板消息异常, 设备: {}, 条件: {}, 模板ID: {}, 错误: {}", deviceId, conditionType, templateId,
                    e.getMessage(), e);
        }
    }

    /**
     * 从蓝牙信号信息中确定语音模板ID
     * 基于数据库配置，从swm_hazard_source表中查询匹配的beacon_tag
     * 
     * @param btSignalInfo 蓝牙信号信息JSON字符串
     * @return 语音模板ID，如果没有匹配则返回null
     * @author Shawn
     * @date 2025/06/22
     */
    private String determineTemplateIdFromBtSignal(String btSignalInfo) {
        if (StringUtils.isBlank(btSignalInfo)) {
            logger.warn("bt_signal_info为空，无法确定语音模板ID");
            return null;
        }

        try {
            // 解析JSON
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            JsonNode btSignalNode = mapper.readTree(btSignalInfo);

            // 检查是否有dataList字段
            if (!btSignalNode.has("dataList")) {
                logger.warn("bt_signal_info中没有dataList字段");
                return null;
            }

            JsonNode dataListNode = btSignalNode.get("dataList");
            if (!dataListNode.isArray()) {
                logger.warn("bt_signal_info中的dataList不是数组格式");
                return null;
            }

            // 查询所有危险源配置
            String sql = "SELECT beacon_tag, voice_template_id, hazard_name FROM swm_hazard_source WHERE status = '0' AND beacon_tag IS NOT NULL AND voice_template_id IS NOT NULL";
            java.util.List<java.util.Map<String, Object>> hazardSources = jdbcTemplate.queryForList(sql);

            // 遍历dataList中的每个MAC地址信息
            for (JsonNode dataNode : dataListNode) {
                if (dataNode.has("name")) {
                    String name = dataNode.get("name").asText();
                    if (StringUtils.isNotBlank(name)) {
                        logger.info("检查MAC name: {}", name);

                        // 硬编码处理EW1,ZK1
                        if (name.startsWith("ZK1") || name.startsWith("EW1")) {
                            logger.info("硬编码匹配到ZK1/EW1前缀，返回模板ID: 1935324575087489024");
                            return "1935324575087489024";
                        }

                        // 检查是否匹配数据库中的配置
                        for (java.util.Map<String, Object> hazardSource : hazardSources) {
                            String beaconTag = (String) hazardSource.get("beacon_tag");
                            String voiceTemplateId = (String) hazardSource.get("voice_template_id");
                            String hazardName = (String) hazardSource.get("hazard_name");

                            if (StringUtils.isNotBlank(beaconTag)) {
                                // beacon_tag用逗号分隔，支持多个前缀
                                String[] tags = beaconTag.split(",");
                                for (String tag : tags) {
                                    tag = tag.trim();
                                    if (StringUtils.isNotBlank(tag) && name.startsWith(tag)) {
                                        logger.info("数据库配置匹配到{}前缀，危险源: {}, 返回模板ID: {}", tag, hazardName,
                                                voiceTemplateId);
                                        return voiceTemplateId;
                                    }
                                }
                            }
                        }
                    }
                }
            }

            logger.warn("未在bt_signal_info中找到匹配的MAC name前缀");
            return null;

        } catch (Exception e) {
            logger.error("解析bt_signal_info异常: {}", btSignalInfo, e);
            return null;
        }
    }

    /**
     * 构建语音模板消息
     * 参照一键召回的消息格式
     * 
     * @param templateContent 语音模板内容
     * @return JSON格式的消息
     * @author Shawn
     * @date 2025-01-07
     */
    private String buildBroadcastMessage(String templateContent) {
        // 转义特殊字符，防止JSON格式错误
        String escapedContent = templateContent
                .replace("\\", "\\\\") // 转义反斜杠
                .replace("\"", "\\\"") // 转义双引号
                .replace("\r", "\\r") // 转义回车
                .replace("\n", "\\n") // 转义换行
                .replace("\t", "\\t"); // 转义制表符

        return String.format(
                "{\"cmd\":\"server_push_ma_broadcast\",\"message\":\"%s\",\"message_type\":0}",
                escapedContent);
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

    /**
     * 检查是否需要处理考勤功能
     * 通过解析bt_signal_info中的name字段判断
     * 
     * @param btSignalInfo 蓝牙信号信息JSON字符串
     * @return 如果需要处理考勤则返回true
     * @author Shawn
     * @date 2025-01-31
     */
    private boolean shouldHandleAttendance(String btSignalInfo) {
        if (StringUtils.isBlank(btSignalInfo)) {
            return false;
        }

        try {
            // 解析JSON
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            JsonNode btSignalNode = mapper.readTree(btSignalInfo);

            // 检查是否有dataList字段
            if (!btSignalNode.has("dataList")) {
                return false;
            }

            JsonNode dataListNode = btSignalNode.get("dataList");
            if (!dataListNode.isArray()) {
                return false;
            }

            // 遍历dataList中的每个MAC地址信息
            for (JsonNode dataNode : dataListNode) {
                if (dataNode.has("name")) {
                    String name = dataNode.get("name").asText();
                    if (StringUtils.isNotBlank(name)) {
                        // 检查name是否以ZKF或EWF开头
                        if (name.startsWith("ZKF") || name.startsWith("EWF")) {
                            logger.info("检测到需要处理考勤的MAC name: {}", name);
                            return true;
                        }
                    }
                }
            }

            return false;

        } catch (Exception e) {
            logger.error("解析bt_signal_info判断考勤处理异常: {}", btSignalInfo, e);
            return false;
        }
    }

    /**
     * 处理ZKF或EWF开头的bt_signal_info name的考勤打卡功能
     * 
     * @param deviceId     设备ID
     * @param btSignalInfo 蓝牙信号信息JSON字符串
     * @author Shawn
     * @date 2025-01-31
     */
    private void handleAttendanceClocking(String deviceId, String btSignalInfo) {
        try {
            logger.info("开始处理ZKF或EWF开头的MAC name的考勤打卡功能, deviceId: {}, btSignalInfo: {}", deviceId, btSignalInfo);

            // 查询身份证
            String idCard = helmetDeviceDao.getAssignedPersonByDeviceId(deviceId);

            if (StringUtils.isBlank(idCard)) {
                logger.warn("未查询到设备绑定的身份证, deviceId: {}", deviceId);
                return;
            }

            // 更新考勤记录
            R<JSONObject> result = swmWarningManagementService.updateAttendanceRecord(deviceId, idCard);

            if (result.getCode() != R.SUCCESS) {
                logger.error("更新考勤记录失败, deviceId: {}, 错误信息: {}", deviceId, result.getMsg());
            } else {
                logger.info("考勤记录更新成功, deviceId: {}", deviceId);
            }
        } catch (Exception e) {
            logger.error("处理ZKF或EWF开头的MAC name的考勤打卡功能异常, deviceId: {}, btSignalInfo: {}, 错误: {}", deviceId,
                    btSignalInfo, e.getMessage(), e);
        }
    }

    /**
     * 检查是否存在危险源信标
     * 解析bt_signal_info中的MAC地址，并在swm_beacon_station表中查询是否有beacon_type为3的记录
     * 
     * @param btSignalInfo 蓝牙信号信息JSON字符串
     * @return 包含检查结果的Map，key为"isDanger"表示是否是危险源，"beaconName"表示危险源信标名称，"hazardCategory"表示危险源类别
     * @author zwf
     * @date 2025-07-20
     */
    private Map<String, String> checkDangerBeacon(String btSignalInfo) {
        try {
            if (StringUtils.isBlank(btSignalInfo)) {
                logger.warn("bt_signal_info为空，无法检查危险源信标");
                return null;
            }

            // 解析JSON
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            JsonNode btSignalNode = mapper.readTree(btSignalInfo);

            // 检查是否有dataList字段
            if (!btSignalNode.has("dataList")) {
                logger.warn("bt_signal_info中没有dataList字段");
                return null;
            }

            JsonNode dataListNode = btSignalNode.get("dataList");
            if (!dataListNode.isArray()) {
                logger.warn("bt_signal_info中的dataList不是数组格式");
                return null;
            }

            // 遍历dataList中的每个MAC地址信息
            for (JsonNode dataNode : dataListNode) {
                if (dataNode.has("mac") && dataNode.has("name")) {
                    String mac = dataNode.get("mac").asText();
                    String name = dataNode.get("name").asText();
                    int rssi = dataNode.has("rssi") ? dataNode.get("rssi").asInt() : -100;

                    // 跳过全0的mac地址
                    if ("00:00:00:00:00:00".equals(mac)) {
                        continue;
                    }

                    logger.info("检查MAC: {}, name: {}, rssi: {}", mac, name, rssi);

                    // 格式化MAC地址（去掉冒号）
                    String formattedMac = mac.replaceAll(":", "").toUpperCase();

                    // 查询MySQL中是否有此信标，且beacon_type为3（危险源信标）
                    String beaconSql = "SELECT location FROM swm_beacon_station WHERE beacon_id = ? AND beacon_type = '3' AND status = '0'";
                    List<Map<String, Object>> beaconResults = jdbcTemplate.queryForList(beaconSql, formattedMac);

                    if (!beaconResults.isEmpty()) {
                        // 找到危险源信标
                        String location = (String) beaconResults.get(0).get("location");
                        logger.info("找到危险源信标: MAC={}, 位置={}", formattedMac, location);

                        // 查询hazard_category
                        String hazardCategorySql = "SELECT hazard_category FROM swm_hazard_source WHERE FIND_IN_SET(?, beacon_identifier) > 0 AND status = '0'";
                        List<Map<String, Object>> hazardResults = jdbcTemplate.queryForList(hazardCategorySql,
                                formattedMac);

                        String hazardCategory = null;
                        if (!hazardResults.isEmpty() && hazardResults.get(0).get("hazard_category") != null) {
                            hazardCategory = (String) hazardResults.get(0).get("hazard_category");
                            logger.info("找到危险源类别: MAC={}, 类别={}", formattedMac, hazardCategory);
                        }

                        Map<String, String> result = new HashMap<>();
                        result.put("isDanger", "true");
                        result.put("beaconName", StringUtils.isNotBlank(location) ? location : name);
                        if (hazardCategory != null) {
                            result.put("hazardCategory", hazardCategory);
                        }
                        return result;
                    }
                }
            }

            // 未找到危险源信标
            logger.info("未找到危险源信标");
            Map<String, String> result = new HashMap<>();
            result.put("isDanger", "false");
            return result;

        } catch (Exception e) {
            logger.error("检查危险源信标异常: {}", btSignalInfo, e);
            return null;
        }
    }

    /**
     * 根据蓝牙信号信息获取预警内容
     * 
     * @param btSignalInfo 蓝牙信号信息JSON字符串
     * @return 包含预警信息的Map，如果无匹配则为空Map
     * @author Shawn
     * @date 2025/06/30
     */
    private Map<String, String> getWarningInfoFromBtSignal(String btSignalInfo) {
        Map<String, String> warningInfo = new HashMap<>();
        if (StringUtils.isBlank(btSignalInfo)) {
            return warningInfo;
        }

        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            JsonNode btSignalNode = mapper.readTree(btSignalInfo);

            if (!btSignalNode.has("dataList")) {
                return warningInfo;
            }

            JsonNode dataListNode = btSignalNode.get("dataList");
            if (!dataListNode.isArray()) {
                return warningInfo;
            }

            for (JsonNode dataNode : dataListNode) {
                if (dataNode.has("name")) {
                    String name = dataNode.get("name").asText();
                    if (StringUtils.isNotBlank(name)) {
                        String warningContent = null;
                        if (name.startsWith("EW1") || name.startsWith("ZK1")) {
                            warningContent = "进入大门";
                        } else if (name.startsWith("EW2") || name.startsWith("ZK2")) {
                            warningContent = "气站预警";
                        } else if (name.startsWith("EW3") || name.startsWith("ZK3")) {
                            warningContent = "油漆库房预警";
                        } else if (name.startsWith("EWF") || name.startsWith("ZKF")) {
                            warningContent = "考勤打卡";
                        }

                        if (warningContent != null) {
                            warningInfo.put("warningContent", warningContent);
                            warningInfo.put("triggerReason", "蓝牙信标触发: " + name);
                            warningInfo.put("alarmRecord", "蓝牙信标报警记录");
                            return warningInfo;
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error("解析 bt_signal_info 获取 warningContent 异常: {}", btSignalInfo, e);
        }

        return warningInfo;
    }

    /**
     * 从bt_signal_info中解析MAC地址并获取location
     * @param btSignalInfo 蓝牙信号信息JSON字符串
     * @return location字符串，如果解析失败返回空字符串
     */
    private String getLocationFromBtSignalInfo(String btSignalInfo) {
        if (StringUtils.isBlank(btSignalInfo)) {
            return "";
        }

        try {
            // 解析JSON
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            JsonNode rootNode = mapper.readTree(btSignalInfo);
            JsonNode dataListNode = rootNode.get("dataList");

            if (dataListNode == null || !dataListNode.isArray()) {
                return "";
            }

            // 找出有位置信息且信号最强的MAC地址
            MacLocationInfo bestMacWithLocation = findBestMacWithLocation(dataListNode);

            if (bestMacWithLocation != null) {
                logger.info("找到有位置信息的最强信号MAC: {}, RSSI: {}, 位置: {}",
                    bestMacWithLocation.getMac(), bestMacWithLocation.getRssi(), bestMacWithLocation.getLocation());
                return bestMacWithLocation.getLocation();
            }

        } catch (Exception e) {
            logger.error("解析bt_signal_info获取location异常: {}", btSignalInfo, e);
        }

        return "";
    }

    /**
     * 根据MAC地址查询location
     * @param mac MAC地址
     * @return location字符串，如果查询失败返回空字符串
     */
    private String queryLocationByMac(String mac) {
        try {
            // 转换MAC地址格式：去掉冒号并转换为大写
            String formattedMac = mac.replace(":", "").toUpperCase();
            
            String sql = "SELECT location FROM swm_beacon_station WHERE beacon_id = ? AND status = '0'";
            List<String> results = jdbcTemplate.queryForList(sql, String.class, formattedMac);
            
            if (!results.isEmpty()) {
                return results.get(0);
            }
        } catch (Exception e) {
            logger.error("根据MAC地址查询location异常, mac: {}", mac, e);
        }
        
        return "";
    }

    /**
     * 找出有位置信息且信号最强的MAC地址
     * @param dataListNode JSON数据列表节点
     * @return 有位置信息的最强信号MAC信息，如果没有找到返回null
     */
    private MacLocationInfo findBestMacWithLocation(JsonNode dataListNode) {
        MacLocationInfo bestMacWithLocation = null;
        int bestRssi = Integer.MIN_VALUE;

        // 遍历所有MAC地址
        for (JsonNode dataNode : dataListNode) {
            JsonNode macNode = dataNode.get("mac");
            JsonNode rssiNode = dataNode.get("rssi");

            if (macNode != null && rssiNode != null) {
                String mac = macNode.asText();
                int rssi = rssiNode.asInt();

                // 过滤掉无效的MAC地址
                if (!"00:00:00:00:00:00".equals(mac)) {
                    // 查询该MAC是否有位置信息
                    String location = queryLocationByMac(mac);

                    // 如果有位置信息，且信号强度更强，则更新最佳选择
                    if (StringUtils.isNotBlank(location) && rssi > bestRssi) {
                        bestRssi = rssi;
                        bestMacWithLocation = new MacLocationInfo(mac, rssi, location);
                        logger.debug("更新最佳MAC: {}, RSSI: {}, 位置: {}", mac, rssi, location);
                    }
                }
            }
        }

        return bestMacWithLocation;
    }

    /**
     * MAC地址位置信息内部类
     */
    private static class MacLocationInfo {
        private final String mac;
        private final int rssi;
        private final String location;

        public MacLocationInfo(String mac, int rssi, String location) {
            this.mac = mac;
            this.rssi = rssi;
            this.location = location;
        }

        public String getMac() {
            return mac;
        }

        public int getRssi() {
            return rssi;
        }

        public String getLocation() {
            return location;
        }
    }

    /**
     * MAC地址区域信息内部类
     * @author Assistant
     * @date 2025/01/25
     */
    private static class MacAreaInfo {
        private final String mac;
        private final int rssi;
        private final String areaId;
        private final String areaName;

        public MacAreaInfo(String mac, int rssi, String areaId, String areaName) {
            this.mac = mac;
            this.rssi = rssi;
            this.areaId = areaId;
            this.areaName = areaName;
        }

        public String getMac() {
            return mac;
        }

        public int getRssi() {
            return rssi;
        }

        public String getAreaId() {
            return areaId;
        }

        public String getAreaName() {
            return areaName;
        }
    }

    /**
     * 从蓝牙信号信息中获取区域名称
     * 逻辑与getLocationFromBtSignalInfo完全一致，但查询的是area信息
     * @param btSignalInfo 蓝牙信号信息
     * @return 区域名称，如果未找到返回空字符串
     * @author Assistant
     * @date 2025/01/25
     */
    private String getAreaNameFromBtSignalInfo(String btSignalInfo) {
        if (StringUtils.isBlank(btSignalInfo)) {
            return "";
        }

        try {
            // 解析JSON
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            JsonNode rootNode = mapper.readTree(btSignalInfo);
            JsonNode dataListNode = rootNode.get("dataList");

            if (dataListNode == null || !dataListNode.isArray()) {
                return "";
            }

            // 找出有区域信息且信号最强的MAC地址
            MacAreaInfo bestMacWithAreaInfo = findBestMacWithAreaInfo(dataListNode);

            if (bestMacWithAreaInfo != null) {
                logger.info("找到有区域信息的最强信号MAC: {}, RSSI: {}, 区域: {}",
                    bestMacWithAreaInfo.getMac(), bestMacWithAreaInfo.getRssi(), bestMacWithAreaInfo.getAreaName());
                return bestMacWithAreaInfo.getAreaName();
            }

        } catch (Exception e) {
            logger.error("解析bt_signal_info获取area异常: {}", btSignalInfo, e);
        }

        return "";
    }

    /**
     * 找出有区域信息且信号最强的MAC地址
     * @param dataListNode JSON数据列表节点
     * @return 有区域信息的最强信号MAC信息，如果没有找到返回null
     * @author Assistant
     * @date 2025/01/25
     */
    private MacAreaInfo findBestMacWithAreaInfo(JsonNode dataListNode) {
        MacAreaInfo bestMacWithAreaInfo = null;
        int bestRssi = Integer.MIN_VALUE;

        // 遍历所有MAC地址
        for (JsonNode dataNode : dataListNode) {
            JsonNode macNode = dataNode.get("mac");
            JsonNode rssiNode = dataNode.get("rssi");

            if (macNode != null && rssiNode != null) {
                String mac = macNode.asText();
                int rssi = rssiNode.asInt();

                // 过滤掉无效的MAC地址
                if (!"00:00:00:00:00:00".equals(mac)) {
                    // 查询该MAC是否有区域信息
                    Map<String, String> areaInfo = queryAreaInfoByMac(mac);

                    // 如果有区域信息，且信号强度更强，则更新最佳选择
                    if (areaInfo != null && StringUtils.isNotBlank(areaInfo.get("areaName")) && rssi > bestRssi) {
                        bestRssi = rssi;
                        bestMacWithAreaInfo = new MacAreaInfo(mac, rssi, areaInfo.get("areaId"), areaInfo.get("areaName"));
                        logger.debug("更新最佳MAC: {}, RSSI: {}, 区域ID: {}, 区域名称: {}", 
                            mac, rssi, areaInfo.get("areaId"), areaInfo.get("areaName"));
                    }
                }
            }
        }

        return bestMacWithAreaInfo;
    }

    /**
     * 根据MAC地址查询区域信息
     * 使用联合查询一次性获取区域ID和区域名称
     * @param mac MAC地址
     * @return 包含areaId和areaName的Map，如果查询失败返回null
     * @author Assistant
     * @date 2025/01/25
     */
    private Map<String, String> queryAreaInfoByMac(String mac) {
        try {
            // 转换MAC地址格式：去掉冒号并转换为大写
            String formattedMac = mac.replace(":", "").toUpperCase();
            
            String sql = "SELECT bs.area as areaId, a.area_name as areaName " +
                        "FROM swm_beacon_station bs " +
                        "LEFT JOIN swm_area a ON bs.area = a.id AND a.del_flag = '0' AND a.status = '0' " +
                        "WHERE bs.beacon_id = ? AND bs.status = '0'";
            
            List<Map<String, Object>> results = jdbcTemplate.queryForList(sql, formattedMac);
            
            if (!results.isEmpty()) {
                Map<String, Object> row = results.get(0);
                Map<String, String> areaInfo = new HashMap<>();
                areaInfo.put("areaId", row.get("areaId") != null ? row.get("areaId").toString() : null);
                areaInfo.put("areaName", row.get("areaName") != null ? row.get("areaName").toString() : null);
                return areaInfo;
            }
        } catch (Exception e) {
            logger.error("根据MAC地址查询area信息异常, mac: {}", mac, e);
        }
        
        return null;
    }
}