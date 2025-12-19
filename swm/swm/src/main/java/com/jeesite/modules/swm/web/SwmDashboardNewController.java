package com.jeesite.modules.swm.web;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.json.JSONObject;
import com.jeesite.common.entity.BaseEntity;
import com.jeesite.common.entity.Page;
import com.jeesite.common.lang.DateUtils;
import com.jeesite.common.mybatis.mapper.query.QueryType;
import com.jeesite.common.web.BaseController;
import com.jeesite.modules.cache.service.RedisService;
import com.jeesite.modules.swm.constant.SwmRedisConstant;
import com.jeesite.modules.swm.entity.*;
import com.jeesite.modules.swm.entity.dto.SwmDashboardDto;
import com.jeesite.modules.swm.service.*;
import com.jeesite.modules.utils.R;
import groovy.lang.Lazy;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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
import java.util.stream.Collectors;
import java.util.stream.Stream;


@Slf4j
@Controller
@RequestMapping(value = "${adminPath}/dashboard2")
@Api(value = "大屏数据看板接口", tags = "大屏数据看板接口")
public class SwmDashboardNewController extends BaseController {

    @Autowired
    private SwmPersonService swmPersonService;
    @Autowired
    private SwmDailyAttendanceService swmDailyAttendanceService;
    @Autowired
    private SwmWarningManagementService swmWarningManagementService;
    @Autowired
    private SwmHelmetDeviceService swmHelmetDeviceService;
    @Autowired
    private SwmOrganizationTreeService swmOrganizationTreeService;
    @Autowired
    private TDengineService tdengineService;
    @Value("${tdengine.dbname}")
    private String dbname;

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private RedisService redisService;

    @Qualifier("swmExecutor")
    @Autowired
    private ThreadPoolTaskExecutor swmExecutor;



    //******************************************************************************数据看板-人员分布******************************************************************//
    @GetMapping("/personnel")
    @ResponseBody
    @ApiOperation("人员分布看板数据")
    public Map<String, Object> todayData() {
        Map<String, Object> result = new HashMap<>();
        // 查询所有在职人员
        List<SwmPerson> swmPersonList = swmPersonService.findActivePersons();
        // Date date = new Date(2025 - 1900, 8, 20);
        Date date = new Date();
        // 查询日考勤数据
        List<SwmDailyAttendance> todayAttendances = swmDailyAttendanceService.findByDate(date);
        // 人员数据统计
        CompletableFuture<Map<String, Object>> todayAttendanceCount = CompletableFuture.supplyAsync(
                () -> getPersonCount(todayAttendances, swmPersonList),swmExecutor);
        // 异常数据统计
        CompletableFuture<Map<String, Object>> todayAbnormalCount = CompletableFuture.supplyAsync(
                () -> getAbnormalCount(swmPersonList),swmExecutor);
        // 实时作业人员变化趋势-每小时统计
        CompletableFuture<Map<String, Object>> hourWorkingCount = CompletableFuture.supplyAsync(
                this::getHourWorkingCount);

        // 等待所有任务完成
        CompletableFuture.allOf(todayAttendanceCount, todayAbnormalCount, hourWorkingCount).join();
        // 组装结果
        try {
            result.put("todayAttendance", todayAttendanceCount.get());
            result.put("todayAbnormalCount", todayAbnormalCount.get());
            result.put("hourWorkingCount", hourWorkingCount.get());
        } catch (Exception e) {
            logger.error("获取统计结果时出错", e);
            throw new RuntimeException("获取统计结果时出错", e);
        }
        return result;
    }

    private Map<String, Object> getHourWorkingCount() {
        Map<String, Object> result = new HashMap<>();
        List<String> todayHour = getTodayHour();
        List<Integer> countList = new ArrayList<>();

        LocalDate today = LocalDate.now(); // 获取当前日期
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        for (String hour : todayHour) {
            String startTime;
            String endTime;
            // 特殊处理 00:00 的情况
            if ("00:00".equals(hour)) {
                LocalDate yesterday = today.minusDays(1);
                // 生成前一天 23:00 的完整时间字符串
                startTime = yesterday.format(DateTimeFormatter.ISO_LOCAL_DATE) + " 23:00:00";
                endTime = today.format(DateTimeFormatter.ISO_LOCAL_DATE) + hour + ":00";
            } else {
                // 其他小时使用当天时间
                LocalDateTime hourStart = today.atTime(Integer.parseInt(hour.split(":")[0]) - 1, 0);
                startTime = hourStart.format(timeFormatter);
                LocalDateTime hourEnd = today.atTime(Integer.parseInt(hour.split(":")[0]), 0);
                endTime = hourEnd.format(timeFormatter);
            }
            log.info("getHourWorkingCount startTime | {} endTime | {}", startTime, endTime);
            Map<String, Integer> hourWorkingCount = getHourWorkingCountByTypeFromTDengine(startTime, endTime);
            Integer workingPersonCount = hourWorkingCount.getOrDefault("worker", 0);
            Integer workingManagerCount = hourWorkingCount.getOrDefault("manager", 0);
            countList.add(workingPersonCount + workingManagerCount);
        }
        result.put("y", countList);
        result.put("x", todayHour);
        return result;
    }

    private Map<String, Object> getAbnormalCount(List<SwmPerson> swmPersonList){
        Map<String, Object> result = new HashMap<>();
        // 统计今天异常记录数
        SwmWarningManagement swmWarningManagement = new SwmWarningManagement();
        // 处理时间范围查询条件
        String beginTime = DateUtils.getDate() + " 00:00:00";
        String endTime = DateUtils.getDate() + " 23:59:59";
        // 设置开始时间和结束时间条件
        swmWarningManagement.setBeginAlarmTime(DateUtils.parseDate(beginTime));
        swmWarningManagement.setEndAlarmTime(DateUtils.parseDate(endTime));


        swmWarningManagement.setMasterDataAlarm(true);
        List<SwmWarningManagement> swmWarningManagements = swmWarningManagementService.listFromTDEngine(swmWarningManagement);
        //今日报警总数
        result.put("swmWarningManagementCount", swmWarningManagements.size());

        // 五天未考勤人数
        Long abnormalAttendanceCount = countAbnormalAttendance(5);
        result.put("abnormalAttendanceCount", abnormalAttendanceCount);
        // 低电量人数
        Long lowBatteryCount = countLowBattery(swmPersonList);
        result.put("lowBatteryCount", lowBatteryCount);
        return result;
    }

    /**
     * 统计低电量人数
     * @param swmPersonList 人员列表
     * @return 低电量人数
     */
    private Long countLowBattery(List<SwmPerson> swmPersonList){
        // 查询电量低于20的设备
        List<String> deviceIdsByBatteryLevel = swmHelmetDeviceService.findDeviceIdsByBatteryLevel(20);
        return swmPersonList.stream().filter(swmPerson -> deviceIdsByBatteryLevel.contains(swmPerson.getSafetyHelmetId())).count();
    }


    /**
     * 统计连续N天有效未打卡的员工数量
     * @param days 连续未打卡天数阈值
     * @return 异常员工数量
     */
    private Long countAbnormalAttendance(int days) {
        // 查询从今天开始往前推30天的考勤记录
        LocalDate today = LocalDate.now();
        LocalDate thirtyDaysAgo = today.minusDays(30);
        Date startDate = Date.from(thirtyDaysAgo.atStartOfDay(ZoneId.systemDefault()).toInstant());
        List<SwmDailyAttendance> attendanceList = swmDailyAttendanceService.findByDateRange(startDate, new Date());
        // 生成最近N个工作日日期（含今天）
        List<LocalDate> targetDates = new ArrayList<>();
        for (int i = 0; i < days; i++) {
            targetDates.add(today.minusDays(i));
        }
        // 按员工分组考勤记录
        Map<String, List<SwmDailyAttendance>> recordsByEmployee = attendanceList.stream()
                .collect(Collectors.groupingBy(SwmDailyAttendance::getEmployeeId));
        // 统计符合条件的员工
        Set<String> abnormalEmployees = new HashSet<>();
        for (Map.Entry<String, List<SwmDailyAttendance>> entry : recordsByEmployee.entrySet()) {
            String empId = entry.getKey();
            List<SwmDailyAttendance> empRecords = entry.getValue();
            boolean allDaysAbsent = true;
            // 检查每个目标日期
            for (LocalDate date : targetDates) {
                Optional<SwmDailyAttendance> recordOptional = empRecords.stream()
                        .filter(record -> record.getAttendanceDate().equals(date))
                        .findFirst();
                // 情况1：该日期无考勤记录（未排班）
                if (!recordOptional.isPresent()) {
                    allDaysAbsent = false;
                    break;
                }
                // 情况2：有记录但非有效未打卡
                SwmDailyAttendance record = recordOptional.get();
                if (!record.isEffectiveAbsence()) {
                    allDaysAbsent = false;
                    break;
                }
            }
            // 所有日期均为有效未打卡
            if (allDaysAbsent) {
                abnormalEmployees.add(empId);
            }
        }
        return (long) abnormalEmployees.size();
    }

    /**
     * 获取今日考勤统计
     */
    private Map<String, Object> getPersonCount(List<SwmDailyAttendance> todayAttendances, List<SwmPerson> swmPersonList) {
        Map<String, Object> result = new HashMap<>();
        // 今日出勤人数
        long todayAttendanceCount = todayAttendances.stream().filter(a -> a.getClockInDate() != null).count();
        result.put("todayAttendanceCount", todayAttendanceCount);
        // 今日出勤工人数
        long todayAttendanceWorkerCount = todayAttendances.stream().filter(a -> (a.getClockInDate() != null || a.getClockOutDate() != null)
                && a.getPersonType().equals(SwmPerson.PersonTypeEnum.WORKER)).count();
        result.put("todayAttendanceWorkerCount", todayAttendanceWorkerCount);
        // 今日出勤管理员数
        long todayAttendanceManagerCount = todayAttendances.stream().filter(a -> (a.getClockInDate() != null || a.getClockOutDate() != null)
                && a.getPersonType().equals(SwmPerson.PersonTypeEnum.MANAGER)).count();
        result.put("todayAttendanceManagerCount", todayAttendanceManagerCount);

        //今日出勤白班人数   classes = 1
     long todayAttendanceWhiteCount = todayAttendances.stream().filter(a -> a.getClockInDate() != null
                && "1".equals(a.getClasses())).count();
        result.put("todayAttendanceWhiteCount", todayAttendanceWhiteCount);

        //今日出勤夜班人数   classes = 3
        long todayAttendanceNightCount = todayAttendances.stream().filter(a -> a.getClockInDate() != null
                && "3".equals(a.getClasses())).count();
        result.put("todayAttendanceNightCount", todayAttendanceNightCount);

        //工人今日在厂
        String[] managerIds = {SwmPerson.PersonTypeEnum.WORKER, SwmPerson.PersonTypeEnum.TEAMLEADER, SwmPerson.PersonTypeEnum.SPECIALTRADES};
        long todayAttendanceWorkerWhiteCount = swmPersonList.stream().filter(a ->  Arrays.asList(managerIds).contains(a.getPersonType())).count();
        result.put("todayAttendanceWorkerWhiteCount", todayAttendanceWorkerWhiteCount);

        //管理员今日在厂
        long todayAttendanceManagerWhiteCount = swmPersonList.stream().filter(a ->  SwmPerson.PersonTypeEnum.MANAGER.equals(a.getPersonType())).count();
        result.put("todayAttendanceManagerWhiteCount", todayAttendanceManagerWhiteCount);


        // 工作中人数：从TDengine查询1小时内有位置数据的人数（按类型分类）
        //这里改下逻辑，取最近5分钟的数据
        Map<String, Integer> workingStats = getWorkingCountByTypeFromTDengine();
        // 实时作业工人数
        Integer workingPersonCount = workingStats.getOrDefault("worker", 0);
        result.put("workingPersonCount", workingPersonCount);
        // 实时作业管理员数
        Integer workingManagerCount = workingStats.getOrDefault("manager", 0);
        result.put("workingManagerCount", workingManagerCount);
        // 实时作业人数
        // 获取SwmPersonController Bean
        result.put("totalWorkingCount", workingPersonCount);

        // 在场工人数
//        long workerCount = swmPersonList.stream().filter(a -> !a.getPersonType().equals(SwmPerson.PersonTypeEnum.MANAGER)).count();
        long workerCount = swmPersonList.size();
        result.put("workerCount", workerCount);
        // 在场管理员数
        long managerCount = swmPersonList.stream().filter(a -> a.getPersonType().equals(SwmPerson.PersonTypeEnum.MANAGER)).count();
        result.put("managerCount", managerCount);
        // 今日出勤率
        if(todayAttendanceCount == 0){
            result.put("todayAttendanceRate", "0.00");
        }else{
            String todayAttendanceRate = BigDecimal.valueOf(todayAttendanceCount).divide(BigDecimal.valueOf(workerCount + managerCount), 2, RoundingMode.HALF_UP).toString();
            result.put("todayAttendanceRate", todayAttendanceRate);
        }
        return result;
    }

    /**
     * 从TDengine查询指定时间内工作中人数（按人员类型分类）
     * 统计指定时间内有位置数据的唯一身份证数量，并按工人/管理员分类
     */
    private Map<String, Integer> getHourWorkingCountByTypeFromTDengine(String startTime, String endTime) {
        Map<String, Integer> result = new HashMap<>();
        result.put("worker", 0);
        result.put("manager", 0);
        try {
            // 从TDengine获取指定时间内的唯一身份证集合
            Set<String> uniqueIdCards = getUniqueIdCardsFromTDengine(startTime, endTime);

            if (!uniqueIdCards.isEmpty()) {
                // 通过身份证查询人员信息并按类型分类统计
                Map<String, Integer> typeStats = classifyPersonsByType(uniqueIdCards);
                result.put("worker", typeStats.get("worker"));
                result.put("manager", typeStats.get("manager"));

                logger.info("查询到指定时间内工作中人数 - 工人: {}, 管理员: {} (开始时间: {} | 结束时间: {} )",
                        typeStats.get("worker"), typeStats.get("manager"), startTime, endTime);
            } else {
                logger.warn("查询到指定时间内工作中人数失败或无数据");
            }
        } catch (Exception e) {
            logger.error("查询TDengine 指定时间内工作中人数分类统计失败", e);
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
//            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//            Calendar calendar = Calendar.getInstance();
//            calendar.add(Calendar.HOUR_OF_DAY, -1); // 减去1小时
//            String oneHourAgo = sdf.format(calendar.getTime());

//            Date now = new Date();
//            Date oneMinuteAgo = DateUtil.offsetMinute(now, -10);
//            String oneHourAgo = DateUtil.formatDateTime(oneMinuteAgo);
//
//            // 从TDengine获取1小时内的唯一身份证集合
//            Set<String> uniqueIdCards = getUniqueIdCardsFromTDengine(oneHourAgo, null);

            //身份证从redis里获取，先获取到所有的设备，然后在获取设备对应的身份证
            Set<Object> deviceIds = redisService.sGet(SwmRedisConstant.Device.ONLINE_DEVICES_KEY);
            Set<String> uniqueIdCards = new HashSet<>();
            if (deviceIds != null) {
                for (Object deviceId : deviceIds) {
                    String currentPerson = (String) redisService.hget(SwmRedisConstant.Helmet.DEVICE_PERSON_MAP, String.valueOf(deviceId));
                    uniqueIdCards.add(currentPerson);
                }
            }

            if (!uniqueIdCards.isEmpty()) {
                // 通过身份证查询人员信息并按类型分类统计
                Map<String, Integer> typeStats = classifyPersonsByType(uniqueIdCards);
                result.put("worker", typeStats.get("worker"));
                result.put("manager", typeStats.get("manager"));

//                logger.info("查询到1小时内工作中人数 - 工人: {}, 管理员: {} (1小时前时间: {})",
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
                logger.info("查询TDengine身份证SQL: {} (时间范围: {} 到 {})", sql, startTime, endTime);
            } else {
                // 单一时间点查询（1小时内数据）
                sql = String.format(
                        "SELECT DISTINCT id_card FROM %s.external_coordinate_data WHERE time >= '%s'",
                        dbname, startTime);
                logger.info("查询TDengine身份证SQL: {} (开始时间: {})", sql, startTime);
            }

            R<JSONObject> result = tdengineService.executeTDengineSQL(sql);

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
                if (SwmPerson.PersonTypeEnum.WORKER.equals(person.getPersonType())
                ||SwmPerson.PersonTypeEnum.TEAMLEADER.equals(person.getPersonType())
                ||SwmPerson.PersonTypeEnum.SPECIALTRADES.equals(person.getPersonType())) {
                    workerCount++; // 工人
                } else if (SwmPerson.PersonTypeEnum.MANAGER.equals(person.getPersonType())) {
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

    private List<String> getTodayHour(){
        // 获取当前时间
        LocalDateTime now = LocalDateTime.now();
        int currentHour = now.getHour(); // 当前小时（0-23）
        // 定义时间格式
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:00");
        // 生成从 00:00 到当前时间的小时列表
        List<String> hourList = new ArrayList<>();
        for (int hour = 6; hour <= currentHour; hour++) {
            String time = LocalTime.of(hour, 0).format(formatter);
            hourList.add(time);
        }
        return hourList;
    }
    //******************************************************************************数据看板-人员分布******************************************************************//

    //******************************************************************************数据看板-劳务管理******************************************************************//
    @GetMapping("/labor")
    @ResponseBody
    @ApiOperation("劳务管理看板数据")
    public Map<String, Object> labor() {
        Map<String, Object> result = new HashMap<>();
        // 查询所有在职人员
        List<SwmPerson> swmPersonList = swmPersonService.findActivePersons();
        // Date date = new Date(2025 - 1900, 8, 20);
        Date date = new Date();
        // 1. 查询日考勤数据
        List<SwmDailyAttendance> todayAttendances = swmDailyAttendanceService.findByDate(date);
        // 工厂人员工种类型分布
        CompletableFuture<List<JobTypeCount>> personJobTypeStatistics = CompletableFuture.supplyAsync(
                () -> swmDailyAttendanceService.statisticsPersonJobType(DateUtils.formatDate(date)),swmExecutor);
        // 进出场记录
        CompletableFuture<List<EntryAndExitRecord>> entryAndExitRecord = CompletableFuture.supplyAsync(
                () -> getEntryAndExitRecord(todayAttendances),swmExecutor);
        // 近七日考勤人数和考勤率分析
        CompletableFuture<Map<String, Object>> last7DaysAttendance = CompletableFuture.supplyAsync(
                () -> getLast7DaysAttendance(swmPersonList),swmExecutor);
        // 近十日出勤人数统计变化趋势
        CompletableFuture<Map<String, Object>> last10DaysAttendance = CompletableFuture.supplyAsync(
                this::getLast10DaysAttendance,swmExecutor);
        // 等待所有任务完成
        CompletableFuture.allOf(personJobTypeStatistics, entryAndExitRecord, last7DaysAttendance, last10DaysAttendance).join();
        // 组装结果
        try {
            result.put("jobTypeCount", personJobTypeStatistics.get());
            result.put("entryAndExitRecord", entryAndExitRecord.get());
            result.put("last7DaysAttendance", last7DaysAttendance.get());
            result.put("last10DaysAttendance", last10DaysAttendance.get());
        } catch (Exception e) {
            logger.error("获取统计结果时出错", e);
            throw new RuntimeException("获取统计结果时出错", e);
        }
        return result;
    }

    @GetMapping("/workshop/attendance/analysis")
    @ResponseBody
    @ApiOperation("车间考勤分析")
    public Map<String, Object> workshop(@RequestParam("companyCode") String companyCode, @RequestParam("companyName") String companyName) {
        Map<String, Object> result = new HashMap<>();
        // Date date = new Date(2025 - 1900, 8, 20);
        Date date = new Date();
        // 车间考勤分析
        List<AttendanceAnalysis> workshopAttendanceAnalysis = getWorkshopAttendanceAnalysis(companyCode, companyName, DateUtils.formatDate(date));
        result.put("workshopAttendanceAnalysis", workshopAttendanceAnalysis);
        return result;
    }

    @GetMapping("/team/attendance/analysis")
    @ResponseBody
    @ApiOperation("班组考勤分析")
    public Map<String, Object> team(@RequestParam("companyCode") String companyCode, @RequestParam("companyName") String companyName) {
        Map<String, Object> result = new HashMap<>();
        // Date date = new Date(2025 - 1900, 8, 20);
        Date date = new Date();
        // 班组考勤分析
        List<AttendanceAnalysis> teamAttendanceAnalysis = getTeamAttendanceAnalysis(companyCode, companyName, DateUtils.formatDate(date));
        result.put("teamAttendanceAnalysis", teamAttendanceAnalysis);
        return result;
    }


    @GetMapping("/team/attendance/analysisNew")
    @ResponseBody
    @ApiOperation("班组考勤分析")
    public Map<String, Object> teamNew(@RequestParam("companyCode") String companyCode, @RequestParam("companyName") String companyName) {
        Map<String, Object> result = new HashMap<>();
        // Date date = new Date(2025 - 1900, 8, 20);
        Date date = new Date();
        // 班组考勤分析
        List<AttendanceAnalysis> teamAttendanceAnalysis = getTeamAttendanceAnalysisNew(companyCode, companyName, DateUtils.formatDate(date));
        result.put("teamAttendanceAnalysis", teamAttendanceAnalysis);
        return result;
    }

    private List<AttendanceAnalysis> getWorkshopAttendanceAnalysis(String companyCode, String companyName, String date) {
        List<AttendanceAnalysis> result = new ArrayList<>();
        // 根据工厂查询车间数据
        List<TreeNode> workshopList = swmOrganizationTreeService.getNodes("office", companyCode,null);
        for (TreeNode treeNode : workshopList) {
            AttendanceAnalysis attendanceAnalysis = new AttendanceAnalysis();
            String workshopName = treeNode.getTitle();
            SwmPerson swmPerson = new SwmPerson();
            swmPerson.setCompany(companyName);
            swmPerson.setDepartment(workshopName);
            List<SwmPerson> list = swmPersonService.findList(swmPerson);

            attendanceAnalysis.setName(workshopName);
            attendanceAnalysis.setCount((long)list.size());

            Long attendanceCount = swmDailyAttendanceService.countAttendanceByWorkshop(companyName, workshopName, date);
            attendanceAnalysis.setAttendanceCount(attendanceCount);
            if(attendanceCount == 0 || list.isEmpty()){
                attendanceAnalysis.setAttendanceRate("0.00");
            }else{
                String attendanceRate = BigDecimal.valueOf(attendanceCount).divide(BigDecimal.valueOf(list.size()), 2, RoundingMode.HALF_UP).toString();
                attendanceAnalysis.setAttendanceRate(attendanceRate);
            }

            result.add(attendanceAnalysis);
        }
        return result;
    }

    private List<AttendanceAnalysis> getTeamAttendanceAnalysis(String companyCode, String companyName, String date) {
        List<AttendanceAnalysis> result = new ArrayList<>();
        // 根据工厂查询车间数据
        List<TreeNode> workshopList = swmOrganizationTreeService.getNodes("office", companyCode,null);
        for (TreeNode workshop : workshopList) {
            // 查询产线数据
            List<TreeNode> lineList = swmOrganizationTreeService.getNodes("workshop", workshop.getId(),null);
            for (TreeNode line : lineList) {
                // 查询班组数据
                List<TreeNode> teamList = swmOrganizationTreeService.getNodes("prodLine", line.getId(),null);
                for (TreeNode team : teamList) {
                    AttendanceAnalysis attendanceAnalysis = new AttendanceAnalysis();
                    String workshopName = workshop.getTitle();
                    String lineName = line.getTitle();
                    String teamName = team.getTitle();
                    SwmPerson swmPerson = new SwmPerson();
                    swmPerson.setCompany(companyName);
                    swmPerson.setDepartment(workshopName);
                    swmPerson.setProdLine(lineName);
                    swmPerson.setTeam(teamName);
                    List<SwmPerson> list = swmPersonService.findList(swmPerson);

                    attendanceAnalysis.setName(teamName);
                    attendanceAnalysis.setCount((long)list.size());

                    Long attendanceCount = swmDailyAttendanceService.countAttendanceByTeam(companyName, workshopName, lineName, teamName, date);
                    attendanceAnalysis.setAttendanceCount(attendanceCount);
                    if(attendanceCount == 0 || list.isEmpty()){
                        attendanceAnalysis.setAttendanceRate("0.00");
                    }else{
                        String attendanceRate = BigDecimal.valueOf(attendanceCount).divide(BigDecimal.valueOf(list.size()), 2, RoundingMode.HALF_UP).toString();
                        attendanceAnalysis.setAttendanceRate(attendanceRate);
                    }

                    result.add(attendanceAnalysis);
                }
            }
        }
        return result;
    }

    private List<AttendanceAnalysis> getTeamAttendanceAnalysisNew(String companyCode, String companyName, String date) {

        //1.先查这个班组下有多少人
        List<AttendanceAnalysis>  allTeam =  swmDailyAttendanceService.getAllTeamNumber(companyCode);

        //2，在查班组出勤人数
        String format = DateUtil.format(new Date(), "yyyy-MM-dd");
        List<AttendanceAnalysis>  teamAttendance =  swmDailyAttendanceService.getTeamAttendance(companyCode, format);
        Map<String, Long> attendanceMap = teamAttendance.stream()
                .collect(Collectors.toMap(
                        AttendanceAnalysis::getName,
                        AttendanceAnalysis::getAttendanceCount
                ));

        // 3. 合并数据，计算出勤率
        for (AttendanceAnalysis team : allTeam) {
            // 班组总人数
            Long totalCount = team.getCount();
            // 若今天没人打卡，则默认为 0
            Long attendance = attendanceMap.getOrDefault(team.getName(), 0L);
            team.setAttendanceCount(attendance); // 设置出勤人数
            //计算出勤率
            double rate = (attendance * 100.0) / totalCount;
            team.setAttendanceRate(String.format("%.2f", rate));
        }
        return allTeam;
    }

    private Map<String, Object> getLast10DaysAttendance() {
        Map<String, Object> result = new HashMap<>();
        List<Long> attendanceCountList = new ArrayList<>();
        List<String> last10Days = getLast10Days();
        for (String last10Day : last10Days) {
            List<SwmDailyAttendance> todayAttendances = swmDailyAttendanceService.findByDate(DateUtils.parseDate(last10Day));
            long count = todayAttendances.stream().filter(a -> a.getClockInTime() != null).count();
            attendanceCountList.add(count);
        }
        List<String> days = new ArrayList<>();
        for (String day : last10Days) {
            days.add(day.substring(5));
        }
        result.put("x", days);
        result.put("y", attendanceCountList);
        return result;
    }

    private Map<String, Object> getLast7DaysAttendance(List<SwmPerson> swmPersonList) {
        Map<String, Object> result = new HashMap<>();

        List<String> last7Days = getLast7Days();
        List<Long> countList = new ArrayList<>();
        List<String> attendanceRateList = new ArrayList<>();
        for (String last7Day : last7Days) {
            log.info("getLast7DaysAttendance last7Day | {}", last7Day);
            List<SwmDailyAttendance> todayAttendances = swmDailyAttendanceService.findByDate(DateUtils.parseDate(last7Day));
            long count = todayAttendances.stream().filter(a -> a.getAttendanceNormal().equals("0")).count();
            countList.add(count);
            String attendanceRate = BigDecimal.valueOf(count).divide(BigDecimal.valueOf(swmPersonList.size()), 2, RoundingMode.HALF_UP).toString();
            attendanceRateList.add(attendanceRate);
        }
        List<String> days = new ArrayList<>();
        for (String last7Day : last7Days) {
            days.add(last7Day.substring(5));
        }
        result.put("x", days);
        result.put("y1", countList);
        result.put("y2", attendanceRateList);
        return result;
    }


    private List<EntryAndExitRecord> getEntryAndExitRecord(List<SwmDailyAttendance> todayAttendances){
        List<EntryAndExitRecord> list = new ArrayList<>();
        for (SwmDailyAttendance todayAttendance : todayAttendances) {
            String employeeId = todayAttendance.getEmployeeId();
            SwmPerson swmPerson = swmPersonService.get(employeeId);

            if(todayAttendance.getClockInTime() != null){
                EntryAndExitRecord record = new EntryAndExitRecord();
                record.setNameNumber(todayAttendance.getEmployeeName() + "-" + swmPerson.getSafetyHelmetId());
                record.setJobType(swmPerson.getJobType());
                record.setTeam(swmPerson.getTeam());
                record.setType("进场");
                record.setDateTime(todayAttendance.getClockInDate());
                list.add(record);
            }
            if(todayAttendance.getClockOutTime() != null){
                EntryAndExitRecord record = new EntryAndExitRecord();
                record.setNameNumber(todayAttendance.getEmployeeName() + "-" + swmPerson.getSafetyHelmetId());
                record.setJobType(swmPerson.getJobType());
                record.setTeam(swmPerson.getTeam());
                record.setType("出场");
                record.setDateTime(todayAttendance.getClockOutDate());
                list.add(record);
            }
        }
        list.sort(Comparator.comparing(EntryAndExitRecord::getDateTime).reversed());
        return list;
    }

    // 统计结果封装类
    @Data
    @AllArgsConstructor
    public static class JobTypeCount {
        private String jobType;
        private Long count;
    }

    @Data
    @AllArgsConstructor
    public static class AttendanceCount {
        private String date;
        private Long count;
    }

    @Data
    public static class EntryAndExitRecord {
        private String nameNumber;
        private String jobType;
        private String team;
        private String type;
        private Date dateTime;
    }

    @Data
    public static class AttendanceAnalysis {
        private String name;
        private Long count;
        private Long attendanceCount;
        private String attendanceRate;
    }
    /**
     * 获取不包含今天在内的近7天日期列表(格式: yyyy-MM-dd)
     */
    private List<String> getLast7Days() {
        List<String> days = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

        Calendar calendar = Calendar.getInstance();
        // 不包含今天，所以从今天往前推6天
        for (int i = 0; i < 7; i++) {
            calendar.add(Calendar.DAY_OF_YEAR, -1);
            days.add(sdf.format(calendar.getTime()));
        }

        // 反转列表，使日期按从早到晚排序
        Collections.reverse(days);
        return days;
    }
    /**
     * 获取包含今天在内的近10天日期列表(格式: yyyy-MM-dd)
     */
    private List<String> getLast10Days() {
        List<String> days = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

        Calendar calendar = Calendar.getInstance();
        // 不包含今天，所以从今天往前推6天
        for (int i = 0; i < 10; i++) {
            days.add(sdf.format(calendar.getTime()));
            calendar.add(Calendar.DAY_OF_YEAR, -1);
        }

        // 反转列表，使日期按从早到晚排序
        Collections.reverse(days);
        return days;
    }
    //******************************************************************************数据看板-劳务管理******************************************************************//

    //******************************************************************************数据看板-列表查询******************************************************************//
    // 今日出勤人数列表
    @GetMapping("/attendance/list")
    @ResponseBody
    @ApiOperation("今日出勤人数列表")
    public Page<Person> attendanceList(HttpServletRequest request, HttpServletResponse response) {
        Date date = new Date();
        // 创建查询参数对象
        Person person = new Person();
        person.setPage(new Page<>(request, response));
        person.setDate(DateUtils.formatDate(date));
        // 调用服务层方法进行数据库分页查询
        Page<Person> resultPage = swmDailyAttendanceService.attendanceList(person);
        return resultPage;
    }
    // 实时作业人数列表
    @GetMapping("/working/list")
    @ResponseBody
    @ApiOperation("实时作业人数列表")
    public Page<Person> workingList(HttpServletRequest request, HttpServletResponse response) {
        Page<SwmPerson> page = new Page<>();
        SwmPerson swmPersonPage = new SwmPerson();
        swmPersonPage.setPage(new Page<>(request, response));
        List<Person> list = new ArrayList<>();
        Page<Person> resultPage = new Page<>();
        // Date date = new Date(2025 - 1900, 8, 20);
        Date date = new Date();
//        // 获取1小时前的时间字符串
//        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//        Calendar calendar = Calendar.getInstance();
//        calendar.add(Calendar.HOUR_OF_DAY, -1); // 减去1小时
//        String oneHourAgo = sdf.format(calendar.getTime());
        // 从TDengine获取1小时内的唯一身份证集合
//        Set<String> uniqueIdCards = getUniqueIdCardsFromTDengine(oneHourAgo, null);

        //身份证从redis里获取，先获取到所有的设备，然后在获取设备对应的身份证
        Set<Object> deviceIds = redisService.sGet(SwmRedisConstant.Device.ONLINE_DEVICES_KEY);
        Set<String> uniqueIdCards = new HashSet<>();
        if (deviceIds != null) {
            for (Object deviceId : deviceIds) {
                String currentPerson = (String) redisService.hget(SwmRedisConstant.Helmet.DEVICE_PERSON_MAP, String.valueOf(deviceId));
                uniqueIdCards.add(currentPerson);
            }
        }

        if (!uniqueIdCards.isEmpty()) {

            swmPersonPage.setIdCards(new ArrayList<>(uniqueIdCards));
            // 通过身份证号查询人员信息
//            List<SwmPerson> persons = swmPersonService.findByIdCards(new ArrayList<>(uniqueIdCards));
            page = swmPersonService.findByIdCardsPage(swmPersonPage);
            List<SwmPerson> persons = page.getList();
            for (SwmPerson swmPerson : persons) {
                // 查询考勤记录
                SwmDailyAttendance dailyAttendance = swmDailyAttendanceService.findByEmployeeIdAndDate(swmPerson.getId(), date);
                Person person = new Person();
                person.setName(swmPerson.getName());
                person.setGender(swmPerson.getGender());
                person.setPhone(swmPerson.getPhoneNumber());
                person.setPersonType(swmPerson.getPersonType());
                if(Objects.isNull(dailyAttendance)){
                    person.setClockInDate(null);
                }else{
                    person.setClockInDate(dailyAttendance.getClockInDate());
                }
                list.add(person);
            }
        } else {
            logger.warn("查询1小时内工作中人数失败或无数据");
        }
        resultPage.setList(list);
        resultPage.setCount(page.getCount());
        resultPage.setPageNo(swmPersonPage.getPageNo());
        resultPage.setPageSize(swmPersonPage.getPageSize());
        return resultPage;
    }
    // 5天未考勤人数列表
    @GetMapping("/abnormalAttendance/list")
    @ResponseBody
    @ApiOperation("5天未考勤人数列表")
    public List<Person> abnormalAttendanceList() {
        List<Person> list = new ArrayList<>();
        // 查询从今天开始往前推30天的考勤记录
        LocalDate today = LocalDate.now();
        LocalDate thirtyDaysAgo = today.minusDays(30);
        Date startDate = Date.from(thirtyDaysAgo.atStartOfDay(ZoneId.systemDefault()).toInstant());
        List<SwmDailyAttendance> attendanceList = swmDailyAttendanceService.findByDateRange(startDate, new Date());
        // 生成最近N个工作日日期（含今天）
        List<LocalDate> targetDates = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            targetDates.add(today.minusDays(i));
        }
        // 按员工分组考勤记录
        Map<String, List<SwmDailyAttendance>> recordsByEmployee = attendanceList.stream()
                .collect(Collectors.groupingBy(SwmDailyAttendance::getEmployeeId));
        // 统计符合条件的员工
        Set<String> abnormalEmployees = new HashSet<>();
        for (Map.Entry<String, List<SwmDailyAttendance>> entry : recordsByEmployee.entrySet()) {
            String empId = entry.getKey();
            List<SwmDailyAttendance> empRecords = entry.getValue();
            boolean allDaysAbsent = true;
            // 检查每个目标日期
            for (LocalDate date : targetDates) {
                Optional<SwmDailyAttendance> recordOptional = empRecords.stream()
                        .filter(record -> record.getAttendanceDate().equals(date))
                        .findFirst();
                // 情况1：该日期无考勤记录（未排班）
                if (!recordOptional.isPresent()) {
                    allDaysAbsent = false;
                    break;
                }
                // 情况2：有记录但非有效未打卡
                SwmDailyAttendance record = recordOptional.get();
                if (!record.isEffectiveAbsence()) {
                    allDaysAbsent = false;
                    break;
                }
            }
            // 所有日期均为有效未打卡
            if (allDaysAbsent) {
                abnormalEmployees.add(empId);
            }
        }
        for (String abnormalEmployee : abnormalEmployees) {
            SwmPerson swmPerson = swmPersonService.get(abnormalEmployee);
            Person person = new Person();
            person.setName(swmPerson.getName());
            person.setGender(swmPerson.getGender());
            person.setPhone(swmPerson.getPhoneNumber());
            person.setPersonType(swmPerson.getPersonType());
            list.add(person);
        }
        return list;
    }
    // 在场工人数列表
    @GetMapping("/worker/list")
    @ResponseBody
    @ApiOperation("在场工人数列表")
    public Page<Person> workerList( HttpServletRequest request, HttpServletResponse response) {
        Page<Person> personPage = new Page<>();
        List<Person> list = new ArrayList<>();
        SwmPerson query = new SwmPerson();
        query.setPersonnelStatus(SwmPerson.PersonStatusEnum.ACTIVE); // '1' - 在职
        query.setStatus("0"); // 正常状态
//        query.setPersonType(SwmPerson.PersonTypeEnum.WORKER);
        query.setPage(new Page<>(request, response));
        Page<SwmPerson> page = swmPersonService.findPage(query);
        List<SwmPerson> swmPersonList = page.getList();
        for (SwmPerson swmPerson : swmPersonList) {
            Person person = new Person();
            person.setName(swmPerson.getName());
            person.setGender(swmPerson.getGender());
            person.setPhone(swmPerson.getPhoneNumber());
            person.setPersonType(swmPerson.getPersonType());
            list.add(person);
        }
        personPage.setCount(page.getCount());
        personPage.setPageNo(page.getPageNo());
        personPage.setPageSize(page.getPageSize());
        personPage.setList(list);
        return personPage;
    }
    // 在场管理员人数列表
    @GetMapping("/manager/list")
    @ResponseBody
    @ApiOperation("在场管理员人数列表")
    public Page<Person> managerList(HttpServletRequest request, HttpServletResponse response) {
        Page<Person> personPage = new Page<>();
        List<Person> list = new ArrayList<>();
        SwmPerson query = new SwmPerson();
        query.setPersonnelStatus(SwmPerson.PersonStatusEnum.ACTIVE); // '1' - 在职
        query.setStatus("0"); // 正常状态
        query.setPersonType(SwmPerson.PersonTypeEnum.MANAGER);
        query.setPage(new Page<>(request, response));
        Page<SwmPerson> page = swmPersonService.findPage(query);
        List<SwmPerson> swmPersonList = page.getList();
        for (SwmPerson swmPerson : swmPersonList) {
            Person person = new Person();
            person.setName(swmPerson.getName());
            person.setGender(swmPerson.getGender());
            person.setPhone(swmPerson.getPhoneNumber());
            person.setPersonType(swmPerson.getPersonType());
            list.add(person);
        }
        personPage.setCount(page.getCount());
        personPage.setPageNo(page.getPageNo());
        personPage.setPageSize(page.getPageSize());
        personPage.setList(list);
        return personPage;
    }
    // 报警记录列表
    @RequestMapping(value = "/warningRecord/list")
    @ResponseBody
    @ApiOperation("报警记录列表")
    public Page<SwmWarningManagement> warningRecordList(SwmWarningManagement swmWarningManagement, HttpServletRequest request,
            HttpServletResponse response) {
        // 创建分页对象
        Page<SwmWarningManagement> page = new Page<>(request, response);
        // 确保对象不为空
        if (swmWarningManagement == null) {
            swmWarningManagement = new SwmWarningManagement();
        }
        // 处理时间范围查询条件
        String beginTime = DateUtils.getDate() + " 00:00:00";
        String endTime = DateUtils.getDate() + " 23:59:59";
        // 设置开始时间和结束时间条件
        swmWarningManagement.setBeginAlarmTime(DateUtils.parseDate(beginTime));
        swmWarningManagement.setEndAlarmTime(DateUtils.parseDate(endTime));
        logger.info(
                "查询参数: id={}, personName={}, warningType={}, warningContent={}, handleStatus={}, excludeSOS={}, excludeAttendance={}, excludeGateEntry={}, beginAlarmTime={}, endAlarmTime={}",
                swmWarningManagement.getId(),
                swmWarningManagement.getPersonName(),
                swmWarningManagement.getWarningType(),
                swmWarningManagement.getWarningContent(),
                swmWarningManagement.getHandleStatus(),
                swmWarningManagement.isExcludeSOS(),
                swmWarningManagement.isExcludeAttendance(),
                swmWarningManagement.isExcludeGateEntry(),
                swmWarningManagement.getBeginAlarmTime(),
                swmWarningManagement.getEndAlarmTime());
        // 调用服务层方法，仅从 TDengine 查询数据
        // 时区调整已在SQL查询中完成，无需再次调整
        swmWarningManagement.setMasterDataAlarm( true);
        Page<SwmWarningManagement> resultPage = swmWarningManagementService.tdEngineFindPage(page, swmWarningManagement);
        logger.info("查询完成，数据中的时区调整已在SQL中进行");
        // 添加日志检查返回的数据
        if (resultPage != null && resultPage.getList() != null && !resultPage.getList().isEmpty()) {
            logger.info("返回数据总条数: {}", resultPage.getCount());
            // 检查数据是否来自MySQL还是时序数据库
            int mysqlCount = 0;
            int tdEngineCount = 0;
            for (SwmWarningManagement item : resultPage.getList()) {
                if (item.getDeviceId() != null || item.getIdCard() != null) {
                    mysqlCount++;
                } else {
                    tdEngineCount++;
                }
                // 确保处置时长字段即使为0也返回
                if (item.getDisposalDuration() == null) {
                    item.setDisposalDuration(0L);
                    logger.debug("ID: {}, 处置时长为空，设置为0", item.getId());
                } else {
                    logger.debug("ID: {}, 处置时长: {}", item.getId(), item.getDisposalDuration());
                }
                // 记录坐标信息，确保即使为空也在日志中显示
                logger.debug("ID: {}, x坐标: {}, y坐标: {}",
                        item.getId(),
                        item.getX() != null ? item.getX() : "null",
                        item.getY() != null ? item.getY() : "null");
            }
            logger.info("返回数据中，来自MySQL的记录: {}条，来自时序数据库的记录: {}条", mysqlCount, tdEngineCount);
        } else {
            logger.info("返回数据为空或没有记录");
        }
        return resultPage;
    }
    // 低电量人数列表
    @GetMapping("/lowBattery/list")
    @ResponseBody
    @ApiOperation("低电量人数列表")
    public List<Person> lowBatteryList() {
        List<Person> list = new ArrayList<>();
        List<SwmPerson> swmPersonList = swmPersonService.findActivePersons();
        // 查询电量低于20的设备
        List<Map<String, String>> deviceList = swmHelmetDeviceService.findDeviceIdAndIdBatteryByBatteryLevel(20);
        for (SwmPerson swmPerson : swmPersonList) {
            for (Map<String, String> map : deviceList) {
                if(swmPerson.getSafetyHelmetId().equals(map.get("deviceId"))){
                    Person person = new Person();
                    person.setName(swmPerson.getName());
                    person.setGender(swmPerson.getGender());
                    person.setPhone(swmPerson.getPhoneNumber());
                    person.setPersonType(swmPerson.getPersonType());
                    person.setBattery(map.get("latestBattery"));
                    list.add(person);
                }
            }
        }
        return list;
    }

    @Data
    public static class Person extends BaseEntity<Person> {
        private String name;
        private String gender;
        private String phone;
        private String personType;
        private Date clockInDate;
        private String battery;
        private String date;
        List<String> personTypeList;

    }
    //******************************************************************************数据看板-列表查询******************************************************************//


    @GetMapping("/worker/todayAttendanceManagerList")
    @ResponseBody
    @ApiOperation("管理员今日在厂")
    public Page<Person> todayAttendanceManagerList(Person vo,  HttpServletRequest request, HttpServletResponse response) {
        String[] managerIds = {SwmPerson.PersonTypeEnum.MANAGER};
        vo.setPersonTypeList(Arrays.asList(managerIds));
        String date = DateUtils.getDate();
        vo.setDate(date);
//        Page<SwmDashboardNewController.Person> page = swmPersonService.findTodayAttendance(vo);
        Page<SwmDashboardNewController.Person> page = swmPersonService.findManageTodayList(vo);
        return page;
    }

    @GetMapping("/worker/todayAttendanceWorkerList")
    @ResponseBody
    @ApiOperation("工人今日在厂")
    public Page<SwmDashboardNewController.Person> todayAttendanceWorkerList(Person vo, HttpServletRequest request, HttpServletResponse response) {
        String[] managerIds = {SwmPerson.PersonTypeEnum.WORKER, SwmPerson.PersonTypeEnum.TEAMLEADER, SwmPerson.PersonTypeEnum.SPECIALTRADES};
        vo.setPersonTypeList(Arrays.asList(managerIds));
        String date = DateUtils.getDate();
        vo.setDate(date);
        Page<SwmDashboardNewController.Person> page = swmPersonService.findManageTodayList(vo);
        return page;
    }

    @GetMapping("/person/idleHoursRanking")
    @ResponseBody
    @ApiOperation("人员休闲区停留时长")
    public Page<SwmDashboardDto.IdleHoursRankingDto> idleHoursRanking(SwmDashboardDto.IdleHoursRankingDto  vo) {
        Page<SwmDashboardDto.IdleHoursRankingDto> result  = swmDailyAttendanceService.idleHoursRanking(vo);
        return result;
    }

    @GetMapping("/person/managementOnDuty")
    @ResponseBody
    @ApiOperation("管理人员在岗情况")
    public Page<SwmDashboardDto.ManagementOnDutyDto> managementOnDuty(SwmDashboardDto.ManagementOnDutyDto  vo) {
        Page<SwmDashboardDto.ManagementOnDutyDto> result  = swmDailyAttendanceService.managementOnDuty(vo);
        return result;
    }

}
