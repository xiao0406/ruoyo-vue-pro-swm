package com.jeesite.modules.swm.web;

import cn.hutool.core.date.DateUtil;
import cn.hutool.json.JSON;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import com.jeesite.common.entity.Page;
import com.jeesite.common.lang.StringUtils;
import com.jeesite.common.web.http.ServletUtils;
import com.jeesite.modules.config.TenantContext;
import com.jeesite.modules.enums.CorpDbEnum;
import com.jeesite.modules.sys.utils.CorpUtils;
import com.jeesite.modules.sys.utils.UserUtils;
import com.jeesite.modules.utils.R;
import com.jeesite.common.web.BaseController;
import com.jeesite.modules.swm.entity.*;
import com.jeesite.modules.swm.service.*;
import com.jeesite.modules.sys.entity.DictData;
import com.jeesite.modules.sys.utils.DictUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.session.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import sun.text.resources.FormatData;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.util.concurrent.CompletableFuture.supplyAsync;

/**
 * 大屏数据看板Controller
 *
 * @author zwf
 * @version 2025-06-03
 */
@Slf4j
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
    @Autowired
    private TDengineService tdengineService;
    @Autowired
    private SwmBeaconStationService swmBeaconStationService;

    @Value("${tdengine.dbname}")
    private String dbname;

    @Qualifier("swmExecutor")
    @Autowired
    private ThreadPoolTaskExecutor swmExecutor;

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
     * 获取危险源信标的分布密度数据（用于热力图展示）
     */
    @GetMapping(value = "hazardBeaconHeatmap")
    @ResponseBody
    @ApiOperation("获取危险源信标的分布密度数据（用于热力图展示）")
    public Map<String, Object> hazardBeaconHeatmap() {
        Map<String, Object> result = new HashMap<>();

        try {
            // 调用Service层方法获取热力图数据
            List<Map<String, Object>> heatmapData = swmDashboardService.getHazardBeaconHeatmapData();

            // 封装返回结果
            result.put("success", true);
            result.put("data", heatmapData);
            result.put("total", heatmapData.size());
            result.put("message", "获取危险源信标分布密度数据成功");

        } catch (Exception e) {
            logger.error("获取危险源信标分布密度数据失败", e);
            result.put("success", false);
            result.put("message", "获取危险源信标分布密度数据失败: " + e.getMessage());
        }

        return result;
    }

    /**
     * 获取违规热力图数据（靠近危险源信标的报警汇总，用于密度分布展示）
     */
    @GetMapping(value = "violationHeatmap")
    @ResponseBody
    @ApiOperation("获取违规热力图数据（靠近危险源信标的报警汇总，用于密度分布展示）")
    public Map<String, Object> violationHeatmap(
            @RequestParam(value = "month", required = false) String month) {
        Map<String, Object> result = new HashMap<>();

        try {
            // 如果未指定月份，则使用当前月份
            if (StringUtils.isBlank(month)) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM");
                month = sdf.format(new Date());
            }

            // 调用Service层方法获取热力图数据
            List<Map<String, Object>> heatmapData = swmDashboardService.getViolationHeatmapData(month);

            // 封装返回结果
            result.put("success", true);
            result.put("data", heatmapData);
            result.put("total", heatmapData.size());
            result.put("message", "获取违规热力图数据成功");

        } catch (Exception e) {
            logger.error("获取违规热力图数据失败", e);
            result.put("success", false);
            result.put("message", "获取违规热力图数据失败: " + e.getMessage());
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
        List<SwmWarningManagement> warnings = swmWarningManagementService.warningStatisticsForPast7Days();

        // 2. 统计每种预警内容的总数
        Map<String, Long> warningMap = warnings.stream()
                .collect(Collectors.groupingBy(
                        SwmWarningManagement::getWarningContent,
                        Collectors.counting()));
        result.put("warning", warningMap);

        // 3. 按日期和预警内容分组统计
        Map<String, Map<String, Long>> dailyWarningStats = warnings.stream()
                .collect(Collectors.groupingBy(
                        w -> {
                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                            return sdf.format(w.getWarningTime());
                        }, // 按日期分组
                        Collectors.groupingBy(
                                SwmWarningManagement::getWarningContent, // 按预警内容分组
                                Collectors.counting() // 统计数量
                        )));

        // 4. 确保7天都有数据，没有的日期补0
        Map<String, Map<String, Long>> fullWeekStats = ensureFullWeekData(dailyWarningStats);

        // 5. 转换为前端需要的格式
        Map<String, Object> chartData = prepareChartData(fullWeekStats);

        result.put("chartData", chartData);
        return result;
    }


    /**
     * 近七日预警报警统计
     */
    @GetMapping(value = "warningStatisticsForPast7DaysNew")
    @ResponseBody
    @ApiOperation("近七日预警报警统计")
    public Map<String, Object> warningStatisticsForPast7DaysNew() {
        Map<String, Object> result = new HashMap<>();

        // 1. 取字典标签（这就是 DB 中的 warning_content 值）
        String dictLabel1 = DictUtils.getDictLabel("warning_content_enum", "长时间静止报警", "长时间静止报警");
        String dictLabel2 = DictUtils.getDictLabel("warning_content_enum", "脱帽报警", "脱帽报警");
        String dictLabel3 = DictUtils.getDictLabel("warning_content_enum", "跌落报警", "跌落报警");
        String dictLabel4 = DictUtils.getDictLabel("warning_content_enum", "危险区域闯入提示", "危险区域闯入提示");
        String dictLabel5 = DictUtils.getDictLabel("warning_content_enum", "应急呼叫", "应急呼叫");

        List<String> labels = Arrays.asList(dictLabel1, dictLabel2, dictLabel3, dictLabel4, dictLabel5);

        // 查询范围：最近 7 天（包含今天），格式 yyyy-MM-dd HH:mm:ss
        LocalDate today = LocalDate.now();
        String startDate = today.minusDays(6).atStartOfDay().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String endDate   = today.atTime(LocalTime.MAX).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        // 2. 使用单条聚合 SQL：按日期（yyyy-MM-dd）和 warning_content 聚合计数
        // 注意：TO_CHAR 用法沿用你项目中其它地方的风格
        String inClause = labels.stream().map(s -> "'" + s + "'").collect(Collectors.joining(","));
        String sql = "SELECT TO_CHAR(create_date, 'yyyy-MM-dd') AS day, warning_content, COUNT(1) AS cnt " +
                "FROM " + dbname + ".swm_warning_management " +
                "WHERE create_date >= '" + startDate + "' " +
                "AND create_date <= '" + endDate + "' " +
                "AND status = '0' " +
                "AND warning_content IN (" + inClause + ") " +
                "GROUP BY TO_CHAR(create_date, 'yyyy-MM-dd'), warning_content " +
                "ORDER BY day, warning_content";

        R<JSONObject> r = tdengineService.executeTDengineSQL(sql);

        // 建立 date -> (type -> count) 地图
        Map<String, Map<String, Long>> dailyMap = new LinkedHashMap<>();

        if (r.getCode() == R.SUCCESS && r.getData() != null) {
            JSONArray data = r.getData().getJSONArray("data");
            if (data != null) {
                for (int i = 0; i < data.size(); i++) {
                    JSONArray row = data.getJSONArray(i);
                    String day = row.getStr(0);
                    String type = row.getStr(1);
                    long cnt = row.getLong(2);

                    dailyMap.computeIfAbsent(day, k -> new HashMap<>())
                            .put(type, cnt);
                }
            }
        }

        // 3. 确保过去7天的每一天都有条目（补 0）
        List<String> last7Days = IntStream.rangeClosed(0, 6)
                .mapToObj(i -> LocalDate.now().minusDays(6 - i).format(DateTimeFormatter.ofPattern("yyyy-MM-dd")))
                .collect(Collectors.toList());

        Map<String, Map<String, Long>> fullWeek = new LinkedHashMap<>();
        for (String day : last7Days) {
            Map<String, Long> dayMap = dailyMap.getOrDefault(day, Collections.emptyMap());
            Map<String, Long> filled = new LinkedHashMap<>();
            // 只保留你要展示的三类/两类（你的业务需要）
            filled.put(dictLabel5, dayMap.getOrDefault(dictLabel5, 0L)); // 应急呼叫
            filled.put(dictLabel4, dayMap.getOrDefault(dictLabel4, 0L)); // 危险区域闯入提示
            // 异常行为预警 = dictLabel3 + dictLabel2 + dictLabel1
            long abnormal = dayMap.getOrDefault(dictLabel3, 0L)
                    + dayMap.getOrDefault(dictLabel2, 0L)
                    + dayMap.getOrDefault(dictLabel1, 0L);
            filled.put("异常行为预警", abnormal);
            fullWeek.put(day, filled);
        }

        // 4. 汇总各类总数（全周期）
        Map<String, Long> warningMapNew = new LinkedHashMap<>();
        long totalEmergency = fullWeek.values().stream().mapToLong(m -> m.getOrDefault(dictLabel5, 0L)).sum();
        long totalDanger = fullWeek.values().stream().mapToLong(m -> m.getOrDefault(dictLabel4, 0L)).sum();
        long totalAbnormal = fullWeek.values().stream().mapToLong(m -> m.getOrDefault("异常行为预警", 0L)).sum();

        warningMapNew.put(dictLabel5, totalEmergency);
        warningMapNew.put(dictLabel4, totalDanger);
        warningMapNew.put("异常行为预警", totalAbnormal);

        result.put("warning", warningMapNew);

        // 5. 构造 chartData（dates + series）
        Map<String, Object> chartData = new HashMap<>();
        List<String> dates = new ArrayList<>(fullWeek.keySet());
        chartData.put("dates", dates);

        Map<String, List<Long>> series = new LinkedHashMap<>();
        // series keys: dictLabel5, dictLabel4, "异常行为预警"
        List<Long> sEmergency = new ArrayList<>();
        List<Long> sDanger = new ArrayList<>();
        List<Long> sAbnormal = new ArrayList<>();
        for (String d : dates) {
            Map<String, Long> m = fullWeek.get(d);
            sEmergency.add(m.getOrDefault(dictLabel5, 0L));
            sDanger.add(m.getOrDefault(dictLabel4, 0L));
            sAbnormal.add(m.getOrDefault("异常行为预警", 0L));
        }
        series.put(dictLabel5, sEmergency);
        series.put(dictLabel4, sDanger);
        series.put("异常行为预警", sAbnormal);

        chartData.put("series", series);
        result.put("chartData", chartData);

        return result;
    }

    /**
     * 全部预警报警统计
     */
    @GetMapping(value = "warningStatistics")
    @ResponseBody
    @ApiOperation("全部预警报警统计")
    public Map<String, Object> warningStatistics() {
        Map<String, Object> result = new HashMap<>();

        // 1. 取字典标签（这就是 DB 中的 warning_content 值）
        String dictLabel1 = DictUtils.getDictLabel("warning_content_enum", "长时间静止报警", "长时间静止报警");
        String dictLabel2 = DictUtils.getDictLabel("warning_content_enum", "脱帽报警", "脱帽报警");
        String dictLabel3 = DictUtils.getDictLabel("warning_content_enum", "跌落报警", "跌落报警");
        String dictLabel4 = DictUtils.getDictLabel("warning_content_enum", "危险区域闯入提示", "危险区域闯入提示");
        String dictLabel5 = DictUtils.getDictLabel("warning_content_enum", "应急呼叫", "应急呼叫");

//        List<String> labels = Arrays.asList(dictLabel1, dictLabel2, dictLabel3, dictLabel4, dictLabel5);

        // 分组查询所有报警记录数量
        String sql = "SELECT warning_content, COUNT(1) AS cnt FROM " + dbname + ".swm_warning_management where status = '0' GROUP BY warning_content";
        R<JSONObject> r = tdengineService.executeTDengineSQL(sql);
        // 建立 date -> (type -> count)
        Map<String, Long> typeCount = new LinkedHashMap<>();
        if (r.getCode() == R.SUCCESS && r.getData() != null) {
            JSONArray data = r.getData().getJSONArray("data");
            if (data != null) {
                for (int i = 0; i < data.size(); i++) {
                    JSONArray row = data.getJSONArray(i);
                    String type = row.getStr(0);
                    Long count = row.getLong(1);
                    typeCount.put(type, count);
                }
            }
        }
        // 构造 chartData
        Map<String, Object> chartData = new HashMap<>();
        List<String> x = new ArrayList<>();
        x.add(dictLabel5);
        x.add(dictLabel4);
        x.add("异常行为");
        chartData.put("x", x);
        // 获取长时间静止报警数量
        long count1 = typeCount.getOrDefault(dictLabel1, 0L);
        // 获取脱帽报警数量
        long count2 = typeCount.getOrDefault(dictLabel2, 0L);
        // 获取跌落报警数量
        long count3 = typeCount.getOrDefault(dictLabel3, 0L);
        // 获取危险区域闯入提示数量
        long count4 = typeCount.getOrDefault(dictLabel4, 0L);
        // 获取应急呼叫报警数量
        long count5 = typeCount.getOrDefault(dictLabel5, 0L);
        List<Long> y = new ArrayList<>();

        String sqlBuilder = "SELECT create_date, person_name FROM " + dbname + ".swm_warning_management WHERE warning_content = '" + dictLabel5 + "'";
        // 执行查询
        R<JSONObject> tdRes = tdengineService.executeTDengineSQL(sqlBuilder);
        List<JSONObject> list = new ArrayList<>();
        if (tdRes.getCode() == R.SUCCESS && tdRes.getData() != null) {
            JSONObject data = tdRes.getData();
            JSONArray rows = data.getJSONArray("data");
//            JSONArray columnMeta = data.getJSONArray("column_meta");
            if (rows != null) {
                for (int i = 0; i < rows.size(); i++) {
                    JSONArray row = rows.getJSONArray(i);
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.set("create_date", row.getDate(0));
                    jsonObject.set("person_name", row.getStr(1));
                    list.add(jsonObject);
                }
            }
        }

        y.add(countUniquePersonsByDay(list));
        y.add(count4);
        y.add(count3 + count2 + count1);
        chartData.put("y", y);
        result.put("chartData", chartData);

        // 分组查询今日报警记录数量
        // 查询范围：今天，格式 yyyy-MM-dd HH:mm:ss
        LocalDate today = LocalDate.now();
        String startDate = today.atStartOfDay().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String endDate   = today.atTime(LocalTime.MAX).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String todaySql = "SELECT warning_content, COUNT(1) AS cnt FROM " + dbname + ".swm_warning_management where create_date >= '" + startDate +
                "' AND create_date <= '" + endDate + "' AND status = '0' GROUP BY warning_content";
        R<JSONObject> todayR = tdengineService.executeTDengineSQL(todaySql);
        Map<String, Long> todayTypeCount = new LinkedHashMap<>();
        if (todayR.getCode() == R.SUCCESS && todayR.getData() != null) {
            JSONArray data = todayR.getData().getJSONArray("data");
            if (data != null) {
                for (int i = 0; i < data.size(); i++) {
                    JSONArray row = data.getJSONArray(i);
                    String type = row.getStr(0);
                    Long count = row.getLong(1);
                    todayTypeCount.put(type, count);
                }
            }
        }

        Map<String, Long> todayData = new LinkedHashMap<>();
        // 获取长时间静止报警数量
        long todayCount1 = todayTypeCount.getOrDefault(dictLabel1, 0L);
        // 获取脱帽报警数量
        long todayCount2 = todayTypeCount.getOrDefault(dictLabel2, 0L);
        // 获取跌落报警数量
        long todayCount3 = todayTypeCount.getOrDefault(dictLabel3, 0L);
        // 获取危险区域闯入提示数量
        long todayCount4 = todayTypeCount.getOrDefault(dictLabel4, 0L);
        // 获取应急呼叫报警数量
        String todayEmergencySql = "SELECT warning_content, person_name, COUNT(1) AS cnt FROM " + dbname + ".swm_warning_management where create_date >= '" + startDate +
                "' AND create_date <= '" + endDate + "' AND status = '0' AND warning_content = '" + dictLabel5 + "' GROUP BY warning_content, person_name";
        R<JSONObject> todayEmergencyR = tdengineService.executeTDengineSQL(todayEmergencySql);
        long emergencyCount = 0;
        if (todayEmergencyR.getCode() == R.SUCCESS && todayEmergencyR.getData() != null) {
            JSONArray data = todayEmergencyR.getData().getJSONArray("data");
            if (data != null) {
                emergencyCount = data.size();
            }
        }
        todayData.put(dictLabel5, emergencyCount);
        todayData.put(dictLabel4, todayCount4);
        todayData.put("异常行为", todayCount3 + todayCount2 + todayCount1);

        result.put("todayData", todayData);
        return result;
    }

    public static long countUniquePersonsByDay(List<JSONObject> list) {
        // 用于存储每天的人员集合（自动去重）
        Map<String, Set<String>> dailyPersons = new HashMap<>();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        for (JSONObject obj : list) {
            try {
                // 解析完整日期并提取日期部分
                Date createDate = (obj.getDate("create_date"));
                String dayKey = dateFormat.format(createDate);
                // 获取或创建当天的Set
                Set<String> personsOfDay = dailyPersons.computeIfAbsent(dayKey, k -> new HashSet<>());
                // 添加人员姓名（自动去重）
                personsOfDay.add(obj.getStr("person_name"));
            } catch (Exception e) {
                e.printStackTrace();
                // 处理异常情况（如日期格式错误）
            }
        }
        long count = 0;
        // 转换为统计结果Map
        for (Map.Entry<String, Set<String>> entry : dailyPersons.entrySet()) {
            count = count + entry.getValue().size();
        }
        return count;
    }


    /**
     * 近七日预警报警记录（分页）
     */
    @GetMapping(value = "warningRecordsForPast7Days")
    @ResponseBody
    @ApiOperation("近七日预警报警记录（分页）")
    public Page<SwmWarningManagement> warningRecordsForPast7Days(SwmWarningManagement swmWarningManagement,
            Page<SwmWarningManagement> page) {
        // 获取近7天的预警数据（分页）
        return swmWarningManagementService.findPast7DaysWarningPage(swmWarningManagement, page);

    }

    /**
     * 近七日预警报警记录（分页）
     */
    @GetMapping(value = "warningRecordsForPast7DaysNew")
    @ResponseBody
    @ApiOperation("近七日预警报警记录（分页）")
    public Page<SwmWarningManagement> warningRecordsForPast7DaysNew(SwmWarningManagement swmWarningManagement,
                                                                 Page<SwmWarningManagement> page) {
        // 获取近7天的预警数据（分页）
        return swmWarningManagementService.findPast7DaysWarningPageNew(swmWarningManagement, page);

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

        String dictLabel1 = DictUtils.getDictLabel("warning_content_enum", "长时间静止报警", "长时间静止报警");
        String dictLabel2 = DictUtils.getDictLabel("warning_content_enum", "跌落报警", "跌落报警");
        String dictLabel3 = DictUtils.getDictLabel("warning_content_enum", "应急呼叫", "应急呼叫");

        // 获取统计数据
        try {
            // 使用TDengine直接查询统计数据
            // 获取今天开始和结束的时间戳
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            long todayStartTime = calendar.getTimeInMillis();

            calendar.add(Calendar.DAY_OF_YEAR, 1);
            long tomorrowStartTime = calendar.getTimeInMillis();


            // TDengine特有语法：1. 不能使用COUNT(*) 2. 不能使用别名
            StringBuilder sqlBuilder = new StringBuilder();
            sqlBuilder.append("SELECT warning_content,COUNT(1) FROM ")
                    .append(dbname).append(".swm_warning_management")
                    .append(" WHERE warning_time >= ").append(todayStartTime)
                    .append(" AND warning_time < ").append(tomorrowStartTime)
                    .append(" AND status = '0'")
                    .append(" AND ")
                    .append(String.format("warning_content IN ('%s','%s','%s')", dictLabel1, dictLabel2, dictLabel3))
                    .append(" GROUP BY warning_content");

            logger.info("执行今日预警统计SQL: {}", sqlBuilder.toString());

            // 执行查询并解析结果
            Map<String, Object> warningMap = new HashMap<>(); // 储存：长时间静止报警、跌落报警、应急呼叫
            Map<String, Long> totalCountMap = new HashMap<>(); // 存储每种类型的总数

            try {
                // 使用tdengineService执行SQL查询
                com.jeesite.modules.utils.R<cn.hutool.json.JSONObject> result = tdengineService
                        .executeTDengineSQL(sqlBuilder.toString());

                if (result.getCode() == com.jeesite.modules.utils.R.SUCCESS && result.getData() != null) {
                    cn.hutool.json.JSONObject data = result.getData();
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
                    logger.error("TDengine统计查询失败: {}", result.getMsg());
                    // 如果TDengine查询失败，回退到原始方法
                    List<SwmWarningManagement> allWarnings = swmWarningManagementService.listTodayWarning();
                    totalCountMap = allWarnings.stream()
                            .collect(Collectors.groupingBy(
                                    SwmWarningManagement::getWarningContent,
                                    Collectors.counting()));
                }
            } catch (Exception e) {
                logger.error("执行TDengine查询异常: {}", e.getMessage());
                // 异常情况回退到原始方法
                List<SwmWarningManagement> allWarnings = swmWarningManagementService.listTodayWarning();
                totalCountMap = allWarnings.stream()
                        .collect(Collectors.groupingBy(
                                SwmWarningManagement::getWarningContent,
                                Collectors.counting()));
            }

            // 查询已处置的预警统计
            Map<String, Long> handledCountMap = new HashMap<>();
            try {
                // 查询已处置预警
                List<SwmWarningManagement> handledWarnings = swmWarningManagementService.listTodayHandledWarning();
                handledCountMap = handledWarnings.stream()
                        .collect(Collectors.groupingBy(
                                SwmWarningManagement::getWarningContent,
                                Collectors.counting()));
            } catch (Exception e) {
                logger.error("查询已处置预警异常: {}", e.getMessage());
                // 如果查询异常，将已处置数量都设为0
            }

            // 处理危险源报警和安全预警（它们是同一个概念）
            Long hazardTotalCount = 0L;
            if (totalCountMap.containsKey("危险源报警")) {
                hazardTotalCount = totalCountMap.get("危险源报警");
                totalCountMap.remove("危险源报警");
            }

            Long hazardHandledCount = 0L;
            if (handledCountMap.containsKey("危险源报警")) {
                hazardHandledCount = handledCountMap.get("危险源报警");
                handledCountMap.remove("危险源报警");
            }




            // 计算跌落报警的总数和已处置数
            //长时间静止报警
            Long fallTotalCount1 = totalCountMap.getOrDefault(dictLabel1, 0L);
            Long fallHandledCount1 = handledCountMap.getOrDefault(dictLabel1, 0L);
            warningMap.put(dictLabel1, fallHandledCount1 + "/" + fallTotalCount1);
            //跌落报警
            Long fallTotalCount2 = totalCountMap.getOrDefault(dictLabel2, 0L);
            Long fallHandledCount2 = handledCountMap.getOrDefault(dictLabel2, 0L);
            warningMap.put(dictLabel2, fallHandledCount2 + "/" + fallTotalCount2);
            //应急呼叫
            Long fallTotalCount3 = totalCountMap.getOrDefault(dictLabel3, 0L);
            Long fallHandledCount3 = handledCountMap.getOrDefault(dictLabel3, 0L);
            warningMap.put(dictLabel3, fallHandledCount3 + "/" + fallTotalCount3);

            map.put("warning", warningMap);
        } catch (Exception e) {
            logger.error("获取今日预警统计数据异常", e);
            // 出现异常时回退到原始方法，使用默认格式
            List<SwmWarningManagement> allWarnings = swmWarningManagementService.listTodayWarning();
            Map<String, Long> totalCountMap = allWarnings.stream()
                    .collect(Collectors.groupingBy(
                            SwmWarningManagement::getWarningContent,
                            Collectors.counting()));

            Map<String, Object> warningMap = new HashMap<>();
//            warningMap.put("安全预警", totalCountMap.getOrDefault("危险源报警", 0L));
//            warningMap.put("跌落报警", "0/" + totalCountMap.getOrDefault("跌落报警", 0L));
//            warningMap.put("静默报警", "0/" + totalCountMap.getOrDefault("静默报警", 0L));
//
//            // 计算主动报警（除安全预警外所有报警的总和）
//            Long activeTotalCount = 0L;
//            for (Map.Entry<String, Long> entry : totalCountMap.entrySet()) {
//                if (!"危险源报警".equals(entry.getKey())) {
//                    activeTotalCount += entry.getValue();
//                }
//            }
//            warningMap.put("主动报警", "0/" + activeTotalCount);

            //长时间静止报警
            Long fallTotalCount1 = totalCountMap.getOrDefault(dictLabel1, 0L);
            warningMap.put(dictLabel1,  "0/" + fallTotalCount1);
            //跌落报警
            Long fallTotalCount2 = totalCountMap.getOrDefault(dictLabel2, 0L);
            warningMap.put(dictLabel2, "0/" + fallTotalCount2);
            //应急呼叫
            Long fallTotalCount3 = totalCountMap.getOrDefault(dictLabel3, 0L);
            warningMap.put(dictLabel3, "0/" + fallTotalCount3);

            map.put("warning", warningMap);
        }

        // 使用混合查询方法获取最新的20条记录
        List<SwmWarningManagement> latestWarnings = swmWarningManagementService.findTodayWarningWithHybrid();

        // 填充班组信息
        swmWarningManagementService.fillWorkGroupInfo(latestWarnings);

        // 填充位置信息
//        swmWarningManagementService.fillLocationInfo(latestWarnings);
        swmWarningManagementService.fillLocationInfoV1(latestWarnings);

        map.put("record", latestWarnings);

        return map;
    }

    /**
     * 今日预警统计
     * 已处置累计报警数/累计报警数、已处置今日报警数/今日报警数、已处置当前报警数/当前报警数（近5分钟）
     */
    @GetMapping("warningStatisticsForTodayNew")
    @ResponseBody
    @ApiOperation("今日预警统计")
    public Map<String, Object> warningStatisticsForTodayNew() {
        String corpCode = CorpUtils.getCurrentCorpCode();
        String corpName = CorpUtils.getCurrentCorpName();
        Map<String, Object> result = new HashMap<>();
        Map<String, Object> warningMap = new ConcurrentHashMap<>(); // 线程安全
        Date now = new Date();
        String format = "yyyy-MM-dd HH:mm:ss";

        try {
            // 统一计算时间
            String nowDayStartTime = DateUtil.format(DateUtil.beginOfDay(now), format);
            String nowDayEndTime = DateUtil.format(DateUtil.endOfDay(now), format);
            String fiveMinuteStartTime = DateUtil.format(DateUtil.offsetMinute(now, -5), format);

            // 异步任务
            CompletableFuture<Void> totalFuture = CompletableFuture.runAsync(() -> {
                try {
                    CorpUtils.setCurrentCorpCode(corpCode, corpName);
                    TenantContext.set(corpCode);
                    String totalAlarmNumber = warningStatistics(null, null);
                    warningMap.put("累计报警数", totalAlarmNumber);
                } catch (Exception e) {
                    logger.error("统计累计报警数异常", e);
                }finally {
                    CorpUtils.removeCurrentCorpCode(null);
                    TenantContext.clear();
                }
            }, swmExecutor);

            CompletableFuture<Void> todayFuture = CompletableFuture.runAsync(() -> {
                try {
                    CorpUtils.setCurrentCorpCode(corpCode, corpName);
                    TenantContext.set(corpCode);
                    String nowDayAlarmNumber = warningStatistics(nowDayStartTime, nowDayEndTime);
                    warningMap.put("今日报警数", nowDayAlarmNumber);
                } catch (Exception e) {
                    logger.error("统计今日报警数异常", e);
                }finally {
                    CorpUtils.removeCurrentCorpCode(null);
                    TenantContext.clear();
                }
            }, swmExecutor);

            CompletableFuture<Void> fiveMinuteFuture = CompletableFuture.runAsync(() -> {
                try {
                    CorpUtils.setCurrentCorpCode(corpCode, corpName);
                    TenantContext.set(corpCode);
                    String fiveMinuteAlarmNumber = warningStatistics(fiveMinuteStartTime, nowDayEndTime);
                    warningMap.put("当前报警数", fiveMinuteAlarmNumber);
                } catch (Exception e) {
                    logger.error("统计近五分钟报警数异常", e);
                }finally {
                    CorpUtils.removeCurrentCorpCode(null);
                    TenantContext.clear();
                }
            }, swmExecutor);

            CompletableFuture<Void> recordFuture = CompletableFuture.runAsync(() -> {
                try {
                    CorpUtils.setCurrentCorpCode(corpCode, corpName);
                    TenantContext.set(corpCode);
                    String dictLabel1 = DictUtils.getDictLabel("warning_content_enum", "长时间静止报警", "长时间静止报警");
                    String dictLabel2 = DictUtils.getDictLabel("warning_content_enum", "脱帽报警", "脱帽报警");
                    String dictLabel3 = DictUtils.getDictLabel("warning_content_enum", "跌落报警", "跌落报警");
                    String dictLabel4 = DictUtils.getDictLabel("warning_content_enum", "危险区域闯入提示", "危险区域闯入提示");
                    String dictLabel5 = DictUtils.getDictLabel("warning_content_enum", "应急呼叫", "应急呼叫");

                    List<String> labels = Arrays.asList(dictLabel1, dictLabel2, dictLabel3, dictLabel4, dictLabel5);
                    String inClause = labels.stream().map(s -> "'" + s + "'").collect(Collectors.joining(","));

                    String todayStartTime = DateUtil.format(DateUtil.beginOfDay(now), "yyyy-MM-dd HH:mm:ss");
                    String tomorrowStartTime = DateUtil.format(DateUtil.endOfDay(now), "yyyy-MM-dd HH:mm:ss");
                    StringBuilder sqlBuilder = new StringBuilder();


                    // 在SQL中使用TIMEDIFF函数添加8小时(28800000ms)到时间字段
                    sqlBuilder.append("SELECT id, person_name, warning_type, warning_content, ")
                            .append("warning_time + 8h AS warning_time, ")
                            .append("alarm_record,alarm_time + 8h AS alarm_time, ")
                            .append("trigger_reason, handler, handle_time, handle_process, handle_status, attachment, ")
                            .append("disposal_duration, ")
                            .append("create_by, create_date +8h AS create_date, update_by, update_date, remarks, status, device_id, id_card, ")
                            .append("front_alarm, type, x, y, hazard_category, location, area ")
                            .append("FROM ").append(dbname)
                            .append(".swm_warning_management_today ")
                            .append("WHERE warning_time >= '").append(todayStartTime).append("' ")
                            .append("AND warning_time < '").append(tomorrowStartTime).append("' ")
                            .append(" AND warning_content IN (")
                            .append(inClause)
                            .append(") ")
                            .append(" and status = '0' and person_name != '未知' ")
                            .append("order by create_date desc ")
                            .append("limit 20 ");
                    // 执行查询
                    R<JSONObject> tdRes = tdengineService.executeTDengineSQL(sqlBuilder.toString());
                    List<SwmWarningManagement> list = new ArrayList<>();
                    if (tdRes.getCode() == R.SUCCESS && tdRes.getData() != null) {
                        JSONObject data = tdRes.getData();
                        JSONArray rows = data.getJSONArray("data");
                        JSONArray columnMeta = data.getJSONArray("column_meta");

                        if (rows != null) {
                            for (int i = 0; i < rows.size(); i++) {
                                try {
                                    JSONArray row = rows.getJSONArray(i);
                                    SwmWarningManagement entity = swmWarningManagementService.convertToEntity(row, columnMeta);
                                    if (entity != null) {
                                        list.add(entity);
                                    }
                                } catch (Exception e) {
                                    logger.error("转换行数据异常: {}", e.getMessage());
                                }
                            }
                        }
                    }
                    swmWarningManagementService.fillWorkGroupInfo(list);
                    swmWarningManagementService.fillLocationInfoV1(list);
                    for (SwmWarningManagement warning : list) {
                        if (Arrays.asList(dictLabel1, dictLabel2, dictLabel3).contains(warning.getWarningContent())){
                            warning.setWarningTypeText(warning.getWarningContent());
                            warning.setWarningContent("异常行为预警");
                        }
                    }
                    result.put("record", list);
                } catch (Exception e) {
                    logger.error("获取已处置数据异常", e);
                } finally {
                    CorpUtils.removeCurrentCorpCode(null);
                    TenantContext.clear();
                }
            }, swmExecutor);

            // 等待所有任务完成
            CompletableFuture.allOf(totalFuture, todayFuture, fiveMinuteFuture, recordFuture).join();

            result.put("warning", warningMap);

        } catch (Exception e) {
            logger.error("获取今日预警统计数据异常", e);
        }

        return result;
    }



    /**
     * 查询报警统计
     */

    private String warningStatistics(String startTime, String endTime) {

        Long totalAlarmNumber = 0L;

        try {

            // TDengine SQL：必须 WHERE 开头
            StringBuilder sqlBuilder = new StringBuilder();
            sqlBuilder.append("SELECT COUNT(1) FROM ")
                    .append(dbname).append(".swm_warning_management")
                    .append(" WHERE status = '0'");

            // 时间条件
            if (StringUtils.isNotEmpty(startTime) && StringUtils.isNotEmpty(endTime)) {
                sqlBuilder.append(" AND warning_time >= '").append(startTime).append("'")
                        .append(" AND warning_time <= '").append(endTime).append("'");
            }

            // 执行
            R<JSONObject> result = tdengineService.executeTDengineSQL(sqlBuilder.toString());

            if (result.getCode() == R.SUCCESS && result.getData() != null) {

                JSONArray rows = result.getData().getJSONArray("data");

                if (rows != null && rows.size() > 0) {
                    // TDengine 的 COUNT 返回格式： [["12345"]]
                    totalAlarmNumber = rows.getJSONArray(0).getLong(0);
                }
            } else {
                logger.error("TDengine统计查询失败: {}", result.getMsg());
            }
        } catch (Exception e) {
            logger.error("执行TDengine查询异常: {}", e.getMessage());
        }


        //2.查询一处置的报警
        Long theTotalAlarmNumber = swmWarningManagementService.theAlarmHasBeenDealtWith(startTime, endTime);
        String result = theTotalAlarmNumber+"/"+totalAlarmNumber;

        return result;
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
     * 大屏概览数据
     */
    @GetMapping("/overview")
    @ResponseBody
    @ApiOperation("大屏概览数据")
    public Map<String, Object> getDashboardOverview() {
        try {
            // 调用服务层方法获取所有概览数据
            Map<String, Object> result = swmDashboardService.getDashboardOverview();
            // 添加接口调用成功标识
            result.put("success", true);
            return result;
        } catch (Exception e) {
            logger.error("获取大屏概览数据失败", e);
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("success", false);
            errorResult.put("message", "获取大屏概览数据失败: " + e.getMessage());
            return errorResult;
        }
    }

    /**
     * 大屏数据-看板-危险源（查询所有数据,去除月份限制)
     */
    @GetMapping("/hazard")
    @ResponseBody
    @ApiOperation("危险源")
    public Map<String, Object> hazard(@RequestParam("year") String year, @RequestParam("month") String month) {
        Map<String, Object> result = new HashMap<>();
        year = null;
        month = null;

        // 1. 按状态统计数量
        Map<String, Integer> statusCounts = new HashMap<>();
        statusCounts.put("WAIT",
                swmHazardSourceService.countByStatusAndMonth(SwmHazardSource.HazardSourceStatusEnum.WAIT, year, month));
        statusCounts.put("IN_PROGRESS", swmHazardSourceService
                .countByStatusAndMonth(SwmHazardSource.HazardSourceStatusEnum.IN_PROGRESS, year, month));

        // 已完成的统计（已关闭和已忽略的）
        int COMPLETED = swmHazardSourceService.countByStatusAndMonth(SwmHazardSource.HazardSourceStatusEnum.COMPLETED,
                year, month);
        int CANCELLED = swmHazardSourceService.countByStatusAndMonth(SwmHazardSource.HazardSourceStatusEnum.CANCELLED,
                year, month);
        statusCounts.put("COMPLETED", COMPLETED + CANCELLED);
        // 已发现(总数)
        statusCounts.put("ALL", swmHazardSourceService.countByYearAndMonth(year, month));
        result.put("statusCounts", statusCounts);

        // 2. 按危险源类别统计数量
        List<DictData> hazardCategoryList = DictUtils.getDictList("hazard_category_enum");
        List<Map<String, Object>> categoryCounts = swmHazardSourceService.countByCategoryAndMonth(year, month);
        List<Map<String, Object>> top10Categories = swmHazardSourceService.getTop10Categories(year, month);

        // 创建字典值(dictValue)到标签(dictLabelRaw)的映射
        Map<String, String> valueToLabelMap = hazardCategoryList.stream()
                .collect(Collectors.toMap(
                        DictData::getDictValue,
                        DictData::getDictLabelRaw,
                        (existing, replacement) -> existing)); // 如果有重复键，保留已存在的

        // 转换categoryCounts中的category值
        List<Map<String, Object>> transformedCategoryCounts = categoryCounts.stream()
                .map(originalMap -> {
                    Map<String, Object> newMap = new HashMap<>(originalMap);
                    if (originalMap.containsKey("category")) {
                        String dictValue = (String) originalMap.get("category");
                        String dictLabel = valueToLabelMap.getOrDefault(dictValue, dictValue);
                        newMap.put("category", dictLabel);
                    }
                    return newMap;
                })
                .collect(Collectors.toList());

        // 转换top10Categories中的category值
        List<Map<String, Object>> transformedTop10Categories = top10Categories.stream()
                .map(originalMap -> {
                    Map<String, Object> newMap = new HashMap<>(originalMap);
                    if (originalMap.containsKey("category")) {
                        String dictValue = (String) originalMap.get("category");
                        String dictLabel = valueToLabelMap.getOrDefault(dictValue, dictValue);
                        newMap.put("category", dictLabel);
                    }
                    return newMap;
                })
                .collect(Collectors.toList());

        result.put("categoryCounts", transformedCategoryCounts);
        result.put("top10Categories", transformedTop10Categories);
        return result;
    }

    /**
     * 看板-劳动力管理-综合考勤统计
     */
    @GetMapping("/attendance/dashboard")
    @ResponseBody
    @ApiOperation("劳动力管理综合考勤统计")
    public Map<String, Object> attendanceDashboard(PersonnelOrganizationQueryParam queryParam) {
        Map<String, Object> result = new HashMap<>();
        Date today = new Date();
        String currentMonth = DateUtil.format(today, "yyyy-MM");

        // 查询符合条件的人员
        List<SwmPerson> swmPersonList = swmPersonService.listByOrgAndWorkType(queryParam);

        // 如果没有查询到人员，返回空数据结构
        if (swmPersonList == null || swmPersonList.isEmpty()) {
            return getEmptyAttendanceDashboard();
        }

        List<String> swmPersonIdList = swmPersonList.stream()
                .map(SwmPerson::getId)
                .collect(Collectors.toList());

        // 1. 查询本月考勤数据并预加载人员信息
        List<SwmDailyAttendance> monthlyAttendance;
        if (!swmPersonIdList.isEmpty()) {
            monthlyAttendance = swmDailyAttendanceService.findByMonth(swmPersonIdList, currentMonth);
        } else {
            monthlyAttendance = new ArrayList<>();
        }

        Map<String, SwmPerson> personMap = swmPersonList.stream()
                .collect(Collectors.toMap(
                        SwmPerson::getId,
                        person -> person,
                        (existing, replacement) -> existing));
        // 3. 并行处理各项统计
        CompletableFuture<Map<String, Object>> todayStats = supplyAsync(
                () -> getTodayAttendanceStats(monthlyAttendance, personMap),swmExecutor);

        CompletableFuture<Map<String, Object>> monthlyEfficiency = supplyAsync(
                () -> getMonthlyEfficiencyStats(monthlyAttendance),swmExecutor);

        CompletableFuture<Map<String, Object>> monthlyAttendanceChart = supplyAsync(
                () -> getMonthlyAttendanceChartData(monthlyAttendance, currentMonth),swmExecutor);

        CompletableFuture<Map<String, Object>> monthlyEfficiencyChart = supplyAsync(
                () -> getMonthlyEfficiencyChartData(monthlyAttendance, currentMonth),swmExecutor);

        CompletableFuture<List<Map<String, Object>>> todayTeamRanking = supplyAsync(
                () -> getTeamRanking(monthlyAttendance, personMap, true, 10),swmExecutor);

        CompletableFuture<List<Map<String, Object>>> todayJobDistribution = supplyAsync(
                () -> getJobDistributionNew(monthlyAttendance, personMap, true, 10,queryParam.getShiftType()),swmExecutor);

        CompletableFuture<List<Map<String, Object>>> monthlyTeamRanking = supplyAsync(
                () -> getTeamRanking(monthlyAttendance, personMap, false, 10),swmExecutor);

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
        result.put("worker",
                calculateStats(byType.getOrDefault(SwmPerson.PersonTypeEnum.WORKER, Collections.emptyList())));
        result.put("manager",
                calculateStats(byType.getOrDefault(SwmPerson.PersonTypeEnum.MANAGER, Collections.emptyList())));

        return result;
    }

    /**
     * 计算基础统计指标
     */
    private Map<String, Object> calculateStats(List<SwmDailyAttendance> attendances) {
        Map<String, Object> stats = new HashMap<>();

        int totalCount = attendances.size();

        // 新的实际出勤人数逻辑：任一字段有值且大于0就算实际出勤
        long presentCount = attendances.parallelStream()
                .filter(a -> a.getClockInTime() != null ||
                        a.getClockOutTime() != null ||
                        (a.getActualHours() != null && a.getActualHours().compareTo(BigDecimal.ZERO) > 0) ||
                        (a.getIdleHours() != null && a.getIdleHours().compareTo(BigDecimal.ZERO) > 0) ||
                        (a.getEffectiveWorkHours() != null && a.getEffectiveWorkHours().compareTo(BigDecimal.ZERO) > 0))
                .count();

        BigDecimal attendanceRate = totalCount == 0 ? BigDecimal.ZERO
                : BigDecimal.valueOf(presentCount)
                        .divide(BigDecimal.valueOf(totalCount), 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100));

        // 获取当前处理的人员类型
        String personType = getCurrentPersonType(attendances);

        // 在场人数：从TDengine查询当天有位置数据的人数（按类型分类）
        Map<String, Integer> onSiteStats = getOnSiteCountByTypeFromTDengine();

        // 工作中人数：从TDengine查询1小时内有位置数据的人数（按类型分类）
        Map<String, Integer> workingStats = getWorkingCountByTypeFromTDengine();

        // 根据人员类型返回对应的统计数据
        int onSiteCount, workingCount;
        if ("0".equals(personType)) {
            // 工人统计
            onSiteCount = onSiteStats.getOrDefault("worker", 0);
            workingCount = workingStats.getOrDefault("worker", 0);
        } else {
            // 管理员统计
            onSiteCount = onSiteStats.getOrDefault("manager", 0);
            workingCount = workingStats.getOrDefault("manager", 0);
        }

        stats.put("totalCount", totalCount);// 应出人数
        stats.put("presentCount", presentCount);// 实出人数
        stats.put("attendanceRate", attendanceRate);// 出勤率
        stats.put("onSiteCount", onSiteCount); // 在场人数（当天有位置数据）
        stats.put("workingCount", workingCount);// 工作中人数（1小时内有位置数据）

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
                .divide(monthlyAttendances.isEmpty() ? BigDecimal.ONE : new BigDecimal(monthlyAttendances.size()), 2,
                        RoundingMode.HALF_UP);
        // 计算本月总实际工作时间和总应出勤时间
        BigDecimal totalScheduledHours = monthlyAttendances.stream()
                .map(a -> a.getScheduledHours() != null ? a.getScheduledHours() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 注意：actualHours现在存储的是基于工作区域计算的实际工作时长
        BigDecimal totalActualHours = monthlyAttendances.stream()
                .map(a -> a.getActualHours() != null ? a.getActualHours() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 计算考勤达成率(总实际工作时长/总应出勤时间)
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
    private Map<String, Object> getMonthlyAttendanceChartData(List<SwmDailyAttendance> monthlyAttendances,
            String month) {
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
            BigDecimal rate = scheduled == 0 ? BigDecimal.ZERO
                    : BigDecimal.valueOf(actual).divide(BigDecimal.valueOf(scheduled), 4, RoundingMode.HALF_UP)
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
    private Map<String, Object> getMonthlyEfficiencyChartData(List<SwmDailyAttendance> monthlyAttendances,
            String month) {
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

            // 实际工作时长总和（基于工作区域计算）
            BigDecimal actualHours = dayAttendances.stream()
                    .map(a -> a.getActualHours() != null ? a.getActualHours() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            // 达成率（实际工作时长/应考勤时长）
            BigDecimal rate = scheduledHours.compareTo(BigDecimal.ZERO) == 0 ? BigDecimal.ZERO
                    : actualHours.divide(scheduledHours, 4, RoundingMode.HALF_UP)
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
                    if (person == null || person.getTeam() == null)
                        return;

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
                    result.put("attendanceRate",
                            stats.totalCount == 0 ? BigDecimal.ZERO
                                    : BigDecimal.valueOf(stats.presentCount)
                                            .divide(BigDecimal.valueOf(stats.totalCount), 4, RoundingMode.HALF_UP));
                    result.put("avgEfficiency",
                            stats.totalCount == 0 ? BigDecimal.ZERO
                                    : stats.efficiencySum.divide(BigDecimal.valueOf(stats.totalCount), 2,
                                            RoundingMode.HALF_UP));
                    return result;
                })
                .sorted((a, b) -> ((BigDecimal) b.get("avgEfficiency")).compareTo((BigDecimal) a.get("avgEfficiency")))
                .limit(limit)
                .collect(Collectors.toList());
    }

    /**
     * 获取工种排名
     *
     * @param attendances
     * @param limit
     * @return
     */
    private List<Map<String, Object>> getJobDistribution(List<SwmDailyAttendance> attendances,
            Map<String, SwmPerson> personMap, boolean isToday, int limit) {
        // 按工种分组统计
        Map<String, Map<String, Object>> jobTypeStats = new HashMap<>();
        String todayStr = DateUtil.format(new Date(), "yyyy-MM-dd");
        // 筛选今日数据
        List<SwmDailyAttendance> todayAttendances = attendances.parallelStream()
                .filter(a -> todayStr.equals(DateUtil.format(a.getAttendanceDate(), "yyyy-MM-dd")))
                .collect(Collectors.toList());

        for (SwmDailyAttendance attendance : todayAttendances) {
            SwmPerson person = personMap.get(attendance.getEmployeeId());
            if (person == null || person.getTeam() == null)
                continue;

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
                    result.put("attendanceRate",
                            count == 0 ? BigDecimal.ZERO
                                    : BigDecimal.valueOf(presentCount).divide(BigDecimal.valueOf(count), 4,
                                            RoundingMode.HALF_UP));
                    result.put("avgEfficiency", count == 0 ? BigDecimal.ZERO
                            : efficiencySum.divide(BigDecimal.valueOf(count), 2, RoundingMode.HALF_UP));
                    return result;
                })
                .sorted((a, b) -> ((BigDecimal) b.get("avgEfficiency")).compareTo((BigDecimal) a.get("avgEfficiency")))
                .limit(limit)
                .collect(Collectors.toList());
    }

    private List<Map<String, Object>> getJobDistributionNew(List<SwmDailyAttendance> attendances,
                                                         Map<String, SwmPerson> personMap, boolean isToday, int limit,
                                                            String shiftType) {
        // 按工种分组统计
        String todayStr = DateUtil.format(new Date(), "yyyy-MM-dd");
        // 筛选今日数据
//        List<SwmDailyAttendance> todayAttendances = attendances.parallelStream()
//                .filter(a -> todayStr.equals(DateUtil.format(a.getAttendanceDate(), "yyyy-MM-dd")))
//                .collect(Collectors.toList());

        //根据传过来的时间，获取班次数据
        List<SwmDailyAttendance> todayAttendances = swmDailyAttendanceService.getShiftType(shiftType,attendances);

        // 对todayAttendances按personType进行分组
        Map<String, List<SwmDailyAttendance>> groupedByPersonType = todayAttendances.stream()
                .collect(Collectors.groupingBy(SwmDailyAttendance::getPersonType));

        List<Map<String, Object>> resultList = new ArrayList<>();
        for (Map.Entry<String, List<SwmDailyAttendance>> entry : groupedByPersonType.entrySet()) {
            Map<String, Object> result = new HashMap<>();
            List<DictData> personTypeEnum = DictUtils.getDictList("person_type_enum");
            for (DictData dictData : personTypeEnum) {
                result.put("name", dictData.getDictLabel());
                result.put("presentCount", 0L);
            }
            
            String key = entry.getKey();
            DictData dictData = DictUtils.getDictData("person_type_enum", key);
            Long number = 0L;
            List<SwmDailyAttendance> value = entry.getValue();
            // 统计打卡人数
            for (SwmDailyAttendance attendance : value) {
                if (attendance.getClockInDate() != null ) {
                    number++;
                }
            }
            result.put("name", dictData.getDictLabel());
            result.put("presentCount", number);
            // 创建子列表，包含具体工种信息
            List<Map<String, Object>> childResultList = new ArrayList<>();

            // 按工种进一步分组统计，同样添加空值检查
            Map<String, List<SwmDailyAttendance>> groupedByJobType = value.stream()
                    .filter(attendance -> attendance.getEmployeeId() != null) // 再次过滤
                    .collect(Collectors.groupingBy(attendance -> {
                        SwmPerson person = personMap.get(attendance.getEmployeeId());
                        // 处理工种为null的情况
                        return (person != null && person.getJobType() != null) ?
                                person.getJobType() : "未知工种";
                    }));

            // 填充具体的工种数据
            for (Map.Entry<String, List<SwmDailyAttendance>> jobEntry : groupedByJobType.entrySet()) {
                Map<String, Object> childResult = new HashMap<>();
                String jobType = jobEntry.getKey();
                List<SwmDailyAttendance> jobAttendances = jobEntry.getValue();

                // 统计该工种的打卡人数
                long jobPresentCount = jobAttendances.stream()
                        .filter(attendance -> attendance.getClockInDate() != null)
                        .count();

                // 只添加数量大于0的记录
                if (jobPresentCount > 0) {
                    childResult.put("jobTypeName", jobType);
                    childResult.put("jobTypeCount", jobPresentCount);
                    childResultList.add(childResult);
                }
            }

            // 按照数量降序排序
            childResultList.sort((a, b) -> {
                Long countA = (Long) a.get("jobTypeCount");
                Long countB = (Long) b.get("jobTypeCount");
                return countB.compareTo(countA);
            });

            result.put("jobTypes", childResultList);
            resultList.add(result);
        }
        return resultList;
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

    /**
     * 获取主动报警热力图数据（一键SOS、脱帽、跌落、静默、近电报警）
     */
    @GetMapping(value = "alarmHeatmap")
    @ResponseBody
    @ApiOperation("获取主动报警热力图数据（一键SOS、脱帽、跌落、静默、近电报警）")
    public Map<String, Object> alarmHeatmap(
            @RequestParam(value = "month", required = false) String month) {
        Map<String, Object> result = new HashMap<>();

        try {
            // 如果未指定月份，则使用当前月份
            if (StringUtils.isBlank(month)) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM");
                month = sdf.format(new Date());
            }

            // 调用Service层方法获取热力图数据
            List<Map<String, Object>> heatmapData = swmDashboardService.getAlarmHeatmapData(month);

            // 封装返回结果
            result.put("success", true);
            result.put("data", heatmapData);
            result.put("total", heatmapData.size());
            result.put("message", "获取主动报警热力图数据成功");

        } catch (Exception e) {
            logger.error("获取主动报警热力图数据失败", e);
            result.put("success", false);
            result.put("message", "获取主动报警热力图数据失败: " + e.getMessage());
        }

        return result;
    }

    /**
     * 从TDengine查询当天在场人数（按人员类型分类）
     * 统计当天有位置数据的唯一身份证数量，并按工人/管理员分类
     */
    private Map<String, Integer> getOnSiteCountByTypeFromTDengine() {
        Map<String, Integer> result = new HashMap<>();
        result.put("worker", 0);
        result.put("manager", 0);

        try {
            // 获取当天的开始和结束时间字符串
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Calendar calendar = Calendar.getInstance();

            // 当天开始时间 00:00:00
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            String todayStartTime = sdf.format(calendar.getTime());

            // 当天结束时间 23:59:59
            calendar.set(Calendar.HOUR_OF_DAY, 23);
            calendar.set(Calendar.MINUTE, 59);
            calendar.set(Calendar.SECOND, 59);
            String todayEndTime = sdf.format(calendar.getTime());

            // 从TDengine获取当天的唯一身份证集合
            Set<String> uniqueIdCards = getUniqueIdCardsFromTDengine(todayStartTime, todayEndTime);

            if (!uniqueIdCards.isEmpty()) {
                // 通过身份证查询人员信息并按类型分类统计
                Map<String, Integer> typeStats = classifyPersonsByType(uniqueIdCards);
                result.put("worker", typeStats.get("worker"));
                result.put("manager", typeStats.get("manager"));

                logger.info("查询到当天在场人数 - 工人: {}, 管理员: {}",
                        typeStats.get("worker"), typeStats.get("manager"));
            } else {
                logger.warn("查询当天在场人数失败或无数据");
            }

        } catch (Exception e) {
            logger.error("查询TDengine当天在场人数分类统计失败", e);
        }

        return result;
    }

    /**
     * 从TDengine查询1小时内工作中人数（按人员类型分类）
     * 统计1小时内有位置数据的唯一身份证数量，并按工人/管理员分类
     */
    private Map<String, Integer> getWorkingCountByTypeFromTDengine() {
        Map<String, Integer> result = new HashMap<>();
        result.put("worker", 0);
        result.put("manager", 0);

        try {
            // 获取1小时前的时间字符串
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.HOUR_OF_DAY, -1); // 减去1小时
            String oneHourAgo = sdf.format(calendar.getTime());

            // 从TDengine获取1小时内的唯一身份证集合
            Set<String> uniqueIdCards = getUniqueIdCardsFromTDengine(oneHourAgo, null);

            if (!uniqueIdCards.isEmpty()) {
                // 通过身份证查询人员信息并按类型分类统计
                Map<String, Integer> typeStats = classifyPersonsByType(uniqueIdCards);
                result.put("worker", typeStats.get("worker"));
                result.put("manager", typeStats.get("manager"));

                logger.info("查询到1小时内工作中人数 - 工人: {}, 管理员: {} (1小时前时间: {})",
                        typeStats.get("worker"), typeStats.get("manager"), oneHourAgo);
            } else {
                logger.warn("查询1小时内工作中人数失败或无数据");
            }

        } catch (Exception e) {
            logger.error("查询TDengine 1小时内工作中人数分类统计失败", e);
        }

        return result;
    }

    /**
     * 从TDengine获取指定时间范围内的唯一身份证集合
     * 
     * @param startTime 开始时间（必填）
     * @param endTime   结束时间（可选，为null时只使用开始时间条件）
     */
    private Set<String> getUniqueIdCardsFromTDengine(String startTime, String endTime) {
        Set<String> uniqueIdCards = new HashSet<>();

        try {
            String sql;

            if (endTime != null) {
                // 时间范围查询（当天数据）
                sql = String.format(
                        "SELECT DISTINCT id_card FROM %s.external_coordinate_data WHERE time >= '%s' AND time <= '%s'",
                        dbname, startTime, endTime);
//                logger.info("查询TDengine身份证SQL: {} (时间范围: {} 到 {})", sql, startTime, endTime);
            } else {
                // 单一时间点查询（1小时内数据）
                sql = String.format(
                        "SELECT DISTINCT id_card FROM %s.external_coordinate_data WHERE time >= '%s'",
                        dbname, startTime);
//                logger.info("查询TDengine身份证SQL: {} (开始时间: {})", sql, startTime);
            }

            R<cn.hutool.json.JSONObject> result = tdengineService.executeTDengineSQL(sql);

            if (result.getCode() == R.SUCCESS && result.getData() != null) {
                cn.hutool.json.JSONObject data = result.getData();
                cn.hutool.json.JSONArray rows = data.getJSONArray("data");
                if (rows != null && !rows.isEmpty()) {
                    for (int i = 0; i < rows.size(); i++) {
                        cn.hutool.json.JSONArray row = rows.getJSONArray(i);
                        if (row != null && row.size() > 0) {
                            String idCard = row.getStr(0);
                            if (idCard != null && !idCard.trim().isEmpty()) {
                                uniqueIdCards.add(idCard);
                            }
                        }
                    }
                    logger.info("从TDengine查询到 {} 个唯一身份证", uniqueIdCards.size());
                }
            }
        } catch (Exception e) {
            logger.error("从TDengine查询身份证失败", e);
        }

        return uniqueIdCards;
    }

    /**
     * 根据身份证号查询人员信息并按类型分类统计
     * 
     * @param idCards 身份证号集合
     * @return 分类统计结果 {"worker": 工人数量, "manager": 管理员数量}
     */
    private Map<String, Integer> classifyPersonsByType(Set<String> idCards) {
        Map<String, Integer> result = new HashMap<>();
        result.put("worker", 0);
        result.put("manager", 0);

        try {
            if (idCards.isEmpty()) {
                return result;
            }

            // 通过身份证号查询人员信息
            List<SwmPerson> persons = swmPersonService.findByIdCards(new ArrayList<>(idCards));

            // 按人员类型分类统计
            int workerCount = 0;
            int managerCount = 0;

            for (SwmPerson person : persons) {
                if ("0".equals(person.getPersonType())) {
                    workerCount++; // 工人
                } else if ("1".equals(person.getPersonType())) {
                    managerCount++; // 管理员
                }
            }

            result.put("worker", workerCount);
            result.put("manager", managerCount);

            logger.info("人员分类统计完成 - 总身份证数: {}, 查询到人员: {}, 工人: {}, 管理员: {}",
                    idCards.size(), persons.size(), workerCount, managerCount);

        } catch (Exception e) {
            logger.error("人员分类统计失败", e);
        }

        return result;
    }

    /**
     * 获取当前处理的人员类型
     * 
     * @param attendances 考勤记录列表
     * @return 人员类型 ("0"=工人, "1"=管理员)
     */
    private String getCurrentPersonType(List<SwmDailyAttendance> attendances) {
        if (attendances.isEmpty()) {
            return "0"; // 默认工人
        }
        // 从第一条记录获取人员类型（因为已经按类型分组了）
        return attendances.get(0).getPersonType();
    }

    /**
     * 获取空的考勤大屏数据结构
     * 当查询条件未匹配到人员时返回，保持数据结构完整性
     * 
     * @return 包含空数据的完整数据结构
     * @author Shawn
     * @date 2025/01/15
     */
    private Map<String, Object> getEmptyAttendanceDashboard() {
        Map<String, Object> result = new HashMap<>();

        // 获取当前月份
        Date today = new Date();
        String currentMonth = DateUtil.format(today, "yyyy-MM");

        // 获取当前月份的所有日期
        List<String> daysInMonth = getDaysInMonth(currentMonth);
        int dayCount = daysInMonth.size();

        // 1. 今日考勤统计 - 工人和管理员都为空
        Map<String, Object> todayAttendance = new HashMap<>();
        Map<String, Object> emptyStats = new HashMap<>();
        emptyStats.put("totalCount", 0);
        emptyStats.put("presentCount", 0);
        emptyStats.put("attendanceRate", BigDecimal.ZERO);
        emptyStats.put("onSiteCount", 0);
        emptyStats.put("workingCount", 0);

        todayAttendance.put("worker", new HashMap<>(emptyStats));
        todayAttendance.put("manager", new HashMap<>(emptyStats));
        result.put("todayAttendance", todayAttendance);

        // 2. 本月平均功效统计
        Map<String, Object> monthlyEfficiency = new HashMap<>();
        monthlyEfficiency.put("avgEfficiency", BigDecimal.ZERO);
        monthlyEfficiency.put("achievementRate", BigDecimal.ZERO);
        result.put("monthlyEfficiency", monthlyEfficiency);

        // 3. 本月每日出勤图表数据 - 生成完整的日期和0值数据
        Map<String, Object> monthlyAttendanceChart = new HashMap<>();
        monthlyAttendanceChart.put("dates", daysInMonth);
        monthlyAttendanceChart.put("scheduled", createZeroArray(dayCount));
        monthlyAttendanceChart.put("actual", createZeroArray(dayCount));
        monthlyAttendanceChart.put("rate", createZeroBigDecimalArray(dayCount));
        result.put("monthlyAttendanceChart", monthlyAttendanceChart);

        // 4. 本月每日功效图表数据 - 生成完整的日期和0值数据
        Map<String, Object> monthlyEfficiencyChart = new HashMap<>();
        monthlyEfficiencyChart.put("dates", daysInMonth);
        monthlyEfficiencyChart.put("scheduledHours", createZeroBigDecimalArray(dayCount));
        monthlyEfficiencyChart.put("actualHours", createZeroBigDecimalArray(dayCount));
        monthlyEfficiencyChart.put("rate", createZeroBigDecimalArray(dayCount));
        result.put("monthlyEfficiencyChart", monthlyEfficiencyChart);

        // 5. 排名和分布数据
        result.put("todayTeamRanking", new ArrayList<>());
        result.put("todayJobDistribution", new ArrayList<>());
        result.put("monthlyTeamRanking", new ArrayList<>());

        return result;
    }

    /**
     * 创建指定长度的整数0值数组
     * 
     * @param size 数组长度
     * @return 包含指定数量0值的整数数组
     */
    private List<Integer> createZeroArray(int size) {
        List<Integer> zeroArray = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            zeroArray.add(0);
        }
        return zeroArray;
    }

    /**
     * 创建指定长度的BigDecimal 0值数组
     * 
     * @param size 数组长度
     * @return 包含指定数量BigDecimal.ZERO值的数组
     */
    private List<BigDecimal> createZeroBigDecimalArray(int size) {
        List<BigDecimal> zeroArray = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            zeroArray.add(BigDecimal.ZERO);
        }
        return zeroArray;
    }

}
