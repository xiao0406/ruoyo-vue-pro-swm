/**
 * 区域围栏数据服务类
 * @author Shawn
 * @date 2025-01-14
 */
package com.jeesite.modules.swm.service;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import com.jeesite.common.lang.StringUtils;
import com.jeesite.modules.swm.entity.AreaFenceData;
import com.jeesite.modules.swm.entity.AttendanceCheckResult;
import com.jeesite.modules.swm.entity.SwmPerson;
import com.jeesite.modules.sys.entity.DictData;
import com.jeesite.modules.sys.utils.DictUtils;
import com.jeesite.modules.utils.R;
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
     * @param idCard 身份证号
     * @param date   日期 (yyyy-MM-dd格式)
     * @return 怠工时长(小时)
     * @author Shawn
     * @date 2025-01-27
     */
    public double calculateIdleTimeByIdCard(String idCard, String date) {
        try {
            logger.info("开始计算身份证号为 {} 在 {} 的怠工时长", idCard, date);

            // 1. 获取字典数据 - 查找所有区域配置
            List<DictData> areaList = getAreaFenceDataDictList();
            if (areaList.isEmpty()) {
                logger.warn("未找到area_fence_data字典配置");
                return 0.0;
            }

            // 2. 查询心跳数据
            List<Map<String, Object>> heartbeatData = queryAreaFenceDataByIdCardAndAreas(idCard, areaList, date);
            if (heartbeatData.isEmpty()) {
                logger.warn("身份证号 {} 在 {} 未找到心跳数据", idCard, date);
                return 0.0;
            }

            // 3. 计算怠工时长
            double idleHours = calculateIdleHoursFromData(heartbeatData);
            logger.info("身份证号 {} 在 {} 的怠工时长为: {} 小时", idCard, date, idleHours);

            return idleHours;

        } catch (Exception e) {
            logger.error("计算怠工时长失败，身份证号: {}, 日期: {}", idCard, date, e);
            return 0.0;
        }
    }

    /**
     * 获取字典类型area_fence_data的所有选项
     * 
     * @return 区域ID列表
     */
    private List<DictData> getAreaFenceDataDictList() {
        try {
            List<DictData> dictList = DictUtils.getDictList("area_fence_data");
            logger.debug("获取到area_fence_data字典数据，区域数量: {}", dictList.size());
            return dictList;
        } catch (Exception e) {
            logger.error("获取area_fence_data字典数据失败", e);
            return new ArrayList<>();
        }
    }

    /**
     * 根据身份证和区域ID列表查询area_fence_data表中的数据
     * 
     * @param idCard    身份证号
     * @param checkDate 查询日期
     * @param areaIds   区域ID列表
     * @return 区域围栏数据列表
     */
    private List<Map<String, Object>> queryAreaFenceDataByIdCardAndAreas(String idCard, List<DictData> areaList,
            String checkDate) {
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

            // 查询当天该身份证的所有区域围栏数据，按时间排序
            String sql = String.format(
                    "SELECT time, x, y, area_name, area_id, device_id, id_card " +
                            "FROM %s.area_fence_data " +
                            "WHERE id_card = '%s' " +
                            "AND time >= '%s 00:00:00' " +
                            "AND time <= '%s 23:59:59' " +
                            "AND %s " +
                            "ORDER BY time ASC",
                    dbname, idCard, checkDate, checkDate, areaIdCondition.toString());

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

                logger.info("查询到身份证 {} 在 {} 的区域围栏数据条数: {}", idCard, checkDate, allData.size());
            } else {
                logger.warn("查询区域围栏数据失败: {}", response.getMsg());
            }

        } catch (Exception e) {
            logger.error("查询区域围栏数据异常", e);
        }

        return allData;
    }

    /**
     * 从怠工区域心跳数据计算怠工时长
     * 算法说明：
     * 1. 查询到的数据都是员工在怠工区域（如休息区）的心跳数据
     * 2. 将数据按时间排序
     * 3. 找出连续的心跳数据段（相邻两个数据点时间间隔小于等于10分钟认为是连续的）
     * 4. 计算每个连续段的时长（从段开始时间到段结束时间）
     * 5. 怠工时长 = 所有连续怠工段的总时长
     * 
     * @param dataList 怠工区域心跳数据列表，必须按时间排序
     * @return 怠工时长（小时）
     */
    private double calculateIdleHoursFromData(List<Map<String, Object>> dataList) {
        if (dataList == null || dataList.isEmpty()) {
            return 0.0;
        }

        // 确保数据按时间排序
        dataList.sort(Comparator.comparing(data -> (Date) data.get("time")));

        // 心跳间隔阈值：10分钟，如果超过这个时间则认为不连续
        final long HEARTBEAT_THRESHOLD_MS = 10 * 60 * 1000;

        long totalIdleMinutes = 0;
        Date lastTime = null;
        Date segmentStartTime = null;

        logger.debug("开始分析怠工区域心跳数据，总数据条数: {}", dataList.size());

        for (int i = 0; i < dataList.size(); i++) {
            Map<String, Object> current = dataList.get(i);
            Date currentTime = (Date) current.get("time");

            if (currentTime == null) {
                continue;
            }

            if (lastTime == null) {
                // 第一条数据，开始新的连续怠工段
                segmentStartTime = currentTime;
                logger.debug("开始新的连续怠工段: {}", DATETIME_FORMAT.format(currentTime));
            } else {
                long timeDiff = currentTime.getTime() - lastTime.getTime();

                if (timeDiff <= HEARTBEAT_THRESHOLD_MS) {
                    // 连续的心跳数据，继续当前怠工段
                    logger.debug("连续怠工心跳: {} -> {}, 间隔: {}ms",
                            DATETIME_FORMAT.format(lastTime), DATETIME_FORMAT.format(currentTime), timeDiff);
                } else {
                    // 超过阈值，结束当前连续怠工段，计算时长
                    if (segmentStartTime != null) {
                        long segmentDurationMs = lastTime.getTime() - segmentStartTime.getTime();
                        long segmentMinutes = segmentDurationMs / (60 * 1000);
                        totalIdleMinutes += segmentMinutes;

                        logger.debug("连续怠工段结束: {} -> {}, 持续时长: {} 分钟",
                                DATETIME_FORMAT.format(segmentStartTime),
                                DATETIME_FORMAT.format(lastTime),
                                segmentMinutes);
                    }

                    // 开始新的连续怠工段
                    segmentStartTime = currentTime;
                    logger.debug("开始新的连续怠工段: {} (间隔过大: {}ms)",
                            DATETIME_FORMAT.format(currentTime), timeDiff);
                }
            }

            lastTime = currentTime;
        }

        // 处理最后一个连续怠工段
        if (segmentStartTime != null && lastTime != null) {
            long segmentDurationMs = lastTime.getTime() - segmentStartTime.getTime();
            long segmentMinutes = segmentDurationMs / (60 * 1000);
            totalIdleMinutes += segmentMinutes;

            logger.debug("最后连续怠工段: {} -> {}, 持续时长: {} 分钟",
                    DATETIME_FORMAT.format(segmentStartTime),
                    DATETIME_FORMAT.format(lastTime),
                    segmentMinutes);
        }

        double idleHours = totalIdleMinutes / 60.0;

        logger.info("怠工时长计算完成 - 总怠工时长: {} 分钟 ({} 小时)", totalIdleMinutes, idleHours);

        return idleHours;
    }
}