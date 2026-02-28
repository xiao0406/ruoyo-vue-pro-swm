package com.jeesite.modules.swm.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.jeesite.modules.constant.TdengineSuperTableConstant;
import com.jeesite.modules.swm.dao.HelmetDeviceDao;
import com.jeesite.modules.swm.service.HelmetRundeCaReportLocationTdEnginService;
import com.jeesite.modules.swm.service.TDengineService;
import com.jeesite.modules.utils.R;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Calendar;

/**
 * 安全帽设备数据服务实现类
 * 集成所有TDengine操作和数据管理功能
 * 
 * @author Shawn
 * @date 2025-01-31
 */
@Slf4j
@Service
public class HelmetRundeCaReportLocationTdEnginServiceImpl implements HelmetRundeCaReportLocationTdEnginService {

    @Autowired
    private HelmetDeviceDao helmetDeviceDao;

    @Autowired
    private TDengineService tdengineService;

    @Value("${tdengine.dbname}")
    private String dbname;

    @Value("${tdengine.retentionPolicy}")
    private String retentionPolicy;



    /**
     * 生成复合子表名称，包含设备ID和身份证信息
     * 
     * @param deviceId 设备ID
     * @param idCard   身份证ID
     * @return 子表名称
     * @author Shawn
     * @date 2025-01-31
     */
    private String generateSubTableName(String deviceId, String idCard) {
        // 清理身份证ID中的特殊字符，只保留字母和数字
        String safeIdCard = (idCard != null && !idCard.trim().isEmpty())
                ? idCard.replaceAll("[^a-zA-Z0-9]", "_")
                : "unknown";
        return TdengineSuperTableConstant.HELMET_RUNDE_CA_REPORT_LOCATION+ "_" + deviceId + "_" + safeIdCard;
    }

    /**
     * 保存安全帽设备数据
     * 
     * @param deviceId 设备ID
     * @param dataMap  设备数据
     * @return 保存结果
     * @author Shawn
     * @date 2025-01-31
     */
    @Override
    public R<cn.hutool.json.JSONObject> saveHelmetData(String deviceId, Map<String, Object> dataMap) {
        log.info("开始保存安全帽设备数据, deviceId: {}", deviceId);

        if (StringUtils.isBlank(deviceId)) {
            return R.fail("设备ID不能为空");
        }

        if (dataMap == null || dataMap.isEmpty()) {
            return R.fail("设备数据不能为空");
        }

        try {
            // 查询设备绑定的身份证ID
            String assignedPerson = helmetDeviceDao.getAssignedPersonByDeviceId(deviceId);
            log.info("设备 {} 绑定的身份证ID: {}", deviceId, assignedPerson);

            // 创建增强的数据Map
            Map<String, Object> enhancedDataMap = new HashMap<>(dataMap);
            enhancedDataMap.put("device_id", deviceId);

            // 添加身份证ID作为tag
            if (assignedPerson != null && !assignedPerson.trim().isEmpty()) {
                enhancedDataMap.put("id_card", assignedPerson);
                log.info("已将身份证ID {} 添加到设备 {} 的tags中", assignedPerson, deviceId);
            } else {
                log.warn("设备 {} 未找到绑定的身份证ID，使用空值", deviceId);
                enhancedDataMap.put("id_card", "");
            }

            // 直接尝试插入数据到子表
            String subTableName = generateSubTableName(deviceId, assignedPerson);
            R<cn.hutool.json.JSONObject> insertResult = insertDataToSubTable(subTableName, enhancedDataMap);

            // 如果插入失败且错误信息包含表不存在，则创建表后重试
            if (insertResult.getCode() == R.FAIL && isTableNotExistError(insertResult.getMsg())) {
                log.info("检测到表不存在错误，开始创建表并重试插入数据");

                // 创建超级表
                R<JSONObject> createSuperTableResult = ensureHelmetSuperTableExists(enhancedDataMap);
                if (createSuperTableResult.getCode() == R.FAIL) {
                    return R.fail("创建超级表失败: " + createSuperTableResult.getMsg());
                }

                // 创建子表
                R<JSONObject> createSubTableResult = ensureHelmetSubTableExists(subTableName, deviceId,
                        assignedPerson != null ? assignedPerson : "");
                if (createSubTableResult.getCode() == R.FAIL) {
                    return R.fail("创建子表失败: " + createSubTableResult.getMsg());
                }

                // 重试插入数据
                insertResult = insertDataToSubTable(subTableName, enhancedDataMap);
            }

            return insertResult;

        } catch (Exception e) {
            log.error("保存安全帽设备数据异常, deviceId: {}", deviceId, e);
            return R.fail("保存失败: " + e.getMessage());
        }
    }

    /**
     * 判断是否为表不存在的错误
     * 
     * @param errorMsg 错误信息
     * @return true表示是表不存在错误
     * @author Shawn
     * @date 2025-01-31
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
     * 确保安全帽超级表存在
     *
     * @param sampleData 样本数据用于确定字段类型
     * @return 创建结果
     * @author Shawn
     * @date 2025-01-04
     */
    private R<JSONObject> ensureHelmetSuperTableExists(Map<String, Object> sampleData) {
        try {
            // 检查超级表是否存在
            String checkSql = "show " + dbname + ".stables like '" + TdengineSuperTableConstant.HELMET_RUNDE_CA_REPORT_LOCATION + "'";
            R<JSONObject> checkResult = tdengineService.executeTDengineSQL(checkSql);

            if (checkResult.getCode() == R.SUCCESS) {
                JSONObject resultData = checkResult.getData();
                JSONArray dataArray = resultData.getJSONArray("data");
                if (dataArray != null && dataArray.size() > 0) {
                    for (int i = 0; i < dataArray.size(); i++) {
                        JSONArray row = dataArray.getJSONArray(i);
                        if (row != null && row.size() > 0) {
                            String tableName = row.getStr(0);
                            if (TdengineSuperTableConstant.HELMET_RUNDE_CA_REPORT_LOCATION.equals(tableName)) {
                                log.info("安全帽超级表已存在: {}", tableName);
                                return R.ok();
                            }
                        }
                    }
                }
            }

            // 超级表不存在，创建超级表
            StringBuilder createSuperTableSql = new StringBuilder("create stable if not exists ")
                    .append(dbname).append(".").append(TdengineSuperTableConstant.HELMET_RUNDE_CA_REPORT_LOCATION).append(" (time TIMESTAMP");

            // 添加固定的x和y坐标字段
//            createSuperTableSql.append(",x DOUBLE");
//            createSuperTableSql.append(",y DOUBLE");

            // 添加数据字段（排除device_id、id_card、x、y，因为device_id和id_card是标签，x和y已经添加）
            sampleData.forEach((key, value) -> {
                if (!"device_id".equals(key) && !"id_card".equals(key) && !"x".equals(key) && !"y".equals(key)) {
                    createSuperTableSql.append(",").append(key.toLowerCase()).append(" ")
                            .append(getColumnType(key, value));
                }
            });

            // 添加标签定义
            createSuperTableSql.append(") TAGS (device_id NCHAR(50), id_card NCHAR(50))");

            log.info("创建安全帽超级表SQL: {}", createSuperTableSql.toString());
            R<JSONObject> result = tdengineService.executeTDengineSQL(createSuperTableSql.toString());

            if (result.getCode() == R.SUCCESS) {
                log.info("安全帽超级表创建成功: {}", TdengineSuperTableConstant.HELMET_RUNDE_CA_REPORT_LOCATION);
            } else {
                log.error("安全帽超级表创建失败: {}", result.getMsg());
            }

            return result;

        } catch (Exception e) {
            log.error("确保安全帽超级表存在时发生异常", e);
            return R.fail("创建超级表失败: " + e.getMessage());
        }
    }

    /**
     * 确保安全帽子表存在
     * 
     * @param subTableName 子表名称
     * @param deviceId     设备ID
     * @param idCard       身份证ID
     * @return 创建结果
     * @author Shawn
     * @date 2025-01-31
     */
    private R<JSONObject> ensureHelmetSubTableExists(String subTableName, String deviceId, String idCard) {
        try {
            // 检查子表是否存在
            String checkSql = "show " + dbname + ".tables like '" + subTableName + "'";
            R<JSONObject> checkResult = tdengineService.executeTDengineSQL(checkSql);

            if (checkResult.getCode() == R.SUCCESS) {
                JSONObject resultData = checkResult.getData();
                JSONArray dataArray = resultData.getJSONArray("data");
                if (dataArray != null && dataArray.size() > 0) {
                    log.info("安全帽子表已存在: {}", subTableName);
                    return R.ok();
                }
            }

            // 子表不存在，创建子表
            String createSubTableSql = String.format(
                    "create table if not exists %s.%s using %s.%s TAGS ('%s', '%s')",
                    dbname, subTableName, dbname, TdengineSuperTableConstant.HELMET_RUNDE_CA_REPORT_LOCATION, deviceId, idCard);

            log.info("创建安全帽子表SQL: {}", createSubTableSql);
            R<JSONObject> result = tdengineService.executeTDengineSQL(createSubTableSql);

            if (result.getCode() == R.SUCCESS) {
                log.info("安全帽子表创建成功: {}, 设备ID: {}, 身份证ID: {}", subTableName, deviceId, idCard);
            } else {
                log.error("安全帽子表创建失败: {}, 错误: {}", subTableName, result.getMsg());
            }

            return result;

        } catch (Exception e) {
            log.error("确保安全帽子表存在时发生异常, subTableName: {}", subTableName, e);
            return R.fail("创建子表失败: " + e.getMessage());
        }
    }

    /**
     * 插入数据到子表
     * 
     * @param subTableName 子表名称
     * @param dataMap      数据Map
     * @return 插入结果
     * @author Shawn
     * @date 2025-01-04
     */
    private R<cn.hutool.json.JSONObject> insertDataToSubTable(String subTableName, Map<String, Object> dataMap) {
        try {
            // 对数据进行预处理，确保字段长度不超出限制
            Map<String, Object> processedDataMap = preprocessDataForInsert(dataMap);

            StringBuilder insertSqlBuilder = new StringBuilder("insert into ")
                    .append(dbname).append(".").append(subTableName).append(" (time,x,y,");

            StringBuilder fieldBuilder = new StringBuilder();
            StringBuilder valBuilder = new StringBuilder().append(DateUtil.date().getTime()).append(",");

            // 添加x和y坐标字段，如果数据中没有则使用默认值0.0
            Double xValue = processedDataMap.containsKey("x") ? Double.valueOf(processedDataMap.get("x").toString())
                    : 0.0;
            Double yValue = processedDataMap.containsKey("y") ? Double.valueOf(processedDataMap.get("y").toString())
                    : 0.0;
            valBuilder.append(xValue).append(",").append(yValue).append(",");

            // 排除device_id、id_card、x、y，因为它们是标签或已经处理
            processedDataMap.forEach((k, v) -> {
                if (!"device_id".equals(k) && !"id_card".equals(k) && !"x".equals(k) && !"y".equals(k)) {
                    k = k.toLowerCase();
                    fieldBuilder.append(k).append(",");
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

            log.info("安全帽数据插入SQL: {}", insertSql);
            R<JSONObject> result = tdengineService.executeTDengineSQL(insertSql);

            if (result.getCode() == R.SUCCESS) {
                log.info("安全帽数据插入成功, 子表: {}", subTableName);
                return R.ok(result.getData());
            } else {
                log.error("安全帽数据插入失败, 子表: {}, 错误: {}", subTableName, result.getMsg());
                return R.fail(result.getMsg());
            }

        } catch (Exception e) {
            log.error("插入数据到子表异常, subTableName: {}", subTableName, e);
            return R.fail("插入数据失败: " + e.getMessage());
        }
    }

    /**
     * 预处理数据，确保字段长度不超出限制
     * 
     * @param dataMap 原始数据Map
     * @return 处理后的数据Map
     * @author Shawn
     * @date 2025-01-04
     */
    private Map<String, Object> preprocessDataForInsert(Map<String, Object> dataMap) {
        Map<String, Object> processedData = new HashMap<>(dataMap);

        processedData.forEach((key, value) -> {
            if (value != null && !isNumeric(value.toString())) {
                String strValue = value.toString();
                String truncatedStr = null;

                // 特殊处理各种字段的长度限制
                switch (key) {
                    case "bt_signal_info":
                        if (strValue.length() > 4000) {
                            truncatedStr = strValue.substring(0, 4000);
                        }
                        break;

                    case "engine_http_response":
                        if (strValue.length() > 3000) {
                            truncatedStr = strValue.substring(0, 3000);
                        }
                        break;

                    case "engine_exception_message":
                    case "engine_parse_error":
                    case "engine_error":
                        if (strValue.length() > 500) {
                            truncatedStr = strValue.substring(0, 500);
                        }
                        break;

                    case "engine_address":
                        if (strValue.length() > 200) {
                            truncatedStr = strValue.substring(0, 200);
                        }
                        break;

                    case "engine_status":
                    case "engine_exception_type":
                    case "phoneNumber":
                    case "tempdata":
                    case "user_id":
                    case "app_version":
                        if (strValue.length() > 100) {
                            truncatedStr = strValue.substring(0, 100);
                        }
                        break;

                    case "device_id":
                    case "id_card":
                        if (strValue.length() > 50) {
                            truncatedStr = strValue.substring(0, 50);
                        }
                        break;

                    default:
                        // 对于其他字符串字段，根据长度动态处理
                        if (strValue.length() > 500) {
                            truncatedStr = strValue.substring(0, 500);
                        } else if (strValue.length() > 200) {
                            truncatedStr = strValue.substring(0, 200);
                        } else if (strValue.length() > 50) {
                            truncatedStr = strValue.substring(0, 50);
                        }
                        break;
                }

                // 如果需要截断，则更新值并记录日志
                if (truncatedStr != null) {
                    processedData.put(key, truncatedStr);
                    log.warn("字段 {} 长度超过限制，已截断。原长度: {}, 截断后长度: {}",
                            key, strValue.length(), truncatedStr.length());
                }
            }
        });

        return processedData;
    }

    /**
     * 根据设备ID获取最新数据
     * 
     * @param deviceId 设备ID
     * @return 最新数据
     * @author Shawn
     * @date 2025-01-31
     */
    @Override
    public R<Map<String, Object>> getLatestData(String deviceId) {
        log.info("获取安全帽设备最新数据, deviceId: {}", deviceId);

        if (StringUtils.isBlank(deviceId)) {
            return R.fail("设备ID不能为空");
        }

        try {
            String sql = String.format("select LAST_ROW(*) from %s.%s where device_id='%s'",
                    dbname, TdengineSuperTableConstant.HELMET_RUNDE_CA_REPORT_LOCATION, deviceId);

            log.info("查询最新数据SQL: {}", sql);
            R<JSONObject> result = tdengineService.executeTDengineSQL(sql);

            if (result.getCode() == R.SUCCESS) {
                List<Map<String, Object>> mapList = processQueryResult(result.getData());
                if (!mapList.isEmpty()) {
                    return R.ok(mapList.get(0));
                }
            }

            return R.fail("未找到设备数据");
        } catch (Exception e) {
            log.error("获取设备最新数据失败, deviceId: {}", deviceId, e);
            return R.fail("获取失败: " + e.getMessage());
        }
    }

    /**
     * 根据设备ID分页查询数据
     * 
     * @param deviceId  设备ID
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @param pageNum   页码
     * @param pageSize  页大小
     * @return 分页数据
     * @author Shawn
     * @date 2025-01-31
     */
    @Override
    public R<Map<String, Object>> getDataByDeviceId(String deviceId, String startTime, String endTime,
            Integer pageNum, Integer pageSize) {
        log.info("分页查询安全帽设备数据, deviceId: {}, pageNum: {}, pageSize: {}", deviceId, pageNum, pageSize);

        if (StringUtils.isBlank(deviceId)) {
            return R.fail("设备ID不能为空");
        }

        if (pageNum == null || pageNum <= 0) {
            pageNum = 1;
        }
        if (pageSize == null || pageSize <= 0) {
            pageSize = 10;
        }

        try {
            StringBuilder sqlBuilder = new StringBuilder("select * from ")
                    .append(dbname).append(".").append(TdengineSuperTableConstant.HELMET_RUNDE_CA_REPORT_LOCATION)
                    .append(" where device_id='").append(deviceId).append("'");

            if (StringUtils.isNotBlank(startTime) && StringUtils.isNotBlank(endTime)) {
                sqlBuilder.append(" and time>='").append(startTime).append("'")
                        .append(" and time<='").append(endTime).append("'");
            }

            sqlBuilder.append(" order by time desc");

            // 计算偏移量
            int offset = (pageNum - 1) * pageSize;
            sqlBuilder.append(" limit ").append(pageSize).append(" offset ").append(offset);

            String sql = sqlBuilder.toString();
            log.info("分页查询SQL: {}", sql);

            R<JSONObject> result = tdengineService.executeTDengineSQL(sql);

            if (result.getCode() == R.SUCCESS) {
                List<Map<String, Object>> rows = processQueryResult(result.getData());

                // 查询总数
                String countSql = String.format("select count(*) from %s.%s where device_id='%s'",
                        dbname, TdengineSuperTableConstant.HELMET_RUNDE_CA_REPORT_LOCATION, deviceId);

                if (StringUtils.isNotBlank(startTime) && StringUtils.isNotBlank(endTime)) {
                    countSql += String.format(" and time>='%s' and time<='%s'", startTime, endTime);
                }

                R<JSONObject> countResult = tdengineService.executeTDengineSQL(countSql);
                long total = 0;
                if (countResult.getCode() == R.SUCCESS) {
                    List<Map<String, Object>> countRows = processQueryResult(countResult.getData());
                    if (!countRows.isEmpty()) {
                        Object countValue = countRows.get(0).values().iterator().next();
                        total = Long.parseLong(countValue.toString());
                    }
                }

                Map<String, Object> resultData = new HashMap<>();
                resultData.put("rows", rows);
                resultData.put("total", total);
                resultData.put("pageNum", pageNum);
                resultData.put("pageSize", pageSize);

                return R.ok(resultData);
            }

            return R.fail("查询失败: " + result.getMsg());
        } catch (Exception e) {
            log.error("分页查询设备数据失败, deviceId: {}", deviceId, e);
            return R.fail("查询失败: " + e.getMessage());
        }
    }

    /**
     * 获取所有安全帽设备列表
     * 
     * @return 设备列表
     * @author Shawn
     * @date 2025-01-31
     */
    @Override
    public R<List<String>> getAllDevices() {
        log.info("获取所有安全帽设备列表");

        try {
            String sql = String.format("select distinct device_id from %s.%s", dbname, TdengineSuperTableConstant.HELMET_RUNDE_CA_REPORT_LOCATION);
            log.info("查询设备列表SQL: {}", sql);

            R<JSONObject> result = tdengineService.executeTDengineSQL(sql);

            if (result.getCode() == R.SUCCESS) {
                List<Map<String, Object>> rows = processQueryResult(result.getData());
                List<String> deviceIds = new ArrayList<>();

                for (Map<String, Object> row : rows) {
                    String deviceId = (String) row.get("device_id");
                    if (deviceId != null && !deviceId.trim().isEmpty()) {
                        deviceIds.add(deviceId);
                    }
                }

                Collections.sort(deviceIds);
                return R.ok(deviceIds);
            }

            return R.fail("查询失败: " + result.getMsg());
        } catch (Exception e) {
            log.error("获取设备列表失败", e);
            return R.fail("获取失败: " + e.getMessage());
        }
    }

    /**
     * 统计安全帽设备数据总数
     * 
     * @param deviceId  设备ID（可选）
     * @param startTime 开始时间（可选）
     * @param endTime   结束时间（可选）
     * @return 数据总数
     * @author Shawn
     * @date 2025-01-31
     */
    @Override
    public R<Long> countData(String deviceId, String startTime, String endTime) {
        log.info("统计安全帽设备数据总数, deviceId: {}", deviceId);

        try {
            StringBuilder sqlBuilder = new StringBuilder("select count(*) from ")
                    .append(dbname).append(".").append(TdengineSuperTableConstant.HELMET_RUNDE_CA_REPORT_LOCATION);

            List<String> conditions = new ArrayList<>();
            if (StringUtils.isNotBlank(deviceId)) {
                conditions.add("device_id='" + deviceId + "'");
            }
            if (StringUtils.isNotBlank(startTime)) {
                conditions.add("time>='" + startTime + "'");
            }
            if (StringUtils.isNotBlank(endTime)) {
                conditions.add("time<='" + endTime + "'");
            }

            if (!conditions.isEmpty()) {
                sqlBuilder.append(" where ").append(String.join(" and ", conditions));
            }

            String sql = sqlBuilder.toString();
            log.info("统计数据SQL: {}", sql);

            R<JSONObject> result = tdengineService.executeTDengineSQL(sql);

            if (result.getCode() == R.SUCCESS) {
                List<Map<String, Object>> rows = processQueryResult(result.getData());
                if (!rows.isEmpty()) {
                    Object countValue = rows.get(0).values().iterator().next();
                    return R.ok(Long.parseLong(countValue.toString()));
                }
            }

            return R.ok(0L);
        } catch (Exception e) {
            log.error("统计数据总数失败, deviceId: {}", deviceId, e);
            return R.fail("统计失败: " + e.getMessage());
        }
    }

    /**
     * 获取安全帽设备统计信息
     * 
     * @return 设备统计信息
     * @author Shawn
     * @date 2025-01-31
     */
    @Override
    public R<Map<String, Object>> getStatistics() {
        log.info("获取安全帽设备统计信息");

        try {
            // 查询设备总数
            String deviceCountSql = String.format("select count(distinct device_id) from %s.%s",
                    dbname, TdengineSuperTableConstant.HELMET_RUNDE_CA_REPORT_LOCATION);

            // 查询记录总数
            String recordCountSql = String.format("select count(*) from %s.%s",
                    dbname, TdengineSuperTableConstant.HELMET_RUNDE_CA_REPORT_LOCATION);

            R<JSONObject> deviceCountResult = tdengineService.executeTDengineSQL(deviceCountSql);
            R<JSONObject> recordCountResult = tdengineService.executeTDengineSQL(recordCountSql);

            Map<String, Object> statistics = new HashMap<>();
            statistics.put("superTableName", TdengineSuperTableConstant.HELMET_RUNDE_CA_REPORT_LOCATION);

            if (deviceCountResult.getCode() == R.SUCCESS) {
                List<Map<String, Object>> rows = processQueryResult(deviceCountResult.getData());
                if (!rows.isEmpty()) {
                    Object countValue = rows.get(0).values().iterator().next();
                    statistics.put("totalDevices", Long.parseLong(countValue.toString()));
                }
            } else {
                statistics.put("totalDevices", 0);
            }

            if (recordCountResult.getCode() == R.SUCCESS) {
                List<Map<String, Object>> rows = processQueryResult(recordCountResult.getData());
                if (!rows.isEmpty()) {
                    Object countValue = rows.get(0).values().iterator().next();
                    statistics.put("totalRecords", Long.parseLong(countValue.toString()));
                }
            } else {
                statistics.put("totalRecords", 0);
            }

            return R.ok(statistics);
        } catch (Exception e) {
            log.error("获取设备统计信息失败", e);
            return R.fail("获取失败: " + e.getMessage());
        }
    }

    /**
     * 获取设备对应的子表名称
     * 
     * @param deviceId 设备ID
     * @return 子表名称列表（可能存在多个，因为身份证可能变更过）
     * @author Shawn
     * @date 2025-01-31
     */
    public R<List<String>> getSubTablesByDeviceId(String deviceId) {
        log.info("获取设备对应的子表名称, deviceId: {}", deviceId);

        if (StringUtils.isBlank(deviceId)) {
            return R.fail("设备ID不能为空");
        }

        try {
            // 查询所有子表
            String sql = String.format("show %s.tables", dbname);
            R<JSONObject> result = tdengineService.executeTDengineSQL(sql);

            if (result.getCode() == R.SUCCESS) {
                List<Map<String, Object>> rows = processQueryResult(result.getData());
                List<String> subTables = new ArrayList<>();

                String prefix = TdengineSuperTableConstant.HELMET_RUNDE_CA_REPORT_LOCATION +"_" + deviceId + "_";
                for (Map<String, Object> row : rows) {
                    String tableName = (String) row.values().iterator().next();
                    if (tableName != null && tableName.startsWith(prefix)) {
                        subTables.add(tableName);
                    }
                }

                Collections.sort(subTables);
                return R.ok(subTables);
            }

            return R.fail("查询失败: " + result.getMsg());
        } catch (Exception e) {
            log.error("获取设备子表列表失败, deviceId: {}", deviceId, e);
            return R.fail("获取失败: " + e.getMessage());
        }
    }

    /**
     * 清理设备的旧子表（身份证变更后的历史表）
     * 
     * @param deviceId      设备ID
     * @param currentIdCard 当前身份证ID
     * @return 清理结果
     * @author Shawn
     * @date 2025-01-31
     */
    public R<Map<String, Object>> cleanupOldSubTables(String deviceId, String currentIdCard) {
        log.info("清理设备旧子表, deviceId: {}, currentIdCard: {}", deviceId, currentIdCard);

        if (StringUtils.isBlank(deviceId)) {
            return R.fail("设备ID不能为空");
        }

        try {
            // 获取所有相关子表
            R<List<String>> subTablesResult = getSubTablesByDeviceId(deviceId);
            if (subTablesResult.getCode() != R.SUCCESS) {
                return R.fail("获取子表列表失败: " + subTablesResult.getMsg());
            }

            String currentSubTable = generateSubTableName(deviceId, currentIdCard);
            List<String> subTables = subTablesResult.getData();
            List<String> deletedTables = new ArrayList<>();
            List<String> errorMessages = new ArrayList<>();

            for (String subTable : subTables) {
                // 跳过当前应该使用的子表
                if (currentSubTable.equals(subTable)) {
                    continue;
                }

                try {
                    // 删除旧的子表
                    String dropSql = String.format("drop table if exists %s.%s", dbname, subTable);
                    R<JSONObject> dropResult = tdengineService.executeTDengineSQL(dropSql);

                    if (dropResult.getCode() == R.SUCCESS) {
                        deletedTables.add(subTable);
                        log.info("成功删除旧子表: {}", subTable);
                    } else {
                        errorMessages.add("删除子表 " + subTable + " 失败: " + dropResult.getMsg());
                        log.error("删除子表失败: {}, 错误: {}", subTable, dropResult.getMsg());
                    }
                } catch (Exception e) {
                    errorMessages.add("删除子表 " + subTable + " 异常: " + e.getMessage());
                    log.error("删除子表异常: {}", subTable, e);
                }
            }

            Map<String, Object> result = new HashMap<>();
            result.put("deviceId", deviceId);
            result.put("currentSubTable", currentSubTable);
            result.put("totalSubTables", subTables.size());
            result.put("deletedTables", deletedTables);
            result.put("deletedCount", deletedTables.size());
            result.put("errorMessages", errorMessages);

            return R.ok(result);
        } catch (Exception e) {
            log.error("清理设备旧子表失败, deviceId: {}", deviceId, e);
            return R.fail("清理失败: " + e.getMessage());
        }
    }

    /**
     * 批量保存安全帽设备数据
     * 
     * @param deviceDataList 设备数据列表
     * @return 保存结果
     * @author Shawn
     * @date 2025-01-31
     */
    @Override
    public R<Map<String, Object>> batchSaveHelmetData(List<Map<String, Object>> deviceDataList) {
        log.info("批量保存安全帽设备数据, 数量: {}", deviceDataList.size());

        if (deviceDataList == null || deviceDataList.isEmpty()) {
            return R.fail("设备数据列表不能为空");
        }

        int successCount = 0;
        int failCount = 0;
        List<String> errorMessages = new ArrayList<>();

        for (Map<String, Object> deviceData : deviceDataList) {
            String deviceId = (String) deviceData.get("deviceId");
            @SuppressWarnings("unchecked")
            Map<String, Object> dataMap = (Map<String, Object>) deviceData.get("dataMap");

            if (deviceId == null || dataMap == null) {
                failCount++;
                errorMessages.add("设备ID或数据为空");
                continue;
            }

            R<cn.hutool.json.JSONObject> result = saveHelmetData(deviceId, dataMap);
            if (result.getCode() == R.SUCCESS) {
                successCount++;
            } else {
                failCount++;
                errorMessages.add("设备" + deviceId + "保存失败: " + result.getMsg());
            }
        }

        Map<String, Object> batchResult = new HashMap<>();
        batchResult.put("totalCount", deviceDataList.size());
        batchResult.put("successCount", successCount);
        batchResult.put("failCount", failCount);
        batchResult.put("errorMessages", errorMessages);

        log.info("批量保存设备数据完成, 总数: {}, 成功: {}, 失败: {}",
                deviceDataList.size(), successCount, failCount);

        return R.ok(batchResult);
    }

    /**
     * 获取安全帽超级表名称
     * 
     * @return 超级表名称
     * @author Shawn
     * @date 2025-01-31
     */
    @Override
    public String getHelmetSuperTableName() {
        return TdengineSuperTableConstant.HELMET_RUNDE_CA_REPORT_LOCATION;
    }

    /**
     * 处理查询结果
     * 
     * @param jsonObject 查询结果
     * @return 处理后的结果列表
     * @author Shawn
     * @date 2025-01-31
     */
    private List<Map<String, Object>> processQueryResult(JSONObject jsonObject) {
        // 尝试从head获取字段名（兼容旧格式）
        JSONArray head = jsonObject.getJSONArray("head");
        // 尝试从column_meta获取字段名（新格式）
        JSONArray columnMeta = jsonObject.getJSONArray("column_meta");
        JSONArray data = jsonObject.getJSONArray("data");
        List<Map<String, Object>> list = new LinkedList<>();

        if (data == null) {
            return list;
        }

        // 提取字段名
        List<String> fieldNames = new ArrayList<>();
        if (head != null && head.size() > 0) {
            // 使用head格式
            for (int i = 0; i < head.size(); i++) {
                fieldNames.add(head.getStr(i));
            }
        } else if (columnMeta != null && columnMeta.size() > 0) {
            // 使用column_meta格式，提取每个数组的第一个元素作为字段名
            for (int i = 0; i < columnMeta.size(); i++) {
                JSONArray metaItem = columnMeta.getJSONArray(i);
                if (metaItem != null && metaItem.size() > 0) {
                    fieldNames.add(metaItem.getStr(0));
                }
            }
        } else {
            log.warn("无法从查询结果中提取字段名信息");
            return list;
        }

        log.debug("提取到的字段名: {}", fieldNames);

        // 处理数据行
        data.forEach(obj -> {
            Map<String, Object> valMap = new TreeMap<>();
            JSONArray arr = JSONUtil.parseArray(obj);
            for (int i = 0; i < arr.size() && i < fieldNames.size(); i++) {
                valMap.put(fieldNames.get(i), arr.get(i));
            }
            list.add(valMap);
        });

        log.debug("处理后的数据行数: {}", list.size());
        return list;
    }

    /**
     * 获取列类型
     * 
     * @param fieldName 字段名
     * @param value     值
     * @return 列类型
     * @author Shawn
     * @date 2025-01-04
     */
    private String getColumnType(String fieldName, Object value) {
        // 特殊处理bt_signal_info字段，使用NCHAR类型存储长字符串，便于查看原文
        if ("bt_signal_info".equals(fieldName)) {
            return "NCHAR(4000)"; // 使用NCHAR类型，可以直接查看原文，长度4000字符
        }

        // 特殊处理HTTP响应字段，需要足够大的长度存储响应内容
        if ("engine_http_response".equals(fieldName)) {
            return "NCHAR(3000)"; // HTTP响应内容，长度3000字符
        }

        // 特殊处理异常信息字段
        if ("engine_exception_message".equals(fieldName) ||
                "engine_parse_error".equals(fieldName) ||
                "engine_error".equals(fieldName)) {
            return "NCHAR(500)"; // 异常信息字段，长度500字符
        }

        // 特殊处理状态字段，这些字段内容相对较短
        if ("engine_status".equals(fieldName) ||
                "engine_exception_type".equals(fieldName) ||
                "phoneNumber".equals(fieldName) ||
                "tempdata".equals(fieldName) ||
                "user_id".equals(fieldName) ||
                "app_version".equals(fieldName)) {
            return "NCHAR(100)"; // 状态和标识字段，长度100字符
        }

        // 特殊处理坐标和地址字段
        if ("engine_x".equals(fieldName) ||
                "engine_y".equals(fieldName) ||
                "x_point".equals(fieldName) ||
                "y_point".equals(fieldName)) {
            return "DOUBLE"; // 坐标字段使用DOUBLE类型
        }

        if ("engine_address".equals(fieldName)) {
            return "NCHAR(200)"; // 地址字段，长度200字符
        }

        // 特殊处理数值字段
        if ("engine_http_status".equals(fieldName) ||
                "engine_map_id".equals(fieldName) ||
                "bat_l".equals(fieldName) ||
                "bat_v".equals(fieldName) ||
                "stepCount".equals(fieldName) ||
                "stepCountTime".equals(fieldName) ||
                "net_strenth".equals(fieldName) ||
                "net_type".equals(fieldName) ||
                "notuploaded_video_count".equals(fieldName) ||
                "sim_data_num".equals(fieldName)) {
            return "BIGINT"; // 数值字段使用BIGINT
        }

        // 根据值类型进行判断
        if (value instanceof String) {
            String strValue = value.toString();
            // 根据字符串长度动态选择类型
            if (strValue.length() > 200) {
                return "NCHAR(500)"; // 长字符串
            } else if (strValue.length() > 50) {
                return "NCHAR(200)"; // 中等长度字符串
            } else {
                return "NCHAR(50)"; // 短字符串
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
            return "NCHAR(50)"; // 默认短字符串
        }
    }

    /**
     * 判断是否为数字类型
     * 
     * @param strV 输入字符串
     * @return true表示是数字
     * @author Shawn
     * @date 2025-01-31
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
     * 演示身份证变更处理的完整流程
     * 
     * @param deviceId  设备ID
     * @param oldIdCard 旧身份证ID
     * @param newIdCard 新身份证ID
     * @param dataMap   新数据
     * @return 处理结果
     * @author Shawn
     * @date 2025-01-31
     */
    public R<Map<String, Object>> handleIdCardChangeExample(String deviceId, String oldIdCard,
            String newIdCard, Map<String, Object> dataMap) {
        log.info("演示身份证变更处理流程, deviceId: {}, oldIdCard: {}, newIdCard: {}",
                deviceId, oldIdCard, newIdCard);

        try {
            Map<String, Object> result = new HashMap<>();
            result.put("deviceId", deviceId);
            result.put("oldIdCard", oldIdCard);
            result.put("newIdCard", newIdCard);

            // 1. 查看当前设备的所有子表
            R<List<String>> subTablesResult = getSubTablesByDeviceId(deviceId);
            if (subTablesResult.getCode() == R.SUCCESS) {
                result.put("existingSubTables", subTablesResult.getData());
            }

            // 2. 使用新身份证保存数据（会自动创建新的子表）
            R<cn.hutool.json.JSONObject> saveResult = saveHelmetData(deviceId, dataMap);
            result.put("saveNewDataResult", saveResult.getCode() == R.SUCCESS ? "成功" : "失败: " + saveResult.getMsg());

            // 3. 再次查看设备的所有子表（应该包含新的子表）
            R<List<String>> newSubTablesResult = getSubTablesByDeviceId(deviceId);
            if (newSubTablesResult.getCode() == R.SUCCESS) {
                result.put("newSubTables", newSubTablesResult.getData());
            }

            // 4. 可选：清理旧的子表
            // R<Map<String, Object>> cleanupResult = cleanupOldSubTables(deviceId,
            // newIdCard);
            // result.put("cleanupResult", cleanupResult.getData());

            return R.ok(result);
        } catch (Exception e) {
            log.error("演示身份证变更处理失败", e);
            return R.fail("处理失败: " + e.getMessage());
        }
    }

    /**
     * 根据身份证列表查询当前日期最后一条记录的x,y坐标
     * 
     * @param idCardList 身份证号列表
     * @return Map<身份证号, Map<坐标信息>>
     * @author Shawn
     * @date 2025-01-31
     */
    public R<Map<String, Map<String, Object>>> getLatestLocationsByIdCards(List<String> idCardList) {
        log.info("根据身份证列表查询当前日期最后一条记录坐标, 身份证数量: {}", idCardList != null ? idCardList.size() : 0);

        if (idCardList == null || idCardList.isEmpty()) {
            return R.fail("身份证列表不能为空");
        }

        try {
            // 获取当前日期的开始和结束时间
            String currentDate = DateUtil.today();
            String startTime = currentDate + " 00:00:00";
            String endTime = currentDate + " 23:59:59";

            // 构建身份证号的IN查询条件
            StringBuilder idCardCondition = new StringBuilder();
            idCardCondition.append("id_card in (");
            for (int i = 0; i < idCardList.size(); i++) {
                idCardCondition.append("'").append(idCardList.get(i)).append("'");
                if (i < idCardList.size() - 1) {
                    idCardCondition.append(",");
                }
            }
            idCardCondition.append(")");

            // 使用LAST_ROW函数配合PARTITION BY进行批量查询
            String sql = String.format(
                    "select LAST_ROW(id_card, time) from %s.%s " +
                            "where %s and time >= '%s' and time <= '%s' " +
                            "partition by id_card",
                    dbname, TdengineSuperTableConstant.HELMET_RUNDE_CA_REPORT_LOCATION,
                    idCardCondition.toString(), startTime, endTime);

            log.info("查询身份证坐标SQL: {}", sql);
            R<JSONObject> result = tdengineService.executeTDengineSQL(sql);

            if (result.getCode() == R.SUCCESS) {
                List<Map<String, Object>> rows = processQueryResult(result.getData());
                Map<String, Map<String, Object>> locationMap = new HashMap<>();

                for (Map<String, Object> row : rows) {
                    // 处理LAST_ROW函数返回的字段名
                    String idCard = (String) row.get("last_row(id_card)");
//                    Object xObj = row.get("last_row(x)");
//                    Object yObj = row.get("last_row(y)");
                    Object timeObj = row.get("last_row(time)");

//                    log.debug("原始数据 - idCard: {}, x: {}, y: {}, time: {}", idCard, xObj, yObj, timeObj);

                    if (idCard != null && !idCard.trim().isEmpty()) {
                        Map<String, Object> locationInfo = new HashMap<>();
//                        locationInfo.put("x", xObj);
//                        locationInfo.put("y", yObj);
                        locationInfo.put("time", timeObj);
                        locationInfo.put("id_card", idCard);

                        locationMap.put(idCard, locationInfo);
//                        log.info("找到身份证 {} 的坐标: x={}, y={}, time={}",
//                                idCard, xObj, yObj, timeObj);
                    }
                }

                log.info("查询完成，找到 {} 个身份证的坐标信息", locationMap.size());
                return R.ok(locationMap);
            }

            return R.fail("查询失败: " + result.getMsg());
        } catch (Exception e) {
            log.error("根据身份证列表查询坐标失败", e);
            return R.fail("查询失败: " + e.getMessage());
        }
    }

    /**
     * 根据单个身份证号查询当前日期最后一条记录的x,y坐标
     * 
     * @param idCard 身份证号
     * @return 坐标信息
     * @author Shawn
     * @date 2025-01-31
     */
    public R<Map<String, Object>> getLatestLocationByIdCard(String idCard) {
        log.info("根据身份证号查询当前日期最后一条记录坐标, 身份证: {}", idCard);

        if (StringUtils.isBlank(idCard)) {
            return R.fail("身份证号不能为空");
        }

        try {
            List<String> idCardList = Arrays.asList(idCard);
            R<Map<String, Map<String, Object>>> result = getLatestLocationsByIdCards(idCardList);

            if (result.getCode() == R.SUCCESS) {
                Map<String, Map<String, Object>> locationMap = result.getData();
                if (locationMap.containsKey(idCard)) {
                    return R.ok(locationMap.get(idCard));
                } else {
                    return R.fail("未找到身份证 " + idCard + " 的坐标数据");
                }
            }

            return R.fail("查询失败: " + result.getMsg());
        } catch (Exception e) {
            log.error("根据身份证号查询坐标失败, idCard: {}", idCard, e);
            return R.fail("查询失败: " + e.getMessage());
        }
    }

    /**
     * 根据身份证号获取当天的所有轨迹坐标
     * 
     * @param idCard 身份证号
     * @return 轨迹坐标列表
     * @author Shawn
     * @date 2025-01-31
     */
    public R<List<Map<String, Object>>> getTodayTrajectoryByIdCard(String idCard) {
        log.info("根据身份证号获取当天所有轨迹坐标, 身份证: {}", idCard);

        if (StringUtils.isBlank(idCard)) {
            return R.fail("身份证号不能为空");
        }

        try {
            // 获取当前日期的开始和结束时间
            String currentDate = DateUtil.today();
            String startTime = currentDate + " 00:00:00";
            String endTime = currentDate + " 23:59:59";

            // 查询当天该身份证的所有坐标数据，按时间排序
            String sql = String.format(
                    "select id_card, time from %s.%s " +
                            "where id_card='%s' and time >= '%s' and time <= '%s' " +
                            "order by time asc",
                    dbname, TdengineSuperTableConstant.HELMET_RUNDE_CA_REPORT_LOCATION,
                    idCard, startTime, endTime);

            log.info("查询轨迹坐标SQL: {}", sql);
            R<JSONObject> result = tdengineService.executeTDengineSQL(sql);

            if (result.getCode() == R.SUCCESS) {
                List<Map<String, Object>> rows = processQueryResult(result.getData());
                List<Map<String, Object>> trajectoryPoints = new ArrayList<>();

                for (Map<String, Object> row : rows) {
                    String resultIdCard = (String) row.get("id_card");
//                    Object xObj = row.get("x");
//                    Object yObj = row.get("y");
                    Object timeObj = row.get("time");

                    if (resultIdCard != null && !resultIdCard.trim().isEmpty()) {
                        Map<String, Object> point = new HashMap<>();
//                        point.put("x", xObj);
//                        point.put("y", yObj);
                        point.put("time", timeObj);
                        point.put("id_card", resultIdCard);

                        trajectoryPoints.add(point);
                    }
                }

                log.info("身份证 {} 当天轨迹点数量: {}", idCard, trajectoryPoints.size());
                return R.ok(trajectoryPoints);
            }

            return R.fail("查询失败: " + result.getMsg());
        } catch (Exception e) {
            log.error("根据身份证号获取当天轨迹坐标失败, idCard: {}", idCard, e);
            return R.fail("查询失败: " + e.getMessage());
        }
    }

    /**
     * 获取指定身份证号在指定日期的第一条和最后一条时间记录
     * 
     * @param idCard  身份证号
     * @param dateStr 日期字符串，格式：yyyy-MM-dd
     * @return Map包含firstTime和lastTime
     * @author Shawn
     * @date 2025/01/27
     */
    @Override
    public R<Map<String, Object>> getFirstAndLastTimeByIdCardAndDate(String idCard, String dateStr) {
        log.info("获取指定身份证号在指定日期的第一条和最后一条时间记录, 身份证: {}, 日期: {}", idCard, dateStr);

        if (StringUtils.isBlank(idCard)) {
            return R.fail("身份证号不能为空");
        }

        if (StringUtils.isBlank(dateStr)) {
            return R.fail("日期不能为空");
        }

        try {
            // 构建开始和结束时间
            String startTime = dateStr + " 00:00:00";
            String endTime = dateStr + " 23:59:59";

            // 查询第一条时间记录
            String firstSql = String.format(
                    "select time from %s.%s " +
                            "where id_card='%s' and time >= '%s' and time <= '%s' " +
                            "order by time asc limit 1",
                    dbname, TdengineSuperTableConstant.HELMET_RUNDE_CA_REPORT_LOCATION,
                    idCard, startTime, endTime);

            // 查询最后一条时间记录
            String lastSql = String.format(
                    "select time from %s.%s " +
                            "where id_card='%s' and time >= '%s' and time <= '%s' " +
                            "order by time desc limit 1",
                    dbname, TdengineSuperTableConstant.HELMET_RUNDE_CA_REPORT_LOCATION,
                    idCard, startTime, endTime);

            log.info("查询第一条时间SQL: {}", firstSql);
            log.info("查询最后一条时间SQL: {}", lastSql);

            R<JSONObject> firstResult = tdengineService.executeTDengineSQL(firstSql);
            R<JSONObject> lastResult = tdengineService.executeTDengineSQL(lastSql);

            Map<String, Object> result = new HashMap<>();
            result.put("idCard", idCard);
            result.put("date", dateStr);

            if (firstResult.getCode() == R.SUCCESS) {
                List<Map<String, Object>> firstRows = processQueryResult(firstResult.getData());
                if (!firstRows.isEmpty()) {
                    Object firstTime = firstRows.get(0).get("time");
                    result.put("firstTime", firstTime);
                    log.info("找到第一条时间记录: {}", firstTime);
                } else {
                    result.put("firstTime", null);
                    log.info("未找到第一条时间记录");
                }
            } else {
                result.put("firstTime", null);
                log.error("查询第一条时间记录失败: {}", firstResult.getMsg());
            }

            if (lastResult.getCode() == R.SUCCESS) {
                List<Map<String, Object>> lastRows = processQueryResult(lastResult.getData());
                if (!lastRows.isEmpty()) {
                    Object lastTime = lastRows.get(0).get("time");
                    result.put("lastTime", lastTime);
                    log.info("找到最后一条时间记录: {}", lastTime);
                } else {
                    result.put("lastTime", null);
                    log.info("未找到最后一条时间记录");
                }
            } else {
                result.put("lastTime", null);
                log.error("查询最后一条时间记录失败: {}", lastResult.getMsg());
            }

            return R.ok(result);
        } catch (Exception e) {
            log.error("获取第一条和最后一条时间记录失败, 身份证: {}, 日期: {}", idCard, dateStr, e);
            return R.fail("查询失败: " + e.getMessage());
        }
    }

    /**
     * 获取指定身份证号在指定日期和工作时间范围内的第一条和最后一条时间记录
     * 
     * @param idCard        身份证号
     * @param dateStr       日期字符串，格式：yyyy-MM-dd
     * @param workTimeRange 工作时间范围，格式：HH:mm-HH:mm（如18:00-03:30）
     * @return Map包含firstTime和lastTime
     * @author Shawn
     * @date 2025/01/27
     */
    @Override
    public R<Map<String, Object>> getFirstAndLastTimeByIdCardAndDate(String idCard, String dateStr,
            String workTimeRange) {
        log.info("获取员工[身份证:{}]在[{}]的最早和最晚打卡时间(班次:{})", idCard, dateStr, workTimeRange);
        if (StringUtils.isBlank(idCard) || StringUtils.isBlank(dateStr)) {
            return R.fail("身份证号和日期不能为空");
        }

        try {
            // 解析工作时间范围，获取TDengine查询的起止时间
            String[] timeBounds = parseWorkTimeRange(dateStr, workTimeRange);
            String startTime = timeBounds[0];
            String endTime = timeBounds[1];

            // 构建SQL查询
            // @author: Shawn
            // @date: 2024/07/31
            // 根据用户要求，从 helmet_runde_ca_report_location 修改为 external_coordinate_data
            String sql = String.format(
                    "select FIRST(time) as firsttime, LAST(time) as lasttime from %s.%s where id_card='%s' and time >= '%s' and time <= '%s'",
                    dbname, "external_coordinate_data", idCard, startTime, endTime);

            log.info("查询最早最晚时间SQL: {}", sql);
            R<JSONObject> result = tdengineService.executeTDengineSQL(sql);

            if (result.getCode() == R.SUCCESS) {
                List<Map<String, Object>> rows = processQueryResult(result.getData());
                if (!rows.isEmpty()) {
                    Map<String, Object> timeInfo = rows.get(0);
                    // 创建一个新的Map来存放结果
                    Map<String, Object> resultMap = new HashMap<>();
                    resultMap.put("firstTime", timeInfo.get("firsttime"));
                    resultMap.put("lastTime", timeInfo.get("lasttime"));
                    return R.ok(resultMap);
                }
            }

            return R.fail("查询失败或无数据: " + result.getMsg());
        } catch (Exception e) {
            log.error("获取第一条和最后一条时间记录失败, 身份证: {}, 日期: {}, 工作时间: {}",
                    idCard, dateStr, workTimeRange, e);
            return R.fail("查询失败: " + e.getMessage());
        }
    }

    /**
     * 解析工作时间范围，处理跨夜班次
     * 
     * @param dateStr       基准日期
     * @param workTimeRange 工作时间范围，格式：HH:mm-HH:mm
     * @return [开始时间, 结束时间] 数组，格式：yyyy-MM-dd HH:mm:ss
     * @author Shawn
     * @date 2025/01/27
     */
    private String[] parseWorkTimeRange(String dateStr, String workTimeRange) {
        try {
            if (!workTimeRange.contains("-")) {
                return null;
            }

            String[] times = workTimeRange.split("-");
            if (times.length != 2) {
                return null;
            }

            String startTimeStr = times[0].trim();
            String endTimeStr = times[1].trim();

            // 验证时间格式
            if (!startTimeStr.matches("\\d{2}:\\d{2}") || !endTimeStr.matches("\\d{2}:\\d{2}")) {
                return null;
            }

            String startDateTime = dateStr + " " + startTimeStr + ":00";
            String endDateTime;

            // 判断是否跨夜
            if (endTimeStr.compareTo(startTimeStr) <= 0) {
                // 跨夜班次，结束时间是第二天
                try {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                    Date baseDate = sdf.parse(dateStr);
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(baseDate);
                    cal.add(Calendar.DAY_OF_MONTH, 1);
                    String nextDay = sdf.format(cal.getTime());
                    endDateTime = nextDay + " " + endTimeStr + ":00";

                    log.info("检测到跨夜班次: {}，开始时间: {}，结束时间: {}",
                            workTimeRange, startDateTime, endDateTime);
                } catch (Exception e) {
                    log.error("解析跨夜班次日期失败: {}", dateStr, e);
                    return null;
                }
            } else {
                // 当日班次
                endDateTime = dateStr + " " + endTimeStr + ":00";
            }

            return new String[] { startDateTime, endDateTime };
        } catch (Exception e) {
            log.error("解析工作时间范围失败: {}", workTimeRange, e);
            return null;
        }
    }
}