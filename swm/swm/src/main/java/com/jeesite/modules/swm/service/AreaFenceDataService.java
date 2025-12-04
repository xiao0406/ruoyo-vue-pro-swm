/**
 * 区域围栏数据服务类
 * @author Shawn
 * @date 2025-01-14
 */
package com.jeesite.modules.swm.service;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import com.jeesite.common.lang.StringUtils;
import com.jeesite.modules.swm.cache.DeviceCorpMappingCache;
import com.jeesite.modules.swm.constant.SwmRedisConstant;
import com.jeesite.modules.swm.entity.AreaFenceData;
import com.jeesite.modules.swm.entity.AttendanceCheckResult;
import com.jeesite.modules.swm.entity.SwmPerson;
import com.jeesite.modules.sys.entity.DictData;
import com.jeesite.modules.sys.utils.DictUtils;
import com.jeesite.modules.utils.R;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.Comparator;
import java.util.stream.Collectors;

/**
 * 区域围栏数据服务类
 */
@Service
public class AreaFenceDataService {

    private static final Logger logger = LoggerFactory.getLogger(AreaFenceDataService.class);

    @Autowired
    private TDengineService tdengineService;

    @Autowired
    private SwmPersonService swmPersonService;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private DeviceCorpMappingCache deviceCorpMappingCache;

    @Value("${tdengine.dbname:swm_db}")
    private String dbname;

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    private static final SimpleDateFormat DATETIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ISO_INSTANT;

    /**
     * 根据身份证查询考勤情况
     * 
     * @param idCard    身份证号
     * @param checkDate 查询日期（可选，默认当天）
     * @return 考勤检查结果
     */
    public AttendanceCheckResult checkAttendanceByIdCard(String idCard, String checkDate) {
        if (StringUtils.isBlank(idCard)) {
            throw new IllegalArgumentException("身份证号不能为空");
        }

        // 如果没有指定日期，使用当前日期
        if (StringUtils.isBlank(checkDate)) {
            checkDate = DATE_FORMAT.format(new Date());
        }

        logger.info("查询身份证 {} 在 {} 的考勤情况", idCard, checkDate);

        AttendanceCheckResult result = new AttendanceCheckResult();
        result.setIdCard(idCard);
        result.setCheckDate(checkDate);

        try {
            // 1. 查询员工信息
            SwmPerson person = getPersonByIdCard(idCard);
            if (person != null) {
                result.setEmployeeName(person.getName());
            }

            // 2. 获取考勤区域字典数据
            List<String> attendanceAreaIds = getAttendanceAreaIds();
            if (attendanceAreaIds.isEmpty()) {
                logger.warn("未找到考勤区域配置，字典类型：swm_area_attendance");
                result.setAttendanceStatus("考勤区域配置缺失");
                return result;
            }

            // 3. 设置班次（默认早班）
            result.setWorkShift("早班");

            // 4. 分别查询各项考勤指标，避免一次性加载大量数据

            // 5. 判断是否旷工 (当天有无数据)
            boolean isAbsent = checkIsAbsent(idCard, checkDate, attendanceAreaIds);
            result.setIsAbsent(isAbsent);

            if (isAbsent) {
                result.setIsLate(false);
                result.setIsEarlyLeave(false);
                result.setIdleHours(BigDecimal.valueOf(8.0)); // 全天旷工
                result.setAttendanceStatus("旷工");
                return result;
            }

            // 6. 判断迟到 (08:05之前是否有数据)
            boolean isLate = checkIsLate(idCard, checkDate, attendanceAreaIds);
            result.setIsLate(isLate);

            // 7. 判断早退 (15:55之后是否有数据)
            boolean isEarlyLeave = checkIsEarlyLeave(idCard, checkDate, attendanceAreaIds);
            result.setIsEarlyLeave(isEarlyLeave);

            // 8. 计算怠工时长
            BigDecimal idleHours = calculateIdleHours(idCard, checkDate, attendanceAreaIds);
            result.setIdleHours(idleHours);

            // 9. 设置考勤状态描述
            String status = generateAttendanceStatus(isLate, isEarlyLeave, idleHours);
            result.setAttendanceStatus(status);

            logger.info("身份证 {} 考勤检查完成：{}", idCard, status);

        } catch (Exception e) {
            logger.error("查询考勤数据失败", e);
            result.setAttendanceStatus("查询失败：" + e.getMessage());
        }

        return result;
    }

    /**
     * 根据身份证获取员工信息
     */
    private SwmPerson getPersonByIdCard(String idCard) {
        try {
            SwmPerson query = new SwmPerson();
            query.setIdentityCard(idCard);
            List<SwmPerson> persons = swmPersonService.findList(query);
            return persons.isEmpty() ? null : persons.get(0);
        } catch (Exception e) {
            logger.error("查询员工信息失败：{}", idCard, e);
            return null;
        }
    }

    /**
     * 获取考勤区域ID列表
     */
    private List<String> getAttendanceAreaIds() {
        try {
            List<DictData> dictList = DictUtils.getDictList("swm_area_attendance");
            List<String> areaIds = new ArrayList<>();
            for (DictData dict : dictList) {
                if (StringUtils.isNotBlank(dict.getDictValue())) {
                    areaIds.add(dict.getDictValue());
                }
            }
            return areaIds;
        } catch (Exception e) {
            logger.error("获取考勤区域字典数据失败", e);
            return new ArrayList<>();
        }
    }

    /**
     * 解析TDengine查询结果行数据
     */
    private AreaFenceData parseAreaFenceDataFromRow(JSONArray row) {
        try {
            AreaFenceData data = new AreaFenceData();

            // 解析时间戳 - 支持ISO 8601格式和普通格式
            String timeStr = row.getStr(0);
            if (StringUtils.isNotBlank(timeStr)) {
                Date parsedTime = parseTimestamp(timeStr);
                data.setTime(parsedTime);
            }

            // 解析坐标
            data.setX(row.getBigDecimal(1));
            data.setY(row.getBigDecimal(2));
            data.setAreaName(row.getStr(3));
            data.setAreaId(row.getStr(4));
            data.setDeviceId(row.getStr(5));
            data.setIdCard(row.getStr(6));

            return data;
        } catch (Exception e) {
            logger.error("解析TDengine数据行失败", e);
            return null;
        }
    }

    /**
     * 解析时间戳，支持多种格式
     */
    private Date parseTimestamp(String timeStr) {
        try {
            // 首先尝试解析ISO 8601格式 (2025-06-16T03:11:17.246Z)
            if (timeStr.contains("T") && timeStr.endsWith("Z")) {
                Instant instant = Instant.parse(timeStr);
                Date result = Date.from(instant);
                logger.debug("解析ISO时间戳: {} -> {}", timeStr, DATETIME_FORMAT.format(result));
                return result;
            }

            // 如果包含T但不以Z结尾，尝试其他ISO格式
            if (timeStr.contains("T")) {
                // 移除可能的时区信息，转为本地时间
                String localTimeStr = timeStr.replace("T", " ");
                if (localTimeStr.endsWith("Z")) {
                    localTimeStr = localTimeStr.substring(0, localTimeStr.length() - 1);
                }
                // 如果有毫秒，去掉毫秒部分
                if (localTimeStr.contains(".")) {
                    localTimeStr = localTimeStr.substring(0, localTimeStr.indexOf("."));
                }
                return DATETIME_FORMAT.parse(localTimeStr);
            }

            // 最后尝试标准格式
            return DATETIME_FORMAT.parse(timeStr);

        } catch (Exception e) {
            logger.error("解析时间戳失败: {}", timeStr, e);
            return null;
        }
    }

    /**
     * 检查是否旷工（当天有无数据）
     */
    private boolean checkIsAbsent(String idCard, String checkDate, List<String> attendanceAreaIds) {
        try {
            // 查询当天是否有任何数据
            StringBuilder sql = new StringBuilder();
            sql.append("SELECT COUNT(*) ");
            sql.append("FROM ").append(dbname).append(".area_fence_data ");
            sql.append("WHERE id_card = '").append(idCard).append("' ");
            sql.append("AND time >= '").append(checkDate).append(" 08:02:00' ");
            sql.append("AND time <= '").append(checkDate).append(" 16:00:00' ");

            if (!attendanceAreaIds.isEmpty()) {
                sql.append("AND area_id IN (");
                for (int i = 0; i < attendanceAreaIds.size(); i++) {
                    if (i > 0)
                        sql.append(",");
                    sql.append("'").append(attendanceAreaIds.get(i)).append("'");
                }
                sql.append(") ");
            }

            logger.debug("执行旷工检查SQL：{}", sql.toString());

            R<JSONObject> response = tdengineService.executeTDengineSQL(sql.toString());
            if (response.getCode() == R.SUCCESS && response.getData() != null) {
                JSONObject data = response.getData();
                JSONArray rows = data.getJSONArray("data");
                if (rows != null && !rows.isEmpty()) {
                    JSONArray row = rows.getJSONArray(0);
                    int count = row.getInt(0);
                    return count == 0; // 没有数据则为旷工
                }
            }
            return true; // 查询失败默认为旷工
        } catch (Exception e) {
            logger.error("检查旷工状态失败", e);
            return true;
        }
    }

    /**
     * 检查是否迟到（08:05之前厂内是否有该人员的数据）
     */
    private boolean checkIsLate(String idCard, String checkDate, List<String> attendanceAreaIds) {
        try {
            // 查询08:02-08:05之间是否有数据
            StringBuilder sql = new StringBuilder();
            sql.append("SELECT COUNT(*) ");
            sql.append("FROM ").append(dbname).append(".area_fence_data ");
            sql.append("WHERE id_card = '").append(idCard).append("' ");
            sql.append("AND time >= '").append(checkDate).append(" 08:02:00' ");
            sql.append("AND time <= '").append(checkDate).append(" 08:05:00' ");

            if (!attendanceAreaIds.isEmpty()) {
                sql.append("AND area_id IN (");
                for (int i = 0; i < attendanceAreaIds.size(); i++) {
                    if (i > 0)
                        sql.append(",");
                    sql.append("'").append(attendanceAreaIds.get(i)).append("'");
                }
                sql.append(") ");
            }

            logger.debug("执行迟到检查SQL：{}", sql.toString());

            R<JSONObject> response = tdengineService.executeTDengineSQL(sql.toString());
            if (response.getCode() == R.SUCCESS && response.getData() != null) {
                JSONObject data = response.getData();
                JSONArray rows = data.getJSONArray("data");
                if (rows != null && !rows.isEmpty()) {
                    JSONArray row = rows.getJSONArray(0);
                    int count = row.getInt(0);
                    return count == 0; // 08:05之前没有数据则为迟到
                }
            }
            return true; // 查询失败默认为迟到
        } catch (Exception e) {
            logger.error("检查迟到状态失败", e);
            return true;
        }
    }

    /**
     * 检查是否早退（15:55之后厂内是否有该人员的数据）
     */
    private boolean checkIsEarlyLeave(String idCard, String checkDate, List<String> attendanceAreaIds) {
        try {
            // 查询15:55-16:00之间是否有数据
            StringBuilder sql = new StringBuilder();
            sql.append("SELECT COUNT(*) ");
            sql.append("FROM ").append(dbname).append(".area_fence_data ");
            sql.append("WHERE id_card = '").append(idCard).append("' ");
            sql.append("AND time >= '").append(checkDate).append(" 15:55:00' ");
            sql.append("AND time <= '").append(checkDate).append(" 16:00:00' ");

            if (!attendanceAreaIds.isEmpty()) {
                sql.append("AND area_id IN (");
                for (int i = 0; i < attendanceAreaIds.size(); i++) {
                    if (i > 0)
                        sql.append(",");
                    sql.append("'").append(attendanceAreaIds.get(i)).append("'");
                }
                sql.append(") ");
            }

            logger.debug("执行早退检查SQL：{}", sql.toString());

            R<JSONObject> response = tdengineService.executeTDengineSQL(sql.toString());
            if (response.getCode() == R.SUCCESS && response.getData() != null) {
                JSONObject data = response.getData();
                JSONArray rows = data.getJSONArray("data");
                if (rows != null && !rows.isEmpty()) {
                    JSONArray row = rows.getJSONArray(0);
                    int count = row.getInt(0);
                    return count == 0; // 15:55之后没有数据则为早退
                }
            }
            return true; // 查询失败默认为早退
        } catch (Exception e) {
            logger.error("检查早退状态失败", e);
            return true;
        }
    }

    /**
     * 计算怠工时长（使用时序数据库直接统计，避免内存占用）
     */
    private BigDecimal calculateIdleHours(String idCard, String checkDate, List<String> workAreaIds) {
        try {
            // 工作时间：08:02-16:00，共7小时58分钟 = 7.97小时
            double totalWorkHours = 7.97;

            // 查询当天在工作区域的数据总数（用于估算在岗时间）
            StringBuilder sql = new StringBuilder();
            sql.append("SELECT COUNT(*) ");
            sql.append("FROM ").append(dbname).append(".area_fence_data ");
            sql.append("WHERE id_card = '").append(idCard).append("' ");
            sql.append("AND time >= '").append(checkDate).append(" 08:02:00' ");
            sql.append("AND time <= '").append(checkDate).append(" 16:00:00' ");

            if (!workAreaIds.isEmpty()) {
                sql.append("AND area_id IN (");
                for (int i = 0; i < workAreaIds.size(); i++) {
                    if (i > 0)
                        sql.append(",");
                    sql.append("'").append(workAreaIds.get(i)).append("'");
                }
                sql.append(") ");
            }

            logger.debug("执行怠工时长计算SQL：{}", sql.toString());

            R<JSONObject> response = tdengineService.executeTDengineSQL(sql.toString());
            if (response.getCode() == R.SUCCESS && response.getData() != null) {
                JSONObject data = response.getData();
                JSONArray rows = data.getJSONArray("data");
                if (rows != null && !rows.isEmpty()) {
                    JSONArray row = rows.getJSONArray(0);
                    int dataCount = row.getInt(0);

                    // 根据数据条数估算工作时间
                    // 假设每小时至少有10条数据，可以根据实际情况调整
                    double estimatedWorkHours = Math.min(dataCount / 10.0, totalWorkHours);

                    // 计算怠工时长：总工作时间 - 估算工作时间
                    double idleHours = Math.max(0, totalWorkHours - estimatedWorkHours);
                    return BigDecimal.valueOf(idleHours).setScale(2, BigDecimal.ROUND_HALF_UP);
                }
            }

            // 查询失败，返回全天怠工
            return BigDecimal.valueOf(totalWorkHours).setScale(2, BigDecimal.ROUND_HALF_UP);

        } catch (Exception e) {
            logger.error("计算怠工时长失败", e);
            return BigDecimal.valueOf(8.0); // 出错时默认8小时
        }
    }

    /**
     * 生成考勤状态描述
     */
    private String generateAttendanceStatus(boolean isLate, boolean isEarlyLeave, BigDecimal idleHours) {
        List<String> issues = new ArrayList<>();

        if (isLate) {
            issues.add("迟到");
        }

        if (isEarlyLeave) {
            issues.add("早退");
        }

        if (idleHours.compareTo(BigDecimal.ZERO) > 0) {
            issues.add("怠工" + idleHours + "小时");
        }

        if (issues.isEmpty()) {
            return "正常";
        } else {
            return String.join("、", issues);
        }
    }

    /**
     * 根据身份证号计算怠工时长
     * 
     * @param idCard        身份证号
     * @param date          日期 (yyyy-MM-dd格式)
     * @param workTimeRange 工作时间范围，格式如"07:00-18:00"或"18:00-03:00"
     * @return 怠工时长(小时)
     * @author Shawn
     * @date 2025/6/20
     */
    public double calculateIdleTimeByIdCard(String idCard, String date, String workTimeRange) {
        try {
            logger.info("开始计算身份证号为 {} 在 {} 工作时间范围 {} 的怠工时长", idCard, date, workTimeRange);

            // 1. 获取字典数据 - 查找所有区域配置
            List<DictData> areaList = getAreaFenceDataDictList(idCard);
            if (areaList.isEmpty()) {
                logger.warn("未找到area_fence_data字典配置");
                return 0.0;
            }

            // 2. 查询心跳数据
            List<Map<String, Object>> heartbeatData = queryAreaFenceDataByIdCardAndAreas(idCard, areaList, date,
                    workTimeRange);
            if (heartbeatData.isEmpty()) {
                logger.warn("身份证号 {} 在 {} 工作时间范围 {} 未找到心跳数据", idCard, date, workTimeRange);
                return 0.0;
            }

            // 3. 计算怠工时长
            double idleHours = calculateIdleHoursFromData(heartbeatData);
            logger.info("身份证号 {} 在 {} 工作时间范围 {} 的怠工时长为: {} 小时", idCard, date, workTimeRange, idleHours);

            return idleHours;

        } catch (Exception e) {
            logger.error("计算怠工时长失败，身份证号: {}, 日期: {}, 工作时间范围: {}", idCard, date, workTimeRange, e);
            return 0.0;
        }
    }

    /**
     * 根据身份证号计算怠工时长（兼容原方法）
     * 
     * @param idCard 身份证号
     * @param date   日期 (yyyy-MM-dd格式)
     * @return 怠工时长(小时)
     * @author Shawn
     * @date 2025/6/20
     */
    public double calculateIdleTimeByIdCard(String idCard, String date) {
        // 兼容原方法，使用默认时间范围
        return calculateIdleTimeByIdCard(idCard, date, "08:00-17:00");
    }

    /**
     * 获取休息区域的配置列表（从Redis缓存iot:area:all中获取area_type为"1"的区域）
     * 
     * @return 休息区域ID列表
     * @author Shawn
     * @date 2025/01/27
     */
    @SuppressWarnings("unchecked")
    private List<DictData> getAreaFenceDataDictList(String idCard) {
        try {
            List<DictData> dictList = new ArrayList<>();


            String corpCode = deviceCorpMappingCache.getCorpCodeByIdCard(idCard);

            logger.info("开始从Redis缓存iot:area:all获取休息区域数据...");

            // 使用StringRedisTemplate来获取原始字符串数据，避免序列化问题
            try {

                String rawData = stringRedisTemplate.opsForValue().get(corpCode + SwmRedisConstant.RedisIotKey.AREA_ALL_CACHE_KEY);
                if (rawData != null && !rawData.isEmpty()) {
                    logger.debug("从Redis获取到原始数据长度: {}", rawData.length());

                    // 解析Jackson序列化格式: ["java.util.ArrayList", [实际数据数组]]
                    JSONArray outerArray = new JSONArray(rawData);

                    if (outerArray.size() >= 2) {
                        // 第一个元素是类型标识符，第二个元素是实际数据
                        Object dataElement = outerArray.get(1);

                        if (dataElement instanceof JSONArray) {
                            JSONArray dataArray = (JSONArray) dataElement;
                            logger.debug("解析到区域数据数组，数量: {}", dataArray.size());

                            for (int i = 0; i < dataArray.size(); i++) {
                                try {
                                    Object item = dataArray.get(i);

                                    // 每个区域项也是一个数组: ["类型", {实际数据对象}]
                                    if (item instanceof JSONArray) {
                                        JSONArray itemArray = (JSONArray) item;
                                        if (itemArray.size() >= 2) {
                                            Object areaDataObj = itemArray.get(1);

                                            if (areaDataObj instanceof JSONObject) {
                                                JSONObject areaJson = (JSONObject) areaDataObj;
                                                String areaType = areaJson.getStr("areaType");

                                                if ("1".equals(areaType)) {
                                                    String areaId = areaJson.getStr("id");
                                                    String areaName = areaJson.getStr("areaName");

                                                    // 创建DictData对象，保持与原有逻辑的兼容性
                                                    DictData dictData = new DictData();
                                                    dictData.setDictValue(areaId); // 区域ID作为字典值
                                                    dictData.setDictLabel(areaName); // 区域名称作为字典标签
                                                    dictData.setDictLabelRaw(areaId); // 原始标签也使用区域ID，用于SQL查询
                                                    dictList.add(dictData);

                                                    logger.debug("找到休息区域: ID={}, 名称={}", areaId, areaName);
                                                }
                                            }
                                        }
                                    }
                                } catch (Exception e) {
                                    logger.warn("解析第{}个区域数据时出错，跳过: {}", i, e.getMessage());
                                    continue;
                                }
                            }
                        }
                    }

                    logger.info("从Redis缓存iot:area:all中获取到休息区域数量: {}", dictList.size());
                } else {
                    logger.warn("Redis缓存iot:area:all为空或不存在");
                }
            } catch (Exception e) {
                logger.warn("从Redis缓存iot:area:all解析数据失败: {}", e.getMessage());

                // 尝试使用RedisTemplate的方式
                try {
                    logger.info("尝试使用RedisTemplate方式获取数据...");
                    Object cachedData = redisTemplate.opsForValue().get(corpCode + SwmRedisConstant.RedisIotKey.AREA_ALL_CACHE_KEY);

                    if (cachedData instanceof List) {
                        List<?> areaList = (List<?>) cachedData;
                        logger.debug("获取到区域列表，数量: {}", areaList.size());

                        for (Object item : areaList) {
                            try {
                                if (item instanceof Map) {
                                    Map<String, Object> areaData = (Map<String, Object>) item;
                                    String areaType = (String) areaData.get("areaType");

                                    if ("1".equals(areaType)) {
                                        String areaId = (String) areaData.get("id");
                                        String areaName = (String) areaData.get("areaName");

                                        DictData dictData = new DictData();
                                        dictData.setDictValue(areaId);
                                        dictData.setDictLabel(areaName);
                                        dictData.setDictLabelRaw(areaId);
                                        dictList.add(dictData);

                                        logger.debug("找到休息区域: ID={}, 名称={}", areaId, areaName);
                                    }
                                }
                            } catch (Exception itemException) {
                                logger.warn("处理区域项时出错，跳过: {}", itemException.getMessage());
                                continue;
                            }
                        }
                    }
                } catch (Exception redisTemplateException) {
                    logger.warn("使用RedisTemplate方式也失败: {}", redisTemplateException.getMessage());
                }
            }

            // 如果从缓存获取失败或没有数据，回退到字典表
            if (dictList.isEmpty()) {
                logger.warn("从Redis缓存获取休息区域失败或无数据，回退到字典表");
                List<DictData> fallbackList = DictUtils.getDictList("area_fence_data");
                logger.info("从字典表获取到area_fence_data数据，区域数量: {}", fallbackList.size());
                return fallbackList;
            }

            return dictList;

        } catch (Exception e) {
            logger.error("从Redis缓存获取休息区域数据失败，回退到字典表", e);
            // 发生异常时回退到原来的字典表方式
            try {
                List<DictData> fallbackList = DictUtils.getDictList("area_fence_data");
                logger.info("回退：从字典表获取到area_fence_data数据，区域数量: {}", fallbackList.size());
                return fallbackList;
            } catch (Exception fallbackException) {
                logger.error("回退到字典表也失败", fallbackException);
                return new ArrayList<>();
            }
        }
    }

    /**
     * 根据身份证和区域ID列表查询area_fence_data表中的数据
     * 
     * @param idCard        身份证号
     * @param areaList      区域配置列表
     * @param checkDate     查询日期
     * @param workTimeRange 工作时间范围，格式如"07:00-18:00"或"18:00-03:00"
     * @return 区域围栏数据列表
     */
    private List<Map<String, Object>> queryAreaFenceDataByIdCardAndAreas(String idCard, List<DictData> areaList,
            String checkDate, String workTimeRange) {
        List<Map<String, Object>> allData = new ArrayList<>();

        try {
            // 构建area_id的IN查询条件
            StringBuilder areaIdCondition = new StringBuilder();
            areaIdCondition.append("area_id IN (");
            for (int i = 0; i < areaList.size(); i++) {
                if (i > 0)
                    areaIdCondition.append(",");
                areaIdCondition.append("'").append(areaList.get(i).getDictLabelRaw()).append("'");
            }
            areaIdCondition.append(")");

            // 解析工作时间范围并构建查询时间
            String[] times = workTimeRange.split("-");
            if (times.length != 2) {
                logger.warn("工作时间范围格式错误: {}, 使用默认时间范围", workTimeRange);
                times = new String[] { "08:00", "17:00" };
            }

            String startTime = times[0];
            String endTime = times[1];

            String queryStartDateTime;
            String queryEndDateTime;

            // 判断是否跨天班次
            if (startTime.compareTo(endTime) > 0) {
                // 跨天班次，如"18:00-03:00"
                queryStartDateTime = checkDate + " " + startTime + ":00";
                // 结束时间需要加一天
                try {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                    Date checkDateParsed = sdf.parse(checkDate);
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(checkDateParsed);
                    cal.add(Calendar.DAY_OF_MONTH, 1);
                    String nextDay = sdf.format(cal.getTime());
                    queryEndDateTime = nextDay + " " + endTime + ":00";
                } catch (Exception e) {
                    logger.error("解析跨天班次日期失败", e);
                    queryEndDateTime = checkDate + " 23:59:59";
                }
                logger.info("跨天班次查询时间范围: {} 到 {}", queryStartDateTime, queryEndDateTime);
            } else {
                // 普通班次，如"07:00-18:00"
                queryStartDateTime = checkDate + " " + startTime + ":00";
                queryEndDateTime = checkDate + " " + endTime + ":00";
                logger.info("普通班次查询时间范围: {} 到 {}", queryStartDateTime, queryEndDateTime);
            }

            // 查询指定工作时间范围内该身份证的所有区域围栏数据，按时间排序
            String sql = String.format(
                    "SELECT time, x, y, area_name, area_id, device_id, id_card " +
                            "FROM %s.area_fence_data " +
                            "WHERE id_card = '%s' " +
                            "AND time >= '%s' " +
                            "AND time <= '%s' " +
                            "AND %s " +
                            "ORDER BY time ASC",
                    dbname, idCard, queryStartDateTime, queryEndDateTime, areaIdCondition.toString());

            logger.debug("查询区域围栏数据SQL: {}", sql);

            R<JSONObject> response = tdengineService.executeTDengineSQL(sql);
            if (response.getCode() == R.SUCCESS && response.getData() != null) {
                JSONObject data = response.getData();
                JSONArray rows = data.getJSONArray("data");

                if (rows != null) {
                    for (int i = 0; i < rows.size(); i++) {
                        JSONArray row = rows.getJSONArray(i);
                        AreaFenceData fenceData = parseAreaFenceDataFromRow(row);
                        if (fenceData != null) {
                            Map<String, Object> dataMap = new HashMap<>();
                            dataMap.put("time", fenceData.getTime());
                            dataMap.put("x", fenceData.getX());
                            dataMap.put("y", fenceData.getY());
                            dataMap.put("area_name", fenceData.getAreaName());
                            dataMap.put("area_id", fenceData.getAreaId());
                            dataMap.put("device_id", fenceData.getDeviceId());
                            dataMap.put("id_card", fenceData.getIdCard());
                            allData.add(dataMap);
                        }
                    }
                }

                logger.info("查询到身份证 {} 在 {} 工作时间范围 {} 的区域围栏数据条数: {}", idCard, checkDate, workTimeRange, allData.size());
            } else {
                logger.warn("查询区域围栏数据失败: {}", response.getMsg());
            }

        } catch (Exception e) {
            logger.error("查询区域围栏数据异常", e);
        }

        return allData;
    }

    /**
     * 根据身份证和区域ID列表查询area_fence_data表中的数据（兼容原方法）
     * 
     * @param idCard    身份证号
     * @param areaList  区域配置列表
     * @param checkDate 查询日期
     * @return 区域围栏数据列表
     */
    private List<Map<String, Object>> queryAreaFenceDataByIdCardAndAreas(String idCard, List<DictData> areaList,
            String checkDate) {
        // 兼容原方法，使用默认时间范围
        return queryAreaFenceDataByIdCardAndAreas(idCard, areaList, checkDate, "08:00-17:00");
    }

    /**
     * 从怠工区域心跳数据计算怠工时长
     * 算法说明：
     * 1. 查询到的数据都是员工在怠工区域（如休息区）的心跳数据
     * 2. 将数据按时间排序
     * 3. 找出连续的心跳数据段（相邻两个数据点时间间隔小于等于30分钟认为是连续的）
     * 4. 计算每个连续段的时长（从段开始时间到段结束时间）
     * 5. 怠工时长 = 所有连续怠工段的总时长
     *
     * @param dataList 怠工区域心跳数据列表，必须按时间排序
     * @return 怠工时长（小时）
     */
    private double calculateIdleHoursFromData(List<Map<String, Object>> dataList) {
        if (dataList == null || dataList.isEmpty()) {
            logger.info("怠工区域心跳数据为空，返回0小时");
            return 0.0;
        }

        // 确保数据按时间排序
        dataList.sort(Comparator.comparing(data -> (Date) data.get("time")));

        // 心跳间隔阈值：30分钟，如果超过这个时间则认为不连续
        final long HEARTBEAT_THRESHOLD_MS = 30 * 60 * 1000;

        long totalIdleMinutes = 0;
        Date lastTime = null;
        Date segmentStartTime = null;
        int segmentCount = 0;

        logger.info("开始分析怠工区域心跳数据，总数据条数: {}", dataList.size());

        // 打印前几条和后几条数据用于调试
        if (dataList.size() > 0) {
            Date firstTime = (Date) dataList.get(0).get("time");
            Date lastDataTime = (Date) dataList.get(dataList.size() - 1).get("time");
            logger.info("怠工数据时间范围: {} 到 {}",
                    DATETIME_FORMAT.format(firstTime),
                    DATETIME_FORMAT.format(lastDataTime));
        }

        for (int i = 0; i < dataList.size(); i++) {
            Map<String, Object> current = dataList.get(i);
            Date currentTime = (Date) current.get("time");

            if (currentTime == null) {
                logger.warn("发现空时间数据，跳过");
                continue;
            }

            if (lastTime == null) {
                // 第一条数据，开始新的连续怠工段
                segmentStartTime = currentTime;
                segmentCount++;
                logger.info("开始第{}个连续怠工段: {}", segmentCount, DATETIME_FORMAT.format(currentTime));
            } else {
                long timeDiff = currentTime.getTime() - lastTime.getTime();

                if (timeDiff <= HEARTBEAT_THRESHOLD_MS) {
                    // 连续的心跳数据，继续当前怠工段
                    logger.debug("连续怠工心跳: {} -> {}, 间隔: {}分钟",
                            DATETIME_FORMAT.format(lastTime),
                            DATETIME_FORMAT.format(currentTime),
                            timeDiff / (60 * 1000.0));
                } else {
                    // 超过阈值，结束当前连续怠工段，计算时长
                    if (segmentStartTime != null) {
                        long segmentDurationMs = lastTime.getTime() - segmentStartTime.getTime();
                        double segmentMinutes = segmentDurationMs / (60.0 * 1000);
                        totalIdleMinutes += Math.round(segmentMinutes);

                        logger.info("第{}个连续怠工段结束: {} -> {}, 持续时长: {:.2f} 分钟",
                                segmentCount,
                                DATETIME_FORMAT.format(segmentStartTime),
                                DATETIME_FORMAT.format(lastTime),
                                segmentMinutes);
                    }

                    // 开始新的连续怠工段
                    segmentStartTime = currentTime;
                    segmentCount++;
                    logger.info("开始第{}个连续怠工段: {} (间隔过大: {:.1f}分钟)",
                            segmentCount,
                            DATETIME_FORMAT.format(currentTime),
                            timeDiff / (60.0 * 1000));
                }
            }

            lastTime = currentTime;
        }

        // 处理最后一个连续怠工段
        if (segmentStartTime != null && lastTime != null) {
            long segmentDurationMs = lastTime.getTime() - segmentStartTime.getTime();
            double segmentMinutes = segmentDurationMs / (60.0 * 1000);
            totalIdleMinutes += Math.round(segmentMinutes);

            logger.info("第{}个连续怠工段结束(最后): {} -> {}, 持续时长: {:.2f} 分钟",
                    segmentCount,
                    DATETIME_FORMAT.format(segmentStartTime),
                    DATETIME_FORMAT.format(lastTime),
                    segmentMinutes);
        }

        // 修改精度计算：使用更精确的小数计算，避免精度丢失
        double idleHours = totalIdleMinutes / 60.0;
        // 保留2位小数，提高精度
        idleHours = Math.round(idleHours * 100.0) / 100.0;

        logger.info("=== 怠工时长计算完成 ===");
        logger.info("总连续怠工段数: {}", segmentCount);
        logger.info("总怠工时长: {} 分钟 ({} 小时)", totalIdleMinutes, idleHours);
        logger.info("============================");

        return idleHours;
    }

    /**
     * 检查员工最近指定时间范围内是否在休息区域
     * 
     * @param idCard    身份证号
     * @param startTime 开始时间 (yyyy-MM-dd HH:mm:ss)
     * @param endTime   结束时间 (yyyy-MM-dd HH:mm:ss)
     * @return true-在休息区域有数据，false-无数据
     * @author Shawn
     * @date 2025/6/20
     */
    public boolean checkInRestAreaRecently(String idCard, String startTime, String endTime) {
        try {
            logger.debug("检查身份证号 {} 在时间范围 {} 到 {} 是否在休息区域", idCard, startTime, endTime);

            // 1. 获取字典数据 - 查找所有休息区域配置
            List<DictData> areaList = getAreaFenceDataDictList(idCard);
            if (areaList.isEmpty()) {
                logger.warn("未找到area_fence_data字典配置");
                return false;
            }

            // 2. 构建area_id的IN查询条件
            StringBuilder areaIdCondition = new StringBuilder();
            areaIdCondition.append("area_id IN (");
            for (int i = 0; i < areaList.size(); i++) {
                if (i > 0)
                    areaIdCondition.append(",");
                areaIdCondition.append("'").append(areaList.get(i).getDictLabelRaw()).append("'");
            }
            areaIdCondition.append(")");

            // 3. 查询指定时间范围内该身份证在休息区域的数据
            String sql = String.format(
                    "SELECT COUNT(*) " +
                            "FROM %s.area_fence_data " +
                            "WHERE id_card = '%s' " +
                            "AND time >= '%s' " +
                            "AND time <= '%s' " +
                            "AND %s " +
                            "LIMIT 1",
                    dbname, idCard, startTime, endTime, areaIdCondition.toString());

            logger.debug("查询休息区域数据SQL: {}", sql);

            R<JSONObject> response = tdengineService.executeTDengineSQL(sql);
            if (response.getCode() == R.SUCCESS && response.getData() != null) {
                JSONObject data = response.getData();
                JSONArray rows = data.getJSONArray("data");

                if (rows != null && !rows.isEmpty()) {
                    JSONArray row = rows.getJSONArray(0);
                    if (row != null && row.size() > 0) {
                        int count = row.getInt(0);
                        boolean inRestArea = count > 0;
                        logger.debug("身份证号 {} 在时间范围 {} 到 {} 休息区域数据条数: {}, 在休息区: {}",
                                idCard, startTime, endTime, count, inRestArea);
                        return inRestArea;
                    }
                }
            } else {
                logger.warn("查询休息区域数据失败: {}", response.getMsg());
            }

            return false;

        } catch (Exception e) {
            logger.error("检查休息区域状态失败，身份证号: {}, 时间范围: {} 到 {}", idCard, startTime, endTime, e);
            return false;
        }
    }

    /**
     * 获取工作区域的配置列表（从Redis缓存iot:area:all中获取area_type为"0"的区域）
     * 
     * @return 工作区域ID列表
     * @author Shawn
     * @date 2025/01/27
     */
    @SuppressWarnings("unchecked")
    private List<DictData> getWorkAreaFenceDataDictList(String idCard) {
        try {
            List<DictData> dictList = new ArrayList<>();

            logger.info("开始从Redis缓存iot:area:all获取工作区域数据...");

            String corpCode = deviceCorpMappingCache.getCorpCodeByIdCard(idCard);

            // 使用StringRedisTemplate来获取原始字符串数据，避免序列化问题
            try {
                String rawData = stringRedisTemplate.opsForValue().get(corpCode + SwmRedisConstant.RedisIotKey.AREA_ALL_CACHE_KEY);
                if (rawData != null && !rawData.isEmpty()) {
                    logger.debug("从Redis获取到原始数据长度: {}", rawData.length());

                    // 解析Jackson序列化格式: ["java.util.ArrayList", [实际数据数组]]
                    JSONArray outerArray = new JSONArray(rawData);

                    if (outerArray.size() >= 2) {
                        // 第一个元素是类型标识符，第二个元素是实际数据
                        Object dataElement = outerArray.get(1);

                        if (dataElement instanceof JSONArray) {
                            JSONArray dataArray = (JSONArray) dataElement;
                            logger.debug("解析到区域数据数组，数量: {}", dataArray.size());

                            for (int i = 0; i < dataArray.size(); i++) {
                                try {
                                    Object item = dataArray.get(i);

                                    // 每个区域项也是一个数组: ["类型", {实际数据对象}]
                                    if (item instanceof JSONArray) {
                                        JSONArray itemArray = (JSONArray) item;
                                        if (itemArray.size() >= 2) {
                                            Object areaDataObj = itemArray.get(1);

                                            if (areaDataObj instanceof JSONObject) {
                                                JSONObject areaJson = (JSONObject) areaDataObj;
                                                String areaType = areaJson.getStr("areaType");

                                                if ("0".equals(areaType)) {
                                                    String areaId = areaJson.getStr("id");
                                                    String areaName = areaJson.getStr("areaName");

                                                    // 创建DictData对象，保持与原有逻辑的兼容性
                                                    DictData dictData = new DictData();
                                                    dictData.setDictValue(areaId); // 区域ID作为字典值
                                                    dictData.setDictLabel(areaName); // 区域名称作为字典标签
                                                    dictData.setDictLabelRaw(areaId); // 原始标签也使用区域ID，用于SQL查询
                                                    dictList.add(dictData);

                                                    logger.debug("找到工作区域: ID={}, 名称={}", areaId, areaName);
                                                }
                                            }
                                        }
                                    }
                                } catch (Exception e) {
                                    logger.warn("解析第{}个区域数据时出错，跳过: {}", i, e.getMessage());
                                    continue;
                                }
                            }
                        }
                    }

                    logger.info("从Redis缓存iot:area:all中获取到工作区域数量: {}", dictList.size());
                } else {
                    logger.warn("Redis缓存iot:area:all为空或不存在");
                }
            } catch (Exception e) {
                logger.warn("从Redis缓存iot:area:all解析数据失败: {}", e.getMessage());

                // 尝试使用RedisTemplate的方式
                try {
                    logger.info("尝试使用RedisTemplate方式获取数据...");
                    Object cachedData = redisTemplate.opsForValue().get(corpCode + SwmRedisConstant.RedisIotKey.AREA_ALL_CACHE_KEY);

                    if (cachedData instanceof List) {
                        List<?> areaList = (List<?>) cachedData;
                        logger.debug("获取到区域列表，数量: {}", areaList.size());

                        for (Object item : areaList) {
                            try {
                                if (item instanceof Map) {
                                    Map<String, Object> areaData = (Map<String, Object>) item;
                                    String areaType = (String) areaData.get("areaType");

                                    if ("0".equals(areaType)) {
                                        String areaId = (String) areaData.get("id");
                                        String areaName = (String) areaData.get("areaName");

                                        DictData dictData = new DictData();
                                        dictData.setDictValue(areaId);
                                        dictData.setDictLabel(areaName);
                                        dictData.setDictLabelRaw(areaId);
                                        dictList.add(dictData);

                                        logger.debug("找到工作区域: ID={}, 名称={}", areaId, areaName);
                                    }
                                }
                            } catch (Exception itemException) {
                                logger.warn("处理区域项时出错，跳过: {}", itemException.getMessage());
                                continue;
                            }
                        }
                    }
                } catch (Exception redisTemplateException) {
                    logger.warn("使用RedisTemplate方式也失败: {}", redisTemplateException.getMessage());
                }
            }

            return dictList;

        } catch (Exception e) {
            logger.error("从Redis缓存获取工作区域数据失败", e);
            return new ArrayList<>();
        }
    }

    /**
     * 根据身份证号计算实际工作时长
     * 新逻辑：统计所有区域的活动时长，不区分工作区域
     * 时间范围：
     * - 白天(7:00-23:59)：统计当天 06:00:00 到 次日 06:05:00
     * - 凌晨(0:00-6:59)：统计前天 06:00:00 到 次日 06:05:00
     *
     * @param idCard        身份证号
     * @param date          日期 (yyyy-MM-dd格式) - 此参数保留向后兼容，但内部使用新的时间计算逻辑
     * @param workTimeRange 工作时间范围 - 此参数保留向后兼容，但内部使用新的时间计算逻辑
     * @return 实际工作时长(小时)
     * @author Shawn
     * @date 2025/01/27
     */
    public double calculateEffectiveWorkHoursByIdCard(String idCard, String date, String workTimeRange) {
        try {
            logger.info("开始计算身份证号为 {} 的实际工作时长（新逻辑：全天活动时长）", idCard);

            // 新逻辑：根据当前时间确定查询范围，忽略传入的date和workTimeRange参数
            String[] timeRange = calculateNewTimeRange();
            String queryStartDateTime = timeRange[0];
            String queryEndDateTime = timeRange[1];

            logger.info("查询时间范围: {} 到 {}", queryStartDateTime, queryEndDateTime);

            // 查询所有区域的心跳数据（不过滤工作区域）
            List<Map<String, Object>> allHeartbeatData = queryAllAreaFenceData(idCard, queryStartDateTime,
                    queryEndDateTime);

            if (allHeartbeatData.isEmpty()) {
                logger.warn("身份证号 {} 在时间范围 {} 到 {} 未找到任何心跳数据", idCard, queryStartDateTime, queryEndDateTime);
                return 0.0;
            }

            // 使用现有的连续性算法计算时长
            double effectiveWorkHours = calculateEffectiveWorkHoursFromData(allHeartbeatData);
            logger.info("身份证号 {} 的总活动时长为: {} 小时", idCard, effectiveWorkHours);

            return effectiveWorkHours;

        } catch (Exception e) {
            logger.error("计算实际工作时长失败，身份证号: {}", idCard, e);
            return 0.0;
        }
    }

    /**
     * 根据身份证号计算实际工作时长（兼容原方法）
     *
     * @param idCard 身份证号
     * @param date   日期 (yyyy-MM-dd格式)
     * @return 实际工作时长(小时)
     * @author Shawn
     * @date 2025/01/27
     */
    public double calculateEffectiveWorkHoursByIdCard(String idCard, String date) {
        // 兼容原方法，使用默认时间范围
        return calculateEffectiveWorkHoursByIdCard(idCard, date, "08:00-17:00");
    }

    /**
     * 计算新的查询时间范围
     * 规则：
     * - 白天(7:00-23:59)：统计当天 06:00:00 到 次日 06:05:00
     * - 凌晨(0:00-6:59)：统计前天 06:00:00 到 次日 06:05:00
     *
     * 注意：往后延5分钟是为了避免定时任务整点启动时遗漏边界数据
     *
     * @return 时间范围数组 [开始时间, 结束时间]
     * @author Shawn
     * @date 2025/01/28
     */
    private String[] calculateNewTimeRange() {
        Calendar now = Calendar.getInstance();
        int currentHour = now.get(Calendar.HOUR_OF_DAY);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        if (currentHour >= 0 && currentHour <= 6) {
            // 凌晨0-6点：统计前天6点到次日6点05分
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DAY_OF_MONTH, -2); // 前天
            String dayBeforeYesterday = dateFormat.format(cal.getTime());

            cal.setTime(new Date());
            cal.add(Calendar.DAY_OF_MONTH, 1); // 次日
            String tomorrow = dateFormat.format(cal.getTime());

            String startTime = dayBeforeYesterday + " 06:00:00";
            String endTime = tomorrow + " 06:05:00";

            logger.info("凌晨{}点执行，查询前天6点到次日6点05分: {} 到 {}", currentHour, startTime, endTime);
            return new String[] { startTime, endTime };
        } else {
            // 白天7-23点：统计当天6点到次日6点05分
            String today = dateFormat.format(new Date());

            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DAY_OF_MONTH, 1); // 次日
            String tomorrow = dateFormat.format(cal.getTime());

            String startTime = today + " 06:00:00";
            String endTime = tomorrow + " 06:05:00";

            logger.info("白天{}点执行，查询当天6点到次日6点05分: {} 到 {}", currentHour, startTime, endTime);
            return new String[] { startTime, endTime };
        }
    }

    /**
     * 查询所有区域的心跳数据（不过滤工作区域）
     *
     * @param idCard             身份证号
     * @param queryStartDateTime 查询开始时间
     * @param queryEndDateTime   查询结束时间
     * @return 心跳数据列表
     * @author Shawn
     * @date 2025/01/28
     */
    private List<Map<String, Object>> queryAllAreaFenceData(String idCard, String queryStartDateTime,
            String queryEndDateTime) {
        List<Map<String, Object>> allData = new ArrayList<>();

        try {
            // 直接查询所有数据，不过滤区域
            String sql = String.format(
                    "SELECT time, x, y, area_name, area_id, device_id, id_card " +
                            "FROM %s.area_fence_data " +
                            "WHERE id_card = '%s' " +
                            "AND time >= '%s' " +
                            "AND time <= '%s' " +
                            "ORDER BY time ASC",
                    dbname, idCard, queryStartDateTime, queryEndDateTime);

            logger.debug("查询所有区域心跳数据SQL: {}", sql);

            R<JSONObject> response = tdengineService.executeTDengineSQL(sql);
            if (response.getCode() == R.SUCCESS && response.getData() != null) {
                JSONObject data = response.getData();
                JSONArray rows = data.getJSONArray("data");

                if (rows != null && !rows.isEmpty()) {
                    for (int i = 0; i < rows.size(); i++) {
                        JSONArray row = rows.getJSONArray(i);
                        Map<String, Object> record = new HashMap<>();

                        // 解析时间字段，处理TDengine的时间格式
                        Object timeObj = row.get(0);
                        Date parsedTime = parseTimeFromTDengine(timeObj);
                        record.put("time", parsedTime);

                        record.put("x", row.get(1));
                        record.put("y", row.get(2));
                        record.put("area_name", row.get(3));
                        record.put("area_id", row.get(4));
                        record.put("device_id", row.get(5));
                        record.put("id_card", row.get(6));
                        allData.add(record);
                    }
                    logger.info("查询到心跳数据 {} 条", allData.size());
                } else {
                    logger.info("查询结果为空");
                }
            } else {
                logger.warn("查询失败: {}", response.getMsg());
            }
        } catch (Exception e) {
            logger.error("查询所有区域心跳数据异常", e);
        }

        return allData;
    }

    /**
     * 解析TDengine返回的时间对象
     *
     * @param timeObj TDengine返回的时间对象
     * @return 解析后的Date对象
     */
    private Date parseTimeFromTDengine(Object timeObj) {
        if (timeObj == null) {
            return null;
        }

        try {
            if (timeObj instanceof Date) {
                return (Date) timeObj;
            } else if (timeObj instanceof Long) {
                return new Date((Long) timeObj);
            } else if (timeObj instanceof String) {
                String timeStr = (String) timeObj;

                // 尝试解析ISO 8601格式：2025-01-28T16:00:14.128Z
                if (timeStr.contains("T") && timeStr.endsWith("Z")) {
                    SimpleDateFormat utcFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
                    utcFormat.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));

                    // 如果没有毫秒，添加毫秒部分
                    String isoTimeStr = timeStr;
                    if (!timeStr.contains(".")) {
                        isoTimeStr = timeStr.replace("Z", ".000Z");
                    }

                    try {
                        return utcFormat.parse(isoTimeStr);
                    } catch (Exception e) {
                        // 尝试不带毫秒的格式
                        SimpleDateFormat utcFormatNoMillis = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
                        utcFormatNoMillis.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));
                        String noMillisStr = timeStr.contains(".")
                                ? timeStr.substring(0, timeStr.lastIndexOf(".")) + "Z"
                                : timeStr;
                        return utcFormatNoMillis.parse(noMillisStr);
                    }
                }

                // 尝试解析普通格式：yyyy-MM-dd HH:mm:ss
                if (timeStr.matches("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}")) {
                    return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(timeStr);
                }
            }
        } catch (Exception e) {
            logger.error("解析时间对象失败: {}", timeObj, e);
        }

        return null;
    }

    /**
     * 从工作区域心跳数据计算实际工作时长
     * 算法说明：
     * 1. 查询到的数据都是员工的心跳数据（新逻辑：所有区域）
     * 2. 将数据按时间排序
     * 3. 找出连续的心跳数据段（相邻两个数据点时间间隔小于等于30分钟认为是连续的）
     * 4. 计算每个连续段的时长（从段开始时间到段结束时间）
     * 5. 实际工作时长 = 所有连续工作段的总时长
     *
     * @param dataList 心跳数据列表，必须按时间排序
     * @return 实际工作时长（小时）
     */
    private double calculateEffectiveWorkHoursFromData(List<Map<String, Object>> dataList) {
        if (dataList == null || dataList.isEmpty()) {
            logger.info("心跳数据为空，返回0小时");
            return 0.0;
        }

        // 确保数据按时间排序
        dataList.sort(Comparator.comparing(data -> (Date) data.get("time")));

        // 心跳间隔阈值：30分钟，如果超过这个时间则认为不连续
        final long HEARTBEAT_THRESHOLD_MS = 30 * 60 * 1000;

        long totalWorkMinutes = 0;
        Date lastTime = null;
        Date segmentStartTime = null;
        int segmentCount = 0;

        logger.info("开始分析心跳数据，总数据条数: {}", dataList.size());

        // 打印前几条和后几条数据用于调试
        if (dataList.size() > 0) {
            Date firstTime = (Date) dataList.get(0).get("time");
            Date lastDataTime = (Date) dataList.get(dataList.size() - 1).get("time");
            logger.info("数据时间范围: {} 到 {}",
                    DATETIME_FORMAT.format(firstTime),
                    DATETIME_FORMAT.format(lastDataTime));
        }

        for (int i = 0; i < dataList.size(); i++) {
            Map<String, Object> current = dataList.get(i);
            Date currentTime = (Date) current.get("time");

            if (currentTime == null) {
                logger.warn("发现空时间数据，跳过");
                continue;
            }

            if (lastTime == null) {
                // 第一条数据，开始新的连续工作段
                segmentStartTime = currentTime;
                segmentCount++;
                logger.info("开始第{}个连续工作段: {}", segmentCount, DATETIME_FORMAT.format(currentTime));
            } else {
                long timeDiff = currentTime.getTime() - lastTime.getTime();

                if (timeDiff <= HEARTBEAT_THRESHOLD_MS) {
                    // 连续的心跳数据，继续当前工作段
                    logger.debug("连续心跳: {} -> {}, 间隔: {}分钟",
                            DATETIME_FORMAT.format(lastTime),
                            DATETIME_FORMAT.format(currentTime),
                            timeDiff / (60 * 1000.0));
                } else {
                    // 超过阈值，结束当前连续工作段，计算时长
                    if (segmentStartTime != null) {
                        long segmentDurationMs = lastTime.getTime() - segmentStartTime.getTime();
                        double segmentMinutes = segmentDurationMs / (60.0 * 1000);
                        totalWorkMinutes += Math.round(segmentMinutes);

                        logger.info("第{}个连续工作段结束: {} -> {}, 持续时长: {:.2f} 分钟",
                                segmentCount,
                                DATETIME_FORMAT.format(segmentStartTime),
                                DATETIME_FORMAT.format(lastTime),
                                segmentMinutes);
                    }

                    // 开始新的连续工作段
                    segmentStartTime = currentTime;
                    segmentCount++;
                    logger.info("开始第{}个连续工作段: {} (间隔过大: {:.1f}分钟)",
                            segmentCount,
                            DATETIME_FORMAT.format(currentTime),
                            timeDiff / (60.0 * 1000));
                }
            }

            lastTime = currentTime;
        }

        // 处理最后一个连续工作段
        if (segmentStartTime != null && lastTime != null) {
            long segmentDurationMs = lastTime.getTime() - segmentStartTime.getTime();
            double segmentMinutes = segmentDurationMs / (60.0 * 1000);
            totalWorkMinutes += Math.round(segmentMinutes);

            logger.info("第{}个连续工作段结束(最后): {} -> {}, 持续时长: {:.2f} 分钟",
                    segmentCount,
                    DATETIME_FORMAT.format(segmentStartTime),
                    DATETIME_FORMAT.format(lastTime),
                    segmentMinutes);
        }

        // 修改精度计算：使用更精确的小数计算，避免精度丢失
        double workHours = totalWorkMinutes / 60.0;
        // 保留2位小数，提高精度
        workHours = Math.round(workHours * 100.0) / 100.0;

        logger.info("=== 实际工作时长计算完成 ===");
        logger.info("总连续工作段数: {}", segmentCount);
        logger.info("总实际工作时长: {} 分钟 ({} 小时)", totalWorkMinutes, workHours);
        logger.info("==============================");

        return workHours;
    }

    /**
     * 获取指定身份证号的实时位置
     *
     * @param idCard 身份证号
     * @return "0"-工作区, "1"-休息区, "3"-未知
     * @author Shawn
     * @date 2025/06/25
     */
    public String getCurrentLocationByIdCard(String idCard) {
        if (StringUtils.isBlank(idCard)) {
            return "3";
        }

        try {
            logger.debug("获取身份证 {} 的实时位置", idCard);

            // 1. 获取工作区和休息区列表
            List<DictData> workAreaList = getWorkAreaFenceDataDictList(idCard);
            List<DictData> restAreaList = getAreaFenceDataDictList(idCard);

            List<String> workAreaIds = workAreaList.stream().map(DictData::getDictLabelRaw)
                    .collect(Collectors.toList());
            List<String> restAreaIds = restAreaList.stream().map(DictData::getDictLabelRaw)
                    .collect(Collectors.toList());

            List<String> allAreaIds = new ArrayList<>();
            allAreaIds.addAll(workAreaIds);
            allAreaIds.addAll(restAreaIds);

            if (allAreaIds.isEmpty()) {
                logger.warn("工作区和休息区均未在字典中配置");
                return "3";
            }

            // 2. 查询最近10分钟内最新的位置记录
            Date now = new Date();
            Date tenMinutesAgo = new Date(now.getTime() - 10 * 60 * 1000);
            SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String startTime = dateTimeFormat.format(tenMinutesAgo);
            String endTime = dateTimeFormat.format(now);

            StringBuilder areaIdCondition = new StringBuilder();
            areaIdCondition.append("area_id IN (");
            for (int i = 0; i < allAreaIds.size(); i++) {
                if (i > 0)
                    areaIdCondition.append(",");
                areaIdCondition.append("'").append(allAreaIds.get(i)).append("'");
            }
            areaIdCondition.append(")");

            String sql = String.format(
                    "SELECT area_id FROM %s.area_fence_data " +
                            "WHERE id_card = '%s' AND time >= '%s' AND time <= '%s' AND %s " +
                            "ORDER BY time DESC LIMIT 1",
                    dbname, idCard, startTime, endTime, areaIdCondition.toString());

            logger.debug("获取实时位置SQL: {}", sql);

            R<JSONObject> response = tdengineService.executeTDengineSQL(sql);
            if (response.getCode() == R.SUCCESS && response.getData() != null) {
                JSONArray rows = response.getData().getJSONArray("data");
                if (rows != null && !rows.isEmpty()) {
                    JSONArray row = rows.getJSONArray(0);
                    if (row != null && !row.isEmpty()) {
                        String lastAreaId = row.getStr(0);
                        if (workAreaIds.contains(lastAreaId)) {
                            logger.debug("身份证 {} 在工作区 (Area ID: {})", idCard, lastAreaId);
                            return "0";
                        }
                        if (restAreaIds.contains(lastAreaId)) {
                            logger.debug("身份证 {} 在休息区 (Area ID: {})", idCard, lastAreaId);
                            return "1";
                        }
                    }
                }
            } else {
                logger.warn("查询TDengine实时位置失败: {}", response.getMsg());
            }

            // TDengine中查不到数据，返回未知
            logger.debug("身份证 {} 在TDengine中未查询到最近10分钟内的位置信息", idCard);
            return "3";

        } catch (Exception e) {
            logger.error("获取实时位置异常, 身份证号: {}", idCard, e);
            return "3";
        }
    }
}