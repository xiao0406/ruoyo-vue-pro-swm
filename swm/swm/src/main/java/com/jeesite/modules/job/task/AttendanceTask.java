package com.jeesite.modules.job.task;

import cn.hutool.core.date.DateUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import com.jeesite.common.lang.DateUtils;
import com.jeesite.common.lang.StringUtils;
import com.jeesite.modules.cache.service.RedisService;
import com.jeesite.modules.config.TenantContext;
import com.jeesite.modules.constant.TdengineSuperTableConstant;
import com.jeesite.modules.enums.CorpDbEnum;
import com.jeesite.modules.swm.entity.*;
import com.jeesite.modules.swm.service.*;
import com.jeesite.modules.sys.entity.User;
import com.jeesite.modules.sys.service.UserService;
import com.jeesite.modules.sys.utils.CorpUtils;
import com.jeesite.modules.util.BatchOperationsUtil;
import com.jeesite.modules.utils.R;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * 考勤统计定时任务
 * 
 * @author: cjie
 * @date: 2025/6/10
 */
@Component
@Slf4j
public class AttendanceTask {
    @Autowired
    private SwmDailyAttendanceService swmDailyAttendanceService;
    @Autowired
    private SwmAttendanceSummaryService swmAttendanceSummaryService;
    @Autowired
    private SwmPersonService swmPersonService;
    @Autowired
    private SwmPersonScheduleService swmPersonScheduleService;
    @Autowired
    private SwmScheduleTimeService swmScheduleTimeService;
    @Autowired
    private HelmetRundeCaReportLocationTdEnginService helmetTdengineService;
    @Autowired
    private SwmJobLogService swmJobLogService;
    @Autowired
    private TDengineService tdengineService;
    @Autowired
    private UserService userService;

    @Value("${tdengine.dbname:swm_db}")
    private String dbname;

    @Autowired
    private RedisService redisService;

    @Qualifier("swmExecutor")
    @Autowired
    private ThreadPoolTaskExecutor swmExecutor;
    
    // 定义常量
    private static final long CONTINUITY_THRESHOLD_MS = 1 * 60 * 1000; // 10分钟连续性阈值
    private static final SimpleDateFormat DATETIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");


    /**
     * 定时生成两个月的日考勤数据-颠覆性产业园使用
     */
    @XxlJob("generateMonthlyDailyAttendance")
    @Transactional(readOnly = false)
    public void generateMonthlyDailyAttendance() {
        SwmJobLog jobLog = new SwmJobLog();
        String corpCode = "DFXCYY";
        //获取参数
        String jobParam = XxlJobHelper.getJobParam();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date startDate = null; // 传入的开始日期
        Date nowDate = new Date(); // 当前日期

        if (StringUtils.isNotBlank(jobParam)) {
            try {
                startDate = dateFormat.parse(jobParam);
            } catch (Exception e) {
                XxlJobHelper.log("日期参数格式错误，使用当天: {}", jobParam);
            }
        }

        // 日期合法性判断：开始日期不能晚于今天
        if (startDate.after(nowDate)) {
            XxlJobHelper.log("开始日期不能晚于当前日期，startDate:{}", dateFormat.format(startDate));
            return;
        }

        // 循环：从 startDate 循环到 nowDate 的每一天
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(startDate);

        // 1.先从排班表查询所有人员
        SwmPersonSchedule swmPersonSchedule = new SwmPersonSchedule();
        swmPersonSchedule.setCorpCode(corpCode);
        List<SwmPersonSchedule> schedules = swmPersonScheduleService.findListByCorpCode(swmPersonSchedule);

        while (!calendar.getTime().after(nowDate)) {
            Date currentDay = calendar.getTime(); // 当前循环到的那一天
            String dayStr = dateFormat.format(currentDay);
            XxlJobHelper.log("开始生成日期：{} 的考勤数据", dayStr);

            // ================ 每一天的考勤生成逻辑 ↓ 放在这里 ================
            generateOneDayAttendance(corpCode, currentDay, schedules);

            // 日期 +1 天
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }

    }


    /**
     * 生成某一天的考勤数据
     */
    private void generateOneDayAttendance(String corpCode, Date targetDate, List<SwmPersonSchedule> scheduleList) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat sdfTime = new SimpleDateFormat("HH:mm:ss");
        SimpleDateFormat sdfDateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        XxlJobHelper.log("生成考勤日期：{}", sdf.format(targetDate));

        if (CollectionUtils.isEmpty(scheduleList)) {
            XxlJobHelper.log("暂无排班数据，跳过");
            return;
        }

        // 2. 查询当天是否已有考勤（有则跳过）
        SwmDailyAttendance attendanceParam = new SwmDailyAttendance();
        attendanceParam.setAttendanceDate(targetDate);
        attendanceParam.setCorpCode(corpCode);
        attendanceParam.setRandom(new Random().nextInt(1_000_000));
        List<SwmDailyAttendance> existList = swmDailyAttendanceService.findList(attendanceParam);

        // 已存在，跳过
        if (CollectionUtils.isNotEmpty(existList)) {
            XxlJobHelper.log("日期 {} 已有考勤数据，跳过", sdf.format(targetDate));
            return;
        }

        // 3. 批量生成考勤
        List<SwmDailyAttendance> insertList = new ArrayList<>();

        for (SwmPersonSchedule schedule : scheduleList) {
            SwmDailyAttendance att = new SwmDailyAttendance();
            att.setEmployeeId(schedule.getEmployeeId());
            att.setEmployeeName(schedule.getPersonName());
            att.setIdentityCard(schedule.getIdCard());
            att.setDeviceId(schedule.getDeviceId());
            att.setPersonType(schedule.getPersonType());
            att.setAttendanceDate(targetDate);
            att.setCorpCode(corpCode);
            att.setCorpName("颠覆性产业园");
            att.setRestTime(new BigDecimal("0"));

            // 1=早班，3=夜班
            String classes = schedule.getClasses();
            att.setClasses(classes);

            try {
                Date clockInTimeDate;   // 上班时间
                Date clockOutTimeDate;  // 下班时间

                String startClockInTimeStr = "05:00:00";
                String endClockInTimeStr = "07:00:00";
                String startClockOutTimeStr = "17:30:00";
                String endClockOutTimeStr = "18:00:00";

                if ("1".equals(classes)) {
                    // ==================== 早班 ====================
                    // 打卡开始时间 01:40:00
                    att.setClockStartTime(sdfDateTime.parse(sdf.format(targetDate) + " 01:40:00"));
                    // 打卡结束时间 次日13:40:00
                    Date nextDay = DateUtils.addDays(targetDate, 1);
                    att.setClockEndTime(sdfDateTime.parse(sdf.format(nextDay) + " 13:40:00"));

                    // 上班：05:40 - 08:00
                    clockInTimeDate = getRandomTime(targetDate, startClockInTimeStr, endClockInTimeStr);
                    // 下班：17:30 - 18:30
                    clockOutTimeDate = getRandomTime(targetDate, startClockOutTimeStr, endClockOutTimeStr);

                } else if ("3".equals(classes)) {
                    // ==================== 夜班 ====================
                    att.setClockStartTime(sdfDateTime.parse(sdf.format(targetDate) + " 13:40:00"));
                    Date nextDay = DateUtils.addDays(targetDate, 1);
                    att.setClockEndTime(sdfDateTime.parse(sdf.format(nextDay) + " 13:40:00"));

                    // 上班：07:00 - 08:00
                    clockInTimeDate = getRandomTime(targetDate, startClockOutTimeStr, endClockOutTimeStr);
                    // 下班：17:30 - 18:30
                    clockOutTimeDate = getRandomTime(targetDate, "22:00:00", "22:30:00");

                } else {
                    // 未知班次跳过
                    continue;
                }

                // 打卡时间（Date类型）
                att.setClockInTime(clockInTimeDate);
                att.setClockOutTime(clockOutTimeDate);
                att.setClockInDate(clockInTimeDate);
                att.setClockOutDate(clockOutTimeDate);

                // 应出勤工时
                BigDecimal scheduledHours = new BigDecimal("10.50");
                att.setScheduledHours(scheduledHours);

// ==================== 工时计算（按你最新规则） ====================
                // 1. 总时长（下班 - 上班）
                BigDecimal totalHours = calculateHours(clockInTimeDate, clockOutTimeDate);

                // 2. 怠工时长：0 ~ 0.5 小时随机（半小时以内）
                Random random = new Random();
                double result = BigDecimal.ZERO.doubleValue() + (new BigDecimal("0.50").doubleValue() - BigDecimal.ZERO.doubleValue()) * random.nextDouble();
                BigDecimal idleHours = new BigDecimal(result).setScale(2, RoundingMode.HALF_UP);

                // 3. 固定减去 1 小时,是中午休息
                BigDecimal subtractHour = new BigDecimal("1.00");

                // 4. 实际考勤时长 = 总时长 - 怠工时长 - 1小时
                BigDecimal actualHours = totalHours.subtract(idleHours).subtract(subtractHour);
                // 防止出现负数（保底 0）
                if (actualHours.compareTo(BigDecimal.ZERO) < 0) {
                    actualHours = BigDecimal.ZERO;
                }

                att.setIdleHours(idleHours);        // 怠工时长
                att.setActualHours(actualHours);    // 实际考勤时长

                // 日效率
                BigDecimal dailyEfficiency = calculateDailyEfficiency(actualHours, scheduledHours);
                att.setDailyEfficiency(dailyEfficiency);

                // 日达成率
                BigDecimal dailyAchievementRate = calculateDailyAchievementRate(actualHours, scheduledHours);
                att.setDailyAchievementRate(dailyAchievementRate);

                // 固定值
                att.setAttendanceNormal("1");
                att.setCurrentPosition("3");
                att.setEffectiveWorkHours(att.getActualHours());
                att.setWorkTimeRange("05:40-17:30");

                insertList.add(att);

            } catch (Exception e) {
                XxlJobHelper.log("生成考勤异常，人员：{}，日期：{}，错误：{}",
                        schedule.getPersonName(), sdf.format(targetDate), e.getMessage());
            }
        }

        // 批量插入
        if (CollectionUtils.isNotEmpty(insertList)) {
            List<List<SwmDailyAttendance>> lists = BatchOperationsUtil.batchCutting(insertList, 50);
            for (List<SwmDailyAttendance> insertData : lists) {
                swmDailyAttendanceService.saveBatch(insertData);
            }
//            swmDailyAttendanceService.saveBatch(insertList);
            XxlJobHelper.log("日期 {} 生成考勤 {} 条", sdf.format(targetDate), insertList.size());
        }
    }

    // ==================== 工具方法 ====================
    private Date getRandomTime(Date baseDate, String startTime, String endTime) throws Exception {
        // 每次都 new ，杜绝线程安全问题！
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat sdfDateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        String base = sdf.format(baseDate);
        Date start = sdfDateTime.parse(base + " " + startTime);
        Date end = sdfDateTime.parse(base + " " + endTime);

        long randomMs = start.getTime() + (long) (Math.random() * (end.getTime() - start.getTime()));
        return new Date(randomMs);
    }

    private BigDecimal calculateHours(Date start, Date end) {
        long ms = end.getTime() - start.getTime();
        double hours = ms / (1000.0 * 60 * 60);
        return new BigDecimal(hours).setScale(2, RoundingMode.HALF_UP);
    }


    /**
     * 计算怠工时长定时任务，工作时长定时任务
     * 
     * @author: Shawn
     * @date: 2025/6/20
     */
    @XxlJob("calculateIdleHours")
    public void calculateIdleHours() {
        SwmJobLog jobLog = new SwmJobLog();
        jobLog.setJobName("calculateIdleHours");
        jobLog.setStartTime(new Date());
        jobLog.setExecuteStatus("1"); // 默认失败

        //获取系统所有租户信息
        List<User> corpList = userService.findCorpList(new User());
        if (CollectionUtils.isEmpty(corpList)) {
            XxlJobHelper.log("没有租户信息");
            return;
        }
        //为每个租户都生成排班计划
        for (User user : corpList) {

            String corpCode = user.getCorpCode();
            String corpName = user.getCorpName();


            try {
                //设置当前线程的租户信息
                CorpUtils.setCurrentCorpCode(corpCode, corpName);
                XxlJobHelper.log("开始处理租户：{} ========================", corpCode);

                XxlJobHelper.log("开始执行怠工时长计算任务...");

                // 获取传入的日期参数，如果没有传入则使用当天
                String jobParam = XxlJobHelper.getJobParam();
                jobLog.setJobParam(jobParam);
                swmJobLogService.save(jobLog);
                jobLog.setIsNewRecord(false);
                Date targetDate;
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

                if (StringUtils.isNotBlank(jobParam)) {
                    try {
                        targetDate = dateFormat.parse(jobParam);
                    } catch (Exception e) {
                        XxlJobHelper.log("日期参数格式错误，使用当天: {}", jobParam);
                        targetDate = new Date();
                    }
                } else {
                    targetDate = new Date();
                }

                // 如果当前时间是00:00到06:59，查询昨天的考勤记录
                Calendar now = Calendar.getInstance();
                int currentHour = now.get(Calendar.HOUR_OF_DAY);
                if (currentHour >= 0 && currentHour <= 6) {
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(targetDate);
                    cal.add(Calendar.DAY_OF_MONTH, -1);
                    targetDate = cal.getTime();
                    XxlJobHelper.log("当前时间为早上{}点，查询昨天的考勤记录: {}", currentHour, dateFormat.format(targetDate));
                }

                String dateStr = dateFormat.format(targetDate);

                // 查询指定日期的所有考勤记录
                SwmDailyAttendance query = new SwmDailyAttendance();
                query.setAttendanceDate(targetDate);
                query.setRandom(new Random().nextInt(1_000_000));
                List<SwmDailyAttendance> attendanceList = swmDailyAttendanceService.findList(query);

                if (attendanceList.isEmpty()) {
                    XxlJobHelper.log("{}没有考勤记录，无需计算怠工时长", dateStr);
                    return;
                }

                int successCount = 0;
                int failCount = 0;

                // 为每条考勤记录计算怠工时长
                for (SwmDailyAttendance record : attendanceList) {
                    try {
                        // 获取员工身份证号
                        String idCard = swmDailyAttendanceService.getIdCardByEmployeeId(record.getEmployeeId());
                        if (idCard != null) {
                            // 获取工作时间范围
                            String workTimeRange = record.getWorkTimeRange();
                            if (workTimeRange == null || workTimeRange.trim().isEmpty()) {
                                workTimeRange = "08:00-17:00"; // 默认工作时间
                                XxlJobHelper.log("员工[{}]{}工作时间范围为空，使用默认时间范围: {}",
                                        record.getEmployeeId(), record.getEmployeeName(), workTimeRange);
                            }

                            // 计算怠工时长，传递工作时间范围
                            double calculatedIdleHours = swmDailyAttendanceService.calculateIdleTimeByIdCard(idCard,
                                    dateStr, workTimeRange);

                            // 更新怠工时长字段
                            record.setIdleHours(BigDecimal.valueOf(calculatedIdleHours).setScale(2, RoundingMode.HALF_UP));

                            // 计算实际工作时长（基于工作区域）
                            double calculatedEffectiveWorkHours = swmDailyAttendanceService
                                    .calculateEffectiveWorkHoursByIdCard(idCard,
                                            dateStr, workTimeRange);

                            // 更新实际工作时长字段
                            record.setEffectiveWorkHours(
                                    BigDecimal.valueOf(calculatedEffectiveWorkHours).setScale(2, RoundingMode.HALF_UP));

                            XxlJobHelper.log("员工[{}]{}的实际工作时长计算完成: {} 小时 (工作时间: {})",
                                    record.getEmployeeId(), record.getEmployeeName(), calculatedEffectiveWorkHours,
                                    workTimeRange);

                            // 获取用于计算的打卡时间（不修改原始记录，只用于计算）
                            // @author: Shawn
                            // @date: 2025/01/27
                            Date clockInTime = record.getClockInTime();
                            Date clockOutTime = record.getClockOutTime();
                            boolean usedTdengineData = false;

                            // 如果打卡时间为空，从TDengine表中获取用于计算的时间
                            if (clockInTime == null || clockOutTime == null) {
                                try {
                                    // 使用考勤记录中的实际考勤日期查询TDengine
                                    String recordDateStr = dateFormat.format(record.getAttendanceDate());
                                    R<Map<String, Object>> tdengineResult = helmetTdengineService
                                            .getFirstAndLastTimeByIdCardAndDate(idCard, recordDateStr, workTimeRange);

                                    if (tdengineResult.getCode() == R.SUCCESS) {
                                        Map<String, Object> timeData = tdengineResult.getData();
                                        Object firstTimeObj = timeData.get("firstTime");
                                        Object lastTimeObj = timeData.get("lastTime");

                                        // 如果上班打卡时间为空，临时使用TDengine的第一条记录时间进行计算
                                        if (clockInTime == null && firstTimeObj != null) {
                                            clockInTime = parseTimeObject(firstTimeObj);
                                            if (clockInTime != null) {
                                                usedTdengineData = true;
                                                log.info("员工[{}]{}上班打卡时间为空，临时使用TDengine第一条记录时间进行计算: {}",
                                                        record.getEmployeeId(), record.getEmployeeName(), clockInTime);
                                            }
                                        }

                                        // 如果下班打卡时间为空，临时使用TDengine的最后一条记录时间进行计算
                                        if (clockOutTime == null && lastTimeObj != null) {
                                            clockOutTime = parseTimeObject(lastTimeObj);
                                            if (clockOutTime != null) {
                                                usedTdengineData = true;
                                                log.info("员工[{}]{}下班打卡时间为空，临时使用TDengine最后一条记录时间进行计算: {}",
                                                        record.getEmployeeId(), record.getEmployeeName(), clockOutTime);
                                            }
                                        }
                                    } else {
                                        XxlJobHelper.log("员工[{}]{}从TDengine查询时间记录失败: {}",
                                                record.getEmployeeId(), record.getEmployeeName(), tdengineResult.getMsg());
                                    }
                                } catch (Exception e) {
                                    XxlJobHelper.log("员工[{}]{}从TDengine获取时间用于计算时异常: {}",
                                            record.getEmployeeId(), record.getEmployeeName(), e.getMessage());
                                }
                            }

                            // 计算实际考勤时长
                            // 修改逻辑: 1. 如果没有上下班打卡时间且TDengine也没有数据，实际考勤为0
                            // 2. 如果有上下班打卡时间（包括从TDengine获取的），实际考勤 = 下班打卡时间 - 上班打卡时间 - 怠工时长
                            // @author: Shawn
                            // @date: 2025/01/27
                            if (clockInTime == null || clockOutTime == null) {
                                // 没有打卡时间且TDengine也没有可用数据，使用兜底逻辑
                                BigDecimal actualHours = applyActualHoursFallback(record, BigDecimal.ZERO,
                                        "没有完整的打卡记录且TDengine也无可用数据");
                                record.setActualHours(actualHours);
                            } else {
                                // 有完整打卡时间（可能来自TDengine），计算实际工作时长
                                BigDecimal clockWorkHours = calculateWorkHoursBetweenTimes(clockInTime, clockOutTime);

                                // 实际考勤时长 = 打卡工作时长 - 怠工时长
                                BigDecimal actualHours = clockWorkHours.subtract(record.getIdleHours());

                                // 应用兜底逻辑
                                actualHours = applyActualHoursFallback(record, actualHours,
                                        "打卡工作时长减去怠工时长后");

                                record.setActualHours(actualHours.setScale(2, RoundingMode.HALF_UP));
                                String dataSource = usedTdengineData ? "(包含TDengine数据)" : "";
                                XxlJobHelper.log("员工[{}]{}实际考勤时长计算{}: 打卡工作{}小时 - 怠工{}小时 = 实际{}小时",
                                        record.getEmployeeId(), record.getEmployeeName(), dataSource,
                                        clockWorkHours, record.getIdleHours(), actualHours);
                            }

                            // 计算日考勤功效
                            // 功效 = 实际考勤时长/应该考勤时长
                            // @author: Shawn
                            // @date: 2025/06/23
                            BigDecimal dailyEfficiency = calculateDailyEfficiency(record.getActualHours(), record.getScheduledHours());
                            record.setDailyEfficiency(dailyEfficiency);
                            XxlJobHelper.log("员工[{}]{}日考勤功效计算: 实际考勤时长{}/应该考勤时长{}，等于={}",
                                    record.getEmployeeId(), record.getEmployeeName(),
                                    record.getActualHours(), record.getScheduledHours(), dailyEfficiency);

                            // 计算日达成率
                            // 达成率 = 实际工作时长 / 应考勤时长
                            // @author: Shawn
                            // @date: 2025/01/27
                            BigDecimal dailyAchievementRate = calculateDailyAchievementRate(record.getEffectiveWorkHours(),
                                    record.getScheduledHours());
                            record.setDailyAchievementRate(dailyAchievementRate);
                            XxlJobHelper.log("员工[{}]{}日达成率计算: {}小时 / {}小时 = {}",
                                    record.getEmployeeId(), record.getEmployeeName(),
                                    record.getEffectiveWorkHours(), record.getScheduledHours(), dailyAchievementRate);

                            // 更新考勤状态逻辑 - 使用新的业务规则
                            // @author: Shawn
                            // @date: 2025/01/27
                            updateAttendanceStatusByNewRule(record);
                            swmDailyAttendanceService.update(record);
                            successCount++;
                            XxlJobHelper.log("员工[{}]{}的怠工时长计算完成: {} 小时 (工作时间: {})",
                                    record.getEmployeeId(), record.getEmployeeName(), calculatedIdleHours, workTimeRange);
                        } else {
                            XxlJobHelper.log("员工[{}]{}未找到身份证号，跳过计算",
                                    record.getEmployeeId(), record.getEmployeeName());
                            failCount++;
                        }
                    } catch (Exception e) {
                        XxlJobHelper.log("计算员工[{}]{}怠工时长失败: {}",
                                record.getEmployeeId(), record.getEmployeeName(), e.getMessage());
                        failCount++;
                    }
                }
                XxlJobHelper.log("怠工时长计算任务完成。成功: {}条，失败: {}条", successCount, failCount);
                jobLog.setExecuteStatus("0"); // 成功
            } catch (Exception e) {
                XxlJobHelper.log("怠工时长计算任务执行异常", e);
                jobLog.setExceptionInfo(e.getMessage());
            } finally {
                jobLog.setEndTime(new Date());
                jobLog.setDuration(jobLog.getEndTime().getTime() - jobLog.getStartTime().getTime());
                swmJobLogService.save(jobLog);
            }
        }
    }

    /**
     * 统计当月考勤数据
     */
    @XxlJob("calculateMonthlyAttendance")
    public void calculateMonthlyAttendance() {
        log.info("开始执行月考勤统计定时任务");


        //获取系统所有租户信息
        List<User> corpList = userService.findCorpList(new User());
        if (CollectionUtils.isEmpty(corpList)) {
            XxlJobHelper.log("没有租户信息");
            return;
        }
        //为每个租户都生成排班计划
        for (User user : corpList) {

            String corpCode = user.getCorpCode();
            String corpName = user.getCorpName();


            try {
                //设置当前线程的租户信息
                CorpUtils.setCurrentCorpCode(corpCode, corpName);
                TenantContext.set(corpCode);
                XxlJobHelper.log("开始处理租户：{} ========================", corpCode);

                // 1. 获取当月日期范围
                Date startDate = DateUtil.beginOfMonth(DateUtil.date());
                Date endDate = DateUtil.date(); // 统计到当天

                // 2. 查询当月所有员工的日考勤记录
                SwmDailyAttendance query = new SwmDailyAttendance();
                query.setBeginAttendanceDate(startDate);
                query.setEndAttendanceDate(endDate);
                // query.setEmployeeId("1935182658540556288"); // 注意，测试使用生产上要去掉，先写死。
                query.setRandom(new Random().nextInt(1_000_000));
                List<SwmDailyAttendance> dailyAttendanceList = swmDailyAttendanceService.findList(query);

                // 3. 按员工ID分组
                Map<String, List<SwmDailyAttendance>> attendanceByEmployee = dailyAttendanceList.stream()
                        .collect(Collectors.groupingBy(SwmDailyAttendance::getEmployeeId));

                // 4. 计算当月应出勤天数(出勤天数按当月的总天数算)
                int scheduledDays = DateUtil.lengthOfMonth(DateUtil.thisMonth(), DateUtil.isLeapYear(DateUtil.thisYear()));

                // 5. 处理每个员工的考勤数据
                for (Map.Entry<String, List<SwmDailyAttendance>> entry : attendanceByEmployee.entrySet()) {
                    String employeeId = entry.getKey();
                    List<SwmDailyAttendance> employeeAttendance = entry.getValue();

                    // 获取员工基本信息
                    SwmPerson person = swmPersonService.get(employeeId);
                    if (person == null) {
                        log.warn("员工ID:{}不存在人员信息，跳过统计", employeeId);
                        continue;
                    }

                    // 计算实际出勤天数(实际考勤时长>0的记录数)
                    long actualDays = employeeAttendance.stream()
                            .filter(att -> att.getActualHours() != null && att.getActualHours().compareTo(BigDecimal.ZERO) > 0)
                            .count();

                    // 出勤率 = 实际出勤天数/应出勤天数
                    BigDecimal attendanceRate = actualDays > 0
                            ? BigDecimal.valueOf(actualDays).divide(BigDecimal.valueOf(scheduledDays), 4, RoundingMode.HALF_UP)
                            : BigDecimal.ZERO;

                    // 计算总时长
                    BigDecimal totalScheduledHours = employeeAttendance.stream()
                            .map(SwmDailyAttendance::getScheduledHours)
                            .filter(Objects::nonNull)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    // 修改：实际工作时间改为累计实际工作时长（基于工作区域计算）
                    BigDecimal totalEffectiveWorkHours = employeeAttendance.stream()
                            .map(SwmDailyAttendance::getEffectiveWorkHours)
                            .filter(Objects::nonNull)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    BigDecimal totalActualHours = employeeAttendance.stream()
                            .map(SwmDailyAttendance::getActualHours)
                            .filter(Objects::nonNull)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    BigDecimal totalIdleHours = employeeAttendance.stream()
                            .map(SwmDailyAttendance::getIdleHours)
                            .filter(Objects::nonNull)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    // 计算功效(平均每日功效)
                    BigDecimal avgEfficiency = employeeAttendance.stream()
                            .map(SwmDailyAttendance::getDailyEfficiency)
                            .filter(Objects::nonNull)
                            .reduce(BigDecimal.ZERO, BigDecimal::add)
                            .divide(BigDecimal.valueOf(actualDays > 0 ? actualDays : 1), 2, RoundingMode.HALF_UP);

                    // 6. 查询是否已存在当月记录
                    String monthStr = DateUtil.format(startDate, "yyyy-MM");
                    SwmAttendanceSummary existingSummary = swmAttendanceSummaryService.findByEmployeeIdAndMonth(employeeId,
                            monthStr,person.getCorpCode());

                    // 7. 保存或更新记录
                    SwmAttendanceSummary summary = existingSummary != null ? existingSummary : new SwmAttendanceSummary();

                    // 设置基本信息
                    summary.setEmployeeId(employeeId);
                    summary.setEmployeeName(person.getName());
                    summary.setDepartment(person.getDepartment());
                    summary.setWorkProcess(person.getWorkProcess());
                    summary.setTeam(person.getTeam());
                    summary.setJobType(person.getJobType());

                    // 获取人员本月班次(不确定人员一个月是不是只能有一个班次，这里用list查询，取第一个)
                    SwmPersonSchedule queryPersonSchedule = new SwmPersonSchedule();
                    queryPersonSchedule.setIdCard(person.getIdentityCard());
                    queryPersonSchedule.setMonth(monthStr);
                    List<SwmPersonSchedule> personScheduleList = swmPersonScheduleService.findList(queryPersonSchedule);
                    if (personScheduleList != null && !personScheduleList.isEmpty()) {
                        summary.setWorkShift(personScheduleList.get(0).getClasses());
                    }
                    summary.setMonth(monthStr);

                    // 设置统计信息
                    summary.setScheduledDays(BigDecimal.valueOf(scheduledDays));
                    summary.setActualDays(BigDecimal.valueOf(actualDays));
                    summary.setAttendanceRate(attendanceRate);
                    summary.setScheduledHours(totalScheduledHours);
                    summary.setActualHours(totalEffectiveWorkHours);
                    summary.setIdleHours(totalIdleHours);
                    summary.setEfficiency(avgEfficiency);

                    // 计算考勤达成率（修改：使用实际工作时长计算）
                    if (totalScheduledHours.compareTo(BigDecimal.ZERO) > 0) {
                        BigDecimal achievementRate = totalEffectiveWorkHours.divide(totalScheduledHours, 4,
                                RoundingMode.HALF_UP);
                        summary.setAttendanceAchievementRate(achievementRate);
                    } else {
                        summary.setAttendanceAchievementRate(BigDecimal.ZERO);
                    }
                    summary.setCorpCode(person.getCorpCode());
                    summary.setCorpName(person.getCorpName());

                    // 保存记录
                    if (existingSummary != null) {
                        swmAttendanceSummaryService.update(summary);
                    } else {
                        swmAttendanceSummaryService.save(summary);
                    }
                }
                log.info("月考勤统计定时任务执行完成，共处理{}名员工的考勤数据", attendanceByEmployee.size());
            }catch (Exception e){
                XxlJobHelper.log("月考勤统计失败：{}", e.getMessage());
            }finally {
                CorpUtils.removeCurrentCorpCode(null);
                TenantContext.clear();
            }
        }


    }

    /**
     * 月度考勤统计任务V2版本
     * 支持传参数进行灵活统计
     * 
     * 参数格式：
     * 1. 无参数 - 统计本月所有人员的考勤
     * 2. month=2025-08 - 统计指定月份所有人员的考勤
     * 3. month=2025-08;idCard=412825197709304513 - 统计指定月份和单个身份证号人员的考勤
     * 4. month=2025-08;idCards=412825197709304513,110101199001011234 - 统计指定月份和多个身份证号人员的考勤
     * 5. idCards=412825197709304513,110101199001011234 - 统计本月多个身份证号人员的考勤
     * 
     * @author Shawn
     * @date 2025-08-20
     */
    @XxlJob("calculateMonthlyAttendanceV2")
    public void calculateMonthlyAttendanceV2() {
        SwmJobLog jobLog = new SwmJobLog();
        jobLog.setJobName("calculateMonthlyAttendanceV2");
        jobLog.setStartTime(new Date());
        jobLog.setExecuteStatus("1"); // 默认失败
        
        try {
            XxlJobHelper.log("开始执行月度考勤统计任务V2...");
            
            // 保存任务参数
            String jobParam = XxlJobHelper.getJobParam();
            jobLog.setJobParam(jobParam);
            swmJobLogService.save(jobLog);
            jobLog.setIsNewRecord(false);
            
            // TODO: 1. 解析参数（月份、身份证号列表）
            MonthlyAttendanceParams params = parseMonthlyAttendanceParams(jobParam);
            XxlJobHelper.log("解析参数完成 - 月份: {}, 身份证数量: {}", 
                params.targetMonth, params.idCardList.size());
            
            // TODO: 2. 应出勤天数scheduled_days
            calculateAndUpdateScheduledDays(params);
            
            // 统一预加载月度日考勤数据缓存（一次性加载，后续多个指标复用）
            XxlJobHelper.log("开始预加载{}月的日考勤数据缓存，供后续指标计算复用", params.targetMonth);
            Map<String, List<SwmDailyAttendance>> monthlyDataCache = 
                loadMonthlyAttendanceDataCache(params.targetMonth);
            
            // 检查缓存加载结果
            if (monthlyDataCache.isEmpty()) {
                XxlJobHelper.log("{}月日考勤数据缓存为空，跳过后续需要日考勤数据的指标计算", params.targetMonth);
            } else {
                XxlJobHelper.log("{}月日考勤数据缓存加载成功，覆盖{}个员工", params.targetMonth, monthlyDataCache.size());
                
                // TODO: 3. 实际出勤天数actual_days
                calculateAndUpdateActualDays(params, monthlyDataCache);
                
                // TODO: 4. 工效（将来复用缓存） efficiency
                // calculateAndUpdateEfficiency(params, monthlyDataCache);
                
                // TODO: 5. 怠工时长（将来复用缓存）idle_hours
                // calculateAndUpdateIdleHours(params, monthlyDataCache);
            }
            
            // TODO: 6. 应考勤时长 scheduled_hours

            // TODO: 7. 实际工作时长 actual_hours

            // TODO: 8. 实际考勤天数，先不存入表

            // TODO: 9. 本月考勤率，先不存入数据库

            // TODO: 10. 出勤率

            // TODO: 11. 考勤达成率
            
            XxlJobHelper.log("月度考勤统计任务V2执行成功");
            jobLog.setExecuteStatus("0"); // 成功
            
        } catch (Exception e) {
            XxlJobHelper.log("月度考勤统计任务V2执行异常", e);
            jobLog.setExceptionInfo(e.getMessage());
        } finally {
            jobLog.setEndTime(new Date());
            jobLog.setDuration(jobLog.getEndTime().getTime() - jobLog.getStartTime().getTime());
            swmJobLogService.save(jobLog);
        }
    }

    /**
     * 创建每日考勤数据
     */
    @XxlJob("createDailyAttendance")
    public void createDailyAttendance() {
        SwmJobLog jobLog = new SwmJobLog();
        jobLog.setJobName("createDailyAttendance");
        jobLog.setStartTime(new Date());
        jobLog.setExecuteStatus("1"); // 默认失败
        try {
            String jobParam = XxlJobHelper.getJobParam();
            jobLog.setJobParam(jobParam);
            swmJobLogService.save(jobLog);
            jobLog.setIsNewRecord(false);

            XxlJobHelper.log("开始执行每日考勤数据创建任务...");

            // 1. 获取当前日期
            Date today = new Date();

            // 2. 查询所有在职人员
            SwmPerson query = new SwmPerson();
            query.setPersonnelStatus(SwmPerson.PersonStatusEnum.ACTIVE); // 在职状态
            query.setStatus("0");// 正常状态
            // query.setIdentityCard("412825197709304513"); // todo为了测试身份证先写死

            // 测试使用，生产上要删除 begin
            // 您可以像下面这样，直接操作SQL MAP来添加IN条件，而无需修改任何其他模块的代码。
            // 1. 创建一个包含身份证号的列表
            // java.util.List<String> ids = new java.util.ArrayList<>();
            // ids.add("1935182658540556288");
            // ids.add("1935182658850934784");
            // // 2. 将列表添加到查询条件中
            // if (ids != null && !ids.isEmpty()) {
            // query.getSqlMap().getWhere().and("id",
            // com.jeesite.common.mybatis.mapper.query.QueryType.IN, ids);
            // }
            // 测试使用，生产上要删除 end

            List<SwmPerson> activePersons = swmPersonService.findList(query);

            if (activePersons.isEmpty()) {
                XxlJobHelper.log("没有在职人员，无需创建考勤记录");
                jobLog.setExecuteStatus("0"); // 成功
                return;
            }

            int createdCount = 0;
            int updatedCount = 0;

            // 3. 为每个在职人员创建/更新考勤记录
            for (SwmPerson person : activePersons) {
                // 3.1 检查是否已有当天的考勤记录（使用专门的查询方法确保排重）
                SwmDailyAttendance existingAttendance = swmDailyAttendanceService
                        .findByEmployeeIdAndDate(person.getId(), today);

                // 3.2 获取员工的排班信息
                ScheduleInfo scheduleInfo = getScheduleTimeForPerson(person, today);
                if (scheduleInfo == null) {
                    XxlJobHelper.log("员工[{}]{}没有排班信息，跳过创建考勤记录", person.getId(), person.getName());
                    continue;
                }
                SwmScheduleTime scheduleTime = scheduleInfo.scheduleTime;
                String workTimeRange = scheduleTime.getStartTime() + "-" + scheduleTime.getEndTime();

                // 3.3 计算应考勤时长
                BigDecimal scheduledHours = calculateScheduledHours(scheduleTime);

                // 3.4 创建或更新考勤记录
                if (existingAttendance == null) {
                    // 创建新记录
                    SwmDailyAttendance newAttendance = new SwmDailyAttendance();
                    newAttendance.setEmployeeId(person.getId());
                    newAttendance.setEmployeeName(person.getName());
                    newAttendance.setPersonType(person.getPersonType());
                    newAttendance.setAttendanceDate(today);
                    newAttendance.setWorkTimeRange(workTimeRange);
                    newAttendance.setClasses(scheduleInfo.classes); // 设置班次
                    newAttendance.setScheduledHours(scheduledHours);
                    newAttendance.setActualHours(BigDecimal.ZERO); // 默认实际考勤时长为0
                    newAttendance.setIdleHours(BigDecimal.ZERO); // 默认怠工时长为0
                    newAttendance.setDailyEfficiency(BigDecimal.ZERO); // 默认功效为0
                    newAttendance.setDailyAchievementRate(BigDecimal.ZERO); // 默认达成率为0
                    newAttendance.setAttendanceNormal("3"); // 默认未考勤
                    // 设置默认的当前位置为"未知"
                    newAttendance.setCurrentPosition("3"); // 默认未知

                    swmDailyAttendanceService.save(newAttendance);
                    createdCount++;
                } else {
                    // 更新现有记录
                    existingAttendance.setWorkTimeRange(workTimeRange);
                    existingAttendance.setClasses(scheduleInfo.classes); // 设置班次
                    existingAttendance.setScheduledHours(scheduledHours);
                    existingAttendance.setPersonType(person.getPersonType());
                    // todo 调用接口获取怠工时长、考勤是否正常等

                    // 保留原有的实际考勤数据
                    swmDailyAttendanceService.save(existingAttendance);
                    updatedCount++;
                }
            }

            XxlJobHelper.log("每日考勤数据创建任务完成。共创建{}条记录，更新{}条记录", createdCount, updatedCount);
            jobLog.setExecuteStatus("0");
        } catch (Exception e) {
            XxlJobHelper.log("创建每日考勤数据时发生异常", e);
            jobLog.setExceptionInfo(e.getMessage());
        } finally {
            jobLog.setEndTime(new Date());
            jobLog.setDuration(jobLog.getEndTime().getTime() - jobLog.getStartTime().getTime());
            swmJobLogService.save(jobLog);
        }
    }

//    /**
//     * 获取员工的排班信息
//     *
//     * @param person 员工信息
//     * @param date   考勤日期
//     * @return ScheduleInfo 包含排班时间信息和班次名称
//     */
//    private ScheduleInfo getScheduleTimeForPerson(SwmPerson person, Date date) {
//        // 1. 获取当前月份
//        String month = DateUtil.format(date, "yyyy-MM");
//
//        // 2. 查询员工的排班信息
//        List<SwmPersonSchedule> personScheduleList = swmPersonScheduleService
//                .findByIdCardAndMonth(person.getIdentityCard(), month);
////        if (personScheduleList == null || personScheduleList.isEmpty()) {
////            return null;
////        }
//
//        SwmPersonSchedule personSchedule = personScheduleList.get(0);
////        if (personSchedule == null || personSchedule.getClasses() == null) {
////            return null;
////        }
//
//        // 3. 查询班次对应的时间
//        SwmScheduleTime scheduleTimeQuery = new SwmScheduleTime();
//        scheduleTimeQuery.setShiftType(personSchedule.getClasses());
//        List<SwmScheduleTime> scheduleTimeList = swmScheduleTimeService.findList(scheduleTimeQuery);
////        if (scheduleTimeList == null || scheduleTimeList.isEmpty()) {
////            return null;
////        }
//
//        // 返回包含班次信息的对象
//        return new ScheduleInfo(scheduleTimeList.get(0), personSchedule.getClasses());
//    }


    /**
     * 根据人员和日期获取排班时间信息
     * @param person 人员信息
     * @param date 考勤日期
     * @return 排班信息（ScheduleInfo），异常时返回null并打印日志
     */
    private ScheduleInfo getScheduleTimeForPerson(SwmPerson person, Date date) {
        // ========== 1. 入参判空 + 基础日志 ==========
        if (person == null) {
            XxlJobHelper.log("【ERROR】getScheduleTimeForPerson：入参person为null，无法查询排班信息");
            return null;
        }
        if (date == null) {
            XxlJobHelper.log("【WARN】getScheduleTimeForPerson：入参date为null，无法格式化月份");
            return null;
        }

        String personId = person.getId() == null ? "未知ID" : person.getId();
        String personName = person.getName() == null ? "未知姓名" : person.getName();
        String idCard = person.getIdentityCard() == null ? "未知身份证" : person.getIdentityCard();
        String dateStr = DateUtil.format(date, "yyyy-MM-dd");
        XxlJobHelper.log("【INFO】getScheduleTimeForPerson：开始查询排班，人员ID：{}，姓名：{}，身份证：{}，日期：{}",
                personId, personName, idCard, dateStr);

        try {
            // ========== 2. 格式化月份 + 日志 ==========
            String month = DateUtil.format(date, "yyyy-MM");
            if (StringUtils.isEmpty(month)) {
                XxlJobHelper.log("【ERROR】getScheduleTimeForPerson：日期{}格式化月份失败，结果为空", dateStr);
                return null;
            }
            XxlJobHelper.log("【INFO】getScheduleTimeForPerson：考勤日期{}对应的月份：{}", dateStr, month);

            // ========== 3. 查询员工排班信息 + 日志 ==========
            List<SwmPersonSchedule> personScheduleList = null;
            try {
                personScheduleList = swmPersonScheduleService.findByIdCardAndMonth(idCard, month);
            } catch (Exception e) {
                XxlJobHelper.log("【ERROR】getScheduleTimeForPerson：查询员工排班信息失败，身份证：{}，月份：{}，异常：{}",
                        idCard, month, e.getMessage());
                throw e; // 抛出异常，让上层捕获
            }

            // 关键：集合判空（你注释了这行，极易导致IndexOutOfBoundsException）
            if (personScheduleList == null || personScheduleList.isEmpty()) {
                XxlJobHelper.log("【WARN】getScheduleTimeForPerson：员工[{}]{}月份{}无排班信息，排班列表为空",
                        personId, personName, month);
                return null;
            }
            XxlJobHelper.log("【INFO】getScheduleTimeForPerson：查询到员工排班列表，数量：{}", personScheduleList.size());

            // ========== 4. 获取第一个排班记录 + 日志 ==========
            SwmPersonSchedule personSchedule = personScheduleList.get(0);
            if (personSchedule == null) {
                XxlJobHelper.log("【ERROR】getScheduleTimeForPerson：员工[{}]{}排班列表第一个元素为null",
                        personId, personName);
                return null;
            }
            String classes = personSchedule.getClasses();
            XxlJobHelper.log("【INFO】getScheduleTimeForPerson：员工[{}]{}班次信息：{}",
                    personId, personName, classes == null ? "null" : classes);

            if (classes == null) {
                XxlJobHelper.log("【WARN】getScheduleTimeForPerson：员工[{}]{}班次(classes)为null",
                        personId, personName);
                return null;
            }

            // ========== 5. 查询班次对应的时间 + 日志 ==========
            SwmScheduleTime scheduleTimeQuery = new SwmScheduleTime();
            scheduleTimeQuery.setShiftType(classes);
            List<SwmScheduleTime> scheduleTimeListOld = null;
            List<SwmScheduleTime> scheduleTimeList = new ArrayList<>();
            try {
//                scheduleTimeList = swmScheduleTimeService.findList(scheduleTimeQuery);
                scheduleTimeListOld = swmScheduleTimeService.findListSingle(scheduleTimeQuery);
                for (SwmScheduleTime time : scheduleTimeListOld) {
                    if (person.getCorpCode().equals(time.getCorpCode())){
                        scheduleTimeList.add(time);
                    }
                }

            } catch (Exception e) {
                XxlJobHelper.log("【ERROR】getScheduleTimeForPerson：查询班次时间失败，人员：{}，班次：{}，异常：{}，人员租户：{}",
                        person.getName(),classes, e.getMessage(), person.getCorpCode());
                throw e; // 抛出异常，让上层捕获
            }

            // 关键：集合判空（你注释了这行，极易导致IndexOutOfBoundsException）
            if (scheduleTimeList == null || scheduleTimeList.isEmpty()) {
                XxlJobHelper.log("【ERROR】getScheduleTimeForPerson：人员：{}，班次{}无对应的时间信息，时间列表为空",person.getName(), classes);
                return null;
            }
            XxlJobHelper.log("【INFO】getScheduleTimeForPerson：人员：{}，查询到班次{}对应的时间列表，数量：{}",
                    person.getName(),classes, scheduleTimeList.size());

            // ========== 6. 获取第一个班次时间 + 日志 ==========
            SwmScheduleTime scheduleTime = scheduleTimeList.get(0);
            if (scheduleTime == null) {
                XxlJobHelper.log("【ERROR】getScheduleTimeForPerson：人员：{}，班次{}时间列表第一个元素为null",person.getName(), classes);
                return null;
            }

            // ========== 7. 构建返回结果 + 日志 ==========
            ScheduleInfo scheduleInfo = new ScheduleInfo(scheduleTime, classes);
            XxlJobHelper.log("【INFO】getScheduleTimeForPerson：查询完成，员工[{}]{}排班信息：班次={}，时间={}",
                    personId, personName, classes, scheduleTime);
            return scheduleInfo;

        } catch (Exception e) {
            // ========== 全局异常捕获 + 完整堆栈 ==========
            XxlJobHelper.log("【ERROR】getScheduleTimeForPerson：执行失败，人员ID：{}，姓名：{}，异常信息：{}",
                    personId, personName, e.getMessage());
            XxlJobHelper.log("【ERROR】异常堆栈：");
            for (StackTraceElement element : e.getStackTrace()) {
                XxlJobHelper.log(element.toString());
            }
            return null;
        }
    }

    /**
     * 计算应考勤时长
     * 
     * @param scheduleTime 排班时间信息
     * @return 应考勤时长(小时)
     */
    private BigDecimal calculateScheduledHours(SwmScheduleTime scheduleTime) {
        if (scheduleTime == null || scheduleTime.getStartTime() == null || scheduleTime.getEndTime() == null) {
            return BigDecimal.ZERO;
        }

        String workTimeRange = scheduleTime.getStartTime() + "-" + scheduleTime.getEndTime();

        try {
            String[] times = workTimeRange.split("-");
            if (times.length != 2) {
                log.error("工作时间范围格式错误: {}", workTimeRange);
                return BigDecimal.ZERO;
            }

            String startTimeStr = times[0].trim();
            String endTimeStr = times[1].trim();

            // 解析时间 - 使用当天的日期作为基准
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
            Date startTime = sdf.parse(startTimeStr);
            Date endTime = sdf.parse(endTimeStr);

            // 计算时间差(毫秒)
            long diffMillis = endTime.getTime() - startTime.getTime();

            // 转换为小时
            double hours = diffMillis / (1000.0 * 60 * 60);

            // 处理跨日班次的情况(如夜班20:30-03:00)
            if (hours < 0) {
                hours += 24;
                log.debug("检测到跨日班次: {}，计算后的工作时长: {} 小时", workTimeRange, hours);
            } else if (hours > 24) {
                // 超过24小时的班次不合理，可能是配置错误
                log.warn("工作时间范围 {} 计算出的时长超过24小时: {} 小时，可能存在配置错误", workTimeRange, hours);
                return BigDecimal.ZERO;
            }

            // 验证总时长的合理性（一般工作时长应该在1-16小时之间）
            if (hours < 1 || hours > 16) {
                log.warn("班次总时长 {} 计算出的时长 {} 小时可能不合理", workTimeRange, hours);
            }

            // 减去休息时长
            Double restTime = scheduleTime.getRestTime();
            if (restTime != null && restTime > 0) {
                hours -= restTime;
            }

            // 确保最终应考勤时长不为负数
            if (hours < 0) {
                log.warn("班次 {} 减去休息时长后，应考勤时长为负数: {} 小时", workTimeRange, hours);
                hours = 0;
            }

            return BigDecimal.valueOf(hours).setScale(2, RoundingMode.HALF_UP);
        } catch (Exception e) {
            log.error("计算应考勤时长时发生异常，workTimeRange: {}", workTimeRange, e);
            return BigDecimal.ZERO;
        }
    }

    /**
     * 计算两个时间之间的工作时长
     * 
     * @param clockInTime  上班打卡时间
     * @param clockOutTime 下班打卡时间
     * @return 工作时长(小时)
     * @author: Shawn
     * @date: 2025/06/23
     */
    private BigDecimal calculateWorkHoursBetweenTimes(Date clockInTime, Date clockOutTime) {
        if (clockInTime == null || clockOutTime == null) {
            return BigDecimal.ZERO;
        }

        try {
            // 计算时间差(毫秒)
            long diffMillis = clockOutTime.getTime() - clockInTime.getTime();

            // 修复跨夜逻辑：正确处理跨日班次
            // 不能简单地通过时间差为负数来判断跨日，因为可能存在数据异常
            // 应该通过实际的日期来判断是否跨日
            Calendar clockInCal = Calendar.getInstance();
            clockInCal.setTime(clockInTime);

            Calendar clockOutCal = Calendar.getInstance();
            clockOutCal.setTime(clockOutTime);

            // 获取上班和下班的日期部分（忽略时间）
            int clockInDay = clockInCal.get(Calendar.DAY_OF_YEAR);
            int clockInYear = clockInCal.get(Calendar.YEAR);
            int clockOutDay = clockOutCal.get(Calendar.DAY_OF_YEAR);
            int clockOutYear = clockOutCal.get(Calendar.YEAR);

            // 如果时间差为负数，但实际上是跨日班次（下班日期 > 上班日期）
            if (diffMillis < 0 && (clockOutYear > clockInYear ||
                    (clockOutYear == clockInYear && clockOutDay > clockInDay))) {
                // 这种情况不应该发生，可能是数据错误
                log.warn("发现异常的打卡时间：上班时间 {} 晚于下班时间 {}，但日期显示确实是跨日",
                        clockInTime, clockOutTime);
                // 使用绝对时间差
                diffMillis = Math.abs(diffMillis);
            } else if (diffMillis < 0) {
                // 时间差为负数且下班日期 <= 上班日期，这是数据错误
                log.warn("发现错误的打卡时间：下班时间 {} 早于上班时间 {}，返回0小时",
                        clockOutTime, clockInTime);
                return BigDecimal.ZERO;
            }

            // 检查工作时长是否合理（超过48小时可能是数据异常）
            double hours = diffMillis / (1000.0 * 60 * 60);
            if (hours > 48) {
                log.warn("工作时长超过48小时（{}小时），可能存在数据异常。上班时间：{}，下班时间：{}",
                        hours, clockInTime, clockOutTime);
                // 可以选择返回0或者设置一个最大值
                return BigDecimal.ZERO;
            }

            return BigDecimal.valueOf(hours).setScale(2, RoundingMode.HALF_UP);
        } catch (Exception e) {
            log.error("计算打卡工作时长时发生异常，clockInTime: {}, clockOutTime: {}", clockInTime, clockOutTime, e);
            return BigDecimal.ZERO;
        }
    }

    /**
     * 计算日考勤功效
     * 功效 = 实际考勤时长/应该考勤时长
     * @param actualHours   实际考勤时长
     * @param scheduledHours 应考勤时长
     * @return 日考勤功效
     * @author: Shawn
     * @date: 2025/06/23
     */
    private BigDecimal calculateDailyEfficiency(BigDecimal actualHours, BigDecimal scheduledHours) {
        try {
            // 应考勤时长为0或null时，工效为0
            if (scheduledHours == null || scheduledHours.compareTo(BigDecimal.ZERO) == 0) {
                return BigDecimal.ZERO;
            }

            // 实际考勤时长为null时，按0处理
            if (actualHours == null) {
                return BigDecimal.ZERO;
            }

            // 计算工效：实际考勤时长 ÷ 应考勤时长（返回小数形式）
            return actualHours.divide(scheduledHours, 4, RoundingMode.HALF_UP);
        } catch (Exception e) {
            log.error("计算日考勤功效时发生异常，actualHours: {}, scheduledHours: {}", actualHours, scheduledHours, e);
            return BigDecimal.ZERO;
        }
    }

    /**
     * 计算日达成率
     * 达成率 = 实际工作时长 / 应考勤时长
     * 
     * @param effectiveWorkHours 实际工作时长
     * @param scheduledHours     应考勤时长
     * @return 日达成率
     * @author: Shawn
     * @date: 2025/01/27
     */
    private BigDecimal calculateDailyAchievementRate(BigDecimal effectiveWorkHours, BigDecimal scheduledHours) {
        if (scheduledHours == null || scheduledHours.compareTo(BigDecimal.ZERO) <= 0) {
            // 应考勤时长为0或负数，达成率为0
            return BigDecimal.ZERO;
        }

        if (effectiveWorkHours == null) {
            effectiveWorkHours = BigDecimal.ZERO;
        }

        try {
            // 达成率 = 实际工作时长 / 应考勤时长
            BigDecimal achievementRate = effectiveWorkHours.divide(scheduledHours, 4, RoundingMode.HALF_UP);

            // 达成率可能超过1（超额完成），不设置上限
            if (achievementRate.compareTo(BigDecimal.ZERO) < 0) {
                achievementRate = BigDecimal.ZERO;
            }

            return achievementRate.setScale(4, RoundingMode.HALF_UP);
        } catch (Exception e) {
            log.error("计算日达成率时发生异常，effectiveWorkHours: {}, scheduledHours: {}",
                    effectiveWorkHours, scheduledHours, e);
            return BigDecimal.ZERO;
        }
    }

    /**
     * 解析时间对象，支持多种格式
     * 支持ISO 8601格式：2025-06-24T16:00:14.128Z
     * 
     * @param timeObj 时间对象
     * @return Date对象，解析失败返回null
     * @author: Shawn
     * @date: 2025/01/27
     */
    private Date parseTimeObject(Object timeObj) {
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

                // 尝试解析ISO 8601格式：2025-06-24T16:00:14.128Z (UTC时间)
                if (timeStr.contains("T") && timeStr.endsWith("Z")) {
                    // 使用ISO 8601格式解析UTC时间
                    SimpleDateFormat utcFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
                    utcFormat.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));

                    // 如果没有毫秒，添加毫秒部分
                    String isoTimeStr = timeStr;
                    if (!timeStr.contains(".")) {
                        isoTimeStr = timeStr.replace("Z", ".000Z");
                    }

                    try {
                        Date utcDate = utcFormat.parse(isoTimeStr);
                        log.debug("解析UTC时间: {} -> {}", timeStr, utcDate);
                        return utcDate;
                    } catch (Exception e) {
                        // 如果解析失败，尝试不带毫秒的格式
                        SimpleDateFormat utcFormatNoMillis = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
                        utcFormatNoMillis.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));
                        try {
                            String noMillisStr = timeStr.contains(".")
                                    ? timeStr.substring(0, timeStr.lastIndexOf(".")) + "Z"
                                    : timeStr;
                            Date utcDate = utcFormatNoMillis.parse(noMillisStr);
                            log.debug("解析UTC时间(无毫秒): {} -> {}", noMillisStr, utcDate);
                            return utcDate;
                        } catch (Exception e2) {
                            log.warn("ISO 8601时间解析失败: {}", timeStr, e2);
                        }
                    }
                }

                // 尝试解析普通格式：yyyy-MM-dd HH:mm:ss
                if (timeStr.matches("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}")) {
                    return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(timeStr);
                }

                // 尝试解析其他常见格式
                String[] patterns = {
                        "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
                        "yyyy-MM-dd'T'HH:mm:ss'Z'",
                        "yyyy-MM-dd'T'HH:mm:ss.SSS",
                        "yyyy-MM-dd'T'HH:mm:ss",
                        "yyyy-MM-dd HH:mm:ss.SSS",
                        "yyyy/MM/dd HH:mm:ss"
                };

                for (String pattern : patterns) {
                    try {
                        return new SimpleDateFormat(pattern).parse(timeStr);
                    } catch (Exception ignored) {
                        // 继续尝试下一个格式
                    }
                }
            }
        } catch (Exception e) {
            log.error("解析时间对象失败: {}", timeObj, e);
        }

        return null;
    }

    /**
     * 判断当前时间是否在给定的工作时间范围内
     *
     * @param workTimeRange 工作时间范围字符串，如 "08:00-17:00" 或 "18:00-03:00"
     * @return 如果当前时间在范围内，返回true，否则返回false
     */
    private boolean isCurrentTimeInWorkRange(String workTimeRange) {
        if (workTimeRange == null || workTimeRange.trim().isEmpty()) {
            return false;
        }

        try {
            String[] times = workTimeRange.split("-");
            if (times.length != 2) {
                log.warn("无效的工作时间范围格式: {}", workTimeRange);
                return false;
            }

            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
            Date startTime = sdf.parse(times[0].trim());
            Date endTime = sdf.parse(times[1].trim());

            // 获取当前时间的 HH:mm 部分，用于比较
            Date nowTime = sdf.parse(sdf.format(new Date()));

            // 如果结束时间早于开始时间，说明是跨天班次 (如 18:00-03:00)
            if (endTime.before(startTime)) {
                // 对于跨天班次，如果当前时间晚于开始时间(在第一天) 或 早于结束时间(在第二天)，则在范围内
                // 例如：班次18:00-03:00。
                // 当前时间19:00 -> 19:00 > 18:00 (true) -> 在范围内
                // 当前时间02:00 -> 02:00 < 03:00 (true) -> 在范围内
                // 当前时间04:00 -> 04:00 > 18:00(false) && 04:00 < 03:00(false) -> 不在范围内
                return nowTime.after(startTime) || nowTime.before(endTime);
            } else {
                // 对于普通当天班次 (如 08:00-17:00)，当前时间必须在两者之间
                return nowTime.after(startTime) && nowTime.before(endTime);
            }
        } catch (Exception e) {
            log.error("解析或比较工作时间范围时出错: {}", workTimeRange, e);
            return false;
        }
    }

    /**
     * 应用实际考勤时长兜底逻辑
     * 当实际考勤时长 <= 0 时，使用实际工作时长作为兜底
     *
     * @param record                考勤记录
     * @param calculatedActualHours 计算出的实际考勤时长
     * @param context               上下文描述，用于日志记录
     * @return 最终的实际考勤时长
     * @author: Shawn
     * @date: 2025/01/27
     */
    private BigDecimal applyActualHoursFallback(SwmDailyAttendance record, BigDecimal calculatedActualHours,
            String context) {
        if (calculatedActualHours.compareTo(BigDecimal.ZERO) <= 0) {
            BigDecimal effectiveWorkHours = record.getEffectiveWorkHours();
            if (effectiveWorkHours != null && effectiveWorkHours.compareTo(BigDecimal.ZERO) > 0) {
                XxlJobHelper.log("员工[{}]{}{}实际考勤时长 <= 0，使用实际工作时长作为兜底: {} 小时",
                        record.getEmployeeId(), record.getEmployeeName(), context, effectiveWorkHours);
                return effectiveWorkHours;
            } else {
                XxlJobHelper.log("员工[{}]{}{}实际考勤时长 <= 0 且实际工作时长也无效，设为0小时",
                        record.getEmployeeId(), record.getEmployeeName(), context);
                return BigDecimal.ZERO;
            }
        }
        return calculatedActualHours;
    }

    /**
     * 根据新业务规则更新考勤状态
     * 正常：有上下班打卡都不为空并且应考勤时长>=实际考勤时长
     * 异常：上下班打卡记录其中一个有值 或 实际工作时长为有值 或 实际考勤时长为有值
     * 判断顺序：先判断正常，再判断异常，最后保持原状态
     * 
     * @param record 考勤记录（actualHours、effectiveWorkHours等字段已计算完成）
     * @author Shawn
     * @date 2025/01/27
     */
    private void updateAttendanceStatusByNewRule(SwmDailyAttendance record) {
        String originalStatus = record.getAttendanceNormal();
        
        // 如果是休息日，保持状态不变
        if ("2".equals(originalStatus)) {
            XxlJobHelper.log("员工[{}]{} 是休息日，保持考勤状态不变",
                record.getEmployeeId(), record.getEmployeeName());
            return;
        }

        // 1. 优先判断正常条件
        if (isNormalAttendanceCondition(record)) {
            record.setAttendanceNormal("0");
            XxlJobHelper.log("员工[{}]{} 考勤状态更新为 [正常] - 完整打卡且实际考勤时长>=应考勤时长",
                    record.getEmployeeId(), record.getEmployeeName());
            return;
        }

//        // 2. 判断异常条件
//        if (isAbnormalAttendanceCondition(record)) {
//            record.setAttendanceNormal("1");
//            XxlJobHelper.log("员工[{}]{} 考勤状态更新为 [异常] - 满足异常条件",
//                    record.getEmployeeId(), record.getEmployeeName());
//            return;
//        }

        // 2. 判断未考勤条件
        if (isNotAttendanceCondition(record)) {
            record.setAttendanceNormal("3");
            XxlJobHelper.log("员工[{}]{} 考勤状态更新为 [未考勤] - 没有打卡且无工作时长数据",
                    record.getEmployeeId(), record.getEmployeeName());
            return;
        } else {
            record.setAttendanceNormal("1");
        }

        // 4. 其他情况保持原状态
        XxlJobHelper.log("员工[{}]{} 考勤状态保持不变: {}",
                record.getEmployeeId(), record.getEmployeeName(), originalStatus);
    }

    /**
     * 判断是否满足正常考勤条件
     * 条件：clockInDate != null AND clockOutDate != null 
     * AND actualHours >= scheduledHours
     * 注意：scheduledHours 已经是扣除休息时长后的净工作时间
     * 
     * @param record 考勤记录
     * @return true-满足正常条件，false-不满足
     * @author Shawn
     * @date 2025/01/09
     */
    private boolean isNormalAttendanceCondition(SwmDailyAttendance record) {
        Date clockInDate = record.getClockInDate();
        Date clockOutDate = record.getClockOutDate();
        BigDecimal scheduledHours = record.getScheduledHours();
        BigDecimal actualHours = record.getActualHours();

        // 必须有完整的打卡时间
        boolean hasCompleteClockTimes = (clockInDate != null && clockOutDate != null);
        if (!hasCompleteClockTimes) {
            return false;
        }

        // 必须有应考勤时长和实际考勤时长
        if (scheduledHours == null || actualHours == null) {
            return false;
        }

        // 确保应考勤时长不为负数
        if (scheduledHours.compareTo(BigDecimal.ZERO) < 0) {
            scheduledHours = BigDecimal.ZERO;
        }

        // 实际考勤时长 >= 应考勤时长
        // 注意：scheduledHours 现在已经是扣除休息时长后的净工作时间
        // 不需要再次扣除休息时长
        boolean actualGEScheduled = actualHours.compareTo(scheduledHours) >= 0;

        // 详细日志记录（DEBUG级别）
        if (log.isDebugEnabled()) {
            log.debug("员工[{}]{} 正常条件判断详情: clockInDate={}, clockOutDate={}, scheduledHours={}, actualHours={}, 结果={}",
                    record.getEmployeeId(), record.getEmployeeName(),
                    clockInDate != null, clockOutDate != null,
                    scheduledHours, actualHours, actualGEScheduled);
        }

        return actualGEScheduled;
    }

    /**
     * 判断是否满足异常考勤条件
     * 条件：满足任一即为异常
     * 条件A：没有上班打卡 (clockInDate == null)
     * 条件B：没有下班打卡 (clockOutDate == null)
     * 条件C：没有实际工作时长 (effectiveWorkHours == null || effectiveWorkHours <= 0)
     * 条件D：没有实际考勤时长 (actualHours == null || actualHours <= 0)
     * 
     * @param record 考勤记录
     * @return true-满足异常条件，false-不满足
     * @author Shawn
     * @date 2025/01/27
     */
    private boolean isAbnormalAttendanceCondition(SwmDailyAttendance record) {
        Date clockInDate = record.getClockInDate();
        Date clockOutDate = record.getClockOutDate();
        BigDecimal effectiveWorkHours = record.getEffectiveWorkHours();
        BigDecimal actualHours = record.getActualHours();

        // 满足任一条件即为异常
        boolean noClockInDate = (clockInDate == null);
        boolean noClockOutDate = (clockOutDate == null);
        boolean noEffectiveWorkHours = (effectiveWorkHours == null 
                || effectiveWorkHours.compareTo(BigDecimal.ZERO) <= 0);
        boolean noActualHours = (actualHours == null || actualHours.compareTo(BigDecimal.ZERO) <= 0);

        boolean isAbnormal = noClockInDate || noClockOutDate || noEffectiveWorkHours || noActualHours;

        // 详细日志记录（DEBUG级别）
        if (log.isDebugEnabled()) {
            log.debug("员工[{}]{} 异常条件判断详情: 无上班打卡完整时间={}, 无下班打卡完整时间={}, 无实际工作时长={}, 无实际考勤时长={}, 结果={}",
                    record.getEmployeeId(), record.getEmployeeName(),
                    noClockInDate, noClockOutDate, noEffectiveWorkHours, noActualHours, isAbnormal);
        }

        return isAbnormal;
    }

    /**
     * 判断是否满足未考勤条件
     * 条件：clockInDate == null AND clockOutDate == null AND effectiveWorkHours ==
     * null AND actualHours == null
     * 
     * @param record 考勤记录
     * @return true-满足未考勤条件，false-不满足
     * @author Shawn
     * @date 2025/01/27
     */
    private boolean isNotAttendanceCondition(SwmDailyAttendance record) {
        Date clockInDate = record.getClockInDate();
        Date clockOutDate = record.getClockOutDate();
        BigDecimal effectiveWorkHours = record.getEffectiveWorkHours();
        BigDecimal actualHours = record.getActualHours();

        // 4个条件必须全部满足
        boolean noClockRecord = (clockInDate == null && clockOutDate == null);
        boolean noEffectiveWorkHours = (effectiveWorkHours == null
                || effectiveWorkHours.compareTo(BigDecimal.ZERO) <= 0);
        boolean noActualHours = (actualHours == null || actualHours.compareTo(BigDecimal.ZERO) <= 0);

        boolean isNotAttendance = noClockRecord && noEffectiveWorkHours && noActualHours;

        // 详细日志记录（DEBUG级别）
        if (log.isDebugEnabled()) {
            log.debug("员工[{}]{} 未考勤条件判断详情: 无打卡完整时间记录={}, 无实际工作时长={}, 无实际考勤时长={}, 结果={}",
                    record.getEmployeeId(), record.getEmployeeName(),
                    noClockRecord, noEffectiveWorkHours, noActualHours, isNotAttendance);
        }

        return isNotAttendance;
    }

    /**
     * 根据实际打卡时间计算应考勤时长
     * 直接使用上下班打卡时间的时间差作为应考勤时长
     * 
     * @param clockInTime  上班打卡时间
     * @param clockOutTime 下班打卡时间
     * @return 应考勤时长（小时）
     * @author Shawn
     * @date 2025-08-12
     */
    private BigDecimal calculateScheduledHoursByClockTime(Date clockInTime, Date clockOutTime) {
        if (clockInTime == null || clockOutTime == null) {
            return BigDecimal.ZERO;
        }

        try {
            // 计算时间差(毫秒)
            long diffMillis = clockOutTime.getTime() - clockInTime.getTime();

            // 处理跨天班次的情况
            if (diffMillis < 0) {
                // 如果下班时间早于上班时间，说明是跨天班次（如夜班20:00-05:00）
                // 需要加上24小时
                diffMillis += 24 * 60 * 60 * 1000;
                log.debug("检测到跨天班次，上班时间: {}，下班时间: {}", clockInTime, clockOutTime);
            }

            // 转换为小时
            double hours = diffMillis / (1000.0 * 60 * 60);

            // 检查合理性（一般班次不超过16小时）
            if (hours > 16) {
                log.warn("计算的应考勤时长超过16小时: {} 小时，上班: {}，下班: {}",
                    hours, clockInTime, clockOutTime);
            }

            // 确保结果不为负数
            if (hours < 0) {
                log.error("计算的应考勤时长为负数: {} 小时，上班: {}，下班: {}",
                    hours, clockInTime, clockOutTime);
                return BigDecimal.ZERO;
            }

            return BigDecimal.valueOf(hours).setScale(2, RoundingMode.HALF_UP);
        } catch (Exception e) {
            log.error("计算应考勤时长时发生异常，clockInTime: {}, clockOutTime: {}", 
                clockInTime, clockOutTime, e);
            return BigDecimal.ZERO;
        }
    }

    /**
     * 自定义时间范围考勤计算任务，新版日考勤统计V2
     * 支持通过xxl-job参数设置时间范围和身份证列表
     * 
     * 支持两种时间模式：
     * 1. 小时数模式（向后兼容）：
     *    - hours=48                                    计算过去48小时所有人的考勤
     *    - hours=48;idCards=110101199001011234         计算过去48小时指定身份证的考勤
     * 
     * 2. 时间范围模式（推荐）：
     *    - startTime=2025-01-01 00:00:00;endTime=2025-01-31 23:59:59
     *      计算指定时间范围所有人的考勤
     *    - startTime=2025-01-01 00:00:00;endTime=2025-01-31 23:59:59;idCards=450422199502070018
     *      计算指定时间范围指定身份证的考勤
     *    - startTime=2025-01-01 00:00:00;endTime=2025-01-31 23:59:59;idCards=110101199001011234,110101199001015678
     *      计算指定时间范围多个身份证的考勤
     * 
     * @author Shawn
     * @date 2025-08-11
     */
    @XxlJob("calculateAttendanceByTimeRange")
    public void calculateAttendanceByTimeRange() {
        SwmJobLog jobLog = new SwmJobLog();
        jobLog.setJobName("calculateAttendanceByTimeRange");
        Date date = new Date();
        jobLog.setStartTime(date);
        jobLog.setExecuteStatus("1"); // 默认失败

        //解析参数
        String jobParam = XxlJobHelper.getJobParam();
        Map<String, String> paramMap = parseJobParams(jobParam);
        if (paramMap != null){
            String dateStr = paramMap.get("date");
            if (StringUtils.isNotBlank(dateStr)) {
                try {
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                    Date parse = dateFormat.parse(dateStr);
                    date = parse;
                    XxlJobHelper.log("使用指定日期: {}", dateStr);
                }catch (Exception e) {
                    XxlJobHelper.log("传参解析错误{}", e);
                }
            }
        }

        //获取系统所有租户信息
        List<User> corpList = userService.findCorpList(new User());
        if (CollectionUtils.isEmpty(corpList)) {
            XxlJobHelper.log("没有租户信息");
            return;
        }
        //为每个租户都生成排班计划
        for (User user : corpList) {

            String corpCode = user.getCorpCode();
            String corpName = user.getCorpName();


            try {
                //设置当前线程的租户信息
                CorpUtils.setCurrentCorpCode(corpCode, corpName);
                TenantContext.set(corpCode);
                XxlJobHelper.log("开始处理租户：{} ========================", corpCode);

                XxlJobHelper.log("开始执行自定义时间范围考勤计算任务...");

                // 保存任务参数
                jobLog.setJobParam(jobParam);
                swmJobLogService.save(jobLog);
                jobLog.setIsNewRecord(false);

                // 1. 解析参数
                SwmDailyAttendance params = new SwmDailyAttendance();
                params.setAttendanceDate(date);
                // 2. 查询待处理记录
                params.setRandom(new Random().nextInt(1_000_000));
                List<SwmDailyAttendance> records = swmDailyAttendanceService.findList(params);

                if (records.isEmpty()) {
                    XxlJobHelper.log("没有找到待处理的考勤记录");
                    jobLog.setExecuteStatus("0"); // 成功
                    continue;
                }

                // 3. 批量处理记录
                AttendanceResult result = processAttendanceRecords(records,corpCode);

                // 4. 记录处理结果
                logProcessResult(result);

                XxlJobHelper.log("自定义时间范围考勤计算任务执行成功");
                jobLog.setExecuteStatus("0"); // 成功

            } catch (Exception e) {
                XxlJobHelper.log("自定义时间范围考勤计算任务执行异常", e);
                jobLog.setExceptionInfo(e.getMessage());
            } finally {
                jobLog.setEndTime(new Date());
                jobLog.setDuration(jobLog.getEndTime().getTime() - jobLog.getStartTime().getTime());
                swmJobLogService.save(jobLog);
                CorpUtils.removeCurrentCorpCode(null);
                TenantContext.clear();
            }
        }
    }

    /**
     * 补打上下班卡
     *
     * 补卡机制：
     * 上班卡： 首先判断当天的人员是否有未打上班卡，有则判断未打卡的人员安全帽是否进入厂区（信号至少持续三分钟），进入则考勤正常，补打卡。
     * 下班卡：判断当天人员是否有未打下班卡，有则判断未打卡的人员安全帽在当天是否有数据，有则说明今天来了，然后再
     * 判断最近三分钟有没有安全帽数据，没有则说明离开车间，补打下班卡
     *
     * 可以指定参数：date=2025-01-10
     *
     * @author Shawn
     * @date 2025-08-11
     */
    @XxlJob("replacementCard")
    @Transactional(readOnly = false)
    public void replacementCard() {
        Date date = new Date();
        SwmJobLog jobLog = new SwmJobLog();
        jobLog.setJobName("replacementCard");
        jobLog.setStartTime(date);
        jobLog.setExecuteStatus("1"); // 默认失败


        //获取系统所有租户信息
        List<User> corpList = userService.findCorpList(new User());
        if (CollectionUtils.isEmpty(corpList)) {
            XxlJobHelper.log("没有租户信息");
            return;
        }

        for (User corp : corpList) {
            String corpCode = corp.getCorpCode();
            String corpName = corp.getCorpName();

            try {
                CorpUtils.setCurrentCorpCode(corpCode, corpName);
                TenantContext.set(corpCode);
                XxlJobHelper.log("开始处理租户：{} ========================", corpCode);
                // 保存任务参数
                String jobParam = XxlJobHelper.getJobParam();
                jobLog.setJobParam(jobParam);
                swmJobLogService.save(jobLog);
                jobLog.setIsNewRecord(false);

                String requestDate = null;
                if (jobParam != null) {
                    requestDate = jobLog.getJobParam();
                }


                // 1. 处理未打上班卡的数据  clockInTime=null
                processclockInCard(requestDate,corpCode);

                // 2. 处理未打下班卡的数据 clockOutTime=null
                processclockOutCard(requestDate,corpCode);

                jobLog.setExecuteStatus("0");

            } catch (Exception e) {
                jobLog.setExceptionInfo(e.getMessage());
            } finally {
                jobLog.setEndTime(new Date());
                jobLog.setDuration(jobLog.getEndTime().getTime() - jobLog.getStartTime().getTime());
                swmJobLogService.save(jobLog);
                CorpUtils.removeCurrentCorpCode(null);
                TenantContext.clear();
            }
        }
    }


    /**
     * 批量补上班卡
     * 上班卡： 首先判断当天的人员是否有未打上班卡，有则判断未打卡的人员安全帽是否进入厂区（信号至少持续三分钟），进入则考勤正常，补打卡。
     *
     * @return 处理结果统计
     */
    @Transactional(readOnly = false)
    public void processclockInCard(String requestDate,String corpCode ) {
        XxlJobHelper.log("开始执行上班卡补卡任务...................");

        //设置请求参数，只查当天的数据
        // 1. 查询当天所有的打卡记录
        String date = DateUtils.getDate();
        if(StringUtils.isNotBlank(requestDate)){
            date = requestDate;
        }
        Date nowDate = new Date();
        Integer random = new Random().nextInt(1_000_000);
        List<SwmDailyAttendance> records = swmDailyAttendanceService.findClockInCardList(date,random);
//        List<SwmDailyAttendance> records = queryPendingAttendanceRecords(params);
        XxlJobHelper.log("上班卡查询范围{}，{}，条数{}",date,date,records.size());

        if (records.isEmpty()) {
            XxlJobHelper.log("没有找到待处理的考勤记录");
            return;
        }

        List<SwmDailyAttendance> onlineDevices = Collections.synchronizedList(new ArrayList<>());
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        for (SwmDailyAttendance item : records) {
            if (item.getClockInDate() == null) {
                CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                    try {
                        String deviceId = item.getDeviceId();
                        Date clockStartTime = item.getClockStartTime();
                        String classes = item.getClasses();
                        if (clockStartTime != null){
                            //看看当前时间是否在应该打卡时间范围之内
                            String startTime = DateUtil.formatDateTime(clockStartTime);
                            if("3".equals( classes)){
                                startTime = DateUtil.formatDateTime(DateUtil.offsetHour(clockStartTime, -7));
                            }
                            String endTime = DateUtil.formatDateTime(DateUtil.offsetHour(clockStartTime, 16));
                            String clockInTimeStr = getLast3MinutesBluetoothCount(deviceId, startTime, endTime,item.getIdentityCard(),corpCode);
                            if (StringUtils.isNotEmpty(clockInTimeStr)) {
                                // 统一去掉毫秒（如果有）
                                Instant instant = Instant.parse(clockInTimeStr);
                                // 解析时间
                                ZoneId zoneId = ZoneId.systemDefault();
                                LocalDateTime clockInDateTime = LocalDateTime.ofInstant(instant, zoneId);
                                if (clockInDateTime.getHour() == 0 && clockInDateTime.getMinute() == 0 && clockInDateTime.getSecond() == 0 && instant.toString().contains("T24")) {
                                    clockInDateTime = clockInDateTime.withHour(23).withMinute(59).withSecond(59);
                                }
                                Date clockInDate = Date.from(clockInDateTime.atZone(zoneId).toInstant());
                                item.setClockInDate(clockInDate);
                                item.setClockInTime(clockInDate);
                                onlineDevices.add(item);
                                XxlJobHelper.log("上班补卡人员：{}", item.getEmployeeName());
                            }
                        }
                    } catch (Exception e) {
                        log.error("处理设备 {} 下班补卡失败", item.getDeviceId(), e);
                    }
                }, swmExecutor);
                futures.add(future);
            }
        }

        // 等待全部执行完
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        //批量更新
        List<List<SwmDailyAttendance>> lists = BatchOperationsUtil.batchCutting(onlineDevices, 50);
        for (List<SwmDailyAttendance> list : lists) {
            swmDailyAttendanceService.updateBatch(list);
        }
    }

    /**
     * 批量补下班卡
     *
    1.接受不到蓝牙信号持续3分钟，补偿打下班卡1次，之后当天不再触发第二次（再次触发的条件：当天再次接收到有效信号后，
    可再次触发下班打卡补偿机制）
    2.补偿打卡机制不影响信标打卡机制，信标打卡依旧可以持续更新下班打卡时间
     */
    @Transactional(readOnly = false)
    public void processclockOutCard(String requestDate,String corpCode) {

        XxlJobHelper.log("开始执行下班卡补卡任务............................");

        //设置请求参数，只查当天的数据
        String nowDate = DateUtils.getDate();
        if(StringUtils.isNotBlank(requestDate)){
            nowDate = requestDate;
        }
        String yestDay = LocalDate.parse(nowDate).minusDays(1).toString();
        // 1. 查询两天所有的打卡记录
        Integer random = new Random().nextInt(1_000_000);
        List<SwmDailyAttendance> records = swmDailyAttendanceService.findClockOutCardList(yestDay, nowDate,random,corpCode);
        //List<SwmDailyAttendance> records = queryPendingAttendanceRecords(params);
        XxlJobHelper.log("下班卡查询范围{}，{}，条数{}", yestDay, nowDate, records.size());

        if (records.isEmpty()) {
            XxlJobHelper.log("没有找到待处理的考勤记录");
            return;
        }

        List<SwmDailyAttendance> clockOutRecords = new ArrayList<>();

        Date now = new Date();
        Date threeMinutesAgo = DateUtil.offsetMinute(now, -10);
        String startTime = DateUtil.formatDateTime(threeMinutesAgo);
        String endTime = DateUtil.formatDateTime(now);

        String startDay = DateUtil.formatDateTime(DateUtil.beginOfDay(now));
        String nowDay = DateUtil.formatDateTime(now);


        for (SwmDailyAttendance item : records) {
            try {
                String deviceId = item.getDeviceId();


                //1.先判断是否有打上班卡，没有打则跳过
                if (item.getClockInDate() == null) {
                    continue;
                }

                //2.判断今天有没有数据，没有则跳过
                Integer dayCount = getLast3MinutesBluetoothCountByClockOut(deviceId,item.getIdentityCard(), startDay, nowDay,corpCode);
                if (dayCount == null || dayCount == 0) {
                    continue;
                }

                // 3. 查询最近3分钟蓝牙信号
                Integer count = getLast3MinutesBluetoothCountByClockOut(deviceId, item.getIdentityCard(),startTime, endTime,corpCode);
                // ============= 【A. 有信号 → 重置补偿状态】 =============
                if (count != null && count > 0) {
                    // 说明员工又出现了 → 补偿机制恢复可再次触发
                    item.setPendingClockOutCompensate(false);
                    // 这里不做下班打卡动作，因为信标机制会更新
                    clockOutRecords.add( item);
                    continue;
                }
                // ============= 【B. 无信号 → 判断是否要补卡】 =============
                // 无信号超过3分钟，但之前已经补偿过但未恢复 → 不能再补
                if (Boolean.TRUE.equals(item.isPendingClockOutCompensate())) {
                    continue;
                }

                // 无信号，未补偿过 → 触发补卡
                if (count != null && count == 0) {
                    // 补偿下班卡
                    ZoneId zoneId = ZoneId.systemDefault();
                    // 当前时间
                    LocalDateTime nowDateTime = LocalDateTime.now(zoneId);
                    // 如果极端情况下出现 24:00:00（理论上不会，但兜底）
                    if (nowDateTime.getHour() == 0 && nowDateTime.getMinute() == 0 && nowDateTime.getSecond() == 0) {
                        // 统一压成 23:59:59（不跨天）
                        nowDateTime = nowDateTime.minusSeconds(1);
                    }
                    Date clockOutDate = Date.from(nowDateTime.atZone(zoneId).toInstant());
                    item.setClockOutDate(clockOutDate);
                    item.setClockOutTime(clockOutDate);
                    // 标记今天已补偿
                    item.setPendingClockOutCompensate(true);
                    clockOutRecords.add(item);
                    XxlJobHelper.log("下班补卡人员：{}", item.getEmployeeName());
                }
            } catch (Exception e) {
                log.error("处理人员 {} 下班补卡失败", item.getEmployeeName(), e);
            }
        }
        //批量更新
        List<List<SwmDailyAttendance>> lists = BatchOperationsUtil.batchCutting(clockOutRecords, 50);
        for (List<SwmDailyAttendance> list : lists) {
            swmDailyAttendanceService.updateBatch(list);
        }
    }


    /**
     * 查询打卡时间范围内是否有数据（用于下班卡）
     */
    private Integer getLast3MinutesBluetoothCountByClockOut(String deviceId,String idCard, String start, String end,String corpCode) {

        Integer res  = null;

        try {
            String sql = "SELECT count(1) FROM " + dbname + ".external_coordinate_data_" + deviceId + "_" + idCard +
                    " WHERE time BETWEEN '" + start + "' AND '" + end + "'";

            log.info("查询最近 10 分钟 TDengine 记录数量 SQL: {}", sql);

            R<JSONObject> result = tdengineService.executeTDengineSQLByXXJOB(sql,corpCode);
            XxlJobHelper.log("下班卡查询 TDengine SQL: {}", sql);

            if (result.getCode() == R.SUCCESS && result.getData() != null) {
                JSONArray rows = result.getData().getJSONArray("data");
                if (rows != null && !rows.isEmpty()) {
                    JSONArray firstRow = (JSONArray) rows.get(0);
                    Object val = firstRow.get(0);
                    res = Integer.parseInt(val.toString());
                    return res;
                }
            }
        } catch (Exception e) {
            log.error("查询 TDengine 失败，deviceId={}", deviceId, e);
        }

        return res;
    }

    /**
     * 查询打卡时间范围内的第一条数据（用于上班卡）
     */
    private String getLast3MinutesBluetoothCount(String deviceId, String start, String end,String idCard,String corpCode) {

        try {
            String tableName = dbname + ".external_coordinate_data_" + deviceId + "_" + idCard;

            String sql = "SELECT time  FROM " + tableName +
                    " WHERE time BETWEEN '" + start + "' AND '" + end + "'" +
                    " ORDER BY time ASC LIMIT 1";

            log.info("查询时间范围内第一条 TDengine 记录 SQL: {}", sql);

            R<JSONObject> result = tdengineService.executeTDengineSQLByXXJOB(sql,corpCode);
            XxlJobHelper.log("上班卡查询 TDengine SQL: {}", sql);

            if (result.getCode() == R.SUCCESS && result.getData() != null) {
                JSONArray rows = result.getData().getJSONArray("data");
                if (rows != null && !rows.isEmpty()) {
                    JSONArray firstRow = rows.getJSONArray(0);
                    Object timeVal = firstRow.get(0);
                    return timeVal.toString();
                }
            }
        } catch (Exception e) {
            log.error("查询 TDengine 失败，deviceId={}", deviceId, e);
        }

        return null;
    }

    /**
     * 考勤计算参数类
     * 
     * @author Shawn
     * @date 2025-08-12
     */
    private static class AttendanceParams {
        // 模式标识：true-使用时间范围模式，false-使用小时数模式
        boolean useTimeRange = false;
        
        // 小时数模式参数
        int hours = 24; // 默认24小时
        
        // 时间范围模式参数
        Date startTime;
        Date endTime;
        
        // 公共参数
        List<String> idCardList = new ArrayList<>();
    }

    /**
     * 考勤处理结果类
     * 
     * @author Shawn
     * @date 2025-08-12
     */
    private static class AttendanceResult {
        int successCount = 0;
        int skipCount = 0;
        int failCount = 0;
        int totalCount = 0;
    }

    /**
     * 解析任务参数
     * 支持两种模式：
     * 1. 小时数模式：hours=48 或直接传数字
     * 2. 时间范围模式：startTime=2025-01-01 00:00:00;endTime=2025-01-31 23:59:59
     * 
     * @return 解析后的参数对象
     * @author Shawn
     * @date 2025-08-12
     */
    private AttendanceParams parseAttendanceParams() {
        AttendanceParams params = new AttendanceParams();
        String jobParam = XxlJobHelper.getJobParam();
        SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        
        if (StringUtils.isNotBlank(jobParam)) {
            try {
                // 支持分号分隔的多个参数
                String[] paramArray = jobParam.split(";");
                for (String param : paramArray) {
                    param = param.trim();
                    
                    // 解析开始时间（时间范围模式）
                    if (param.startsWith("startTime=")) {
                        String timeStr = param.substring("startTime=".length()).trim();
                        try {
                            params.startTime = dateTimeFormat.parse(timeStr);
                            params.useTimeRange = true;
                            XxlJobHelper.log("解析开始时间: {}", timeStr);
                        } catch (Exception e) {
                            XxlJobHelper.log("开始时间格式错误: {}，正确格式: yyyy-MM-dd HH:mm:ss", timeStr);
                            throw new IllegalArgumentException("开始时间格式错误: " + timeStr);
                        }
                    }
                    
                    // 解析结束时间（时间范围模式）
                    else if (param.startsWith("endTime=")) {
                        String timeStr = param.substring("endTime=".length()).trim();
                        try {
                            params.endTime = dateTimeFormat.parse(timeStr);
                            params.useTimeRange = true;
                            XxlJobHelper.log("解析结束时间: {}", timeStr);
                        } catch (Exception e) {
                            XxlJobHelper.log("结束时间格式错误: {}，正确格式: yyyy-MM-dd HH:mm:ss", timeStr);
                            throw new IllegalArgumentException("结束时间格式错误: " + timeStr);
                        }
                    }
                    
                    // 解析小时数参数（仅在非时间范围模式下生效）
                    else if (!params.useTimeRange && (param.startsWith("hours=") || param.matches("\\d+"))) {
                        String hoursStr = param;
                        if (param.contains("=")) {
                            String[] keyValue = param.split("=");
                            if (keyValue.length == 2 && "hours".equalsIgnoreCase(keyValue[0].trim())) {
                                hoursStr = keyValue[1].trim();
                            }
                        }
                        params.hours = Integer.parseInt(hoursStr);
                        
                        if (params.hours <= 0) {
                            XxlJobHelper.log("小时数必须大于0，使用默认值24小时");
                            params.hours = 24;
                        } else if (params.hours > 720) { // 限制最大30天
                            XxlJobHelper.log("小时数超过最大限制（720小时/30天），设置为720小时");
                            params.hours = 720;
                        }
                    }
                    
                    // 解析身份证参数（两种模式都支持）
                    else if (param.startsWith("idCards=")) {
                        String idCardsStr = param.substring("idCards=".length()).trim();
                        if (!idCardsStr.isEmpty()) {
                            String[] idCards = idCardsStr.split(",");
                            for (String idCard : idCards) {
                                idCard = idCard.trim();
                                // 验证身份证格式（15位或18位）
                                if (idCard.matches("\\d{15}|\\d{17}[\\dXx]")) {
                                    params.idCardList.add(idCard);
                                } else {
                                    XxlJobHelper.log("身份证格式不正确，跳过: {}", idCard);
                                }
                            }
                        }
                    }
                }
                
                // 验证时间范围模式的参数完整性
                if (params.useTimeRange) {
                    if (params.startTime == null || params.endTime == null) {
                        throw new IllegalArgumentException("使用时间范围模式时，startTime和endTime都必须提供");
                    }
                    if (params.startTime.after(params.endTime)) {
                        throw new IllegalArgumentException("开始时间不能晚于结束时间");
                    }
                    // 限制时间范围不超过1年
                    long diffDays = (params.endTime.getTime() - params.startTime.getTime()) / (1000 * 60 * 60 * 24);
                    if (diffDays > 365) {
                        throw new IllegalArgumentException("时间范围不能超过365天");
                    }
                }
                
            } catch (IllegalArgumentException e) {
                XxlJobHelper.log("参数验证失败: {}", e.getMessage());
                throw e;
            } catch (Exception e) {
                XxlJobHelper.log("参数解析失败: {}，错误: {}", jobParam, e.getMessage());
                XxlJobHelper.log("参数格式示例：");
                XxlJobHelper.log("  小时数模式: hours=48;idCards=110101199001011234");
                XxlJobHelper.log("  时间范围模式: startTime=2025-01-01 00:00:00;endTime=2025-01-31 23:59:59;idCards=110101199001011234");
                params.hours = 24;
                params.useTimeRange = false;
            }
        }
        
        // 根据模式计算最终的时间范围
        if (params.useTimeRange) {
            // 时间范围模式：直接使用提供的时间
            XxlJobHelper.log("使用时间范围模式");
        } else {
            // 小时数模式：根据小时数计算时间范围
            params.endTime = new Date();
            Calendar cal = Calendar.getInstance();
            cal.setTime(params.endTime);
            cal.add(Calendar.HOUR_OF_DAY, -params.hours);
            params.startTime = cal.getTime();
            XxlJobHelper.log("使用小时数模式，过去 {} 小时", params.hours);
        }
        
        // 记录最终的时间范围
        XxlJobHelper.log("最终时间范围: {} 至 {}", 
            dateTimeFormat.format(params.startTime), 
            dateTimeFormat.format(params.endTime));
        
        if (!params.idCardList.isEmpty()) {
            XxlJobHelper.log("指定身份证数量: {}个", params.idCardList.size());
            if (params.idCardList.size() <= 10) {
                XxlJobHelper.log("身份证列表: {}", String.join(", ", params.idCardList));
            }
        } else {
            XxlJobHelper.log("处理所有人员");
        }
        
        return params;
    }

    /**
     * 查询待处理的考勤记录
     * 
     * @param params 查询参数
     * @return 待处理记录列表
     * @author Shawn
     * @date 2025-08-12
     */
    private List<SwmDailyAttendance> queryPendingAttendanceRecords(AttendanceParams params) {
        SwmDailyAttendance query = new SwmDailyAttendance();
        query.setStatus("0"); // 只查询未处理的记录
        
        // 使用create_date作为时间范围过滤（通过SQL条件）
        SimpleDateFormat sqlDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        query.getSqlMap().getWhere()
            .and("create_date", com.jeesite.common.mybatis.mapper.query.QueryType.GTE, 
                sqlDateFormat.format(params.startTime))
            .and("create_date", com.jeesite.common.mybatis.mapper.query.QueryType.LTE, 
                sqlDateFormat.format(params.endTime));
        
        // 如果指定了身份证，添加身份证过滤
        if (!params.idCardList.isEmpty()) {
            query.getSqlMap().getWhere().and("identity_card", 
                com.jeesite.common.mybatis.mapper.query.QueryType.IN, params.idCardList);
        }
        
        List<SwmDailyAttendance> records = swmDailyAttendanceService.findList(query);
        XxlJobHelper.log("查询到 {} 条待处理记录", records.size());
        
        return records;
    }

    /**
     * 批量处理考勤记录
     * 
     * @param records 待处理记录列表
     * @return 处理结果统计
     * @author Shawn
     * @date 2025-08-12
     */
    private AttendanceResult processAttendanceRecords(List<SwmDailyAttendance> records,String corpCode) {
        AttendanceResult result = new AttendanceResult();
        result.totalCount = records.size();
        
        for (SwmDailyAttendance record : records) {
            try {
                boolean processed = processAttendanceRecord(record,corpCode);
                if (processed) {
                    result.successCount++;
                } else {
                    result.skipCount++;
                }
            } catch (Exception e) {
                result.failCount++;
                XxlJobHelper.log("处理员工[{}]{}的记录失败: {}",
                    record.getEmployeeId(), record.getEmployeeName(), e.getMessage());
            }
        }
        
        return result;
    }

    /**
     * 处理单条考勤记录
     * 
     * @param record 考勤记录
     * @return 是否成功处理
     * @author Shawn
     * @date 2025-08-12
     */
    private boolean processAttendanceRecord(SwmDailyAttendance record,String corpCode) {
        boolean updated = false;
        
        // 计算实际考勤时长 - 修改为工作区域时长
        // 新逻辑：实际考勤时长 = 上下班打卡时间范围内的工作区域活动时长
        // @author: Shawn
        // @date: 2025-08-20
        if (shouldCalculateActualHours(record)  || shouldCalculateActualHoursOther(record)) {
            BigDecimal workAreaHours = calculateWorkAreaHours(record,corpCode);
            if (workAreaHours != null) {
                record.setActualHours(workAreaHours);
                updated = true;
                XxlJobHelper.log("员工[{}]{}实际考勤时长更新为工作区域时长: {} 小时",
                    record.getEmployeeId(), record.getEmployeeName(), workAreaHours);
            }
        }
        
        // 计算实际工作时长（使用当天24小时工作区域时长）
        if (shouldCalculateWorkHours(record)) {
            BigDecimal workHours = calculateWorkAreaHours24h(record,corpCode);
            if (workHours != null) {
                // 兜底逻辑：如果24小时工作区域时长小于实际考勤时长，使用实际考勤时长作为兜底
                if (record.getActualHours() != null && 
                    workHours.compareTo(record.getActualHours()) < 0) {
                    
                    XxlJobHelper.log("员工[{}]{}计算的24小时工作区域时长{}小时小于实际考勤时长{}小时，使用实际考勤时长作为兜底", 
                        record.getEmployeeId(), record.getEmployeeName(), 
                        workHours, record.getActualHours());
                    
                    record.setEffectiveWorkHours(record.getActualHours());
                } else {
                    record.setEffectiveWorkHours(workHours);
                }
                
                updated = true;
                XxlJobHelper.log("员工[{}]{}最终实际工作时长: {} 小时",
                    record.getEmployeeId(), record.getEmployeeName(), 
                    record.getEffectiveWorkHours());
            }
        }

        // 如果实际考勤时长为0 ,判断工作区时长不为0 则实际考勤时长为工作区时长
        if ((record.getActualHours() != null && record.getActualHours().compareTo(BigDecimal.ZERO) == 0 ) &&
                (record.getEffectiveWorkHours() != null && record.getEffectiveWorkHours().compareTo(BigDecimal.ZERO) > 0)){
            record.setActualHours(record.getEffectiveWorkHours());
            updated = true;
            XxlJobHelper.log("实际考勤时长为0,工作区时长不为0--员工[{}]{}最终实际工作时长: {} 小时",
                    record.getEmployeeId(), record.getEmployeeName(),
                    record.getEffectiveWorkHours());
        }

        // 计算怠工时长
        if (shouldCalculateWorkHours(record)) {
            if(record.getActualHours() != null && record.getActualHours().compareTo(BigDecimal.ZERO) == 0){
                record.setIdleHours(BigDecimal.ZERO);
                updated = true;
                XxlJobHelper.log("员工[{}]{},实际工作时长为0,怠工时长也为0",
                        record.getEmployeeId(), record.getEmployeeName());
            }else {
                BigDecimal idleHours = calculateIdleAreaHours(record,corpCode);
                if (idleHours != null) {
                    record.setIdleHours(idleHours);
                    updated = true;
                    XxlJobHelper.log("员工[{}]{}怠工时长更新为: {} 小时",
                            record.getEmployeeId(), record.getEmployeeName(), idleHours);
                }
            }
        }

        
        // 如果有更新，标记为已处理并保存
        if (updated) {
            // 计算应考勤时长（如果还没有值）
            if (record.getScheduledHours() == null || record.getScheduledHours().compareTo(BigDecimal.ZERO) <= 0) {
                if (StringUtils.isNotBlank(record.getWorkTimeRange())) {
                    BigDecimal scheduledHours = calculateScheduledHoursFromWorkTimeRange(record.getWorkTimeRange());
                    record.setScheduledHours(scheduledHours);
                    XxlJobHelper.log("员工[{}]{}应考勤时长计算为: {} 小时",
                        record.getEmployeeId(), record.getEmployeeName(), scheduledHours);
                }
            }
            
            // 计算日考勤功效
            // 功效 = 实际考勤时长/应该考勤时长
            // @author: Shawn
            // @date: 2025/01/13
            BigDecimal dailyEfficiency = calculateDailyEfficiency(record.getActualHours(), record.getScheduledHours());
            record.setDailyEfficiency(dailyEfficiency);
            XxlJobHelper.log("员工[{}]{}日考勤功效计算: 实际考勤时长{}/应该考勤时长{}，等于={}",
                record.getEmployeeId(), record.getEmployeeName(),
                record.getActualHours(), record.getScheduledHours(), dailyEfficiency);
            
            // 计算日达成率
            // 达成率 = 实际工作时长 / 应考勤时长
            // @author: Shawn
            // @date: 2025/01/13
            BigDecimal dailyAchievementRate = calculateDailyAchievementRate(record.getEffectiveWorkHours(), record.getScheduledHours());
            record.setDailyAchievementRate(dailyAchievementRate);
            XxlJobHelper.log("员工[{}]{}日达成率计算: {}小时 / {}小时 = {}",
                record.getEmployeeId(), record.getEmployeeName(),
                record.getEffectiveWorkHours(), record.getScheduledHours(), dailyAchievementRate);
            
            // 更新考勤状态逻辑 - 使用新的业务规则
            // @author: Shawn
            // @date: 2025/01/13
            updateAttendanceStatusByNewRule(record);
            
            markAsProcessed(record);
            swmDailyAttendanceService.update(record);
            return true;
        } else {
            XxlJobHelper.log("员工[{}]{}缺少必要数据，跳过处理",
                record.getEmployeeId(), record.getEmployeeName());
            return false;
        }
    }

    /**
     * 判断是否需要计算实际考勤时长
     * 
     * @param record 考勤记录
     * @return 是否需要计算
     * @author Shawn
     * @date 2025-08-12
     */
    private boolean shouldCalculateActualHours(SwmDailyAttendance record) {
        return record.getClockInDate() != null && record.getClockOutDate() != null;
    }

    /**
     * 判断是否需要计算实际考勤时长（上下班打卡时间不全，且应考勤时长>0）
     * 打卡不全场景：有上班无下班、有下班无上班、都无
     */
    private boolean shouldCalculateActualHoursOther(SwmDailyAttendance record) {
        // 1. 打卡时间不全（核心判断）
        boolean isClockIncomplete = (record.getClockInDate() == null || record.getClockOutDate() == null);
        // 2. 应考勤时长>0（业务前提，避免无意义计算）
        boolean isScheduledHoursValid = record.getScheduledHours() != null
                && record.getScheduledHours().compareTo(BigDecimal.ZERO) > 0;

        return isClockIncomplete && isScheduledHoursValid;
    }

    /**
     * 计算并更新实际考勤时长
     * 
     * @param record 考勤记录
     * @author Shawn
     * @date 2025-08-12
     */
    private void calculateAndUpdateActualHours(SwmDailyAttendance record) {
        BigDecimal actualHours = calculateScheduledHoursByClockTime(
            record.getClockInTime(), record.getClockOutTime());
        record.setActualHours(actualHours);
        XxlJobHelper.log("员工[{}]{}实际考勤时长更新为: {} 小时",
            record.getEmployeeId(), record.getEmployeeName(), actualHours);
    }

    /**
     * 标记记录为已处理
     * 
     * @param record 考勤记录
     * @author Shawn
     * @date 2025-08-12
     */
    private void markAsProcessed(SwmDailyAttendance record) {
        record.setStatus("1");
        record.setUpdateDate(new Date());
    }

    /**
     * 记录处理结果
     * 
     * @param result 处理结果
     * @author Shawn
     * @date 2025-08-12
     */
    private void logProcessResult(AttendanceResult result) {
        XxlJobHelper.log("考勤处理完成 - 总计: {}条，成功: {}条，跳过: {}条，失败: {}条",
            result.totalCount, result.successCount, result.skipCount, result.failCount);
    }

    /**
     * 考勤生成参数类
     * @author Shawn
     * @date 2025-08-12
     */
    private static class AttendanceGenerationParams {
        Date targetDate;
        String idCard;  // 单个身份证（向后兼容）
        List<String> idCardList = new ArrayList<>();  // 多个身份证列表
    }
    
    /**
     * 排班信息包装类
     * @author Shawn
     * @date 2025-08-19
     */
    private static class ScheduleInfo {
        SwmScheduleTime scheduleTime;
        String classes; // 班次名称
        
        ScheduleInfo(SwmScheduleTime scheduleTime, String classes) {
            this.scheduleTime = scheduleTime;
            this.classes = classes;
        }
    }

    /**
     * 考勤生成结果类
     * @author Shawn
     * @date 2025-08-12
     */
    private static class AttendanceGenerationResult {
        int createdCount = 0;
        int updatedCount = 0;
        int skippedCount = 0;
        int failCount = 0;
    }

    /**
     * 处理结果枚举
     * @author Shawn
     * @date 2025-08-12
     */
    private enum ProcessResult {
        CREATED, UPDATED, SKIPPED, FAILED
    }

    /**
     * 创建每日考勤数据V2版本
     * 支持参数化生成考勤记录，默认生成明天的考勤数据
     * 
     * 参数格式：
     * - 无参数 - 生成明天所有在职人员的考勤
     * - date=2025-01-10 - 生成指定日期所有在职人员的考勤
     * - idCard=450422199502070018 - 生成指定身份证明天的考勤（向后兼容）
     * - idCards=450422199502070018 - 生成单个身份证明天的考勤
     * - idCards=450422199502070018,320106198801011234 - 批量生成多个身份证明天的考勤
     * - date=2025-01-10,idCard=450422199502070018 - 生成指定日期和身份证的考勤
     * - date=2025-01-10,idCards=450422199502070018,320106198801011234 - 生成指定日期多个身份证的考勤
     * 
     * 注意事项：
     * - idCard 和 idCards 参数同时存在时，会合并处理（自动去重）
     * - 只处理在职状态（ACTIVE）且状态正常（status=0）的人员
     * - 如果指定的身份证对应人员不存在或状态异常，会跳过并记录日志
     * - 应考勤时长 = 班次总时长 - 休息时长（净工作时间）
     * 
     * @author Shawn
     * @date 2025-01-09
     */
    @XxlJob("createDailyAttendanceV2")
    public void createDailyAttendanceV2() {
        SwmJobLog jobLog = initJobLog("createDailyAttendanceV2");


        //获取系统所有租户信息
        List<User> corpList = userService.findCorpList(new User());
        if (CollectionUtils.isEmpty(corpList)) {
            XxlJobHelper.log("没有租户信息");
            return;
        }

        //为每个租户都生成排班计划
        for (User user : corpList) {

            String corpCode = user.getCorpCode();
            String corpName = user.getCorpName();


            try {
                //设置当前线程的租户信息
                CorpUtils.setCurrentCorpCode(corpCode, corpName);
                TenantContext.set(corpCode);
                XxlJobHelper.log("开始处理租户：{} ========================", corpCode);

                // 1. 解析参数
                AttendanceGenerationParams params = parseAttendanceGenerationParams();

                // 2. 查询目标人员
                List<SwmPerson> targetPersons = queryTargetPersons(params,corpCode);
                if (targetPersons.isEmpty()) {
                    XxlJobHelper.log("没有需要处理的在职人员");
                    jobLog.setExecuteStatus("0");
                    continue;
                }

                // 3. 生成考勤记录
                AttendanceGenerationResult result = generateAttendanceRecords(targetPersons, params.targetDate,corpCode);

                // 4. 输出统计结果
                logGenerationResult(result, targetPersons.size());

                jobLog.setExecuteStatus("0"); // 成功

            } catch (Exception e) {
                XxlJobHelper.log("创建每日考勤数据V2时发生异常", e);
                jobLog.setExceptionInfo(e.getMessage());
            } finally {
                saveJobLog(jobLog);
                CorpUtils.removeCurrentCorpCode(null);
                TenantContext.clear();
            }
        }
    }

    /**
     * 解析任务参数
     * @param jobParam 参数字符串，格式：key1=value1,key2=value2
     * @return 参数Map
     */
    private Map<String, String> parseJobParams(String jobParam) {
        Map<String, String> params = new HashMap<>();
        if (StringUtils.isNotBlank(jobParam)) {
            String[] pairs = jobParam.split(",");
            for (String pair : pairs) {
                String[] keyValue = pair.trim().split("=");
                if (keyValue.length == 2) {
                    params.put(keyValue[0].trim(), keyValue[1].trim());
                }
            }
        }
        return params;
    }

    // ========== createDailyAttendanceV2 重构方法 ==========

    /**
     * 解析考勤生成参数
     * @return 参数对象
     */
    private AttendanceGenerationParams parseAttendanceGenerationParams() {
        AttendanceGenerationParams params = new AttendanceGenerationParams();
        
        // 默认生成明天的考勤数据
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, 1);
        params.targetDate = cal.getTime();
        
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        XxlJobHelper.log("默认生成日期: {} (明天)", dateFormat.format(params.targetDate));
        
        String jobParam = XxlJobHelper.getJobParam();
        if (StringUtils.isNotBlank(jobParam)) {
            Map<String, String> paramMap = parseJobParams(jobParam);
            
            // 解析日期参数
            String dateStr = paramMap.get("date");
            if (StringUtils.isNotBlank(dateStr)) {
                try {
                    params.targetDate = dateFormat.parse(dateStr);
                    XxlJobHelper.log("使用指定日期: {}", dateStr);
                } catch (Exception e) {
                    XxlJobHelper.log("日期参数格式错误: {}，使用默认日期（明天）", dateStr);
                    cal = Calendar.getInstance();
                    cal.add(Calendar.DAY_OF_MONTH, 1);
                    params.targetDate = cal.getTime();
                }
            }
            
            // 解析身份证参数（支持单个和多个）
            // 1. 先尝试解析单个身份证（向后兼容）
            params.idCard = paramMap.get("idCard");
            if (StringUtils.isNotBlank(params.idCard)) {
                params.idCardList.add(params.idCard);
                XxlJobHelper.log("使用指定身份证(idCard): {}", params.idCard);
            }
            
            // 2. 解析多个身份证参数
            String idCards = paramMap.get("idCards");
            if (StringUtils.isNotBlank(idCards)) {
                String[] idCardArray = idCards.split(",");
                for (String id : idCardArray) {
                    String trimmedId = id.trim();
                    if (StringUtils.isNotBlank(trimmedId) && !params.idCardList.contains(trimmedId)) {
                        params.idCardList.add(trimmedId);
                    }
                }
                XxlJobHelper.log("使用指定身份证列表(idCards): {} 个身份证 - {}", 
                    params.idCardList.size(), String.join(", ", params.idCardList));
            }
        }
        
        return params;
    }

    /**
     * 查询目标人员
     */
    private List<SwmPerson> queryTargetPersons(AttendanceGenerationParams params,String corpCode) {
        // 如果有身份证列表，查询指定的人员
        if (!params.idCardList.isEmpty()) {
            return queryMultiplePersons(params.idCardList);
        } 
        // 向后兼容：如果只有单个身份证参数
        else if (StringUtils.isNotBlank(params.idCard)) {
            return querySinglePerson(params.idCard);
        } 
        // 否则查询所有在职人员
        else {
            return queryAllActivePersons(corpCode);
        }
    }

    /**
     * 查询单个人员
     */
    private List<SwmPerson> querySinglePerson(String idCard) {
        List<SwmPerson> targetPersons = new ArrayList<>();
        SwmPerson person = swmPersonService.getByIdentityCard(idCard);
        
        if (person != null 
            && SwmPerson.PersonStatusEnum.ACTIVE.equals(person.getPersonnelStatus())
            && "0".equals(person.getStatus())) {
            targetPersons.add(person);
            XxlJobHelper.log("找到在职人员：{}, 身份证：{}", person.getName(), idCard);
        } else {
            if (person == null) {
                XxlJobHelper.log("未找到身份证为 {} 的人员", idCard);
            } else if (!SwmPerson.PersonStatusEnum.ACTIVE.equals(person.getPersonnelStatus())) {
                XxlJobHelper.log("身份证 {} 对应的人员 {} 不是在职状态", idCard, person.getName());
            } else {
                XxlJobHelper.log("身份证 {} 对应的人员 {} 状态异常", idCard, person.getName());
            }
        }
        
        return targetPersons;
    }

    /**
     * 查询多个人员
     * @param idCardList 身份证列表
     * @return 符合条件的在职人员列表
     */
    private List<SwmPerson> queryMultiplePersons(List<String> idCardList) {
        List<SwmPerson> targetPersons = new ArrayList<>();
        int successCount = 0;
        int skipCount = 0;
        
        for (String idCard : idCardList) {
            SwmPerson person = swmPersonService.getByIdentityCard(idCard);
            
            if (person != null 
                && SwmPerson.PersonStatusEnum.ACTIVE.equals(person.getPersonnelStatus())
                && "0".equals(person.getStatus())) {
                targetPersons.add(person);
                successCount++;
                XxlJobHelper.log("找到在职人员：{}, 身份证：{}", person.getName(), idCard);
            } else {
                skipCount++;
                if (person == null) {
                    XxlJobHelper.log("跳过：未找到身份证为 {} 的人员", idCard);
                } else if (!SwmPerson.PersonStatusEnum.ACTIVE.equals(person.getPersonnelStatus())) {
                    XxlJobHelper.log("跳过：身份证 {} 对应的人员 {} 不是在职状态", idCard, person.getName());
                } else {
                    XxlJobHelper.log("跳过：身份证 {} 对应的人员 {} 状态异常", idCard, person.getName());
                }
            }
        }
        
        XxlJobHelper.log("批量查询完成：总计 {} 个身份证，找到 {} 名在职人员，跳过 {} 个", 
            idCardList.size(), successCount, skipCount);
        
        return targetPersons;
    }
    
    /**
     * 查询所有在职人员
     */
    private List<SwmPerson> queryAllActivePersons(String corpCode) {
        SwmPerson query = new SwmPerson();
        query.setPersonnelStatus(SwmPerson.PersonStatusEnum.ACTIVE);
        query.setStatus("0");
        query.setRandom(new Random().nextInt(1_000_000));
        query.setCorpCode(corpCode);
        List<SwmPerson> persons = swmPersonService.findList(query);
        for (SwmPerson p : persons) {
            XxlJobHelper.log("【刚查出来】name={}, dbCorpCode={}， dbCorpCode={}", p.getName(), p.getCorpCode(),p.getCorpName());
        }

        XxlJobHelper.log("查询到 {} 名在职人员", persons.size());
        return persons;
    }

    /**
     * 生成考勤记录
     */
    private AttendanceGenerationResult generateAttendanceRecords(List<SwmPerson> persons, Date targetDate,String corpCode) {
        AttendanceGenerationResult result = new AttendanceGenerationResult();
        String dateStr = new SimpleDateFormat("yyyy-MM-dd").format(targetDate);
        
        for (SwmPerson person : persons) {
            if (!corpCode.equals(person.getCorpCode())) {
                XxlJobHelper.log("【跨租户人员被跳过】person={}, personCorp={}, taskCorp={}", person.getName(), person.getCorpCode(), corpCode);
                return result;
            }

            ProcessResult processResult = processSinglePerson(person, targetDate, dateStr);
            updateResultCounters(result, processResult);
        }
        
        return result;
    }

    /**
     * 处理单个人员
     */
    private ProcessResult processSinglePerson(SwmPerson person, Date targetDate, String dateStr) {
        try {
            // 验证身份证
            if (!validateIdentityCard(person)) {
                return ProcessResult.FAILED;
            }
            
            // 检查是否已存在
            SwmDailyAttendance existing = swmDailyAttendanceService.findByIdentityCardAndDate(person.getIdentityCard(), targetDate,person.getCorpCode());
            
            if (existing != null) {
                return handleExistingAttendance(existing, person, targetDate, dateStr);
            }
            if (existing == null){
                XxlJobHelper.log("查询的数据不存在：日期{},员工[{}]身份信息不存在", targetDate, person.getName());
                log.error("日期{},员工[{}]身份信息不存在", targetDate, person.getName());
            }
            
            // 创建新记录
            XxlJobHelper.log("日期{},员工[{}]身份信息不存在", targetDate, person.getName());
            createNewAttendance(person, targetDate);
            return ProcessResult.CREATED;
            
        } catch (Exception e) {
            XxlJobHelper.log("为员工[{}]{}创建考勤记录失败：{}", person.getId(), person.getName(), e.getMessage());
            return ProcessResult.FAILED;
        }
    }

    /**
     * 处理已存在的考勤记录
     */
    private ProcessResult handleExistingAttendance(SwmDailyAttendance existing, SwmPerson person, 
                                                   Date targetDate, String dateStr) {
        XxlJobHelper.log("身份证 {} 在 {} 已有考勤记录，检查是否需要更新", 
            person.getIdentityCard(), dateStr);
        
        boolean needUpdate = false;
        
        // 如果员工ID不同，说明可能是同一人的不同记录
        if (!person.getId().equals(existing.getEmployeeId())) {
            XxlJobHelper.log("发现同一身份证的不同员工记录，保留原记录，员工ID: {} -> {}", 
                existing.getEmployeeId(), person.getId());
        }
        
        // 尝试更新排班信息（如果原记录没有）
        if (StringUtils.isEmpty(existing.getWorkTimeRange()) || StringUtils.isEmpty(existing.getClasses())) {
            ScheduleInfo scheduleInfo = getScheduleTimeForPerson(person, targetDate);
            if (scheduleInfo != null) {
                SwmScheduleTime scheduleTime = scheduleInfo.scheduleTime;
                String workTimeRange = scheduleTime.getStartTime() + "-" + scheduleTime.getEndTime();
                existing.setWorkTimeRange(workTimeRange);
                existing.setClasses(scheduleInfo.classes); // 设置班次
                
                // 计算应考勤时长（减去休息时长）
                BigDecimal scheduledHours = calculateScheduledHoursFromWorkTimeRange(workTimeRange, scheduleTime.getRestTime());
                existing.setScheduledHours(scheduledHours);
                
                needUpdate = true;
                XxlJobHelper.log("更新排班信息：{}，班次：{}，应考勤时长：{} 小时", 
                    workTimeRange, scheduleInfo.classes, scheduledHours);
            }
        }
        
        if (needUpdate) {
            swmDailyAttendanceService.update(existing);
            return ProcessResult.UPDATED;
        }
        
        return ProcessResult.SKIPPED;
    }

    /**
     * 创建新的考勤记录
     */
    private void createNewAttendance(SwmPerson person, Date targetDate) {
        SwmDailyAttendance attendance = buildNewAttendance(person, targetDate);
        setScheduleInfo(attendance, person, targetDate);
        setDefaultValues(attendance);

        attendance.setCorpCode(person.getCorpCode());
        attendance.setCorpName(person.getCorpName());

        swmDailyAttendanceService.save(attendance);
        XxlJobHelper.log("为员工[{}]{}创建考勤记录成功，person租户信息：{}，{}，attendance租户信息：{}，{}", person.getId(), person.getName(),person.getCorpCode(), person.getCorpName(),attendance.getCorpCode(), attendance.getCorpName());
    }




    /**
     * 构建新的考勤记录对象
     * @param person 人员信息
     * @param targetDate 考勤日期
     * @return 新的考勤记录对象（若入参非法返回null）
     */
    private SwmDailyAttendance buildNewAttendance(SwmPerson person, Date targetDate) {
        // 日期格式化工具（避免重复创建）
        SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

        // ========== 1. 核心入参判空 + XXL-Job日志 ==========
        if (person == null) {
            XxlJobHelper.log("【ERROR】构建考勤记录：入参person为null，无法构建考勤记录");
            return null;
        }
        if (targetDate == null) {
            XxlJobHelper.log("【WARN】构建考勤记录：入参targetDate为null，默认使用当前时间");
            targetDate = new Date(); // 兜底：避免日期字段为null
        }

        // ========== 2. 打印基础上下文日志 ==========
        String personId = person.getId() == null ? "未知ID" : person.getId();
        String personName = person.getName() == null ? "未知姓名" : person.getName();
        String attendanceDate = DATE_FORMAT.format(targetDate);
        XxlJobHelper.log("【INFO】构建考勤记录：开始构建，人员ID：{}，姓名：{}，考勤日期：{}",
                personId, personName, attendanceDate);

        try {
            SwmDailyAttendance attendance = new SwmDailyAttendance();

            // ========== 3. 逐个属性赋值 + 空值日志 ==========
            // 员工ID
            if (person.getId() != null) {
                attendance.setEmployeeId(person.getId());
            } else {
                XxlJobHelper.log("【WARN】构建考勤记录：人员ID为null，employeeId字段未设置，人员姓名：{}", personName);
            }

            // 员工姓名
            if (person.getName() != null) {
                attendance.setEmployeeName(person.getName());
            } else {
                XxlJobHelper.log("【WARN】构建考勤记录：人员姓名为null，employeeName字段未设置，人员ID：{}", personId);
            }

            // 身份证号
            if (person.getIdentityCard() != null) {
                attendance.setIdentityCard(person.getIdentityCard());
            } else {
                XxlJobHelper.log("【WARN】构建考勤记录：人员身份证号为null，identityCard字段未设置，人员ID：{}，姓名：{}", personId, personName);
            }

            // 绑定设备号（安全帽ID）
            if (person.getSafetyHelmetId() != null) {
                attendance.setDeviceId(person.getSafetyHelmetId());
                XxlJobHelper.log("【DEBUG】构建考勤记录：绑定设备号：{}", person.getSafetyHelmetId());
            } else {
                XxlJobHelper.log("【WARN】构建考勤记录：人员安全帽ID为null，deviceId字段未设置，人员ID：{}，姓名：{}", personId, personName);
            }

            // 人员类型
            if (person.getPersonType() != null) {
                attendance.setPersonType(person.getPersonType());
            } else {
                XxlJobHelper.log("【WARN】构建考勤记录：人员类型为null，personType字段未设置，人员ID：{}，姓名：{}", personId, personName);
            }

            // 考勤日期（已兜底，必赋值）
            attendance.setAttendanceDate(targetDate);

            // 企业编码
            if (person.getCorpCode() != null) {
                attendance.setCorpCode(person.getCorpCode());
            } else {
                XxlJobHelper.log("【WARN】构建考勤记录：企业编码为null，corpCode字段未设置，人员ID：{}，姓名：{}", personId, personName);
            }

            // 企业名称
            if (person.getCorpName() != null) {
                attendance.setCorpName(person.getCorpName());
            } else {
                XxlJobHelper.log("【WARN】构建考勤记录：企业名称为null，corpName字段未设置，人员ID：{}，姓名：{}", personId, personName);
            }

            // ========== 4. 构建完成日志 ==========
            XxlJobHelper.log("【INFO】构建考勤记录：完成，人员ID：{}，考勤记录关键信息：employeeId={}, identityCard={}, attendanceDate={}",
                    personId,
                    attendance.getEmployeeId(),
                    attendance.getIdentityCard(),
                    DATE_FORMAT.format(attendance.getAttendanceDate()));

            return attendance;

        } catch (Exception e) {
            // ========== 5. 异常捕获日志（含堆栈） ==========
            XxlJobHelper.log("【ERROR】构建考勤记录：失败，人员ID：{}，姓名：{}，考勤日期：{}，异常信息：{}",
                    personId, personName, attendanceDate, e.getMessage());
            // 打印完整异常堆栈（XXL-Job日志支持换行）
            XxlJobHelper.log("【ERROR】异常堆栈：");
            for (StackTraceElement element : e.getStackTrace()) {
                XxlJobHelper.log(element.toString());
            }
            return null; // 异常时返回null，避免上游报错
        }
    }
    /**
     * 根据工作时间范围计算应考勤时长（用于createDailyAttendanceV2）
     * @param workTimeRange 工作时间范围，格式如 "07:00-18:00"
     * @return 应考勤时长（小时）
     * @author Shawn
     * @date 2025-08-13
     */
    private BigDecimal calculateScheduledHoursFromWorkTimeRange(String workTimeRange) {
        // 调用重载方法，不传入休息时长
        return calculateScheduledHoursFromWorkTimeRange(workTimeRange, null);
    }
    
    /**
     * 根据工作时间范围和休息时长计算应考勤时长（用于createDailyAttendanceV2）
     * @param workTimeRange 工作时间范围，格式如 "07:00-18:00"
     * @param restTime 休息时长（小时），可以为null
     * @return 应考勤时长（小时）= 班次总时长 - 休息时长
     * @author Shawn
     * @date 2025-01-09
     */
    private BigDecimal calculateScheduledHoursFromWorkTimeRange(String workTimeRange, Double restTime) {
        if (StringUtils.isBlank(workTimeRange)) {
            return BigDecimal.ZERO;
        }
        
        try {
            String[] times = workTimeRange.split("-");
            if (times.length != 2) {
                XxlJobHelper.log("工作时间范围格式错误: {}", workTimeRange);
                return BigDecimal.ZERO;
            }
            
            String startTimeStr = times[0].trim();
            String endTimeStr = times[1].trim();
            
            // 解析时间
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
            Date startTime = sdf.parse(startTimeStr);
            Date endTime = sdf.parse(endTimeStr);
            
            // 计算时间差(毫秒)
            long diffMillis = endTime.getTime() - startTime.getTime();
            
            // 转换为小时
            double hours = diffMillis / (1000.0 * 60 * 60);
            
            // 处理跨日班次的情况(如夜班20:00-05:00)
            if (hours < 0) {
                hours += 24;
                XxlJobHelper.log("检测到跨日班次: {}，计算后的班次总时长: {} 小时", workTimeRange, hours);
            }
            
            // 确保时长在合理范围内
            if (hours > 24) {
                XxlJobHelper.log("工作时间范围 {} 计算出的时长超过24小时: {} 小时，可能存在配置错误", workTimeRange, hours);
                return BigDecimal.ZERO;
            }
            
            // 减去休息时长
            if (restTime != null && restTime > 0) {
                double originalHours = hours;
                hours -= restTime;
                XxlJobHelper.log("班次 {} 总时长: {} 小时，减去休息时长: {} 小时，应考勤时长: {} 小时", 
                    workTimeRange, originalHours, restTime, hours);
            }
            
            // 确保最终应考勤时长不为负数
            if (hours < 0) {
                XxlJobHelper.log("班次 {} 减去休息时长后，应考勤时长为负数: {} 小时，设置为0", workTimeRange, hours);
                hours = 0;
            }
            
            return BigDecimal.valueOf(hours).setScale(2, RoundingMode.HALF_UP);
            
        } catch (Exception e) {
            XxlJobHelper.log("计算应考勤时长时发生异常，workTimeRange: {}, restTime: {}, 错误: {}", 
                workTimeRange, restTime, e.getMessage());
            return BigDecimal.ZERO;
        }
    }

    /**
     * 判断指定日期是否为休息日
     * @param scheduleTime 排班时间配置
     * @param targetDate 目标日期
     * @return true-是休息日，false-不是休息日
     * @author Shawn
     * @date 2025-08-14
     */
    private boolean isRestDay(SwmScheduleTime scheduleTime, Date targetDate) {
        if (scheduleTime == null || StringUtils.isBlank(scheduleTime.getRestDays())) {
            return false;
        }
        
        // 获取星期几（1-7对应周一到周日）
        Calendar cal = Calendar.getInstance();
        cal.setTime(targetDate);
        int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
        // Calendar.SUNDAY = 1, Calendar.MONDAY = 2, ... Calendar.SATURDAY = 7
        // 转换为 1-7 代表周一到周日
        int weekDay = dayOfWeek == Calendar.SUNDAY ? 7 : dayOfWeek - 1;
        
        // 解析休息日配置
        String[] restDays = scheduleTime.getRestDays().split(",");
        for (String restDay : restDays) {
            if (String.valueOf(weekDay).equals(restDay.trim())) {
                return true;
            }
        }
        
        return false;
    }



    private void setScheduleInfo(SwmDailyAttendance attendance, SwmPerson person, Date targetDate) {
        // ========== 1. 入参判空日志 ==========
        if (attendance == null) {
            XxlJobHelper.log("【警告】setScheduleInfo入参attendance为null，无法设置排班信息");
            return;
        }
        if (person == null) {
            XxlJobHelper.log("【警告】setScheduleInfo入参person为null，无法设置排班信息");
            return;
        }
        if (targetDate == null) {
            XxlJobHelper.log("【警告】setScheduleInfo入参targetDate为null，默认使用当前时间");
            targetDate = new Date(); // 兜底：避免后续空指针
        }

        // ========== 2. scheduleInfo判空日志 ==========
//        ScheduleInfo scheduleInfo = getScheduleTimeForPerson(person, targetDate);
        ScheduleInfo scheduleInfo = null;
        try {
            scheduleInfo = getScheduleTimeForPerson(person, targetDate);
        } catch (Exception e) {
            XxlJobHelper.log("【getScheduleTimeForPerson失败】员工[{}]{}，异常：{}",
                    person.getId(), person.getName(), e.getMessage());
            throw e; // 抛出异常，让上层捕获
        }
        XxlJobHelper.log("【调试】员工[{}]{}获取到排班基础信息：{}",
                person.getId(), person.getName(), scheduleInfo);

        // ========== 3. scheduleTime判空日志 ==========
        SwmScheduleTime scheduleTime = scheduleInfo.scheduleTime;
        if (scheduleTime == null) {
            XxlJobHelper.log("【警告】员工[{}]{}的排班信息ScheduleInfo中scheduleTime为null，无法计算考勤时长",
                    person.getId(), person.getName());
//            attendance.setClasses(scheduleInfo.classes); // 仅设置班次（如有）
//            attendance.setWorkTimeRange(null);
//            attendance.setScheduledHours(BigDecimal.ZERO);
//            attendance.setRestTime(BigDecimal.ZERO);
            return;
        }

        // ========== 4. 排班时间字段判空日志 ==========
        String startTime = scheduleTime.getStartTime();
        String endTime = scheduleTime.getEndTime();
        if (StringUtils.isEmpty(startTime)) {
            XxlJobHelper.log("【警告】员工[{}]{}的排班时间startTime为null/空，班次：{}",
                    person.getId(), person.getName(), scheduleInfo.classes);
        }
        if (StringUtils.isEmpty(endTime)) {
            XxlJobHelper.log("【警告】员工[{}]{}的排班时间endTime为null/空，班次：{}",
                    person.getId(), person.getName(), scheduleInfo.classes);
        }
        // 拼接时间范围，空值兜底
        String workTimeRange = (StringUtils.isEmpty(startTime) ? "未知开始时间" : startTime)
                + "-" + (StringUtils.isEmpty(endTime) ? "未知结束时间" : endTime);

        // ========== 5. 班次信息判空日志 ==========
        String classes = scheduleInfo.classes;
        if (StringUtils.isEmpty(classes)) {
            XxlJobHelper.log("【提示】员工[{}]{}的排班信息中班次(classes)为null/空，时间范围：{}",
                    person.getId(), person.getName(), workTimeRange);
        }

        // 设置班次信息
        attendance.setClasses(classes);
        attendance.setWorkTimeRange(workTimeRange);

        // ========== 6. 应考勤时长计算日志（含入参空值提示） ==========
        BigDecimal scheduledHours = calculateScheduledHoursFromWorkTimeRange(workTimeRange, scheduleTime.getRestTime());
        if (scheduledHours == null) {
            XxlJobHelper.log("【警告】员工[{}]{}应考勤时长计算结果为null，默认设为0小时",
                    person.getId(), person.getName());
            scheduledHours = BigDecimal.ZERO;
        }
        attendance.setScheduledHours(scheduledHours);

        // ========== 7. 休息时长判空日志 ==========
        Double restTime = scheduleTime.getRestTime();
        BigDecimal restTimeBigDecimal;
        if (restTime == null) {
            XxlJobHelper.log("【提示】员工[{}]{}的排班休息时长(restTime)为null，默认设为0小时",
                    person.getId(), person.getName());
            restTimeBigDecimal = BigDecimal.ZERO;
        } else {
            restTimeBigDecimal = BigDecimal.valueOf(restTime).setScale(1, RoundingMode.HALF_UP);
            XxlJobHelper.log("【调试】员工[{}]{}的排班休息时长：{} 小时（原始值）→ {} 小时（保留1位小数）",
                    person.getId(), person.getName(), restTime, restTimeBigDecimal);
        }
        attendance.setRestTime(restTimeBigDecimal);

        // ========== 8. 打卡时间范围计算日志（兜底异常） ==========
        try {
            calculateAndSetClockTimeRange(attendance, targetDate, workTimeRange);
        } catch (Exception e) {
            XxlJobHelper.log("【错误】员工[{}]{}计算打卡时间范围失败，时间范围：{}，异常信息：{}",
                    person.getId(), person.getName(), workTimeRange, e.getMessage());
        }

        // ========== 9. 休息日判断日志 ==========
        boolean isRestDayFlag = false;
        try {
            isRestDayFlag = isRestDay(scheduleTime, targetDate);
            if (isRestDayFlag) {
                attendance.setAttendanceNormal("2"); // 设置为休息日
                XxlJobHelper.log("【提示】员工[{}]{} {}是休息日，班次：{}，按工作日逻辑计算时间字段",
                        person.getId(), person.getName(),
                        new SimpleDateFormat("yyyy-MM-dd").format(targetDate),
                        classes == null ? "未知班次" : classes);
            }
        } catch (Exception e) {
            XxlJobHelper.log("【错误】员工[{}]{}判断休息日失败，异常信息：{}，默认按工作日处理",
                    person.getId(), person.getName(), e.getMessage());
        }

        // ========== 10. 最终结果日志（空值兜底） ==========
        XxlJobHelper.log("【完成】员工[{}]{}排班信息设置完成：班次：{}，时间范围：{}，应考勤时长：{} 小时，休息时长：{} 小时，是否休息日：{}",
                person.getId() == null ? "未知ID" : person.getId(),
                person.getName() == null ? "未知姓名" : person.getName(),
                classes == null ? "未知班次" : classes,
                workTimeRange,
                scheduledHours,
                attendance.getRestTime(),
                isRestDayFlag ? "是" : "否");
    }

    /**
     * 设置默认值
     */
    private void setDefaultValues(SwmDailyAttendance attendance) {
        // scheduledHours 已在 setScheduleInfo 中设置，这里不再设置
        attendance.setActualHours(BigDecimal.ZERO);
        attendance.setIdleHours(BigDecimal.ZERO);
        attendance.setEffectiveWorkHours(BigDecimal.ZERO);
        attendance.setDailyEfficiency(BigDecimal.ZERO);
        attendance.setDailyAchievementRate(BigDecimal.ZERO);
        
        // 设置默认休息时长（如果还没有设置的话）
        if (attendance.getRestTime() == null) {
            attendance.setRestTime(BigDecimal.ZERO);
        }
        
        // 如果不是休息日，设置为未考勤
        if (!"2".equals(attendance.getAttendanceNormal())) {
            attendance.setAttendanceNormal("3"); // 未考勤
        }
        
        attendance.setCurrentPosition("3"); // 未知
    }

    /**
     * 验证身份证
     */
    private boolean validateIdentityCard(SwmPerson person) {
        if (StringUtils.isEmpty(person.getIdentityCard())) {
            XxlJobHelper.log("员工[{}]{}没有身份证信息，跳过创建考勤记录", 
                person.getId(), person.getName());
            return false;
        }
        return true;
    }

    /**
     * 更新结果计数器
     */
    private void updateResultCounters(AttendanceGenerationResult result, ProcessResult processResult) {
        switch (processResult) {
            case CREATED:
                result.createdCount++;
                break;
            case UPDATED:
                result.updatedCount++;
                break;
            case SKIPPED:
                result.skippedCount++;
                break;
            case FAILED:
                result.failCount++;
                break;
        }
    }

    /**
     * 输出生成结果
     */
    private void logGenerationResult(AttendanceGenerationResult result, int totalPersons) {
        XxlJobHelper.log("每日考勤数据创建任务V2完成。");
        XxlJobHelper.log("共处理 {} 名在职人员", totalPersons);
        XxlJobHelper.log("创建：{} 条", result.createdCount);
        XxlJobHelper.log("更新：{} 条（补充排班信息）", result.updatedCount);
        XxlJobHelper.log("跳过：{} 条（已存在）", result.skippedCount);
        XxlJobHelper.log("失败：{} 条", result.failCount);
    }

    /**
     * 初始化任务日志
     */
    private SwmJobLog initJobLog(String jobName) {
        SwmJobLog jobLog = new SwmJobLog();
        jobLog.setJobName(jobName);
        jobLog.setStartTime(new Date());
        jobLog.setExecuteStatus("1"); // 默认失败
        
        String jobParam = XxlJobHelper.getJobParam();
        jobLog.setJobParam(jobParam);
        swmJobLogService.save(jobLog);
        jobLog.setIsNewRecord(false);
        
        XxlJobHelper.log("开始执行{}...", jobName);
        XxlJobHelper.log("任务参数: {}", jobParam);
        
        return jobLog;
    }

    /**
     * 保存任务日志
     */
    private void saveJobLog(SwmJobLog jobLog) {
        jobLog.setEndTime(new Date());
        jobLog.setDuration(jobLog.getEndTime().getTime() - jobLog.getStartTime().getTime());
        swmJobLogService.save(jobLog);
    }
    
    /**
     * 工作段实体类
     * @author Shawn
     * @date 2025-08-13
     */
    private static class WorkSegment {
        Date startTime;
        Date endTime;
        long durationMs;
        
        WorkSegment(Date start, Date end) {
            this.startTime = start;
            this.endTime = end;
            this.durationMs = end.getTime() - start.getTime();
        }
    }
    
    /**
     * 判断是否需要计算实际工作时长
     * @param record 考勤记录
     * @return 是否需要计算
     * @author Shawn
     * @date 2025-08-13
     */
    private boolean shouldCalculateWorkHours(SwmDailyAttendance record) {
        // 必须有身份证号
        if (StringUtils.isBlank(record.getIdentityCard())) {
            return false;
        }
        
        // 必须有时间信息（打卡时间或应考勤时间）
        return record.getClockInDate() != null || 
               record.getClockOutDate() != null || 
               StringUtils.isNotBlank(record.getWorkTimeRange());
    }
    
    /**
     * 计算指定区域活动时长（通用方法）
     * @param record 考勤记录
     * @param areaType 区域类型（0-工作区，1-休息区）
     * @param areaName 区域名称（用于日志）
     * @return 活动时长（小时）
     * @author Shawn
     * @date 2025-08-13
     */
    private BigDecimal calculateAreaHours(SwmDailyAttendance record, String areaType, String areaName,String corpCode) {
        try {
            // 1. 确定查询时间范围
            String[] timeRange = determineQueryTimeRange(record);
            if (timeRange == null) {
                XxlJobHelper.log("员工[{}]{}无法确定时间范围，跳过{}时长计算", 
                    record.getEmployeeId(), record.getEmployeeName(), areaName);
                return null;
            }
            
            // 2. 查询指定区域的连续段
            List<WorkSegment> workSegments = queryAreaSegments(
                record.getIdentityCard(), 
                timeRange[0], 
                timeRange[1],
                areaType,corpCode
            );
            
            if (workSegments.isEmpty()) {
                XxlJobHelper.log("员工[{}]{}在时间范围内无{}活动数据", 
                    record.getEmployeeId(), record.getEmployeeName(), areaName);
                return BigDecimal.ZERO;
            }
            
            // 3. 计算总工作时长
            double totalHours = calculateTotalHours(workSegments);
            
            // 4. 计算打卡时间范围的上限
            double maxHours = calculateMaxWorkHours(timeRange[0], timeRange[1]);

            // 5. 如果计算的活动时长超过上限，使用上限值
            if (totalHours > maxHours) {
                XxlJobHelper.log("员工[{}]{}计算的{}时长{}小时超过打卡时长{}小时，使用打卡时长", 
                    record.getEmployeeId(), record.getEmployeeName(), 
                    areaName, String.format("%.2f", totalHours), String.format("%.2f", maxHours));
                totalHours = maxHours;
            }
            
            XxlJobHelper.log("员工[{}]{}{}活动段数: {}, 总时长: {}小时", 
                record.getEmployeeId(), record.getEmployeeName(), 
                areaName, workSegments.size(), String.format("%.2f", totalHours));
            
            return BigDecimal.valueOf(totalHours).setScale(2, RoundingMode.HALF_UP);
            
        } catch (Exception e) {
            XxlJobHelper.log("计算员工[{}]{}{}时长失败: {}", 
                record.getEmployeeId(), record.getEmployeeName(), areaName, e.getMessage());
            return null;
        }
    }
    
    /**
     * 计算工作区域活动时长（包装方法）
     * @param record 考勤记录
     * @return 工作时长（小时）
     * @author Shawn
     * @date 2025-08-13
     */
    private BigDecimal calculateWorkAreaHours(SwmDailyAttendance record,String corpCode) {
        return calculateAreaHours(record, "0", "工作区",corpCode);
    }
    
    /**
     * 计算休息区域活动时长/怠工时长（包装方法）
     * @param record 考勤记录
     * @return 怠工时长（小时）
     * @author Shawn
     * @date 2025-08-13
     */
    private BigDecimal calculateIdleAreaHours(SwmDailyAttendance record,String corpCode) {
        return calculateAreaHours(record, "1", "休息区",corpCode);
    }
    
    /**
     * 计算当天24小时工作区域活动时长（专用方法）
     * 时间范围：当天00:00:00到23:59:59
     * @param record 考勤记录
     * @return 24小时工作区域时长（小时）
     * @author Shawn
     * @date 2025-08-20
     */
    private BigDecimal calculateWorkAreaHours24h(SwmDailyAttendance record,String corpCode) {
        try {
            // 直接使用当天24小时范围
//            String dateStr = new SimpleDateFormat("yyyy-MM-dd").format(record.getAttendanceDate());

            // 根据班次确定时间范围
            String[] timeRange = determineQueryTimeRange(record);
            if (timeRange == null) {
                XxlJobHelper.log("员工[{}]{}无法确定时间范围，跳过工作区域时长计算",
                        record.getEmployeeId(), record.getEmployeeName());
                return null;
            }
//            String startTime = dateStr + " 00:00:00";
//            String endTime = dateStr + " 23:59:59";

            String startTime = timeRange[0];
            String endTime = timeRange[1];
            
            XxlJobHelper.log("员工[{}]{}使用24小时时间范围: {} 到 {}", 
                record.getEmployeeId(), record.getEmployeeName(), startTime, endTime);
            
            // 查询工作区域的连续段
            List<WorkSegment> workSegments = queryAreaSegments(
                record.getIdentityCard(), startTime, endTime, "0",corpCode);
            
            if (workSegments.isEmpty()) {
                XxlJobHelper.log("员工[{}]{}在当天24小时内无工作区域活动数据", 
                    record.getEmployeeId(), record.getEmployeeName());
                return BigDecimal.ZERO;
            }
            
            // 计算总时长
            double totalHours = calculateTotalHours(workSegments);
            
            XxlJobHelper.log("员工[{}]{}24小时工作区域活动段数: {}, 总时长: {}小时", 
                record.getEmployeeId(), record.getEmployeeName(), 
                workSegments.size(), String.format("%.2f", totalHours));
            
            return BigDecimal.valueOf(totalHours).setScale(2, RoundingMode.HALF_UP);
            
        } catch (Exception e) {
            XxlJobHelper.log("计算24小时工作区域时长失败: {}", e.getMessage());
            return BigDecimal.ZERO;
        }
    }


    /**
     * 确定查询时间范围
     * @param record 考勤记录
     * 新需求 这里不按照 原来的逻辑，改为按照班次进行查询 早班按照  00:00:00 到 23:59:59 晚班按照  12:00:00 到 12:00:00（+1）
     * @return 时间范围数组 [开始时间, 结束时间]，如果无法确定返回null
     * @author fangxiaolong
     * @date 2025-12-29
     */
    private String[] determineQueryTimeRange(SwmDailyAttendance record) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String attendanceDate = dateFormat.format(record.getAttendanceDate());

        // 检查是否有班次信息
        if (StringUtils.isBlank(record.getClasses())) {
            return null;
        }

        String classes = record.getClasses();

        try {
            if ("1".equals(classes)) {
                // 白班：当天 00:00:00 到 24:00:00
                String startTime = attendanceDate + " 00:00:00";
                String endTime = attendanceDate + " 23:59:59";

                return new String[]{startTime, endTime};
            } else if ("3".equals(classes)) {
                // 夜班：当天中午12:00 到 次日中午12:00
                Calendar cal = Calendar.getInstance();
                cal.setTime(record.getAttendanceDate());
                cal.add(Calendar.DAY_OF_MONTH, 1); // 次日
                String nextDay = dateFormat.format(cal.getTime());

                String startTime = attendanceDate + " 12:00:00";
                String endTime = nextDay + " 12:00:00";

                return new String[]{startTime, endTime};
            } else {
                // 其他班次类型，暂时返回null
                return null;
            }
        } catch (Exception e) {
            log.error("确定班次时间范围失败: {}", classes, e);
            return null;
        }
    }


    /**
     * 查询区域活动段
     * @param idCard 身份证号
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param areaType 区域类型（0-工作区，1-休息区）
     * @return 活动段列表
     * @author Shawn
     * @date 2025-08-13
     */
    private List<WorkSegment> queryAreaSegments(String idCard, String startTime, String endTime, String areaType,String corpCode) {
        List<WorkSegment> segments = new ArrayList<>();
        
        try {
            // 查询指定区域的时间戳
            String sql = String.format(
                "SELECT time FROM %s.%s " +
                "WHERE id_card = '%s' " +
                "AND area_type = '%s' " +
                "AND time >= '%s' " +
                "AND time <= '%s' " +
                "ORDER BY time ASC",
                dbname, TdengineSuperTableConstant.AREA_FENCE_DATA, idCard, areaType, startTime, endTime
            );
            
            XxlJobHelper.log("查询区域数据SQL: {}", sql);
            
            R<JSONObject> response = tdengineService.executeTDengineSQLByXXJOB(sql,corpCode);
            if (response.getCode() != R.SUCCESS || response.getData() == null) {
                XxlJobHelper.log("查询失败或无数据");
                return segments;
            }
            
            JSONArray rows = response.getData().getJSONArray("data");
            if (rows == null || rows.isEmpty()) {
                return segments;
            }
            
            // 解析时间戳并识别连续段
            List<Date> timestamps = new ArrayList<>();
            for (int i = 0; i < rows.size(); i++) {
                JSONArray row = rows.getJSONArray(i);
                Date timestamp = parseTimestamp(row.get(0).toString());
                if (timestamp != null) {
                    timestamps.add(timestamp);
                }
            }
            
            // 边界补充逻辑：处理打卡时间与实际数据的差异
            if (!timestamps.isEmpty()) {
                try {
                    Date queryStart = DATETIME_FORMAT.parse(startTime);
                    Date queryEnd = DATETIME_FORMAT.parse(endTime);
                    Date firstData = timestamps.get(0);
                    Date lastData = timestamps.get(timestamps.size() - 1);
                    
                    // 补充开始时间：如果第一条数据晚于查询开始时间且在10分钟内
                    long startGap = firstData.getTime() - queryStart.getTime();
                    if (startGap > 0 && startGap <= CONTINUITY_THRESHOLD_MS) {
                        timestamps.add(0, queryStart);
                        XxlJobHelper.log("第一条数据晚于打卡时间{}分钟，补充打卡时间作为开始", 
                            startGap / (60 * 1000));
                    }
                    
                    // 补充结束时间：如果最后一条数据早于查询结束时间且在10分钟内
                    long endGap = queryEnd.getTime() - lastData.getTime();
                    if (endGap > 0 && endGap <= CONTINUITY_THRESHOLD_MS) {
                        timestamps.add(queryEnd);
                        XxlJobHelper.log("最后一条数据早于打卡时间{}分钟，补充打卡时间作为结束", 
                            endGap / (60 * 1000));
                    }
                } catch (Exception e) {
                    XxlJobHelper.log("边界补充处理失败: {}", e.getMessage());
                }
            }
            
            // 构建连续工作段
            Date segmentStart = null;
            Date lastTime = null;
            
            for (Date currentTime : timestamps) {
                if (segmentStart == null) {
                    // 开始新段
                    segmentStart = currentTime;
                    lastTime = currentTime;
                } else {
                    long gap = currentTime.getTime() - lastTime.getTime();
                    
                    if (gap <= CONTINUITY_THRESHOLD_MS) {
                        // 连续，更新最后时间
                        lastTime = currentTime;
                    } else {
                        // 间隔太大，结束当前段
                        segments.add(new WorkSegment(segmentStart, lastTime));
                        
                        // 开始新段
                        segmentStart = currentTime;
                        lastTime = currentTime;
                    }
                }
            }
            
            // 处理最后一段
            if (segmentStart != null) {
                segments.add(new WorkSegment(segmentStart, lastTime));
            }
            
            XxlJobHelper.log("识别到{}个连续工作段", segments.size());
            
        } catch (Exception e) {
            XxlJobHelper.log("查询工作段失败: {}", e.getMessage());
        }
        
        return segments;
    }
    
    /**
     * 计算总时长
     * @param segments 工作段列表
     * @return 总时长（小时）
     * @author Shawn
     * @date 2025-08-13
     */
    private double calculateTotalHours(List<WorkSegment> segments) {
        long totalMs = 0;
        
        for (WorkSegment segment : segments) {
            // 每个段的时长 = 结束时间 - 开始时间
            totalMs += segment.durationMs;
            
            XxlJobHelper.log("工作段: {} 到 {}, 时长: {}分钟",
                DATETIME_FORMAT.format(segment.startTime),
                DATETIME_FORMAT.format(segment.endTime),
                segment.durationMs / (60 * 1000));
        }
        
        // 转换为小时
        double hours = totalMs / (1000.0 * 60 * 60);
        
        return hours;
    }
    
    /**
     * 计算最大工作时长（打卡时间范围）
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 最大工作时长（小时）
     * @author Shawn
     * @date 2025-08-13
     */
    private double calculateMaxWorkHours(String startTime, String endTime) {
        try {
            Date start = DATETIME_FORMAT.parse(startTime);
            Date end = DATETIME_FORMAT.parse(endTime);
            long diffMs = end.getTime() - start.getTime();
            
            // 如果是负数，说明跨天了，需要加24小时
            if (diffMs < 0) {
                diffMs += 24 * 60 * 60 * 1000;
            }
            
            double hours = diffMs / (1000.0 * 60 * 60);
            XxlJobHelper.log("打卡时间范围: {} 到 {}，最大工作时长: {}小时", 
                startTime, endTime, String.format("%.2f", hours));
            return hours;
        } catch (Exception e) {
            XxlJobHelper.log("计算最大工作时长失败: {}", e.getMessage());
            return 24.0; // 默认最大24小时
        }
    }
    
    /**
     * 解析时间戳
     * @param timeStr 时间字符串
     * @return Date对象
     * @author Shawn
     * @date 2025-08-13
     */
    private Date parseTimestamp(String timeStr) {
        try {
            // 处理TDengine的ISO格式时间
            if (timeStr.contains("T")) {
                return Date.from(Instant.parse(timeStr));
            } else {
                return DATETIME_FORMAT.parse(timeStr);
            }
        } catch (Exception e) {
            XxlJobHelper.log("解析时间戳失败: {}", timeStr);
            return null;
        }
    }

    /**
     * 计算并设置打卡时间范围
     * 基于考勤日期和工作时间范围，计算有效的打卡时间窗口
     * 
     * @param attendance 考勤记录
     * @param targetDate 考勤日期
     * @param workTimeRange 工作时间范围（格式：HH:mm-HH:mm）
     * @author Shawn
     * @date 2025-08-20
     */
    private void calculateAndSetClockTimeRange(SwmDailyAttendance attendance, Date targetDate, String workTimeRange) {
        if (StringUtils.isBlank(workTimeRange) || !workTimeRange.contains("-")) {
            XxlJobHelper.log("工作时间范围格式无效: {}, 跳过打卡时间范围计算", workTimeRange);
            return;
        }
        
        try {
            // 解析工作时间范围
            String[] times = workTimeRange.split("-");
            if (times.length != 2) {
                XxlJobHelper.log("工作时间范围格式错误: {}", workTimeRange);
                return;
            }
            
            String startTimeStr = times[0].trim();
            String endTimeStr = times[1].trim();
            
            // 构建工作开始时间的完整日期时间
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
            SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            
            String targetDateStr = dateFormat.format(targetDate);
            String workStartDateTimeStr = targetDateStr + " " + startTimeStr + ":00";
            Date workStartDateTime = dateTimeFormat.parse(workStartDateTimeStr);
            
            // 计算打卡开始时间：工作开始时间减去4小时
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(workStartDateTime);
            calendar.add(Calendar.HOUR_OF_DAY, -4);
            Date clockStartTime = calendar.getTime();
            
            // 计算打卡结束时间：打卡开始时间加24小时
            calendar.setTime(clockStartTime);
            calendar.add(Calendar.HOUR_OF_DAY, 24);
            Date clockEndTime = calendar.getTime();
            
            // 设置到考勤记录中
            attendance.setClockStartTime(clockStartTime);
            attendance.setClockEndTime(clockEndTime);
            
            // 记录日志
            SimpleDateFormat logFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            XxlJobHelper.log("员工[{}]{}打卡时间范围计算完成: {} 到 {} (工作时间: {})", 
                attendance.getEmployeeId(), attendance.getEmployeeName(),
                logFormat.format(clockStartTime), logFormat.format(clockEndTime), workTimeRange);
                
        } catch (Exception e) {
            XxlJobHelper.log("计算打卡时间范围时发生异常，targetDate: {}, workTimeRange: {}, 错误: {}", 
                targetDate, workTimeRange, e.getMessage());
        }
    }

    /**
     * 月度考勤统计参数类
     * @author Shawn
     * @date 2025-08-20
     */
    private static class MonthlyAttendanceParams {
        /** 目标月份（格式：yyyy-MM） */
        String targetMonth;
        /** 身份证号列表 */
        List<String> idCardList = new ArrayList<>();
    }

    /**
     * 解析月度考勤统计参数
     * 支持多种参数格式的解析
     * 
     * @param jobParam xxl-job传入的参数字符串
     * @return 解析后的参数对象
     * @author Shawn
     * @date 2025-08-20
     */
    private MonthlyAttendanceParams parseMonthlyAttendanceParams(String jobParam) {
        MonthlyAttendanceParams params = new MonthlyAttendanceParams();
        
        // 默认使用当前月份
        SimpleDateFormat monthFormat = new SimpleDateFormat("yyyy-MM");
        params.targetMonth = monthFormat.format(new Date());
        
        if (StringUtils.isBlank(jobParam)) {
            logParseResult(params);
            return params;
        }
        
        try {
            // 支持分号分隔的多个参数
            String[] paramArray = jobParam.split(";");
            for (String param : paramArray) {
                parseParameter(param.trim(), params);
            }
        } catch (Exception e) {
            XxlJobHelper.log("参数解析失败: {}，使用默认参数。错误: {}", jobParam, e.getMessage());
            logParameterExamples();
        }
        
        logParseResult(params);
        return params;
    }

    /**
     * 解析单个参数
     * @param param 单个参数字符串
     * @param params 参数对象
     * @author Shawn
     * @date 2025-08-20
     */
    private void parseParameter(String param, MonthlyAttendanceParams params) {
        if (param.startsWith("month=")) {
            parseMonthParameter(param, params);
        } else if (param.startsWith("idCard=")) {
            parseSingleIdCardParameter(param, params);
        } else if (param.startsWith("idCards=")) {
            parseMultipleIdCardsParameter(param, params);
        }
    }

    /**
     * 解析月份参数
     * @param param 月份参数字符串
     * @param params 参数对象
     * @author Shawn
     * @date 2025-08-20
     */
    private void parseMonthParameter(String param, MonthlyAttendanceParams params) {
        String monthStr = param.substring("month=".length()).trim();
        if (monthStr.matches("\\d{4}-\\d{2}")) {
            params.targetMonth = monthStr;
            XxlJobHelper.log("解析月份参数: {}", monthStr);
        } else {
            XxlJobHelper.log("月份格式错误: {}，使用默认当前月份: {}", monthStr, params.targetMonth);
        }
    }

    /**
     * 解析单个身份证参数
     * @param param 身份证参数字符串
     * @param params 参数对象
     * @author Shawn
     * @date 2025-08-20
     */
    private void parseSingleIdCardParameter(String param, MonthlyAttendanceParams params) {
        String idCard = param.substring("idCard=".length()).trim();
        if (isValidIdCard(idCard)) {
            params.idCardList.add(idCard);
            XxlJobHelper.log("解析单个身份证参数: {}", idCard);
        } else {
            XxlJobHelper.log("身份证格式不正确，跳过: {}", idCard);
        }
    }

    /**
     * 解析多个身份证参数
     * @param param 多个身份证参数字符串
     * @param params 参数对象
     * @author Shawn
     * @date 2025-08-20
     */
    private void parseMultipleIdCardsParameter(String param, MonthlyAttendanceParams params) {
        String idCardsStr = param.substring("idCards=".length()).trim();
        if (StringUtils.isBlank(idCardsStr)) {
            return;
        }
        
        String[] idCards = idCardsStr.split(",");
        int validCount = 0;
        
        for (String idCard : idCards) {
            String trimmedIdCard = idCard.trim();
            if (isValidIdCard(trimmedIdCard)) {
                params.idCardList.add(trimmedIdCard);
                validCount++;
            } else {
                XxlJobHelper.log("身份证格式不正确，跳过: {}", trimmedIdCard);
            }
        }
        
        XxlJobHelper.log("解析多个身份证参数: 总数{}, 有效{}", idCards.length, validCount);
    }

    /**
     * 记录解析结果
     * @param params 参数对象
     * @author Shawn
     * @date 2025-08-20
     */
    private void logParseResult(MonthlyAttendanceParams params) {
        XxlJobHelper.log("参数解析完成 - 目标月份: {}", params.targetMonth);
        
        if (params.idCardList.isEmpty()) {
            XxlJobHelper.log("处理所有人员");
            return;
        }
        
        XxlJobHelper.log("指定身份证数量: {}个", params.idCardList.size());
        if (params.idCardList.size() <= 10) {
            XxlJobHelper.log("身份证列表: {}", String.join(", ", params.idCardList));
        } else {
            XxlJobHelper.log("身份证列表（前10个）: {}", 
                String.join(", ", params.idCardList.subList(0, 10)));
        }
    }

    /**
     * 记录参数格式示例
     * @author Shawn
     * @date 2025-08-20
     */
    private void logParameterExamples() {
        XxlJobHelper.log("参数格式示例:");
        XxlJobHelper.log("  month=2025-08");
        XxlJobHelper.log("  month=2025-08;idCard=412825197709304513");
        XxlJobHelper.log("  month=2025-08;idCards=412825197709304513,110101199001011234");
        XxlJobHelper.log("  idCards=412825197709304513,110101199001011234");
    }

    /**
     * 验证身份证号格式
     * @param idCard 身份证号
     * @return true-格式正确，false-格式错误
     * @author Shawn
     * @date 2025-08-20
     */
    private boolean isValidIdCard(String idCard) {
        if (StringUtils.isBlank(idCard)) {
            return false;
        }
        // 验证身份证格式（15位或18位，18位最后一位可以是X）
        return idCard.matches("\\d{15}|\\d{17}[\\dXx]");
    }

    /**
     * 计算并更新实际出勤天数
     * 根据参数中的身份证列表或从缓存中提取，计算实际出勤天数并更新到考勤统计表
     * 
     * @param params 月度考勤统计参数
     * @param monthlyDataCache 月度日考勤数据缓存
     * @author Shawn
     * @date 2025-08-21
     */
    private void calculateAndUpdateActualDays(MonthlyAttendanceParams params, 
                                             Map<String, List<SwmDailyAttendance>> monthlyDataCache) {
        try {
            XxlJobHelper.log("开始计算并更新实际出勤天数 - 目标月份: {}", params.targetMonth);
            
            // 检查缓存是否可用
            if (monthlyDataCache.isEmpty()) {
                XxlJobHelper.log("{}月没有日考勤数据缓存，跳过实际出勤天数计算", params.targetMonth);
                return;
            }
            
            // 1. 获取目标身份证列表
            List<String> targetIdCards = getTargetIdCardsFromCache(params, monthlyDataCache);
            
            if (targetIdCards.isEmpty()) {
                XxlJobHelper.log("没有需要处理的人员身份证");
                return;
            }
            
            XxlJobHelper.log("共需要处理 {} 个身份证号的实际出勤天数", targetIdCards.size());
            
            int successCount = 0;
            int failCount = 0;
            
            // 2. 逐个计算并更新实际出勤天数
            for (String identityCard : targetIdCards) {
                try {
                    // 从缓存计算实际出勤天数
                    BigDecimal actualDays = calculateActualDaysFromCache(identityCard, monthlyDataCache);
                    
                    // 更新到考勤统计表
                    boolean updateResult = updateActualDaysToSummary(identityCard, params.targetMonth, actualDays);
                    
                    if (updateResult) {
                        successCount++;
                        XxlJobHelper.log("身份证号[{}]实际出勤天数更新成功: {}天", identityCard, actualDays);
                    } else {
                        failCount++;
                        XxlJobHelper.log("身份证号[{}]实际出勤天数更新失败", identityCard);
                    }
                    
                } catch (Exception e) {
                    failCount++;
                    XxlJobHelper.log("处理身份证号[{}]时发生异常: {}", identityCard, e.getMessage());
                }
            }
            
            XxlJobHelper.log("实际出勤天数计算完成 - 成功: {}个, 失败: {}个", successCount, failCount);
            
        } catch (Exception e) {
            XxlJobHelper.log("计算并更新实际出勤天数时发生异常: {}", e.getMessage());
        }
    }

    /**
     * 从缓存或参数中获取目标身份证列表
     * 优先使用参数中指定的身份证列表，否则从缓存中提取所有身份证号
     * 
     * @param params 月度考勤统计参数
     * @param dataCache 月度日考勤数据缓存
     * @return 身份证列表
     * @author Shawn
     * @date 2025-08-21
     */
    private List<String> getTargetIdCardsFromCache(MonthlyAttendanceParams params, 
                                                  Map<String, List<SwmDailyAttendance>> dataCache) {
        if (!params.idCardList.isEmpty()) {
            XxlJobHelper.log("使用参数指定的身份证列表，共{}个", params.idCardList.size());
            return params.idCardList;
        }
        
        // 从缓存中提取所有身份证号
        List<String> idCardList = new ArrayList<>(dataCache.keySet());
        XxlJobHelper.log("从日考勤数据缓存中提取身份证列表，共{}个", idCardList.size());
        
        return idCardList;
    }

    /**
     * 更新实际出勤天数到考勤统计表
     * 根据身份证号查询现有记录并更新actual_days字段
     * 
     * @param identityCard 身份证号
     * @param targetMonth 目标月份
     * @param actualDays 实际出勤天数
     * @return 更新是否成功
     * @author Shawn
     * @date 2025-08-21
     */
    private boolean updateActualDaysToSummary(String identityCard, String targetMonth, BigDecimal actualDays) {
        try {
            // 查询现有记录
            List<SwmAttendanceSummary> existingRecords = swmAttendanceSummaryService
                .findByIdentityCardAndMonth(identityCard, targetMonth);
            
            if (!existingRecords.isEmpty()) {
                // 更新现有记录的实际出勤天数
                for (SwmAttendanceSummary record : existingRecords) {
                    record.setActualDays(actualDays);
                    swmAttendanceSummaryService.update(record);
                }
                XxlJobHelper.log("身份证号[{}]在{}月已有{}条考勤统计记录，已批量更新实际出勤天数", 
                    identityCard, targetMonth, existingRecords.size());
                return true;
            } else {
                XxlJobHelper.log("身份证号[{}]在{}月没有考勤统计记录，跳过实际出勤天数更新", 
                    identityCard, targetMonth);
                return false;
            }
            
        } catch (Exception e) {
            XxlJobHelper.log("更新身份证号[{}]的实际出勤天数到考勤统计表失败: {}", identityCard, e.getMessage());
            return false;
        }
    }

    /**
     * 计算并更新应出勤天数
     * 根据参数中的身份证列表或查询所有有排班的人员，计算应出勤天数并更新到考勤统计表
     * 
     * @param params 月度考勤统计参数
     * @author Shawn
     * @date 2025-08-21
     */
    private void calculateAndUpdateScheduledDays(MonthlyAttendanceParams params) {
        try {
            XxlJobHelper.log("开始计算并更新应出勤天数 - 目标月份: {}", params.targetMonth);
            
            // 1. 获取目标身份证列表
            List<String> targetIdCards = getTargetIdCards(params);
            
            if (targetIdCards.isEmpty()) {
                XxlJobHelper.log("没有需要处理的人员身份证");
                return;
            }
            
            XxlJobHelper.log("共需要处理 {} 个身份证号的应出勤天数", targetIdCards.size());
            
            int successCount = 0;
            int failCount = 0;
            
            // 2. 逐个计算并更新应出勤天数
            for (String identityCard : targetIdCards) {
                try {
                    // 计算应出勤天数
                    BigDecimal scheduledDays = calculateScheduledDays(identityCard, params.targetMonth);
                    
                    if (scheduledDays == null) {
                        XxlJobHelper.log("身份证号[{}]应出勤天数计算失败，跳过更新", identityCard);
                        failCount++;
                        continue;
                    }
                    
                    // 更新到考勤统计表
                    boolean updateResult = updateScheduledDaysToSummary(identityCard, params.targetMonth, scheduledDays);
                    
                    if (updateResult) {
                        successCount++;
                        XxlJobHelper.log("身份证号[{}]应出勤天数更新成功: {}天", identityCard, scheduledDays);
                    } else {
                        failCount++;
                        XxlJobHelper.log("身份证号[{}]应出勤天数更新失败", identityCard);
                    }
                    
                } catch (Exception e) {
                    failCount++;
                    XxlJobHelper.log("处理身份证号[{}]时发生异常: {}", identityCard, e.getMessage());
                }
            }
            
            XxlJobHelper.log("应出勤天数计算完成 - 成功: {}个, 失败: {}个", successCount, failCount);
            
        } catch (Exception e) {
            XxlJobHelper.log("计算并更新应出勤天数时发生异常: {}", e.getMessage());
        }
    }

    /**
     * 获取目标身份证列表
     * 如果参数中指定了身份证列表，则使用指定的；否则查询所有有排班的人员
     * 
     * @param params 月度考勤统计参数
     * @return 身份证列表
     * @author Shawn
     * @date 2025-08-21
     */
    private List<String> getTargetIdCards(MonthlyAttendanceParams params) {
        if (!params.idCardList.isEmpty()) {
            XxlJobHelper.log("使用参数指定的身份证列表，共{}个", params.idCardList.size());
            return params.idCardList;
        }
        
        // 查询指定月份所有有排班的人员身份证号
        try {
            SwmPersonSchedule query = new SwmPersonSchedule();
            query.setMonth(params.targetMonth);
            List<SwmPersonSchedule> scheduleList = swmPersonScheduleService.findList(query);
            
            List<String> idCardList = new ArrayList<>();
            Set<String> idCardSet = new HashSet<>(); // 用于去重
            
            for (SwmPersonSchedule schedule : scheduleList) {
                if (StringUtils.isNotBlank(schedule.getIdCard()) && !idCardSet.contains(schedule.getIdCard())) {
                    idCardList.add(schedule.getIdCard());
                    idCardSet.add(schedule.getIdCard());
                }
            }
            
            XxlJobHelper.log("查询到{}月有排班的人员共{}个", params.targetMonth, idCardList.size());
            return idCardList;
            
        } catch (Exception e) {
            XxlJobHelper.log("查询{}月排班人员失败: {}", params.targetMonth, e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * 更新应出勤天数到考勤统计表
     * 根据身份证号查询现有记录，存在则更新，不存在则创建新记录
     * 
     * @param identityCard 身份证号
     * @param targetMonth 目标月份
     * @param scheduledDays 应出勤天数
     * @return 更新是否成功
     * @author Shawn
     * @date 2025-08-21
     */
    private boolean updateScheduledDaysToSummary(String identityCard, String targetMonth, BigDecimal scheduledDays) {
        try {
            // 1. 查询现有记录
            List<SwmAttendanceSummary> existingRecords = swmAttendanceSummaryService
                .findByIdentityCardAndMonth(identityCard, targetMonth);
            
            if (!existingRecords.isEmpty()) {
                // 2. 更新现有记录
                for (SwmAttendanceSummary record : existingRecords) {
                    record.setScheduledDays(scheduledDays);
                    swmAttendanceSummaryService.update(record);
                }
                XxlJobHelper.log("身份证号[{}]在{}月已有{}条考勤统计记录，已批量更新应出勤天数", 
                    identityCard, targetMonth, existingRecords.size());
                return true;
            } else {
                // 3. 创建新记录
                return createNewAttendanceSummaryRecord(identityCard, targetMonth, scheduledDays);
            }
            
        } catch (Exception e) {
            XxlJobHelper.log("更新身份证号[{}]的应出勤天数到考勤统计表失败: {}", identityCard, e.getMessage());
            return false;
        }
    }

    /**
     * 创建新的考勤统计记录
     * 
     * @param identityCard 身份证号
     * @param targetMonth 目标月份
     * @param scheduledDays 应出勤天数
     * @return 创建是否成功
     * @author Shawn
     * @date 2025-08-21
     */
    private boolean createNewAttendanceSummaryRecord(String identityCard, String targetMonth, BigDecimal scheduledDays) {
        try {
            // 1. 查询人员基本信息
            SwmPerson person = swmPersonService.getByIdentityCard(identityCard);
            if (person == null) {
                XxlJobHelper.log("身份证号[{}]没有找到对应的人员信息，无法创建考勤统计记录", identityCard);
                return false;
            }
            
            // 2. 查询排班信息获取班次
            List<SwmPersonSchedule> scheduleList = swmPersonScheduleService
                .findByIdCardAndMonth(identityCard, targetMonth);
            String workShift = null;
            if (!scheduleList.isEmpty()) {
                workShift = scheduleList.get(0).getClasses();
            }
            
            // 3. 创建新的考勤统计记录
            SwmAttendanceSummary summary = new SwmAttendanceSummary();
            summary.setEmployeeId(person.getId());
            summary.setEmployeeName(person.getName());
            summary.setIdentityCard(identityCard);
            summary.setDepartment(person.getDepartment());
            summary.setWorkProcess(person.getWorkProcess());
            summary.setTeam(person.getTeam());
            summary.setJobType(person.getJobType());
            summary.setWorkShift(workShift);
            summary.setMonth(targetMonth);
            summary.setScheduledDays(scheduledDays);
            
            // 设置其他字段的默认值
            summary.setActualDays(BigDecimal.ZERO);
            summary.setAttendanceRate(BigDecimal.ZERO);
            summary.setScheduledHours(BigDecimal.ZERO);
            summary.setActualHours(BigDecimal.ZERO);
            summary.setAttendanceAchievementRate(BigDecimal.ZERO);
            summary.setIdleHours(BigDecimal.ZERO);
            summary.setEfficiency(BigDecimal.ZERO);
            
            swmAttendanceSummaryService.save(summary);
            
            XxlJobHelper.log("为身份证号[{}]({})创建{}月考勤统计记录成功，应出勤天数: {}天", 
                identityCard, person.getName(), targetMonth, scheduledDays);
            return true;
            
        } catch (Exception e) {
            XxlJobHelper.log("为身份证号[{}]创建考勤统计记录失败: {}", identityCard, e.getMessage());
            return false;
        }
    }

    /**
     * 预加载月度日考勤数据到内存缓存
     * 一次性查询整个月的所有日考勤记录，按身份证号分组存储
     * 
     * @param targetMonth 目标月份（格式：yyyy-MM）
     * @return 按身份证号分组的日考勤数据缓存
     * @author Shawn
     * @date 2025-08-21
     */
    private Map<String, List<SwmDailyAttendance>> loadMonthlyAttendanceDataCache(String targetMonth) {
        try {
            XxlJobHelper.log("开始预加载{}月的日考勤数据到内存缓存", targetMonth);
            
            // 一次性查询整个月的所有日考勤记录
            List<SwmDailyAttendance> allRecords = swmDailyAttendanceService.findByMonth(targetMonth);
            
            if (allRecords == null || allRecords.isEmpty()) {
                XxlJobHelper.log("{}月没有找到任何日考勤记录", targetMonth);
                return new HashMap<>();
            }
            
            // 按身份证号分组，过滤掉身份证号为空的记录
            Map<String, List<SwmDailyAttendance>> dataCache = allRecords.stream()
                .filter(record -> StringUtils.isNotBlank(record.getIdentityCard()))
                .collect(Collectors.groupingBy(SwmDailyAttendance::getIdentityCard));
            
            XxlJobHelper.log("{}月日考勤数据预加载完成 - 总记录数: {}, 员工数: {}", 
                targetMonth, allRecords.size(), dataCache.size());
            
            return dataCache;
            
        } catch (Exception e) {
            XxlJobHelper.log("预加载{}月日考勤数据失败: {}", targetMonth, e.getMessage());
            return new HashMap<>();
        }
    }

    /**
     * 从缓存中计算实际出勤天数
     * 统计日考勤记录中有打卡记录的天数（clock_in_date 或 clock_out_date 不为空）
     * 
     * @param identityCard 身份证号
     * @param dataCache 月度日考勤数据缓存
     * @return 实际出勤天数
     * @author Shawn
     * @date 2025-08-21
     */
    private BigDecimal calculateActualDaysFromCache(String identityCard, 
                                                   Map<String, List<SwmDailyAttendance>> dataCache) {
        try {
            XxlJobHelper.log("开始从缓存计算身份证号[{}]的实际出勤天数", identityCard);
            
            // 从缓存中获取该身份证号的所有日考勤记录
            List<SwmDailyAttendance> records = dataCache.get(identityCard);
            
            if (records == null || records.isEmpty()) {
                XxlJobHelper.log("身份证号[{}]在缓存中没有找到日考勤记录，实际出勤天数为0", identityCard);
                return BigDecimal.ZERO;
            }
            
            // 统计有打卡记录的天数（clock_in_date 或 clock_out_date 不为空）
            long actualDays = records.stream()
                .filter(record -> record.getClockInDate() != null || record.getClockOutDate() != null)
                .count();
            
            XxlJobHelper.log("身份证号[{}]实际出勤天数计算完成: 总记录{}条, 有打卡记录{}天", 
                identityCard, records.size(), actualDays);
            
            return BigDecimal.valueOf(actualDays);
            
        } catch (Exception e) {
            XxlJobHelper.log("从缓存计算身份证号[{}]的实际出勤天数失败: {}", identityCard, e.getMessage());
            return BigDecimal.ZERO;
        }
    }

    /**
     * 计算应出勤天数
     * 应出勤天数 = 指定月份总天数 - 该月休息日天数
     * 
     * @param identityCard 身份证号
     * @param targetMonth 目标月份（格式：yyyy-MM）
     * @return 应出勤天数，如果计算失败返回null
     * @author Shawn
     * @date 2025-08-21
     */
    private BigDecimal calculateScheduledDays(String identityCard, String targetMonth) {
        try {
            XxlJobHelper.log("开始计算身份证号[{}]在{}月的应出勤天数", identityCard, targetMonth);
            
            // 1. 查询员工排班信息
            List<SwmPersonSchedule> personScheduleList = swmPersonScheduleService
                .findByIdCardAndMonth(identityCard, targetMonth);
            
            if (personScheduleList == null || personScheduleList.isEmpty()) {
                XxlJobHelper.log("身份证号[{}]在{}月没有排班信息，应出勤天数为0", identityCard, targetMonth);
                return BigDecimal.ZERO;
            }
            
            // 取第一条排班记录（一般一个月一个人只有一个班次）
            SwmPersonSchedule personSchedule = personScheduleList.get(0);
            String classes = personSchedule.getClasses();
            
            if (StringUtils.isBlank(classes)) {
                XxlJobHelper.log("身份证号[{}]在{}月的排班信息中班次为空，应出勤天数为0", identityCard, targetMonth);
                return BigDecimal.ZERO;
            }
            
            // 2. 根据班次查询休息日配置
            SwmScheduleTime scheduleTimeQuery = new SwmScheduleTime();
            scheduleTimeQuery.setShiftType(classes);
            List<SwmScheduleTime> scheduleTimeList = swmScheduleTimeService.findList(scheduleTimeQuery);
            
            if (scheduleTimeList == null || scheduleTimeList.isEmpty()) {
                XxlJobHelper.log("班次[{}]没有找到时间配置，应出勤天数为0", classes);
                return BigDecimal.ZERO;
            }
            
            SwmScheduleTime scheduleTime = scheduleTimeList.get(0);
            String restDays = scheduleTime.getRestDays();
            
            XxlJobHelper.log("身份证号[{}]，班次[{}]，休息日配置：{}", identityCard, classes, restDays);
            
            // 3. 计算目标月份的总天数
            int totalDaysInMonth = calculateTotalDaysInMonth(targetMonth);
            
            // 4. 计算该月的休息日天数
            int restDaysCount = calculateRestDaysInMonth(targetMonth, restDays);
            
            // 5. 计算应出勤天数
            int scheduledDays = totalDaysInMonth - restDaysCount;
            
            // 确保应出勤天数不为负数
            if (scheduledDays < 0) {
                scheduledDays = 0;
            }
            
            XxlJobHelper.log("身份证号[{}]在{}月：总天数{}天，休息日{}天，应出勤{}天", 
                identityCard, targetMonth, totalDaysInMonth, restDaysCount, scheduledDays);
            
            return BigDecimal.valueOf(scheduledDays);
            
        } catch (Exception e) {
            XxlJobHelper.log("计算身份证号[{}]在{}月的应出勤天数失败：{}", identityCard, targetMonth, e.getMessage());
            return null;
        }
    }

    /**
     * 计算指定月份的总天数
     * 
     * @param targetMonth 目标月份（格式：yyyy-MM）
     * @return 总天数
     * @author Shawn
     * @date 2025-08-21
     */
    private int calculateTotalDaysInMonth(String targetMonth) {
        try {
            SimpleDateFormat monthFormat = new SimpleDateFormat("yyyy-MM");
            Date monthDate = monthFormat.parse(targetMonth);
            
            Calendar cal = Calendar.getInstance();
            cal.setTime(monthDate);
            
            // 获取该月的最大天数
            int totalDays = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
            
            XxlJobHelper.log("{}月总天数：{}天", targetMonth, totalDays);
            return totalDays;
            
        } catch (Exception e) {
            XxlJobHelper.log("计算{}月总天数失败：{}", targetMonth, e.getMessage());
            return 0;
        }
    }

    /**
     * 计算指定月份的休息日天数
     * 
     * @param targetMonth 目标月份（格式：yyyy-MM）
     * @param restDays 休息日配置（格式：1,2,6,7 表示周一、周二、周六、周日）
     * @return 休息日天数
     * @author Shawn
     * @date 2025-08-21
     */
    private int calculateRestDaysInMonth(String targetMonth, String restDays) {
        try {
            if (StringUtils.isBlank(restDays)) {
                XxlJobHelper.log("{}月无休息日配置", targetMonth);
                return 0;
            }
            
            // 解析休息日配置
            String[] restDayArray = restDays.split(",");
            Set<Integer> restDaySet = new HashSet<>();
            
            for (String restDay : restDayArray) {
                try {
                    int day = Integer.parseInt(restDay.trim());
                    // 1-7 代表周一到周日
                    if (day >= 1 && day <= 7) {
                        restDaySet.add(day);
                    }
                } catch (NumberFormatException e) {
                    XxlJobHelper.log("无效的休息日配置：{}", restDay);
                }
            }
            
            if (restDaySet.isEmpty()) {
                XxlJobHelper.log("{}月没有有效的休息日配置", targetMonth);
                return 0;
            }
            
            // 计算该月的休息日天数
            SimpleDateFormat monthFormat = new SimpleDateFormat("yyyy-MM");
            Date monthDate = monthFormat.parse(targetMonth);
            
            Calendar cal = Calendar.getInstance();
            cal.setTime(monthDate);
            
            int totalDays = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
            int restDaysCount = 0;
            
            // 遍历该月的每一天
            for (int day = 1; day <= totalDays; day++) {
                cal.set(Calendar.DAY_OF_MONTH, day);
                
                // 获取星期几（Calendar.SUNDAY = 1, Calendar.MONDAY = 2, ..., Calendar.SATURDAY = 7）
                int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
                
                // 转换为 1-7 格式（1=周一，7=周日）
                int weekDay = dayOfWeek == Calendar.SUNDAY ? 7 : dayOfWeek - 1;
                
                // 检查是否是休息日
                if (restDaySet.contains(weekDay)) {
                    restDaysCount++;
                }
            }
            
            XxlJobHelper.log("{}月休息日配置[{}]，共有{}天休息日", targetMonth, restDays, restDaysCount);
            return restDaysCount;
            
        } catch (Exception e) {
            XxlJobHelper.log("计算{}月休息日天数失败：{}", targetMonth, e.getMessage());
            return 0;
        }
    }
}
