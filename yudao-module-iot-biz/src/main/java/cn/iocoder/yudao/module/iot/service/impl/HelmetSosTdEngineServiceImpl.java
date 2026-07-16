package cn.iocoder.yudao.module.iot.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.iocoder.yudao.module.iot.cache.service.RedisService;
import cn.iocoder.yudao.module.iot.service.HelmetSosTdEngineService;
import cn.iocoder.yudao.module.iot.service.TDengineService;
import cn.iocoder.yudao.module.iot.service.UnknownMessageTdEngineService;
import cn.iocoder.yudao.module.iot.util.R;
import cn.iocoder.yudao.module.swm.api.constant.SwmRedisKeyConstants;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * RuoYi implementation of the migrated helmet SOS TDengine writer.
 */
@Slf4j
@Service
@Validated
public class HelmetSosTdEngineServiceImpl implements HelmetSosTdEngineService {

    private static final String SUPER_TABLE_NAME = "helmet_ca_sos";

    @Resource
    private TDengineService tdengineService;
    @Resource
    private UnknownMessageTdEngineService unknownMessageTdEngineService;
    @Resource
    private RedisService redisService;

    @Value("${tdengine.dbname}")
    private String dbname;

    @Override
    public R<JSONObject> saveHelmetSosData(String deviceId, Map<String, Object> data) {
        if (StrUtil.isBlank(deviceId)) {
            return R.fail("deviceId cannot be blank");
        }

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("device_id", deviceId);
        payload.put("sos_time", DateUtil.date().getTime());
        if (data != null) {
            data.forEach((key, value) -> {
                if (!"device_id".equals(key) && !"id_card".equals(key)) {
                    payload.put(normalizeColumnName(key), value);
                }
            });
        }

        String idCard = StrUtil.toStringOrNull(redisService.hget(SwmRedisKeyConstants.GlobalKey.DEVICE_PERSON_MAP, deviceId));
        payload.put("id_card", StrUtil.blankToDefault(idCard, ""));
        String subTableName = SUPER_TABLE_NAME + "_" + sanitizeTag(deviceId) + "_"
                + sanitizeTag(StrUtil.blankToDefault(idCard, "unknown"));

        // Create both stable and subtable up front so migrated processors do not silently drop alarms.
        R<JSONObject> ensureResult = ensureTables(deviceId, subTableName, StrUtil.blankToDefault(idCard, ""));
        if (ensureResult.getCode() == R.FAIL) {
            saveFallback(deviceId, payload, ensureResult.getMsg());
            return ensureResult;
        }

        R<JSONObject> insertResult = insertPayload(deviceId, subTableName, payload);
        if (insertResult.getCode() == R.FAIL) {
            saveFallback(deviceId, payload, insertResult.getMsg());
        }
        return insertResult;
    }

    private R<JSONObject> ensureTables(String deviceId, String subTableName, String idCard) {
        String createStableSql = "create stable if not exists " + dbname + "." + SUPER_TABLE_NAME
                + " (time TIMESTAMP, sos_time BIGINT, type INT, act NCHAR(128), bt_signal_info NCHAR(4096))"
                + " TAGS (device_id NCHAR(64), id_card NCHAR(64))";
        R<JSONObject> stableResult = tdengineService.executeTDengineSQLByDeviceId(createStableSql, deviceId);
        if (stableResult.getCode() == R.FAIL) {
            return stableResult;
        }

        String createSubTableSql = String.format(
                "create table if not exists %s.%s using %s.%s tags ('%s', '%s')",
                dbname, subTableName, dbname, SUPER_TABLE_NAME, escapeSql(deviceId), escapeSql(idCard));
        return tdengineService.executeTDengineSQLByDeviceId(createSubTableSql, deviceId);
    }

    private R<JSONObject> insertPayload(String deviceId, String subTableName, Map<String, Object> payload) {
        String sql = String.format(
                "insert into %s.%s (time, sos_time, type, act, bt_signal_info) values (%d, %s, %s, '%s', '%s')",
                dbname,
                subTableName,
                System.currentTimeMillis(),
                numberOrNull(payload.get("sos_time")),
                numberOrNull(payload.get("type")),
                escapeSql(StrUtil.toStringOrNull(payload.get("act"))),
                escapeSql(StrUtil.toStringOrNull(payload.get("bt_signal_info"))));
        return tdengineService.executeTDengineSQLByDeviceId(sql, deviceId);
    }

    private void saveFallback(String deviceId, Map<String, Object> payload, String errorMessage) {
        try {
            JSONObject body = new JSONObject(payload);
            body.set("error_message", errorMessage);
            unknownMessageTdEngineService.saveUnknownMessageData(
                    deviceId, "helmet_sos_" + System.currentTimeMillis(), body.toString());
        } catch (Exception ex) {
            log.error("Failed to save helmet SOS fallback data, deviceId={}", deviceId, ex);
        }
    }

    private String normalizeColumnName(String key) {
        return "timestamp".equalsIgnoreCase(key) ? "timestamp_1" : key.toLowerCase();
    }

    private String numberOrNull(Object value) {
        if (value == null) {
            return "null";
        }
        try {
            Double.parseDouble(String.valueOf(value));
            return String.valueOf(value);
        } catch (NumberFormatException ex) {
            return "null";
        }
    }

    private String sanitizeTag(String value) {
        return StrUtil.blankToDefault(value, "unknown").replaceAll("[^a-zA-Z0-9_\\-]", "_");
    }

    private String escapeSql(String value) {
        return StrUtil.blankToDefault(value, "").replace("'", "''");
    }
}
