package com.jeesite.modules.swm.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import com.jeesite.modules.swm.service.ExternalCoordinateDataService;
import com.jeesite.modules.swm.service.TDengineService;
import com.jeesite.modules.utils.R;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

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

            // 使用LAST_ROW函数配合PARTITION BY进行批量查询external_coordinate_data表
            String sql = String.format(
                    "select LAST_ROW(id_card, x, y, time) from %s.%s " +
                            "where %s and time >= '%s' and time <= '%s' " +
                            "partition by id_card",
                    dbname, EXTERNAL_COORDINATE_SUPER_TABLE_NAME,
                    idCardCondition.toString(), startTime, endTime);

            log.info("查询external_coordinate_data身份证坐标SQL: {}", sql);
            R<JSONObject> result = tdengineService.executeTDengineSQL(sql);

            if (result.getCode() == R.SUCCESS) {
                List<Map<String, Object>> rows = processQueryResult(result.getData());
                Map<String, Map<String, Object>> locationMap = new HashMap<>();

                for (Map<String, Object> row : rows) {
                    // 处理LAST_ROW函数返回的字段名
                    String idCard = (String) row.get("last_row(id_card)");
                    Object xObj = row.get("last_row(x)");
                    Object yObj = row.get("last_row(y)");
                    Object timeObj = row.get("last_row(time)");

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
            String sql = String.format(
                    "select id_card, x, y, time from %s.%s " +
                            "where id_card='%s' and time >= '%s' and time <= '%s' " +
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
     * 
     * @param jsonObject TDengine查询结果
     * @return 处理后的数据列表
     */
    private List<Map<String, Object>> processQueryResult(JSONObject jsonObject) {
        List<Map<String, Object>> list = new ArrayList<>();

        try {
            JSONArray columnMeta = jsonObject.getJSONArray("column_meta");
            JSONArray data = jsonObject.getJSONArray("data");

            if (columnMeta != null && data != null) {
                for (int i = 0; i < data.size(); i++) {
                    JSONArray row = data.getJSONArray(i);
                    Map<String, Object> map = new HashMap<>();

                    for (int j = 0; j < columnMeta.size() && j < row.size(); j++) {
                        JSONArray columnInfo = columnMeta.getJSONArray(j);
                        String columnName = columnInfo.getStr(0);
                        Object value = row.get(j);
                        map.put(columnName, value);
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
}