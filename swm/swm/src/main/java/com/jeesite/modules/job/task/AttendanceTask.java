package com.jeesite.modules.job.task;

import cn.hutool.core.date.DateUtil;
import com.jeesite.modules.swm.entity.*;
import com.jeesite.modules.swm.service.*;
import com.jeesite.modules.utils.R;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
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

            if (StringUtils.hasText(jobParam)) {
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
            // query.setEmployeeId("1935182658540556288"); // 注意，测试使用生产上要去掉，先写死。
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
                            // 没有打卡时间且TDengine也没有可用数据，实际考勤时长为0
                            record.setActualHours(BigDecimal.ZERO);
                            XxlJobHelper.log("员工[{}]{}没有完整的打卡记录且TDengine也无可用数据，实际考勤时长为0",
                                    record.getEmployeeId(), record.getEmployeeName());
                        } else {
                            // 有完整打卡时间（可能来自TDengine），计算实际工作时长
                            BigDecimal clockWorkHours = calculateWorkHoursBetweenTimes(clockInTime, clockOutTime);

                            // 实际考勤时长 = 打卡工作时长 - 怠工时长
                            BigDecimal actualHours = clockWorkHours.subtract(record.getIdleHours());

                            // 确保实际考勤时长不为负数
                            if (actualHours.compareTo(BigDecimal.ZERO) < 0) {
                                actualHours = BigDecimal.ZERO;
                            }

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
        try {
            XxlJobHelper.log("开始执行每日考勤数据创建任务...");

            // 1. 获取当前日期
            Date today = new Date();

            // 2. 查询所有在职人员
            SwmPerson query = new SwmPerson();
            query.setPersonnelStatus(SwmPerson.PersonStatusEnum.ACTIVE); // 在职状态
            query.setStatus("0");// 正常状态
            // query.setIdentityCard("412825197709304513"); // todo为了测试身份证先写死
            List<SwmPerson> activePersons = swmPersonService.findList(query);

            if (activePersons.isEmpty()) {
                XxlJobHelper.log("没有在职人员，无需创建考勤记录");
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
                String workTimeRange = getWorkTimeRangeForPerson(person, today);
                if (workTimeRange == null) {
                    XxlJobHelper.log("员工[{}]{}没有排班信息，跳过创建考勤记录", person.getId(), person.getName());
                    continue;
                }

                // 3.3 计算应考勤时长
                BigDecimal scheduledHours = calculateScheduledHours(workTimeRange);

                // 3.4 创建或更新考勤记录
                if (existingAttendance == null) {
                    // 创建新记录
                    SwmDailyAttendance newAttendance = new SwmDailyAttendance();
                    newAttendance.setEmployeeId(person.getId());
                    newAttendance.setEmployeeName(person.getName());
                    newAttendance.setAttendanceDate(today);
                    newAttendance.setWorkTimeRange(workTimeRange);
                    newAttendance.setScheduledHours(scheduledHours);
                    newAttendance.setActualHours(BigDecimal.ZERO); // 默认实际考勤时长为0
                    newAttendance.setIdleHours(BigDecimal.ZERO); // 默认怠工时长为0
                    newAttendance.setDailyEfficiency(BigDecimal.ZERO); // 默认功效为0
                    newAttendance.setDailyAchievementRate(BigDecimal.ZERO); // 默认达成率为0
                    newAttendance.setAttendanceNormal("0"); // 默认考勤正常

                    swmDailyAttendanceService.save(newAttendance);
                    createdCount++;
                } else {
                    // 更新现有记录
                    existingAttendance.setWorkTimeRange(workTimeRange);
                    existingAttendance.setScheduledHours(scheduledHours);
                    // todo 调用接口获取怠工时长、考勤是否正常等

                    // 保留原有的实际考勤数据
                    swmDailyAttendanceService.save(existingAttendance);
                    updatedCount++;
                }
            }

            XxlJobHelper.log("每日考勤数据创建任务完成。共创建{}条记录，更新{}条记录", createdCount, updatedCount);
        } catch (Exception e) {
            XxlJobHelper.log("创建每日考勤数据时发生异常", e);
        }
    }

    /**
     * 获取员工的应考勤时间范围
     * 
     * @param person 员工信息
     * @param date   考勤日期
     * @return 应考勤时间范围字符串，格式如"08:00-17:00"
     */
    private String getWorkTimeRangeForPerson(SwmPerson person, Date date) {
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
        SwmScheduleTime scheduleTime = scheduleTimeList.get(0);

        if (scheduleTime == null) {
            return null;
        }

        // 4. 组合时间范围字符串
        return scheduleTime.getStartTime() + "-" + scheduleTime.getEndTime();
    }

    /**
     * 计算应考勤时长
     * 
     * @param workTimeRange 工作时间范围，格式如"08:00-17:00"或"20:30-03:00"
     * @return 应考勤时长(小时)
     */
    private BigDecimal calculateScheduledHours(String workTimeRange) {
        if (workTimeRange == null || !workTimeRange.contains("-")) {
            return BigDecimal.ZERO;
        }

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

            // 验证时长的合理性（一般工作时长应该在1-16小时之间）
            if (hours < 1 || hours > 16) {
                log.warn("工作时间范围 {} 计算出的时长 {} 小时可能不合理", workTimeRange, hours);
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
}
