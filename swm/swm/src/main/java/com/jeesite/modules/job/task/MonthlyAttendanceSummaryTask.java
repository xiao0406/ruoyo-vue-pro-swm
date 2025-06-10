package com.jeesite.modules.job.task;

import cn.hutool.core.date.DateUtil;
import com.jeesite.modules.swm.entity.SwmAttendanceSummary;
import com.jeesite.modules.swm.entity.SwmDailyAttendance;
import com.jeesite.modules.swm.entity.SwmPerson;
import com.jeesite.modules.swm.entity.SwmPersonSchedule;
import com.jeesite.modules.swm.service.SwmAttendanceSummaryService;
import com.jeesite.modules.swm.service.SwmDailyAttendanceService;
import com.jeesite.modules.swm.service.SwmPersonScheduleService;
import com.jeesite.modules.swm.service.SwmPersonService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 月考勤统计定时任务
 * @author: cjie
 * @date: 2025/6/10
 */
@Component
@Slf4j
public class MonthlyAttendanceSummaryTask {
    @Autowired
    private SwmDailyAttendanceService swmDailyAttendanceService;
    @Autowired
    private SwmAttendanceSummaryService swmAttendanceSummaryService;
    @Autowired
    private SwmPersonService swmPersonService;
    @Autowired
    private SwmPersonScheduleService swmPersonScheduleService;

    /**
     * 统计当月考勤数据
     */
    public void currentMonth() {
        log.info("开始执行月考勤统计定时任务");

        // 1. 获取当月日期范围
        Date startDate = DateUtil.beginOfMonth(DateUtil.date());
        Date endDate = DateUtil.date(); // 统计到当天

        // 2. 查询当月所有员工的日考勤记录
        SwmDailyAttendance query = new SwmDailyAttendance();
        query.setBeginAttendanceDate(startDate);
        query.setEndAttendanceDate(endDate);
        List<SwmDailyAttendance> dailyAttendanceList = swmDailyAttendanceService.findList(query);

        // 3. 按员工ID分组
        Map<String, List<SwmDailyAttendance>> attendanceByEmployee = dailyAttendanceList.stream()
                .collect(Collectors.groupingBy(SwmDailyAttendance::getEmployeeId));

        // 4. 计算当月应出勤天数(本月1号到当天的天数)
        int scheduledDays = calculateScheduledDays(startDate, endDate);

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
            BigDecimal attendanceRate = actualDays > 0 ?
                    BigDecimal.valueOf(actualDays).divide(BigDecimal.valueOf(scheduledDays), 4, RoundingMode.HALF_UP) :
                    BigDecimal.ZERO;

            // 计算总时长
            BigDecimal totalScheduledHours = employeeAttendance.stream()
                    .map(SwmDailyAttendance::getScheduledHours)
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
            SwmAttendanceSummary summaryQuery = new SwmAttendanceSummary();
            summaryQuery.setEmployeeId(employeeId);
            summaryQuery.setMonth(monthStr);
            SwmAttendanceSummary existingSummary = swmAttendanceSummaryService.get(summaryQuery);

            // 7. 保存或更新记录
            SwmAttendanceSummary summary = existingSummary != null ? existingSummary : new SwmAttendanceSummary();

            // 设置基本信息
            summary.setEmployeeId(employeeId);
            summary.setEmployeeName(person.getName());
            summary.setDepartment(person.getDepartment());
            summary.setWorkProcess(person.getWorkProcess());
            summary.setTeam(person.getTeam());
            summary.setJobType(person.getJobType());

            //获取人员本月班次(不确定人员一个月是不是只能有一个班次，这里用list查询，取第一个)
            SwmPersonSchedule queryPersonSchedule = new SwmPersonSchedule();
            queryPersonSchedule.setPersonName(person.getName());
            queryPersonSchedule.setMonth(monthStr);
            List<SwmPersonSchedule> personScheduleList = swmPersonScheduleService.findList(queryPersonSchedule);
            if(personScheduleList != null && !personScheduleList.isEmpty()){
                summary.setWorkShift(personScheduleList.get(0).getClasses());
            }
            summary.setMonth(monthStr);

            // 设置统计信息
            summary.setScheduledDays(BigDecimal.valueOf(scheduledDays));
            summary.setActualDays(BigDecimal.valueOf(actualDays));
            summary.setAttendanceRate(attendanceRate);
            summary.setScheduledHours(totalScheduledHours);
            summary.setActualHours(totalActualHours);
            summary.setIdleHours(totalIdleHours);
            summary.setEfficiency(avgEfficiency);

            // 计算考勤达成率
            if (totalScheduledHours.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal achievementRate = totalActualHours.divide(totalScheduledHours, 4, RoundingMode.HALF_UP);
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
     * 计算应出勤天数(从开始日期到结束日期的天数)
     */
    private int calculateScheduledDays(Date startDate, Date endDate) {
        long diffInMillis = endDate.getTime() - startDate.getTime();
        return (int) (diffInMillis / (1000 * 60 * 60 * 24)) + 1; // 包含首尾两天
    }
}
