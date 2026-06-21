package cn.iocoder.yudao.module.iot.tcp.processor;

import cn.iocoder.yudao.module.iot.service.HelmetRundeCaReportLocationTdEnginService;
import cn.iocoder.yudao.module.iot.service.GeoPixelCompareDataService;
import cn.iocoder.yudao.module.iot.service.LocationEngineService;
import cn.iocoder.yudao.module.iot.service.SiteMapMatchService;
import cn.iocoder.yudao.module.iot.service.TcpToHelmetDataConverter;
import cn.iocoder.yudao.module.iot.tcp.model.TcpMessageData;
import cn.iocoder.yudao.module.iot.util.R;
import cn.iocoder.yudao.module.iot.cache.BeaconMacAddressCache;
import cn.iocoder.yudao.module.iot.dal.dataobject.BeaconStationDO;
import cn.hutool.json.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 报警值为0的TCP消息处理器
 * 处理静止状态的设备数据，调用定位引擎并保存数据
 *
 * @author Shawn
 * @date 2026-03-31
 */
@Component
public class AlarmZeroTcpProcessor {

    /**
     * 防抖只回看最近4条历史，再加上当前包，组成"5票窗口"。
     */
    private static final int DECISION_HISTORY_LIMIT = 4;
    /**
     * 5票里拿到3票，就认为这一侧已经形成稳定态。
     */
    private static final int INDOOR_VOTE_THRESHOLD = 3;
    private static final long RECENT_BLE_LOOKBACK_MILLIS = 60 * 1000L;
    /**
     * 历史票和当前包相隔超过10分钟，就视为上一段行为，直接断档。
     */
    private static final long HISTORY_GAP_TIMEOUT_MILLIS = 10 * 60 * 1000L;
    private static final String FINAL_SCENE_INDOOR = "INDOOR";
    private static final String FINAL_SCENE_OUTDOOR = "OUTDOOR";
    private static final Logger logger = LoggerFactory.getLogger(AlarmZeroTcpProcessor.class);

    @Resource
    private LocationEngineService locationEngineService;

    @Resource
    private TcpToHelmetDataConverter dataConverter;

    @Resource
    private HelmetRundeCaReportLocationTdEnginService helmetService;

    @Resource
    private BeaconMacAddressCache beaconMacAddressCache;

    /**
     * 场地图层匹配服务：用于把协议经纬度匹配到 mapId/mapName，并输出标准日志。
     */
    @Resource
    private SiteMapMatchService siteMapMatchService;

    /**
     * 经纬度像素对比落库服务：写入 geo_pixel_compare_data 超级表。
     */
    @Resource
    private GeoPixelCompareDataService geoPixelCompareDataService;

    /**
     * 判断是否可以处理该消息
     *
     * @param messageData TCP消息数据
     * @return true表示可以处理（报警值为0）
     */
    public boolean canProcess(TcpMessageData messageData) {
        return messageData != null &&
                messageData.getAlarmValue() != null &&
                messageData.getAlarmValue() == 0;
    }

    /**
     * 处理TCP消息
     *
     * @param messageData TCP消息数据
     */
    public void process(TcpMessageData messageData) {
        String deviceId = messageData.getDeviceId();

        try {
            logger.info("开始处理报警值为0的TCP消息, 设备ID: {}, 信标数量: {}",
                    deviceId,
                    messageData.getBluetoothBeacons() != null ? messageData.getBluetoothBeacons().size() : 0);

            // 1. 调用定位引擎
            Map<String, Object> locationResult = callLocationEngineIfNeeded(messageData);
            if (locationResult == null) {
                locationResult = new java.util.HashMap<>();
            }


            // 1.5 基于协议经纬度做地图匹配并投影像素，输出日志并返回结构化结果。
            // 放在外部坐标落库前执行，便于同一批消息按时间顺序排查。
            SiteMapMatchService.MapPixelMatchResult mapPixelMatchResult =
                    siteMapMatchService.matchAndProjectAndLogWithResult(messageData);

            // 1.6 读取最近几条定位决策，用于边缘区防抖。
            List<GeoPixelCompareDataService.RecentDecisionRecord> recentDecisionRecords =
                    geoPixelCompareDataService.getRecentDecisionRecordsForTcp(deviceId, DECISION_HISTORY_LIMIT);

            // external像素固定记录蓝牙定位原始像素，不受后续最终像素选型影响。
            Map<String, Object> bleRawLocationResult = new HashMap<>(locationResult);

            // 1.7 按"蓝牙门槛规则"选择最终像素，并回写到locationResult.x/y。
            // 该最终像素会同时影响 external_coordinate_data 与 area_fence_data。
            Long currentBusinessTimestamp = resolveCurrentBusinessTimestamp(messageData);
            PixelDecision pixelDecision = applyFinalPixelByRule(locationResult, mapPixelMatchResult, deviceId,
                    currentBusinessTimestamp, recentDecisionRecords);
            logFinalPixelDecisionSummary(deviceId, bleRawLocationResult, pixelDecision);

            boolean skipExternal = shouldSkipExternalCoordinateWrite(deviceId, bleRawLocationResult, pixelDecision,
                    currentBusinessTimestamp);
            LocationEngineService.ExternalWriteResult externalWriteResult;
            if (skipExternal) {
                externalWriteResult = LocationEngineService.ExternalWriteResult.notWritten(
                        "原本准备按GEO写入，但因最近1分钟内存在BLE/BLE_FALLBACK记录被拦截。");
                logger.info("设备[{}] 本次未写入external_coordinate_data，原因：当前包未算出BLE坐标，本次最终选择GEO，且最近1分钟内存在BLE/BLE_FALLBACK记录。",
                        deviceId);
            } else {
                Long externalCoordinateTime = resolveExternalCoordinateTime(messageData, pixelDecision, deviceId);
                externalWriteResult = locationEngineService.saveLocationToExternalTableForTcp(deviceId, locationResult,
                        externalCoordinateTime);
            }

            String compareRemarks = buildCompareRemarks(pixelDecision, skipExternal, externalWriteResult);

            // 1.8 把"原始经纬度 + 算法像素 + 最终决策"写入对比表，便于核对偏差和回放切换过程。
            geoPixelCompareDataService.saveCompareDataForTcp(messageData, bleRawLocationResult, mapPixelMatchResult,
                    pixelDecision.isIndoorCandidate(), pixelDecision.getRssiGtMinus85Count(),
                    pixelDecision.getFinalPixelSource(), pixelDecision.getFinalPixelX(), pixelDecision.getFinalPixelY(),
                    compareRemarks);
            logger.info("设备[{}] 本次geo_pixel_compare_data已正常入库，remarks写入内容：{}",
                    deviceId, compareRemarks);

            // 2. 转换数据格式
            Map<String, Object> helmetData = dataConverter.convert(messageData, locationResult);

            // 3. 保存数据到TDengine
//            saveDataToTDengine(deviceId, helmetData);
            //保存电量：helmet_runde_ca_report_location
            this.saveHelmetRundeData(messageData.getDeviceId(),messageData,helmetData);

            logger.info("TCP消息处理完成, 设备ID: {}, 定位状态: {}",
                    deviceId,
                    locationResult != null ? locationResult.get("engine_status") : "未调用");

        } catch (Exception e) {
            logger.error("处理TCP消息时发生异常, 设备ID: {}", deviceId, e);

            // 即使处理失败，也尝试保存基础数据
            try {
                Map<String, Object> basicData = dataConverter.convert(messageData, null);
                basicData.put("engine_status", "processing_error");
                basicData.put("engine_exception_message", e.getMessage());
                saveDataToTDengine(deviceId, basicData);
                logger.info("已保存基础数据, 设备ID: {}", deviceId);
            } catch (Exception saveException) {
                logger.error("保存基础数据也失败, 设备ID: {}", deviceId, saveException);
            }
        }
    }

    /**
     * external_coordinate_data 只有在最终选中GEO像素时，才切换到经纬度链时间。
     * BLE 与 BLE_FALLBACK 继续沿用原来的蓝牙扫描时间差逻辑。
     */
    private Long resolveExternalCoordinateTime(TcpMessageData messageData, PixelDecision pixelDecision, String deviceId) {
        String finalSource = pixelDecision == null ? "NONE" : normalizeFinalPixelSource(pixelDecision.getFinalPixelSource());
        if (!"GEO".equals(finalSource)) {
            Long scanTimestamp = messageData != null ? messageData.getScanTimestamp() : null;
            logger.info("设备[{}]external时间沿用旧链路, finalSource={}, scanTimestamp={}",
                    deviceId, finalSource, scanTimestamp);
            return scanTimestamp;
        }

        if (messageData != null && messageData.getGpsUtcTimestamp() != null) {
            logger.info("设备[{}]external时间切到经纬度链路, finalSource=GEO, source=gps_utc, gpsUtcRaw={}, timestamp={}",
                    deviceId, messageData.getGpsUtcRaw(), messageData.getGpsUtcTimestamp());
            return messageData.getGpsUtcTimestamp();
        }

        Long currentTime = System.currentTimeMillis();
        logger.info("设备[{}]external时间切到经纬度链路, finalSource=GEO, source=current_time, gpsUtcRaw={}, timestamp={}",
                deviceId, messageData != null ? messageData.getGpsUtcRaw() : null, currentTime);
        return currentTime;
    }

    /**
     * external 拦截规则只在"当前没BLE坐标且当前最终来源是GEO"时生效。
     * 即使拦截 external，也不影响 geo_pixel_compare_data 的内容和条数。
     */
    private boolean shouldSkipExternalCoordinateWrite(String deviceId,
            Map<String, Object> bleRawLocationResult,
            PixelDecision pixelDecision,
            Long currentBusinessTimestamp) {
        // 先按"当前包有没有 BLE 原始坐标"做第一层过滤。
        Double currentBleX = parseDouble(bleRawLocationResult != null ? bleRawLocationResult.get("x") : null);
        Double currentBleY = parseDouble(bleRawLocationResult != null ? bleRawLocationResult.get("y") : null);
        boolean currentBleValid = hasValidPixel(currentBleX, currentBleY);

        // 第二层只认当前最终来源是不是 GEO，别的来源维持原样。
        String finalSource = pixelDecision == null ? "NONE" : normalizeFinalPixelSource(pixelDecision.getFinalPixelSource());
        if (currentBleValid || !"GEO".equals(finalSource)) {
            logger.info("设备[{}] 准备写入external_coordinate_data前进行拦截判断：{}，本次最终选择{}，不满足'当前无BLE且最终为GEO'的拦截条件，所以继续执行external_coordinate_data写入流程。",
                    deviceId, buildBleCoordinateDescription(currentBleValid), describePixelSource(finalSource));
            return false;
        }

        // 第三层再回看最近1分钟，只要历史里有 BLE / BLE_FALLBACK 就拦住这次 GEO。
        boolean recentBleExists = geoPixelCompareDataService.hasRecentBleSourceForTcp(deviceId, currentBusinessTimestamp);
        if (!recentBleExists) {
            logger.info("设备[{}] 准备写入external_coordinate_data前进行拦截判断：当前包未算出BLE坐标，本次最终选择GEO。回看最近{}分钟的geo_pixel_compare_data后，未发现BLE/BLE_FALLBACK记录，所以继续写入external_coordinate_data。",
                    deviceId, RECENT_BLE_LOOKBACK_MILLIS / 1000 / 60);
            return false;
        }

        logger.info("设备[{}] 准备写入external_coordinate_data前进行拦截判断：当前包未算出BLE坐标，本次最终选择GEO。回看最近{}分钟的geo_pixel_compare_data后，发现存在BLE/BLE_FALLBACK记录，所以本次不写入external_coordinate_data。",
                deviceId, RECENT_BLE_LOOKBACK_MILLIS / 1000 / 60);
        return true;
    }

    /**
     * 用中文摘要说明这次最终到底选了哪条坐标链路。
     */
    private void logFinalPixelDecisionSummary(String deviceId,
            Map<String, Object> bleRawLocationResult,
            PixelDecision pixelDecision) {
        boolean currentBleValid = hasValidPixel(
                parseDouble(bleRawLocationResult != null ? bleRawLocationResult.get("x") : null),
                parseDouble(bleRawLocationResult != null ? bleRawLocationResult.get("y") : null));
        String finalSource = pixelDecision == null ? "NONE" : normalizeFinalPixelSource(pixelDecision.getFinalPixelSource());
        String finalX = pixelDecision == null ? "null" : String.valueOf(pixelDecision.getFinalPixelX());
        String finalY = pixelDecision == null ? "null" : String.valueOf(pixelDecision.getFinalPixelY());
        logger.info("设备[{}] 本次定位最终选择{}，最终坐标为({}, {})，{}。",
                deviceId, describePixelSource(finalSource), finalX, finalY, buildBleCoordinateDescription(currentBleValid));
    }

    /**
     * 把来源码翻成中文，日志里直接给人看。
     */
    private String describePixelSource(String finalSource) {
        if ("BLE".equalsIgnoreCase(finalSource)) {
            return "BLE坐标";
        }
        if ("GEO".equalsIgnoreCase(finalSource)) {
            return "GEO坐标";
        }
        if ("BLE_FALLBACK".equalsIgnoreCase(finalSource)) {
            return "BLE_FALLBACK坐标";
        }
        return "无有效坐标";
    }

    /**
     * 把"当前包有没有BLE坐标"翻成中文描述。
     */
    private String buildBleCoordinateDescription(boolean currentBleValid) {
        if (currentBleValid) {
            return "当前包已算出BLE坐标";
        }
        return "当前包未算出BLE坐标";
    }

    /**
     * 把 external 的真实结果翻成对比表 remarks 文案。
     */
    private String buildCompareRemarks(PixelDecision pixelDecision,
            boolean skipExternal,
            LocationEngineService.ExternalWriteResult externalWriteResult) {
        String finalSource = pixelDecision == null ? "NONE" : normalizeFinalPixelSource(pixelDecision.getFinalPixelSource());
        String sourceText = describePixelSourceCode(finalSource);

        if (skipExternal) {
            return "本次原本准备按GEO写入external_coordinate_data，但因最近1分钟内存在BLE/BLE_FALLBACK记录，已被拦截，最终未写入external_coordinate_data。";
        }

        if (externalWriteResult == null) {
            return "本次原本准备写入external_coordinate_data，但未拿到写入结果，最终未确认是否写入external_coordinate_data。";
        }

        if (externalWriteResult.isWritten()) {
            return "本次已写入external_coordinate_data，写入来源为" +
                    describePixelSourceCode(externalWriteResult.getSource()) + "。";
        }

        if (StringUtils.equals("定位结果为空。", externalWriteResult.getReason())) {
            return "本次未写入external_coordinate_data，原因：定位结果为空。";
        }
        if (StringUtils.equals("坐标无效。", externalWriteResult.getReason())) {
            return "本次未写入external_coordinate_data，原因：坐标无效。";
        }

        return "本次原本准备按" + sourceText + "写入external_coordinate_data，但执行写入时失败，最终未写入external_coordinate_data。";
    }

    /**
     * remarks 里来源不带"坐标"二字，读起来更顺。
     */
    private String describePixelSourceCode(String finalSource) {
        if ("BLE".equalsIgnoreCase(finalSource)) {
            return "BLE";
        }
        if ("GEO".equalsIgnoreCase(finalSource)) {
            return "GEO";
        }
        if ("BLE_FALLBACK".equalsIgnoreCase(finalSource)) {
            return "BLE_FALLBACK";
        }
        return "未知来源";
    }

    /**
     * 保存helmet_runde_ca_report_location表数据
     */
    public R<JSONObject> saveHelmetRundeData(String deviceId, TcpMessageData messageData,Map<String, Object> helmetData) {
        R<JSONObject> result = helmetService.saveHelmetRundeData(deviceId, messageData, helmetData);
        return result;
    }

    /**
     * 根据需要调用定位引擎
     *
     * @param messageData TCP消息数据
     * @return 定位引擎结果，如果没有蓝牙数据则返回null
     */
    private Map<String, Object> callLocationEngineIfNeeded(TcpMessageData messageData) {
        List<Map<String, Object>> bluetoothBeacons = messageData.getBluetoothBeacons();
        String deviceId = messageData.getDeviceId();

        if (bluetoothBeacons == null || bluetoothBeacons.isEmpty()) {
            logger.warn("设备[{}]没有蓝牙信标数据，跳过定位引擎调用", deviceId);
            return buildEngineSkippedResult("no_beacon_data", 0, 0, 0, "无蓝牙信标数据");
        }

        try {
            // 转换蓝牙信标数据格式为定位引擎所需格式
            List<Map<String, Object>> engineBeaconData = convertBeaconDataForEngine(bluetoothBeacons);
            int originalCount = bluetoothBeacons.size();

            if (engineBeaconData.isEmpty()) {
                logger.warn("设备[{}]转换后的蓝牙信标数据为空，跳过定位引擎调用", deviceId);
                return buildEngineSkippedResult("no_valid_beacons", originalCount, 0, 0, "转换后无有效信标");
            }

            // 过滤有效的beacon数据（只保留MAC地址在缓存中的）
            List<Map<String, Object>> filteredBeaconData = filterValidBeaconData(deviceId, engineBeaconData);
            int filteredCount = filteredBeaconData.size();
            int rssiGtMinus85Count = countRssiGreaterThan(filteredBeaconData, -85);

            // 如果过滤后没有有效的beacon数据，返回特定状态
            if (filteredBeaconData.isEmpty()) {
                logger.warn("设备[{}]过滤后没有有效的beacon数据（MAC地址都不在缓存中），跳过定位引擎调用", deviceId);
                Map<String, Object> result = new java.util.HashMap<>();
                result.put("engine_status", "no_valid_beacons");
                result.put("http_status_code", 0);
                result.put("http_response_body", "所有信标MAC地址都不在缓存中");
                result.put("original_beacon_count", originalCount);
                result.put("filtered_beacon_count", 0);
                result.put("rssi_gt_minus85_count", 0);
                result.put("location_engine_request_body", "未生成请求（无有效信标）");
                return result;
            }

            // 调用定位引擎，传递过滤后的数据和扫描时间戳（不再传全局上下文）
            // @author Shawn @date 2026-04-09
            // 打印设备原始消息，便于排查第三方算法返回异常时定位问题
            // @author Shawn @date 2026-04-17
            logger.info("设备[{}] 调用定位引擎前原始消息: {}", deviceId, messageData.getRawMessage());
            Map<String, Object> locationResult = locationEngineService.callLocationEngine(deviceId, filteredBeaconData,
                    messageData.getScanTimestamp(), messageData.getRawMessage());
            if (locationResult == null) {
                locationResult = new java.util.HashMap<>();
            }

            // 添加原始和过滤后的数量信息
            locationResult.put("original_beacon_count", originalCount);
            locationResult.put("filtered_beacon_count", filteredCount);
            locationResult.put("rssi_gt_minus85_count", rssiGtMinus85Count);

            logger.info("定位引擎调用完成, 设备ID: {}, 状态: {}, 原始信标数: {}, 过滤后信标数: {}",
                    deviceId,
                    locationResult.get("engine_status"),
                    originalCount,
                    filteredCount);

            return locationResult;

        } catch (Exception e) {
            logger.error("调用定位引擎时发生异常, 设备ID: {}", deviceId, e);

            // 返回异常信息
            Map<String, Object> errorResult = new java.util.HashMap<>();
            errorResult.put("engine_status", "call_exception");
            errorResult.put("exception_message", e.getMessage());
            errorResult.put("http_status_code", 0);
            errorResult.put("http_response_body", "");
            errorResult.put("original_beacon_count", bluetoothBeacons.size());
            errorResult.put("filtered_beacon_count", 0);
            errorResult.put("rssi_gt_minus85_count", 0);
            errorResult.put("location_engine_request_body", "调用异常");

            return errorResult;
        }
    }

    /**
     * 构建"跳过定位引擎"场景的统一返回值。
     * 统一带上信标统计字段，便于后续像素来源决策。
     */
    private Map<String, Object> buildEngineSkippedResult(String engineStatus,
            int originalCount,
            int filteredCount,
            int rssiGtMinus85Count,
            String responseBody) {
        Map<String, Object> result = new java.util.HashMap<>();
        result.put("engine_status", engineStatus);
        result.put("http_status_code", 0);
        result.put("http_response_body", responseBody);
        result.put("original_beacon_count", originalCount);
        result.put("filtered_beacon_count", filteredCount);
        result.put("rssi_gt_minus85_count", rssiGtMinus85Count);
        result.put("location_engine_request_body", "未调用定位引擎");
        return result;
    }

    /**
     * 统计"RSSI > threshold"的信标数量。
     * 这里按已过滤可定位信标统计，严格执行你的门槛规则。
     */
    private int countRssiGreaterThan(List<Map<String, Object>> beaconDataList, int threshold) {
        if (beaconDataList == null || beaconDataList.isEmpty()) {
            return 0;
        }

        int count = 0;
        for (Map<String, Object> beaconData : beaconDataList) {
            Integer rssi = parseRssi(beaconData);
            if (rssi == null) {
                continue;
            }
            if (rssi > threshold) {
                count++;
            }
        }
        return count;
    }

    /**
     * 解析信标RSSI。
     */
    private Integer parseRssi(Map<String, Object> beaconData) {
        if (beaconData == null) {
            return null;
        }

        Object rssiObj = beaconData.get("RSSI");
        if (rssiObj == null) {
            return null;
        }

        try {
            return Integer.parseInt(rssiObj.toString().trim());
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 按"当前包先判室内/室外，再做5票防抖"的规则选择最终像素。
     */
    static void applyFinalPixelByRule(Map<String, Object> locationResult,
            SiteMapMatchService.MapPixelMatchResult mapPixelMatchResult,
            String deviceId) {
        applyFinalPixelByRule(locationResult, mapPixelMatchResult, deviceId, System.currentTimeMillis(),
                Collections.emptyList());
    }

    static PixelDecision applyFinalPixelByRule(Map<String, Object> locationResult,
            SiteMapMatchService.MapPixelMatchResult mapPixelMatchResult,
            String deviceId,
            Long currentBusinessTimestamp,
            List<GeoPixelCompareDataService.RecentDecisionRecord> recentDecisionRecords) {
        if (locationResult == null) {
            return PixelDecision.none(0, false);
        }

        // 这里同时拿到 BLE 原始像素和 GEO 投影像素，后续只回写最终稳定输出。
        Double bleRawX = parseDouble(locationResult.get("x"));
        Double bleRawY = parseDouble(locationResult.get("y"));
        Double geoPixelX = mapPixelMatchResult != null ? mapPixelMatchResult.getCalcPixelX() : null;
        Double geoPixelY = mapPixelMatchResult != null ? mapPixelMatchResult.getCalcPixelY() : null;

        int rssiGtMinus85Count = parseInt(locationResult.get("rssi_gt_minus85_count"), 0);
        boolean currentIndoorCandidate = isIndoorCandidate(locationResult);
        boolean blePixelValid = hasValidPixel(bleRawX, bleRawY);
        boolean geoPixelValid = hasValidPixel(geoPixelX, geoPixelY);
        VoteSummary voteSummary = buildVoteSummary(currentIndoorCandidate, currentBusinessTimestamp,
                recentDecisionRecords);
        String finalScene = resolveFinalScene(currentIndoorCandidate, voteSummary);

        logger.info("设备[{}]像素防抖决策开始, currentIndoorCandidate={}, currentBusinessTimestamp={}, historyUsedCount={}, " +
                        "historyDiscardedByTimeoutCount={}, historyIndoorVoteCount={}, historyOutdoorVoteCount={}, " +
                        "indoorVoteCount={}, outdoorVoteCount={}, finalScene={}, blePixelValid={}, geoPixelValid={}, " +
                        "rssi_gt_minus85_count={}",
                deviceId, currentIndoorCandidate, currentBusinessTimestamp, voteSummary.getHistoryUsedCount(),
                voteSummary.getHistoryDiscardedByTimeoutCount(), voteSummary.getHistoryIndoorVoteCount(),
                voteSummary.getHistoryOutdoorVoteCount(), voteSummary.getIndoorVoteCount(),
                voteSummary.getOutdoorVoteCount(), finalScene, blePixelValid, geoPixelValid, rssiGtMinus85Count);

        PixelDecision pixelDecision = decideByFinalScene(locationResult, mapPixelMatchResult, bleRawX, bleRawY,
                blePixelValid, currentIndoorCandidate, geoPixelValid, rssiGtMinus85Count, finalScene);

        logger.info("设备[{}]像素防抖决策完成, finalScene={}, finalSource={}, finalX={}, finalY={}",
                deviceId, finalScene, pixelDecision.getFinalPixelSource(),
                pixelDecision.getFinalPixelX(), pixelDecision.getFinalPixelY());
        return pixelDecision;
    }

    static void applyGeoPixelOverride(Map<String, Object> locationResult,
            SiteMapMatchService.MapPixelMatchResult mapPixelMatchResult) {
        if (locationResult == null || mapPixelMatchResult == null) {
            return;
        }

        locationResult.put("x", mapPixelMatchResult.getCalcPixelX());
        locationResult.put("y", mapPixelMatchResult.getCalcPixelY());
        locationResult.put("pixel_source", "GEO");

        clearBleSpatialMetadata(locationResult);

        // GEO像素仅用于回写最终坐标，external_coordinate_data 先不落 map_id。
        locationResult.remove("mapId");
        if (mapPixelMatchResult.getMapName() != null) {
            locationResult.put("mapName", mapPixelMatchResult.getMapName());
        }
    }

    static void clearBleSpatialMetadata(Map<String, Object> locationResult) {
        if (locationResult == null) {
            return;
        }
        locationResult.remove("address");
        locationResult.remove("nearestBeacon");
        locationResult.remove("usedBeacons");
    }

    /**
     * 只要有1个可定位信标 RSSI > -85，就把当前包视为"室内候选"。
     */
    private static boolean isIndoorCandidate(Map<String, Object> locationResult) {
        int rssiGtMinus85Count = parseInt(locationResult.get("rssi_gt_minus85_count"), 0);
        return rssiGtMinus85Count >= 1;
    }

    /**
     * 先根据最终稳定态选主路径，再走对应兜底。
     */
    private static PixelDecision decideByFinalScene(Map<String, Object> locationResult,
            SiteMapMatchService.MapPixelMatchResult mapPixelMatchResult,
            Double bleRawX,
            Double bleRawY,
            boolean blePixelValid,
            boolean currentIndoorCandidate,
            boolean geoPixelValid,
            int rssiGtMinus85Count,
            String finalScene) {
        if (FINAL_SCENE_INDOOR.equals(finalScene)) {
            return decideIndoorScene(locationResult, mapPixelMatchResult, bleRawX, bleRawY,
                    blePixelValid, currentIndoorCandidate, geoPixelValid, rssiGtMinus85Count);
        }
        return decideOutdoorScene(locationResult, mapPixelMatchResult, bleRawX, bleRawY,
                blePixelValid, currentIndoorCandidate, geoPixelValid, rssiGtMinus85Count);
    }

    /**
     * 室内稳定态优先保住 BLE，不让单条抖动轻易把室内链路切走。
     */
    private static PixelDecision decideIndoorScene(Map<String, Object> locationResult,
            SiteMapMatchService.MapPixelMatchResult mapPixelMatchResult,
            Double bleRawX,
            Double bleRawY,
            boolean blePixelValid,
            boolean currentIndoorCandidate,
            boolean geoPixelValid,
            int rssiGtMinus85Count) {
        if (blePixelValid) {
            locationResult.put("pixel_source", "BLE");
            return PixelDecision.of("BLE", bleRawX, bleRawY, currentIndoorCandidate, rssiGtMinus85Count);
        }

        if (geoPixelValid) {
            applyGeoPixelOverride(locationResult, mapPixelMatchResult);
            return PixelDecision.of("GEO", parseDouble(locationResult.get("x")), parseDouble(locationResult.get("y")),
                    currentIndoorCandidate, rssiGtMinus85Count);
        }

        return clearFinalPixel(locationResult, rssiGtMinus85Count, currentIndoorCandidate);
    }

    /**
     * 室外稳定态优先 GEO，GEO 缺失时再退回 BLE_FALLBACK。
     */
    private static PixelDecision decideOutdoorScene(Map<String, Object> locationResult,
            SiteMapMatchService.MapPixelMatchResult mapPixelMatchResult,
            Double bleRawX,
            Double bleRawY,
            boolean blePixelValid,
            boolean currentIndoorCandidate,
            boolean geoPixelValid,
            int rssiGtMinus85Count) {
        if (geoPixelValid) {
            applyGeoPixelOverride(locationResult, mapPixelMatchResult);
            return PixelDecision.of("GEO", parseDouble(locationResult.get("x")), parseDouble(locationResult.get("y")),
                    currentIndoorCandidate, rssiGtMinus85Count);
        }

        if (blePixelValid) {
            locationResult.put("pixel_source", "BLE_FALLBACK");
            return PixelDecision.of("BLE_FALLBACK", bleRawX, bleRawY, currentIndoorCandidate, rssiGtMinus85Count);
        }

        return clearFinalPixel(locationResult, rssiGtMinus85Count, currentIndoorCandidate);
    }

    /**
     * 两侧都不可用时清空最终坐标，让后续 external 表写入链路自动跳过。
     */
    private static PixelDecision clearFinalPixel(Map<String, Object> locationResult,
            int rssiGtMinus85Count,
            boolean currentIndoorCandidate) {
        locationResult.remove("x");
        locationResult.remove("y");
        locationResult.put("pixel_source", "NONE");
        return PixelDecision.none(rssiGtMinus85Count, currentIndoorCandidate);
    }

    /**
     * 当前包和最近4条历史一起投票，票数只关心"室内候选 / 室外候选"。
     */
    private static VoteSummary buildVoteSummary(boolean currentIndoorCandidate,
            Long currentBusinessTimestamp,
            List<GeoPixelCompareDataService.RecentDecisionRecord> recentDecisionRecords) {
        int historyIndoorVoteCount = 0;
        int historyOutdoorVoteCount = 0;
        int historyUsedCount = 0;
        int historyDiscardedByTimeoutCount = 0;
        if (recentDecisionRecords == null || recentDecisionRecords.isEmpty()) {
            return VoteSummary.of(currentIndoorCandidate, historyIndoorVoteCount, historyOutdoorVoteCount,
                    historyUsedCount, historyDiscardedByTimeoutCount);
        }

        for (GeoPixelCompareDataService.RecentDecisionRecord record : recentDecisionRecords) {
            if (record == null) {
                continue;
            }
            Long historyBusinessTimestamp = record.getBusinessTimestamp();
            if (isHistoryTimedOut(currentBusinessTimestamp, historyBusinessTimestamp)) {
                historyDiscardedByTimeoutCount++;
                continue;
            }
            Boolean historyIndoorCandidate = record.getIndoorCandidate();
            if (historyIndoorCandidate == null) {
                continue;
            }
            historyUsedCount++;
            if (historyIndoorCandidate) {
                historyIndoorVoteCount++;
                continue;
            }
            historyOutdoorVoteCount++;
        }
        return VoteSummary.of(currentIndoorCandidate, historyIndoorVoteCount, historyOutdoorVoteCount,
                historyUsedCount, historyDiscardedByTimeoutCount);
    }

    private static boolean isHistoryTimedOut(Long currentBusinessTimestamp, Long historyBusinessTimestamp) {
        if (currentBusinessTimestamp == null || historyBusinessTimestamp == null) {
            return true;
        }
        return Math.abs(currentBusinessTimestamp - historyBusinessTimestamp) > HISTORY_GAP_TIMEOUT_MILLIS;
    }

    /**
     * 满足"5票取3票"时按票数定稳定态，否则回退到当前包原始判断。
     */
    private static String resolveFinalScene(boolean currentIndoorCandidate, VoteSummary voteSummary) {
        if (voteSummary.getIndoorVoteCount() >= INDOOR_VOTE_THRESHOLD) {
            return FINAL_SCENE_INDOOR;
        }
        if (voteSummary.getOutdoorVoteCount() >= INDOOR_VOTE_THRESHOLD) {
            return FINAL_SCENE_OUTDOOR;
        }
        return currentIndoorCandidate ? FINAL_SCENE_INDOOR : FINAL_SCENE_OUTDOOR;
    }

    /**
     * 统一归一化历史来源文本，避免不同大小写或空值影响状态机判断。
     */
    private static String normalizeFinalPixelSource(String source) {
        if (source == null) {
            return "NONE";
        }

        String normalized = source.trim().toUpperCase();
        if (normalized.isEmpty()) {
            return "NONE";
        }
        return normalized;
    }

    /**
     * 当前包业务时间和对比表 time 字段保持同一口径：优先GPS UTC，没有就退系统时间。
     */
    private static Long resolveCurrentBusinessTimestamp(TcpMessageData messageData) {
        if (messageData != null && messageData.getGpsUtcTimestamp() != null) {
            return messageData.getGpsUtcTimestamp();
        }
        return System.currentTimeMillis();
    }

    /**
     * 坐标有效性判定：兼容现有 external_coordinate_data 的"x>0 或 y>0"规则。
     */
    private static boolean hasValidPixel(Double x, Double y) {
        if (x == null || y == null) {
            return false;
        }
        return x > 0 || y > 0;
    }

    /**
     * 安全解析Double。
     */
    private static Double parseDouble(Object value) {
        if (value == null) {
            return null;
        }

        String text = value.toString().trim();
        if (text.isEmpty() || "null".equalsIgnoreCase(text)) {
            return null;
        }

        try {
            return Double.parseDouble(text);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 安全解析整数。
     */
    private static int parseInt(Object value, int defaultValue) {
        if (value == null) {
            return defaultValue;
        }

        try {
            return Integer.parseInt(value.toString().trim());
        } catch (Exception e) {
            return defaultValue;
        }
    }

    /**
     * 本次最终决策快照会同时用于 compare 表落库和 external 表写入。
     */
    static final class PixelDecision {
        private final String finalPixelSource;
        private final Double finalPixelX;
        private final Double finalPixelY;
        private final boolean indoorCandidate;
        private final int rssiGtMinus85Count;

        private PixelDecision(String finalPixelSource,
                Double finalPixelX,
                Double finalPixelY,
                boolean indoorCandidate,
                int rssiGtMinus85Count) {
            this.finalPixelSource = finalPixelSource;
            this.finalPixelX = finalPixelX;
            this.finalPixelY = finalPixelY;
            this.indoorCandidate = indoorCandidate;
            this.rssiGtMinus85Count = rssiGtMinus85Count;
        }

        static PixelDecision of(String finalPixelSource,
                Double finalPixelX,
                Double finalPixelY,
                boolean indoorCandidate,
                int rssiGtMinus85Count) {
            return new PixelDecision(finalPixelSource, finalPixelX, finalPixelY, indoorCandidate, rssiGtMinus85Count);
        }

        static PixelDecision none(int rssiGtMinus85Count, boolean indoorCandidate) {
            return new PixelDecision("NONE", null, null, indoorCandidate, rssiGtMinus85Count);
        }

        String getFinalPixelSource() {
            return finalPixelSource;
        }

        Double getFinalPixelX() {
            return finalPixelX;
        }

        Double getFinalPixelY() {
            return finalPixelY;
        }

        boolean isIndoorCandidate() {
            return indoorCandidate;
        }

        int getRssiGtMinus85Count() {
            return rssiGtMinus85Count;
        }
    }

    /**
     * 投票摘要把"历史票"和"总票"拆开记录，方便日志直接打印。
     */
    static final class VoteSummary {
        private final int historyIndoorVoteCount;
        private final int historyOutdoorVoteCount;
        private final int historyUsedCount;
        private final int historyDiscardedByTimeoutCount;
        private final int indoorVoteCount;
        private final int outdoorVoteCount;

        private VoteSummary(int historyIndoorVoteCount,
                int historyOutdoorVoteCount,
                int historyUsedCount,
                int historyDiscardedByTimeoutCount,
                int indoorVoteCount,
                int outdoorVoteCount) {
            this.historyIndoorVoteCount = historyIndoorVoteCount;
            this.historyOutdoorVoteCount = historyOutdoorVoteCount;
            this.historyUsedCount = historyUsedCount;
            this.historyDiscardedByTimeoutCount = historyDiscardedByTimeoutCount;
            this.indoorVoteCount = indoorVoteCount;
            this.outdoorVoteCount = outdoorVoteCount;
        }

        static VoteSummary of(boolean currentIndoorCandidate,
                int historyIndoorVoteCount,
                int historyOutdoorVoteCount,
                int historyUsedCount,
                int historyDiscardedByTimeoutCount) {
            int indoorVoteCount = historyIndoorVoteCount + (currentIndoorCandidate ? 1 : 0);
            int outdoorVoteCount = historyOutdoorVoteCount + (currentIndoorCandidate ? 0 : 1);
            return new VoteSummary(historyIndoorVoteCount, historyOutdoorVoteCount, historyUsedCount,
                    historyDiscardedByTimeoutCount, indoorVoteCount, outdoorVoteCount);
        }

        int getHistoryIndoorVoteCount() {
            return historyIndoorVoteCount;
        }

        int getHistoryOutdoorVoteCount() {
            return historyOutdoorVoteCount;
        }

        int getHistoryUsedCount() {
            return historyUsedCount;
        }

        int getHistoryDiscardedByTimeoutCount() {
            return historyDiscardedByTimeoutCount;
        }

        int getIndoorVoteCount() {
            return indoorVoteCount;
        }

        int getOutdoorVoteCount() {
            return outdoorVoteCount;
        }
    }

    /**
     * 转换蓝牙信标数据为定位引擎所需格式
     * TCP格式：{MAC: "80ECCCD0A8E4", RSSI: "-71", TIME: "011"}
     * 引擎格式：保持相同格式，但确保数据完整性
     *
     * @param tcpBeacons TCP格式的蓝牙信标数据
     * @return 定位引擎格式的信标数据
     */
    private List<Map<String, Object>> convertBeaconDataForEngine(List<Map<String, Object>> tcpBeacons) {
        List<Map<String, Object>> engineBeacons = new java.util.ArrayList<>();

        for (Map<String, Object> beacon : tcpBeacons) {
            try {
                String mac = (String) beacon.get("MAC");
                String rssi = (String) beacon.get("RSSI");
                String time = (String) beacon.get("TIME");

                // 验证数据完整性
                if (mac == null || rssi == null || time == null ||
                        mac.trim().isEmpty() || rssi.trim().isEmpty() || time.trim().isEmpty()) {
                    logger.info("跳过不完整的信标数据: {}", beacon);
                    continue;
                }

                // 创建引擎格式的信标数据
                Map<String, Object> engineBeacon = new java.util.HashMap<>();
                engineBeacon.put("MAC", mac.trim());
                engineBeacon.put("RSSI", rssi.trim());
                engineBeacon.put("TIME", time.trim());

                engineBeacons.add(engineBeacon);

            } catch (Exception e) {
                logger.error("转换信标数据时发生异常: {}", beacon, e);
            }
        }

        logger.info("信标数据转换完成: 原始数量={}, 转换后数量={}", tcpBeacons.size(), engineBeacons.size());
        return engineBeacons;
    }

    /**
     * 保存数据到TDengine
     *
     * @param deviceId   设备ID
     * @param helmetData 转换后的helmet表格式数据
     */
    private void saveDataToTDengine(String deviceId, Map<String, Object> helmetData) {
        try {
            R<JSONObject> result = helmetService.saveHelmetData(deviceId, helmetData);

            if (result.getCode() == R.SUCCESS) {
                logger.info("数据保存成功, 设备ID: {}", deviceId);
            } else {
                logger.error("数据保存失败, 设备ID: {}, 错误信息: {}", deviceId, result.getMsg());
            }

        } catch (Exception e) {
            logger.error("保存数据到TDengine时发生异常, 设备ID: {}", deviceId, e);
            throw e; // 重新抛出异常，让上层处理
        }
    }

    /**
     * 过滤有效的beacon数据，只保留MAC地址在缓存中的数据
     *
     * @param deviceId       设备ID
     * @param beaconDataList 原始beacon数据列表
     * @return 过滤后的beacon数据列表
     */
    private List<Map<String, Object>> filterValidBeaconData(String deviceId, List<Map<String, Object>> beaconDataList) {
        List<Map<String, Object>> filteredList = new java.util.ArrayList<>();

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

            // 提取MAC地址
            String macAddress = extractMacFromBeacon(beaconData);

            if (macAddress != null) {
                // 格式化MAC地址为缓存格式（带冒号）
                String formattedMac = formatMacAddressForCache(macAddress);

                if (formattedMac != null) {
                    // 从缓存中获取完整的BeaconStation对象
                    BeaconStationDO beaconStation = beaconMacAddressCache.getBeaconStation(formattedMac);
                    if (beaconStation != null && "1".equals(beaconStation.getBeaconType())) {
                        // 给每个保留下来的信标填充它自身的楼层/建筑上下文
                        // @author Shawn @date 2026-04-09
                        if (StringUtils.isNotBlank(beaconStation.getFloorId())) {
                            beaconData.put("floorId", beaconStation.getFloorId());
                        }
                        if (StringUtils.isNotBlank(beaconStation.getFloor())) {
                            beaconData.put("floor", beaconStation.getFloor());
                        }
                        if (StringUtils.isNotBlank(beaconStation.getBuildingId())) {
                            beaconData.put("buildingId", beaconStation.getBuildingId());
                        }
                        if (StringUtils.isNotBlank(beaconStation.getBuilding())) {
                            beaconData.put("building", beaconStation.getBuilding());
                        }
                        filteredList.add(beaconData);
                        validCount++;
                        logger.info("设备 {} 的beacon MAC地址 {} (格式化后: {}) 是类型1的信标，保留数据",
                                deviceId, macAddress, formattedMac);
                    } else {
                        invalidCount++;
                        if (beaconStation != null) {
                            logger.info("设备 {} 的beacon MAC地址 {} (格式化后: {}) 的信标类型为 {}，过滤掉数据",
                                    deviceId, macAddress, formattedMac, beaconStation.getBeaconType());
                        } else {
                            logger.info("设备 {} 的beacon MAC地址 {} (格式化后: {}) 不在缓存中，过滤掉数据",
                                    deviceId, macAddress, formattedMac);
                        }
                    }
                } else {
                    invalidCount++;
                    logger.debug("设备 {} 的beacon MAC地址 {} 格式化失败，过滤掉数据", deviceId, macAddress);
                }
            } else {
                invalidCount++;
                logger.debug("设备 {} 的beacon数据中无法提取有效MAC地址，过滤掉数据: {}", deviceId, beaconData);
            }
        }

        logger.info("设备 {} beacon数据过滤完成，原始数量: {}, 有效数量: {}, 无效数量: {}",
                deviceId, originalCount, validCount, invalidCount);

        return filteredList;
    }

    /**
     * 格式化MAC地址为缓存所需格式
     * 从 80ECCCD09BFD 转换为 80:EC:CC:D0:9B:FD
     *
     * @param tcpMac TCP格式的MAC地址（无冒号）
     * @return 标准格式的MAC地址（带冒号），如果格式化失败返回null
     */
    private String formatMacAddressForCache(String tcpMac) {
        if (tcpMac == null || tcpMac.trim().isEmpty()) {
            return null;
        }

        // 移除可能的空格并转为大写
        String cleanMac = tcpMac.trim().toUpperCase();

        // 检查长度是否为12位
        if (cleanMac.length() != 12) {
            logger.debug("MAC地址长度不正确: {}, 期望12位", cleanMac);
            return null;
        }

        // 检查是否都是十六进制字符
        if (!cleanMac.matches("^[0-9A-F]+$")) {
            logger.debug("MAC地址包含非法字符: {}", cleanMac);
            return null;
        }

        // 每两个字符插入一个冒号
        StringBuilder formatted = new StringBuilder();
        for (int i = 0; i < cleanMac.length(); i += 2) {
            if (i > 0) {
                formatted.append(":");
            }
            formatted.append(cleanMac.substring(i, i + 2));
        }

        return formatted.toString();
    }

    /**
     * 从beacon数据中提取MAC地址
     *
     * @param beacon beacon数据
     * @return MAC地址，如果提取失败返回null
     */
    private String extractMacFromBeacon(Map<String, Object> beacon) {
        if (beacon == null) {
            return null;
        }

        Object macValue = beacon.get("MAC");
        if (macValue == null) {
            return null;
        }

        String mac = macValue.toString().trim();
        return mac.isEmpty() ? null : mac;
    }
}
