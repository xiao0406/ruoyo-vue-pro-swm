package cn.iocoder.yudao.module.iot.tcp.processor;

import cn.iocoder.yudao.module.iot.cache.service.RedisService;
import cn.iocoder.yudao.module.swm.api.constant.SwmRedisKeyConstants;
import cn.iocoder.yudao.module.iot.mqtt.handler.zhongtai.DeviceSoSAlarmHandler;
import cn.iocoder.yudao.module.iot.tcp.model.TcpMessageData;
import cn.iocoder.yudao.module.iot.service.HelmetSosTdEngineService;
import cn.iocoder.yudao.module.iot.service.SwmWarningManagementService;
import cn.iocoder.yudao.module.swm.service.TDengineService;
import cn.iocoder.yudao.module.iot.tcp.parser.TcpMessageParser;
import cn.iocoder.yudao.module.iot.tcp.service.TcpBeaconLocationService;
import cn.iocoder.yudao.module.iot.util.R;
import cn.hutool.json.JSONObject;
import io.netty.channel.ChannelHandlerContext;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * SOS报警TCP消息处理器
 * 仅处理报警值恰好为8（纯SOS报警）的设备数据，并下发语音提醒
 * 如果报警值包含其他报警类型，将被过滤不处理
 * 
 * @author Shawn
 * @date 2025-01-26
 */
@Component
public class SosTcpProcessor {
    
    private static final Logger logger = LoggerFactory.getLogger(SosTcpProcessor.class);
    
    @Autowired
    private HelmetSosTdEngineService helmetSosTdEngineService;
    
    @Autowired
    private SwmWarningManagementService swmWarningManagementService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private TcpBeaconLocationService tcpBeaconLocationService;

    @Autowired
    private TDengineService tdEngineService;
    
    @Autowired
    private TcpMessageParser tcpMessageParser;
    @Autowired
    private RedisService redisService;
    @Autowired
    @Lazy
    private DeviceSoSAlarmHandler soSAlarmHandler;
    
    @Value("${tdengine.dbname}")
    private String dbname;
    
    // SOS报警在第3位（从0开始），对应的值是2^3=8
    private static final int SOS_ALARM_BIT_VALUE = 8;
    
    // 字典配置常量
    private static final String DICT_TYPE = "klt_xiafa_yy_cs";
    private static final String DICT_KEY_SOS = "sos";
    private static final String DEFAULT_VOICE_TEXT = "SOS报警已发送";
    
    /**
     * 判断是否可以处理该消息
     * 只有当报警值恰好等于8（纯SOS报警）时才处理
     * 
     * @param messageData TCP消息数据
     * @return true表示仅包含SOS报警
     */
    public boolean canProcess(TcpMessageData messageData) {
        if (messageData == null || messageData.getAlarmValue() == null) {
            return false;
        }
        
        Integer alarmValue = messageData.getAlarmValue();
        // 判断是否仅包含SOS报警（报警值必须恰好等于8）
        boolean isOnlySosAlarm = alarmValue == SOS_ALARM_BIT_VALUE;
        
        if (isOnlySosAlarm) {
            logger.info("检测到纯SOS报警, 设备ID: {}, 报警值: {} (二进制: {})", 
                messageData.getDeviceId(), 
                alarmValue,
                Integer.toBinaryString(alarmValue));
        }
        
        return isOnlySosAlarm;
    }
    
    /**
     * 处理SOS报警消息
     * 
     * @param messageData TCP消息数据
     * @param ctx 用于发送响应的通道上下文
     */
    public void process(TcpMessageData messageData, ChannelHandlerContext ctx) {
        String deviceId = messageData.getDeviceId();
        
        try {
            logger.info("开始处理SOS报警, 设备ID: {}, 报警值: {}", 
                deviceId, messageData.getAlarmValue());
            
            // 1. 保存原始TCP消息到bt_signal_info字段
            String btSignalInfo = messageData.getRawMessage() != null ? messageData.getRawMessage() : "";
            
            // 2. 获取位置和区域信息
            String location = tcpBeaconLocationService.getLocationFromBeacons(messageData);
            String areaName = tcpBeaconLocationService.getAreaNameFromBeacons(messageData);
            
            // 如果当前消息无法获取位置，从历史消息中查询
            if (StringUtils.isBlank(location)) {
                logger.info("当前SOS消息无法获取位置，尝试从历史消息中获取: deviceId={}", deviceId);
                
                Map<String, String> historyLocation = getLocationAndAreaFromHistoryMessages(deviceId, messageData);
                if (!historyLocation.isEmpty()) {
                    location = historyLocation.get("location");
                    String historyAreaName = historyLocation.get("areaName");
                    
                    // 如果当前没有areaName，使用历史的areaName
                    if (StringUtils.isBlank(areaName) && StringUtils.isNotBlank(historyAreaName)) {
                        areaName = historyAreaName;
                    }
                    
                    logger.info("从历史消息成功获取位置信息: deviceId={}, location={}, areaName={}", 
                        deviceId, location, areaName);
                }
            }
            
            logger.info("SOS报警位置信息: deviceId={}, location={}, areaName={}", 
                deviceId, location, areaName);
            
            // 3. 保存SOS数据到TDengine
            saveSosDataToTDengine(deviceId, messageData, btSignalInfo);
            
            // 4. 保存到swm_warning_management表（type=7表示一键SOS报警）
            saveWarningToSwmWarningManagement(deviceId, "7", btSignalInfo, location, areaName);
            
            // 5. 从字典获取语音提示内容
            String voiceText = DictUtils.getDictLabel(DICT_TYPE, DICT_KEY_SOS, DEFAULT_VOICE_TEXT);
            logger.info("获取语音提示内容 - 字典类型: {}, 字典键: {}, 获取值: {}",
                DICT_TYPE, DICT_KEY_SOS, voiceText);
            
            // 6. 构建并发送语音指令
            if (!messageData.isZTDevice()){
                String voiceCommand = buildVoiceCommand(voiceText);
                sendVoiceCommand(deviceId, voiceCommand);
                logger.info("SOS报警处理完成, 设备ID: {}, 语音内容: {}, 语音指令: {}",
                        deviceId, voiceText, voiceCommand.trim());
                //中泰设备语音提示
            }else if (messageData.isZTDevice()){
                soSAlarmHandler.sendVoiceCommand(deviceId, voiceText);
            }

            
        } catch (Exception e) {
            logger.error("处理SOS报警时发生异常, 设备ID: {}", deviceId, e);
        }
    }
    
    /**
     * 构建语音指令
     * 格式: $xx,PS,0,TTSD,zzzz,#\n
     * 
     * @param text 语音内容
     * @return 完整的语音指令
     */
    private String buildVoiceCommand(String text) {
        // 构建需要计算长度的部分: PS,0,TTSD,[语音内容],#
        String dataSection = "PS,0,TTSD," + text + ",#";
        
        // 计算UTF-8字节长度
        int byteLength = dataSection.getBytes(StandardCharsets.UTF_8).length;
        
        // 转换为十六进制字符串（大写）
        String hexLength = Integer.toHexString(byteLength).toUpperCase();
        
        // 构建完整指令
        String command = "$" + hexLength + "," + dataSection + "\n";
        
        logger.info("构建语音指令: 文本='{}', UTF-8字节长度={}, 十六进制长度={}",
            text, byteLength, hexLength);
        
        return command;
    }
    
    /**
     * 发送语音指令给设备
     *
     * @param deviceId 设备ID
     * @param voiceCommand 语音指令
     */
    private void sendVoiceCommand(String deviceId, String voiceCommand) {
        com.jeesite.modules.tcp.session.SessionManager sessionManager =
            com.jeesite.modules.tcp.server.NettyTcpServer.getInstance().getSessionManager();
        boolean success = sessionManager.sendMessageToDevice(deviceId, voiceCommand);
        if (success) {
            logger.debug("语音指令发送成功, deviceId: {}", deviceId);
        } else {
            logger.warn("语音指令发送失败, deviceId: {}", deviceId);
        }
    }
    
    /**
     * 构建蓝牙信标信息的JSON格式
     * 将TCP格式的信标数据转换为WebSocket兼容的JSON格式
     * 
     * @param messageData TCP消息数据
     * @return JSON格式的bt_signal_info字符串
     */
    private String buildBtSignalInfo(TcpMessageData messageData) {
        List<Map<String, Object>> bluetoothBeacons = messageData.getBluetoothBeacons();
        if (bluetoothBeacons == null || bluetoothBeacons.isEmpty()) {
            return "";
        }
        
        StringBuilder json = new StringBuilder("{\"dataList\":[");
        boolean first = true;
        
        for (Map<String, Object> beacon : bluetoothBeacons) {
            String mac = (String) beacon.get("MAC");
            String rssi = (String) beacon.get("RSSI");
            
            if (StringUtils.isNotBlank(mac) && StringUtils.isNotBlank(rssi)) {
                if (!first) {
                    json.append(",");
                }
                first = false;
                
                // 将MAC地址添加冒号（从80ECCCD0BCF6转为80:EC:CC:D0:BC:F6）
                String formattedMac = tcpBeaconLocationService.formatMacWithColon(mac);
                
                json.append("{");
                json.append("\"mac\":\"").append(formattedMac).append("\",");
                json.append("\"rssi\":").append(rssi).append(",");
                json.append("\"name\":\"\"");
                json.append("}");
            }
        }
        
        json.append("]}");
        
        logger.debug("构建的bt_signal_info: {}", json.toString());
        return json.toString();
    }
    
    /**
     * 保存SOS数据到TDengine
     * 
     * @param deviceId 设备ID
     * @param messageData TCP消息数据
     * @param btSignalInfo 蓝牙信标信息JSON
     */
    private void saveSosDataToTDengine(String deviceId, TcpMessageData messageData, String btSignalInfo) {
        try {
            // 构建SOS数据Map
            Map<String, Object> sosDataMap = new HashMap<>();
            sosDataMap.put("device_id", deviceId);
            sosDataMap.put("type", "7"); // 一键SOS报警
            sosDataMap.put("act", "ca_sos"); // 保持与WebSocket一致
            
            // 添加sos_time字段（使用扫描时间戳）
            if (messageData.getScanTimestamp() != null) {
                sosDataMap.put("sos_time", messageData.getScanTimestamp());
            }
            
            // 添加蓝牙信标信息
            if (StringUtils.isNotBlank(btSignalInfo)) {
                sosDataMap.put("bt_signal_info", btSignalInfo);
            }
            
            // 调用服务保存数据
            R<JSONObject> result = helmetSosTdEngineService.saveHelmetSosData(deviceId, sosDataMap);
            
            if (result.getCode() != R.SUCCESS) {
                logger.error("保存SOS数据到TDengine失败, deviceId: {}, 错误: {}", 
                    deviceId, result.getMsg());
            } else {
                logger.info("成功保存SOS数据到TDengine, deviceId: {}", deviceId);
            }
            
        } catch (Exception e) {
            logger.error("保存SOS数据到TDengine异常, deviceId: {}", deviceId, e);
        }
    }
    
    /**
     * 保存预警信息到swm_warning_management表
     * 
     * @param deviceId 设备ID
     * @param type 报警类型（7=一键SOS报警）
     * @param btSignalInfo 蓝牙信标信息
     * @param location 位置信息
     * @param areaName 区域名称
     */
    private void saveWarningToSwmWarningManagement(String deviceId, String type, 
            String btSignalInfo, String location, String areaName) {
        try {
            // 查询设备绑定的身份证（与WebSocket保持一致）
//            String sql = "SELECT assigned_person FROM swm_helmet_device WHERE device_id = ? AND status = '0'";
//            List<String> results = jdbcTemplate.queryForList(sql, String.class, deviceId);
//            String idCard = !results.isEmpty() ? results.get(0) : null;
            String cardId = (String) redisService.hget(SwmRedisKeyConstants.GlobalKey.DEVICE_PERSON_MAP, String.valueOf(deviceId));
            String idCard = !cardId.isEmpty() ? cardId : null;
            
            // 构建预警信息
//            String warningContent = "一键SOS报警";
//            String triggerReason = "工人手动触发SOS求救信号";
//            String alarmRecord = "安全帽一键SOS报警记录";
            String warningType = "1"; // 主动报警
            String warningContent = DictUtils.getDictLabel("warning_content_enum", "应急呼叫", "应急呼叫");
            String triggerReason = "工人手动触发"+warningContent;
            String alarmRecord = "安全帽"+triggerReason+"记录";

            // 调用服务保存预警信息
            R<JSONObject> result = swmWarningManagementService.saveCustomWarningToSwmWarningManagement(
                    deviceId, 
                    idCard, 
                    warningContent, 
                    triggerReason, 
                    alarmRecord, 
                    warningType, 
                    type,           // 原始type值=7
                    false,          // locationAdded
                    null,           // hazardCategory
                    btSignalInfo,   // bt_signal_info
                    location,       // location
                    areaName        // areaName
            );
            
            if (result.getCode() != R.SUCCESS) {
                logger.error("保存预警信息到swm_warning_management失败, deviceId: {}, 错误: {}", 
                    deviceId, result.getMsg());
            } else {
                logger.info("成功保存预警信息到swm_warning_management, deviceId: {}, type: {}", 
                    deviceId, type);
                // 报警灯通知会在saveCustomWarningToSwmWarningManagement内部自动触发
            }
            
        } catch (Exception e) {
            logger.error("保存预警信息异常, deviceId: {}", deviceId, e);
        }
    }
    
    /**
     * 直接从原始报文判断是否仅包含SOS报警
     * 这个方法可以独立使用，不依赖TcpMessageData对象
     * 
     * @param rawMessage 原始TCP报文
     * @return true表示仅包含SOS报警（报警值=8）
     */
    public boolean hasSosAlarmFromRawMessage(String rawMessage) {
        try {
            // 验证基本格式
            if (rawMessage == null || !rawMessage.startsWith("$") || !rawMessage.endsWith("#")) {
                return false;
            }
            
            // 检查是否为S类型报文
            String[] parts = rawMessage.substring(1, rawMessage.length() - 1).split(",");
            if (parts.length < 2 || !"S".equals(parts[1])) {
                return false;
            }
            
            // 从S开始算作第1个字段，报警值在第14个位置
            // 所以在整个数组中是第14个元素（索引13）
            if (parts.length < 14) {
                logger.debug("报文字段不足，无法提取报警值: {}", rawMessage);
                return false;
            }
            
            // 提取并解析报警值
            String alarmField = parts[13].trim();
            int alarmValue;
            
            // 判断是否为16进制格式
            if (alarmField.matches("[0-9A-Fa-f]+") && alarmField.length() <= 4) {
                // 16进制格式（如 A0 或 8）
                alarmValue = Integer.parseInt(alarmField, 16);
            } else {
                // 10进制格式
                alarmValue = Integer.parseInt(alarmField);
            }
            
            // 判断是否仅包含SOS报警
            boolean isOnlySos = alarmValue == SOS_ALARM_BIT_VALUE;
            
            if (isOnlySos) {
                logger.info("从原始报文检测到纯SOS报警, 报警值: {} (二进制: {})", 
                    alarmValue, Integer.toBinaryString(alarmValue));
            }
            
            return isOnlySos;
            
        } catch (NumberFormatException e) {
            logger.warn("报警值字段格式错误: {}", e.getMessage());
            return false;
        } catch (Exception e) {
            logger.error("解析原始报文时发生异常: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * 从历史消息中获取位置和区域信息
     * 当前消息无法获取位置时，回退查询最近20条历史消息
     * 
     * @param deviceId 设备ID
     * @param currentMessageData 当前消息数据
     * @return 包含location和areaName的Map，未找到返回空Map
     */
    private Map<String, String> getLocationAndAreaFromHistoryMessages(String deviceId, TcpMessageData currentMessageData) {
        try {
            // 查询最近20条消息
            String sql = String.format(
                "SELECT message_content, time FROM %s.raw_message_log " +
                "WHERE device_id = '%s' " +
                "ORDER BY time DESC " +
                "LIMIT 20", 
                dbname, deviceId
            );
            
            R<JSONObject> result = tdEngineService.executeTDengineSQLByDeviceId(sql,deviceId);
            if (result.getCode() != R.SUCCESS || result.getData() == null) {
                logger.warn("查询历史消息失败: deviceId={}, 错误={}", deviceId, result.getMsg());
                return new HashMap<>();
            }
            
            JSONObject data = result.getData();
            Object dataArray = data.get("data");
            
            // 转换为List，处理TDEngine返回的二维数组格式
            List<Map<String, Object>> results = new ArrayList<>();
            if (dataArray instanceof List) {
                for (Object item : (List<?>) dataArray) {
                    if (item instanceof List) {
                        List<?> row = (List<?>) item;
                        if (row.size() >= 2) {
                            Map<String, Object> rowMap = new HashMap<>();
                            rowMap.put("message_content", row.get(0));  // 第一列是message_content
                            rowMap.put("time", row.get(1));             // 第二列是time
                            results.add(rowMap);
                        }
                    }
                }
            }
            
            if (results == null || results.isEmpty()) {
                logger.debug("未找到历史消息: deviceId={}", deviceId);
                return new HashMap<>();
            }
            
            int validSMessageCount = 0;
            int processedCount = 0;
            
            for (Map<String, Object> row : results) {
                processedCount++;
                
                try {  // 单条记录的异常处理
                    String rawMessage = (String) row.get("message_content");
                    
                    // 1. 跳过与当前消息相同的记录
                    if (rawMessage.equals(currentMessageData.getRawMessage())) {
                        logger.debug("跳过与当前消息相同的历史记录");
                        continue;
                    }
                    
                    // 2. 快速检查是否为S类型消息
                    if (!isStatusMessage(rawMessage)) {
                        logger.debug("跳过非S类型消息: {}", 
                            rawMessage.length() > 50 ? rawMessage.substring(0, 50) + "..." : rawMessage);
                        continue;
                    }
                    
                    // 3. 解析S类型消息（可能抛出异常）
                    TcpMessageData historyData = null;
                    try {
                        historyData = tcpMessageParser.parseMessage(rawMessage);
                    } catch (Exception parseEx) {
                        logger.warn("解析历史消息失败，跳过此条: deviceId={}, 消息={}, 异常={}", 
                            deviceId, 
                            rawMessage.length() > 100 ? rawMessage.substring(0, 100) + "..." : rawMessage,
                            parseEx.getMessage());
                        continue;  // 继续处理下一条
                    }
                    
                    if (historyData == null || historyData.getBluetoothBeacons() == null || 
                        historyData.getBluetoothBeacons().isEmpty()) {
                        logger.debug("消息无有效蓝牙信标数据，跳过");
                        continue;
                    }
                    
                    validSMessageCount++;
                    
                    // 4. 尝试获取位置和区域信息（可能抛出异常）
                    String location = null;
                    String areaName = null;
                    try {
                        location = tcpBeaconLocationService.getLocationFromBeacons(historyData);
                        if (StringUtils.isNotBlank(location)) {
                            // 同时获取区域信息
                            areaName = tcpBeaconLocationService.getAreaNameFromBeacons(historyData);
                        }
                    } catch (Exception locEx) {
                        logger.warn("从历史消息获取位置时异常，跳过此条: deviceId={}, 第{}条S消息, 异常={}", 
                            deviceId, validSMessageCount, locEx.getMessage());
                        continue;  // 继续处理下一条
                    }
                    
                    if (StringUtils.isNotBlank(location)) {
                        Map<String, String> locationResult = new HashMap<>();
                        locationResult.put("location", location);
                        locationResult.put("areaName", areaName != null ? areaName : "");
                        
                        logger.info("从历史消息获取到位置: deviceId={}, location={}, areaName={}, " +
                            "第{}条有效S消息(总处理{}条)", 
                            deviceId, location, areaName, validSMessageCount, processedCount);
                        return locationResult;
                    }
                    
                    // 5. 处理10条有效S消息后停止
                    if (validSMessageCount >= 10) {
                        logger.info("已处理10条有效S消息，均未找到位置，停止查询");
                        break;
                    }
                    
                } catch (Exception rowEx) {
                    // 单条记录的未预期异常
                    logger.error("处理历史消息记录时发生异常，跳过此条继续: deviceId={}, " +
                        "第{}条记录, 异常信息={}", 
                        deviceId, processedCount, rowEx.getMessage(), rowEx);
                    // 继续处理下一条，不影响整体流程
                }
            }
            
            logger.warn("历史消息查询完成: deviceId={}, 总处理{}条, 有效S消息{}条, 未找到位置", 
                deviceId, processedCount, validSMessageCount);
            
        } catch (Exception e) {
            // 整体查询异常（如SQL执行失败）
            logger.error("查询历史消息位置失败: deviceId={}, 异常={}", deviceId, e.getMessage(), e);
        }
        
        return new HashMap<>();
    }
    
    /**
     * 快速判断是否为S类型消息
     * 避免对所有消息进行完整解析
     * 
     * @param rawMessage 原始TCP消息
     * @return true表示是S类型消息
     */
    private boolean isStatusMessage(String rawMessage) {
        try {
            if (StringUtils.isBlank(rawMessage)) {
                return false;
            }
            
            if (!rawMessage.startsWith("$") || !rawMessage.endsWith("#")) {
                return false;
            }
            
            // 快速检查第二个字段是否为S
            // 格式: $xxx,S,... 或 $xx,S,...
            int firstComma = rawMessage.indexOf(',');
            if (firstComma > 0 && firstComma + 2 < rawMessage.length()) {
                int secondComma = rawMessage.indexOf(',', firstComma + 1);
                if (secondComma > firstComma) {
                    String commandType = rawMessage.substring(firstComma + 1, secondComma).trim();
                    return "S".equals(commandType);
                }
            }
        } catch (Exception e) {
            logger.debug("判断消息类型时异常: {}", e.getMessage());
        }
        return false;
    }
}