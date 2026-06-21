package cn.iocoder.yudao.module.iot.tcp.processor;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.iocoder.yudao.module.iot.cache.DeviceCorpMappingCache;
import cn.iocoder.yudao.module.iot.cache.service.RedisService;
import cn.iocoder.yudao.module.swm.api.constant.SwmRedisKeyConstants;
import cn.iocoder.yudao.module.iot.mqtt.constant.MqttConstants;
import cn.iocoder.yudao.module.iot.mqtt.handler.zhongtai.DeviceSoSAlarmHandler;
import cn.iocoder.yudao.module.swm.service.TDengineService;
import cn.iocoder.yudao.module.iot.tcp.model.TcpMessageData;
import cn.iocoder.yudao.module.iot.service.HelmetSosTdEngineService;
import cn.iocoder.yudao.module.iot.service.SwmWarningManagementService;
import cn.iocoder.yudao.module.iot.tcp.service.TcpBeaconLocationService;
import cn.iocoder.yudao.module.iot.util.R;
import cn.iocoder.yudao.module.swm.util.DictUtils;
import cn.hutool.json.JSONObject;
import io.netty.channel.ChannelHandlerContext;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Lazy;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 脱帽报警TCP消息处理器
 * 处理报警值中包含脱帽报警位（Bit.7=1）的设备数据，并下发语音提醒
 * 与SOS报警不同，只要报警值包含脱帽位就会处理，不要求纯脱帽报警
 *
 * @author Shawn
 * @date 2025-01-28
 */
@Component
public class HelmetOffTcpProcessor {

    private static final Logger logger = LoggerFactory.getLogger(HelmetOffTcpProcessor.class);

    @Resource
    private HelmetSosTdEngineService helmetSosTdEngineService;

    @Resource
    private SwmWarningManagementService swmWarningManagementService;

    @Resource
    private JdbcTemplate jdbcTemplate;

    @Resource
    private TcpBeaconLocationService tcpBeaconLocationService;
    @Resource
    private RedisService redisService;
    @Resource
    private DeviceCorpMappingCache deviceCorpMappingCache;
    @Resource
    private TDengineService tdengineService;

    @Resource
    @Lazy
    private DeviceSoSAlarmHandler soSAlarmHandler;

    // 脱帽报警在第7位（从0开始），对应的值是2^7=128
    private static final int HELMET_OFF_ALARM_BIT_MASK = 128;

    // 字典配置常量
    private static final String DICT_TYPE = "klt_xiafa_yy_cs";
    private static final String DICT_KEY_HELMET_OFF = "helmet_off";
    private static final String DEFAULT_VOICE_TEXT = "脱帽报警已触发";

    /**
     * 判断是否可以处理该消息
     * 只要报警值包含脱帽报警位（Bit.7=1）就处理
     *
     * @param messageData TCP消息数据
     * @return true表示包含脱帽报警
     */
    public boolean canProcess(TcpMessageData messageData) {
        if (messageData == null || messageData.getAlarmValue() == null) {
            return false;
        }

        Integer alarmValue = messageData.getAlarmValue();
        // 判断是否包含脱帽报警（Bit.7=1）
        boolean hasHelmetOffAlarm = (alarmValue & HELMET_OFF_ALARM_BIT_MASK) != 0;

        if (hasHelmetOffAlarm) {
            logger.info("检测到脱帽报警, 设备ID: {}, 报警值: {} (二进制: {})",
                messageData.getDeviceId(),
                alarmValue,
                Integer.toBinaryString(alarmValue));
        }

        return hasHelmetOffAlarm;
    }

    /**
     * 处理脱帽报警消息
     *
     * @param messageData TCP消息数据
     * @param ctx 用于发送响应的通道上下文
     */
    public void process(TcpMessageData messageData, ChannelHandlerContext ctx) {
        String deviceId = messageData.getDeviceId();
        String corpCode = deviceCorpMappingCache.getCorpCode(deviceId);

        //1.查询设备绑定的身份证，用来拼接区域表  area_fence_data_设备ID_身份证id
        String idCard = (String) redisService.hget(SwmRedisKeyConstants.GlobalKey.DEVICE_PERSON_MAP, String.valueOf(deviceId));
        if (StringUtils.isNotBlank(idCard)) {
            // 判断帽子在近2分钟是否触发过静默报警，触发过则不报警
            boolean helmetOffAlarm = isHelmetOffAlarm(idCard, deviceId, corpCode);
            if (helmetOffAlarm) {
                logger.info("设备ID={}已触发过脱帽报警，跳过处理", deviceId);
                return;
            }
        }


        try {
            logger.info("开始处理脱帽报警, 设备ID: {}, 报警值: {}",
                deviceId, messageData.getAlarmValue());

            // 1. 保存原始TCP消息到bt_signal_info字段
            String btSignalInfo = messageData.getRawMessage() != null ? messageData.getRawMessage() : "";

            // 2. 获取位置和区域信息
            String location = tcpBeaconLocationService.getLocationFromBeacons(messageData);
//            String areaName = tcpBeaconLocationService.getAreaNameFromBeacons(messageData);
            //修改所属区域，只要信号强度>-80的信标，拿到所属区域

            Map<String,Object> areaMap = tcpBeaconLocationService.getAreaNameFromBeaconsFilter(messageData);
            String areaName = "未知区域";
            String areaid = "未知区域";
            if (ObjectUtils.isNotEmpty(areaMap)){
                // 用 String.valueOf() 替代强转，兼容任意类型的值
                if (areaMap.containsKey("areaName") && areaMap.get("areaName") != null) {
                    areaName = String.valueOf(areaMap.get("areaName"));
                }
                if (areaMap.containsKey("id") && areaMap.get("id") != null) {
                    areaid = String.valueOf(areaMap.get("id"));
                }
            }

            //判断是否属于生产区域，属于则发起语音提醒，不属于则跳过
            Object object = redisService.get(corpCode + SwmRedisKeyConstants.SwmKey.AREA_CACHE);
            if (object != null) {
                List<String> areaIds = (List<String>) object;
                if (!areaIds.contains(areaid)){
                    return;
                }
            }

            logger.info("脱帽报警位置信息: deviceId={}, location={}, areaName={}",
                deviceId, location, areaName);

            // 3. 保存脱帽报警数据到TDengine
            saveHelmetOffDataToTDengine(deviceId, messageData, btSignalInfo);

            // 4. 保存到swm_warning_management表（type=1表示脱帽报警）
            saveWarningToSwmWarningManagement(deviceId, "1", btSignalInfo, location, areaName,idCard);

            // 5. 从字典获取语音提示内容
            String voiceText = DictUtils.getDictLabel(DICT_TYPE, DICT_KEY_HELMET_OFF, DEFAULT_VOICE_TEXT);
            logger.info("获取语音提示内容 - 字典类型: {}, 字典键: {}, 获取值: {}",
                DICT_TYPE, DICT_KEY_HELMET_OFF, voiceText);

            // 6. 构建并发送语音指令
            if (!messageData.isZTDevice()){
                String voiceCommand = buildVoiceCommand(voiceText);
                sendVoiceCommand(deviceId, voiceCommand);
                logger.info("脱帽报警处理完成, 设备ID: {}, 语音内容: {}, 语音指令: {}",
                        deviceId, voiceText, voiceCommand.trim());
            }else {
                // 发送语音
                JSONObject result = soSAlarmHandler.sendVoiceCommand(deviceId, voiceText);
                //发送成功写进缓存
                if(result.getBool("success")){
                    logger.info("脱帽报警处理完成, 设备ID: {}, 语音内容: {}", deviceId, voiceText);
                }
            }


        } catch (Exception e) {
            logger.error("处理脱帽报警时发生异常, 设备ID: {}", deviceId, e);
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
        cn.iocoder.yudao.module.iot.tcp.session.SessionManager sessionManager =
            cn.iocoder.yudao.module.iot.tcp.server.NettyTcpServer.getInstance().getSessionManager();
        boolean success = sessionManager.sendMessageToDevice(deviceId, voiceCommand);
        if (success) {
            logger.debug("语音指令发送成功, deviceId: {}", deviceId);
        } else {
            logger.warn("语音指令发送失败, deviceId: {}", deviceId);
        }
    }

    /**
     * 保存脱帽报警数据到TDengine
     *
     * @param deviceId 设备ID
     * @param messageData TCP消息数据
     * @param btSignalInfo 蓝牙信标信息JSON
     */
    private void saveHelmetOffDataToTDengine(String deviceId, TcpMessageData messageData, String btSignalInfo) {
        try {
            // 构建脱帽报警数据Map
            Map<String, Object> helmetOffDataMap = new HashMap<>();
            helmetOffDataMap.put("device_id", deviceId);
            helmetOffDataMap.put("type", "1"); // 脱帽报警
            helmetOffDataMap.put("act", "ca_sos"); // 保持与WebSocket一致

            // 添加sos_time字段（使用扫描时间戳）
            if (messageData.getScanTimestamp() != null) {
                helmetOffDataMap.put("sos_time", messageData.getScanTimestamp());
            }

            // 添加蓝牙信标信息
            if (StringUtils.isNotBlank(btSignalInfo)) {
                helmetOffDataMap.put("bt_signal_info", btSignalInfo);
            }

            // 调用服务保存数据
            R<JSONObject> result = helmetSosTdEngineService.saveHelmetSosData(deviceId, helmetOffDataMap);

            if (result.getCode() != R.SUCCESS) {
                logger.error("保存脱帽报警数据到TDengine失败, deviceId: {}, 错误: {}",
                    deviceId, result.getMsg());
            } else {
                logger.info("成功保存脱帽报警数据到TDengine, deviceId: {}", deviceId);
            }

        } catch (Exception e) {
            logger.error("保存脱帽报警数据到TDengine异常, deviceId: {}", deviceId, e);
        }
    }

    /**
     * 保存预警信息到swm_warning_management表
     *
     * @param deviceId 设备ID
     * @param type 报警类型（1=脱帽报警）
     * @param btSignalInfo 蓝牙信标信息
     * @param location 位置信息
     * @param areaName 区域名称
     */
    private void saveWarningToSwmWarningManagement(String deviceId, String type,
            String btSignalInfo, String location, String areaName, String idCard) {
        try {
            // 查询设备绑定的身份证（与WebSocket保持一致）
//            String sql = "SELECT assigned_person FROM swm_helmet_device WHERE device_id = ? AND status = '0'";
//            List<String> results = jdbcTemplate.queryForList(sql, String.class, deviceId);
//            String idCard = !results.isEmpty() ? results.get(0) : null;

            // 构建预警信息
//            String warningContent = "脱帽报警";
            String triggerReason = "检测到工人未正确佩戴安全帽";
            String alarmRecord = "安全帽脱帽报警记录";
            String warningType = "2"; // 被动报警
            String warningContent = DictUtils.getDictLabel("warning_content_enum", "脱帽报警", "脱帽报警");

            // 调用服务保存预警信息
            R<JSONObject> result = swmWarningManagementService.saveCustomWarningToSwmWarningManagement(
                    deviceId,
                    idCard,
                    warningContent,
                    triggerReason,
                    alarmRecord,
                    warningType,
                    type,           // 原始type值=1
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
     * 直接从原始报文判断是否包含脱帽报警
     * 这个方法可以独立使用，不依赖TcpMessageData对象
     *
     * @param rawMessage 原始TCP报文
     * @return true表示包含脱帽报警（报警值Bit.7=1）
     */
    public boolean hasHelmetOffAlarmFromRawMessage(String rawMessage) {
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
                // 16进制格式（如 A0）
                alarmValue = Integer.parseInt(alarmField, 16);
            } else {
                // 10进制格式
                alarmValue = Integer.parseInt(alarmField);
            }

            // 判断是否包含脱帽报警
            boolean hasHelmetOff = (alarmValue & HELMET_OFF_ALARM_BIT_MASK) != 0;

            if (hasHelmetOff) {
                logger.info("从原始报文检测到脱帽报警, 报警值: 0x{} (二进制: {})",
                    alarmField, Integer.toBinaryString(alarmValue));
            }

            return hasHelmetOff;

        } catch (NumberFormatException e) {
            logger.warn("报警值字段格式错误: {}", e.getMessage());
            return false;
        } catch (Exception e) {
            logger.error("解析原始报文时发生异常: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 新增逻辑：脱帽报警触发前，判断帽子在近2分钟是否触发过静默报警，触发过则不报警
     */
    private boolean isHelmetOffAlarm(String idCard,String deviceId,String corpCode) {
        //拼接表名：swm_warning_management_2015869173_44132319880218851x
        String dbName = deviceCorpMappingCache.getDbName(deviceId);
        String tableName = "swm_warning_management_" + deviceId + "_" + idCard;
        Date date = new Date();
        String startTimeStr = DateUtil.format(DateUtil.offsetMinute(date, -2), DatePattern.NORM_DATETIME_PATTERN);
        String endTimeStr = DateUtil.format(date, DatePattern.NORM_DATETIME_PATTERN);
        String dictValue = DictUtils.getDictValue("warning_content_enum", "长时间静止报警", "长时间静止报警");
        String sql = "SELECT count(1) FROM " + dbName + "." + tableName + " WHERE warning_content = '" + dictValue + "' AND create_date >= '" + startTimeStr + "' AND create_date <= '" + endTimeStr + "'";
        Long count = tdengineService.selectCount(sql);
        if (count > 0) {
            return true;
        }
        return false;

    }
}
