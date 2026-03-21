package com.jeesite.modules.swm.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.jeesite.modules.cache.service.RedisService;
import com.jeesite.modules.config.OkHttpClientManager;
import com.jeesite.modules.config.TenantContext;
import com.jeesite.modules.constant.TdengineSuperTableConstant;
import com.jeesite.modules.enums.CorpDbEnum;
import com.jeesite.modules.swm.cache.DeviceCorpMappingCache;
import com.jeesite.modules.constant.DebugConstant;
import com.jeesite.modules.swm.service.TDengineService;
import com.jeesite.modules.sys.utils.CorpUtils;
import com.jeesite.modules.vo.DeviceDataDTO;
import com.jeesite.modules.vo.QueryParamDTO;
import com.xxl.job.core.context.XxlJobHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import com.jeesite.modules.utils.R;

import javax.annotation.PostConstruct;
import java.util.*;

/**
 * tdengine服务实现类
 *
 * @author: cjie
 * @date: 2023/1/3
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TdengineServiceImpl implements TDengineService {
    /**
     * 连接路径
     */
    @Value("${tdengine.url}")
    private String url;
    /**
     * 鉴权
     */
    @Value("${tdengine.authorization}")
    private String authorization;
    /**
     * 数据库名称
     */
    @Value("${tdengine.dbname}")
    private String dbname;
    /**
     * 保留时长
     */
    @Value("${tdengine.retentionPolicy}")
    private String retentionPolicy;

    private final OkHttpClientManager okHttpClientManager;

    @Qualifier("swmExecutor")
    @Autowired
    private ThreadPoolTaskExecutor swmExecutor;

    @Autowired
    private DeviceCorpMappingCache deviceCorpMappingCache;

    @Autowired
    private RedisService redisService;

    /**
     * 项目启动时自动创建数据库
     */
    @PostConstruct
    public void init() {
        log.info("tdengine服务初始化");
        createDb();
    }

    /**
     * 创建数据库
     */
    private void createDb() {
        log.info("初始化 TDengine 多租户数据库");

        // 遍历所有租户数据库映射
        for (CorpDbEnum corpDb : CorpDbEnum.values()) {
            String dbName = corpDb.getDbName();
            String sql = "create database if not exists " + dbName + " keep " + retentionPolicy;
            log.info("创建数据库 SQL: {}", sql);
            R<JSONObject> result = execute(sql);
            if (R.SUCCESS != result.getCode()) {
                log.error("创建数据库失败: {}", result.getMsg());
            }
        }
    }

    /**
     * 执行任意sql
     *
     * @param sql 操作语句
     */
    @Override
    public R<JSONObject> execute(String sql) {
        String result = HttpRequest.post(url).header("Authorization", authorization).body(sql).execute().body();
        if (DebugConstant.open) {
            if (StringUtils.isNotBlank(DebugConstant.deviceNum)) {
                if (sql.contains(DebugConstant.deviceNum)) {
//                    log.info("执行sql:" + sql);
//                    log.info("执行结果：" + result);
                }
            } else {
                log.info("执行sql:" + sql);
                log.info("执行结果：" + result);
            }
        }

        JSONObject jsonObject = JSONUtil.parseObj(result);
        if (!"succ".equals(jsonObject.getStr("status"))
                && (jsonObject.getInt("code") == null || jsonObject.getInt("code") != 0)) { // jsonObject.getInt("code")为空，新版的tdengine返回code为0表示成功
            log.error("执行失败：" + result);
            log.error("失败sql:" + sql);
            return R.fail(jsonObject.getStr("desc"));
        }
        return R.ok(jsonObject);
    }

    /**
     * 设备数据存储
     *
     * @param deviceDataDTO 设备数据
     */
    @Override
    public R<JSONObject> insertTsData(DeviceDataDTO deviceDataDTO) {
        if (deviceDataDTO == null || StringUtils.isBlank(deviceDataDTO.getDeviceNum())
                || deviceDataDTO.getData() == null || deviceDataDTO.getData().isEmpty()) {
            return R.fail("设备数据不能为空");
        }
        try {
            // 检查是否为安全帽超级表
            if (TdengineSuperTableConstant.HELMET_RUNDE_CA_REPORT_LOCATION.equals(deviceDataDTO.getDeviceNum())) {
                return insertHelmetSuperTableData(deviceDataDTO);
            }

            // 原有的普通表逻辑
            StringBuilder insertSqlBuilder = new StringBuilder("insert into ")
                    .append(dbname).append(".").append(deviceDataDTO.getDeviceNum()).append(" (time,");

            StringBuilder fieldBuilder = new StringBuilder();
            StringBuilder valBuilder = new StringBuilder().append(DateUtil.date().getTime()).append(",");

            deviceDataDTO.getData().forEach((k, v) -> {
                k = k.toLowerCase();
                fieldBuilder.append(k).append(",");
                if (v != null && !isNumeric(String.valueOf(v))) {
                    valBuilder.append("'").append(v).append("',");
                } else {
                    valBuilder.append(v).append(",");
                }

            });

            String field = fieldBuilder.substring(0, fieldBuilder.length() - 1) + ")";
            String val = valBuilder.substring(0, valBuilder.length() - 1) + ")";
            String insertSql = insertSqlBuilder.append(field).append(" values (").append(val).toString();
            R<JSONObject> executeR = execute(insertSql);
            if (executeR.getCode() == R.FAIL && "Table does not exist".equals(executeR.getMsg())) {
                // 如果表不存在，先创建表，再执行一次插入操作
                createTsTable(deviceDataDTO);
                return execute(insertSql);
            }
            return executeR;
        } catch (Exception e) {
            log.error("TDengine存储设备[{}]数据异常", deviceDataDTO.getData(), e);
            return R.fail(e.getMessage());
        }
    }

    /**
     * 安全帽超级表数据插入
     * 
     * @param deviceDataDTO 设备数据
     * @return 插入结果
     * @author Shawn
     * @date 2025-05-31
     */
    private R<JSONObject> insertHelmetSuperTableData(DeviceDataDTO deviceDataDTO) {
        try {
            Map<String, Object> data = deviceDataDTO.getData();
            String deviceId = (String) data.get("device_id");
            String idCard = (String) data.get("id_card");

            if (StringUtils.isBlank(deviceId)) {
                return R.fail("设备ID不能为空");
            }

            // 先检查数据库状态
            checkDatabaseStatus();

            // 确保超级表存在
            R<JSONObject> createResult = ensureHelmetSuperTableExists(data);
            if (createResult.getCode() == R.FAIL) {
                return createResult;
            }

            // 确保子表存在
            String subTableName = generateSubTableName(deviceId, idCard);
            R<JSONObject> subTableResult = ensureHelmetSubTableExists(subTableName, deviceId, idCard);
            if (subTableResult.getCode() == R.FAIL) {
                return subTableResult;
            }

            // 构建插入SQL（插入到子表）
            StringBuilder insertSqlBuilder = new StringBuilder("insert into ")
                    .append(dbname).append(".").append(subTableName).append(" (time,");

            StringBuilder fieldBuilder = new StringBuilder();
            StringBuilder valBuilder = new StringBuilder().append(DateUtil.date().getTime()).append(",");

            // 排除device_id和id_card，因为它们是标签
            data.forEach((k, v) -> {
                if (!"device_id".equals(k) && !"id_card".equals(k)) {
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

            log.info("安全帽超级表插入SQL: {}", insertSql);
            return execute(insertSql);

        } catch (Exception e) {
            log.error("安全帽超级表数据插入异常", e);
            return R.fail("插入失败: " + e.getMessage());
        }
    }

    /**
     * 检查数据库状态 - 用于诊断
     * 
     * @author Shawn
     * @date 2025-05-31
     */
    private void checkDatabaseStatus() {
        try {
            log.info("=== 开始检查数据库状态 ===");

            // 检查超级表是否存在 - 使用指定数据库
            String checkSuperTableSql = "show " + dbname + ".stables";
            log.info("检查超级表SQL: {}", checkSuperTableSql);

            R<JSONObject> stablesResult = execute(checkSuperTableSql);
            boolean superTableExists = false;
            if (stablesResult.getCode() == R.SUCCESS) {
                JSONObject resultData = stablesResult.getData();
                JSONArray dataArray = resultData.getJSONArray("data");
                if (dataArray != null) {
                    for (int i = 0; i < dataArray.size(); i++) {
                        JSONArray row = dataArray.getJSONArray(i);
                        if (row != null && row.size() > 0) {
                            String tableName = row.getStr(0);
                            if (TdengineSuperTableConstant.HELMET_RUNDE_CA_REPORT_LOCATION.equals(tableName)) {
                                superTableExists = true;
                                break;
                            }
                        }
                    }
                }
            }

            // 只检查安全帽相关的表
            String showHelmetTablesSql = "show " + dbname + ".tables like 'helmet_%'";
            R<JSONObject> tablesResult = execute(showHelmetTablesSql);
            if (tablesResult.getCode() == R.SUCCESS) {
                JSONObject resultData = tablesResult.getData();
                JSONArray dataArray = resultData.getJSONArray("data");
                log.info("安全帽相关表数量: {}", dataArray != null ? dataArray.size() : 0);
                if (dataArray != null && dataArray.size() > 0) {
                    for (int i = 0; i < dataArray.size(); i++) {
                        JSONArray row = dataArray.getJSONArray(i);
                        if (row != null && row.size() > 0) {
                            String tableName = row.getStr(0);
                            log.info("安全帽子表: {}", tableName);
                        }
                    }
                }
            } else {
                log.error("查询安全帽表失败: {}", tablesResult.getMsg());
            }

            log.info("=== 数据库状态检查完成 ===");

        } catch (Exception e) {
            log.error("检查数据库状态时发生异常", e);
        }
    }

    /**
     * 确保安全帽超级表存在
     * 
     * @param sampleData 样本数据用于确定字段类型
     * @return 创建结果
     * @author Shawn
     * @date 2025-05-31
     */
    private R<JSONObject> ensureHelmetSuperTableExists(Map<String, Object> sampleData) {
        try {
            // 检查超级表是否存在 - 使用指定数据库
            String checkSql = "show " + dbname + ".stables";
            R<JSONObject> checkResult = execute(checkSql);

            if (checkResult.getCode() == R.SUCCESS) {
                JSONObject resultData = checkResult.getData();
                JSONArray dataArray = resultData.getJSONArray("data");
                if (dataArray != null && dataArray.size() > 0) {
                    // 检查是否存在helmet_runde_ca_report_location超级表
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
                    .append(dbname).append("." +TdengineSuperTableConstant.HELMET_RUNDE_CA_REPORT_LOCATION+ " (time TIMESTAMP");

            // 添加数据字段（排除device_id和id_card，因为它们是标签）
            sampleData.forEach((key, value) -> {
                if (!"device_id".equals(key) && !"id_card".equals(key)) {
                    createSuperTableSql.append(",").append(key.toLowerCase()).append(" ").append(getColumnType(value));
                }
            });

            // 添加标签定义 - 包含device_id和id_card
            createSuperTableSql.append(") TAGS (device_id NCHAR(50), id_card NCHAR(50))");

            String sql = createSuperTableSql.toString();
            log.info("创建安全帽超级表SQL: {}", sql);

            R<JSONObject> result = execute(sql);
            if (result.getCode() == R.SUCCESS) {
                log.info("安全帽超级表创建成功");
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
            // 检查子表是否存在 - 使用指定数据库和表名过滤
            String checkSql = "show " + dbname + ".tables like '" + subTableName + "'";
            R<JSONObject> checkResult = execute(checkSql);

            if (checkResult.getCode() == R.SUCCESS) {
                JSONObject resultData = checkResult.getData();
                JSONArray dataArray = resultData.getJSONArray("data");
                if (dataArray != null && dataArray.size() > 0) {
                    // 检查是否存在指定的子表
                    for (int i = 0; i < dataArray.size(); i++) {
                        JSONArray row = dataArray.getJSONArray(i);
                        if (row != null && row.size() > 0) {
                            String tableName = row.getStr(0);
                            if (subTableName.equals(tableName)) {
                                log.info("安全帽子表已存在: {}", subTableName);
                                return R.ok();
                            }
                        }
                    }
                }
            }

            // 子表不存在，创建子表
            // 如果身份证ID为空，使用空字符串
            String idCardValue = (idCard != null && !idCard.trim().isEmpty()) ? idCard : "";
            String createSubTableSql = "create table if not exists " + dbname + "." + subTableName
                    + " using " + dbname + "."+TdengineSuperTableConstant.HELMET_RUNDE_CA_REPORT_LOCATION+" tags ('" + deviceId + "', '" + idCardValue
                    + "')";

            log.info("创建安全帽子表SQL: {}", createSubTableSql);

            R<JSONObject> result = execute(createSubTableSql);
            if (result.getCode() == R.SUCCESS) {
                log.info("安全帽子表创建成功: {}, 设备ID: {}, 身份证ID: {}", subTableName, deviceId, idCardValue);
            } else {
                log.error("安全帽子表创建失败: {}, 错误: {}", subTableName, result.getMsg());

                // 如果是因为超级表不存在导致的错误，先检查超级表
                if (result.getMsg() != null && result.getMsg().contains("super table")) {
                    log.warn("可能是超级表不存在，尝试重新检查超级表状态");
                    return R.fail("超级表可能不存在，请检查超级表状态");
                }
            }

            return result;

        } catch (Exception e) {
            log.error("确保安全帽子表存在时发生异常, subTableName: {}", subTableName, e);
            return R.fail("创建子表失败: " + e.getMessage());
        }
    }

    /**
     * 判断是否为数字类型，利用 Java 内置的 Double.parseDouble 判断（更高效）
     *
     * @param strV 输入字符串
     * @return true表示是数字
     */
    public static boolean isNumeric(String strV) {
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
     * 创建时序库表
     *
     * @param deviceDataDTO 设备数据
     * @return
     */
    private R createTsTable(DeviceDataDTO deviceDataDTO) {
        StringBuilder sqlBuilder = new StringBuilder("create table if not exists ").append(dbname)
                .append(".").append(deviceDataDTO.getDeviceNum());

        StringBuilder columnBuilder = new StringBuilder(" (time TIMESTAMP");
        deviceDataDTO.getData().forEach((key, value) -> {
            columnBuilder.append(",").append(key).append(" ").append(getColumnType(value));
        });
        String sql = sqlBuilder.append(columnBuilder).append(")").toString();
        log.info("创建表 sql:" + sql);
        R<JSONObject> jsonObjectR = execute(sql);
        log.info("创建时序库表返回结果：" + jsonObjectR.toString());
        return jsonObjectR;
    }

    /**
     * 获取列类型
     *
     * @param value
     * @return
     */
    private String getColumnType(Object value) {
        if (value instanceof String) {
            return "NCHAR(20)";
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
            return "NCHAR(20)";
        }
    }

    /**
     * 查询对应表中的最后一条记录
     * tdengine:R(code=0, msg=null,
     * data={"status":"succ","head":["ts","running","starting"],"column_meta":[["ts",9,8],["running",10,64],["starting",10,64]],"data":[["2023-01-06
     * 10:24:41.820","false","false"]],"rows":1})
     *
     * @param deviceNum 设备编号
     * @return
     */
    @Override
    public Map<String, Object> getLastRow(String deviceNum) {
        // 检查是否为安全帽超级表查询
        if (TdengineSuperTableConstant.HELMET_RUNDE_CA_REPORT_LOCATION.equals(deviceNum)) {
            return getLastRowForHelmetSuperTable();
        }

        // 原有的普通表查询逻辑
        String sql = "select LAST_ROW(*) from " + dbname + "." + deviceNum;
        // 查询最新一条记录
        R<JSONObject> lastRowR = execute(sql);
        if (lastRowR.getCode() == R.SUCCESS) {
            List<Map<String, Object>> mapList = queryResultProcess(lastRowR.getData());
            if (!mapList.isEmpty()) {
                return mapList.get(0);
            }
        }
        return new HashMap<>();
    }

    /**
     * 获取安全帽超级表最新记录
     * 
     * @return 最新记录
     * @author Shawn
     * @date 2025-05-31
     */
    private Map<String, Object> getLastRowForHelmetSuperTable() {
        try {
            String sql = "select LAST_ROW(*) from " + dbname + "." + TdengineSuperTableConstant.HELMET_RUNDE_CA_REPORT_LOCATION;
            log.info("安全帽超级表最新记录查询SQL: {}", sql);

            R<JSONObject> lastRowR = execute(sql);
            if (lastRowR.getCode() == R.SUCCESS) {
                List<Map<String, Object>> mapList = queryResultProcess(lastRowR.getData());
                if (!mapList.isEmpty()) {
                    return mapList.get(0);
                }
            } else {
                log.error("安全帽超级表最新记录查询失败: {}", lastRowR.getMsg());
            }

        } catch (Exception e) {
            log.error("获取安全帽超级表最新记录异常", e);
        }

        return new HashMap<>();
    }

    /**
     * 查询结果处理
     * 返回格式：[{"ts":"2023-08-30
     * 00:00:01.000","temperature":16.0001,"humidity":34.0002},{"ts":"2023-08-30
     * 00:00:02.000","temperature":17.0001,"humidity":34.0002}]
     *
     * @param jsonObject 查询结果
     * @return
     */
    private List<Map<String, Object>> queryResultProcess(JSONObject jsonObject) {
        JSONArray head = jsonObject.getJSONArray("head");
        JSONArray data = jsonObject.getJSONArray("data");
        List<Map<String, Object>> list = new LinkedList<>();
        data.forEach(obj -> {
            Map<String, Object> valMap = new TreeMap<>();
            JSONArray arr = JSONUtil.parseArray(obj);
            for (int i = 0; i < arr.size(); i++) {
                valMap.put(head.getStr(i), arr.getStr(i));
            }
            list.add(valMap);
        });
        return list;
    }

    /**
     * 分页查询
     *
     * @param param 查询参数
     * @return
     */
    @Override
    public R listPage(QueryParamDTO param) {
        if (param == null || StringUtils.isBlank(param.getDeviceCode())) {
            return R.fail("设备编号不能为空");
        }

        // 检查是否为安全帽超级表查询
        if (TdengineSuperTableConstant.HELMET_RUNDE_CA_REPORT_LOCATION.equals(param.getDeviceCode())) {
            return listPageForHelmetSuperTable(param);
        }

        // 原有的普通表查询逻辑
        Map<String, Object> map = new HashMap<>();
        // 组装sql
        String sql = "select ";

        if (StringUtils.isNotBlank(param.getColumn())) {
            sql += param.getColumn() + " ";
        } else {
            sql += "* ";
        }
        String tableName = dbname + "." + param.getDeviceCode();
        sql += "from " + tableName + " ";

        if (StringUtils.isNotBlank(param.getStartTime()) && StringUtils.isNotBlank(param.getEndTime())) {
            sql += "where time>='" + param.getStartTime() + "' and time<='" + param.getEndTime() + "' ";
        }
        if (param.getPageNum() == null || param.getPageNum() == 0) {
            param.setPageNum(1);
        }
        if (param.getPageSize() == null || param.getPageSize() == 0) {
            param.setPageSize(10);
        }

        int startIndex = (param.getPageNum() - 1) * param.getPageSize();
        sql += "limit " + startIndex + "," + param.getPageSize();

        R<JSONObject> result = execute(sql);
        if (R.SUCCESS == result.getCode()) {
            map.put("rows", queryResultProcess(result.getData()));
        } else {
            log.info("分页查询sql:" + sql);
            return R.fail(result.getMsg());
        }

        map.put("total", count(param));
        return R.ok(map);
    }

    /**
     * 安全帽超级表分页查询
     * 
     * @param param 查询参数
     * @return 查询结果
     * @author Shawn
     * @date 2025-05-31
     */
    private R listPageForHelmetSuperTable(QueryParamDTO param) {
        try {
            Map<String, Object> map = new HashMap<>();

            // 组装SQL - 查询超级表
            String sql = "select ";

            if (StringUtils.isNotBlank(param.getColumn())) {
                sql += param.getColumn() + " ";
            } else {
                sql += "* ";
            }

            sql += "from " + dbname + "."+TdengineSuperTableConstant.HELMET_RUNDE_CA_REPORT_LOCATION+" ";

            // 添加时间条件
            if (StringUtils.isNotBlank(param.getStartTime()) && StringUtils.isNotBlank(param.getEndTime())) {
                sql += "where time>='" + param.getStartTime() + "' and time<='" + param.getEndTime() + "' ";
            }

            // 添加排序
            sql += "order by time desc ";

            // 分页参数处理
            if (param.getPageNum() == null || param.getPageNum() == 0) {
                param.setPageNum(1);
            }
            if (param.getPageSize() == null || param.getPageSize() == 0) {
                param.setPageSize(10);
            }

            int startIndex = (param.getPageNum() - 1) * param.getPageSize();
            sql += "limit " + startIndex + "," + param.getPageSize();

            log.info("安全帽超级表分页查询SQL: {}", sql);

            R<JSONObject> result = execute(sql);
            if (R.SUCCESS == result.getCode()) {
                map.put("rows", queryResultProcess(result.getData()));
            } else {
                log.error("安全帽超级表分页查询失败, SQL: {}, 错误: {}", sql, result.getMsg());
                return R.fail(result.getMsg());
            }

            map.put("total", countForHelmetSuperTable(param));
            return R.ok(map);

        } catch (Exception e) {
            log.error("安全帽超级表分页查询异常", e);
            return R.fail("查询失败: " + e.getMessage());
        }
    }

    /**
     * 查询总数
     *
     * @param param 查询参数
     * @return
     */
    @Override
    public Long count(QueryParamDTO param) {
        // 检查是否为安全帽超级表查询
        if (TdengineSuperTableConstant.HELMET_RUNDE_CA_REPORT_LOCATION.equals(param.getDeviceCode())) {
            return countForHelmetSuperTable(param);
        }

        // 原有的普通表统计逻辑
        String countSql = "select count(1) from ";
        String tableName = dbname + "." + param.getDeviceCode();
        countSql += tableName + " ";
        if (StringUtils.isNotBlank(param.getStartTime()) && StringUtils.isNotBlank(param.getEndTime())) {
            countSql += "where time>='" + param.getStartTime() + "' and time<='" + param.getEndTime() + "' ";
        }

        R<JSONObject> result = execute(countSql);
        if (R.SUCCESS == result.getCode()) {
            JSONObject jsonObject = result.getData();
            JSONArray data = jsonObject.getJSONArray("data");

            if (data != null && data.size() == 1) {
                return data.getJSONArray(0).getLong(0);
            }

        } else {
            log.error("查询总数countSql:" + countSql);
        }
        return 0L;
    }

    /**
     * 安全帽超级表数据统计
     * 
     * @param param 查询参数
     * @return 数据总数
     * @author Shawn
     * @date 2025-05-31
     */
    private Long countForHelmetSuperTable(QueryParamDTO param) {
        try {
            String countSql = "select count(1) from " + dbname + "." +TdengineSuperTableConstant.HELMET_RUNDE_CA_REPORT_LOCATION+" ";

            if (StringUtils.isNotBlank(param.getStartTime()) && StringUtils.isNotBlank(param.getEndTime())) {
                countSql += "where time>='" + param.getStartTime() + "' and time<='" + param.getEndTime() + "' ";
            }

            log.info("安全帽超级表统计SQL: {}", countSql);

            R<JSONObject> result = execute(countSql);
            if (R.SUCCESS == result.getCode()) {
                JSONObject jsonObject = result.getData();
                JSONArray data = jsonObject.getJSONArray("data");

                if (data != null && data.size() == 1) {
                    return data.getJSONArray(0).getLong(0);
                }
            } else {
                log.error("安全帽超级表统计失败, SQL: {}, 错误: {}", countSql, result.getMsg());
            }

        } catch (Exception e) {
            log.error("安全帽超级表统计异常", e);
        }

        return 0L;
    }

    /**
     * 重建超级表
     * 
     * @return 重建结果
     * @author Shawn
     * @date 2025-05-31
     */
    private R<String> rebuildSuperTable() {
        try {
            // 删除旧的超级表（如果存在）
            String dropSuperTableSql = "drop stable if exists " + dbname + "."+TdengineSuperTableConstant.HELMET_RUNDE_CA_REPORT_LOCATION;
            log.info("删除超级表SQL: {}", dropSuperTableSql);
            R<JSONObject> dropResult = execute(dropSuperTableSql);
            if (dropResult.getCode() != R.SUCCESS) {
                log.error("删除超级表失败: {}", dropResult.getMsg());
                return R.fail("删除超级表失败: " + dropResult.getMsg());
            }

            // 重新创建超级表（传入空的数据用于创建表结构）
            Map<String, Object> emptyData = new HashMap<>();
            ensureHelmetSuperTableExists(emptyData);

            log.info("重建超级表 helmet_runde_ca_report_location 成功");
            return R.ok("重建超级表成功");

        } catch (Exception e) {
            log.error("重建超级表时发生异常", e);
            return R.fail("重建超级表失败: " + e.getMessage());
        }
    }


    @Override
    public R<JSONObject> executeTDengineSQL(String sql) {

        String dbNameNew = dbname;
//        String corpCode = CorpUtils.getCurrentCorpCode();
        String corpCode = TenantContext.get();
        if (StringUtils.isNotBlank(corpCode)) {
            dbNameNew = CorpDbEnum.getDbNameByCorpCode(corpCode);
        }

        // 3. 替换 SQL 中的占位 {db} 为真实数据库名
        String realSql = sql.replace(dbname, dbNameNew);

        try {

            String result = okHttpClientManager.post(url, authorization, realSql);

            JSONObject jsonObject = JSONUtil.parseObj(result);
            if (!"succ".equals(jsonObject.getStr("status"))
                    && (jsonObject.getInt("code") == null || jsonObject.getInt("code") != 0)) {
                log.error("SQL执行失败: {}", result);
                log.error("失败SQL: {}", realSql);
                return R.fail(jsonObject.getStr("desc"));
            }
            return R.ok(jsonObject);
        } catch (Exception e) {
            log.error("执行TDengine SQL异常: {}", realSql, e);
            return R.fail("SQL执行异常: " + e.getMessage());
        }
    }

    @Override
    public R<JSONObject> executeTDengineSQLByXXJOB(String sql,String corpCode) {

        String dbNameNew = dbname;
        if (StringUtils.isBlank(corpCode)){
            corpCode = CorpUtils.getCurrentCorpCode();
        }
        if (StringUtils.isNotBlank(corpCode)) {
            dbNameNew = CorpDbEnum.getDbNameByCorpCode(corpCode);
        }

        // 3. 替换 SQL 中的占位 {db} 为真实数据库名
        String realSql = sql.replace(dbname, dbNameNew);

        XxlJobHelper.log("定时任务查询SQL: {}", realSql);

        try {

            String result = okHttpClientManager.post(url, authorization, realSql);

            JSONObject jsonObject = JSONUtil.parseObj(result);
            if (!"succ".equals(jsonObject.getStr("status"))
                    && (jsonObject.getInt("code") == null || jsonObject.getInt("code") != 0)) {
                log.error("SQL执行失败: {}", result);
                log.error("失败SQL: {}", realSql);
                return R.fail(jsonObject.getStr("desc"));
            }
            return R.ok(jsonObject);
        } catch (Exception e) {
            log.error("执行TDengine SQL异常: {}", realSql, e);
            return R.fail("SQL执行异常: " + e.getMessage());
        }
    }


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
        return TdengineSuperTableConstant.HELMET_RUNDE_CA_REPORT_LOCATION+"_" + deviceId + "_" + safeIdCard;
    }
}
