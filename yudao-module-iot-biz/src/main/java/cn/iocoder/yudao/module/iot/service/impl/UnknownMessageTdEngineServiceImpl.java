package cn.iocoder.yudao.module.iot.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.iocoder.yudao.module.iot.service.TDengineService;
import cn.iocoder.yudao.module.iot.service.UnknownMessageTdEngineService;
import cn.iocoder.yudao.module.iot.util.R;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Persists unclassified IOT messages into TDengine.
 */
@Slf4j
@Service
public class UnknownMessageTdEngineServiceImpl implements UnknownMessageTdEngineService {

    private static final String SUPER_TABLE_NAME = "iot_unknown_message";

    @Resource
    private TDengineService tdengineService;

    @Value("${tdengine.dbname}")
    private String dbname;

    @Override
    public R<JSONObject> saveUnknownMessageData(String deviceId, String sessionId, String messageContent) {
        String safeDeviceId = sanitizeTag(StrUtil.blankToDefault(deviceId, "unknown"));
        String safeSessionId = sanitizeTag(StrUtil.blankToDefault(sessionId, "unknown"));
        String subTableName = SUPER_TABLE_NAME + "_" + safeDeviceId + "_" + safeSessionId;

        R<JSONObject> superTableResult = ensureSuperTable(deviceId);
        if (superTableResult.getCode() == R.FAIL) {
            return superTableResult;
        }

        String createSubTableSql = String.format(
                "create table if not exists %s.%s using %s.%s tags ('%s', '%s')",
                dbname, subTableName, dbname, SUPER_TABLE_NAME, escapeSql(safeDeviceId), escapeSql(safeSessionId));
        R<JSONObject> subTableResult = tdengineService.executeTDengineSQLByDeviceId(createSubTableSql, deviceId);
        if (subTableResult.getCode() == R.FAIL) {
            return subTableResult;
        }

        String insertSql = String.format(
                "insert into %s.%s (time, message_content) values (%d, '%s')",
                dbname, subTableName, System.currentTimeMillis(), escapeSql(messageContent));
        return tdengineService.executeTDengineSQLByDeviceId(insertSql, deviceId);
    }

    @Override
    public String getUnknownMessageSuperTableName() {
        return SUPER_TABLE_NAME;
    }

    private R<JSONObject> ensureSuperTable(String deviceId) {
        String sql = "create stable if not exists " + dbname + "." + SUPER_TABLE_NAME
                + " (time TIMESTAMP, message_content NCHAR(4096))"
                + " TAGS (device_id NCHAR(64), session_id NCHAR(128))";
        return tdengineService.executeTDengineSQLByDeviceId(sql, deviceId);
    }

    private String sanitizeTag(String value) {
        return value.replaceAll("[^a-zA-Z0-9_\\-]", "_");
    }

    private String escapeSql(String value) {
        return StrUtil.blankToDefault(value, "").replace("'", "''");
    }

}
