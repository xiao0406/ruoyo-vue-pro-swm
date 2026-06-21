package cn.iocoder.yudao.module.iot.mqtt.handler.weigou;

import cn.hutool.core.date.DateUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;
import cn.iocoder.yudao.module.iot.cache.service.RedisService;
import cn.iocoder.yudao.module.swm.api.constant.SwmRedisKeyConstants;
import cn.iocoder.yudao.module.iot.dal.dao.HelmetDeviceDao;
import cn.iocoder.yudao.module.iot.enums.MqttSendPositionParamEnum;
import cn.iocoder.yudao.module.iot.mqtt.handler.MqttBusinessHandler;
import cn.iocoder.yudao.module.iot.service.*;
import cn.iocoder.yudao.module.iot.util.R;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 定位数据
 * 处理设备定位MQTT消息
 *
 * @author fangxiaolong
 * @date 2026-01-13
 */
@Component
@ConditionalOnProperty(prefix = "iot.mqtt", name = "enabled", havingValue = "true")
@Slf4j
public class DevicePositionMessageHandler implements MqttBusinessHandler {

    private static final Logger logger = LoggerFactory.getLogger(DevicePositionMessageHandler.class);

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

    // MQTT 设备位置告警超级表名称
    private static final String MQTT_POSITION_SUPER_TABLE_NAME = "mqtt_device_position";


    @Override
    public boolean canHandle(String topic, MqttMessage message) {
        try {
            // 检查主题是否匹配
            if (!topic.contains(MqttSendPositionParamEnum.POSITION_PARAM_ENUM_POSITION.name())) {
                return false;
            }
            String payload = new String(message.getPayload(), StandardCharsets.UTF_8);
            // 高频消息避免 stdout 输出；必要时可开启 debug 查看截断内容
            if (log.isDebugEnabled()) {
                String p = payload.length() > 500 ? payload.substring(0, 500) + "..." : payload;
                log.debug("mqtt 定位 - canHandle 命中 topic={}, payload={}", topic, p);
            }
            return true;

        } catch (Exception e) {
            logger.debug("判断是否为定位定位数据消息时发生异常: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public void handle(String topic, MqttMessage message) throws Exception {
       long startTime = System.currentTimeMillis();
        String sessionId = "mqtt_" + System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 8);
        String payload = null;
        String deviceId = null;

        try {
            // 1. 解析 MQQT 消息
            payload = new String(message.getPayload(), StandardCharsets.UTF_8);
           log.info("mqtt 定位 - 开始处理 MQTT 消息 - sessionId: {}, topic: {}, payload: {}", sessionId, topic, payload);

            // 2. 解析 JSON 数据
            JSONObject mqttData = JSONUtil.parseObj(payload);
            deviceId = mqttData.getStr("deviceId");

            // 校验必填字段
            if (deviceId == null || deviceId.trim().isEmpty()) {
                throw new IllegalArgumentException("MQTT 消息中 deviceId 不能为空");
            }

            // 3. 构建告警数据并保存到 mqtt_device_position 表
            saveToMqttPositionAlarmTable(deviceId, mqttData, sessionId);

           log.info("mqtt 定位-MQTT 消息处理完成 - sessionId: {}, deviceId: {}, 耗时：{}ms",
                    sessionId, deviceId, System.currentTimeMillis() - startTime);

        } catch (Exception e) {
           log.error("mqtt 定位 - 处理 MQTT 消息异常 - sessionId: {}, topic: {}, error: {}",
                    sessionId, topic, e.getMessage(), e);

            // 保存失败数据到未知消息表
            saveToUnknownMessageTable(deviceId, sessionId, null, "处理 MQTT 消息异常：" + e.getMessage());

            // 抛出异常（根据业务需求决定是否抛出）
            throw e;
        }
    }


    /**
     * 保存到 mqtt_device_position_alarm 表
     */
   private void saveToMqttPositionAlarmTable(String deviceId, JSONObject mqttData, String sessionId) {
        try {
            // 查询设备绑定的身份证 ID（优先从 Redis 获取）
            String cardId = (String) redisService.hget(SwmRedisKeyConstants.GlobalKey.DEVICE_PERSON_MAP, deviceId);
            String assignedPerson = StringUtils.isNotBlank(cardId) ? cardId : null;

            // 如果 Redis 中没有，尝试从数据库获取
            if (assignedPerson == null) {
                assignedPerson = helmetDeviceDao.getAssignedPersonByDeviceId(deviceId);
            }

           log.info("mqtt 定位 - 设备 {} 绑定的身份证 ID: {}", deviceId, assignedPerson);

            // 创建增强的数据 Map
            Map<String, Object> enhancedDataMap = new HashMap<>();
            enhancedDataMap.put("device_id", deviceId);
            enhancedDataMap.put("alarm_time", DateUtil.date().getTime());

            // 添加身份证 ID 作为 tag
            if (assignedPerson != null && !assignedPerson.trim().isEmpty()) {
                enhancedDataMap.put("id_card", assignedPerson);
               log.info("mqtt 定位 - 已将身份证 ID {} 添加到设备 {} 的 tags 中", assignedPerson, deviceId);
            } else {
               log.warn("mqtt 定位 - 设备 {} 未找到绑定的身份证 ID，使用空值", deviceId);
                enhancedDataMap.put("id_card", "");
            }

            // 添加 MQTT 告警数据中的其他字段
            enhancedDataMap.put("id", mqttData.getInt("id"));
            enhancedDataMap.put("name", mqttData.getStr("name"));
            enhancedDataMap.put("phone_number", mqttData.getStr("phoneNumber"));
            enhancedDataMap.put("sync_id", mqttData.getStr("syncId"));
            enhancedDataMap.put("lng", mqttData.getStr("lng"));
            enhancedDataMap.put("lat", mqttData.getStr("lat"));
            enhancedDataMap.put("floor_id", mqttData.getStr("floorId"));
            enhancedDataMap.put("identity_card_input", mqttData.getStr("identityCard"));

            // 直接尝试插入数据到子表
            String subTableName = generateSubTableName(deviceId, assignedPerson);
            R<cn.hutool.json.JSONObject> insertResult = insertAlarmDataToSubTable(subTableName, enhancedDataMap, deviceId);

            // 如果插入失败且错误信息包含表不存在，则创建表后重试
            if (insertResult.getCode() == R.FAIL && isTableNotExistError(insertResult.getMsg())) {
               log.info("mqtt 定位 - 检测到表不存在错误，开始创建表并重试插入数据");

                // 创建超级表
                R<JSONObject> createSuperTableResult = ensureMqttPositionAlarmSuperTableExists(enhancedDataMap, deviceId);
                if (createSuperTableResult.getCode() == R.FAIL) {
                   log.error("mqtt 定位 - 创建 MQTT 位置告警超级表失败，将数据保存到未知消息表");
                    saveToUnknownMessageTable(deviceId, sessionId, enhancedDataMap,
                            "创建 MQTT 位置告警超级表失败：" + createSuperTableResult.getMsg());
                   return;
                }

                // 创建子表
                R<JSONObject> createSubTableResult = ensureMqttPositionAlarmSubTableExists(subTableName, deviceId,
                        assignedPerson != null ? assignedPerson : "");
                if (createSubTableResult.getCode() == R.FAIL) {
                   log.error("mqtt 定位 - 创建 MQTT 位置告警子表失败，将数据保存到未知消息表");
                    saveToUnknownMessageTable(deviceId, sessionId, enhancedDataMap,
                            "创建 MQTT 位置告警子表失败：" + createSubTableResult.getMsg());
                   return;
                }

                // 重试插入数据
                insertResult = insertAlarmDataToSubTable(subTableName, enhancedDataMap, deviceId);
            }

            // 如果最终插入仍然失败，保存到未知消息表
            if (insertResult.getCode() == R.FAIL) {
               log.error("mqtt 定位-MQTT 设备位置告警数据入库失败，将数据保存到未知消息表");
                saveToUnknownMessageTable(deviceId, sessionId, enhancedDataMap,
                        "MQTT 设备位置告警数据入库失败：" + insertResult.getMsg());
            } else {
               log.info("mqtt 定位-MQTT 位置告警数据保存成功 - deviceId: {}, lng: {}, lat: {}, floorId: {}",
                        deviceId, mqttData.getDouble("lng"), mqttData.getDouble("lat"), mqttData.get("floorId"));
            }

        } catch (Exception e) {
           log.error("mqtt 定位 - 保存 MQTT 设备位置告警数据异常 - deviceId: {}", deviceId, e);
            try {
                saveToUnknownMessageTable(deviceId, sessionId, null,
                        "保存 MQTT 设备位置告警数据异常：" + e.getMessage());
            } catch (Exception ex) {
               log.error("mqtt 定位 - 保存到未知消息表也失败了", ex);
            }
        }
    }

    /**
     * 生成复合子表名称，包含设备 ID 和身份证信息
     *
     * @param deviceId 设备 ID
     * @param idCard   身份证 ID
     * @return 子表名称
     */
    private String generateSubTableName(String deviceId, String idCard) {
        // 清理身份证 ID 中的特殊字符，只保留字母和数字
        String safeIdCard = (idCard != null && !idCard.trim().isEmpty())
                ? idCard.replaceAll("[^a-zA-Z0-9]", "_")
                : "unknown";
        return "mqtt_device_position_" + deviceId + "_" + safeIdCard;
    }

    /**
     * 判断是否为表不存在的错误
     *
     * @param errorMsg 错误信息
     * @return true 表示是表不存在错误
     */
    private boolean isTableNotExistError(String errorMsg) {
        if (StringUtils.isBlank(errorMsg)) {
            return false;
        }
        String lowerErrorMsg = errorMsg.toLowerCase();
        return lowerErrorMsg.contains("table does not exist") ||
                lowerErrorMsg.contains("table doesn't exist") ||
                lowerErrorMsg.contains("stable does not exist") ||
                lowerErrorMsg.contains("stable doesn't exist") ||
                lowerErrorMsg.contains("不存在");
    }

    /**
     * 确保 MQTT 位置告警超级表存在
     *
     * @param sampleData 样本数据用于确定字段类型
     * @param deviceId   设备 ID
     * @return 创建结果
     */
   private R<JSONObject> ensureMqttPositionAlarmSuperTableExists(Map<String, Object> sampleData, String deviceId) {
        try {
            // 检查超级表是否存在
            String checkSql = "show " + dbname + ".stables like '" + MQTT_POSITION_SUPER_TABLE_NAME + "'";
            R<JSONObject> checkResult = tdengineService.executeTDengineSQLByDeviceId(checkSql, deviceId);

            if (checkResult.getCode() == R.SUCCESS) {
                JSONObject resultData = checkResult.getData();
                JSONArray dataArray = resultData.getJSONArray("data");
                if (dataArray != null && dataArray.size() > 0) {
                    for (int i = 0; i < dataArray.size(); i++) {
                        JSONArray row = dataArray.getJSONArray(i);
                        if (row != null && row.size() > 0) {
                            String tableName = row.getStr(0);
                            if (MQTT_POSITION_SUPER_TABLE_NAME.equals(tableName)) {
                               log.info("mqtt 定位-MQTT 位置告警超级表已存在：{}", tableName);
                               return R.ok();
                            }
                        }
                    }
                }
            }

            // 超级表不存在，创建超级表
            StringBuilder createSuperTableSql = new StringBuilder("create stable if not exists ")
                    .append(dbname).append(".").append(MQTT_POSITION_SUPER_TABLE_NAME)
                    .append(" (time TIMESTAMP");

            // 添加数据字段（排除 device_id 和 id_card，因为它们是标签）
            sampleData.forEach((key, value) -> {
                if (!"device_id".equals(key) && !"id_card".equals(key)) {
                    String fieldName = key.toLowerCase();
                    // 重命名可能冲突的字段
                    if ("time".equals(fieldName) || "timestamp".equals(fieldName)) {
                        fieldName = fieldName + "_1";
                    }
                    createSuperTableSql.append(",").append(fieldName).append(" ").append(getColumnType(value));
                }
            });

            // 添加标签定义
            createSuperTableSql.append(") TAGS (device_id NCHAR(50), id_card NCHAR(50))");

           log.info("mqtt 定位 - 创建 MQTT 位置告警超级表 SQL: {}", createSuperTableSql.toString());
            R<JSONObject> result = tdengineService.executeTDengineSQLByDeviceId(createSuperTableSql.toString(), deviceId);

            if (result.getCode() == R.SUCCESS) {
               log.info("mqtt 定位-MQTT 位置告警超级表创建成功：{}", MQTT_POSITION_SUPER_TABLE_NAME);
            } else {
               log.error("mqtt 定位-MQTT 位置告警超级表创建失败：{}", result.getMsg());
            }

           return result;

        } catch (Exception e) {
           log.error("mqtt 定位 - 确保 MQTT 位置告警超级表存在时发生异常", e);
           return R.fail("创建 MQTT 位置告警超级表失败：" + e.getMessage());
        }
    }

    /**
     * 确保 MQTT 位置告警子表存在
     *
     * @param subTableName 子表名称
     * @param deviceId     设备 ID
     * @param idCard       身份证 ID
     * @return 创建结果
     */
   private R<JSONObject> ensureMqttPositionAlarmSubTableExists(String subTableName, String deviceId, String idCard) {
        try {
            // 检查子表是否存在
            String checkSql = "show " + dbname + ".tables like '" + subTableName + "'";
            R<JSONObject> checkResult = tdengineService.executeTDengineSQLByDeviceId(checkSql, deviceId);

            if (checkResult.getCode() == R.SUCCESS) {
                JSONObject resultData = checkResult.getData();
                JSONArray dataArray = resultData.getJSONArray("data");
                if (dataArray != null && dataArray.size() > 0) {
                   log.info("mqtt 定位-MQTT 位置告警子表已存在：{}", subTableName);
                   return R.ok();
                }
            }

            // 子表不存在，创建子表
            String createSubTableSql = String.format(
                    "create table if not exists %s.%s using %s.%s TAGS ('%s', '%s')",
                    dbname, subTableName, dbname, MQTT_POSITION_SUPER_TABLE_NAME, deviceId, idCard);

           log.info("mqtt 定位 - 创建 MQTT 位置告警子表 SQL: {}", createSubTableSql);
            R<JSONObject> result = tdengineService.executeTDengineSQLByDeviceId(createSubTableSql, deviceId);

            if (result.getCode() == R.SUCCESS) {
               log.info("mqtt 定位-MQTT 位置告警子表创建成功：{}, 设备 ID: {}, 身份证 ID: {}", subTableName, deviceId, idCard);
            } else {
               log.error("mqtt 定位-MQTT 位置告警子表创建失败：{}, 错误：{}", subTableName, result.getMsg());
            }

           return result;

        } catch (Exception e) {
           log.error("mqtt 定位 - 确保 MQTT 位置告警子表存在时发生异常，subTableName: {}", subTableName, e);
           return R.fail("创建 MQTT 位置告警子表失败：" + e.getMessage());
        }
    }

    /**
     * 插入告警数据到子表
     *
     * @param subTableName 子表名称
     * @param dataMap      数据 Map
     * @param deviceId     设备 ID
     * @return 插入结果
     */
   private R<JSONObject> insertAlarmDataToSubTable(String subTableName, Map<String, Object> dataMap, String deviceId) {
        try {
            StringBuilder insertSqlBuilder = new StringBuilder("insert into ")
                    .append(dbname).append(".").append(subTableName).append(" (time,");

            StringBuilder fieldBuilder = new StringBuilder();
            StringBuilder valBuilder = new StringBuilder().append(DateUtil.date().getTime()).append(",");

            // 排除 device_id 和 id_card，因为它们是标签
            dataMap.forEach((k, v) -> {
                if (!"device_id".equals(k) && !"id_card".equals(k)) {
                    String fieldName = k.toLowerCase();
                    // 重命名可能冲突的字段
                    if ("time".equals(fieldName) || "timestamp".equals(fieldName)) {
                        fieldName = fieldName + "_1";
                    }

                    fieldBuilder.append(fieldName).append(",");
                    if (v != null && !isNumeric(String.valueOf(v))) {
                        valBuilder.append("'").append(v).append("',");
                    } else {
                        valBuilder.append(v).append(",");
                    }
                }
            });

            String field = fieldBuilder.substring(0, fieldBuilder.length() - 1) + ")";
            String val = valBuilder.substring(0, valBuilder.length() - 1) + ")";
            String insertSql = insertSqlBuilder.append(field).append(" values (").append(val).toString();

           log.info("mqtt 定位-MQTT 位置告警数据插入 SQL: {}", insertSql);
            R<JSONObject> result = tdengineService.executeTDengineSQLByDeviceId(insertSql, deviceId);

            if (result.getCode() == R.SUCCESS) {
               log.info("mqtt 定位-MQTT 位置告警数据插入成功，子表：{}", subTableName);
               return R.ok(result.getData());
            } else {
               log.error("mqtt 定位-MQTT 位置告警数据插入失败，子表：{}, 错误：{}", subTableName, result.getMsg());
               return R.fail(result.getMsg());
            }

        } catch (Exception e) {
           log.error("mqtt 定位 - 插入 MQTT 位置告警数据到子表异常，subTableName: {}", subTableName, e);
           return R.fail("插入 MQTT 位置告警数据失败：" + e.getMessage());
        }
    }

    /**
     * 获取列类型
     *
     * @param value 值
     * @return 列类型
     */
    private String getColumnType(Object value) {
        if (value instanceof String) {
            String strValue = (String) value;
            int length = strValue != null ? strValue.length() : 0;

            // 根据实际数据长度动态设置字段大小
            if (length <= 20) {
               return "NCHAR(50)";       // 短字符串（如 floor_id, phone_number 等）
            } else if (length <= 100) {
               return "NCHAR(200)";      // 中等字符串（如 sync_id 等）
            } else if (length <= 500) {
               return "NCHAR(1000)";     // 较长字符串
            } else {
               return "NCHAR(2000)";     // 长字符串（最大限制）
            }
        } else if (value instanceof Integer) {
           return "INT";
        } else if (value instanceof Long) {
           return "BIGINT";
        } else if (value instanceof Float) {
           return "FLOAT";
        } else if (value instanceof Double) {
           return "DOUBLE";
        } else if (value instanceof Boolean) {
           return "BOOL";
        } else {
           return "NCHAR(200)"; // 默认字符串字段长度
        }
    }

    /**
     * 判断是否为数字类型
     *
     * @param strV 输入字符串
     * @return true 表示是数字
     */
    private boolean isNumeric(String strV) {
        if (strV == null || strV.trim().isEmpty()) {
            return false;
        }
        try {
            Double.parseDouble(strV.trim());
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * 保存到未知消息表（失败数据）
     */
   private void saveToUnknownMessageTable(String deviceId, String sessionId, Map<String, Object> alarmData, String errorMsg) {
        try {
            String sessionIdForLog = sessionId != null ? sessionId : ("mqtt_" + System.currentTimeMillis());

            // 构建消息内容
            StringBuilder messageContent = new StringBuilder();
            messageContent.append("MQTT 设备位置告警数据入库失败\n");
            messageContent.append("错误信息：").append(errorMsg).append("\n");
            messageContent.append("设备 ID: ").append(deviceId).append("\n");
            messageContent.append("会话 ID: ").append(sessionIdForLog).append("\n");

            // 如果有告警数据，添加到消息内容中
            if (alarmData != null && !alarmData.isEmpty()) {
                cn.hutool.json.JSONObject jsonData = new cn.hutool.json.JSONObject(alarmData);
                messageContent.append("数据内容：").append(jsonData.toString()).append("\n");
            }

            R<cn.hutool.json.JSONObject> result = unknownMessageTdEngineService.saveUnknownMessageData(deviceId, sessionIdForLog,
                    messageContent.toString());
            if (result.getCode() == R.SUCCESS) {
               log.info("mqtt 定位 - 保存到未知消息表成功 - sessionId: {}, deviceId: {}", sessionIdForLog, deviceId);
            } else {
               log.error("mqtt 定位 - 保存到未知消息表失败 - sessionId: {}, deviceId: {}, error: {}",
                        sessionIdForLog, deviceId, result.getMsg());
            }
        } catch (Exception e) {
           log.error("mqtt 定位 - 保存到未知消息表异常 - sessionId: {}, deviceId: {}", sessionId, deviceId, e);
        }
    }

    // 日期格式化工具
    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    static {
        sdf.setTimeZone(TimeZone.getDefault());
    }


}
