package cn.iocoder.yudao.module.swm.dal.tdengine;
import cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder;
import cn.iocoder.yudao.module.swm.enums.TdengineSuperTableConstants;
import cn.iocoder.yudao.module.swm.enums.SwmRedisKeyConstants;
import cn.iocoder.yudao.module.swm.api.enums.CorpDbEnum;
import cn.iocoder.yudao.module.swm.dal.dataobject.DeviceDataDTO;
import cn.iocoder.yudao.module.swm.dal.dataobject.QueryParamDTO;

import cn.hutool.core.date.DateUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.swm.config.OkHttpClientManager;
import com.xxl.job.core.context.XxlJobHelper;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * tdengine服务实现类
 *
 * @author: cjie
 * @date: 2023/1/3
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TdengineRestClient {
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
    @Resource
    private ThreadPoolTaskExecutor swmExecutor;

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
            CommonResult<JSONObject> result = execute(sql);
            if (0 != result.getCode()) {
                log.error("创建数据库失败: {}", result.getMsg());
            }
        }
    }

    /**
     * 执行任意sql
     *
     * @param sql 操作语句
     */
    public CommonResult<JSONObject> execute(String sql) {
        String result = HttpRequest.post(url).header("Authorization", authorization).body(sql).execute().body();
        log.info("执行sql: {}", sql);
        log.info("执行结果: {}", result);

        JSONObject jsonObject = JSONUtil.parseObj(result);
        if (!"succ".equals(jsonObject.getStr("status"))
                && (jsonObject.getInt("code") == null || jsonObject.getInt("code") != 0)) {
            log.error("执行失败: {}", result);
            log.error("失败sql: {}", sql);
            return CommonResult.error(1, jsonObject.getStr("desc"));
        }
        return CommonResult.success(jsonObject);
    }

    /**
     * 设备数据存储
     *
     * @param deviceDataDTO 设备数据
     */
    public CommonResult<JSONObject> insertTsData(DeviceDataDTO deviceDataDTO) {
        if (deviceDataDTO == null || StringUtils.isBlank(deviceDataDTO.getDeviceNum())
                || deviceDataDTO.getData() == null || deviceDataDTO.getData().isEmpty()) {
            return CommonResult.error(1, "设备数据不能为空");
        }
        try {
            // 检查是否为安全帽超级表
            if (TdengineSuperTableConstants.HELMET_RUNDE_CA_REPORT_LOCATION.equals(deviceDataDTO.getDeviceNum())) {
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
            CommonResult<JSONObject> executeR = execute(insertSql);
            if (!executeR.isSuccess() && "Table does not exist".equals(executeR.getMsg())) {
                // 如果表不存在，先创建表，再执行一次插入操作
                createTsTable(deviceDataDTO);
                return execute(insertSql);
            }
            return executeR;
        } catch (Exception e) {
            log.error("TDengine存储设备[{}]数据异常", deviceDataDTO.getData(), e);
            return CommonResult.error(1, e.getMessage());
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
    private CommonResult<JSONObject> insertHelmetSuperTableData(DeviceDataDTO deviceDataDTO) {
        try {
            Map<String, Object> data = deviceDataDTO.getData();
            String deviceId = (String) data.get("device_id");
            String idCard = (String) data.get("id_card");

            if (StringUtils.isBlank(deviceId)) {
                return CommonResult.error(1, "设备ID不能为空");
            }

            // 先检查数据库状态
            checkDatabaseStatus();

            // 确保超级表存在
            CommonResult<JSONObject> createResult = ensureHelmetSuperTableExists(data);
            if (!createResult.isSuccess()) {
                return createResult;
            }

            // 确保子表存在
            String subTableName = generateSubTableName(deviceId, idCard);
            CommonResult<JSONObject> subTableResult = ensureHelmetSubTableExists(subTableName, deviceId, idCard);
            if (!subTableResult.isSuccess()) {
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
            return CommonResult.error(1, "插入失败: " + e.getMessage());
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

            CommonResult<JSONObject> stablesResult = execute(checkSuperTableSql);
            boolean superTableExists = false;
            if (stablesResult.getCode() == 0) {
                JSONObject resultData = stablesResult.getData();
                JSONArray dataArray = resultData.getJSONArray("data");
                if (dataArray != null) {
                    for (int i = 0; i < dataArray.size(); i++) {
                        JSONArray row = dataArray.getJSONArray(i);
                        if (row != null && row.size() > 0) {
                            String tableName = row.getStr(0);
                            if (TdengineSuperTableConstants.HELMET_RUNDE_CA_REPORT_LOCATION.equals(tableName)) {
                                superTableExists = true;
                                break;
                            }
                        }
                    }
                }
            }

            // 只检查安全帽相关的表
            String showHelmetTablesSql = "show " + dbname + ".tables like 'helmet_%'";
            CommonResult<JSONObject> tablesResult = execute(showHelmetTablesSql);
            if (tablesResult.getCode() == 0) {
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
    private CommonResult<JSONObject> ensureHelmetSuperTableExists(Map<String, Object> sampleData) {
        try {
            // 检查超级表是否存在 - 使用指定数据库
            String checkSql = "show " + dbname + ".stables";
            CommonResult<JSONObject> checkResult = execute(checkSql);

            if (checkResult.getCode() == 0) {
                JSONObject resultData = checkResult.getData();
                JSONArray dataArray = resultData.getJSONArray("data");
                if (dataArray != null && dataArray.size() > 0) {
                    // 检查是否存在helmet_runde_ca_report_location超级表
                    for (int i = 0; i < dataArray.size(); i++) {
                        JSONArray row = dataArray.getJSONArray(i);
                        if (row != null && row.size() > 0) {
                            String tableName = row.getStr(0);
                            if (TdengineSuperTableConstants.HELMET_RUNDE_CA_REPORT_LOCATION.equals(tableName)) {
                                log.info("安全帽超级表已存在: {}", tableName);
                                return CommonResult.success(new JSONObject());
                            }
                        }
                    }
                }
            }

            // 超级表不存在，创建超级表
            StringBuilder createSuperTableSql = new StringBuilder("create stable if not exists ")
                    .append(dbname).append("." +TdengineSuperTableConstants.HELMET_RUNDE_CA_REPORT_LOCATION+ " (time TIMESTAMP");

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

            CommonResult<JSONObject> result = execute(sql);
            if (result.getCode() == 0) {
                log.info("安全帽超级表创建成功");
            } else {
                log.error("安全帽超级表创建失败: {}", result.getMsg());
            }

            return result;

        } catch (Exception e) {
            log.error("确保安全帽超级表存在时发生异常", e);
            return CommonResult.error(1, "创建超级表失败: " + e.getMessage());
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
    private CommonResult<JSONObject> ensureHelmetSubTableExists(String subTableName, String deviceId, String idCard) {
        try {
            // 检查子表是否存在 - 使用指定数据库和表名过滤
            String checkSql = "show " + dbname + ".tables like '" + subTableName + "'";
            CommonResult<JSONObject> checkResult = execute(checkSql);

            if (checkResult.getCode() == 0) {
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
                                return CommonResult.success(new JSONObject());
                            }
                        }
                    }
                }
            }

            // 子表不存在，创建子表
            // 如果身份证ID为空，使用空字符串
            String idCardValue = (idCard != null && !idCard.trim().isEmpty()) ? idCard : "";
            String createSubTableSql = "create table if not exists " + dbname + "." + subTableName
                    + " using " + dbname + "."+TdengineSuperTableConstants.HELMET_RUNDE_CA_REPORT_LOCATION+" tags ('" + deviceId + "', '" + idCardValue
                    + "')";

            log.info("创建安全帽子表SQL: {}", createSubTableSql);

            CommonResult<JSONObject> result = execute(createSubTableSql);
            if (result.getCode() == 0) {
                log.info("安全帽子表创建成功: {}, 设备ID: {}, 身份证ID: {}", subTableName, deviceId, idCardValue);
            } else {
                log.error("安全帽子表创建失败: {}, 错误: {}", subTableName, result.getMsg());

                // 如果是因为超级表不存在导致的错误，先检查超级表
                if (result.getMsg() != null && result.getMsg().contains("super table")) {
                    log.warn("可能是超级表不存在，尝试重新检查超级表状态");
                    return CommonResult.error(1, "超级表可能不存在，请检查超级表状态");
                }
            }

            return result;

        } catch (Exception e) {
            log.error("确保安全帽子表存在时发生异常, subTableName: {}", subTableName, e);
            return CommonResult.error(1, "创建子表失败: " + e.getMessage());
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
    private CommonResult createTsTable(DeviceDataDTO deviceDataDTO) {
        StringBuilder sqlBuilder = new StringBuilder("create table if not exists ").append(dbname)
                .append(".").append(deviceDataDTO.getDeviceNum());

        StringBuilder columnBuilder = new StringBuilder(" (time TIMESTAMP");
        deviceDataDTO.getData().forEach((key, value) -> {
            columnBuilder.append(",").append(key).append(" ").append(getColumnType(value));
        });
        String sql = sqlBuilder.append(columnBuilder).append(")").toString();
        log.info("创建表 sql:" + sql);
        CommonResult<JSONObject> jsonObjectR = execute(sql);
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
    public Map<String, Object> getLastRow(String deviceNum) {
        // 检查是否为安全帽超级表查询
        if (TdengineSuperTableConstants.HELMET_RUNDE_CA_REPORT_LOCATION.equals(deviceNum)) {
            return getLastRowForHelmetSuperTable();
        }

        // 原有的普通表查询逻辑
        String sql = "select LAST_ROW(*) from " + dbname + "." + deviceNum;
        // 查询最新一条记录
        CommonResult<JSONObject> lastRowR = execute(sql);
        if (lastRowR.getCode() == 0) {
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
            String sql = "select LAST_ROW(*) from " + dbname + "." + TdengineSuperTableConstants.HELMET_RUNDE_CA_REPORT_LOCATION;
            log.info("安全帽超级表最新记录查询SQL: {}", sql);

            CommonResult<JSONObject> lastRowR = execute(sql);
            if (lastRowR.getCode() == 0) {
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
    public CommonResult listPage(QueryParamDTO param) {
        if (param == null || StringUtils.isBlank(param.getDeviceCode())) {
            return CommonResult.error(1, "设备编号不能为空");
        }

        // 检查是否为安全帽超级表查询
        if (TdengineSuperTableConstants.HELMET_RUNDE_CA_REPORT_LOCATION.equals(param.getDeviceCode())) {
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

        CommonResult<JSONObject> result = execute(sql);
        if (0 == result.getCode()) {
            map.put("rows", queryResultProcess(result.getData()));
        } else {
            log.info("分页查询sql:" + sql);
            return CommonResult.error(1, result.getMsg());
        }

        map.put("total", count(param));
        return CommonResult.success(map);
    }

    /**
     * 安全帽超级表分页查询
     *
     * @param param 查询参数
     * @return 查询结果
     * @author Shawn
     * @date 2025-05-31
     */
    private CommonResult listPageForHelmetSuperTable(QueryParamDTO param) {
        try {
            Map<String, Object> map = new HashMap<>();

            // 组装SQL - 查询超级表
            String sql = "select ";

            if (StringUtils.isNotBlank(param.getColumn())) {
                sql += param.getColumn() + " ";
            } else {
                sql += "* ";
            }

            sql += "from " + dbname + "."+TdengineSuperTableConstants.HELMET_RUNDE_CA_REPORT_LOCATION+" ";

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

            CommonResult<JSONObject> result = execute(sql);
            if (0 == result.getCode()) {
                map.put("rows", queryResultProcess(result.getData()));
            } else {
                log.error("安全帽超级表分页查询失败, SQL: {}, 错误: {}", sql, result.getMsg());
                return CommonResult.error(1, result.getMsg());
            }

            map.put("total", countForHelmetSuperTable(param));
            return CommonResult.success(map);

        } catch (Exception e) {
            log.error("安全帽超级表分页查询异常", e);
            return CommonResult.error(1, "查询失败: " + e.getMessage());
        }
    }

    /**
     * 查询总数
     *
     * @param param 查询参数
     * @return
     */
    public Long count(QueryParamDTO param) {
        // 检查是否为安全帽超级表查询
        if (TdengineSuperTableConstants.HELMET_RUNDE_CA_REPORT_LOCATION.equals(param.getDeviceCode())) {
            return countForHelmetSuperTable(param);
        }

        // 原有的普通表统计逻辑
        String countSql = "select count(1) from ";
        String tableName = dbname + "." + param.getDeviceCode();
        countSql += tableName + " ";
        if (StringUtils.isNotBlank(param.getStartTime()) && StringUtils.isNotBlank(param.getEndTime())) {
            countSql += "where time>='" + param.getStartTime() + "' and time<='" + param.getEndTime() + "' ";
        }

        CommonResult<JSONObject> result = execute(countSql);
        if (0 == result.getCode()) {
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
            String countSql = "select count(1) from " + dbname + "." +TdengineSuperTableConstants.HELMET_RUNDE_CA_REPORT_LOCATION+" ";

            if (StringUtils.isNotBlank(param.getStartTime()) && StringUtils.isNotBlank(param.getEndTime())) {
                countSql += "where time>='" + param.getStartTime() + "' and time<='" + param.getEndTime() + "' ";
            }

            log.info("安全帽超级表统计SQL: {}", countSql);

            CommonResult<JSONObject> result = execute(countSql);
            if (0 == result.getCode()) {
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
    private CommonResult<String> rebuildSuperTable() {
        try {
            // 删除旧的超级表（如果存在）
            String dropSuperTableSql = "drop stable if exists " + dbname + "."+TdengineSuperTableConstants.HELMET_RUNDE_CA_REPORT_LOCATION;
            log.info("删除超级表SQL: {}", dropSuperTableSql);
            CommonResult<JSONObject> dropResult = execute(dropSuperTableSql);
            if (dropResult.getCode() != 0) {
                log.error("删除超级表失败: {}", dropResult.getMsg());
                return CommonResult.error(1, "删除超级表失败: " + dropResult.getMsg());
            }

            // 重新创建超级表（传入空的数据用于创建表结构）
            Map<String, Object> emptyData = new HashMap<>();
            ensureHelmetSuperTableExists(emptyData);

            log.info("重建超级表 helmet_runde_ca_report_location 成功");
            return CommonResult.success("重建超级表成功");

        } catch (Exception e) {
            log.error("重建超级表时发生异常", e);
            return CommonResult.error(1, "重建超级表失败: " + e.getMessage());
        }
    }

    public CommonResult<JSONObject> executeTDengineSQL(String sql) {

        String dbNameNew = dbname;
        Long tenantId = TenantContextHolder.getTenantId();
        if (tenantId != null) {
            dbNameNew = CorpDbEnum.getDbNameByTenantId(tenantId);
        }

        // 3. 替换 SQL 中的占位 {db} 为真实数据库名
        String realSql = sql;
        if(!dbname.equals(dbNameNew)){
            realSql = sql.replace(dbname, dbNameNew);
        }

        log.info("======执行 TDengine 真实 SQL: {}===================", realSql);

        try {

            String result = okHttpClientManager.post(url, authorization, realSql);

            JSONObject jsonObject = JSONUtil.parseObj(result);
            if (!"succ".equals(jsonObject.getStr("status"))
                    && (jsonObject.getInt("code") == null || jsonObject.getInt("code") != 0)) {
                log.error("SQL执行失败: {}", result);
                log.error("失败SQL: {}", realSql);
                return CommonResult.error(1, jsonObject.getStr("desc"));
            }
            return CommonResult.success(jsonObject);
        } catch (Exception e) {
            log.error("执行TDengine SQL异常: {}", realSql, e);
            return CommonResult.error(1, "SQL执行异常: " + e.getMessage());
        }
    }

    public CommonResult<JSONObject> executeTDengineSQLByXXJOB(String sql, Long tenantId) {

        String dbNameNew = dbname;
        if (tenantId == null) {
            tenantId = TenantContextHolder.getTenantId();
        }
        if (tenantId != null) {
            dbNameNew = CorpDbEnum.getDbNameByTenantId(tenantId);
        }

        // 3. 替换 SQL 中的占位 {db} 为真实数据库名
        String realSql = sql;
        if(!dbname.equals(dbNameNew)){
            realSql = sql.replace(dbname, dbNameNew);
        }

        XxlJobHelper.log("定时任务查询SQL: {}", realSql);

        try {

            String result = okHttpClientManager.post(url, authorization, realSql);

            JSONObject jsonObject = JSONUtil.parseObj(result);
            if (!"succ".equals(jsonObject.getStr("status"))
                    && (jsonObject.getInt("code") == null || jsonObject.getInt("code") != 0)) {
                log.error("SQL执行失败: {}", result);
                log.error("失败SQL: {}", realSql);
                return CommonResult.error(1, jsonObject.getStr("desc"));
            }
            return CommonResult.success(jsonObject);
        } catch (Exception e) {
            log.error("执行TDengine SQL异常: {}", realSql, e);
            return CommonResult.error(1, "SQL执行异常: " + e.getMessage());
        }
    }

    /**
     * 根据租户ID执行TDengine SQL
     *
     * @param sql      SQL语句
     * @param tenantId 租户ID
     * @return 执行结果
     */
    public CommonResult<JSONObject> executeByTenantId(String sql, Long tenantId) {
        if (tenantId == null) {
            return CommonResult.error(1, "租户ID不能为空");
        }
        return executeTDengineSQLByXXJOB(sql, tenantId);
    }

    /**
     * 使用当前租户上下文执行SQL
     *
     * @param sql SQL语句
     * @return 执行结果
     */
    public CommonResult<JSONObject> executeByCurrentTenant(String sql) {
        Long tenantId = TenantContextHolder.getTenantId();
        return executeTDengineSQLByXXJOB(sql, tenantId);
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
        return TdengineSuperTableConstants.HELMET_RUNDE_CA_REPORT_LOCATION+"_" + deviceId + "_" + safeIdCard;
    }
}
