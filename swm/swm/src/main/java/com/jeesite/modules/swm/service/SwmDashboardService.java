package com.jeesite.modules.swm.service;

import cn.hutool.core.date.DateUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import com.jeesite.modules.cache.service.RedisService;
import com.jeesite.modules.swm.constant.SwmRedisConstant;
import com.jeesite.modules.sys.utils.CorpUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jeesite.common.lang.DateUtils;
import com.jeesite.modules.swm.entity.SwmPerson;
import com.jeesite.modules.swm.entity.SwmSiteMapManagement;
import com.jeesite.modules.swm.entity.SwmWarningManagement;
import com.jeesite.modules.swm.entity.SwmBeaconStation;
import com.jeesite.modules.utils.R;
import java.util.*;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;
import java.text.SimpleDateFormat;
import java.util.concurrent.TimeUnit;

/**
 * 大屏数据看板Service
 * 
 * @author zwf
 * @version 2025-06-03
 */
@Service
@Transactional(readOnly = true)
public class SwmDashboardService {
    
    // 添加 logger 实例
    private static final Logger logger = LoggerFactory.getLogger(SwmDashboardService.class);

    @Autowired
    private SwmSiteMapManagementService swmSiteMapManagementService;
    
    @Autowired
    private SwmPersonService swmPersonService;
    
    @Autowired
    private SwmPersonScheduleService swmPersonScheduleService;
    
    @Autowired
    private TDengineService tdengineService;
    
    @Autowired
    private SwmWarningManagementService swmWarningManagementService;
    
    @Autowired
    private SwmBeaconStationService swmBeaconStationService;

    @Value("${tdengine.dbname}")
    private String dbname;

    @Autowired
    private RedisService redisService;
    /**
     * 获取启用状态的地图路径
     * 
     * @return 地图文件路径
     */
    public String getActiveMapPath() {
        // 直接使用专门查询启用状态地图的方法
        SwmSiteMapManagement map = swmSiteMapManagementService.findActiveMap();
        
        if (map != null && map.getFilePath() != null) {
            return map.getFilePath();
        }
        
        return null;
    }
    
    /**
     * 获取大屏概览统计数据
     * 
     * @return 包含各种统计数据的Map
     */
    public Map<String, Object> getDashboardOverview() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 1. 获取总人数
            result.put("totalPersons", getTotalPersonCount());
            
            // 2. 获取排班人数
            result.put("scheduledPersons", getScheduledPersonCount());
            
            // 3. 获取在场人员统计
            // 使用新方法查询（基于当天位置数据）
            Map<String, Integer> onSiteStats = getOnSitePersonCountV2();
            // 如需使用原方法（基于20分钟内位置数据），可以切换为：
            // Map<String, Integer> onSiteStats = getOnSitePersonCount();
            result.put("onSiteWorkers", onSiteStats.get("workers"));
            result.put("onSiteManagers", onSiteStats.get("managers"));
            
            // 4. 获取触发报警数（主动报警）- 使用V2版本（当日数据，排除考勤打卡和进入大门）
            Map<String, Object> alarmStats = getAlarmStatsV2();
            // 如需使用原方法（当月数据），可以切换为：
            // Map<String, Object> alarmStats = getAlarmStats();
            result.put("activeAlarms", alarmStats.get("activeAlarms"));
            
            // 5. 获取触发预警数（危险源报警/安全预警）
            result.put("hazardAlarms", alarmStats.get("hazardAlarms"));
            
            // 6. 获取低电量人数
            result.put("lowBatteryPersons", getLowBatteryPersonCount());
            
        } catch (Exception e) {
            // 记录异常但返回空结果
            e.printStackTrace();
        }
        
        return result;
    }
    
    /**
     * 获取总人数（唯一身份证）
     */
    private int getTotalPersonCount() {
        try {
            // 通过DAO层查询唯一身份证数量
            return swmPersonService.countDistinctByIdentityCard();
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }
    
    /**
     * 获取当月排班人数（唯一身份证）
     */
    private int getScheduledPersonCount() {
        try {
            // 获取当前年月，格式为：yyyy-MM
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH) + 1; // Calendar月份从0开始
            
            // 格式化为"yyyy-MM"
            String yearMonth = String.format("%d-%02d", year, month);
            
            return swmPersonScheduleService.countDistinctPersonByYearAndMonth(yearMonth);
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }
    
    /**
     * 获取在场人员统计（工人和管理人员）
     */
    private Map<String, Integer> getOnSitePersonCount() {
        Map<String, Integer> result = new HashMap<>();
        result.put("workers", 0);
        result.put("managers", 0);
        
        try {
            // 计算20分钟前的时间戳
            long twentyMinutesAgo = System.currentTimeMillis() - 20 * 60 * 1000;
            
            // 查询20分钟内有数据的设备和人员
            StringBuilder sqlBuilder = new StringBuilder();
            sqlBuilder.append("SELECT DISTINCT device_id, id_card FROM ")
                     .append(dbname).append(".helmet_runde_ca_report_location")
                     .append(" WHERE time > ").append(twentyMinutesAgo);
            
            R<JSONObject> queryResult = tdengineService.executeTDengineSQL(sqlBuilder.toString());
            
            if (queryResult.getCode() == R.SUCCESS && queryResult.getData() != null) {
                JSONObject data = queryResult.getData();
                JSONArray rows = data.getJSONArray("data");
                
                if (rows != null && rows.size() > 0) {
                    // 收集所有在场人员的身份证号
                    Set<String> onSitePersonIdCards = new HashSet<>();
                    
                    for (int i = 0; i < rows.size(); i++) {
                        JSONArray row = rows.getJSONArray(i);
                        if (row != null && row.size() >= 2) {
                            String idCard = row.getStr(1);
                            if (idCard != null && !idCard.isEmpty()) {
                                onSitePersonIdCards.add(idCard);
                            }
                        }
                    }
                    
                    // 通过身份证号查询人员类型
                    if (!onSitePersonIdCards.isEmpty()) {
                        List<SwmPerson> onSitePersons = swmPersonService.findByIdCards(new ArrayList<>(onSitePersonIdCards));
                        
                        // 根据人员类型统计
                        int workerCount = 0;
                        int managerCount = 0;
                        
                        for (SwmPerson person : onSitePersons) {
                            if ("0".equals(person.getPersonType())) {
                                workerCount++;
                            } else if ("1".equals(person.getPersonType())) {
                                managerCount++;
                            }
                        }
                        
                        result.put("workers", workerCount);
                        result.put("managers", managerCount);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return result;
    }
    
    /**
     * 获取在场人员统计（新方法：基于当天位置数据）
     * 查询逻辑：
     * 1. 先从TDengine查询当天所有有位置数据的身份证
     * 2. 再从MySQL查询这些身份证对应的人员信息，只统计在职人员
     * 
     * @return 包含工人和管理人员在场数量的Map
     * @author Shawn
     * @date 2025-01-24
     */
    private Map<String, Integer> getOnSitePersonCountV2() {
        Map<String, Integer> result = new HashMap<>();
        result.put("workers", 0);
        result.put("managers", 0);
        
        try {
            // 1. 直接从TDengine查询当天所有有位置数据的身份证
//            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//            Calendar calendar = Calendar.getInstance();
            
//            // 当天开始时间 00:00:00
//            calendar.set(Calendar.HOUR_OF_DAY, 0);
//            calendar.set(Calendar.MINUTE, 0);
//            calendar.set(Calendar.SECOND, 0);
//            calendar.set(Calendar.MILLISECOND, 0);
//            String todayStartTime = sdf.format(calendar.getTime());
//
//            // 当天结束时间 23:59:59
//            calendar.set(Calendar.HOUR_OF_DAY, 23);
//            calendar.set(Calendar.MINUTE, 59);
//            calendar.set(Calendar.SECOND, 59);
//            String todayEndTime = sdf.format(calendar.getTime());

//            Date now = new Date();
//            Date oneMinuteAgo = DateUtil.offsetMinute(now, -10);
//            String todayStartTime = DateUtil.formatDateTime(oneMinuteAgo);
//            String todayEndTime = DateUtil.formatDateTime(now);
            
//            // 从TDengine获取当天所有有位置数据的身份证
//            Set<String> todayOnSiteIdCards = getTodayOnSiteIdCards(todayStartTime, todayEndTime);

            //身份证从redis里获取，先获取到所有的设备，然后在获取设备对应的身份证
            String corpCode = CorpUtils.getCurrentCorpCode();
            Set<Object> deviceIds = redisService.sGet(corpCode + SwmRedisConstant.RedisIotKey.ONLINE_DEVICES_KEY);
            Set<String> todayOnSiteIdCards = new HashSet<>();
            if (deviceIds != null) {
                for (Object deviceId : deviceIds) {
                    String currentPerson = (String) redisService.hget(SwmRedisConstant.RedisGlobalKey.DEVICE_PERSON_MAP, String.valueOf(deviceId));
                    todayOnSiteIdCards.add(currentPerson);
                }
            }



            if (todayOnSiteIdCards.isEmpty()) {
                return result;
            }
            
            // 2. 根据这些身份证查询MySQL，获取在职人员信息并按类型统计
            List<SwmPerson> onSitePersons = swmPersonService.findByIdCards(new ArrayList<>(todayOnSiteIdCards));
            
            int workerCount = 0;
            int managerCount = 0;
            
            for (SwmPerson person : onSitePersons) {
                // 只统计在职人员 (personnel_status='1' and status='0')
                if ("0".equals(person.getStatus()) && 
                    "1".equals(person.getPersonnelStatus())) {
                    if ("0".equals(person.getPersonType())) {
                        workerCount++;
                    } else if ("1".equals(person.getPersonType())) {
                        managerCount++;
                    }
                }
            }
            
            result.put("workers", workerCount);
            result.put("managers", managerCount);
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return result;
    }
    
    /**
     * 从TDengine获取当天所有有位置数据的身份证集合
     * 
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 身份证集合
     */
    private Set<String> getTodayOnSiteIdCards(String startTime, String endTime) {
        Set<String> idCards = new HashSet<>();
        
        try {
            // 查询当天有位置数据的所有身份证
            StringBuilder sqlBuilder = new StringBuilder();
            sqlBuilder.append("SELECT DISTINCT id_card FROM ")
                      .append(dbname).append(".external_coordinate_data")
                      .append(" WHERE time >= '").append(startTime).append("'")
                      .append(" AND time <= '").append(endTime).append("'")
                      .append(" AND original_x > 0")
                      .append(" AND original_y > 0")
                      .append(" AND id_card IS NOT NULL")
                      .append(" AND id_card != ''");
            
            R<JSONObject> queryResult = tdengineService.executeTDengineSQL(sqlBuilder.toString());
            
            if (queryResult.getCode() == R.SUCCESS && queryResult.getData() != null) {
                JSONObject data = queryResult.getData();
                JSONArray rows = data.getJSONArray("data");
                
                if (rows != null) {
                    for (int i = 0; i < rows.size(); i++) {
                        JSONArray row = rows.getJSONArray(i);
                        if (row != null && row.size() > 0) {
                            String idCard = row.getStr(0);
                            if (idCard != null && !idCard.isEmpty()) {
                                idCards.add(idCard);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return idCards;
    }
    
    
    /**
     * 获取报警和预警统计
     */
    private Map<String, Object> getAlarmStats() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 获取当月开始和结束的时间戳
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.DAY_OF_MONTH, 1); // 当月第一天
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            long monthStartTime = calendar.getTimeInMillis();
            
            calendar.add(Calendar.MONTH, 1); // 下个月第一天
            long nextMonthStartTime = calendar.getTimeInMillis();
            
            // TDengine特有语法：1. 不能使用COUNT(*)  2. 不能使用别名
            StringBuilder sqlBuilder = new StringBuilder();
            sqlBuilder.append("SELECT warning_content,COUNT(1) FROM ")
                    .append(dbname).append(".swm_warning_management")
                    .append(" WHERE warning_time >= ").append(monthStartTime)
                    .append(" AND warning_time < ").append(nextMonthStartTime)
                    .append(" AND status = '0'")
                    .append(" GROUP BY warning_content");
            
            // 执行查询并解析结果
            Map<String, Long> totalCountMap = new HashMap<>(); // 存储每种类型的总数
            
            try {
                // 使用tdengineService执行SQL查询
                R<JSONObject> queryResult = tdengineService.executeTDengineSQL(sqlBuilder.toString());
                
                if (queryResult.getCode() == R.SUCCESS && queryResult.getData() != null) {
                    JSONObject data = queryResult.getData();
                    JSONArray rows = data.getJSONArray("data");
                    
                    if (rows != null) {
                        for (int i = 0; i < rows.size(); i++) {
                            JSONArray row = rows.getJSONArray(i);
                            if (row != null && row.size() >= 2) {
                                String warningContent = row.getStr(0);
                                Long count = row.getLong(1);
                                totalCountMap.put(warningContent, count);
                            }
                        }
                    }
                } else {
                    // 如果TDengine查询失败，回退到原始方法
                    List<SwmWarningManagement> allWarnings = swmWarningManagementService.listCurrentMonthWarning();
                    totalCountMap = allWarnings.stream()
                            .collect(Collectors.groupingBy(
                                    SwmWarningManagement::getWarningContent,
                                    Collectors.counting()
                            ));
                }
            } catch (Exception e) {
                e.printStackTrace();
                // 异常情况回退到原始方法
                List<SwmWarningManagement> allWarnings = swmWarningManagementService.listCurrentMonthWarning();
                totalCountMap = allWarnings.stream()
                        .collect(Collectors.groupingBy(
                                SwmWarningManagement::getWarningContent,
                                Collectors.counting()
                        ));
            }
            
            // 查询已处置的预警统计 - 从MySQL数据库查询
            Map<String, Long> handledCountMap = new HashMap<>();
            try {
                // 从MySQL查询已处置预警
                List<SwmWarningManagement> handledWarnings = swmWarningManagementService.listCurrentMonthHandledWarning();
                handledCountMap = handledWarnings.stream()
                        .collect(Collectors.groupingBy(
                                SwmWarningManagement::getWarningContent,
                                Collectors.counting()
                        ));
            } catch (Exception e) {
                e.printStackTrace();
                // 如果查询异常，将已处置数量都设为0
            }
            
            // 4. 获取危险源报警（安全预警）数量
            long hazardAlarmCount = totalCountMap.getOrDefault("危险源报警", 0L);
            result.put("hazardAlarms", hazardAlarmCount);
            
            // 5. 计算主动报警（除了危险源报警外的所有）
            long activeTotalCount = 0L;
            long activeHandledCount = 0L;
            
            // 遍历所有报警类型，计算除危险源报警之外的总数和已处置数
            for (String warningType : totalCountMap.keySet()) {
                if (!"危险源报警".equals(warningType)) {
                    activeTotalCount += totalCountMap.get(warningType);
                }
            }
            
            for (String warningType : handledCountMap.keySet()) {
                if (!"危险源报警".equals(warningType)) {
                    activeHandledCount += handledCountMap.get(warningType);
                }
            }
            
            // 设置为"已处置/总数"格式
            result.put("activeAlarms", activeHandledCount + "/" + activeTotalCount);
            
        } catch (Exception e) {
            e.printStackTrace();
            result.put("hazardAlarms", 0);
            result.put("activeAlarms", "0/0");
        }
        
        return result;
    }
    
    /**
     * 获取报警和预警统计V2版本（查询当日数据，排除考勤打卡和进入大门）
     * 
     * @return Map containing:
     *   - activeAlarms: 主动报警 "已处置/总数"（当日，排除考勤打卡和进入大门）
     *   - hazardAlarms: 危险源报警数量（保持原有逻辑，查询当月）
     * @author Shawn
     * @date 2025-01-24
     */
    private Map<String, Object> getAlarmStatsV2() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 获取当日开始和结束的时间戳
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            long todayStartTime = calendar.getTimeInMillis();
            
            calendar.set(Calendar.HOUR_OF_DAY, 23);
            calendar.set(Calendar.MINUTE, 59);
            calendar.set(Calendar.SECOND, 59);
            calendar.set(Calendar.MILLISECOND, 999);
            long todayEndTime = calendar.getTimeInMillis();
            
            // 1. 查询当日总数（从TDengine，排除考勤打卡和进入大门）
            long totalCount = 0;
            StringBuilder totalSql = new StringBuilder();
            totalSql.append("SELECT COUNT(1) FROM ")
                    .append(dbname).append(".swm_warning_management")
                    .append(" WHERE warning_time >= ").append(todayStartTime)
                    .append(" AND warning_time <= ").append(todayEndTime)
                    .append(" AND warning_content NOT IN ('考勤打卡', '进入大门')")
                    .append(" AND status = '0'");
            
            try {
                R<JSONObject> totalResult = tdengineService.executeTDengineSQL(totalSql.toString());
                
                if (totalResult.getCode() == R.SUCCESS && totalResult.getData() != null) {
                    JSONObject data = totalResult.getData();
                    JSONArray rows = data.getJSONArray("data");
                    
                    if (rows != null && rows.size() > 0) {
                        JSONArray row = rows.getJSONArray(0);
                        if (row != null && row.size() > 0) {
                            totalCount = row.getLong(0);
                        }
                    }
                }
            } catch (Exception e) {
                logger.error("查询TDengine当日预警总数失败", e);
            }
            
            // 2. 查询当日已处理数（从MySQL，排除考勤打卡和进入大门）
            long handledCount = 0;
            try {
                handledCount = swmWarningManagementService.countTodayHandledWarnings();
            } catch (Exception e) {
                logger.error("查询MySQL当日已处理预警数失败", e);
            }
            
            // 3. 设置为"已处置/总数"格式
            result.put("activeAlarms", handledCount + "/" + totalCount);
            
            // 4. 危险源报警数量（保持原有逻辑，查询当月）
            long hazardAlarmCount = 0;
            try {
                // 获取当月开始和结束的时间戳
                calendar = Calendar.getInstance();
                calendar.set(Calendar.DAY_OF_MONTH, 1);
                calendar.set(Calendar.HOUR_OF_DAY, 0);
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.MILLISECOND, 0);
                long monthStartTime = calendar.getTimeInMillis();
                
                calendar.add(Calendar.MONTH, 1);
                long nextMonthStartTime = calendar.getTimeInMillis();
                
                // 查询当月危险源报警数量
                StringBuilder hazardSql = new StringBuilder();
                hazardSql.append("SELECT COUNT(1) FROM ")
                        .append(dbname).append(".swm_warning_management")
                        .append(" WHERE warning_time >= ").append(monthStartTime)
                        .append(" AND warning_time < ").append(nextMonthStartTime)
                        .append(" AND warning_content = '危险源报警'")
                        .append(" AND status = '0'");
                
                R<JSONObject> hazardResult = tdengineService.executeTDengineSQL(hazardSql.toString());
                
                if (hazardResult.getCode() == R.SUCCESS && hazardResult.getData() != null) {
                    JSONObject data = hazardResult.getData();
                    JSONArray rows = data.getJSONArray("data");
                    
                    if (rows != null && rows.size() > 0) {
                        JSONArray row = rows.getJSONArray(0);
                        if (row != null && row.size() > 0) {
                            hazardAlarmCount = row.getLong(0);
                        }
                    }
                }
            } catch (Exception e) {
                logger.error("查询TDengine危险源报警数失败", e);
            }
            
            result.put("hazardAlarms", hazardAlarmCount);
            
        } catch (Exception e) {
            logger.error("获取报警统计信息V2失败", e);
            result.put("hazardAlarms", 0);
            result.put("activeAlarms", "0/0");
        }
        
        return result;
    }
    
    /**
     * 获取低电量人数
     */
    private int getLowBatteryPersonCount() {
        int lowBatteryCount = 0;
        
        try {
            // 使用TDengine的last_row函数获取每个设备最新的一条记录
            StringBuilder sqlBuilder = new StringBuilder();
            sqlBuilder.append("SELECT device_id, LAST_ROW(bat_l) as bat_l FROM ")
                      .append(dbname).append(".helmet_runde_ca_report_location")
                      .append(" WHERE bat_l IS NOT NULL")
                      .append(" GROUP BY device_id");
            
            R<JSONObject> queryResult = tdengineService.executeTDengineSQL(sqlBuilder.toString());
            
            if (queryResult.getCode() == R.SUCCESS && queryResult.getData() != null) {
                JSONObject data = queryResult.getData();
                JSONArray rows = data.getJSONArray("data");
                
                if (rows != null) {
                    for (int i = 0; i < rows.size(); i++) {
                        JSONArray row = rows.getJSONArray(i);
                        if (row != null && row.size() >= 2) {
                            // 获取电池电量
                            Integer batLevel = row.getInt(1);
                            if (batLevel != null && batLevel < 20) {
                                lowBatteryCount++;
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return lowBatteryCount;
    }

    /**
     * 获取危险源信标的分布密度数据（用于热力图展示）
     * 
     * @return 包含热力图数据的列表
     */
    public List<Map<String, Object>> getHazardBeaconHeatmapData() {
        // 创建查询对象，筛选危险源信标（beacon_type=3）
        SwmBeaconStation query = new SwmBeaconStation();
        query.setBeaconType(SwmBeaconStation.BeaconTypeEnum.DANGEROUS_SOURCE);
        
        // 只查询已部署的信标 (先这样写，后面再改)
        query.setDeployStatus(SwmBeaconStation.DeployStatusEnum.DEPLOYED);
        
        // 执行查询
        List<SwmBeaconStation> beaconList = swmBeaconStationService.findList(query);
        
        // 使用Map来合并相同坐标点的数据
        Map<String, Map<String, Object>> pointMap = new HashMap<>();
        
        if (beaconList != null && !beaconList.isEmpty()) {
            for (SwmBeaconStation beacon : beaconList) {
                // 确保坐标不为空
                if (beacon.getPixelX() != null && beacon.getPixelY() != null) {
                    // 使用x,y组合作为Map的键
                    String key = beacon.getPixelX() + "," + beacon.getPixelY();
                    
                    // 如果坐标点已存在，增加权重值并添加信标信息
                    if (pointMap.containsKey(key)) {
                        Map<String, Object> point = pointMap.get(key);
                        int value = (int) point.get("value");
                        point.put("value", value + 1);
                        
                        // 更新该点的信标列表
                        @SuppressWarnings("unchecked")
                        List<Map<String, Object>> beacons = (List<Map<String, Object>>) point.get("beacons");
                        Map<String, Object> beaconInfo = new HashMap<>();
                        beaconInfo.put("id", beacon.getId());
                        beaconInfo.put("beaconId", beacon.getBeaconId());
                        beaconInfo.put("deviceName", beacon.getDeviceName());
                        beaconInfo.put("location", beacon.getLocation());
                        beaconInfo.put("area", beacon.getArea());
                        beacons.add(beaconInfo);
                    } else {
                        // 新建坐标点
                        Map<String, Object> point = new HashMap<>();
                        point.put("x", beacon.getPixelX());
                        point.put("y", beacon.getPixelY());
                        point.put("value", 1);
                        
                        // 添加信标信息列表
                        List<Map<String, Object>> beacons = new ArrayList<>();
                        Map<String, Object> beaconInfo = new HashMap<>();
                        beaconInfo.put("id", beacon.getId());
                        beaconInfo.put("beaconId", beacon.getBeaconId());
                        beaconInfo.put("deviceName", beacon.getDeviceName());
                        beaconInfo.put("location", beacon.getLocation());
                        beaconInfo.put("area", beacon.getArea());
                        beacons.add(beaconInfo);
                        point.put("beacons", beacons);
                        
                        pointMap.put(key, point);
                    }
                }
            }
        }
        
        // 将Map转换为列表
        List<Map<String, Object>> heatmapData = new ArrayList<>(pointMap.values());
        
        // 输出统计信息
        logger.info("查询到 {} 个危险源信标，合并后生成 {} 个热力点", 
                beaconList != null ? beaconList.size() : 0, heatmapData.size());
        
        return heatmapData;
    }
    
    /**
     * 获取违规热力图数据（靠近危险源信标的报警汇总，用于密度分布展示）
     * 
     * @param month 查询月份，格式 yyyy-MM
     * @return 包含热力图数据的列表
     */
    public List<Map<String, Object>> getViolationHeatmapData(String month) {
        List<Map<String, Object>> heatmapData = new ArrayList<>();
        
        try {
            // 计算月份的起止时间戳
            Calendar calendar = Calendar.getInstance();
            
            // 解析传入的月份
            String[] parts = month.split("-");
            int year = Integer.parseInt(parts[0]);
            int monthOfYear = Integer.parseInt(parts[1]);
            
            // 设置为当月1号
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, monthOfYear - 1); // 月份从0开始
            calendar.set(Calendar.DAY_OF_MONTH, 1);
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            
            long startTime = calendar.getTimeInMillis();
            
            // 设置为下月1号
            calendar.add(Calendar.MONTH, 1);
            long endTime = calendar.getTimeInMillis();
            
            // 构建SQL查询语句 - 第一步：获取危险源报警记录
            // 危险源报警类型为type=8
            StringBuilder warningSQL = new StringBuilder();
            warningSQL.append("SELECT device_id, id_card, warning_time FROM ")
                    .append(dbname).append(".swm_warning_management")
                    .append(" WHERE warning_time >= ").append(startTime)
                    .append(" AND warning_time < ").append(endTime)
                    .append(" AND type = 8")
                    .append(" AND status = '0'");
            
            logger.debug("危险源报警查询SQL: {}", warningSQL.toString());
            R<JSONObject> warningResult = tdengineService.executeTDengineSQL(warningSQL.toString());
            
            // 处理警告查询结果
            if (warningResult.getCode() != R.SUCCESS || warningResult.getData() == null) {
                logger.warn("未查询到危险源报警记录，SQL: {}", warningSQL.toString());
                return heatmapData;
            }
            
            JSONObject warningData = warningResult.getData();
            JSONArray warningRows = warningData.getJSONArray("data");
            
            if (warningRows == null || warningRows.size() == 0) {
                logger.warn("危险源报警记录为空");
                return heatmapData;
            }
            
            logger.info("查询到{}条危险源报警记录", warningRows.size());
            
            // 预先创建日期格式化对象，避免重复创建
            final java.text.SimpleDateFormat sdfUTC = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
            sdfUTC.setTimeZone(TimeZone.getTimeZone("UTC"));
            
            final java.text.SimpleDateFormat sdfLocal = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
            
            // 使用并行流处理数据，提高性能
            List<Map<String, Object>> violationList = Collections.synchronizedList(new ArrayList<>());
            
            // 并行处理所有行数据
            IntStream.range(0, warningRows.size()).parallel().forEach(i -> {
                JSONArray row = warningRows.getJSONArray(i);
                if (row != null && row.size() >= 3) {
                    try {
                        String deviceId = row.getStr(0);
                        String idCard = row.getStr(1);
                        
                        // 添加更多日志，但减少日志输出频率以提高性能
                        if (i % 100 == 0) {
                            logger.debug("处理报警记录: deviceId={}, idCard={}, row={}", deviceId, idCard, row);
                        }
                        
                        // 安全地获取warningTime值，先获取原始值
                        Object warningTimeObj = row.get(2);
                        Long warningTime = null;
                        
                        if (warningTimeObj != null) {
                            // 根据实际类型进行转换
                            if (warningTimeObj instanceof Long) {
                                warningTime = (Long) warningTimeObj;
                            } else if (warningTimeObj instanceof String) {
                                try {
                                    // 尝试解析ISO日期格式字符串
                                    String timeStr = (String) warningTimeObj;
                                    
                                    // 使用预先创建的SimpleDateFormat对象，避免重复创建
                                    synchronized (sdfUTC) {  // 同步访问，SimpleDateFormat不是线程安全的
                                        if (timeStr.endsWith("Z")) {
                                            // 处理ISO UTC格式，例如：2025-06-25T10:31:09.404Z
                                            timeStr = timeStr.replace("T", " ")
                                                            .replace("Z", "");
                                            warningTime = sdfUTC.parse(timeStr).getTime();
                                        } else {
                                            // 处理普通日期时间格式
                                            warningTime = sdfLocal.parse(timeStr).getTime();
                                        }
                                    }
                                    
                                    // 减少日志输出频率以提高性能
                                    if (i % 100 == 0) {
                                        logger.debug("解析日期字符串: {}, 转换后的warningTime: {}", timeStr, warningTime);
                                    }
                                } catch (Exception e) {
                                    logger.warn("解析警告时间字符串失败: {}", warningTimeObj, e);
                                }
                            } else if (warningTimeObj instanceof Number) {
                                warningTime = ((Number) warningTimeObj).longValue();
                            }
                        }
                        
                        // 只有当成功获取到时间才继续处理
                        if (warningTime == null) {
                            logger.warn("警告记录的时间为空或无法解析: deviceId={}, idCard={}, warningTimeObj={}", 
                                    deviceId, idCard, warningTimeObj);
                            return; // 在并行流中使用return而不是continue
                        }
                        
                        // 获取设备ID后8位
                        String deviceIdSuffix = "";
                        if (deviceId != null && deviceId.length() >= 8) {
                            deviceIdSuffix = deviceId.substring(deviceId.length() - 8);
                        }
                        
                        if (!deviceIdSuffix.isEmpty() && idCard != null && !idCard.isEmpty()) {
                            Map<String, Object> violation = new HashMap<>();
                            violation.put("deviceIdSuffix", deviceIdSuffix);
                            violation.put("idCard", idCard);
                            violation.put("warningTime", warningTime);
                            synchronized (violationList) {
                                violationList.add(violation);
                            }
                        }
                    } catch (Exception e) {
                        logger.error("处理报警记录出错: row={}", row, e);
                    }
                }
            });
            
            if (violationList.isEmpty()) {
                logger.warn("有效的危险源报警记录为空");
                return heatmapData;
            }
            
            logger.info("处理后得到{}条有效报警记录", violationList.size());
            
            // 随机取几条记录进行日志输出，便于调试
            if (!violationList.isEmpty()) {
                int sampleSize = Math.min(5, violationList.size());
                for (int i = 0; i < sampleSize; i++) {
                    Map<String, Object> sample = violationList.get(i);
                    logger.info("报警记录样本 #{}: deviceIdSuffix={}, idCard={}, warningTime={}", 
                            i+1, sample.get("deviceIdSuffix"), sample.get("idCard"), 
                            new Date((Long)sample.get("warningTime")));
                }
            }
            
            // 修改策略：不再使用一个大的批量查询，而是拆分为多个小查询，并使用更灵活的条件
            List<Map<String, Object>> coordinateDataList = new ArrayList<>();
            int batchSize = 5000; // 每批处理的记录数，从50改为200，提高效率
            
            // 统计信息
            int totalBatches = (violationList.size() + batchSize - 1) / batchSize;
            int successfulBatches = 0;
            int totalCoordinatesFound = 0;
            int targetCoordinatesCount = violationList.size(); // 目标坐标数据数量应等于报警记录数量
            
            for (int batchStart = 0; batchStart < violationList.size(); batchStart += batchSize) {
                // 如果已经获取到足够的坐标数据，可以提前结束查询
                if (totalCoordinatesFound >= targetCoordinatesCount) {
                    logger.info("已获取到足够的坐标数据（{}），停止查询", totalCoordinatesFound);
                    break;
                }
                
                int batchEnd = Math.min(batchStart + batchSize, violationList.size());
                List<Map<String, Object>> batch = violationList.subList(batchStart, batchEnd);
                
                int batchNumber = (batchStart / batchSize) + 1;
                logger.info("处理报警记录批次 {}/{}, 大小: {}", 
                        batchNumber, totalBatches, batch.size());
                
                // 查询这一批次的坐标数据
                List<Map<String, Object>> batchResults = getCoordinateDataForBatch(batch, dbname);
                if (batchResults != null && !batchResults.isEmpty()) {
                    coordinateDataList.addAll(batchResults);
                    totalCoordinatesFound += batchResults.size();
                    successfulBatches++;
                    logger.info("批次查询成功, 获取到{}条坐标数据, 累计: {}", 
                            batchResults.size(), totalCoordinatesFound);
                }
            }
            
            logger.info("所有批次处理完成，{}/{}个批次成功，总共获取到{}条坐标数据", 
                    successfulBatches, totalBatches, totalCoordinatesFound);
            
            // 如果没有找到足够的坐标数据，可以尝试使用更宽松的查询条件
            if (totalCoordinatesFound < targetCoordinatesCount) {
                logger.warn("坐标数据数量不足（{}），需要{}条，尝试使用更宽松的查询条件...", 
                       totalCoordinatesFound, targetCoordinatesCount);
                List<Map<String, Object>> relaxedResults = getCoordinateDataWithRelaxedConditions(violationList, dbname);
                
                if (relaxedResults != null && !relaxedResults.isEmpty()) {
                    int additionalResults = relaxedResults.size();
                    
                    // 如果放宽条件后获取的记录数量超过了需要的数量，截断到目标数量
                    int neededMore = targetCoordinatesCount - totalCoordinatesFound;
                    if (additionalResults > neededMore) {
                        relaxedResults = relaxedResults.subList(0, neededMore);
                        additionalResults = neededMore;
                    }
                    
                    coordinateDataList.addAll(relaxedResults);
                    totalCoordinatesFound += additionalResults;
                    logger.info("使用宽松条件额外获取到{}条坐标数据，累计: {}", 
                            additionalResults, totalCoordinatesFound);
                }
            }
            
            if (coordinateDataList.isEmpty()) {
                logger.warn("未找到任何坐标数据");
                
                // 输出最终统计
                logger.info("======= 统计信息 =======");
                logger.info("有效危险源报警记录总数: {}", violationList.size());
                logger.info("成功获取坐标的记录数: 0");
                logger.info("生成热力点数量: 0");
                logger.info("=========================");
                
                return heatmapData;
            }
            
            // 重置总坐标记录数，确保它反映的是实际处理的记录数
            totalCoordinatesFound = 0;
            
            // 使用Map来合并相同坐标点的数据，统计每个坐标点上的违规次数
            Map<String, Map<String, Object>> pointMap = new HashMap<>();
            int totalValueSum = 0; // 用于验证value总和
            int validCoordinateCount = 0; // 有效坐标记录数
            
            for (Map<String, Object> coordData : coordinateDataList) {
                String elderId = (String) coordData.get("elderId");
                String idCard = (String) coordData.get("idCard");
                
                // 修复类型转换问题，兼容Integer和Double两种类型
                int x, y;
                Object xObj = coordData.get("x");
                Object yObj = coordData.get("y");
                
                if (xObj instanceof Integer) {
                    x = (Integer) xObj;
                } else if (xObj instanceof Double) {
                    x = ((Double) xObj).intValue(); // 直接截断小数点后的数字，不进行四舍五入
                } else {
                    logger.warn("无效的x坐标类型: {}", xObj != null ? xObj.getClass().getName() : "null");
                    continue;
                }
                
                if (yObj instanceof Integer) {
                    y = (Integer) yObj;
                } else if (yObj instanceof Double) {
                    y = ((Double) yObj).intValue(); // 直接截断小数点后的数字，不进行四舍五入
                } else {
                    logger.warn("无效的y坐标类型: {}", yObj != null ? yObj.getClass().getName() : "null");
                    continue;
                }
                
                validCoordinateCount++; // 增加有效坐标计数
                
                // 使用x,y组合作为Map的键
                String key = x + "," + y;
                
                // 如果坐标点已存在，增加权重值
                if (pointMap.containsKey(key)) {
                    Map<String, Object> point = pointMap.get(key);
                    int value = (int) point.get("value");
                    point.put("value", value + 1);
                    totalValueSum++; // 增加计数
                    
                    // 更新该点的违规记录列表
                    @SuppressWarnings("unchecked")
                    List<String> violations = (List<String>) point.get("violations");
                    String violationInfo = "身份证: " + idCard + ", 设备号: " + elderId;
                    if (!violations.contains(violationInfo)) {
                        violations.add(violationInfo);
                    }
                } else {
                    // 新建坐标点
                    Map<String, Object> point = new HashMap<>();
                    point.put("x", x); // 使用整数坐标
                    point.put("y", y); // 使用整数坐标
                    point.put("value", 1);
                    totalValueSum++; // 增加计数
                    
                    // 添加违规记录信息
                    List<String> violations = new ArrayList<>();
                    violations.add("idCard: " + idCard + ", deviceId: " + elderId);
                    point.put("violations", violations);
                    
                    pointMap.put(key, point);
                }
            }
            
            // 更新实际处理的坐标记录数
            totalCoordinatesFound = validCoordinateCount;
            
            // 将Map转换为列表
            heatmapData = new ArrayList<>(pointMap.values());
            
            // 输出最终统计
            logger.info("======= 统计信息 =======");
            logger.info("有效危险源报警记录总数: {}", violationList.size());
            logger.info("成功获取坐标的记录数: {}", totalCoordinatesFound);
            logger.info("生成热力点数量: {}", heatmapData.size());
            logger.info("生成热力点value总和: {}", totalValueSum);
            
            // 验证value总和与记录数的一致性
            if (totalValueSum != totalCoordinatesFound) {
                logger.warn("热力点value总和({})与成功获取坐标的记录数({})不一致！", 
                           totalValueSum, totalCoordinatesFound);
            }
            
            // 验证坐标记录数与警告记录数的一致性
            if (totalCoordinatesFound != violationList.size()) {
                logger.warn("成功获取坐标的记录数({})与有效危险源报警记录总数({})不一致！", 
                           totalCoordinatesFound, violationList.size());
            }
            
            logger.info("=========================");
            
        } catch (Exception e) {
            logger.error("获取违规热力图数据异常", e);
            throw e;
        }
        
        return heatmapData;
    }
    
    /**
     * 为一批报警记录查询对应的坐标数据
     * 
     * @param batch 报警记录批次
     * @param dbname 数据库名
     * @return 坐标数据列表
     */
    private List<Map<String, Object>> getCoordinateDataForBatch(List<Map<String, Object>> batch, String dbname) {
        List<Map<String, Object>> results = new ArrayList<>();
        if (batch == null || batch.isEmpty()) {
            return results;
        }
        
        try {
            StringBuilder coordinateSQL = new StringBuilder();
            coordinateSQL.append("SELECT elder_id, id_card, time, x, y FROM ")
                    .append(dbname).append(".external_coordinate_data")
                    .append(" WHERE (");
            
            // 构建查询条件
            boolean hasValidCondition = false;
            for (int i = 0; i < batch.size(); i++) {
                try {
                    Map<String, Object> violation = batch.get(i);
                    String deviceIdSuffix = (String) violation.get("deviceIdSuffix");
                    String idCard = (String) violation.get("idCard");
                    Long warningTime = (Long) violation.get("warningTime");
                    
                    // 添加五分钟时间范围（扩大时间窗口）
                    long timeStart = warningTime - 300000; // 前5分钟
                    long timeEnd = warningTime + 300000;   // 后5分钟
                    
                    if (i > 0 && hasValidCondition) {
                        coordinateSQL.append(" OR ");
                    }
                    
                    // 使用更宽松的条件: elder_id包含设备ID后缀 AND id_card匹配 AND 时间范围匹配
                    coordinateSQL.append("(elder_id LIKE '%").append(deviceIdSuffix).append("'")
                                .append(" AND id_card = '").append(idCard).append("'")
                                .append(" AND time >= ").append(timeStart)
                                .append(" AND time <= ").append(timeEnd).append(")");
                    
                    hasValidCondition = true;
                } catch (Exception e) {
                    logger.error("构建坐标查询条件出错", e);
                }
            }
            
            coordinateSQL.append(")");
            
            if (!hasValidCondition) {
                logger.warn("没有有效的查询条件，跳过此批次");
                return results;
            }
            
            // 输出部分SQL语句用于调试
            String sqlForLog = coordinateSQL.toString();
            if (sqlForLog.length() > 500) {
                sqlForLog = sqlForLog.substring(0, 500) + "... [截断]";
            }
            logger.debug("坐标数据查询SQL: {}", sqlForLog);
            
            R<JSONObject> coordinateResult = tdengineService.executeTDengineSQL(coordinateSQL.toString());
            
            // 处理坐标查询结果
            if (coordinateResult.getCode() != R.SUCCESS || coordinateResult.getData() == null) {
                logger.warn("未查询到坐标数据");
                return results;
            }
            
            JSONObject coordinateData = coordinateResult.getData();
            JSONArray coordinateRows = coordinateData.getJSONArray("data");
            
            if (coordinateRows == null || coordinateRows.size() == 0) {
                logger.warn("坐标数据为空");
                return results;
            }
            
            logger.info("查询到{}条坐标数据", coordinateRows.size());
            
            for (int i = 0; i < coordinateRows.size(); i++) {
                try {
                    JSONArray row = coordinateRows.getJSONArray(i);
                    if (row != null && row.size() >= 5) {
                        String elderId = row.getStr(0);
                        String idCard = row.getStr(1);
                        Double x = row.getDouble(3);
                        Double y = row.getDouble(4);
                        
                        if (x != null && y != null) {
                            Map<String, Object> coordData = new HashMap<>();
                            coordData.put("elderId", elderId);
                            coordData.put("idCard", idCard);
                            // 将坐标转换为整数
                            coordData.put("x", (int)Math.round(x));
                            coordData.put("y", (int)Math.round(y));
                            results.add(coordData);
                        }
                    }
                } catch (Exception e) {
                    logger.error("处理坐标数据行出错: 索引={}", i, e);
                }
            }
            
            // 随机取几条记录进行日志输出，便于调试
            if (!results.isEmpty()) {
                int sampleSize = Math.min(3, results.size());
                for (int i = 0; i < sampleSize; i++) {
                    Map<String, Object> sample = results.get(i);
                    logger.info("坐标数据样本 #{}: elderId={}, idCard={}, x={}, y={}", 
                            i+1, sample.get("elderId"), sample.get("idCard"), 
                            sample.get("x"), sample.get("y"));
                }
            }
            
        } catch (Exception e) {
            logger.error("查询批次坐标数据异常", e);
        }
        
        return results;
    }
    
    /**
     * 使用更宽松的条件查询坐标数据
     * 
     * @param violationList 报警记录列表
     * @param dbname 数据库名
     * @return 坐标数据列表
     */
    private List<Map<String, Object>> getCoordinateDataWithRelaxedConditions(List<Map<String, Object>> violationList, String dbname) {
        List<Map<String, Object>> results = new ArrayList<>();
        
        // 收集所有不同的身份证号
        Set<String> allIdCards = new HashSet<>();
        for (Map<String, Object> violation : violationList) {
            String idCard = (String) violation.get("idCard");
            if (idCard != null && !idCard.isEmpty()) {
                allIdCards.add(idCard);
            }
        }
        
        if (allIdCards.isEmpty()) {
            logger.warn("没有有效的身份证号，无法查询");
            return results;
        }
        
        // 随机选择最多10个身份证号，避免查询条件过大
        List<String> sampleIdCards;
        if (allIdCards.size() <= 10) {
            sampleIdCards = new ArrayList<>(allIdCards);
        } else {
            sampleIdCards = new ArrayList<>(allIdCards);
            java.util.Collections.shuffle(sampleIdCards);
            sampleIdCards = sampleIdCards.subList(0, 10);
        }
        
        logger.info("使用宽松条件查询，选择了{}个身份证号", sampleIdCards.size());
        
        try {
            // 找出当月最早和最晚的报警时间，作为查询时间范围
            long minTime = Long.MAX_VALUE;
            long maxTime = Long.MIN_VALUE;
            
            for (Map<String, Object> violation : violationList) {
                Long warningTime = (Long) violation.get("warningTime");
                if (warningTime != null) {
                    minTime = Math.min(minTime, warningTime);
                    maxTime = Math.max(maxTime, warningTime);
                }
            }
            
            // 给时间范围增加一天的缓冲
            minTime -= 86400000; // 减少1天
            maxTime += 86400000; // 增加1天
            
            StringBuilder relaxedSQL = new StringBuilder();
            relaxedSQL.append("SELECT elder_id, id_card, time, x, y FROM ")
                    .append(dbname).append(".external_coordinate_data")
                    .append(" WHERE id_card IN (");
            
            // 添加身份证号条件
            for (int i = 0; i < sampleIdCards.size(); i++) {
                if (i > 0) {
                    relaxedSQL.append(",");
                }
                relaxedSQL.append("'").append(sampleIdCards.get(i)).append("'");
            }
            
            relaxedSQL.append(") AND time >= ").append(minTime)
                    .append(" AND time <= ").append(maxTime)
                    .append(" LIMIT 5000"); // 增加记录限制数量，以匹配更多报警记录
            
            logger.debug("宽松条件查询SQL: {}", relaxedSQL.toString());
            
            R<JSONObject> relaxedResult = tdengineService.executeTDengineSQL(relaxedSQL.toString());
            
            // 处理查询结果
            if (relaxedResult.getCode() != R.SUCCESS || relaxedResult.getData() == null) {
                logger.warn("宽松条件未查询到坐标数据");
                return results;
            }
            
            JSONObject relaxedData = relaxedResult.getData();
            JSONArray relaxedRows = relaxedData.getJSONArray("data");
            
            if (relaxedRows == null || relaxedRows.size() == 0) {
                logger.warn("宽松条件坐标数据为空");
                return results;
            }
            
            logger.info("宽松条件查询到{}条坐标数据", relaxedRows.size());
            
            for (int i = 0; i < relaxedRows.size(); i++) {
                try {
                    JSONArray row = relaxedRows.getJSONArray(i);
                    if (row != null && row.size() >= 5) {
                        String elderId = row.getStr(0);
                        String idCard = row.getStr(1);
                        Double x = row.getDouble(3);
                        Double y = row.getDouble(4);
                        
                        if (x != null && y != null) {
                            Map<String, Object> coordData = new HashMap<>();
                            coordData.put("elderId", elderId);
                            coordData.put("idCard", idCard);
                            // 将坐标转换为整数
                            coordData.put("x", (int)Math.round(x));
                            coordData.put("y", (int)Math.round(y));
                            results.add(coordData);
                        }
                    }
                } catch (Exception e) {
                    logger.error("处理宽松条件坐标数据行出错: 索引={}", i, e);
                }
            }
            
        } catch (Exception e) {
            logger.error("宽松条件查询坐标数据异常", e);
        }
        
        return results;
    }

    /**
     * 获取报警热力图数据（主动报警：一键SOS报警、脱帽报警、跌落报警、静默报警、近电报警）
     * 
     * @param month 查询月份，格式 yyyy-MM
     * @return 包含热力图数据的列表
     */
    public List<Map<String, Object>> getAlarmHeatmapData(String month) {
        List<Map<String, Object>> heatmapData = new ArrayList<>();
        
        try {
            // 计算月份的起止时间戳
            Calendar calendar = Calendar.getInstance();
            
            // 解析传入的月份
            String[] parts = month.split("-");
            int year = Integer.parseInt(parts[0]);
            int monthOfYear = Integer.parseInt(parts[1]);
            
            // 设置为当月1号
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, monthOfYear - 1); // 月份从0开始
            calendar.set(Calendar.DAY_OF_MONTH, 1);
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            
            long startTime = calendar.getTimeInMillis();
            
            // 设置为下月1号
            calendar.add(Calendar.MONTH, 1);
            long endTime = calendar.getTimeInMillis();
            
            // 构建SQL查询语句 - 查询主动报警记录
            // 这里筛选报警类型：一键SOS(0)、脱帽报警(1)、跌落报警(4)、静默报警(6)、近电报警(11)
            StringBuilder alarmSQL = new StringBuilder();
            alarmSQL.append("SELECT device_id, id_card, x, y, type FROM ")
                    .append(dbname).append(".swm_warning_management")
                    .append(" WHERE alarm_time >= ").append(startTime)
                    .append(" AND alarm_time < ").append(endTime)
                    .append(" AND type IN (0,1,4,6,11)")
                    .append(" AND status = '0'")
                    .append(" AND x IS NOT NULL AND y IS NOT NULL");
            
            logger.debug("主动报警热力图查询SQL: {}", alarmSQL.toString());
            R<JSONObject> alarmResult = tdengineService.executeTDengineSQL(alarmSQL.toString());
            
            // 处理查询结果
            if (alarmResult.getCode() != R.SUCCESS || alarmResult.getData() == null) {
                logger.warn("未查询到主动报警记录，SQL: {}", alarmSQL.toString());
                return heatmapData;
            }
            
            JSONObject alarmData = alarmResult.getData();
            JSONArray alarmRows = alarmData.getJSONArray("data");
            
            if (alarmRows == null || alarmRows.size() == 0) {
                logger.warn("主动报警记录为空");
                return heatmapData;
            }
            
            logger.info("查询到{}条主动报警记录", alarmRows.size());
            
            // 使用Map来合并相同坐标点的数据
            Map<String, Map<String, Object>> pointMap = new HashMap<>();
            
            // 处理每一行报警数据
            for (int i = 0; i < alarmRows.size(); i++) {
                JSONArray row = alarmRows.getJSONArray(i);
                if (row != null && row.size() >= 5) {
                    try {
                        String deviceId = row.getStr(0);
                        String idCard = row.getStr(1);
                        Double x = row.getDouble(2);
                        Double y = row.getDouble(3);
                        Integer type = row.getInt(4);
                        
                        if (x != null && y != null) {
                            // 使用x,y坐标作为key
                            String key = x + "," + y;
                            
                            // 如果已有该坐标点，增加权重值
                            if (pointMap.containsKey(key)) {
                                Map<String, Object> point = pointMap.get(key);
                                int value = (int) point.get("value");
                                point.put("value", value + 1);
                                
                                // 更新报警记录
                                @SuppressWarnings("unchecked")
                                List<Map<String, Object>> alarms = (List<Map<String, Object>>) point.get("alarms");
                                Map<String, Object> alarmInfo = new HashMap<>();
                                alarmInfo.put("deviceId", deviceId);
                                alarmInfo.put("idCard", idCard);
                                alarmInfo.put("type", type);
                                alarmInfo.put("typeText", getAlarmTypeText(type));
                                alarms.add(alarmInfo);
                            } else {
                                // 创建新的坐标点
                                Map<String, Object> point = new HashMap<>();
                                point.put("x", x);
                                point.put("y", y);
                                point.put("value", 1);
                                
                                // 添加报警记录
                                List<Map<String, Object>> alarms = new ArrayList<>();
                                Map<String, Object> alarmInfo = new HashMap<>();
                                alarmInfo.put("deviceId", deviceId);
                                alarmInfo.put("idCard", idCard);
                                alarmInfo.put("type", type);
                                alarmInfo.put("typeText", getAlarmTypeText(type));
                                alarms.add(alarmInfo);
                                point.put("alarms", alarms);
                                
                                pointMap.put(key, point);
                            }
                        }
                    } catch (Exception e) {
                        logger.error("处理报警记录出错: row={}", row, e);
                    }
                }
            }
            
            // 将Map转换为列表
            heatmapData = new ArrayList<>(pointMap.values());
            
            // 输出统计信息
            logger.info("查询到{}条主动报警记录，合并后生成{}个热力点",
                    alarmRows.size(), heatmapData.size());
            
        } catch (Exception e) {
            logger.error("生成报警热力图数据失败", e);
        }
        
        return heatmapData;
    }

    /**
     * 获取报警类型的显示文本
     * 
     * @param type 报警类型值
     * @return 报警类型显示文本
     */
    private String getAlarmTypeText(Integer type) {
        if (type == null) return "未知";
        
        switch (type) {
            case 0:
                return "一键SOS报警";
            case 1:
                return "脱帽报警";
            case 4:
                return "跌落报警";
            case 6:
                return "静默报警";
            case 11:
                return "近电报警";
            default:
                return "其他报警";
        }
    }
} 