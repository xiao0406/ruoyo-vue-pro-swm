package com.jeesite.modules.swm.web;

import cn.hutool.core.date.DateUtil;
import com.jeesite.common.web.BaseController;
import com.jeesite.modules.swm.entity.SwmDailyAttendance;
import com.jeesite.modules.swm.entity.SwmHazardSource;
import com.jeesite.modules.swm.entity.SwmPerson;
import com.jeesite.modules.swm.entity.SwmWarningManagement;
import com.jeesite.modules.swm.service.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 大屏数据看板Controller
 *
 * @author zwf
 * @version 2025-06-03
 */
@Controller
@RequestMapping(value = "${adminPath}/dashboard")
@Api(value = "大屏数据看板接口", tags = "大屏数据看板接口")
public class SwmDashboardController extends BaseController {

    @Autowired
    private SwmDashboardService swmDashboardService;
    @Autowired
    private SwmWarningManagementService swmWarningManagementService;
    @Autowired
    private SwmPersonService swmPersonService;
    @Autowired
    private SwmHazardSourceService swmHazardSourceService;
    @Autowired
    private SwmDailyAttendanceService swmDailyAttendanceService;

    /**
     * 获取启用状态的地图路径
     */
    @GetMapping(value = "getActiveMapPath")
    @ResponseBody
    @ApiOperation("获取启用状态的地图路径")
    public Map<String, Object> getActiveMapPath() {
        Map<String, Object> result = new HashMap<>();

        try {
            // 调用服务层方法获取地图路径
            String filePath = swmDashboardService.getActiveMapPath();

            if (filePath != null && !filePath.isEmpty()) {
                result.put("success", true);
                result.put("filePath", filePath);
            } else {
                result.put("success", false);
                result.put("message", "未找到启用状态的地图");
            }
        } catch (Exception e) {
            logger.error("获取地图路径失败", e);
            result.put("success", false);
            result.put("message", "获取地图路径失败: " + e.getMessage());
        }

        return result;
    }

    /**
     * 近七日预警报警统计
     */
    @GetMapping(value = "warningStatisticsForPast7Days")
    @ResponseBody
    @ApiOperation("近七日预警报警统计")
    public Map<String, Object> warningStatisticsForPast7Days() {
        Map<String, Object> result = new HashMap<>();

        // 1. 获取近7天的预警数据
        List<SwmWarningManagement> warnings = swmWarningManagementService.listPast7DaysWarning();
        result.put("record", warnings);

        // 2. 统计每种预警内容的总数
        Map<String, Long> warningMap = warnings.stream()
                .collect(Collectors.groupingBy(
                        SwmWarningManagement::getWarningContent,
                        Collectors.counting()
                ));
        result.put("warning", warningMap);

        // 3. 按日期和预警内容分组统计
        Map<String, Map<String, Long>> dailyWarningStats = warnings.stream()
                .collect(Collectors.groupingBy(
                        w -> DateUtil.format(w.getWarningTime(), "yyyy-MM-dd"), // 按日期分组
                        Collectors.groupingBy(
                                SwmWarningManagement::getWarningContent, // 按预警内容分组
                                Collectors.counting() // 统计数量
                        )
                ));

        // 4. 确保7天都有数据，没有的日期补0
        Map<String, Map<String, Long>> fullWeekStats = ensureFullWeekData(dailyWarningStats);

        // 5. 转换为前端需要的格式
        Map<String, Object> chartData = prepareChartData(fullWeekStats);

        result.put("chartData", chartData);
        return result;
    }

    /**
     * 确保7天都有数据，没有的日期补0
     */
    private Map<String, Map<String, Long>> ensureFullWeekData(Map<String, Map<String, Long>> originalData) {
        Map<String, Map<String, Long>> result = new LinkedHashMap<>();

        // 获取过去7天的日期列表
        List<String> last7Days = getLast7Days();

        // 获取所有预警内容类型
        Set<String> warningTypes = originalData.values().stream()
                .flatMap(map -> map.keySet().stream())
                .collect(Collectors.toSet());

        for (String day : last7Days) {
            Map<String, Long> dayData = originalData.getOrDefault(day, new HashMap<>());

            // 确保每种预警类型都有数据，没有的补0
            Map<String, Long> fullDayData = new HashMap<>();
            for (String type : warningTypes) {
                fullDayData.put(type, dayData.getOrDefault(type, 0L));
            }

            result.put(day, fullDayData);
        }

        return result;
    }

    /**
     * 获取包含今天在内的近7天日期列表(格式: yyyy-MM-dd)
     * 例如今天2025-06-15，返回: [2025-06-09, 2025-06-10, ..., 2025-06-15]
     */
    private List<String> getLast7Days() {
        List<String> days = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

        Calendar calendar = Calendar.getInstance();
        // 包含今天，所以从今天往前推6天
        for (int i = 0; i < 7; i++) {
            days.add(sdf.format(calendar.getTime()));
            calendar.add(Calendar.DAY_OF_YEAR, -1);
        }

        // 反转列表，使日期按从早到晚排序
        Collections.reverse(days);
        return days;
    }


    /**
     * 准备图表数据
     */
    private Map<String, Object> prepareChartData(Map<String, Map<String, Long>> fullWeekStats) {
        Map<String, Object> chartData = new HashMap<>();

        // 1. 准备日期列表
        List<String> dates = new ArrayList<>(fullWeekStats.keySet());
        chartData.put("dates", dates);

        // 2. 准备每种预警类型的数据系列
        Map<String, List<Long>> seriesData = new HashMap<>();

        // 获取所有预警类型
        Set<String> warningTypes = fullWeekStats.values().stream()
                .flatMap(map -> map.keySet().stream())
                .collect(Collectors.toSet());

        // 为每种预警类型准备数据
        for (String type : warningTypes) {
            List<Long> data = new ArrayList<>();
            for (String date : dates) {
                data.add(fullWeekStats.get(date).getOrDefault(type, 0L));
            }
            seriesData.put(type, data);
        }

        chartData.put("series", seriesData);

        return chartData;
    }

    /**
     * 今日预警统计
     */
    @GetMapping(value = "warningStatisticsForToday")
    @ResponseBody
    @ApiOperation("今日预警统计")
    public Map<String, Object> warningStatisticsForToday() {
        Map<String, Object> map = new HashMap<>();
        List<SwmWarningManagement> warnings = swmWarningManagementService.listTodayWarning();
        map.put("record", warnings);
        Map<String, Long> warningMap = warnings.stream()
                .collect(Collectors.groupingBy(
                        SwmWarningManagement::getWarningContent,
                        Collectors.counting()
                ));
        map.put("warning", warningMap);
        return map;
    }

    /**
     * 告警总数
     */
    @GetMapping("/warning/count")
    @ResponseBody
    @ApiOperation("报警预警总数")
    public Map<String, Object> warningCount() {
        Map<String, Object> map = new HashMap<>();
        SwmPerson swmPerson = new SwmPerson();
        swmPerson.setStatus("0");
        swmPerson.setPersonnelStatus(SwmPerson.PersonStatusEnum.ACTIVE);
        map.put("personCount", swmPersonService.findCount(swmPerson));
        SwmWarningManagement alarmQuery = new SwmWarningManagement();
        alarmQuery.setStatus("0");
        alarmQuery.setAlarmTimeIsNotNull(true);
        map.put("alarmCount", swmWarningManagementService.findCount(alarmQuery));
        SwmWarningManagement warningQuery = new SwmWarningManagement();
        warningQuery.setStatus("0");
        warningQuery.setWarningTimeIsNotNull(true);
        map.put("warningCount", swmWarningManagementService.findCount(warningQuery));
        return map;
    }

    /**
     * 危险源top10
     */
    @GetMapping("/hazard")
    @ResponseBody
    @ApiOperation("危险源")
    public Map<String, Object> hazard(@RequestParam("year") String year, @RequestParam("month") String month) {
        Map<String, Object> result = new HashMap<>();

        // 1. 按状态统计数量
        Map<String, Integer> statusCounts = new HashMap<>();
        statusCounts.put("WAIT", swmHazardSourceService.countByStatusAndMonth(SwmHazardSource.HazardSourceStatusEnum.WAIT, year, month));
        statusCounts.put("IN_PROGRESS", swmHazardSourceService.countByStatusAndMonth(SwmHazardSource.HazardSourceStatusEnum.IN_PROGRESS, year, month));
        statusCounts.put("COMPLETED", swmHazardSourceService.countByStatusAndMonth(SwmHazardSource.HazardSourceStatusEnum.COMPLETED, year, month));
        result.put("statusCounts", statusCounts);

        // 2. 按危险源类别统计数量
        List<Map<String, Object>> categoryCounts = swmHazardSourceService.countByCategoryAndMonth(year, month);
        result.put("categoryCounts", categoryCounts);

        // 3. 危险源类别排名前10
        List<Map<String, Object>> top10Categories = swmHazardSourceService.getTop10Categories(year, month);
        result.put("top10Categories", top10Categories);
        return result;
    }

    /**
     * 看板-劳动力管理-综合考勤统计
     */
    @GetMapping("/attendance/dashboard")
    @ResponseBody
    @ApiOperation("劳动力管理综合考勤统计")
    public Map<String, Object> attendanceDashboard(SwmPerson swmPerson) {
        Map<String, Object> result = new HashMap<>();
        Date today = new Date();
        String currentMonth = DateUtil.format(today, "yyyy-MM");

        // 1. 查询本月考勤数据并预加载人员信息
        List<SwmDailyAttendance> monthlyAttendance = swmDailyAttendanceService.findByMonth(null, currentMonth);
        Map<String, SwmPerson> personMap = preloadPersonData(monthlyAttendance);

        // 2. 筛选符合条件的考勤数据
        List<SwmDailyAttendance> filteredAttendance = filterAttendances(monthlyAttendance, personMap, swmPerson);

        // 3. 并行处理各项统计
        CompletableFuture<Map<String, Object>> todayStats = CompletableFuture.supplyAsync(
                () -> getTodayAttendanceStats(filteredAttendance, personMap));

        CompletableFuture<Map<String, Object>> monthlyEfficiency = CompletableFuture.supplyAsync(
                () -> getMonthlyEfficiencyStats(filteredAttendance));

        CompletableFuture<Map<String, Object>> monthlyAttendanceChart = CompletableFuture.supplyAsync(
                () -> getMonthlyAttendanceChartData(filteredAttendance, currentMonth));

        CompletableFuture<Map<String, Object>> monthlyEfficiencyChart = CompletableFuture.supplyAsync(
                () -> getMonthlyEfficiencyChartData(filteredAttendance, currentMonth));

        CompletableFuture<List<Map<String, Object>>> todayTeamRanking = CompletableFuture.supplyAsync(
                () -> getTeamRanking(filteredAttendance, personMap, true, 10));

        CompletableFuture<List<Map<String, Object>>> todayJobDistribution = CompletableFuture.supplyAsync(
                () -> getJobDistribution(filteredAttendance, personMap, true, 10));

        CompletableFuture<List<Map<String, Object>>> monthlyTeamRanking = CompletableFuture.supplyAsync(
                () -> getTeamRanking(filteredAttendance, personMap, false, 10));

        // 等待所有任务完成
        CompletableFuture.allOf(todayStats, monthlyEfficiency, monthlyAttendanceChart,
                monthlyEfficiencyChart, todayTeamRanking, todayJobDistribution,
                monthlyTeamRanking).join();

        // 组装结果
        try {
            result.put("todayAttendance", todayStats.get());
            result.put("monthlyEfficiency", monthlyEfficiency.get());
            result.put("monthlyAttendanceChart", monthlyAttendanceChart.get());
            result.put("monthlyEfficiencyChart", monthlyEfficiencyChart.get());
            result.put("todayTeamRanking", todayTeamRanking.get());
            result.put("todayJobDistribution", todayJobDistribution.get());
            result.put("monthlyTeamRanking", monthlyTeamRanking.get());
        } catch (Exception e) {
            logger.error("获取统计结果时出错", e);
            throw new RuntimeException("获取统计结果时出错", e);
        }

        return result;
    }

    /**
     * 预加载人员数据
     */
    private Map<String, SwmPerson> preloadPersonData(List<SwmDailyAttendance> attendances) {
        Set<String> employeeIds = attendances.stream()
                .map(SwmDailyAttendance::getEmployeeId)
                .collect(Collectors.toSet());

        return swmPersonService.findListByIds(employeeIds).stream()
                .collect(Collectors.toMap(SwmPerson::getId, Function.identity()));
    }

    /**
     * 筛选考勤数据
     */
    private List<SwmDailyAttendance> filterAttendances(List<SwmDailyAttendance> attendances,
                                                       Map<String, SwmPerson> personMap,
                                                       SwmPerson queryParams) {
        return attendances.parallelStream()
                .filter(a -> {
                    SwmPerson person = personMap.get(a.getEmployeeId());
                    return matchesQuery(person, queryParams);
                })
                .collect(Collectors.toList());
    }

    /**
     * 检查人员是否匹配查询条件
     */
    private boolean matchesQuery(SwmPerson person, SwmPerson queryParams) {
        if (person == null) return false;

        // 单位条件
        if (StringUtils.isNotBlank(queryParams.getCompany()) &&
                !queryParams.getCompany().equals(person.getCompany())) {
            return false;
        }

        // 车间条件
        if (StringUtils.isNotBlank(queryParams.getDepartment()) &&
                !queryParams.getDepartment().equals(person.getDepartment())) {
            return false;
        }

        // 工序条件
        if (StringUtils.isNotBlank(queryParams.getWorkProcess()) &&
                !queryParams.getWorkProcess().equals(person.getWorkProcess())) {
            return false;
        }

        // 班组条件
        if (StringUtils.isNotBlank(queryParams.getTeam()) &&
                !queryParams.getTeam().equals(person.getTeam())) {
            return false;
        }

        // 工种条件
        if (StringUtils.isNotBlank(queryParams.getJobType()) &&
                !queryParams.getJobType().equals(person.getJobType())) {
            return false;
        }

        return true;
    }

    /**
     * 获取今日考勤统计
     */
    private Map<String, Object> getTodayAttendanceStats(List<SwmDailyAttendance> attendances,
                                                        Map<String, SwmPerson> personMap) {
        Map<String, Object> result = new HashMap<>();
        String todayStr = DateUtil.format(new Date(), "yyyy-MM-dd");

        // 筛选今日数据
        List<SwmDailyAttendance> todayAttendances = attendances.parallelStream()
                .filter(a -> todayStr.equals(DateUtil.format(a.getAttendanceDate(), "yyyy-MM-dd")))
                .collect(Collectors.toList());

        // 按人员类型分组
        Map<String, List<SwmDailyAttendance>> byType = todayAttendances.parallelStream()
                .collect(Collectors.groupingBy(
                        a -> personMap.get(a.getEmployeeId()).getPersonType()));

        // 计算统计
        result.put("worker", calculateStats(byType.getOrDefault(SwmPerson.PersonTypeEnum.WORKER, Collections.emptyList())));
        result.put("manager", calculateStats(byType.getOrDefault(SwmPerson.PersonTypeEnum.MANAGER, Collections.emptyList())));

        return result;
    }

    /**
     * 计算基础统计指标
     */
    private Map<String, Object> calculateStats(List<SwmDailyAttendance> attendances) {
        Map<String, Object> stats = new HashMap<>();

        int totalCount = attendances.size();
        long presentCount = attendances.parallelStream()
                .filter(a -> a.getActualHours() != null && a.getActualHours().compareTo(BigDecimal.ZERO) > 0)
                .count();

        BigDecimal attendanceRate = totalCount == 0 ? BigDecimal.ZERO :
                BigDecimal.valueOf(presentCount)
                        .divide(BigDecimal.valueOf(totalCount), 4, RoundingMode.HALF_UP);

        long workingCount = attendances.parallelStream()
                .filter(a -> "0".equals(a.getCurrentPosition()))
                .count();

        stats.put("totalCount", totalCount);//应出人数
        stats.put("presentCount", presentCount);//实出人数
        stats.put("attendanceRate", attendanceRate);//出勤率
        stats.put("onSiteCount", totalCount); // 在场人数等于总人数
        stats.put("workingCount", workingCount);//工作中人数

        return stats;
    }
    // 2. 本月平均功效和考勤达成率
    private Map<String, Object> getMonthlyEfficiencyStats(List<SwmDailyAttendance> monthlyAttendances) {
        Map<String, Object> stats = new HashMap<>();

        // 计算平均功效
        BigDecimal avgEfficiency = monthlyAttendances.stream()
                .filter(a -> a.getDailyEfficiency() != null)
                .map(SwmDailyAttendance::getDailyEfficiency)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(monthlyAttendances.isEmpty() ? BigDecimal.ONE :
                        new BigDecimal(monthlyAttendances.size()), 2, RoundingMode.HALF_UP);
        // 计算本月总实际出勤时间和总应出勤时间
        BigDecimal totalScheduledHours = monthlyAttendances.stream()
                .map(a -> a.getScheduledHours() != null ? a.getScheduledHours() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalActualHours = monthlyAttendances.stream()
                .map(a -> a.getActualHours() != null ? a.getActualHours() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 计算考勤达成率(总实际/总应出勤)
        BigDecimal achievementRate = BigDecimal.ZERO;
        if (totalScheduledHours.compareTo(BigDecimal.ZERO) > 0) {
            achievementRate = totalActualHours
                    .divide(totalScheduledHours, 4, RoundingMode.HALF_UP);
        }


        stats.put("avgEfficiency", avgEfficiency);
        stats.put("achievementRate", achievementRate);
        return stats;
    }

    // 3. 本月每日出勤统计(折线图数据)
    private Map<String, Object> getMonthlyAttendanceChartData(List<SwmDailyAttendance> monthlyAttendances,String month) {
        Map<String, Object> chartData = new HashMap<>();

        // 获取本月所有天数
        List<String> daysInMonth = getDaysInMonth(month);
        chartData.put("dates", daysInMonth);

        // 按日期分组
        Map<String, List<SwmDailyAttendance>> dailyAttendances = monthlyAttendances.stream()
                .collect(Collectors.groupingBy(
                        a -> DateUtil.format(a.getAttendanceDate(), "yyyy-MM-dd")));

        // 准备图表数据
        List<Integer> scheduledList = new ArrayList<>();
        List<Integer> actualList = new ArrayList<>();
        List<BigDecimal> rateList = new ArrayList<>();

        for (String day : daysInMonth) {
            List<SwmDailyAttendance> dayAttendances = dailyAttendances.getOrDefault(day, Collections.emptyList());

            // 应出勤人数(有排班记录的人数)
            int scheduled = dayAttendances.size();
            // 实际出勤人数(实际考勤时长>0)
            long actual = dayAttendances.stream()
                    .filter(a -> a.getActualHours() != null && a.getActualHours().compareTo(BigDecimal.ZERO) > 0)
                    .count();
            // 出勤率
            BigDecimal rate = scheduled == 0 ? BigDecimal.ZERO :
                    BigDecimal.valueOf(actual).divide(BigDecimal.valueOf(scheduled), 4, RoundingMode.HALF_UP)
                            .multiply(BigDecimal.valueOf(100));

            scheduledList.add(scheduled);
            actualList.add((int) actual);
            rateList.add(rate);
        }

        chartData.put("scheduled", scheduledList);
        chartData.put("actual", actualList);
        chartData.put("rate", rateList);
        return chartData;
    }

    // 4. 本月每日功效统计(折线图数据)
    private Map<String, Object> getMonthlyEfficiencyChartData(List<SwmDailyAttendance> monthlyAttendances, String month) {
        Map<String, Object> chartData = new HashMap<>();

        // 获取本月所有天数
        List<String> daysInMonth = getDaysInMonth(month);
        chartData.put("dates", daysInMonth);

        // 按日期分组
        Map<String, List<SwmDailyAttendance>> dailyAttendances = monthlyAttendances.stream()
                .collect(Collectors.groupingBy(
                        a -> DateUtil.format(a.getAttendanceDate(), "yyyy-MM-dd")));

        // 准备图表数据
        List<BigDecimal> scheduledHoursList = new ArrayList<>();
        List<BigDecimal> actualHoursList = new ArrayList<>();
        List<BigDecimal> rateList = new ArrayList<>();

        for (String day : daysInMonth) {
            List<SwmDailyAttendance> dayAttendances = dailyAttendances.getOrDefault(day, Collections.emptyList());

            // 应考勤时长总和
            BigDecimal scheduledHours = dayAttendances.stream()
                    .map(a -> a.getScheduledHours() != null ? a.getScheduledHours() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            // 实际考勤时长总和
            BigDecimal actualHours = dayAttendances.stream()
                    .map(a -> a.getActualHours() != null ? a.getActualHours() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            // 考勤率
            BigDecimal rate = scheduledHours.compareTo(BigDecimal.ZERO) == 0 ? BigDecimal.ZERO :
                    actualHours.divide(scheduledHours, 4, RoundingMode.HALF_UP)
                            .multiply(BigDecimal.valueOf(100));

            scheduledHoursList.add(scheduledHours);
            actualHoursList.add(actualHours);
            rateList.add(rate);
        }

        chartData.put("scheduledHours", scheduledHoursList);
        chartData.put("actualHours", actualHoursList);
        chartData.put("rate", rateList);
        return chartData;
    }

    /**
     * 获取班组排名(今日或本月)
     */
    private List<Map<String, Object>> getTeamRanking(List<SwmDailyAttendance> attendances,
                                                     Map<String, SwmPerson> personMap,
                                                     boolean isToday,
                                                     int limit) {
        String todayStr = DateUtil.format(new Date(), "yyyy-MM-dd");

        // 按班组分组统计
        Map<String, TeamStats> teamStats = new ConcurrentHashMap<>();

        attendances.parallelStream()
                .filter(a -> !isToday || todayStr.equals(DateUtil.format(a.getAttendanceDate(), "yyyy-MM-dd")))
                .forEach(a -> {
                    SwmPerson person = personMap.get(a.getEmployeeId());
                    if (person == null || person.getTeam() == null) return;

                    TeamStats stats = teamStats.computeIfAbsent(person.getTeam(), k -> new TeamStats());
                    stats.totalCount++;

                    if (a.getActualHours() != null && a.getActualHours().compareTo(BigDecimal.ZERO) > 0) {
                        stats.presentCount++;
                    }

                    if (a.getDailyEfficiency() != null) {
                        stats.efficiencySum = stats.efficiencySum.add(a.getDailyEfficiency());
                    }
                });

        // 转换为结果并排序
        return teamStats.entrySet().stream()
                .map(entry -> {
                    TeamStats stats = entry.getValue();
                    Map<String, Object> result = new HashMap<>();
                    result.put("name", entry.getKey());
                    result.put("count", stats.totalCount);
                    result.put("presentCount", stats.presentCount);
                    result.put("attendanceRate", stats.totalCount == 0 ? BigDecimal.ZERO :
                            BigDecimal.valueOf(stats.presentCount)
                                    .divide(BigDecimal.valueOf(stats.totalCount), 4, RoundingMode.HALF_UP));
                    result.put("avgEfficiency", stats.totalCount == 0 ? BigDecimal.ZERO :
                            stats.efficiencySum.divide(BigDecimal.valueOf(stats.totalCount), 2, RoundingMode.HALF_UP));
                    return result;
                })
                .sorted((a, b) -> ((BigDecimal) b.get("avgEfficiency")).compareTo((BigDecimal) a.get("avgEfficiency")))
                .limit(limit)
                .collect(Collectors.toList());
    }

    /**
     * 获取工种排名
     * @param attendances
     * @param limit
     * @return
     */
    private List<Map<String, Object>> getJobDistribution(List<SwmDailyAttendance> attendances,Map<String, SwmPerson> personMap, boolean isToday, int limit) {
        // 按工种分组统计
        Map<String, Map<String, Object>> jobTypeStats = new HashMap<>();
        String todayStr = DateUtil.format(new Date(), "yyyy-MM-dd");
        // 筛选今日数据
        List<SwmDailyAttendance> todayAttendances = attendances.parallelStream()
                .filter(a -> todayStr.equals(DateUtil.format(a.getAttendanceDate(), "yyyy-MM-dd")))
                .collect(Collectors.toList());

        for (SwmDailyAttendance attendance : todayAttendances) {
            SwmPerson person = personMap.get(attendance.getEmployeeId());
            if (person == null || person.getTeam() == null) continue;

            String jobType = person.getJobType();
            Map<String, Object> stats = jobTypeStats.computeIfAbsent(jobType, k -> new HashMap<>());

            // 统计人数（应出勤）
            stats.put("count", (int) stats.getOrDefault("count", 0) + 1);

            // 统计实际出勤人数
            if (attendance.getActualHours() != null && attendance.getActualHours().compareTo(BigDecimal.ZERO) > 0) {
                stats.put("presentCount", (int) stats.getOrDefault("presentCount", 0) + 1);
            }

            // 累计功效
            if (attendance.getDailyEfficiency() != null) {
                stats.put("efficiencySum",
                        ((BigDecimal) stats.getOrDefault("efficiencySum", BigDecimal.ZERO))
                                .add(attendance.getDailyEfficiency()));
            }
        }

        // 计算各项指标并排序
        return jobTypeStats.entrySet().stream()
                .map(entry -> {
                    Map<String, Object> teamStat = entry.getValue();
                    int count = (int) teamStat.get("count");
                    int presentCount = (int) teamStat.getOrDefault("presentCount", 0);
                    BigDecimal efficiencySum = (BigDecimal) teamStat.getOrDefault("efficiencySum", BigDecimal.ZERO);

                    Map<String, Object> result = new HashMap<>();
                    result.put("name", entry.getKey());
                    result.put("count", count);
                    result.put("presentCount", presentCount);
                    result.put("attendanceRate", count == 0 ? BigDecimal.ZERO :
                            BigDecimal.valueOf(presentCount).divide(BigDecimal.valueOf(count), 4, RoundingMode.HALF_UP));
                    result.put("avgEfficiency", count == 0 ? BigDecimal.ZERO :
                            efficiencySum.divide(BigDecimal.valueOf(count), 2, RoundingMode.HALF_UP));
                    return result;
                })
                .sorted((a, b) -> ((BigDecimal) b.get("avgEfficiency")).compareTo((BigDecimal) a.get("avgEfficiency")))
                .limit(limit)
                .collect(Collectors.toList());
    }

    // 辅助方法：获取月份所有天数
    private List<String> getDaysInMonth(String month) {
        List<String> days = new ArrayList<>();
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM");
            Date date = sdf.parse(month);
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);

            int daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
            for (int i = 1; i <= daysInMonth; i++) {
                days.add(String.format("%s-%02d", month, i));
            }
        } catch (Exception e) {
            logger.error("获取月份天数出错", e);
        }
        return days;
    }

    /**
     * 班组统计辅助类
     */
    private static class TeamStats {
        int totalCount;
        int presentCount;
        BigDecimal efficiencySum = BigDecimal.ZERO;
    }

}
