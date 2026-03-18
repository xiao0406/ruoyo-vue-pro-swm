package com.jeesite.modules.swm.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import com.jeesite.modules.cache.service.RedisService;
import com.jeesite.modules.constant.SwmRedisConstant;
import com.jeesite.modules.swm.service.ExternalCoordinateDataService;
import com.jeesite.modules.swm.service.TDengineService;
import com.jeesite.modules.sys.utils.CorpUtils;
import com.jeesite.modules.utils.R;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 外部坐标数据服务实现类
 * 专门处理external_coordinate_data表的数据操作
 * 
 * @author Shawn
 */
@Slf4j
@Service
public class ExternalCoordinateDataServiceImpl implements ExternalCoordinateDataService {

    @Autowired
    private TDengineService tdengineService;

    @Value("${tdengine.dbname}")
    private String dbname;

    @Autowired
    private RedisService redisService;

    /**
     * 外部坐标数据超级表名称
     */
    private static final String EXTERNAL_COORDINATE_SUPER_TABLE_NAME = "external_coordinate_data";

    /**
     * 根据身份证列表查询当前日期最后一条记录的x,y坐标
     * 
     * @param idCardList 身份证号列表
     * @return Map<身份证号, Map<坐标信息>>
     */
    @Override
    public R<Map<String, Map<String, Object>>> getLatestLocationsByIdCards(List<String> idCardList) {
        log.info("从external_coordinate_data表根据身份证列表查询当前日期最后一条记录坐标, 身份证数量: {}",
                idCardList != null ? idCardList.size() : 0);

        if (idCardList == null || idCardList.isEmpty()) {
            return R.fail("身份证列表不能为空");
        }

        idCardList.clear();
        String corpCode = CorpUtils.getCurrentCorpCode();
        Set<Object> deviceIds = redisService.sGet(corpCode + SwmRedisConstant.RedisIotKey.ONLINE_DEVICES_KEY);
        if (deviceIds != null) {
            for (Object deviceId : deviceIds) {
                String currentPerson = (String) redisService.hget(SwmRedisConstant.RedisGlobalKey.DEVICE_PERSON_MAP, String.valueOf(deviceId));
                idCardList.add(currentPerson);
            }
        }

        try {
            // 获取当前日期的开始和结束时间
//            String currentDate = DateUtil.today();
//            String startTime = currentDate + " 00:00:00";
//            String endTime = currentDate + " 23:59:59";
            Date now = new Date();
            Date oneMinuteAgo = DateUtil.offsetDay(now, -7);
            String startTime = DateUtil.formatDateTime(oneMinuteAgo);
            String endTime = DateUtil.formatDateTime(now);

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

            // 使用LAST_ROW函数配合PARTITION BY进行批量查询external_coordinate_data表
            // 添加别名强制指定列名格式，兼容不同环境的TDengine返回格式
            // 添加过滤条件排除original_x或original_y小于等于0的数据
            String sql = String.format(
                    "select LAST_ROW(id_card) as id_card, LAST_ROW(x) as x, " +
                            "LAST_ROW(y) as y, LAST_ROW(time) as time from %s.%s " +
                            "where %s and time >= '%s' and time <= '%s' " +
                            "and original_x > 0 and original_y > 0 " +
                            "partition by id_card",
                    dbname, EXTERNAL_COORDINATE_SUPER_TABLE_NAME,
                    idCardCondition.toString(), startTime, endTime);

            log.info("查询external_coordinate_data身份证坐标SQL: {}", sql);

            R<JSONObject> result = tdengineService.executeTDengineSQL(sql);
            // 添加调试日志，打印TDengine返回的原始列元数据和数据
            if (result.getCode() == R.SUCCESS && result.getData() != null) {
                JSONArray columnMeta = result.getData().getJSONArray("column_meta");
                JSONArray data = result.getData().getJSONArray("data");
                log.info("TDengine返回的列元数据: {}", columnMeta);
                log.info("TDengine返回的数据行数: {}", data != null ? data.size() : 0);
                if (data != null && data.size() > 0) {
                    log.info("TDengine返回的第一行数据: {}", data.getJSONArray(0));
                }
            }

            if (result.getCode() == R.SUCCESS) {
                List<Map<String, Object>> rows = processQueryResult(result.getData());
                Map<String, Map<String, Object>> locationMap = new HashMap<>();

                for (Map<String, Object> row : rows) {
                    // 处理LAST_ROW函数返回的字段名
                    String idCard = (String) row.get("id_card");
                    Object xObj = row.get("x");
                    Object yObj = row.get("y");
                    Object timeObj = row.get("time");

                    log.debug("external_coordinate_data原始数据 - idCard: {}, x: {}, y: {}, time: {}",
                            idCard, xObj, yObj, timeObj);

                    if (idCard != null && !idCard.trim().isEmpty()) {
                        Map<String, Object> locationInfo = new HashMap<>();
                        locationInfo.put("x", xObj);
                        locationInfo.put("y", yObj);
                        locationInfo.put("time", timeObj);
                        locationInfo.put("id_card", idCard);

                        locationMap.put(idCard, locationInfo);
                        log.info("从external_coordinate_data找到身份证 {} 的坐标: x={}, y={}, time={}",
                                idCard, xObj, yObj, timeObj);
                    }
                }

                log.info("从external_coordinate_data查询完成，找到 {} 个身份证的坐标信息", locationMap.size());
                return R.ok(locationMap);
            }

            return R.fail("查询失败: " + result.getMsg());
        } catch (Exception e) {
            log.error("从external_coordinate_data根据身份证列表查询坐标失败", e);
            return R.fail("查询失败: " + e.getMessage());
        }
    }

    /**
     * 根据单个身份证号查询当前日期最后一条记录的x,y坐标
     * 
     * @param idCard 身份证号
     * @return 坐标信息
     * @author Shawn
     * @date 2025/06/21
     */
    @Override
    public R<Map<String, Object>> getLatestLocationByIdCard(String idCard) {
        log.info("从external_coordinate_data根据身份证号查询当前日期最后一条记录坐标, 身份证: {}", idCard);

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
            log.error("从external_coordinate_data根据身份证号查询坐标失败, idCard: {}", idCard, e);
            return R.fail("查询失败: " + e.getMessage());
        }
    }

    /**
     * 根据身份证号获取当天的所有轨迹坐标
     * 
     * @param idCard 身份证号
     * @return 轨迹坐标列表
     * @author Shawn
     * @date 2025/06/21
     */
    @Override
    public R<List<Map<String, Object>>> getTodayTrajectoryByIdCard(String idCard) {
        log.info("从external_coordinate_data根据身份证号获取当天所有轨迹坐标, 身份证: {}", idCard);

        if (StringUtils.isBlank(idCard)) {
            return R.fail("身份证号不能为空");
        }

        try {
            // 获取当前日期的开始和结束时间
            String currentDate = DateUtil.today();
            String startTime = currentDate + " 00:00:00";
            String endTime = currentDate + " 23:59:59";

            // 查询当天该身份证的所有坐标数据，按时间排序
            // 添加过滤条件排除original_x或original_y小于等于0的数据
            String sql = String.format(
                    "select id_card, x, y, time from %s.%s " +
                            "where id_card='%s' and time >= '%s' and time <= '%s' " +
                            "and original_x > 0 and original_y > 0 " +
                            "order by time asc",
                    dbname, EXTERNAL_COORDINATE_SUPER_TABLE_NAME,
                    idCard, startTime, endTime);

            log.info("查询external_coordinate_data轨迹坐标SQL: {}", sql);
            R<JSONObject> result = tdengineService.executeTDengineSQL(sql);

            if (result.getCode() == R.SUCCESS) {
                List<Map<String, Object>> rows = processQueryResult(result.getData());
                List<Map<String, Object>> trajectoryPoints = new ArrayList<>();

                for (Map<String, Object> row : rows) {
                    String resultIdCard = (String) row.get("id_card");
                    Object xObj = row.get("x");
                    Object yObj = row.get("y");
                    Object timeObj = row.get("time");

                    if (resultIdCard != null && !resultIdCard.trim().isEmpty() && xObj != null && yObj != null) {
                        Map<String, Object> point = new HashMap<>();
                        point.put("x", xObj);
                        point.put("y", yObj);
                        point.put("time", timeObj);
                        point.put("id_card", resultIdCard);

                        trajectoryPoints.add(point);
                    }
                }

                log.info("从external_coordinate_data获取身份证 {} 当天轨迹点数量: {}", idCard, trajectoryPoints.size());
                return R.ok(trajectoryPoints);
            }

            return R.fail("查询失败: " + result.getMsg());
        } catch (Exception e) {
            log.error("从external_coordinate_data根据身份证号获取当天轨迹坐标失败, idCard: {}", idCard, e);
            return R.fail("查询失败: " + e.getMessage());
        }
    }

    /**
     * 根据身份证号和时间范围获取轨迹坐标
     * 
     * @param idCard    身份证号
     * @param startDate 开始日期 (格式: YYYY-MM-DD)
     * @param endDate   结束日期 (格式: YYYY-MM-DD)
     * @param startTime 开始时间（当日的秒数，可选）
     * @param endTime   结束时间（当日的秒数，可选）
     * @return 轨迹坐标列表
     * @author Shawn
     * @date 2025/06/21
     */
    @Override
    public R<List<Map<String, Object>>> getTrajectoryByIdCardAndTimeRange(String idCard, String startDate,
            String endDate, Integer startTime, Integer endTime) {
        log.info("从external_coordinate_data根据身份证号和时间范围获取轨迹坐标, 身份证: {}, 开始日期: {}, 结束日期: {}, 开始时间: {}, 结束时间: {}",
                idCard, startDate, endDate, startTime, endTime);

        if (StringUtils.isBlank(idCard)) {
            return R.fail("身份证号不能为空");
        }

        try {
            // 构建时间条件
            String timeCondition = buildTimeCondition(startDate, endDate, startTime, endTime);

            // 查询该身份证在指定时间范围内的所有坐标数据，按时间排序
            // 添加过滤条件排除original_x或original_y小于等于0的数据
            String sql = String.format(
                    "select id_card, x, y, time from %s.%s " +
                            "where id_card='%s' %s " +
                            "and original_x > 0 and original_y > 0 " +
                            "order by time asc",
                    dbname, EXTERNAL_COORDINATE_SUPER_TABLE_NAME,
                    idCard, timeCondition);

            log.info("查询external_coordinate_data时间范围轨迹坐标SQL: {}", sql);
            R<JSONObject> result = tdengineService.executeTDengineSQL(sql);

            if (result.getCode() == R.SUCCESS) {
                List<Map<String, Object>> rows = processQueryResult(result.getData());
                List<Map<String, Object>> trajectoryPoints = new ArrayList<>();

                for (Map<String, Object> row : rows) {
                    String resultIdCard = (String) row.get("id_card");
                    Object xObj = row.get("x");
                    Object yObj = row.get("y");
                    Object timeObj = row.get("time");

                    if (resultIdCard != null && !resultIdCard.trim().isEmpty() && xObj != null && yObj != null) {
                        Map<String, Object> point = new HashMap<>();
                        point.put("x", xObj);
                        point.put("y", yObj);
                        point.put("time", timeObj);
                        point.put("id_card", resultIdCard);

                        trajectoryPoints.add(point);
                    }
                }

                log.info("从external_coordinate_data获取身份证 {} 时间范围轨迹点数量: {}", idCard, trajectoryPoints.size());
                return R.ok(trajectoryPoints);
            }

            return R.fail("查询失败: " + result.getMsg());
        } catch (Exception e) {
            log.error("从external_coordinate_data根据身份证号和时间范围获取轨迹坐标失败, idCard: {}", idCard, e);
            return R.fail("查询失败: " + e.getMessage());
        }
    }

    /**
     * 构建时间查询条件
     * 
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @param startTime 开始时间（秒数）
     * @param endTime   结束时间（秒数）
     * @return 时间查询条件
     */
    private String buildTimeCondition(String startDate, String endDate, Integer startTime, Integer endTime) {
        StringBuilder condition = new StringBuilder();

        // 如果没有指定日期，使用当天
        if (StringUtils.isBlank(startDate) && StringUtils.isBlank(endDate)) {
            String currentDate = DateUtil.today();
            startDate = currentDate;
            endDate = currentDate;
        } else if (StringUtils.isBlank(startDate)) {
            startDate = endDate;
        } else if (StringUtils.isBlank(endDate)) {
            endDate = startDate;
        }

        // 构建基本的日期范围条件
        String queryStartDateTime;
        String queryEndDateTime;

        if (startTime != null && endTime != null) {
            // 如果指定了具体时间（秒数），转换为时分秒
            String startTimeStr = formatSecondsToTime(startTime);
            String endTimeStr = formatSecondsToTime(endTime);

            queryStartDateTime = startDate + " " + startTimeStr;
            queryEndDateTime = endDate + " " + endTimeStr;
        } else {
            // 如果没有指定具体时间，使用全天
            queryStartDateTime = startDate + " 00:00:00";
            queryEndDateTime = endDate + " 23:59:59";
        }

        condition.append(" and time >= '").append(queryStartDateTime).append("'");
        condition.append(" and time <= '").append(queryEndDateTime).append("'");

        return condition.toString();
    }

    /**
     * 将秒数转换为时分秒格式
     * 
     * @param seconds 秒数
     * @return 时分秒格式字符串 (HH:mm:ss)
     */
    private String formatSecondsToTime(int seconds) {
        int hours = seconds / 3600;
        int minutes = (seconds % 3600) / 60;
        int secs = seconds % 60;
        return String.format("%02d:%02d:%02d", hours, minutes, secs);
    }

    /**
     * 保存外部坐标数据
     * 
     * @param dataMap 坐标数据
     * @return 保存结果
     */
    @Override
    public R<Map<String, Object>> saveCoordinateData(Map<String, Object> dataMap) {
        log.info("保存external_coordinate_data数据: {}", dataMap);

        if (dataMap == null || dataMap.isEmpty()) {
            return R.fail("坐标数据不能为空");
        }

        try {
            // 构建插入SQL
            StringBuilder insertSql = new StringBuilder("insert into ")
                    .append(dbname).append(".").append(EXTERNAL_COORDINATE_SUPER_TABLE_NAME)
                    .append(" (time");

            StringBuilder values = new StringBuilder("values (")
                    .append(DateUtil.date().getTime());

            // 添加数据字段
            for (Map.Entry<String, Object> entry : dataMap.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();

                // 跳过TAG字段（elder_id和id_card）
                if (!"elder_id".equals(key) && !"id_card".equals(key)) {
                    insertSql.append(",").append(key);
                    values.append(",");
                    if (value != null && !isNumeric(String.valueOf(value))) {
                        values.append("'").append(value).append("'");
                    } else {
                        values.append(value);
                    }
                }
            }

            insertSql.append(") ").append(values).append(")");

            log.info("保存external_coordinate_data SQL: {}", insertSql.toString());
            R<JSONObject> result = tdengineService.executeTDengineSQL(insertSql.toString());

            if (result.getCode() == R.SUCCESS) {
                Map<String, Object> resultMap = new HashMap<>();
                resultMap.put("success", true);
                resultMap.put("message", "数据保存成功");
                return R.ok(resultMap);
            } else {
                return R.fail("保存失败: " + result.getMsg());
            }

        } catch (Exception e) {
            log.error("保存external_coordinate_data数据失败", e);
            return R.fail("保存失败: " + e.getMessage());
        }
    }

    /**
     * 获取外部坐标数据统计信息
     * 
     * @return 统计信息
     */
    @Override
    public R<Map<String, Object>> getStatistics() {
        log.info("获取external_coordinate_data统计信息");

        try {
            // 查询记录总数
            String recordCountSql = String.format("select count(*) from %s.%s",
                    dbname, EXTERNAL_COORDINATE_SUPER_TABLE_NAME);

            // 查询不同身份证数量
            String idCardCountSql = String.format("select count(distinct id_card) from %s.%s",
                    dbname, EXTERNAL_COORDINATE_SUPER_TABLE_NAME);

            R<JSONObject> recordCountResult = tdengineService.executeTDengineSQL(recordCountSql);
            R<JSONObject> idCardCountResult = tdengineService.executeTDengineSQL(idCardCountSql);

            Map<String, Object> statistics = new HashMap<>();
            statistics.put("superTableName", EXTERNAL_COORDINATE_SUPER_TABLE_NAME);

            if (recordCountResult.getCode() == R.SUCCESS) {
                List<Map<String, Object>> rows = processQueryResult(recordCountResult.getData());
                if (!rows.isEmpty()) {
                    Object countValue = rows.get(0).values().iterator().next();
                    statistics.put("totalRecords", Long.parseLong(countValue.toString()));
                }
            } else {
                statistics.put("totalRecords", 0);
            }

            if (idCardCountResult.getCode() == R.SUCCESS) {
                List<Map<String, Object>> rows = processQueryResult(idCardCountResult.getData());
                if (!rows.isEmpty()) {
                    Object countValue = rows.get(0).values().iterator().next();
                    statistics.put("totalIdCards", Long.parseLong(countValue.toString()));
                }
            } else {
                statistics.put("totalIdCards", 0);
            }

            return R.ok(statistics);
        } catch (Exception e) {
            log.error("获取external_coordinate_data统计信息失败", e);
            return R.fail("获取失败: " + e.getMessage());
        }
    }

    /**
     * 处理查询结果
     * 兼容TDengine不同环境的列名格式（支持last_row(column)和column两种格式）
     *
     * @param jsonObject TDengine查询结果
     * @return 处理后的数据列表
     * @author Shawn
     * @date 2025-09-19
     */
    private List<Map<String, Object>> processQueryResult(JSONObject jsonObject) {
        List<Map<String, Object>> list = new ArrayList<>();

        try {
            JSONArray columnMeta = jsonObject.getJSONArray("column_meta");
            JSONArray data = jsonObject.getJSONArray("data");

            if (columnMeta != null && data != null) {
                // 打印每行的列名映射关系用于调试
                log.debug("processQueryResult - 处理{}行数据，列数: {}", data.size(), columnMeta.size());

                for (int i = 0; i < data.size(); i++) {
                    JSONArray row = data.getJSONArray(i);
                    Map<String, Object> map = new HashMap<>();

                    for (int j = 0; j < columnMeta.size() && j < row.size(); j++) {
                        JSONArray columnInfo = columnMeta.getJSONArray(j);
                        String originalColumnName = columnInfo.getStr(0);
                        Object value = row.get(j);

                        // 标准化列名：处理last_row(column)格式，提取实际的列名
                        String normalizedColumnName = normalizeColumnName(originalColumnName);

                        // 打印列名映射关系用于调试
                        if (!originalColumnName.equals(normalizedColumnName)) {
                            log.debug("列名标准化: '{}' -> '{}'", originalColumnName, normalizedColumnName);
                        }

                        // 处理时间字段的时区转换
                        if (isTimeColumn(normalizedColumnName) && value != null) {
                            value = convertUtcToBeijingTime(value.toString());
                        }

                        // 使用标准化后的列名存储数据
                        map.put(normalizedColumnName, value);

                        // 打印每个字段的值用于调试
                        log.debug("字段值: {} = {}", normalizedColumnName, value);
                    }
                    list.add(map);
                }
            }
        } catch (Exception e) {
            log.error("处理external_coordinate_data查询结果失败", e);
        }

        return list;
    }

    /**
     * 标准化列名，兼容last_row(column)和column两种格式
     *
     * @param columnName 原始列名
     * @return 标准化后的列名
     * @author Shawn
     * @date 2025-09-19
     */
    private String normalizeColumnName(String columnName) {
        if (columnName == null) {
            return null;
        }

        // 处理last_row(column)格式，提取括号内的列名
        if (columnName.startsWith("last_row(") && columnName.endsWith(")")) {
            String extracted = columnName.substring(9, columnName.length() - 1);
            log.debug("提取LAST_ROW列名: '{}' -> '{}'", columnName, extracted);
            return extracted;
        }

        // 如果不是last_row格式，直接返回原列名
        return columnName;
    }

    /**
     * 判断字符串是否为数字
     *
     * @param str 字符串
     * @return 是否为数字
     */
    private boolean isNumeric(String str) {
        try {
            Double.parseDouble(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * 判断是否为时间列（基于标准化后的列名）
     *
     * @param columnName 列名（应该是已标准化的列名）
     * @return 是否为时间列
     * @author Shawn
     * @date 2025-09-19
     */
    private boolean isTimeColumn(String columnName) {
        return "time".equals(columnName) ||
                columnName.contains("time") ||
                columnName.contains("Time");
    }

    /**
     * 将UTC时间转换为北京时间
     *
     * @param utcTimeStr UTC时间字符串
     * @return 北京时间字符串
     */
    private String convertUtcToBeijingTime(String utcTimeStr) {
        try {
            if (StringUtils.isBlank(utcTimeStr)) {
                return utcTimeStr;
            }

            // 处理不同的时间格式
            String normalizedTime = utcTimeStr;

            // 如果包含T，替换为空格
            if (normalizedTime.contains("T")) {
                normalizedTime = normalizedTime.replace("T", " ");
            }

            // 去掉毫秒部分和时区信息
            if (normalizedTime.contains(".")) {
                normalizedTime = normalizedTime.substring(0, normalizedTime.indexOf("."));
            }
            if (normalizedTime.contains("Z")) {
                normalizedTime = normalizedTime.replace("Z", "");
            }
            if (normalizedTime.contains("+")) {
                normalizedTime = normalizedTime.substring(0, normalizedTime.indexOf("+"));
            }

            // 解析UTC时间
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            LocalDateTime utcDateTime = LocalDateTime.parse(normalizedTime, formatter);

            // 转换为UTC时区的ZonedDateTime
            ZonedDateTime utcZoned = utcDateTime.atZone(ZoneId.of("UTC"));

            // 转换为北京时间
            ZonedDateTime beijingZoned = utcZoned.withZoneSameInstant(ZoneId.of("Asia/Shanghai"));

            // 格式化为字符串
            return beijingZoned.format(formatter);

        } catch (Exception e) {
            log.warn("时间转换失败，使用原始时间: {}", utcTimeStr, e);
            return utcTimeStr;
        }
    }
}