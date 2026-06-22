package cn.iocoder.yudao.module.iot.mqtt.handler.weigou;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.iocoder.yudao.module.iot.cache.DeviceCorpMappingCache;
import cn.iocoder.yudao.module.iot.enums.AlarmConfigEnum;
import cn.iocoder.yudao.module.iot.enums.MqttSendPositionParamEnum;
import cn.iocoder.yudao.module.iot.mqtt.handler.MqttBusinessHandler;
import cn.iocoder.yudao.module.iot.service.HelmetSosTdEngineService;
import cn.iocoder.yudao.module.iot.service.SwmWarningManagementService;
import cn.iocoder.yudao.module.iot.service.TcpDeviceCommandLogService;
import cn.iocoder.yudao.module.iot.util.DictUtils;
import cn.iocoder.yudao.module.iot.util.R;
import lombok.Data;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.annotation.Resource;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * 电子围栏告警数据
 * 处理电子围栏告警MQTT消息
 *
 * @author fangxiaolong
 * @date 2026-02-06
 */
@Component
@ConditionalOnProperty(prefix = "iot.mqtt", name = "enabled", havingValue = "true")
public class FenceAlarmMessageHandler implements MqttBusinessHandler {

    private static final Logger logger = LoggerFactory.getLogger(FenceAlarmMessageHandler.class);

    private static final String LOG_PREFIX = "【MQTT消息-电子围栏告警】- ";

    private static final String DICT_TYPE = "klt_xiafa_yy_cs";
    private static final String DEFAULT_VOICE_TEXT = "电子围栏报警已发送";

    @Resource
    private SwmWarningManagementService swmWarningManagementService; // 预警信息保存服务

    @Resource
    private TcpDeviceCommandLogService tcpDeviceCommandLogService; // 指令日志保存服务

    @Resource
    private HelmetSosTdEngineService helmetSosTdEngineService;

    @Resource
    private DeviceCorpMappingCache deviceCorpMappingCache; // 设备企业映射缓存

    @Resource
    private JdbcTemplate jdbcTemplate; // JDBC 模板

    @Override
    public boolean canHandle(String topic, MqttMessage message) {
        try {
            // 检查主题是否匹配
            if (!topic.contains(MqttSendPositionParamEnum.PARAM_ENUM_ALARM.name())) {
                return false;
            }
            String payload = new String(message.getPayload(), StandardCharsets.UTF_8);

            logger.info("{}接收到电子围栏告警数据，payload={}", LOG_PREFIX, payload);

            return true;

        } catch (Exception e) {
            logger.debug("{}接收到电子围栏告警数据时发生异常：{}", LOG_PREFIX, e.getMessage());
            return false;
        }
    }

    /**
     * 处理 MQTT 告警消息
     */
    public void handle(String topic, MqttMessage message) throws Exception {
        long startTime = System.currentTimeMillis();
        String payload = null;
        MqttAlarmDTO alarmDTO = null;

        try {
            // 1. 解析 MQTT 消息体
            payload = new String(message.getPayload(), StandardCharsets.UTF_8);
            logger.info("{}开始处理 MQTT 告警消息，topic={}, payload={}", LOG_PREFIX, topic, payload);

            // 2. 转换为告警 DTO
            alarmDTO = JSONUtil.toBean(payload, MqttAlarmDTO.class);
            if (alarmDTO == null || StrUtil.isBlank(alarmDTO.getDeviceId())) {
                logger.error("{}MQTT 告警消息解析失败或设备 ID 为空，payload={}", LOG_PREFIX, payload);
                return;
            }

            // 3. 判断是否需要进行告警
            if (!isAlarm(alarmDTO.getAlarmType(), alarmDTO.getDeviceId())) {
                logger.info("{}设备未启用告警功能，跳过处理，deviceId={}, alarmType={}", LOG_PREFIX,
                        alarmDTO.getDeviceId(), alarmDTO.getAlarmType());
                return;
            }

            // 4. 告警类型枚举转换
            String alarmKey = getAlarmKeyCode(alarmDTO.getAlarmType());
            if (StrUtil.isBlank(alarmKey)) {
                logger.error("{}未知的告警类型，无法判断, alarmType={}", LOG_PREFIX, alarmDTO.getAlarmType());
                return;
            }
            alarmDTO.setAlarmType(alarmKey);
            // 5. 处理告警
            processAlarm(alarmDTO);

            logger.info("{}MQTT 告警消息处理完成，deviceId={}, alarmType={}, 耗时={}ms", LOG_PREFIX,
                    alarmDTO.getDeviceId(), alarmDTO.getAlarmType(), System.currentTimeMillis() - startTime);


        } catch (Exception e) {
            logger.error("{}处理 MQTT 告警消息异常，topic={}, payload={}, error={}", LOG_PREFIX,
                    topic, payload, e.getMessage(), e);
            if (alarmDTO != null) {
                saveErrorLog(alarmDTO, e.getMessage());
            }
            throw e;
        }
    }

    /**
     * 核心告警处理逻辑
     */
    private void processAlarm(MqttAlarmDTO alarmDTO) {
        String deviceId = alarmDTO.getDeviceId();
        String alarmType = alarmDTO.getAlarmType();

        try {
            logger.info("{}开始处理告警，deviceId={}, alarmType={}, 告警类型描述={}", LOG_PREFIX,
                    deviceId, alarmType, getAlarmTypeDesc(alarmType));

            // 1. 构建位置信息
            String location = buildLocation(alarmDTO);
            String areaName = getAreaNameByFloorId(alarmDTO.getFloorId());

            // 2. 保存 MQTT 告警数据到 TDengine
            saveElectronicFenceDataToTDengine(deviceId,alarmType);

            // 3. 保存到预警管理表
            saveWarningToSwmWarningManagement(alarmDTO, location, areaName);

            // 4. 针对 SOS 告警发送语音指令
            sendVoiceCommandForFence(deviceId, alarmType);

        } catch (Exception e) {
            logger.error("{}处理告警异常，deviceId={}, alarmType={}, error={}", LOG_PREFIX,
                    deviceId, alarmType, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 保存电子围栏告警数据到 TDengine
     *
     * @param deviceId  设备 ID
     * @param alarmType
     */
    private void saveElectronicFenceDataToTDengine(String deviceId, String alarmType) {
        try {
            // 构建危险源报警数据 Map
            Map<String, Object> electronicFenceData = new HashMap<>();
            electronicFenceData.put("device_id", deviceId);
            electronicFenceData.put("type", alarmType); // 危险源报警
            electronicFenceData.put("act", "ca_sos"); // 保持与 WebSocket 一致

            electronicFenceData.put("sos_time", System.currentTimeMillis());

            // 添加蓝牙信标信息
            electronicFenceData.put("bt_signal_info", "");

            // 调用服务保存数据
            R<JSONObject> result = helmetSosTdEngineService.saveHelmetSosData(deviceId, electronicFenceData);

            if (result.getCode() != R.SUCCESS) {
                logger.error("{}保存电子围栏告警数据到 TDengine 失败, deviceId: {}, 错误: {}", LOG_PREFIX,
                        deviceId, result.getMsg());
            } else {
                logger.info("{}成功电子围栏告警数据到 TDengine, deviceId: {}", LOG_PREFIX, deviceId);
            }

        } catch (Exception e) {
            logger.error("{}保存电子围栏告警数据到 TDengine 异常, deviceId: {}", LOG_PREFIX, deviceId, e);
        }
    }

    /**
     * 构建位置信息
     */
    private String buildLocation(MqttAlarmDTO alarmDTO) {
        if (alarmDTO.getLng() == null || alarmDTO.getLat() == null) {
            return "未知位置";
        }
        return String.format("经度：%s, 纬度：%s", alarmDTO.getLng(), alarmDTO.getLat());
    }

    /**
     * 根据楼层 ID 获取区域名称（需补充实现）
     */
    private String getAreaNameByFloorId(String floorId) {
        if (StrUtil.isBlank(floorId)) {
            return "";
        }
        // TODO: 补充根据 floorId 查询区域名称的逻辑
        return "未知区域";
    }

    /**
     * 保存到预警管理表
     */
    private void saveWarningToSwmWarningManagement(MqttAlarmDTO alarmDTO, String location, String areaName) {
        try {
            String warningContent = getWarningContentFromDict(alarmDTO.getAlarmType());
            String triggerReason = buildTriggerReason(alarmDTO, warningContent);
            String alarmRecord = alarmDTO.getAlarmType() + "-" + getAlarmTypeDesc(alarmDTO.getAlarmType()) + "记录";
            String warningType = "1"; // 1=主动报警
            String type = String.valueOf(alarmDTO.getAlarmType());

            R<JSONObject> result = swmWarningManagementService.saveCustomWarningToSwmWarningManagement(
                    alarmDTO.getDeviceId(),
                    alarmDTO.getIdentityCard(),
                    warningContent,
                    triggerReason,
                    alarmRecord,
                    warningType,
                    type
            );

            if (result.getCode() != R.SUCCESS) {
                logger.error("{}保存预警信息到 swm_warning_management 失败，deviceId={}, error={}", LOG_PREFIX,
                        alarmDTO.getDeviceId(), result.getMsg());
            } else {
                logger.info("{}成功保存预警信息到 swm_warning_management, deviceId={}", LOG_PREFIX, alarmDTO.getDeviceId());
            }

        } catch (Exception e) {
            logger.error("{}保存预警信息异常，deviceId={}, error={}", LOG_PREFIX,
                    alarmDTO.getDeviceId(), e.getMessage(), e);
        }
    }

    /**
     * 为电子围栏告警发送语音指令
     */
    private void sendVoiceCommandForFence(String deviceId, String alarmType) {
        try {
            String voiceText = DictUtils.getDictLabel(DICT_TYPE, alarmType, DEFAULT_VOICE_TEXT);
            logger.info("{}获取电子围栏报警 - 语音提示内容：{}", LOG_PREFIX, voiceText);

            String voiceCommand = buildVoiceCommand(voiceText);
            cn.iocoder.yudao.module.iot.tcp.session.SessionManager sessionManager =
                    cn.iocoder.yudao.module.iot.tcp.server.NettyTcpServer.getInstance().getSessionManager();
            boolean success = sessionManager.sendMessageToDevice(deviceId, voiceCommand);
            if (success) {
                logger.info("{}电子围栏报警 - 语音指令发送成功，deviceId={}", LOG_PREFIX, deviceId);
            } else {
                logger.warn("{}电子围栏报警 - 语音指令发送失败，deviceId={}", LOG_PREFIX, deviceId);
                saveCommandLog(deviceId, voiceCommand, "设备不在线或会话不活跃");
            }

        } catch (Exception e) {
            logger.error("{}发送电子围栏报警 - 语音指令异常，deviceId={}, error={}", LOG_PREFIX, deviceId, e.getMessage(), e);
            saveCommandLog(deviceId, "", "发送语音指令异常：" + e.getMessage());
        }
    }

    /**
     * 构建语音指令
     */
    private String buildVoiceCommand(String text) {
        String dataSection = "PS,0,TTSD," + text + ",#";
        int byteLength = dataSection.getBytes(StandardCharsets.UTF_8).length;
        String hexLength = Integer.toHexString(byteLength).toUpperCase();
        String command = "$" + hexLength + "," + dataSection + "\n";

        logger.info("{}构建语音指令：文本='{}', UTF-8 字节长度={}, 十六进制长度={}", LOG_PREFIX,
                text, byteLength, hexLength);

        return command;
    }

    /**
     * 保存指令日志
     */
    private void saveCommandLog(String deviceId, String sendMessage, String errorMessage) {
        try {
            tcpDeviceCommandLogService.saveTcpCommandLog(deviceId, sendMessage, errorMessage);
        } catch (Exception e) {
            logger.error("{}保存 TCP 指令日志失败，deviceId={}, error={}", LOG_PREFIX, deviceId, e.getMessage());
        }
    }

    /**
     * 构建触发原因
     */
    private String buildTriggerReason(MqttAlarmDTO alarmDTO, String warningContent) {
        String formattedTime = DateUtil.format(DateUtil.date(), "yyyy-MM-dd HH:mm:ss");
        return String.format("%s于%s在%s发生%s",
                StrUtil.isNotBlank(alarmDTO.getName()) ? alarmDTO.getName() : "未知人员",
                formattedTime,
                buildLocation(alarmDTO),
                warningContent);
    }

    /**
     * 从字典获取预警内容
     */
    private String getWarningContentFromDict(String alarmType) {
        // TODO: 补充从字典查询的逻辑
        return getAlarmTypeDesc(alarmType);
    }

    /**
     * 获取告警类型描述
     */
    private String getAlarmTypeDesc(String alarmType) {
        if (alarmType == null) {
            return "未知告警";
        }
        switch (alarmType) {
            case "0":
                return "串岗";
            case "1":
                return "离岗";
            case "2":
                return "静止";
            case "3":
                return "超员";
            case "4":
                return "缺员";
            case "5":
                return "离线";
            case "6":
                return "越界进入";
            case "7":
                return "越界离开";
            case "8":
                return "滞留";
            case "12":
                return "SOS求救";
            case "13":
                return "低电量";
            default:
                return "未知告警类型";
        }
    }

    /**
     * 保存错误日志
     */
    private void saveErrorLog(MqttAlarmDTO alarmDTO, String errorMsg) {
        logger.error("{}告警处理错误日志，deviceId={}, alarmType={}, error={}", LOG_PREFIX,
                alarmDTO.getDeviceId(), alarmDTO.getAlarmType(), errorMsg);
    }

    /**
     * 判断是否需要进行告警
     * @param alarmType 告警类型
     * @param deviceId 设备 ID
     * @return true-需要告警，false-不需要告警
     */
    private boolean isAlarm(String alarmType, String deviceId) {
        logger.info("{}开始判断是否需要告警，deviceId={}, alarmType={}", LOG_PREFIX, deviceId, alarmType);

        if (alarmType == null) {
            logger.error("{}告警类型为 null，不需要告警，deviceId={}", LOG_PREFIX, deviceId);
            return false;
        }

        // 将告警类型转换为枚举编码
        String alarmKey = getAlarmKeyCode(alarmType);
        if (StrUtil.isBlank(alarmKey)) {
            logger.error("{}未知的告警类型，无法判断，deviceId={}, alarmType={}", LOG_PREFIX, deviceId, alarmType);
            return false;
        }

        logger.info("{}告警类型转换成功，alarmKey={}, deviceId={}", LOG_PREFIX, alarmKey, deviceId);

        // 获取设备所属企业编码
        String corpCode = deviceCorpMappingCache.getCorpCode(deviceId);
        logger.info("{}获取到设备所属企业编码，corpCode={}, deviceId={}", LOG_PREFIX, corpCode, deviceId);

        if (StrUtil.isBlank(corpCode)) {
            logger.error("{}未找到设备所属企业，不允许告警，deviceId={}", LOG_PREFIX, deviceId);
            return false;
        }

        // 查询告警配置表：1-启用，0-禁用
        String sql = "select enable_alarm from swm_alarm_config where alarm_key = ? and `status` = '0' and corp_code = ?";
        logger.info("{}执行 SQL 查询告警配置：sql={}, alarmKey={}, corpCode={}", LOG_PREFIX, sql, alarmKey, corpCode);

        try {
            java.util.List<String> results = jdbcTemplate.queryForList(sql, String.class, alarmKey, corpCode);
            logger.info("{}查询告警配置完成，结果数量={}, deviceId={}, alarmKey={}, corpCode={}",
                    LOG_PREFIX, results.size(), deviceId, alarmKey, corpCode);

            if (results.isEmpty()) {
                logger.info("{}未找到告警配置记录，不允许告警，deviceId={}, alarmKey={}, corpCode={}",
                        LOG_PREFIX, deviceId, alarmKey, corpCode);
                return false;
            }

            // 打印所有查询结果用于调试
            logger.info("{}告警配置查询结果：{}", LOG_PREFIX, results);

            if (results.contains("0")) {
                logger.info("{}【禁止告警】设备已禁用该类型告警，deviceId={}, alarmKey={}, alarmType={}, corpCode={}, 配置结果={}",
                        LOG_PREFIX, deviceId, alarmKey, alarmType, corpCode, results);
                return false;
            } else if (results.contains("1")) {
                logger.info("{}【允许告警】设备已启用该类型告警，deviceId={}, alarmKey={}, alarmType={}, corpCode={}, 配置结果={}",
                        LOG_PREFIX, deviceId, alarmKey, alarmType, corpCode, results);
                return true;
            } else {
                logger.error("{}告警配置值异常，既不是 0 也不是 1，不允许告警，deviceId={}, alarmKey={}, corpCode={}, 配置结果={}",
                        LOG_PREFIX, deviceId, alarmKey, corpCode, results);
                return false;
            }

        } catch (Exception e) {
            logger.error("{}查询告警配置异常，不允许告警，deviceId={}, alarmKey={}, corpCode={}, error={}",
                    LOG_PREFIX, deviceId, alarmKey, corpCode, e.getMessage(), e);
            return false;
        }
    }

    /**
     * 根据告警类型获取对应的枚举编码
     * @param alarmType 告警类型（0-12 等）
     * @return 枚举编码（如 CG、LG 等）
     */
    private String getAlarmKeyCode(String alarmType) {
        if (alarmType == null) {
            return null;
        }
        switch (alarmType) {
            case "0":
                return AlarmConfigEnum.CG.getCode(); // 串岗
            case "1":
                return AlarmConfigEnum.LG.getCode(); // 离岗
            case "2":
                return AlarmConfigEnum.JZ.getCode(); // 静止
            case "3":
                return AlarmConfigEnum.CY.getCode(); // 超员
            case "4":
                return AlarmConfigEnum.QY.getCode(); // 缺员
            case "5":
                return AlarmConfigEnum.LX.getCode(); // 离线
            case "6":
                return AlarmConfigEnum.YJJR.getCode(); // 越界进入
            case "7":
                return AlarmConfigEnum.YJLK.getCode(); // 越界离开
            case "8":
                return AlarmConfigEnum.ZL.getCode(); // 滞留
            case "12":
                return AlarmConfigEnum.SOS.getCode(); // SOS 求救
            case "13":
                return AlarmConfigEnum.DDL.getCode(); // 低电量
            default:
                return null;
        }
    }

    /**
     * MQTT 告警数据传输对象
     */
    @Data
    public static class MqttAlarmDTO {
        private String id; // 主键
        private String name; // 姓名
        private String phoneNumber; // 电话号码
        private String syncId; // 同步 ID
        private Double lng; // 经度
        private Double lat; // 纬度
        private String floorId; // 楼层 ID
        private String deviceId; // 设备 ID
        private String identityCard; // 身份证号
        private String alarmType; // 告警类型
    }


}
