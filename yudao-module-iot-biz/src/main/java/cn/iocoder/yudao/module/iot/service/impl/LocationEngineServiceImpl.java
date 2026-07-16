package cn.iocoder.yudao.module.iot.service.impl;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONUtil;
import cn.hutool.json.JSONObject;
import cn.iocoder.yudao.framework.common.biz.system.dict.dto.DictDataRespDTO;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.module.iot.cache.DeviceTenantMappingCache;
import cn.iocoder.yudao.module.iot.config.LocationEngineConfig;
import cn.iocoder.yudao.module.iot.service.LocationEngineService;
import cn.iocoder.yudao.module.swm.api.enums.TenantDbEnum;
import cn.iocoder.yudao.module.system.api.dict.DictDataApi;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Location engine service.
 *
 * <p>Chat algorithm base URLs are resolved from RuoYi dict type {@code corp_code_chat_base_url}
 * using the tenant's legacy project code. If the algorithm call fails, the service returns a local
 * BLE fallback result based on the strongest beacon.
 */
@Slf4j
@Service
@Validated
public class LocationEngineServiceImpl implements LocationEngineService {

    private static final String CHAT_URL_DICT_TYPE = "corp_code_chat_base_url";

    @Resource
    private LocationEngineConfig locationEngineConfig;
    @Resource
    private DeviceTenantMappingCache deviceTenantMappingCache;
    @Resource
    private DictDataApi dictDataApi;

    @Override
    public Object getCoordinate(Object... args) {
        if (args == null || args.length < 2 || !(args[1] instanceof List<?> list)) {
            return buildSkippedResult("invalid_args", 0, "Invalid coordinate arguments");
        }
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> beacons = (List<Map<String, Object>>) list;
        return callLocationEngine(String.valueOf(args[0]), beacons, null);
    }

    @Override
    public Map<String, Object> callLocationEngine(String deviceId, List<Map<String, Object>> beaconDataList, Long scanTimestamp) {
        return callLocationEngine(deviceId, beaconDataList, scanTimestamp, null);
    }

    @Override
    public Map<String, Object> callLocationEngine(String deviceId, List<Map<String, Object>> beaconDataList,
                                                  Long scanTimestamp, String rawMessage) {
        if (beaconDataList == null || beaconDataList.isEmpty()) {
            return buildSkippedResult("no_valid_beacons", 0, "No valid beacon data");
        }

        String chatBaseUrl = resolveChatBaseUrl(deviceId);
        if (StrUtil.isNotBlank(chatBaseUrl)) {
            Map<String, Object> chatResult = callChatLocationEngine(deviceId, beaconDataList, scanTimestamp, rawMessage, chatBaseUrl);
            if ("success".equals(chatResult.get("engine_status"))) {
                return chatResult;
            }
            Map<String, Object> fallback = buildBleFallbackResult(deviceId, beaconDataList, scanTimestamp, rawMessage);
            fallback.put("chat_engine_status", chatResult.get("engine_status"));
            fallback.put("chat_http_status_code", chatResult.get("http_status_code"));
            fallback.put("chat_http_response_body", chatResult.get("http_response_body"));
            return fallback;
        }

        return buildBleFallbackResult(deviceId, beaconDataList, scanTimestamp, rawMessage);
    }

    @Override
    public String getEngineType() {
        return StrUtil.isNotBlank(resolveChatBaseUrl(null)) ? "chat" : locationEngineConfig.getEffectiveEngineType();
    }

    @Override
    public boolean isChatEngineAvailable() {
        return StrUtil.isNotBlank(resolveChatBaseUrl(null));
    }

    @Override
    public boolean isHistoryEnabled() {
        return locationEngineConfig.isHistoryEnabled();
    }

    @Override
    public int getHistorySeconds() {
        return locationEngineConfig.getHistorySeconds();
    }

    private Map<String, Object> callChatLocationEngine(String deviceId, List<Map<String, Object>> beaconDataList,
                                                       Long scanTimestamp, String rawMessage, String chatBaseUrl) {
        List<Map<String, Object>> convertedBeacons = convertBeaconData(beaconDataList);
        if (convertedBeacons.isEmpty()) {
            return buildSkippedResult("no_valid_beacon_data", 0, "No valid beacon data after conversion");
        }
        List<Map<String, Object>> deduplicatedBeacons = deduplicateBeaconDataByMac(convertedBeacons);
        String scanTime = DateUtil.format(scanTimestamp == null ? DateUtil.date() : DateUtil.date(scanTimestamp),
                DatePattern.NORM_DATETIME_PATTERN);
        Map<String, Object> requestBody = buildChatRequestBody(deviceId, deduplicatedBeacons, scanTime, rawMessage);
        String jsonRequestBody = JSONUtil.toJsonStr(requestBody);
        String chatUrl = StrUtil.removeSuffix(chatBaseUrl, "/") + locationEngineConfig.getChatEndpoint();

        Map<String, Object> result = new HashMap<>();
        result.put("engine_type", "chat");
        result.put("original_beacon_count", beaconDataList.size());
        result.put("filtered_beacon_count", convertedBeacons.size());
        result.put("location_engine_request_body", jsonRequestBody);
        result.put("algorithm_url", chatUrl);

        try (HttpResponse response = HttpRequest.post(chatUrl)
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .header("User-Agent", "IoT-Helmet-Location-Client/1.0")
                .body(jsonRequestBody)
                .timeout(locationEngineConfig.getChatTimeout())
                .execute()) {
            int statusCode = response.getStatus();
            String responseBody = response.body();
            result.put("http_status_code", statusCode);
            result.put("http_response_body", truncate(responseBody));
            if (statusCode != 200) {
                result.put("engine_status", "http_error");
                return result;
            }
            if (StrUtil.isBlank(responseBody)) {
                result.put("engine_status", "empty_response");
                return result;
            }
            return parseChatResponse(deviceId, responseBody, result);
        } catch (Exception ex) {
            log.warn("Call Chat location engine failed, deviceId={}, url={}", deviceId, chatUrl, ex);
            result.put("engine_status", "exception");
            result.put("exception_type", ex.getClass().getSimpleName());
            result.put("exception_message", ex.getMessage());
            result.put("http_status_code", 0);
            result.put("http_response_body", "");
            return result;
        }
    }

    private Map<String, Object> parseChatResponse(String deviceId, String responseBody, Map<String, Object> result) {
        try {
            JSONObject response = JSONUtil.parseObj(responseBody);
            JSONObject data = response;
            if (response.containsKey("code") && response.containsKey("data") && response.get("data") != null) {
                int code = response.getInt("code", 0);
                if (code != 200) {
                    result.put("engine_status", "business_error");
                    result.put("business_code", code);
                    result.put("business_message", response.getStr("message", response.getStr("msg", "")));
                    return result;
                }
                data = response.getJSONObject("data");
            }

            copyIfPresent(data, result, "x", "y", "address", "mapId", "orgCd", "type", "appId",
                    "warningId", "time", "elderId", "nearestBeacon", "usedBeacons",
                    "floorId", "floor", "buildingId", "building");
            result.put("chat_success", true);
            result.put("engine_status", "success");
            log.info("Chat location engine succeeded, deviceId={}, x={}, y={}", deviceId, result.get("x"), result.get("y"));
            return result;
        } catch (Exception ex) {
            result.put("engine_status", "json_parse_error");
            result.put("parse_error", ex.getMessage());
            return result;
        }
    }

    private Map<String, Object> buildBleFallbackResult(String deviceId, List<Map<String, Object>> beaconDataList,
                                                       Long scanTimestamp, String rawMessage) {
        Map<String, Object> nearestBeacon = beaconDataList.stream()
                .filter(item -> StrUtil.isNotBlank(StrUtil.toStringOrNull(item.get("MAC"))))
                .max(Comparator.comparingInt(this::parseRssi))
                .orElse(null);
        if (nearestBeacon == null) {
            return buildSkippedResult("no_valid_beacons", beaconDataList.size(), "No beacon MAC data");
        }

        Double x = parseDouble(firstPresent(nearestBeacon, "x", "X", "lng", "longitude"));
        Double y = parseDouble(firstPresent(nearestBeacon, "y", "Y", "lat", "latitude"));

        Map<String, Object> result = new HashMap<>();
        result.put("engine_type", "ble_fallback");
        result.put("engine_status", x != null || y != null ? "ble_fallback" : "no_coordinate");
        result.put("x", x == null ? 0D : x);
        result.put("y", y == null ? 0D : y);
        result.put("nearest_beacon", nearestBeacon.get("MAC"));
        result.put("nearest_beacon_rssi", nearestBeacon.get("RSSI"));
        result.put("scan_timestamp", scanTimestamp == null ? System.currentTimeMillis() : scanTimestamp);
        result.put("http_status_code", 0);
        result.put("http_response_body", "external provider not available; used local BLE fallback");
        result.put("location_engine_request_body", JSONUtil.toJsonStr(Map.of(
                "deviceId", deviceId,
                "rawMessage", StrUtil.blankToDefault(rawMessage, ""),
                "beacons", beaconDataList)));
        log.info("Location fallback finished, deviceId={}, status={}, nearestBeacon={}",
                deviceId, result.get("engine_status"), result.get("nearest_beacon"));
        return result;
    }

    private String resolveChatBaseUrl(String deviceId) {
        Long tenantId = deviceTenantMappingCache.getTenantId(deviceId);
        String tenantCode = TenantDbEnum.getTenantCodeByTenantId(tenantId);
        if (StrUtil.isNotBlank(tenantCode)) {
            try {
                for (DictDataRespDTO item : dictDataApi.getDictDataList(CHAT_URL_DICT_TYPE)) {
                    if (item != null
                            && Objects.equals(item.getStatus(), CommonStatusEnum.ENABLE.getStatus())
                            && Objects.equals(item.getValue(), tenantCode)
                            && StrUtil.isNotBlank(item.getLabel())) {
                        return item.getLabel();
                    }
                }
            } catch (Exception ex) {
                log.warn("Resolve Chat algorithm URL from dict failed, tenantId={}, tenantCode={}", tenantId, tenantCode, ex);
            }
        }
        return locationEngineConfig.getChatBaseUrl();
    }

    private Map<String, Object> buildChatRequestBody(String deviceId, List<Map<String, Object>> beaconDataList,
                                                     String scanTime, String rawMessage) {
        Map<String, Object> request = new LinkedHashMap<>();
        request.put("MESSAGE", StrUtil.blankToDefault(rawMessage, ""));
        request.put("SCANCOUNT", String.valueOf(beaconDataList.size()));
        request.put("HWTYPE", "RD_SAFTY_TAG");
        request.put("HWID", deviceId);
        request.put("VERSION", "1.0");
        request.put("oldCoordinate", null);
        request.put("SCANTIME", scanTime);
        request.put("SCANDATA", beaconDataList);
        return request;
    }

    private List<Map<String, Object>> convertBeaconData(List<Map<String, Object>> beaconDataList) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (Map<String, Object> beacon : beaconDataList) {
            String mac = StrUtil.toStringOrNull(beacon.get("MAC"));
            if (StrUtil.isBlank(mac)) {
                continue;
            }
            Map<String, Object> converted = new LinkedHashMap<>();
            converted.put("BATTERY", StrUtil.toString(beacon.getOrDefault("BATTERY", "4")));
            converted.put("RSSI", StrUtil.toString(beacon.getOrDefault("RSSI", "")));
            converted.put("DATA", StrUtil.toString(beacon.getOrDefault("DATA", "")));
            converted.put("MAC", formatMacAddress(mac));
            copyIfPresent(beacon, converted, "floorId", "floor", "buildingId", "building");
            result.add(converted);
        }
        return result;
    }

    private List<Map<String, Object>> deduplicateBeaconDataByMac(List<Map<String, Object>> beaconDataList) {
        Map<String, Map<String, Object>> macToBeacon = new LinkedHashMap<>();
        for (Map<String, Object> beacon : beaconDataList) {
            String mac = StrUtil.toStringOrNull(beacon.get("MAC"));
            if (StrUtil.isBlank(mac)) {
                continue;
            }
            Map<String, Object> existing = macToBeacon.get(mac);
            if (existing == null || parseRssi(beacon) > parseRssi(existing)) {
                macToBeacon.put(mac, beacon);
            }
        }
        return new ArrayList<>(macToBeacon.values());
    }

    private Map<String, Object> buildSkippedResult(String status, int beaconCount, String message) {
        Map<String, Object> result = new HashMap<>();
        result.put("engine_type", "chat");
        result.put("engine_status", status);
        result.put("x", 0D);
        result.put("y", 0D);
        result.put("http_status_code", 0);
        result.put("http_response_body", message);
        result.put("filtered_beacon_count", beaconCount);
        result.put("location_engine_request_body", "request not generated: " + message);
        return result;
    }

    private void copyIfPresent(Map<String, Object> source, Map<String, Object> target, String... keys) {
        for (String key : keys) {
            Object value = source.get(key);
            if (value != null) {
                target.put(key, value);
            }
        }
    }

    private void copyIfPresent(JSONObject source, Map<String, Object> target, String... keys) {
        for (String key : keys) {
            if (source.containsKey(key)) {
                target.put(key, source.get(key));
            }
        }
    }

    private int parseRssi(Map<String, Object> beacon) {
        Object value = beacon.get("RSSI");
        if (value == null) {
            value = beacon.get("rssi");
        }
        try {
            return Integer.parseInt(String.valueOf(value));
        } catch (Exception ex) {
            return Integer.MIN_VALUE;
        }
    }

    private String formatMacAddress(String mac) {
        String cleanMac = mac.replaceAll("[:\\s]", "").toUpperCase();
        if (cleanMac.length() != 12) {
            return mac;
        }
        StringBuilder formatted = new StringBuilder();
        for (int i = 0; i < cleanMac.length(); i += 2) {
            if (i > 0) {
                formatted.append(":");
            }
            formatted.append(cleanMac.substring(i, i + 2).toLowerCase());
        }
        return formatted.toString();
    }

    private Object firstPresent(Map<String, Object> map, String... keys) {
        for (String key : keys) {
            Object value = map.get(key);
            if (value != null) {
                return value;
            }
        }
        return null;
    }

    private Double parseDouble(Object value) {
        if (value == null || !NumberUtil.isNumber(String.valueOf(value))) {
            return null;
        }
        return Double.parseDouble(String.valueOf(value));
    }

    private String truncate(String value) {
        if (value == null || value.length() <= 2900) {
            return StrUtil.blankToDefault(value, "");
        }
        return value.substring(0, 2900) + "...[truncated]";
    }
}
