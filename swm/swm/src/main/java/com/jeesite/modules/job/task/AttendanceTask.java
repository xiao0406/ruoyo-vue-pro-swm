package com.jeesite.modules.job.task;

import cn.hutool.core.date.DateUtil;
import com.jeesite.modules.swm.entity.*;
import com.jeesite.modules.swm.service.*;
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

    /**
     * 计算怠工时长定时任务，工作时长定时任务
     * 
     * @author: Shawn
     * @date: 2025/6/20
     */
    @XxlJob("calculateIdleHours")
    public void calculateIdleHours() {
        try {
            XxlJobHelper.log("开始执行怠工时长计算任务...");

            // 获取传入的日期参数，如果没有传入则使用当天
            String jobParam = XxlJobHelper.getJobParam();
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
            query.setEmployeeId("1935182658540556288"); // 注意，测试使用生产上要去掉，先写死。
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

                        // 计算实际考勤时长
                        // 修改逻辑: 1. 如果没有上下班打卡时间，实际考勤为0
                        // 2. 如果有上下班打卡时间，实际考勤 = 下班打卡时间 - 上班打卡时间 - 怠工时长
                        // @author: Shawn
                        // @date: 2025/06/23
                        if (record.getClockInTime() == null || record.getClockOutTime() == null) {
                            // 没有打卡时间，实际考勤时长为0
                            record.setActualHours(BigDecimal.ZERO);
                            XxlJobHelper.log("员工[{}]{}没有完整的上下班打卡记录，实际考勤时长为0",
                                    record.getEmployeeId(), record.getEmployeeName());
                        } else {
                            // 有完整打卡时间，计算实际工作时长
                            BigDecimal clockWorkHours = calculateWorkHoursBetweenTimes(
                                    record.getClockInTime(), record.getClockOutTime());

                            // 实际考勤时长 = 打卡工作时长 - 怠工时长
                            BigDecimal actualHours = clockWorkHours.subtract(record.getIdleHours());

                            // 确保实际考勤时长不为负数
                            if (actualHours.compareTo(BigDecimal.ZERO) < 0) {
                                actualHours = BigDecimal.ZERO;
                            }

                            record.setActualHours(actualHours.setScale(2, RoundingMode.HALF_UP));
                            XxlJobHelper.log("员工[{}]{}实际考勤时长计算: 打卡工作{}小时 - 怠工{}小时 = 实际{}小时",
                                    record.getEmployeeId(), record.getEmployeeName(),
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
        } catch (Exception e) {
            XxlJobHelper.log("怠工时长计算任务执行异常", e);
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
        query.setEmployeeId("1935182658540556288"); // 注意，测试使用生产上要去掉，先写死。
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
            // query.setIdentityCard("430312122334343333"); // todo为了测试身份证先写死
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
     * @param workTimeRange 工作时间范围，格式如"08:00-17:00"
     * @return 应考勤时长(小时)
     */
    private BigDecimal calculateScheduledHours(String workTimeRange) {
        if (workTimeRange == null || !workTimeRange.contains("-")) {
            return BigDecimal.ZERO;
        }

        try {
            String[] times = workTimeRange.split("-");
            String startTimeStr = times[0];
            String endTimeStr = times[1];

            // 解析时间
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
            Date startTime = sdf.parse(startTimeStr);
            Date endTime = sdf.parse(endTimeStr);

            // 计算时间差(毫秒)
            long diffMillis = endTime.getTime() - startTime.getTime();

            // 转换为小时
            double hours = diffMillis / (1000.0 * 60 * 60);

            // 考虑跨日班次的情况(如夜班)
            if (hours < 0) {
                hours += 24;
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

            // 如果下班时间小于上班时间，说明是跨日班次
            if (diffMillis < 0) {
                diffMillis += 24 * 60 * 60 * 1000; // 加上24小时的毫秒数
            }

            // 转换为小时
            double hours = diffMillis / (1000.0 * 60 * 60);

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
}
