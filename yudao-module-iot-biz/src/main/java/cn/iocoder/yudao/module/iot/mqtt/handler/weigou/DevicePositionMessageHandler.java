package cn.iocoder.yudao.module.iot.mqtt.handler.weigou;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.iocoder.yudao.module.iot.cache.service.RedisService;
import cn.iocoder.yudao.module.iot.dal.dao.HelmetDeviceDao;
import cn.iocoder.yudao.module.iot.enums.MqttSendPositionParamEnum;
import cn.iocoder.yudao.module.iot.mqtt.handler.MqttBusinessHandler;
import cn.iocoder.yudao.module.iot.service.TDengineService;
import cn.iocoder.yudao.module.iot.service.UnknownMessageTdEngineService;
import cn.iocoder.yudao.module.iot.util.R;
import cn.iocoder.yudao.module.swm.api.constant.SwmRedisKeyConstants;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Handles Weigou MQTT device position messages.
 */
@Slf4j
@Component
@ConditionalOnProperty(prefix = "iot.mqtt", name = "enabled", havingValue = "true")
public class DevicePositionMessageHandler implements MqttBusinessHandler {

    private static final String MQTT_POSITION_SUPER_TABLE_NAME = "mqtt_device_position";

    @Resource
    private HelmetDeviceDao helmetDeviceDao;
    @Resource
    private TDengineService tdengineService;
    @Resource
    private UnknownMessageTdEngineService unknownMessageTdEngineService;
    @Resource
    private RedisService redisService;

    @Value("${tdengine.dbname}")
    private String dbname;

    @Override
    public boolean canHandle(String topic, MqttMessage message) {
        try {
            if (!topic.contains(MqttSendPositionParamEnum.POSITION_PARAM_ENUM_POSITION.name())) {
                return false;
            }
            if (log.isDebugEnabled()) {
                String payload = new String(message.getPayload(), StandardCharsets.UTF_8);
                log.debug("MQTT position handler matched topic={}, payload={}", topic, StrUtil.maxLength(payload, 500));
            }
            return true;
        } catch (Exception e) {
            log.debug("Failed to check MQTT position message", e);
            return false;
        }
    }

    @Override
    public void handle(String topic, MqttMessage message) throws Exception {
        long startTime = System.currentTimeMillis();
        String sessionId = "mqtt_" + System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 8);
        String deviceId = null;

        try {
            String payload = new String(message.getPayload(), StandardCharsets.UTF_8);
            log.info("Start handling MQTT position message, sessionId={}, topic={}", sessionId, topic);

            JSONObject mqttData = JSONUtil.parseObj(payload);
            deviceId = mqttData.getStr("deviceId");
            if (StringUtils.isBlank(deviceId)) {
                throw new IllegalArgumentException("MQTT position message deviceId cannot be blank");
            }

            saveToMqttPositionTable(deviceId, mqttData, sessionId);
            log.info("MQTT position message handled, sessionId={}, deviceId={}, cost={}ms",
                    sessionId, deviceId, System.currentTimeMillis() - startTime);
        } catch (Exception e) {
            log.error("Failed to handle MQTT position message, sessionId={}, topic={}, error={}",
                    sessionId, topic, e.getMessage(), e);
            saveToUnknownMessageTable(deviceId, sessionId, null,
                    "Failed to handle MQTT position message: " + e.getMessage());
            throw e;
        }
    }

    private void saveToMqttPositionTable(String deviceId, JSONObject mqttData, String sessionId) {
        try {
            String cardId = (String) redisService.hget(SwmRedisKeyConstants.GlobalKey.DEVICE_PERSON_MAP, deviceId);
            String assignedPerson = StringUtils.isNotBlank(cardId) ? cardId : helmetDeviceDao.getAssignedPersonByDeviceId(deviceId);

            Map<String, Object> data = new HashMap<>();
            data.put("device_id", deviceId);
            data.put("id_card", StringUtils.defaultString(assignedPerson));
            data.put("alarm_time", DateUtil.date().getTime());
            data.put("id", mqttData.getInt("id"));
            data.put("name", mqttData.getStr("name"));
            data.put("phone_number", mqttData.getStr("phoneNumber"));
            data.put("sync_id", mqttData.getStr("syncId"));
            data.put("lng", mqttData.getStr("lng"));
            data.put("lat", mqttData.getStr("lat"));
            data.put("floor_id", mqttData.getStr("floorId"));
            data.put("identity_card_input", mqttData.getStr("identityCard"));

            String subTableName = generateSubTableName(deviceId, assignedPerson);
            R<JSONObject> insertResult = insertAlarmDataToSubTable(subTableName, data, deviceId);
            if (insertResult.getCode() != R.FAIL) {
                return;
            }

            R<JSONObject> superTableResult = ensureMqttPositionSuperTableExists(data, deviceId);
            if (superTableResult.getCode() == R.FAIL) {
                saveToUnknownMessageTable(deviceId, sessionId, data,
                        "Failed to create MQTT position super table: " + superTableResult.getMsg());
                return;
            }

            R<JSONObject> subTableResult = ensureMqttPositionSubTableExists(subTableName, deviceId,
                    StringUtils.defaultString(assignedPerson));
            if (subTableResult.getCode() == R.FAIL) {
                saveToUnknownMessageTable(deviceId, sessionId, data,
                        "Failed to create MQTT position sub table: " + subTableResult.getMsg());
                return;
            }

            insertResult = insertAlarmDataToSubTable(subTableName, data, deviceId);
            if (insertResult.getCode() == R.FAIL) {
                saveToUnknownMessageTable(deviceId, sessionId, data,
                        "Failed to insert MQTT position data: " + insertResult.getMsg());
            }
        } catch (Exception e) {
            log.error("Failed to save MQTT position data, deviceId={}", deviceId, e);
            saveToUnknownMessageTable(deviceId, sessionId, null,
                    "Failed to save MQTT position data: " + e.getMessage());
        }
    }

    private R<JSONObject> ensureMqttPositionSuperTableExists(Map<String, Object> sampleData, String deviceId) {
        String checkSql = "show " + dbname + ".stables like '" + MQTT_POSITION_SUPER_TABLE_NAME + "'";
        R<JSONObject> checkResult = tdengineService.executeTDengineSQLByDeviceId(checkSql, deviceId);
        if (checkResult.getCode() != R.FAIL && hasRows(checkResult.getData())) {
            return R.ok(checkResult.getData());
        }

        StringBuilder sql = new StringBuilder("create stable if not exists ")
                .append(dbname).append(".").append(MQTT_POSITION_SUPER_TABLE_NAME)
                .append(" (time TIMESTAMP");
        sampleData.forEach((key, value) -> {
            if (!"device_id".equals(key) && !"id_card".equals(key)) {
                sql.append(",").append(normalizeColumnName(key)).append(" ").append(getColumnType(value));
            }
        });
        sql.append(") TAGS (device_id NCHAR(50), id_card NCHAR(50))");
        return tdengineService.executeTDengineSQLByDeviceId(sql.toString(), deviceId);
    }

    private R<JSONObject> ensureMqttPositionSubTableExists(String subTableName, String deviceId, String idCard) {
        String checkSql = "show " + dbname + ".tables like '" + subTableName + "'";
        R<JSONObject> checkResult = tdengineService.executeTDengineSQLByDeviceId(checkSql, deviceId);
        if (checkResult.getCode() != R.FAIL && hasRows(checkResult.getData())) {
            return R.ok(checkResult.getData());
        }

        String sql = String.format("create table if not exists %s.%s using %s.%s tags ('%s', '%s')",
                dbname, subTableName, dbname, MQTT_POSITION_SUPER_TABLE_NAME, escapeSql(deviceId), escapeSql(idCard));
        return tdengineService.executeTDengineSQLByDeviceId(sql, deviceId);
    }

    private R<JSONObject> insertAlarmDataToSubTable(String subTableName, Map<String, Object> dataMap, String deviceId) {
        StringBuilder fields = new StringBuilder("time");
        StringBuilder values = new StringBuilder(String.valueOf(DateUtil.date().getTime()));
        dataMap.forEach((key, value) -> {
            if (!"device_id".equals(key) && !"id_card".equals(key)) {
                fields.append(",").append(normalizeColumnName(key));
                values.append(",").append(toSqlValue(value));
            }
        });

        String sql = "insert into " + dbname + "." + subTableName
                + " (" + fields + ") values (" + values + ")";
        return tdengineService.executeTDengineSQLByDeviceId(sql, deviceId);
    }

    private boolean hasRows(JSONObject result) {
        if (result == null) {
            return false;
        }
        JSONArray dataArray = result.getJSONArray("data");
        return dataArray != null && !dataArray.isEmpty();
    }

    private String generateSubTableName(String deviceId, String idCard) {
        String safeDeviceId = sanitizeIdentifier(deviceId);
        String safeIdCard = StringUtils.isNotBlank(idCard) ? sanitizeIdentifier(idCard) : "unknown";
        return MQTT_POSITION_SUPER_TABLE_NAME + "_" + safeDeviceId + "_" + safeIdCard;
    }

    private String normalizeColumnName(String key) {
        String fieldName = sanitizeIdentifier(key).toLowerCase();
        if ("time".equals(fieldName) || "timestamp".equals(fieldName)) {
            return fieldName + "_1";
        }
        return fieldName;
    }

    private String sanitizeIdentifier(String value) {
        return StringUtils.defaultString(value).replaceAll("[^a-zA-Z0-9_]", "_");
    }

    private String toSqlValue(Object value) {
        if (value == null) {
            return "null";
        }
        if (value instanceof Number || value instanceof Boolean) {
            return String.valueOf(value);
        }
        return "'" + escapeSql(String.valueOf(value)) + "'";
    }

    private String escapeSql(String value) {
        return StringUtils.defaultString(value).replace("'", "''");
    }

    private String getColumnType(Object value) {
        if (value instanceof Integer) {
            return "INT";
        }
        if (value instanceof Long) {
            return "BIGINT";
        }
        if (value instanceof Float) {
            return "FLOAT";
        }
        if (value instanceof Double) {
            return "DOUBLE";
        }
        if (value instanceof Boolean) {
            return "BOOL";
        }
        int length = value == null ? 0 : String.valueOf(value).length();
        if (length <= 20) {
            return "NCHAR(50)";
        }
        if (length <= 100) {
            return "NCHAR(200)";
        }
        if (length <= 500) {
            return "NCHAR(1000)";
        }
        return "NCHAR(2000)";
    }

    private void saveToUnknownMessageTable(String deviceId, String sessionId, Map<String, Object> alarmData, String errorMsg) {
        try {
            StringBuilder content = new StringBuilder(errorMsg)
                    .append("\nDevice ID: ").append(deviceId)
                    .append("\nSession ID: ").append(sessionId);
            if (alarmData != null && !alarmData.isEmpty()) {
                content.append("\nData: ").append(JSONUtil.toJsonStr(alarmData));
            }
            R<JSONObject> result = unknownMessageTdEngineService.saveUnknownMessageData(deviceId, sessionId, content.toString());
            if (result.getCode() == R.FAIL) {
                log.error("Failed to save unknown MQTT position message, sessionId={}, deviceId={}, error={}",
                        sessionId, deviceId, result.getMsg());
            }
        } catch (Exception e) {
            log.error("Failed to save unknown MQTT position message, sessionId={}, deviceId={}", sessionId, deviceId, e);
        }
    }

}
