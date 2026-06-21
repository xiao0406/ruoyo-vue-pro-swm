package cn.iocoder.yudao.module.iot.tcp.processor;

import cn.hutool.core.date.DateUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.core.util.ObjectUtil;
import cn.iocoder.yudao.module.iot.cache.DeviceCorpMappingCache;
import cn.iocoder.yudao.module.iot.cache.service.RedisService;
import cn.iocoder.yudao.module.swm.api.constant.TdengineSuperTableConstants;
import cn.iocoder.yudao.module.swm.dal.dataobject.SwmBeaconStationDO;
import cn.iocoder.yudao.module.swm.dal.dataobject.SwmHazardSourceDO;
import cn.iocoder.yudao.module.iot.mqtt.handler.zhongtai.DeviceSoSAlarmHandler;
import cn.iocoder.yudao.module.swm.service.TDengineService;
import cn.iocoder.yudao.module.iot.tcp.model.TcpMessageData;
import cn.iocoder.yudao.module.iot.service.HelmetSosTdEngineService;
import cn.iocoder.yudao.module.iot.service.SwmWarningManagementService;
import cn.iocoder.yudao.module.swm.service.VoiceAlarmService;
import cn.iocoder.yudao.module.swm.util.DictUtils;
import cn.iocoder.yudao.module.iot.tcp.service.TcpBeaconLocationService;
import cn.iocoder.yudao.module.iot.util.R;
import cn.hutool.json.JSONObject;
import io.netty.channel.ChannelHandlerContext;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import cn.iocoder.yudao.module.swm.api.constant.SwmRedisKeyConstants;

import java.util.*;
import java.util.stream.Collectors;
import java.sql.Timestamp;

import static cn.iocoder.yudao.module.swm.api.constant.SwmRedisKeyConstants.SwmKey.MAC_TO_HAZARD_INFO;

/**
 * 危险源报警TCP消息处理器
 * 处理报警值中包含危险源信标报警位（Bit.5=1）的设备数据
 * 通过匹配MAC地址与危险源信标，触发语音报警
 *
 * @author Shawn
 * @date 2025-01-31
 */
@Component
public class HazardSourceTcpProcessor {

    private static final Logger logger = LoggerFactory.getLogger(HazardSourceTcpProcessor.class);

    @Resource
    private HelmetSosTdEngineService helmetSosTdEngineService;

    @Resource
    private SwmWarningManagementService swmWarningManagementService;

    @Resource
    private JdbcTemplate jdbcTemplate;

    @Resource
    private TcpBeaconLocationService tcpBeaconLocationService;

    @Resource
    private VoiceAlarmService voiceAlarmService;

    @Resource
    private RedisService redisService;

    @Value("${tdengine.dbname}")
    private String dbname;

    @Resource
    private TDengineService tdengineService;
    @Resource
    private DeviceCorpMappingCache deviceCorpMappingCache;
    @Resource
    @Lazy
    private DeviceSoSAlarmHandler soSAlarmHandler;


    // 危险源信标报警在第5位（从0开始），对应的值是2^5=32
    private static final int HAZARD_SOURCE_ALARM_BIT_MASK = 32;

    // 固定语音模板ID（用于ZK1、EW1前缀） swm_voice_template表的template_id字段
    // DEPRECATED: 已移除固定模板逻辑，所有非ZK0的危险源信标统一走配置化
    // private static String FIXED_VOICE_TEMPLATE_ID = "1935324575087489024";



    /**
     * 判断是否可以处理该消息
     * 报警值不为0 或者 包含危险源信标报警位（Bit.5=1）时处理
     *
     * @param messageData TCP消息数据
     * @return true表示需要处理该报警
     */
    public boolean canProcess(TcpMessageData messageData) {
        if (messageData == null || messageData.getAlarmValue() == null) {
            return false;
        }

        Integer alarmValue = messageData.getAlarmValue();
        // 修改判断条件：报警值不为0 或者 包含危险源信标报警位（Bit.5=1）
        boolean shouldProcess = (alarmValue != 0) || ((alarmValue & HAZARD_SOURCE_ALARM_BIT_MASK) != 0);

        if (shouldProcess) {
            logger.info("检测到需要处理的报警, 设备ID: {}, 报警值: {} (二进制: {})",
                    messageData.getDeviceId(),
                    alarmValue,
                    Integer.toBinaryString(alarmValue));
        }

        return shouldProcess;
    }

    /**
     * 处理危险源报警消息
     *
     * @param messageData TCP消息数据
     * @param ctx         用于发送响应的通道上下文（本处理器不直接下发语音）
     */
    public void process(TcpMessageData messageData, ChannelHandlerContext ctx) {
        String deviceId = messageData.getDeviceId();

        try {

            List<Map<String, Object>> matchedHazardSources = getMatchedHazardSourcesWithVoiceByRedis(messageData);
            if (matchedHazardSources.isEmpty()) {
                return;
            }

            String corpCode = deviceCorpMappingCache.getCorpCode(deviceId);

            logger.info("开始处理报警消息（危险源检测）, 设备ID: {}, 报警值: {}",
                    deviceId, messageData.getAlarmValue());

            // 1. 获取信标缓存数据，建立MAC到设备名称的映射
//            Map<String, String> macToDeviceName = buildMacToDeviceNameMap();
            Map<String, String> macToDeviceName = buildMacToDeviceNameMapByRedis(corpCode,messageData.getBluetoothBeacons());

            // 2. 检查特殊前缀（ZKF、EWF用于考勤打卡）
            boolean hasAttendancePrefix = false;

            List<Map<String, Object>> beacons = messageData.getBluetoothBeacons();
            if (beacons != null) {
                for (Map<String, Object> beacon : beacons) {
                    String mac = (String) beacon.get("MAC");
                    if (StringUtils.isNotBlank(mac)) {
                        String deviceName = macToDeviceName.get(mac.toUpperCase());
                        if (StringUtils.isNotBlank(deviceName)) {
                            // 已移除ZK1/EW1特殊处理，所有非ZK0的信标统一走配置化危险源处理
                            if (deviceName.startsWith("ZKF") || deviceName.startsWith("EWF")) {
                                hasAttendancePrefix = true;
                                logger.info("检测到考勤前缀: MAC={}, deviceName={}", mac, deviceName);
                            }
                        }
                    }
                }
            }

            // 3. 保存原始TCP消息到bt_signal_info字段（增加设备名称信息）
            String btSignalInfo = buildBtSignalInfoWithDeviceName(messageData, macToDeviceName);

            // 4. 判断是否需要继续处理
            if (matchedHazardSources.isEmpty()) {
                logger.warn("未找到匹配的危险源信标, 设备ID: {}", deviceId);
                return;
            }

            logger.info("匹配到 {} 个危险源信标: {}, 设备ID: {}",
                    matchedHazardSources.size(),
                    matchedHazardSources.stream().map(h -> (String) h.get("hazard_name")).collect(Collectors.joining(", ")),
                    deviceId);

            // 6. 遍历每个危险源，统一防重 + 入库 + 语音（三者一致）
            for (Map<String, Object> hazard : matchedHazardSources) {
                String hazardId = (String) hazard.get("id");
                String hazardName = (String) hazard.get("hazard_name");
                String voiceTemplateId = (String) hazard.get("voice_template_id");
                String mac = (String) hazard.get("mac");

                // 6.1 从 Redis Hash 获取该危险源对应的设备集合
                Object cacheObj = redisService.hget(corpCode+ SwmRedisKeyConstants.SwmKey.Hazard_ISALARM_BEACON, hazardId);
                if (cacheObj instanceof Set) {
                    Set<String> disabledDevices = (Set<String>) cacheObj;
                    if (disabledDevices.contains(deviceId)) {
                        logger.info("设备在危险源 {} 的禁用报警列表中, 设备ID: {}", hazardName, deviceId);
                        continue;
                    }
                }

                // 6.2 按危险源自己的MAC查location和area（不用最强信号）
                String hazardLocation = "";
                String hazardArea = "";
                if (StringUtils.isNotBlank(mac)) {
                    hazardLocation = tcpBeaconLocationService.getLocationByMac(mac, corpCode);
                    Map<String, Object> areaResult = tcpBeaconLocationService.getAreaNameByMac(mac, corpCode);
                    if (areaResult != null) {
                        hazardArea = (String) areaResult.get("areaName");
                    }
                }

                // 6.3 原子防重：同一信标位置3分钟内不入库也不语音
                String dedupLocation = StringUtils.isNotBlank(hazardLocation) ? hazardLocation : "NO_LOCATION:" + mac;
                String dedupKey = corpCode + ":hazard_voice_dedup:" + deviceId + ":" + dedupLocation;
                Boolean locked = redisService.setNxValue(dedupKey, "1", 3 * 60L);
                if (locked == null || !locked) {
                    logger.info("设备 {} 危险源 {} 位置 {} 3分钟内已处理，跳过", deviceId, hazardName, hazardLocation);
                    continue;
                }

                logger.info("危险源入库+语音: 名称={}, location={}, area={}, mac={}, 设备ID={}",
                        hazardName, hazardLocation, hazardArea, mac, deviceId);

                // 6.4 保存危险源报警数据到TDengine
                saveHazardSourceDataToTDengine(deviceId, messageData, btSignalInfo);

                // 6.5 保存到swm_warning_management表（每个危险源一条，带自己的位置）
                saveWarningToSwmWarningManagement(deviceId, "8", btSignalInfo, hazardLocation, hazardArea,
                        Collections.singletonList(hazardName));

                // 6.6 下发语音
                if (StringUtils.isNotBlank(voiceTemplateId)) {
                    sendVoiceForHazard(deviceId, voiceTemplateId, hazardName, hazardLocation, messageData);
                } else {
                    logger.warn("危险源 {} 没有配置语音模板ID，跳过语音下发", hazardName);
                }
            }

            logger.info("报警消息处理完成（危险源检测）, 设备ID: {}", deviceId);

        } catch (Exception e) {
            logger.error("处理危险源报警时发生异常, 设备ID: {}", deviceId, e);
        }
    }

    /**
     * 构建MAC到设备名称的映射
     * 直接从数据库查询所有信标数据
     *
     * @return MAC地址到设备名称的映射
     */
    private Map<String, String> buildMacToDeviceNameMap() {
        Map<String, String> macToDeviceName = new HashMap<>();

        try {
            // 直接查询swm_beacon_station表获取所有status='0'的记录
            String sql = "SELECT beacon_id, device_name FROM swm_beacon_station WHERE status = '0' AND beacon_id IS NOT NULL AND device_name IS NOT NULL";
            List<Map<String, Object>> results = jdbcTemplate.queryForList(sql);

            for (Map<String, Object> row : results) {
                String beaconId = (String) row.get("beacon_id");
                String deviceName = (String) row.get("device_name");

                if (StringUtils.isNotBlank(beaconId) && StringUtils.isNotBlank(deviceName)) {
                    // beacon_id（MAC地址）转大写作为key
                    macToDeviceName.put(beaconId.toUpperCase(), deviceName);
                }
            }

            logger.info("构建MAC到设备名称映射完成，共{}条记录", macToDeviceName.size());

        } catch (Exception e) {
            logger.error("构建MAC到设备名称映射失败", e);
        }

        return macToDeviceName;
    }

    /**
     * 构建MAC到设备名称的映射
     * 直接从数据库查询所有信标数据
     *
     * @return MAC地址到设备名称的映射
     */
    private Map<String, String> buildMacToDeviceNameMapByRedis(String corpCode,List<Map<String, Object>> beacons) {
        Map<String, String> macToDeviceName = new HashMap<>();
        try {
            if(beacons == null){
                return macToDeviceName;
            }
            String beaconCacheKey = corpCode+ SwmRedisKeyConstants.SwmKey.BEACON_MAC_CACHE_KEY;
            for (Map<String, Object> beacon : beacons) {
                String mac = (String) beacon.get("MAC");

                SwmBeaconStation beaconStation = (SwmBeaconStation) redisService.hget(beaconCacheKey,mac.toUpperCase() );
                if (ObjectUtil.isEmpty(beaconStation)){
                    continue;
                }
                if (StringUtils.isNotBlank(beaconStation.getBeaconId()) && StringUtils.isNotBlank(beaconStation.getDeviceName())) {
                    // beacon_id（MAC地址）转大写作为key
                    macToDeviceName.put(beaconStation.getBeaconId().toUpperCase(), beaconStation.getDeviceName());
                }
            }
        } catch (Exception e) {
            logger.error("构建MAC到设备名称映射失败", e);
        }

        return macToDeviceName;
    }

    /**
     * 构建bt_signal_info JSON字符串（包含设备名称）
     * 格式与WebSocket保持一致，包含dataList数组
     *
     * @param messageData     TCP消息数据
     * @param macToDeviceName MAC到设备名称的映射
     * @return bt_signal_info JSON字符串
     */
    private String buildBtSignalInfoWithDeviceName(TcpMessageData messageData, Map<String, String> macToDeviceName) {
        try {
            JSONObject btSignal = new JSONObject();
            List<JSONObject> dataList = new ArrayList<>();

            // 获取蓝牙信标列表
            List<Map<String, Object>> beacons = messageData.getBluetoothBeacons();
            if (beacons != null) {
                for (Map<String, Object> beacon : beacons) {
                    String mac = (String) beacon.get("MAC");
                    String rssi = (String) beacon.get("RSSI");

                    if (StringUtils.isNotBlank(mac)) {
                        JSONObject data = new JSONObject();

                        // 将MAC格式化为带冒号的格式，用于dataList中的mac字段
                        String formattedMac = tcpBeaconLocationService.formatMacWithColon(mac);
                        data.put("mac", formattedMac);

                        // name字段：如果有设备名称使用设备名称，否则使用MAC
                        String deviceName = macToDeviceName.get(mac.toUpperCase());
                        if (StringUtils.isNotBlank(deviceName)) {
                            data.put("name", deviceName);
                        } else {
                            data.put("name", mac.toUpperCase());
                        }

                        if (StringUtils.isNotBlank(rssi)) {
                            try {
                                data.put("rssi", Integer.parseInt(rssi));
                            } catch (NumberFormatException e) {
                                data.put("rssi", 0);
                            }
                        }

                        dataList.add(data);
                    }
                }
            }

            btSignal.put("dataList", dataList);
            return btSignal.toString();

        } catch (Exception e) {
            logger.error("构建bt_signal_info失败", e);
            // 如果构建失败，返回原始消息
            return messageData.getRawMessage() != null ? messageData.getRawMessage() : "";
        }
    }

    /**
     * 构建bt_signal_info JSON字符串
     * 格式与WebSocket保持一致，包含dataList数组
     */
    private String buildBtSignalInfo(TcpMessageData messageData) {
        try {
            JSONObject btSignal = new JSONObject();
            List<JSONObject> dataList = new ArrayList<>();

            // 获取蓝牙信标列表
            List<Map<String, Object>> beacons = messageData.getBluetoothBeacons();
            if (beacons != null) {
                for (Map<String, Object> beacon : beacons) {
                    String mac = (String) beacon.get("MAC");
                    String rssi = (String) beacon.get("RSSI");

                    if (StringUtils.isNotBlank(mac)) {
                        JSONObject data = new JSONObject();

                        // 将MAC格式化为带冒号的格式，用于dataList中的mac字段
                        String formattedMac = tcpBeaconLocationService.formatMacWithColon(mac);
                        data.put("mac", formattedMac);

                        // name字段使用无冒号的大写格式
                        data.put("name", mac.toUpperCase());

                        if (StringUtils.isNotBlank(rssi)) {
                            try {
                                data.put("rssi", Integer.parseInt(rssi));
                            } catch (NumberFormatException e) {
                                data.put("rssi", 0);
                            }
                        }

                        dataList.add(data);
                    }
                }
            }

            btSignal.put("dataList", dataList);
            return btSignal.toString();

        } catch (Exception e) {
            logger.error("构建bt_signal_info失败", e);
            // 如果构建失败，返回原始消息
            return messageData.getRawMessage() != null ? messageData.getRawMessage() : "";
        }
    }

    /**
     * 获取匹配的危险源列表（包含语音信息）
     *
     * @param messageData TCP消息数据
     * @return 匹配到的危险源信息列表（包含危险源名称、语音模板ID等）
     */
    private List<Map<String, Object>> getMatchedHazardSourcesWithVoice(TcpMessageData messageData) {
        List<Map<String, Object>> matchedHazardSources = new ArrayList<>();
        Set<String> processedHazardBeaconKeys = new HashSet<>();

        try {
            // 获取所有蓝牙信标
            List<Map<String, Object>> beacons = messageData.getBluetoothBeacons();
            if (beacons == null || beacons.isEmpty()) {
                return matchedHazardSources;
            }

            // 遍历每个MAC地址
            for (Map<String, Object> beacon : beacons) {
                String mac = (String) beacon.get("MAC");
                if (StringUtils.isBlank(mac)) {
                    continue;
                }

                // MAC地址已经是大写无冒号格式，直接使用
                String formattedMac = mac.toUpperCase();

                // 查询匹配的危险源
                String sql = "SELECT id, hazard_name, voice_template_id, hazard_category FROM swm_hazard_source " +
                        "WHERE hazard_status IN ('0','1') " +
                        "AND status = '0' " +
                        "AND FIND_IN_SET(?, beacon_identifier) > 0";

                List<Map<String, Object>> results = jdbcTemplate.queryForList(sql, formattedMac);

                for (Map<String, Object> hazard : results) {
                    String hazardId = (String) hazard.get("id");
                    hazard.put("mac", formattedMac);

                    // 避免重复记录同一个危险源
                    String hazardBeaconKey = hazardId + ":" + formattedMac;
                    if (!processedHazardBeaconKeys.contains(hazardBeaconKey)) {
                        processedHazardBeaconKeys.add(hazardBeaconKey);
                        matchedHazardSources.add(hazard);
                        logger.info("匹配到危险源: MAC={}, 危险源名称={}, 语音模板ID={}",
                                formattedMac, hazard.get("hazard_name"), hazard.get("voice_template_id"));
                    }
                }
            }

        } catch (Exception e) {
            logger.error("查询匹配的危险源时发生异常", e);
        }

        return matchedHazardSources;
    }

    /**
     * 获取匹配的危险源列表（包含语音信息）
     *
     * @param messageData TCP消息数据
     * @return 匹配到的危险源信息列表（包含危险源名称、语音模板ID等）
     */
    private List<Map<String, Object>> getMatchedHazardSourcesWithVoiceByRedis(TcpMessageData messageData) {
        List<Map<String, Object>> matchedHazardSources = new ArrayList<>();
        Set<String> processedHazardBeaconKeys = new HashSet<>();

        try {
            // 获取所有蓝牙信标
            List<Map<String, Object>> beacons = messageData.getBluetoothBeacons();
            if (beacons == null || beacons.isEmpty()) {
                return matchedHazardSources;
            }
            String deviceId = messageData.getDeviceId();
            String corpCode = deviceCorpMappingCache.getCorpCode(deviceId);
            String redisKey = corpCode + SwmRedisKeyConstants.SwmKey.MAC_TO_HAZARD_INFO;

            // 遍历每个MAC
            for (Map<String, Object> beacon : beacons) {
                String mac = (String) beacon.get("MAC");
                if (StringUtils.isBlank(mac)) {
                    continue;
                }

                // 统一大写
                String formattedMac = mac.toUpperCase();

                // ============== 从 Redis Hash 中获取危险源 ==============
                SwmHazardSource hazardSource = (SwmHazardSource) redisService.hget(redisKey, formattedMac);
                if (hazardSource == null) {
                    continue; // 没匹配到，跳过
                }

                // 转成 Map（和原来返回结构保持一致，上层不用改）
                Map<String, Object> hazardMap = new HashMap<>();
                String hazardId = hazardSource.getId().toString();
                String hazardCategory = getHazardCategoryById(hazardId);
                hazardMap.put("id", hazardId);
                hazardMap.put("hazard_name", hazardSource.getHazardName());
                hazardMap.put("voice_template_id", hazardSource.getVoiceTemplateId());
                hazardMap.put("hazard_category", hazardCategory);
                // 携带该危险源对应的MAC地址，用于后续按MAC查自己的location
                hazardMap.put("mac", formattedMac);

                // 去重（同一个危险源只加一次）
                String hazardBeaconKey = hazardId + ":" + formattedMac;
                if (!processedHazardBeaconKeys.contains(hazardBeaconKey)) {
                    processedHazardBeaconKeys.add(hazardBeaconKey);
                    matchedHazardSources.add(hazardMap);

                    logger.info("【Redis缓存】匹配到危险源: MAC={}, 名称={}, 语音ID={}",
                            formattedMac, hazardSource.getHazardName(), hazardSource.getVoiceTemplateId());
                }
            }

        } catch (Exception e) {
            logger.error("Redis查询匹配危险源异常", e);
        }

        return matchedHazardSources;
    }

    private String getHazardCategoryById(String hazardId) {
        if (StringUtils.isBlank(hazardId)) {
            return null;
        }
        try {
            String sql = "SELECT hazard_category FROM swm_hazard_source WHERE id = ? AND status = '0' LIMIT 1";
            List<String> results = jdbcTemplate.queryForList(sql, String.class, hazardId);
            return results.isEmpty() ? null : results.get(0);
        } catch (Exception e) {
            logger.warn("查询危险源类别失败, hazardId={}", hazardId, e);
            return null;
        }
    }


    /**
     * 为匹配到的危险源发送语音报警
     * 所有非ZK0的危险源信标统一走配置化处理
     * 每个危险源按自己的MAC查对应的location发送语音
     *
     * @param deviceId             设备ID
     * @param matchedHazardSources 匹配到的危险源信息列表
     * @param messageData          TCP消息数据
     */
    /**
     * 为单个危险源下发语音报警
     */
    private void sendVoiceForHazard(String deviceId, String voiceTemplateId, String hazardName,
                                    String hazardLocation, TcpMessageData messageData) {
        try {
            String sql = "SELECT voice_text FROM swm_voice_template WHERE id = ? AND status = '0'";
            List<String> results = jdbcTemplate.queryForList(sql, String.class, voiceTemplateId);

            if (results.isEmpty()) {
                logger.warn("未找到语音模板: ID={}, 危险源={}", voiceTemplateId, hazardName);
                return;
            }

            String voiceText = results.get(0);
            if (StringUtils.isBlank(voiceText)) {
                logger.warn("语音模板内容为空: ID={}, 危险源={}", voiceTemplateId, hazardName);
                return;
            }

            voiceText = voiceText.replace("{location}", StringUtils.isNotBlank(hazardLocation) ? hazardLocation : "");

            logger.info("准备下发语音报警: 设备ID={}, 危险源={}, 位置={}, 语音内容={}",
                    deviceId, hazardName, hazardLocation, voiceText);
            if (!messageData.isZTDevice()){
                voiceAlarmService.sendVoiceAlarm(deviceId, voiceText);
                logger.info("语音报警下发成功: 设备ID={}, 危险源={}, 位置={}",
                        deviceId, hazardName, hazardLocation);
            } else {
                soSAlarmHandler.sendVoiceCommand(deviceId, voiceText);
                logger.info("ZT设备发送语音报警: 设备ID={}, 位置={}, 内容={}", deviceId, hazardLocation, voiceText);
            }

        } catch (Exception e) {
            logger.error("下发语音报警时发生异常: 设备ID={}, 危险源={}", deviceId, hazardName, e);
        }
    }

    /**
     * 保存危险源报警数据到TDengine
     *
     * @param deviceId     设备ID
     * @param messageData  TCP消息数据
     * @param btSignalInfo 蓝牙信标信息JSON
     */
    private void saveHazardSourceDataToTDengine(String deviceId, TcpMessageData messageData, String btSignalInfo) {
        try {
            // 构建危险源报警数据Map
            Map<String, Object> hazardDataMap = new HashMap<>();
            hazardDataMap.put("device_id", deviceId);
            hazardDataMap.put("type", "8"); // 危险源报警
            hazardDataMap.put("act", "ca_sos"); // 保持与WebSocket一致

            // 添加sos_time字段（使用扫描时间戳）
            if (messageData.getScanTimestamp() != null) {
                hazardDataMap.put("sos_time", messageData.getScanTimestamp());
            }

            // 添加蓝牙信标信息
            if (StringUtils.isNotBlank(btSignalInfo)) {
                hazardDataMap.put("bt_signal_info", btSignalInfo);
            }

            // 调用服务保存数据
            R<JSONObject> result = helmetSosTdEngineService.saveHelmetSosData(deviceId, hazardDataMap);

            if (result.getCode() != R.SUCCESS) {
                logger.error("保存危险源报警数据到TDengine失败, deviceId: {}, 错误: {}",
                        deviceId, result.getMsg());
            } else {
                logger.info("成功保存危险源报警数据到TDengine, deviceId: {}", deviceId);
            }

        } catch (Exception e) {
            logger.error("保存危险源报警数据到TDengine异常, deviceId: {}", deviceId, e);
        }
    }

    /**
     * 保存预警信息到swm_warning_management表
     *
     * @param deviceId           设备ID
     * @param type               报警类型（8=危险源报警）
     * @param btSignalInfo       蓝牙信标信息
     * @param location           位置信息
     * @param areaName           区域名称
     * @param matchedHazardNames 匹配到的危险源名称列表
     */
    private void saveWarningToSwmWarningManagement(String deviceId, String type,
            String btSignalInfo, String location, String areaName, List<String> matchedHazardNames) {
        try {
            // 查询设备绑定的身份证（与WebSocket保持一致）
//            String sql = "SELECT assigned_person FROM swm_helmet_device WHERE device_id = ? AND status = '0'";
//            List<String> results = jdbcTemplate.queryForList(sql, String.class, deviceId);
//            String idCard = !results.isEmpty() ? results.get(0) : null;
            String cardId = (String) redisService.hget(SwmRedisKeyConstants.GlobalKey.DEVICE_PERSON_MAP, String.valueOf(deviceId));
            String idCard = !cardId.isEmpty() ? cardId : null;

            // 解析btSignalInfo并根据name前缀设置warningContent
            String warningContent = parseWarningContentFromBtSignalInfo(btSignalInfo);

            String triggerReason = "检测到工人接近危险源: " + String.join(", ", matchedHazardNames);
            String alarmRecord = "危险源信标报警记录";
            String warningType = "2"; // 被动报警

            // 新增：查询危险源类别
            String hazardCategory = getHazardCategoryFromMatchedSources(matchedHazardNames);

            // 调用服务保存预警信息
            // 注意：type=8时，WebSocket层的handleType8VoiceTemplate会自动处理语音下发
            R<JSONObject> result = swmWarningManagementService.saveCustomWarningToSwmWarningManagement(
                    deviceId,
                    idCard,
                    warningContent,
                    triggerReason,
                    alarmRecord,
                    warningType,
                    type, // 原始type值=8
                    false, // locationAdded
                    hazardCategory, // 不再是null，而是查询到的值
                    btSignalInfo, // bt_signal_info
                    location, // location
                    areaName // areaName
            );

            if (result.getCode() != R.SUCCESS) {
                logger.error("保存预警信息到swm_warning_management失败, deviceId: {}, 错误: {}",
                        deviceId, result.getMsg());
            } else {
                logger.info("成功保存预警信息到swm_warning_management, deviceId: {}, type: {}, hazardCategory: {}",
                        deviceId, type, hazardCategory);
            }

        } catch (Exception e) {
            logger.error("保存预警信息异常, deviceId: {}", deviceId, e);
        }
    }

    /**
     * 解析btSignalInfo JSON串，根据name前缀确定warningContent
     * 逻辑：不是ZK0的都为危险源信标
     *
     * @param btSignalInfo 蓝牙信标信息JSON串
     * @return warningContent内容
     */
    private String parseWarningContentFromBtSignalInfo(String btSignalInfo) {
        // 默认值为"危险区域闯入提示"（危险源报警）
        String defaultWarningContent = DictUtils.getDictLabel("warning_content_enum", "危险区域闯入提示", "危险区域闯入提示");

        if (StringUtils.isBlank(btSignalInfo)) {
            return defaultWarningContent;
        }

        try {
            // 解析JSON
            JSONObject btSignalJson = new JSONObject(btSignalInfo);

            // 获取dataList数组
            Object dataListObj = btSignalJson.get("dataList");
            if (!(dataListObj instanceof List)) {
                logger.debug("btSignalInfo中dataList格式不正确，使用默认warningContent");
                return defaultWarningContent;
            }

            @SuppressWarnings("unchecked")
            List<Object> dataList = (List<Object>) dataListObj;

            // 遍历dataList，查找符合条件的name
            for (Object item : dataList) {
                if (item instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> dataItem = (Map<String, Object>) item;
                    String name = (String) dataItem.get("name");

                    if (StringUtils.isNotBlank(name)) {
                        // ZK0前缀排除，不作为危险源
                        if (name.startsWith("ZK0")) {
                            logger.debug("检测到ZK0前缀，排除: {}", name);
                            continue;
                        }

                        // 检查是否以EWF或ZKF开头（考勤打卡）
                        if (name.startsWith("EWF") || name.startsWith("ZKF")) {
                            logger.info("检测到考勤打卡前缀: {}", name);
                            return "考勤打卡";
                        }

                        // 其他所有非ZK0的信标都视为危险源（包括ZK1、EW1等）
                        // 直接返回默认的危险源报警内容
                        logger.info("检测到危险源信标前缀: {}, 返回危险源报警", name);
                        return defaultWarningContent;
                    }
                }
            }

            // 没有找到匹配的前缀，返回默认值
            return defaultWarningContent;

        } catch (Exception e) {
            logger.error("解析btSignalInfo时发生异常，使用默认warningContent", e);
            return defaultWarningContent;
        }
    }

    /**
     * 直接从原始报文判断是否需要处理
     * 这个方法可以独立使用，不依赖TcpMessageData对象
     *
     * @param rawMessage 原始TCP报文
     * @return true表示需要处理（报警值不为0 或 Bit.5=1）
     */
    public boolean hasHazardSourceAlarmFromRawMessage(String rawMessage) {
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

            // 修改判断条件：报警值不为0 或者 包含危险源信标报警位（Bit.5=1）
            boolean shouldProcess = (alarmValue != 0) || ((alarmValue & HAZARD_SOURCE_ALARM_BIT_MASK) != 0);

            if (shouldProcess) {
                logger.info("从原始报文检测到需要处理的报警, 报警值: {} (二进制: {})",
                        alarmValue, Integer.toBinaryString(alarmValue));
            }

            return shouldProcess;

        } catch (NumberFormatException e) {
            logger.warn("报警值字段格式错误: {}", e.getMessage());
            return false;
        } catch (Exception e) {
            logger.error("解析原始报文时发生异常: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 处理TCP方式的考勤打卡
     * 当检测到ZKF或EWF开头的信标时调用
     *
     * @param deviceId 设备ID
     */
    private void handleAttendanceForTcp(String deviceId) {
        try {
            logger.info("开始处理TCP方式的考勤打卡, deviceId: {}", deviceId);

            // 查询设备绑定的身份证
            String sql = "SELECT assigned_person FROM swm_helmet_device WHERE device_id = ? AND status = '0'";
            List<String> results = jdbcTemplate.queryForList(sql, String.class, deviceId);

            if (results.isEmpty()) {
                logger.warn("未查询到设备绑定的身份证, deviceId: {}", deviceId);
                return;
            }

            String idCard = results.get(0);
            if (StringUtils.isBlank(idCard)) {
                logger.warn("设备绑定的身份证为空, deviceId: {}", deviceId);
                return;
            }

            // 插入考勤日志记录（异常不影响主业务）
            insertAttendanceLogForTcp(deviceId, idCard);

            // 更新考勤记录
            R<JSONObject> result = swmWarningManagementService.updateAttendanceRecord(deviceId, idCard);

            if (result.getCode() != R.SUCCESS) {
                logger.error("更新考勤记录失败, deviceId: {}, idCard: {}, 错误信息: {}",
                        deviceId, idCard, result.getMsg());
            } else {
                logger.info("考勤记录更新成功, deviceId: {}, idCard: {}", deviceId, idCard);
            }

        } catch (Exception e) {
            logger.error("处理TCP方式的考勤打卡异常, deviceId: {}", deviceId, e);
        }
    }

    /**
     * 插入TCP考勤日志记录
     *
     * @param deviceId 设备ID
     * @param idCard   身份证号码
     * @author Shawn
     * @date 2025-08-24
     */
    private void insertAttendanceLogForTcp(String deviceId, String idCard) {
        try {
            // 查询员工姓名
            String selectNameSql = "SELECT name FROM swm_person WHERE identity_card = ? AND status = '0' LIMIT 1";
            List<Map<String, Object>> nameResults = jdbcTemplate.queryForList(selectNameSql, idCard);

            String employeeName = null;
            if (!nameResults.isEmpty() && nameResults.get(0).get("name") != null) {
                employeeName = nameResults.get(0).get("name").toString();
            }

            // 生成主键ID
            String logId = UUID.randomUUID().toString().replace("-", "");

            // 获取当前时间
            Timestamp currentTime = new Timestamp(System.currentTimeMillis());

            // 插入考勤日志
            String insertSql = "INSERT INTO swm_attendance_log " +
                    "(id, employee_name, identity_card, clock_time, device_id, clock_source, " +
                    "create_by, create_date, status) VALUES " +
                    "(?, ?, ?, ?, ?, ?, ?, ?, ?)";

            jdbcTemplate.update(insertSql,
                    logId,
                    employeeName,
                    idCard,
                    currentTime,
                    deviceId,
                    "TCP",
                    "system",
                    currentTime,
                    "0");

            logger.info("TCP考勤日志插入成功: deviceId={}, idCard={}, name={}",
                       deviceId, idCard, employeeName);

        } catch (Exception e) {
            logger.warn("插入TCP考勤日志失败, deviceId: {}, idCard: {}, 错误: {}",
                       deviceId, idCard, e.getMessage());
        }
    }

    /**
     * 根据匹配的危险源名称查询危险源类别（从字典表获取中文文本）
     *
     * @param matchedHazardNames 匹配到的危险源名称列表
     * @return 危险源类别的中文文本，如果没有找到则返回null
     */
    private String getHazardCategoryFromMatchedSources(List<String> matchedHazardNames) {
        if (matchedHazardNames == null || matchedHazardNames.isEmpty()) {
            logger.debug("危险源名称列表为空，无法查询危险源类别");
            return null;
        }

        try {
            // 构建IN查询条件
            String placeholders = matchedHazardNames.stream()
                    .map(name -> "?")
                    .collect(Collectors.joining(","));

            String sql = "SELECT DISTINCT hazard_category FROM swm_hazard_source " +
                    "WHERE hazard_name IN (" + placeholders + ") " +
                    "AND status = '0' " +
                    "AND hazard_category IS NOT NULL " +
                    "AND hazard_category != ''";

            List<String> categories = jdbcTemplate.queryForList(sql, String.class,
                    matchedHazardNames.toArray());

            if (categories.size() == 1) {
                // 只有一个类别，使用字典查询中文文本
                String categoryCode = categories.get(0);
                String categoryText = DictUtils.getDictLabel("hazard_category_enum", categoryCode, null);

                logger.info("查询到危险源类别: {} -> {}, 危险源: {}", categoryCode, categoryText,
                        String.join(", ", matchedHazardNames));

                // 返回中文文本，如果字典查询失败则返回原编码
                return StringUtils.isNotBlank(categoryText) ? categoryText : categoryCode;
            } else if (categories.size() > 1) {
                // 多个不同类别，使用第一个
                String firstCategoryCode = categories.get(0);
                String categoryText = DictUtils.getDictLabel("hazard_category_enum", firstCategoryCode, null);

                logger.warn("匹配到多个不同的危险源类别: {}, 危险源: {}, 使用第一个类别: {} -> {}",
                        categories, String.join(", ", matchedHazardNames), firstCategoryCode, categoryText);

                // 返回中文文本，如果字典查询失败则返回原编码
                return StringUtils.isNotBlank(categoryText) ? categoryText : firstCategoryCode;
            } else {
                // 没有找到类别
                logger.info("未找到危险源类别, 危险源: {}", String.join(", ", matchedHazardNames));
                return null;
            }

        } catch (Exception e) {
            logger.error("查询危险源类别时发生异常, 危险源: {}",
                    String.join(", ", matchedHazardNames), e);
            return null;
        }
    }
}
