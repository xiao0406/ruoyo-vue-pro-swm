package com.jeesite.modules.service;


import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONNull;
import cn.hutool.json.JSONObject;
import com.alibaba.cloud.commons.lang.StringUtils;
import com.jeesite.common.entity.Page;
import com.jeesite.common.lang.ObjectUtils;
import com.jeesite.modules.swm.dao.SwmWarningManagementDao;
import com.jeesite.modules.swm.entity.SwmDailyAttendance;
import com.jeesite.modules.swm.entity.SwmHelmetDevice;
import com.jeesite.modules.swm.entity.SwmPerson;
import com.jeesite.modules.swm.service.*;
import com.jeesite.modules.cache.service.RedisService;
import com.jeesite.modules.constant.SwmRedisConstant;
import com.jeesite.modules.sys.utils.CorpUtils;
import com.jeesite.modules.sys.utils.DictUtils;
import com.jeesite.modules.utils.R;
import com.jeesite.modules.entity.AiDto;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
public class AiServiceImpl {

    @Autowired
    private SwmDailyAttendanceService swmDailyAttendanceService;

    @Autowired
    private TDengineService tdengineService;
    @Autowired
    private SwmPersonService swmPersonService;
    @Value("${tdengine.dbname}")
    private String dbname;

    @Qualifier("swmExecutor")
    @Autowired
    private ThreadPoolTaskExecutor swmExecutor;
    @Autowired
    @Lazy
    private SwmAreaService swmAreaService;
    @Autowired
    private SwmHelmetDeviceService swmHelmetDeviceService;
    @Autowired
    private RedisService redisService;
    @Autowired
    private SwmWarningManagementDao swmWarningManagementDao;


    /**
     * 工效统计
     * 白班应到、白班实到、白班出勤率、白班有效作业时长，
     * 夜班应到、夜班实到、夜班出勤率、夜班有效作业时长，
     * 全天应到、全天实到、全天出勤率、全天有效作业时长
     *
     * @return
     */
    public Map<String, Object> workEfficiencyTask(String startDate, String endDate) {
        Map<String, Object> result = new HashMap<>();

        DateTime yesterday = DateUtil.yesterday();
        if (ObjectUtils.isEmpty(startDate)) {
            startDate = DateUtil.format(DateUtil.beginOfDay(yesterday), "yyyy-MM-dd");
        }
        if (ObjectUtils.isEmpty(endDate)) {
            endDate = DateUtil.format(DateUtil.endOfDay(yesterday), "yyyy-MM-dd");
        }

        log.info("【工效统计】开始：{} -> {}", startDate, endDate);

        List<SwmDailyAttendance> list = swmDailyAttendanceService.findAllList(startDate, endDate);
        if (CollectionUtils.isEmpty(list)) {
            return result;
        }

        List<AiDto.WorkEfficiencyDto> resultList = new ArrayList<>();

        // ========= 按班次分组 =========
        Map<String, List<SwmDailyAttendance>> shiftMap = list.stream().collect(Collectors.groupingBy(SwmDailyAttendance::getClasses));

        // ========= 统计维度：班组 =========
        handleDimensionStatistic("班组", shiftMap, list.stream().filter(x -> StringUtils.isNotBlank(x.getTeamName())).collect(Collectors.groupingBy(SwmDailyAttendance::getTeamName)), resultList);

        // ========= 统计维度：车间 =========
        handleDimensionStatistic("车间", shiftMap, list.stream().filter(x -> StringUtils.isNotBlank(x.getDepartmentName())).collect(Collectors.groupingBy(SwmDailyAttendance::getDepartmentName)), resultList);


        // ========= 统计维度：全体工人 =========
        Map<String, List<SwmDailyAttendance>> allMap = new HashMap<>();
        allMap.put("全体工人", list);
        handleDimensionStatistic("全体工人", shiftMap, allMap, resultList);

        result.put("workEfficiency", resultList);
        return result;
    }

    private void handleDimensionStatistic(String type, Map<String, List<SwmDailyAttendance>> shiftMap, Map<String, List<SwmDailyAttendance>> groupMap, List<AiDto.WorkEfficiencyDto> resultList) {

        for (Map.Entry<String, List<SwmDailyAttendance>> entry : groupMap.entrySet()) {

            String name = entry.getKey();

            // 白班
            List<SwmDailyAttendance> dayList = new ArrayList<>();
            List<SwmDailyAttendance> daySource = shiftMap.get("1");
            if (daySource != null) {
                for (SwmDailyAttendance item : daySource) {
                    if (nameMatch(item, name, type)) {
                        dayList.add(item);
                    }
                }
            }

            // 夜班
            List<SwmDailyAttendance> nightList = new ArrayList<>();
            List<SwmDailyAttendance> nightSource = shiftMap.get("3");
            if (nightSource != null) {
                for (SwmDailyAttendance item : nightSource) {
                    if (nameMatch(item, name, type)) {
                        nightList.add(item);
                    }
                }
            }

            // 全天 = 白班 + 夜班
            List<SwmDailyAttendance> fullList = new ArrayList<>();
            fullList.addAll(dayList);
            fullList.addAll(nightList);

            AiDto.WorkEfficiencyDto dto = new AiDto.WorkEfficiencyDto();
            dto.setType(type);
            dto.setName(name);

            dto.setDayShift(getAttendanceData(dayList));
            dto.setNightShift(getAttendanceData(nightList));
            dto.setAllDayShift(getAttendanceData(fullList));

            resultList.add(dto);
        }
    }

    private boolean nameMatch(SwmDailyAttendance a, String name, String type) {

        switch (type) {
            case "班组":
                return name.equals(a.getTeamName());
            case "车间":
                return name.equals(a.getDepartmentName());
            case "全体工人":
                return true;
            default:
                return false;
        }
    }


    private AiDto.AttendanceData getAttendanceData(List<SwmDailyAttendance> list) {

        AiDto.AttendanceData data = new AiDto.AttendanceData();

        int shouldArrive = list.size();
        int actualArrive = 0;
        BigDecimal hours = BigDecimal.ZERO;

        for (SwmDailyAttendance a : list) {
            if (a.getClockInDate() != null || a.getClockOutDate() != null) {
                actualArrive++;
            }
            if (a.getActualHours() != null) {
                hours = hours.add(a.getActualHours());
            }
        }

        data.setShouldArrive(shouldArrive);
        data.setActualArrive(actualArrive);
        data.setEffectiveWorkingHours(hours);

        if (shouldArrive == 0) {
            data.setAttendanceRate(BigDecimal.ZERO);
        } else {
            data.setAttendanceRate(new BigDecimal(actualArrive).divide(new BigDecimal(shouldArrive), 2, RoundingMode.HALF_UP));
        }

        return data;
    }

    public Page<AiDto.WorkerFatigue> workerFatigue(AiDto.WorkerFatigue vo) {
        Page<AiDto.WorkerFatigue> page = vo.getPage();
        vo.setActualHours(BigDecimal.valueOf(9));
        DateTime yesterday = DateUtil.yesterday();
        if (ObjectUtils.isEmpty(vo.getStartDate())) {
            vo.setStartDate(DateUtil.format(DateUtil.beginOfDay(yesterday), "yyyy-MM-dd"));
        }
        if (ObjectUtils.isEmpty(vo.getEndDate())) {
            vo.setEndDate(DateUtil.format(DateUtil.endOfDay(yesterday), "yyyy-MM-dd"));
        }
        if (vo.getPageNo() == null || vo.getPageNo() < 1) {
            vo.setPageNo(1);
        }
        if (vo.getPageSize() == null || vo.getPageSize() < 1) {
            vo.setPageSize(10);
        }
        List<AiDto.WorkerFatigue> list = swmDailyAttendanceService.workerFatigue(vo);
        page.setList(list);
        return page;
    }

    public Page<AiDto.RiskStatistics> riskStatistics(AiDto.RiskStatistics vo) {

        Page<AiDto.RiskStatistics> page = vo.getPage();

        // ====== 日期处理 ======
        DateTime yesterday = DateUtil.yesterday();
        if (ObjectUtils.isEmpty(vo.getStartDate())) {
            vo.setStartDate(DateUtil.format(DateUtil.beginOfDay(yesterday), "yyyy-MM-dd HH:mm:ss"));
        }
        if (ObjectUtils.isEmpty(vo.getEndDate())) {
            vo.setEndDate(DateUtil.format(DateUtil.endOfDay(yesterday), "yyyy-MM-dd HH:mm:ss"));
        }
        String startDate = vo.getStartDate();
        String endDate = vo.getEndDate();

        // ====== 字典读取 ======
        Map<String, String> alarmDict = getAlarmDict();
        // ====== 超级表 SQL（一次查询所有人）======
        String sql = "SELECT person_name, warning_content, COUNT(1) AS cnt " + "FROM " + dbname + ".swm_warning_management " + "WHERE create_date >= '" + startDate + "' " + "AND create_date <= '" + endDate + "' " + "AND person_name != '未知' " + "GROUP BY person_name, warning_content";

        R<JSONObject> result = tdengineService.executeTDengineSQL(sql);
        if (result.getCode() != R.SUCCESS || result.getData() == null) {
            page.setList(Collections.emptyList());
            page.setCount(0);
            return page;
        }

        JSONArray dataArray = result.getData().getJSONArray("data");
        Map<String, AiDto.RiskStatistics> map = new HashMap<>();

        // ====== 聚合每个人的报警数据 ======
        if (dataArray != null) {
            for (int i = 0; i < dataArray.size(); i++) {
                JSONArray row = dataArray.getJSONArray(i);

                String personName = getStringSafe(row.get(0));
                String content = getStringSafe(row.get(1));
                int cnt = getIntSafe(row.get(2));

                if (StringUtils.isBlank(personName)) {
                    continue;
                }

                AiDto.RiskStatistics dto = map.computeIfAbsent(personName, k -> new AiDto.RiskStatistics());
                dto.setEmployeeName(personName);

                if (alarmDict.get("staticAlarm").equals(content)) dto.setStaticAlarm(cnt);
                else if (alarmDict.get("unsealAlarm").equals(content)) dto.setUnsealAlarm(cnt);
                else if (alarmDict.get("emergencyCall").equals(content)) dto.setEmergencyCall(cnt);
                else if (alarmDict.get("fallAlarm").equals(content)) dto.setFallAlarm(cnt);
                else if (alarmDict.get("dangerousSourceEntry").equals(content)) dto.setDangerousSourceEntry(cnt);

                // 统计总报警数
                dto.setTotalAlarm(dto.getTotalAlarm() + cnt);
            }
        }

        // ====== 转 list 并排序 ======
        List<AiDto.RiskStatistics> resultList = new ArrayList<>(map.values());
        resultList.sort((o1, o2) -> o2.getTotalAlarm() - o1.getTotalAlarm());

        // ====== 分页处理 ======
        int start = (page.getPageNo() - 1) * page.getPageSize();
        int end = Math.min(start + page.getPageSize(), resultList.size());

        if (start >= resultList.size()) {
            page.setList(Collections.emptyList());
        } else {
            page.setList(resultList.subList(start, end));
        }
        page.setCount(resultList.size());

        return page;
    }


    private String getStringSafe(Object obj) {
        if (obj == null || obj instanceof JSONNull) {
            return null;
        }
        return String.valueOf(obj);
    }

    private Integer getIntSafe(Object obj) {
        if (obj == null || obj instanceof JSONNull) {
            return 0;
        }
        return Integer.parseInt(obj.toString());
    }


    public Page<AiDto.RiskStatistics> riskStatisticsDate(AiDto.RiskStatistics vo) {

        // ====== 分页处理 ======
        if (vo.getPageNo() == null || vo.getPageNo() < 1) vo.setPageNo(1);
        if (vo.getPageSize() == null || vo.getPageSize() < 1) vo.setPageSize(100);
        Page<AiDto.RiskStatistics> page = vo.getPage();

        // ====== 日期处理 ======
        DateTime yesterday = DateUtil.yesterday();
        if (ObjectUtils.isEmpty(vo.getStartDate())) {
            vo.setStartDate(DateUtil.format(DateUtil.beginOfDay(yesterday), "yyyy-MM-dd HH:mm:ss"));
        }
        if (ObjectUtils.isEmpty(vo.getEndDate())) {
            vo.setEndDate(DateUtil.format(DateUtil.endOfDay(yesterday), "yyyy-MM-dd HH:mm:ss"));
        }
        String startDate = vo.getStartDate();
        String endDate = vo.getEndDate();

        // ====== 字典读取 ======
        Map<String, String> alarmDict = getAlarmDict();

        // ====== 超级表 SQL（一次查询所有人）======
        String sql = "SELECT " + "    TO_CHAR(create_date, 'HH24') AS hour, " + "    warning_content, " + "    COUNT(1) AS total " + "FROM " + dbname + ".swm_warning_management " + "WHERE create_date >= '" + startDate + "' " + "AND create_date <= '" + endDate + "' " + "GROUP BY TO_CHAR(create_date, 'HH24'), warning_content " + "ORDER BY hour, warning_content DESC";

        R<JSONObject> result = tdengineService.executeTDengineSQL(sql);
        if (result.getCode() != R.SUCCESS || result.getData() == null) {
            page.setList(Collections.emptyList());
            page.setCount(0);
            return page;
        }

        JSONArray dataArray = result.getData().getJSONArray("data");
        if (dataArray == null || dataArray.isEmpty()) {
            page.setList(Collections.emptyList());
            page.setCount(0);
            return page;
        }

        // key = hour, value = AiDto.RiskStatistics
        Map<String, AiDto.RiskStatistics> map = new LinkedHashMap<>();

        for (int i = 0; i < dataArray.size(); i++) {
            JSONArray item = dataArray.getJSONArray(i);

            String hour = String.valueOf(item.get(0));
            String warningContent = String.valueOf(item.get(1));
            int cnt = Integer.parseInt(item.get(2).toString());

            AiDto.RiskStatistics dto = map.computeIfAbsent(hour, h -> {
                AiDto.RiskStatistics obj = new AiDto.RiskStatistics();
                int hInt = Integer.parseInt(h); // 当前小时
                int next = hInt + 1;            // 下一小时

                // 格式化为两位数：00~01、01~02 ... 23~24
                String hourRange = String.format("%02d-%02d", hInt, next);

                obj.setHours(hourRange);
                return obj;
            });

            if (alarmDict.get("staticAlarm").equals(warningContent)) dto.setStaticAlarm(cnt);
            else if (alarmDict.get("unsealAlarm").equals(warningContent)) dto.setUnsealAlarm(cnt);
            else if (alarmDict.get("emergencyCall").equals(warningContent)) dto.setEmergencyCall(cnt);
            else if (alarmDict.get("fallAlarm").equals(warningContent)) dto.setFallAlarm(cnt);
            else if (alarmDict.get("dangerousSourceEntry").equals(warningContent)) dto.setDangerousSourceEntry(cnt);

            dto.setTotalAlarm(dto.getTotalAlarm() + cnt);
        }

        // ====== 分页处理 ======
        List<AiDto.RiskStatistics> allList = new ArrayList<>(map.values());
        int total = allList.size();
        int fromIndex = Math.min((vo.getPageNo() - 1) * vo.getPageSize(), total);
        int toIndex = Math.min(fromIndex + vo.getPageSize(), total);
        List<AiDto.RiskStatistics> pageList = allList.subList(fromIndex, toIndex);

        page.setList(pageList);
        page.setCount(total);

        return page;
    }

    public Page<AiDto.RiskStatistics> riskStatisticsArea(AiDto.RiskStatistics vo) {

        Page<AiDto.RiskStatistics> page = vo.getPage();

        // ====== 日期处理 ======
        fillDefaultDate(vo);
        String startDate = vo.getStartDate();
        String endDate = vo.getEndDate();

        // ====== 字典读取 ======
        Map<String, String> alarmDict = getAlarmDict();

        // ====== 超级表 SQL（一次查询所有区域）======
        String sql = "SELECT area, warning_content, COUNT(1) AS cnt " + "FROM " + dbname + ".swm_warning_management " + "WHERE create_date >= '" + startDate + "' " + "AND create_date <= '" + endDate + "' " + "AND area IS NOT NULL " + "GROUP BY area, warning_content";

        R<JSONObject> result = tdengineService.executeTDengineSQL(sql);
        if (result.getCode() != R.SUCCESS || result.getData() == null) {
            page.setList(Collections.emptyList());
            page.setCount(0);
            return page;
        }

        JSONArray dataArray = result.getData().getJSONArray("data");
        Map<String, AiDto.RiskStatistics> map = new HashMap<>();

        // ====== 聚合每个区域的报警数据 ======
        if (dataArray != null) {
            for (int i = 0; i < dataArray.size(); i++) {
                JSONArray row = dataArray.getJSONArray(i);

                String area = getStringSafe(row.get(0));
                String content = getStringSafe(row.get(1));
                int cnt = getIntSafe(row.get(2));

                if (StringUtils.isBlank(area)) {
                    continue;
                }

                AiDto.RiskStatistics dto = map.computeIfAbsent(area, k -> new AiDto.RiskStatistics());
                dto.setArea(area);

                if (alarmDict.get("staticAlarm").equals(content)) dto.setStaticAlarm(cnt);
                else if (alarmDict.get("unsealAlarm").equals(content)) dto.setUnsealAlarm(cnt);
                else if (alarmDict.get("emergencyCall").equals(content)) dto.setEmergencyCall(cnt);
                else if (alarmDict.get("fallAlarm").equals(content)) dto.setFallAlarm(cnt);
                else if (alarmDict.get("dangerousSourceEntry").equals(content)) dto.setDangerousSourceEntry(cnt);

                // 统计总报警数
                dto.setTotalAlarm(dto.getTotalAlarm() + cnt);
            }
        }

        // ====== 转 list 并排序 ======
        List<AiDto.RiskStatistics> resultList = new ArrayList<>(map.values());
        resultList.sort((o1, o2) -> o2.getTotalAlarm() - o1.getTotalAlarm());

        // ====== 分页处理 ======
        int start = (page.getPageNo() - 1) * page.getPageSize();
        int end = Math.min(start + page.getPageSize(), resultList.size());

        if (start >= resultList.size()) {
            page.setList(Collections.emptyList());
        } else {
            page.setList(resultList.subList(start, end));
        }
        page.setCount(resultList.size());

        return page;
    }

    public Page<AiDto.RiskStatisticsAreaDate> riskStatisticsAreaDate(AiDto.RiskStatisticsAreaDate vo) {

        if (vo.getPageNo() == null || vo.getPageNo() < 1) vo.setPageNo(1);
        if (vo.getPageSize() == null || vo.getPageSize() < 1) vo.setPageSize(100);
        Page<AiDto.RiskStatisticsAreaDate> page = new Page<>();

        DateTime yesterday = DateUtil.yesterday();
        if (ObjectUtils.isEmpty(vo.getStartDate())) {
            vo.setStartDate(DateUtil.format(DateUtil.beginOfDay(yesterday), "yyyy-MM-dd HH:mm:ss"));
        }
        if (ObjectUtils.isEmpty(vo.getEndDate())) {
            vo.setEndDate(DateUtil.format(DateUtil.endOfDay(yesterday), "yyyy-MM-dd HH:mm:ss"));
        }
        String startDate = vo.getStartDate();
        String endDate = vo.getEndDate();

        // ===== SQL：按 hour + area 聚合 =====
        String sql = "SELECT TO_CHAR(create_date, 'HH24') AS hour, area, COUNT(1) AS cnt " +
                "FROM " + dbname + ".swm_warning_management " +
                "WHERE create_date >= '" + startDate + "' " +
                "AND create_date <= '" + endDate + "' " +
                "AND area IS NOT NULL " +
                "GROUP BY TO_CHAR(create_date, 'HH24'), area " +
                "ORDER BY TO_CHAR(create_date, 'HH24'), area";

        R<JSONObject> result = tdengineService.executeTDengineSQL(sql);
        if (result.getCode() != R.SUCCESS || result.getData() == null) {
            page.setList(Collections.emptyList());
            page.setCount(0);
            return page;
        }

        JSONArray dataArray = result.getData().getJSONArray("data");
        if (dataArray == null || dataArray.isEmpty()) {
            page.setList(Collections.emptyList());
            page.setCount(0);
            return page;
        }

        // ============================
        // 【关键结构】
        // hourRange -> List<AiDto.RiskStatistics>
        // ============================
        Map<String, List<AiDto.RiskStatisticsAreaDate>> hourGroupMap = new LinkedHashMap<>();

        for (int i = 0; i < dataArray.size(); i++) {
            JSONArray row = dataArray.getJSONArray(i);

            String hourStr = getStringSafe(row.get(0));   // 00,01,02
            String area = getStringSafe(row.get(1));      // 区域
            int cnt = getIntSafe(row.get(2));             // 数量

            if (StringUtils.isBlank(hourStr) || StringUtils.isBlank(area)) {
                continue;
            }

            int hInt = Integer.parseInt(hourStr);
            String hourRange = String.format("%02d-%02d", hInt, hInt + 1);

            // 获取 list，没有就创建
            List<AiDto.RiskStatisticsAreaDate> areaList =
                    hourGroupMap.computeIfAbsent(hourRange, k -> new ArrayList<>());

            AiDto.RiskStatisticsAreaDate dto = new AiDto.RiskStatisticsAreaDate();
            dto.setHours(hourRange);    // 00~01
            dto.setArea(area);          // 区域名称
            dto.setTotalAlarm(cnt);     // 数量

            areaList.add(dto);
        }

        // ============================
        // 组装最终 list
        // 每个小时一个 AiDto.RiskStatistics 对象
        // 内含 areaList
        // ============================
        List<AiDto.RiskStatisticsAreaDate> finalList = new ArrayList<>();

        for (Map.Entry<String, List<AiDto.RiskStatisticsAreaDate>> entry : hourGroupMap.entrySet()) {
            String hourRange = entry.getKey();
            List<AiDto.RiskStatisticsAreaDate> areaItems = entry.getValue();
            for (AiDto.RiskStatisticsAreaDate dto : areaItems) {
                dto.setHours(null);
            }

            AiDto.RiskStatisticsAreaDate parent = new AiDto.RiskStatisticsAreaDate();
            parent.setHours(hourRange);       // 如 00~01
            parent.setAreaList(areaItems);    // 子列表

            finalList.add(parent);
        }

        // 排序（按 00~01，01~02……）
        finalList.sort(
                Comparator.comparing(AiDto.RiskStatisticsAreaDate::getHours, Comparator.nullsFirst(String::compareTo))
        );


        // 分页
        int start = (page.getPageNo() - 1) * page.getPageSize();
        int end = Math.min(start + page.getPageSize(), finalList.size());

        if (start >= finalList.size()) {
            page.setList(Collections.emptyList());
        } else {
            page.setList(finalList.subList(start, end));
        }

        page.setCount(finalList.size());
        return page;
    }



    /**
     * 日期默认处理
     */
    private void fillDefaultDate(AiDto.RiskStatistics vo) {
        DateTime yesterday = DateUtil.yesterday();
        if (ObjectUtils.isEmpty(vo.getStartDate())) {
            vo.setStartDate(DateUtil.format(DateUtil.beginOfDay(yesterday), "yyyy-MM-dd HH:mm:ss"));
        }
        if (ObjectUtils.isEmpty(vo.getEndDate())) {
            vo.setEndDate(DateUtil.format(DateUtil.endOfDay(yesterday), "yyyy-MM-dd HH:mm:ss"));
        }
    }

    /**
     * 公共方法：获取字典
     */
    private Map<String, String> getAlarmDict() {
        Map<String, String> dict = new HashMap<>();
        dict.put("staticAlarm", DictUtils.getDictLabel("warning_content_enum", "长时间静止报警", "长时间静止报警"));
        dict.put("unsealAlarm", DictUtils.getDictLabel("warning_content_enum", "脱帽报警", "脱帽报警"));
        dict.put("emergencyCall", DictUtils.getDictLabel("warning_content_enum", "应急呼叫", "应急呼叫"));
        dict.put("fallAlarm", DictUtils.getDictLabel("warning_content_enum", "跌落报警", "跌落报警"));
        dict.put("dangerousSourceEntry", DictUtils.getDictLabel("warning_content_enum", "危险区域闯入提示", "危险区域闯入提示"));
        return dict;
    }


    public Page<AiDto.Trajectory> trajectory(AiDto.Trajectory vo) {

        //查询人员信息
        List<AiDto.Trajectory> personList = swmPersonService.findPersonList();
        Map<String, AiDto.Trajectory> personMap = personList.stream().collect(Collectors.toMap(AiDto.Trajectory::getIdCard, Function.identity()));

        //查询区域信息
        List<AiDto.Trajectory> swmAreaList = swmAreaService.findAddressList();
        Map<String, String> areaMap = swmAreaList.stream().collect(Collectors.toMap(AiDto.Trajectory::getAddress, AiDto.Trajectory::getAreaName));


        // ====== 分页处理 ======
        if (vo.getPageNo() == null || vo.getPageNo() < 1) vo.setPageNo(1);
        if (vo.getPageSize() == null || vo.getPageSize() < 1) vo.setPageSize(10);
        Page<AiDto.Trajectory> page = vo.getPage();

        // ====== 日期处理 ======
        DateTime now = DateUtil.date();
        if (ObjectUtils.isEmpty(vo.getStartDate())) {
            vo.setStartDate(DateUtil.format(DateUtil.offsetSecond(now, -120), "yyyy-MM-dd HH:mm:ss"));
        }
        if (ObjectUtils.isEmpty(vo.getEndDate())) {
            vo.setEndDate(DateUtil.format(now, "yyyy-MM-dd HH:mm:ss"));
        }

        String startDate = vo.getStartDate();
        String endDate = vo.getEndDate();

        int offset = (page.getPageNo() - 1) * page.getPageSize();

        // ====== 第一步：分页查询时间段内所有 elder_id + id_card（从超级表）=====
        String idSql = "SELECT DISTINCT elder_id, id_card " + "FROM " + dbname + ".external_coordinate_data " + "WHERE time >= '" + startDate + "' " + "AND time <= '" + endDate + "' " + "AND id_card IS NOT NULL " + "ORDER BY elder_id, id_card " + "LIMIT " + page.getPageSize() + " OFFSET " + offset;

        R<JSONObject> idResult = tdengineService.executeTDengineSQL(idSql);
        if (idResult.getCode() != R.SUCCESS || idResult.getData() == null) {
            page.setList(Collections.emptyList());
            page.setCount(0);
            return page;
        }

        JSONArray idArray = idResult.getData().getJSONArray("data");
        if (idArray == null || idArray.isEmpty()) {
            page.setList(Collections.emptyList());
            page.setCount(0);
            return page;
        }

        List<CompletableFuture<AiDto.Trajectory>> futureList = new ArrayList<>();
        for (int i = 0; i < idArray.size(); i++) {

            JSONArray row = idArray.getJSONArray(i);
            String elderId = String.valueOf(row.get(0));
            String idCard = String.valueOf(row.get(1));

            CompletableFuture<AiDto.Trajectory> future = CompletableFuture.supplyAsync(() -> {

                String childTable = dbname + ".external_coordinate_data_" + elderId + "_" + idCard;

                String trackSql = "SELECT x, y, address, time " + "FROM " + childTable + " " + "WHERE time >= '" + vo.getStartDate() + "' " + "AND time <= '" + vo.getEndDate() + "' " + "ORDER BY time desc limit 1";

                System.out.println("trackSql:" + trackSql);
                R<JSONObject> trackResult = tdengineService.executeTDengineSQL(trackSql);
                JSONArray trackArray = trackResult.getData() != null ? trackResult.getData().getJSONArray("data") : new JSONArray();

                AiDto.Trajectory dto = new AiDto.Trajectory();
                dto.setDeviceId(elderId);
//                dto.setIdCard(idCard);

                // 设置人员信息
                AiDto.Trajectory person = personMap.get(idCard);
                if (person != null) {
                    dto.setEmployeeName(person.getEmployeeName());
                    dto.setPhoneNumber(person.getPhoneNumber());
                    dto.setDepartmentName(person.getDepartmentName());
                    dto.setTeamName(person.getTeamName());
                }

                List<AiDto.Trajectory> points = new ArrayList<>();

                if (trackArray != null) {
                    for (int j = 0; j < trackArray.size(); j++) {
                        JSONArray t = trackArray.getJSONArray(j);

                        AiDto.Trajectory p = new AiDto.Trajectory();
                        p.setX((Integer) t.get(0));
                        p.setY((Integer) t.get(1));
//                        p.setAddress((String) t.get(2));
                        p.setTime((String) t.get(3));
                        p.setAreaName(areaMap.get(t.get(2))); // 区域

                        points.add(p);
                    }
                }
                dto.setXyList(points);
                return dto;

            }, swmExecutor);

            futureList.add(future);
        }

        // ====== 等待所有异步任务完成 ======
        List<AiDto.Trajectory> list = futureList.stream().map(CompletableFuture::join).collect(Collectors.toList());

        // ====== 总数统计 ======
        String countSql = "SELECT COUNT(*) FROM (" + "   SELECT DISTINCT elder_id, id_card " + "   FROM " + dbname + ".external_coordinate_data " + "   WHERE time >= '" + startDate + "' " + "   AND time <= '" + endDate + "' " + ") t";

        R<JSONObject> countResult = tdengineService.executeTDengineSQL(countSql);

        int total = 0;
        if (countResult.getCode() == R.SUCCESS && countResult.getData() != null) {
            JSONArray arr = countResult.getData().getJSONArray("data");
            if (arr != null && !arr.isEmpty()) {
                total = (int) arr.getJSONArray(0).get(0);
            }
        }

        page.setList(list);
        page.setCount(total);

        return page;
    }

    public List<AiDto.TeamActualHours> teamActualHours(AiDto.TeamActualHours vo) {
        List<AiDto.TeamActualHours> resultList = new ArrayList<>();

        DateTime yesterday = DateUtil.yesterday();
        if (ObjectUtils.isEmpty(vo.getStartDate())) {
            vo.setStartDate(DateUtil.format(DateUtil.beginOfDay(yesterday), "yyyy-MM-dd HH:mm:ss"));
        }
        if (ObjectUtils.isEmpty(vo.getEndDate())) {
            vo.setEndDate(DateUtil.format(DateUtil.endOfDay(yesterday), "yyyy-MM-dd HH:mm:ss"));
        }

        // 1. 获取所有的人员打卡信息
        List<SwmDailyAttendance> teamList = swmDailyAttendanceService.findAllList(vo.getStartDate(), vo.getEndDate());

        // 2. 分组，teamName 为 null 的记录归为 "未知班组"
        Map<String, List<SwmDailyAttendance>> teamMap = teamList.stream()
                .collect(Collectors.groupingBy(a -> Optional.ofNullable(a.getTeamName()).orElse("未知班组")));

        // 3. 统计每个班组的有效时长
        for (Map.Entry<String, List<SwmDailyAttendance>> entry : teamMap.entrySet()) {
            String teamName = entry.getKey();
            List<SwmDailyAttendance> value = entry.getValue();

            BigDecimal totalActualHours = value.stream()
                    .map(SwmDailyAttendance::getActualHours)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            AiDto.TeamActualHours teamActualHours = new AiDto.TeamActualHours();
            teamActualHours.setTeamName(teamName);
            teamActualHours.setActualHours(totalActualHours);
            resultList.add(teamActualHours);
        }

        return resultList;
    }

    public Page<AiDto.Trajectory> trajectoryV1(AiDto.Trajectory vo) {
        //查询人员信息
        List<AiDto.Trajectory> personList = swmPersonService.findPersonList();
        Map<String, AiDto.Trajectory> personMap = personList.stream().collect(Collectors.toMap(AiDto.Trajectory::getIdCard, Function.identity()));

        //查询区域信息
        List<AiDto.Trajectory> swmAreaList = swmAreaService.findAddressList();
        Map<String, String> areaMap = swmAreaList.stream().collect(Collectors.toMap(AiDto.Trajectory::getAddress, AiDto.Trajectory::getAreaName));


        // ====== 分页处理 ======
        if (vo.getPageNo() == null || vo.getPageNo() < 1) vo.setPageNo(1);
        if (vo.getPageSize() == null || vo.getPageSize() < 1) vo.setPageSize(10);
        Page<AiDto.Trajectory> page = vo.getPage();

        // ====== 日期处理 ======
        DateTime now = DateUtil.date();
        if (ObjectUtils.isEmpty(vo.getStartDate())) {
            vo.setStartDate(DateUtil.format(DateUtil.offsetSecond(now, -120), "yyyy-MM-dd HH:mm:ss"));
        }
        if (ObjectUtils.isEmpty(vo.getEndDate())) {
            vo.setEndDate(DateUtil.format(now, "yyyy-MM-dd HH:mm:ss"));
        }

        String startDate = vo.getStartDate();
        String endDate = vo.getEndDate();

        int offset = (page.getPageNo() - 1) * page.getPageSize();

        // ====== 第一步：分页查询时间段内所有 elder_id + id_card（从超级表）=====
        String idSql = "SELECT DISTINCT elder_id, id_card " + "FROM " + dbname + ".external_coordinate_data " + "WHERE time >= '" + startDate + "' " + "AND time <= '" + endDate + "' " + "AND id_card IS NOT NULL " + "ORDER BY elder_id, id_card " + "LIMIT " + page.getPageSize() + " OFFSET " + offset;

        R<JSONObject> idResult = tdengineService.executeTDengineSQL(idSql);
        if (idResult.getCode() != R.SUCCESS || idResult.getData() == null) {
            page.setList(Collections.emptyList());
            page.setCount(0);
            return page;
        }

        JSONArray idArray = idResult.getData().getJSONArray("data");
        if (idArray == null || idArray.isEmpty()) {
            page.setList(Collections.emptyList());
            page.setCount(0);
            return page;
        }

        List<CompletableFuture<AiDto.Trajectory>> futureList = new ArrayList<>();
        for (int i = 0; i < idArray.size(); i++) {

            JSONArray row = idArray.getJSONArray(i);
            String elderId = String.valueOf(row.get(0));
            String idCard = String.valueOf(row.get(1));

            CompletableFuture<AiDto.Trajectory> future = CompletableFuture.supplyAsync(() -> {

                String childTable = dbname + ".external_coordinate_data_" + elderId + "_" + idCard;

                String trackSql = "SELECT x, y, address, time " + "FROM " + childTable + " " + "WHERE time >= '" + vo.getStartDate() + "' " + "AND time <= '" + vo.getEndDate() + "' " + "ORDER BY time desc limit 1";

                System.out.println("trackSql:" + trackSql);
                R<JSONObject> trackResult = tdengineService.executeTDengineSQL(trackSql);
                JSONArray trackArray = trackResult.getData() != null ? trackResult.getData().getJSONArray("data") : new JSONArray();

                AiDto.Trajectory dto = new AiDto.Trajectory();
                dto.setId(elderId);

                // 设置人员信息
                AiDto.Trajectory person = personMap.get(idCard);
                if (person != null) {
                    dto.setName(person.getEmployeeName());
                    dto.setGroup(person.getDepartmentName());
                    dto.setTeam(person.getTeamName());
                }


                if (trackArray != null) {
                    JSONArray object = trackArray.getJSONArray(trackArray.size() - 1);
                    dto.setX((Integer) object.get(0));
                    dto.setY((Integer) object.get(1));
                    dto.setAreaName(areaMap.get(object.get(2)));
                }
                return dto;

            }, swmExecutor);

            futureList.add(future);
        }

        // ====== 等待所有异步任务完成 ======
        List<AiDto.Trajectory> list = futureList.stream().map(CompletableFuture::join).collect(Collectors.toList());

        // ====== 总数统计 ======
        String countSql = "SELECT COUNT(*) FROM (" + "   SELECT DISTINCT elder_id, id_card " + "   FROM " + dbname + ".external_coordinate_data " + "   WHERE time >= '" + startDate + "' " + "   AND time <= '" + endDate + "' " + ") t";

        R<JSONObject> countResult = tdengineService.executeTDengineSQL(countSql);

        int total = 0;
        if (countResult.getCode() == R.SUCCESS && countResult.getData() != null) {
            JSONArray arr = countResult.getData().getJSONArray("data");
            if (arr != null && !arr.isEmpty()) {
                total = (int) arr.getJSONArray(0).get(0);
            }
        }

        page.setList(list);
        page.setCount(total);
        Map<String, Object> otherData = new HashMap<>();
        otherData.put("time", new Date());
        page.setOtherData(otherData);

        return page;
    }

    /**
     * 未注册人员：有设备在线但未在swm_person中注册的人员
     */
    public List<AiDto.UnregisteredPersonnel> unregisteredPersonnel(AiDto.UnregisteredPersonnel vo) {

        // 日期默认处理
        DateTime yesterday = DateUtil.yesterday();
        if (ObjectUtils.isEmpty(vo.getStartDate())) {
            vo.setStartDate(DateUtil.format(DateUtil.beginOfDay(yesterday), "yyyy-MM-dd HH:mm:ss"));
        }
        if (ObjectUtils.isEmpty(vo.getEndDate())) {
            vo.setEndDate(DateUtil.format(DateUtil.endOfDay(yesterday), "yyyy-MM-dd HH:mm:ss"));
        }

        // 1. 从Redis获取在线设备ID
        String corpCode = CorpUtils.getCurrentCorpCode();
        Set<Object> deviceIds = redisService.sGet(corpCode + SwmRedisConstant.RedisIotKey.ONLINE_DEVICES_KEY);
        if (deviceIds == null || deviceIds.isEmpty()) {
            return Collections.emptyList();
        }

        // 2. 获取设备→人员映射
        Set<String> allIdCards = new HashSet<>();
        for (Object deviceId : deviceIds) {
            String idCard = (String) redisService.hget(
                    SwmRedisConstant.RedisGlobalKey.DEVICE_PERSON_MAP, String.valueOf(deviceId));
            if (idCard != null && !idCard.isEmpty()) {
                allIdCards.add(idCard);
            }
        }
        if (allIdCards.isEmpty()) {
            return Collections.emptyList();
        }

        // 3. 查询已注册人员，取差集得到未注册人员
        List<SwmPerson> registeredPersons = swmPersonService.findByIdCards(new ArrayList<>(allIdCards));
        Set<String> registeredIdCards = registeredPersons.stream()
                .map(SwmPerson::getIdentityCard)
                .collect(Collectors.toSet());

        List<String> unregisteredIdCards = allIdCards.stream()
                .filter(idCard -> !registeredIdCards.contains(idCard))
                .collect(Collectors.toList());

        if (unregisteredIdCards.isEmpty()) {
            return Collections.emptyList();
        }

        // 4. 从Redis获取未注册人员的设备ID映射
        Map<String, String> idCardDeviceMap = new HashMap<>();
        for (Object deviceId : deviceIds) {
            String idCard = (String) redisService.hget(
                    SwmRedisConstant.RedisGlobalKey.DEVICE_PERSON_MAP, String.valueOf(deviceId));
            if (idCard != null && unregisteredIdCards.contains(idCard)) {
                idCardDeviceMap.put(idCard, String.valueOf(deviceId));
            }
        }

        // 5. 查询TDengine获取最后坐标
        String idCardList = unregisteredIdCards.stream()
                .map(id -> "'" + id + "'")
                .collect(Collectors.joining(","));

        String sql = "SELECT LAST_ROW(id_card), LAST_ROW(x), LAST_ROW(y), LAST_ROW(time), LAST_ROW(address) " +
                "FROM " + dbname + ".external_coordinate_data " +
                "WHERE id_card IN (" + idCardList + ") " +
                "AND time >= '" + vo.getStartDate() + "' AND time <= '" + vo.getEndDate() + "' " +
                "PARTITION BY id_card";

        R<JSONObject> result = tdengineService.executeTDengineSQL(sql);

        List<AiDto.UnregisteredPersonnel> resultList = new ArrayList<>();
        if (result.getCode() == R.SUCCESS && result.getData() != null) {
            JSONArray dataArray = result.getData().getJSONArray("data");
            if (dataArray != null) {
                // 查询区域映射
                List<AiDto.Trajectory> swmAreaList = swmAreaService.findAddressList();
                Map<String, String> areaMap = swmAreaList.stream()
                        .collect(Collectors.toMap(AiDto.Trajectory::getAddress, AiDto.Trajectory::getAreaName, (a, b) -> a));

                for (int i = 0; i < dataArray.size(); i++) {
                    JSONArray row = dataArray.getJSONArray(i);
                    AiDto.UnregisteredPersonnel dto = new AiDto.UnregisteredPersonnel();
                    String idCard = getStringSafe(row.get(0));
                    dto.setIdCard(idCard);
                    dto.setDeviceId(idCardDeviceMap.get(idCard));
                    dto.setTime(getStringSafe(row.get(3)));
                    String address = getStringSafe(row.get(4));
                    dto.setAreaName(address != null ? areaMap.get(address) : null);
                    resultList.add(dto);
                }
            }
        }
        return resultList;
    }

    /**
     * 设备异常：电量低于20%的设备列表
     */
    public List<AiDto.DeviceAnomaly> deviceAnomalies(AiDto.DeviceAnomaly vo) {

        // 从TDengine查询低电量设备
        List<Map<String, String>> lowBatteryDevices = swmHelmetDeviceService.findDeviceIdAndIdBatteryByBatteryLevel(20);
        if (lowBatteryDevices.isEmpty()) {
            return Collections.emptyList();
        }

        List<AiDto.DeviceAnomaly> resultList = new ArrayList<>();
        for (Map<String, String> m : lowBatteryDevices) {
            String deviceId = m.get("deviceId");
            Integer batteryLevel = Integer.parseInt(m.get("latestBattery"));

            AiDto.DeviceAnomaly dto = new AiDto.DeviceAnomaly();
            dto.setDeviceId(deviceId);
            dto.setBatteryLevel(batteryLevel);

            SwmHelmetDevice device = swmHelmetDeviceService.getByDeviceId(deviceId);
            if (device != null) {
                dto.setDeviceName(device.getDeviceId());
                dto.setAssignedPerson(device.getPersonName());
            }
            resultList.add(dto);
        }

        return resultList;
    }

    /**
     * 闭环追踪：按报警类型统计已排查工单数/报警工单总数
     */
    public List<AiDto.ClosedLoopTracking> closedLoopTracking(AiDto.ClosedLoopTracking vo) {

        // 日期默认处理
        DateTime yesterday = DateUtil.yesterday();
        if (ObjectUtils.isEmpty(vo.getStartDate())) {
            vo.setStartDate(DateUtil.format(DateUtil.beginOfDay(yesterday), "yyyy-MM-dd"));
        }
        if (ObjectUtils.isEmpty(vo.getEndDate())) {
            vo.setEndDate(DateUtil.format(DateUtil.endOfDay(yesterday), "yyyy-MM-dd"));
        }

        Map<String, String> alarmDict = getAlarmDict();
        List<String> alarmLabels = new ArrayList<>(alarmDict.values());

        // 1. 从TDengine查各报警类型总数
        String labelInClause = alarmLabels.stream()
                .map(l -> "'" + l + "'")
                .collect(Collectors.joining(","));

        String sql = "SELECT warning_content, COUNT(1) AS cnt " +
                "FROM " + dbname + ".swm_warning_management " +
                "WHERE create_date >= '" + vo.getStartDate() + "' " +
                "AND create_date <= '" + vo.getEndDate() + "' " +
                "AND warning_content IN (" + labelInClause + ") " +
                "GROUP BY warning_content";

        Map<String, Long> totalMap = new HashMap<>();
        R<JSONObject> result = tdengineService.executeTDengineSQL(sql);
        if (result.getCode() == R.SUCCESS && result.getData() != null) {
            JSONArray dataArray = result.getData().getJSONArray("data");
            if (dataArray != null) {
                for (int i = 0; i < dataArray.size(); i++) {
                    JSONArray row = dataArray.getJSONArray(i);
                    String content = getStringSafe(row.get(0));
                    long cnt = Long.parseLong(row.get(2).toString());
                    if (content != null) {
                        totalMap.put(content, cnt);
                    }
                }
            }
        }

        // 2. 从MySQL查各报警类型已处置数
        Date beginDate = DateUtil.beginOfDay(DateUtil.parse(vo.getStartDate(), "yyyy-MM-dd"));
        Date endDate = DateUtil.endOfDay(DateUtil.parse(vo.getEndDate(), "yyyy-MM-dd"));
        List<Map<String, Object>> handledList = swmWarningManagementDao.countHandledGroupByWarningContent(beginDate, endDate);
        Map<String, Long> handledMap = new HashMap<>();
        for (Map<String, Object> row : handledList) {
            String content = (String) row.get("warningContent");
            Long count = ((Number) row.get("count")).longValue();
            handledMap.put(content, count);
        }

        // 3. 合并计算处置率
        List<AiDto.ClosedLoopTracking> resultList = new ArrayList<>();
        for (Map.Entry<String, String> entry : alarmDict.entrySet()) {
            String label = entry.getValue();
            long total = totalMap.getOrDefault(label, 0L);
            long handled = handledMap.getOrDefault(label, 0L);

            AiDto.ClosedLoopTracking dto = new AiDto.ClosedLoopTracking();
            dto.setAlarmType(label);
            dto.setTotalCount(total);
            dto.setHandledCount(handled);
            dto.setHandleRate(total > 0
                    ? new BigDecimal(handled).divide(new BigDecimal(total), 2, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO);
            resultList.add(dto);
        }

        return resultList;
    }

}

