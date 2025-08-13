package com.jeesite.modules.job.task;

import cn.hutool.core.date.DateUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import com.jeesite.common.lang.StringUtils;
import com.jeesite.modules.swm.entity.*;
import com.jeesite.modules.swm.service.*;
import com.jeesite.modules.utils.R;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
    
    @Value("${tdengine.dbname:swm_db}")
    private String dbname;
    
    // 定义常量
    private static final long CONTINUITY_THRESHOLD_MS = 20 * 60 * 1000; // 20分钟连续性阈值
    private static final SimpleDateFormat DATETIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

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
        try {
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

            // 测试使用，生产上要删除 begin
            // java.util.List<String> employeeIds = new java.util.ArrayList<>();
            // employeeIds.add("1935182658452475904");
            // // employeeIds.add("1935182658850934784");
            // if (employeeIds != null && !employeeIds.isEmpty()) {
            // query.getSqlMap().getWhere().and("employee_id",
            // com.jeesite.common.mybatis.mapper.query.QueryType.IN,
            // employeeIds);
            // }
            // 测试使用，生产上要删除 end

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
                        // 功效 = 1 - 怠工时长/实际考勤时长
                        // @author: Shawn
                        // @date: 2025/06/23
                        BigDecimal dailyEfficiency = calculateDailyEfficiency(record.getIdleHours(),
                                record.getActualHours());
                        record.setDailyEfficiency(dailyEfficiency);
                        XxlJobHelper.log("员工[{}]{}日考勤功效计算: 1 - {}小时/{}小时 = {}",
                                record.getEmployeeId(), record.getEmployeeName(),
                                record.getIdleHours(), record.getActualHours(), dailyEfficiency);

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

    /**
     * 统计当月考勤数据
     */
    @XxlJob("calculateMonthlyAttendance")
    public void calculateMonthlyAttendance() {
        log.info("开始执行月考勤统计定时任务");

        // 1. 获取当月日期范围
        Date startDate = DateUtil.beginOfMonth(DateUtil.date());
        Date endDate = DateUtil.date(); // 统计到当天

        // 2. 查询当月所有员工的日考勤记录
        SwmDailyAttendance query = new SwmDailyAttendance();
        query.setBeginAttendanceDate(startDate);
        query.setEndAttendanceDate(endDate);
        // query.setEmployeeId("1935182658540556288"); // 注意，测试使用生产上要去掉，先写死。
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
                    monthStr);

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

            // 保存记录
            if (existingSummary != null) {
                swmAttendanceSummaryService.update(summary);
            } else {
                swmAttendanceSummaryService.save(summary);
            }
        }

        log.info("月考勤统计定时任务执行完成，共处理{}名员工的考勤数据", attendanceByEmployee.size());
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
                SwmScheduleTime scheduleTime = getScheduleTimeForPerson(person, today);
                if (scheduleTime == null) {
                    XxlJobHelper.log("员工[{}]{}没有排班信息，跳过创建考勤记录", person.getId(), person.getName());
                    continue;
                }
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

    /**
     * 获取员工的排班信息
     * 
     * @param person 员工信息
     * @param date   考勤日期
     * @return SwmScheduleTime 排班时间信息
     */
    private SwmScheduleTime getScheduleTimeForPerson(SwmPerson person, Date date) {
        // 1. 获取当前月份
        String month = DateUtil.format(date, "yyyy-MM");

        // 2. 查询员工的排班信息
        List<SwmPersonSchedule> personScheduleList = swmPersonScheduleService
                .findByIdCardAndMonth(person.getIdentityCard(), month);
        if (personScheduleList == null || personScheduleList.isEmpty()) {
            return null;
        }

        SwmPersonSchedule personSchedule = personScheduleList.get(0);
        if (personSchedule == null || personSchedule.getClasses() == null) {
            return null;
        }

        // 3. 查询班次对应的时间
        SwmScheduleTime scheduleTimeQuery = new SwmScheduleTime();
        scheduleTimeQuery.setShiftType(personSchedule.getClasses());
        List<SwmScheduleTime> scheduleTimeList = swmScheduleTimeService.findList(scheduleTimeQuery);
        if (scheduleTimeList == null || scheduleTimeList.isEmpty()) {
            return null;
        }
        return scheduleTimeList.get(0);
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
     * 功效 = 1 - 怠工时长/实际考勤时长
     * 
     * @param idleHours   怠工时长
     * @param actualHours 实际考勤时长
     * @return 日考勤功效
     * @author: Shawn
     * @date: 2025/06/23
     */
    private BigDecimal calculateDailyEfficiency(BigDecimal idleHours, BigDecimal actualHours) {
        if (actualHours == null || actualHours.compareTo(BigDecimal.ZERO) <= 0) {
            // 实际考勤时长为0或负数，功效为0
            return BigDecimal.ZERO;
        }

        if (idleHours == null) {
            idleHours = BigDecimal.ZERO;
        }

        try {
            // 功效 = 1 - 怠工时长/实际考勤时长
            BigDecimal idleRate = idleHours.divide(actualHours, 4, RoundingMode.HALF_UP);
            BigDecimal efficiency = BigDecimal.ONE.subtract(idleRate);

            // 确保功效在0到1之间
            if (efficiency.compareTo(BigDecimal.ZERO) < 0) {
                efficiency = BigDecimal.ZERO;
            } else if (efficiency.compareTo(BigDecimal.ONE) > 0) {
                efficiency = BigDecimal.ONE;
            }

            return efficiency.setScale(4, RoundingMode.HALF_UP);
        } catch (Exception e) {
            log.error("计算日考勤功效时发生异常，idleHours: {}, actualHours: {}", idleHours, actualHours, e);
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

        // 1. 优先判断正常条件
        if (isNormalAttendanceCondition(record)) {
            record.setAttendanceNormal("0");
            XxlJobHelper.log("员工[{}]{} 考勤状态更新为 [正常] - 完整打卡且应考勤时长>=实际考勤时长",
                    record.getEmployeeId(), record.getEmployeeName());
            return;
        }

        // 2. 判断异常条件
        if (isAbnormalAttendanceCondition(record)) {
            record.setAttendanceNormal("1");
            XxlJobHelper.log("员工[{}]{} 考勤状态更新为 [异常] - 满足异常条件",
                    record.getEmployeeId(), record.getEmployeeName());
            return;
        }

        // 3. 判断未考勤条件
        if (isNotAttendanceCondition(record)) {
            record.setAttendanceNormal("3");
            XxlJobHelper.log("员工[{}]{} 考勤状态更新为 [未考勤] - 没有打卡且无工作时长数据",
                    record.getEmployeeId(), record.getEmployeeName());
            return;
        }

        // 4. 其他情况保持原状态
        XxlJobHelper.log("员工[{}]{} 考勤状态保持不变: {}",
                record.getEmployeeId(), record.getEmployeeName(), originalStatus);
    }

    /**
     * 判断是否满足正常考勤条件
     * 条件：clockInTime != null AND clockOutTime != null AND scheduledHours != null
     * AND actualHours != null AND scheduledHours >= actualHours
     * 
     * @param record 考勤记录
     * @return true-满足正常条件，false-不满足
     * @author Shawn
     * @date 2025/01/27
     */
    private boolean isNormalAttendanceCondition(SwmDailyAttendance record) {
        Date clockInTime = record.getClockInTime();
        Date clockOutTime = record.getClockOutTime();
        BigDecimal scheduledHours = record.getScheduledHours();
        BigDecimal actualHours = record.getActualHours();

        // 5个条件必须全部满足
        boolean hasCompleteClockTimes = (clockInTime != null && clockOutTime != null);
        boolean hasScheduledHours = (scheduledHours != null);
        boolean hasActualHours = (actualHours != null);
        boolean scheduledGEActual = (scheduledHours != null && actualHours != null &&
                scheduledHours.compareTo(actualHours) >= 0);

        boolean isNormal = hasCompleteClockTimes && hasScheduledHours && hasActualHours && scheduledGEActual;

        // 详细日志记录（DEBUG级别）
        if (log.isDebugEnabled()) {
            log.debug("员工[{}]{} 正常条件判断详情: clockIn={}, clockOut={}, scheduled={}, actual={}, 结果={}",
                    record.getEmployeeId(), record.getEmployeeName(),
                    clockInTime != null, clockOutTime != null,
                    scheduledHours, actualHours, isNormal);
        }

        return isNormal;
    }

    /**
     * 判断是否满足异常考勤条件
     * 条件：满足任一即为异常
     * 条件A：clockInTime != null OR clockOutTime != null
     * 条件B：effectiveWorkHours != null AND effectiveWorkHours > 0
     * 条件C：actualHours != null AND actualHours > 0
     * 
     * @param record 考勤记录
     * @return true-满足异常条件，false-不满足
     * @author Shawn
     * @date 2025/01/27
     */
    private boolean isAbnormalAttendanceCondition(SwmDailyAttendance record) {
        Date clockInTime = record.getClockInTime();
        Date clockOutTime = record.getClockOutTime();
        BigDecimal effectiveWorkHours = record.getEffectiveWorkHours();
        BigDecimal actualHours = record.getActualHours();

        // 满足任一条件即为异常
        boolean hasPartialClockRecord = (clockInTime != null || clockOutTime != null);
        boolean hasEffectiveWorkHours = (effectiveWorkHours != null
                && effectiveWorkHours.compareTo(BigDecimal.ZERO) > 0);
        boolean hasActualHours = (actualHours != null && actualHours.compareTo(BigDecimal.ZERO) > 0);

        boolean isAbnormal = hasPartialClockRecord || hasEffectiveWorkHours || hasActualHours;

        // 详细日志记录（DEBUG级别）
        if (log.isDebugEnabled()) {
            log.debug("员工[{}]{} 异常条件判断详情: 部分打卡记录={}, 实际工作时长有值={}, 实际考勤时长有值={}, 结果={}",
                    record.getEmployeeId(), record.getEmployeeName(),
                    hasPartialClockRecord, hasEffectiveWorkHours, hasActualHours, isAbnormal);
        }

        return isAbnormal;
    }

    /**
     * 判断是否满足未考勤条件
     * 条件：clockInTime == null AND clockOutTime == null AND effectiveWorkHours ==
     * null AND actualHours == null
     * 
     * @param record 考勤记录
     * @return true-满足未考勤条件，false-不满足
     * @author Shawn
     * @date 2025/01/27
     */
    private boolean isNotAttendanceCondition(SwmDailyAttendance record) {
        Date clockInTime = record.getClockInTime();
        Date clockOutTime = record.getClockOutTime();
        BigDecimal effectiveWorkHours = record.getEffectiveWorkHours();
        BigDecimal actualHours = record.getActualHours();

        // 4个条件必须全部满足
        boolean noClockRecord = (clockInTime == null && clockOutTime == null);
        boolean noEffectiveWorkHours = (effectiveWorkHours == null
                || effectiveWorkHours.compareTo(BigDecimal.ZERO) <= 0);
        boolean noActualHours = (actualHours == null || actualHours.compareTo(BigDecimal.ZERO) <= 0);

        boolean isNotAttendance = noClockRecord && noEffectiveWorkHours && noActualHours;

        // 详细日志记录（DEBUG级别）
        if (log.isDebugEnabled()) {
            log.debug("员工[{}]{} 未考勤条件判断详情: 无打卡记录={}, 无实际工作时长={}, 无实际考勤时长={}, 结果={}",
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
     *    - startTime=2025-01-01 00:00:00;endTime=2025-01-31 23:59:59;idCards=110101199001011234
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
        jobLog.setStartTime(new Date());
        jobLog.setExecuteStatus("1"); // 默认失败
        
        try {
            XxlJobHelper.log("开始执行自定义时间范围考勤计算任务...");
            
            // 保存任务参数
            String jobParam = XxlJobHelper.getJobParam();
            jobLog.setJobParam(jobParam);
            swmJobLogService.save(jobLog);
            jobLog.setIsNewRecord(false);
            
            // 1. 解析参数
            AttendanceParams params = parseAttendanceParams();
            
            // 2. 查询待处理记录
            List<SwmDailyAttendance> records = queryPendingAttendanceRecords(params);
            
            if (records.isEmpty()) {
                XxlJobHelper.log("没有找到待处理的考勤记录");
                jobLog.setExecuteStatus("0"); // 成功
                return;
            }
            
            // 3. 批量处理记录
            AttendanceResult result = processAttendanceRecords(records);
            
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
        }
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
    private AttendanceResult processAttendanceRecords(List<SwmDailyAttendance> records) {
        AttendanceResult result = new AttendanceResult();
        result.totalCount = records.size();
        
        for (SwmDailyAttendance record : records) {
            try {
                boolean processed = processAttendanceRecord(record);
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
    private boolean processAttendanceRecord(SwmDailyAttendance record) {
        boolean updated = false;
        
        // 计算实际考勤时长
        if (shouldCalculateActualHours(record)) {
            calculateAndUpdateActualHours(record);
            updated = true;
        }
        
        // 计算实际工作时长
        if (shouldCalculateWorkHours(record)) {
            BigDecimal workHours = calculateWorkAreaHours(record);
            if (workHours != null) {
                record.setEffectiveWorkHours(workHours);
                updated = true;
                XxlJobHelper.log("员工[{}]{}实际工作时长更新为: {} 小时",
                    record.getEmployeeId(), record.getEmployeeName(), workHours);
            }
            
            // 计算怠工时长
            BigDecimal idleHours = calculateIdleAreaHours(record);
            if (idleHours != null) {
                record.setIdleHours(idleHours);
                updated = true;
                XxlJobHelper.log("员工[{}]{}怠工时长更新为: {} 小时",
                    record.getEmployeeId(), record.getEmployeeName(), idleHours);
            }
        }
        
        // 后续可以添加其他计算
        // if (shouldCalculateIdleHours(record)) {
        //     calculateAndUpdateIdleHours(record);
        //     updated = true;
        // }
        
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
            // 功效 = 1 - 怠工时长/实际考勤时长
            // @author: Shawn
            // @date: 2025/01/13
            BigDecimal dailyEfficiency = calculateDailyEfficiency(record.getIdleHours(), record.getActualHours());
            record.setDailyEfficiency(dailyEfficiency);
            XxlJobHelper.log("员工[{}]{}日考勤功效计算: 1 - {}小时/{}小时 = {}",
                record.getEmployeeId(), record.getEmployeeName(),
                record.getIdleHours(), record.getActualHours(), dailyEfficiency);
            
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
        return record.getClockInTime() != null && record.getClockOutTime() != null;
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
        String idCard;
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
     * 支持参数化生成考勤记录
     * 默认生成明天的考勤数据
     * 参数格式：
     * - 无参数 - 生成明天所有在职人员的考勤
     * - date=2025-08-12 - 生成指定日期的考勤
     * - idCard=412825197709304513 - 生成指定身份证明天的考勤
     * - date=2025-08-12,idCard=412825197709304513 - 生成指定日期和身份证的考勤
     * 
     * @author Shawn
     * @date 2025-08-12
     */
    @XxlJob("createDailyAttendanceV2")
    public void createDailyAttendanceV2() {
        SwmJobLog jobLog = initJobLog("createDailyAttendanceV2");
        
        try {
            // 1. 解析参数
            AttendanceGenerationParams params = parseAttendanceGenerationParams();
            
            // 2. 查询目标人员
            List<SwmPerson> targetPersons = queryTargetPersons(params);
            if (targetPersons.isEmpty()) {
                XxlJobHelper.log("没有需要处理的在职人员");
                jobLog.setExecuteStatus("0");
                return;
            }
            
            // 3. 生成考勤记录
            AttendanceGenerationResult result = generateAttendanceRecords(targetPersons, params.targetDate);
            
            // 4. 输出统计结果
            logGenerationResult(result, targetPersons.size());
            
            jobLog.setExecuteStatus("0"); // 成功
            
        } catch (Exception e) {
            XxlJobHelper.log("创建每日考勤数据V2时发生异常", e);
            jobLog.setExceptionInfo(e.getMessage());
        } finally {
            saveJobLog(jobLog);
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
            
            // 解析身份证参数
            params.idCard = paramMap.get("idCard");
            if (StringUtils.isNotBlank(params.idCard)) {
                XxlJobHelper.log("使用指定身份证: {}", params.idCard);
            }
        }
        
        return params;
    }

    /**
     * 查询目标人员
     */
    private List<SwmPerson> queryTargetPersons(AttendanceGenerationParams params) {
        if (StringUtils.isNotBlank(params.idCard)) {
            return querySinglePerson(params.idCard);
        } else {
            return queryAllActivePersons();
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
     * 查询所有在职人员
     */
    private List<SwmPerson> queryAllActivePersons() {
        SwmPerson query = new SwmPerson();
        query.setPersonnelStatus(SwmPerson.PersonStatusEnum.ACTIVE);
        query.setStatus("0");
        List<SwmPerson> persons = swmPersonService.findList(query);
        XxlJobHelper.log("查询到 {} 名在职人员", persons.size());
        return persons;
    }

    /**
     * 生成考勤记录
     */
    private AttendanceGenerationResult generateAttendanceRecords(List<SwmPerson> persons, Date targetDate) {
        AttendanceGenerationResult result = new AttendanceGenerationResult();
        String dateStr = new SimpleDateFormat("yyyy-MM-dd").format(targetDate);
        
        for (SwmPerson person : persons) {
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
            SwmDailyAttendance existing = swmDailyAttendanceService
                .findByIdentityCardAndDate(person.getIdentityCard(), targetDate);
            
            if (existing != null) {
                return handleExistingAttendance(existing, person, targetDate, dateStr);
            }
            
            // 创建新记录
            createNewAttendance(person, targetDate);
            return ProcessResult.CREATED;
            
        } catch (Exception e) {
            XxlJobHelper.log("为员工[{}]{}创建考勤记录失败：{}", 
                person.getId(), person.getName(), e.getMessage());
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
        if (StringUtils.isEmpty(existing.getWorkTimeRange())) {
            SwmScheduleTime scheduleTime = getScheduleTimeForPerson(person, targetDate);
            if (scheduleTime != null) {
                String workTimeRange = scheduleTime.getStartTime() + "-" + scheduleTime.getEndTime();
                existing.setWorkTimeRange(workTimeRange);
                
                // 计算应考勤时长
                BigDecimal scheduledHours = calculateScheduledHoursFromWorkTimeRange(workTimeRange);
                existing.setScheduledHours(scheduledHours);
                
                needUpdate = true;
                XxlJobHelper.log("更新排班信息：{}，应考勤时长：{} 小时", workTimeRange, scheduledHours);
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
        
        swmDailyAttendanceService.save(attendance);
        XxlJobHelper.log("为员工[{}]{}创建考勤记录成功", person.getId(), person.getName());
    }

    /**
     * 构建新的考勤记录对象
     */
    private SwmDailyAttendance buildNewAttendance(SwmPerson person, Date targetDate) {
        SwmDailyAttendance attendance = new SwmDailyAttendance();
        attendance.setEmployeeId(person.getId());
        attendance.setEmployeeName(person.getName());
        attendance.setIdentityCard(person.getIdentityCard());
        attendance.setPersonType(person.getPersonType());
        attendance.setAttendanceDate(targetDate);
        return attendance;
    }

    /**
     * 根据工作时间范围计算应考勤时长（用于createDailyAttendanceV2）
     * @param workTimeRange 工作时间范围，格式如 "07:00-18:00"
     * @return 应考勤时长（小时）
     * @author Shawn
     * @date 2025-08-13
     */
    private BigDecimal calculateScheduledHoursFromWorkTimeRange(String workTimeRange) {
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
                XxlJobHelper.log("检测到跨日班次: {}，计算后的应考勤时长: {} 小时", workTimeRange, hours);
            }
            
            // 确保时长在合理范围内
            if (hours > 24) {
                XxlJobHelper.log("工作时间范围 {} 计算出的时长超过24小时: {} 小时，可能存在配置错误", workTimeRange, hours);
                return BigDecimal.ZERO;
            }
            
            return BigDecimal.valueOf(hours).setScale(2, RoundingMode.HALF_UP);
            
        } catch (Exception e) {
            XxlJobHelper.log("计算应考勤时长时发生异常，workTimeRange: {}, 错误: {}", workTimeRange, e.getMessage());
            return BigDecimal.ZERO;
        }
    }

    /**
     * 设置排班信息
     */
    private void setScheduleInfo(SwmDailyAttendance attendance, SwmPerson person, Date targetDate) {
        SwmScheduleTime scheduleTime = getScheduleTimeForPerson(person, targetDate);
        if (scheduleTime != null) {
            String workTimeRange = scheduleTime.getStartTime() + "-" + scheduleTime.getEndTime();
            attendance.setWorkTimeRange(workTimeRange);
            
            // 计算应考勤时长
            BigDecimal scheduledHours = calculateScheduledHoursFromWorkTimeRange(workTimeRange);
            attendance.setScheduledHours(scheduledHours);
            
            XxlJobHelper.log("员工[{}]{}有排班信息：{}，应考勤时长：{} 小时", 
                person.getId(), person.getName(), workTimeRange, scheduledHours);
        } else {
            attendance.setWorkTimeRange(null);
            attendance.setScheduledHours(BigDecimal.ZERO);
            XxlJobHelper.log("员工[{}]{}没有排班信息", person.getId(), person.getName());
        }
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
        attendance.setAttendanceNormal("3"); // 未考勤
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
        return record.getClockInTime() != null || 
               record.getClockOutTime() != null || 
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
    private BigDecimal calculateAreaHours(SwmDailyAttendance record, String areaType, String areaName) {
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
                areaType
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
    private BigDecimal calculateWorkAreaHours(SwmDailyAttendance record) {
        return calculateAreaHours(record, "0", "工作区");
    }
    
    /**
     * 计算休息区域活动时长/怠工时长（包装方法）
     * @param record 考勤记录
     * @return 怠工时长（小时）
     * @author Shawn
     * @date 2025-08-13
     */
    private BigDecimal calculateIdleAreaHours(SwmDailyAttendance record) {
        return calculateAreaHours(record, "1", "休息区");
    }
    
    /**
     * 确定查询时间范围
     * @param record 考勤记录
     * @return 时间范围数组 [开始时间, 结束时间]，如果无法确定返回null
     * @author Shawn
     * @date 2025-08-13
     */
    private String[] determineQueryTimeRange(SwmDailyAttendance record) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
        String attendanceDate = dateFormat.format(record.getAttendanceDate());
        
        // 优先级1：使用实际打卡时间
        if (record.getClockInTime() != null && record.getClockOutTime() != null) {
            String startTime = attendanceDate + " " + timeFormat.format(record.getClockInTime());
            String endTime = attendanceDate + " " + timeFormat.format(record.getClockOutTime());
            
            // 处理跨天情况
            if (record.getClockOutTime().before(record.getClockInTime())) {
                Calendar cal = Calendar.getInstance();
                cal.setTime(record.getAttendanceDate());
                cal.add(Calendar.DAY_OF_MONTH, 1);
                String nextDay = dateFormat.format(cal.getTime());
                endTime = nextDay + " " + timeFormat.format(record.getClockOutTime());
            }
            
            return new String[]{startTime, endTime};
        }
        
        // 优先级2：只有上班打卡时间 + 应考勤时间的下班时间
        if (record.getClockInTime() != null && StringUtils.isNotBlank(record.getWorkTimeRange())) {
            String[] times = record.getWorkTimeRange().split("-");
            if (times.length == 2) {
                String startTime = attendanceDate + " " + timeFormat.format(record.getClockInTime());
                String endTime = attendanceDate + " " + times[1].trim() + ":00";
                
                // 处理跨天
                if (times[1].trim().compareTo(times[0].trim()) < 0) {
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(record.getAttendanceDate());
                    cal.add(Calendar.DAY_OF_MONTH, 1);
                    endTime = dateFormat.format(cal.getTime()) + " " + times[1].trim() + ":00";
                }
                
                return new String[]{startTime, endTime};
            }
        }
        
        // 优先级3：只有下班打卡时间 + 应考勤时间的上班时间
        if (record.getClockOutTime() != null && StringUtils.isNotBlank(record.getWorkTimeRange())) {
            String[] times = record.getWorkTimeRange().split("-");
            if (times.length == 2) {
                String startTime = attendanceDate + " " + times[0].trim() + ":00";
                String endTime = attendanceDate + " " + timeFormat.format(record.getClockOutTime());
                
                // 处理跨天
                if (timeFormat.format(record.getClockOutTime()).compareTo(times[0].trim()) < 0) {
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(record.getAttendanceDate());
                    cal.add(Calendar.DAY_OF_MONTH, 1);
                    endTime = dateFormat.format(cal.getTime()) + " " + timeFormat.format(record.getClockOutTime());
                }
                
                return new String[]{startTime, endTime};
            }
        }
        
        // 优先级4：只有应考勤时间范围
        if (StringUtils.isNotBlank(record.getWorkTimeRange())) {
            String[] times = record.getWorkTimeRange().split("-");
            if (times.length == 2) {
                String startTime = attendanceDate + " " + times[0].trim() + ":00";
                String endTime = attendanceDate + " " + times[1].trim() + ":00";
                
                // 处理跨天班次
                if (times[1].trim().compareTo(times[0].trim()) < 0) {
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(record.getAttendanceDate());
                    cal.add(Calendar.DAY_OF_MONTH, 1);
                    endTime = dateFormat.format(cal.getTime()) + " " + times[1].trim() + ":00";
                }
                
                return new String[]{startTime, endTime};
            }
        }
        
        // 无法确定时间范围
        return null;
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
    private List<WorkSegment> queryAreaSegments(String idCard, String startTime, String endTime, String areaType) {
        List<WorkSegment> segments = new ArrayList<>();
        
        try {
            // 查询指定区域的时间戳
            String sql = String.format(
                "SELECT time FROM %s.area_fence_data " +
                "WHERE id_card = '%s' " +
                "AND area_type = '%s' " +
                "AND time >= '%s' " +
                "AND time <= '%s' " +
                "ORDER BY time ASC",
                dbname, idCard, areaType, startTime, endTime
            );
            
            XxlJobHelper.log("查询区域数据SQL: {}", sql);
            
            R<JSONObject> response = tdengineService.executeTDengineSQL(sql);
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
                    
                    // 补充开始时间：如果第一条数据晚于查询开始时间且在30分钟内
                    long startGap = firstData.getTime() - queryStart.getTime();
                    if (startGap > 0 && startGap <= CONTINUITY_THRESHOLD_MS) {
                        timestamps.add(0, queryStart);
                        XxlJobHelper.log("第一条数据晚于打卡时间{}分钟，补充打卡时间作为开始", 
                            startGap / (60 * 1000));
                    }
                    
                    // 补充结束时间：如果最后一条数据早于查询结束时间且在30分钟内
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
}
