package cn.iocoder.yudao.module.iot.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.iocoder.yudao.module.iot.service.TDengineService;
import cn.iocoder.yudao.module.iot.util.R;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Shared TDengine writer for migrated helmet message services.
 *
 * <p>Several JeeSite services used separate dynamic table builders. During the RuoYi migration we
 * keep a stable JSON payload table per message type and use device/session tags for lookup.
 */
@Component
public class TdengineJsonPayloadWriter {

    @Resource
    private TDengineService tdengineService;

    @Value("${tdengine.dbname}")
    private String dbname;

    public R<JSONObject> savePayload(String stableName, String deviceId, String sessionId, Map<String, Object> data) {
        return savePayload(stableName, deviceId, sessionId, JSONUtil.toJsonStr(data));
    }

    public R<JSONObject> savePayload(String stableName, String deviceId, String sessionId, String payload) {
        if (StrUtil.isBlank(deviceId)) {
            return R.fail("deviceId cannot be blank");
        }
        String safeDeviceId = sanitizeTag(deviceId);
        String safeSessionId = sanitizeTag(StrUtil.blankToDefault(sessionId, "default"));
        String subTableName = stableName + "_" + safeDeviceId + "_" + safeSessionId;

        String createStableSql = "create stable if not exists " + dbname + "." + stableName
                + " (time TIMESTAMP, payload NCHAR(4096))"
                + " TAGS (device_id NCHAR(64), session_id NCHAR(128))";
        R<JSONObject> stableResult = tdengineService.executeTDengineSQLByDeviceId(createStableSql, deviceId);
        if (stableResult.getCode() == R.FAIL) {
            return stableResult;
        }

        String createSubTableSql = String.format(
                "create table if not exists %s.%s using %s.%s tags ('%s', '%s')",
                dbname, subTableName, dbname, stableName, escapeSql(deviceId), escapeSql(StrUtil.blankToDefault(sessionId, "")));
        R<JSONObject> subTableResult = tdengineService.executeTDengineSQLByDeviceId(createSubTableSql, deviceId);
        if (subTableResult.getCode() == R.FAIL) {
            return subTableResult;
        }

        String insertSql = String.format(
                "insert into %s.%s (time, payload) values (%d, '%s')",
                dbname, subTableName, System.currentTimeMillis(), escapeSql(payload));
        return tdengineService.executeTDengineSQLByDeviceId(insertSql, deviceId);
    }

    private String sanitizeTag(String value) {
        return StrUtil.blankToDefault(value, "unknown").replaceAll("[^a-zA-Z0-9_\\-]", "_");
    }

    private String escapeSql(String value) {
        return StrUtil.blankToDefault(value, "").replace("'", "''");
    }
}
