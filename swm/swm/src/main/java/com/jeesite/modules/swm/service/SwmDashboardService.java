package com.jeesite.modules.swm.service;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
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

/**
 * 大屏数据看板Service
 * 
 * @author zwf
 * @version 2025-06-03
 */
@Service
@Transactional(readOnly = true)
public class SwmDashboardService {

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
            Map<String, Integer> onSiteStats = getOnSitePersonCount();
            result.put("onSiteWorkers", onSiteStats.get("workers"));
            result.put("onSiteManagers", onSiteStats.get("managers"));
            
            // 4. 获取触发报警数（主动报警）
            Map<String, Object> alarmStats = getAlarmStats();
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
            // TDengine数据库名
            String dbname = "plb";
            
            // 计算20分钟前的时间戳
            long twentyMinutesAgo = System.currentTimeMillis() - 20 * 60 * 1000;
            
            // 查询20分钟内有数据的设备和人员
            StringBuilder sqlBuilder = new StringBuilder();
            sqlBuilder.append("SELECT DISTINCT device_id, id_card FROM ")
                     .append(dbname).append(".helmet_runde_ca_report_location")
                     .append(" WHERE time > ").append(twentyMinutesAgo);
            
            R<cn.hutool.json.JSONObject> queryResult = tdengineService.executeTDengineSQL(sqlBuilder.toString());
            
            if (queryResult.getCode() == R.SUCCESS && queryResult.getData() != null) {
                cn.hutool.json.JSONObject data = queryResult.getData();
                cn.hutool.json.JSONArray rows = data.getJSONArray("data");
                
                if (rows != null && rows.size() > 0) {
                    // 收集所有在场人员的身份证号
                    Set<String> onSitePersonIdCards = new HashSet<>();
                    
                    for (int i = 0; i < rows.size(); i++) {
                        cn.hutool.json.JSONArray row = rows.getJSONArray(i);
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
     * 获取报警和预警统计
     */
    private Map<String, Object> getAlarmStats() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // TDengine数据库名
            String dbname = "plb";
            
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
                R<cn.hutool.json.JSONObject> queryResult = tdengineService.executeTDengineSQL(sqlBuilder.toString());
                
                if (queryResult.getCode() == R.SUCCESS && queryResult.getData() != null) {
                    cn.hutool.json.JSONObject data = queryResult.getData();
                    cn.hutool.json.JSONArray rows = data.getJSONArray("data");
                    
                    if (rows != null) {
                        for (int i = 0; i < rows.size(); i++) {
                            cn.hutool.json.JSONArray row = rows.getJSONArray(i);
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
     * 获取低电量人数
     */
    private int getLowBatteryPersonCount() {
        int lowBatteryCount = 0;
        
        try {
            // TDengine数据库名
            String dbname = "plb";
            
            // 使用TDengine的last_row函数获取每个设备最新的一条记录
            StringBuilder sqlBuilder = new StringBuilder();
            sqlBuilder.append("SELECT device_id, LAST_ROW(bat_l) as bat_l FROM ")
                      .append(dbname).append(".helmet_runde_ca_report_location")
                      .append(" WHERE bat_l IS NOT NULL")
                      .append(" GROUP BY device_id");
            
            R<cn.hutool.json.JSONObject> queryResult = tdengineService.executeTDengineSQL(sqlBuilder.toString());
            
            if (queryResult.getCode() == R.SUCCESS && queryResult.getData() != null) {
                cn.hutool.json.JSONObject data = queryResult.getData();
                cn.hutool.json.JSONArray rows = data.getJSONArray("data");
                
                if (rows != null) {
                    for (int i = 0; i < rows.size(); i++) {
                        cn.hutool.json.JSONArray row = rows.getJSONArray(i);
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
        query.setDeployStatus("已部署");
        
        // 执行查询
        List<SwmBeaconStation> beaconList = swmBeaconStationService.findList(query);
        
        // 转换为热力图所需格式
        List<Map<String, Object>> heatmapData = new ArrayList<>();
        
        if (beaconList != null && !beaconList.isEmpty()) {
            for (SwmBeaconStation beacon : beaconList) {
                // 确保坐标不为空
                if (beacon.getPixelX() != null && beacon.getPixelY() != null) {
                    Map<String, Object> point = new HashMap<>();
                    point.put("x", beacon.getPixelX());
                    point.put("y", beacon.getPixelY());
                    // 默认权重为1，表示每个点的热度相同
                    point.put("value", 1);
                    
                    // 添加额外信息，用于展示详情
                    point.put("id", beacon.getId());
                    point.put("beaconId", beacon.getBeaconId());
                    point.put("deviceName", beacon.getDeviceName());
                    point.put("location", beacon.getLocation());
                    point.put("area", beacon.getArea());
                    
                    heatmapData.add(point);
                }
            }
        }
        
        return heatmapData;
    }
} 