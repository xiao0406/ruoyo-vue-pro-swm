package com.jeesite.modules.swm.service;

import com.alibaba.cloud.commons.lang.StringUtils;
import com.jeesite.common.entity.Page;
import com.jeesite.common.service.CrudService;
import com.jeesite.modules.swm.dao.SwmAttendanceSummaryDao;
import com.jeesite.modules.swm.entity.SwmAttendanceSummary;
import com.jeesite.modules.swm.entity.SwmDailyAttendance;
import com.jeesite.modules.swm.entity.SwmPersonSchedule;
import com.jeesite.modules.swm.entity.SwmScheduleTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 考勤月统计表Service
 * 
 * @author zwf
 * @version 2025-05-20
 */
@Service
@Transactional(readOnly = true)
public class SwmAttendanceSummaryService extends CrudService<SwmAttendanceSummaryDao, SwmAttendanceSummary> {

    @Autowired
    private SwmPersonScheduleService swmPersonScheduleService;

    @Autowired
    private SwmScheduleTimeService swmScheduleTimeService;

    @Autowired
    private SwmDailyAttendanceService swmDailyAttendanceService;

    /**
     * 获取单条数据
     * 
     * @param swmAttendanceSummary
     * @return
     */
    @Override
    public SwmAttendanceSummary get(SwmAttendanceSummary swmAttendanceSummary) {
        return super.get(swmAttendanceSummary);
    }

    /**
     * 查询分页数据
     * 
     * @param swmAttendanceSummary
     * @return
     */
    @Override
    public Page<SwmAttendanceSummary> findPage(SwmAttendanceSummary swmAttendanceSummary) {
        return super.findPage(swmAttendanceSummary);
    }

    /**
     * 查询列表数据
     * 
     * @param swmAttendanceSummary
     * @return
     */
    @Override
    public List<SwmAttendanceSummary> findList(SwmAttendanceSummary swmAttendanceSummary) {
        return super.findList(swmAttendanceSummary);
    }

    /**
     * 保存数据（插入或更新）
     * 保存时自动计算：出勤率、考勤达成率
     * 
     * @param swmAttendanceSummary
     */
    @Override
    @Transactional(readOnly = false)
    public void save(SwmAttendanceSummary swmAttendanceSummary) {
        // 如果是新记录，进行数据初始化
        if (swmAttendanceSummary.getIsNewRecord()) {
            // 设置默认值，避免空指针异常
            if (swmAttendanceSummary.getScheduledDays() == null) {
                swmAttendanceSummary.setScheduledDays(BigDecimal.ZERO);
            }
            if (swmAttendanceSummary.getActualDays() == null) {
                swmAttendanceSummary.setActualDays(BigDecimal.ZERO);
            }
            if (swmAttendanceSummary.getScheduledHours() == null) {
                swmAttendanceSummary.setScheduledHours(BigDecimal.ZERO);
            }
            if (swmAttendanceSummary.getActualHours() == null) {
                swmAttendanceSummary.setActualHours(BigDecimal.ZERO);
            }
            if (swmAttendanceSummary.getIdleHours() == null) {
                swmAttendanceSummary.setIdleHours(BigDecimal.ZERO);
            }
            if (swmAttendanceSummary.getActualAttendanceDays() == null) {
                swmAttendanceSummary.setActualAttendanceDays(BigDecimal.ZERO);
            }
        }

        // 计算出勤率 = 实际出勤天数 / 应出勤天数
        if (swmAttendanceSummary.getScheduledDays() != null &&
                swmAttendanceSummary.getActualDays() != null &&
                swmAttendanceSummary.getScheduledDays().compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal attendanceRate = swmAttendanceSummary.getActualDays()
                    .divide(swmAttendanceSummary.getScheduledDays(), 4, RoundingMode.HALF_UP);
            swmAttendanceSummary.setAttendanceRate(attendanceRate);
        } else {
            swmAttendanceSummary.setAttendanceRate(BigDecimal.ZERO);
        }

        // 计算考勤达成率 = 实际工作时间 / 应考勤时间
        if (swmAttendanceSummary.getScheduledHours() != null &&
                swmAttendanceSummary.getActualHours() != null &&
                swmAttendanceSummary.getScheduledHours().compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal attendanceAchievementRate = swmAttendanceSummary.getActualHours()
                    .divide(swmAttendanceSummary.getScheduledHours(), 4, RoundingMode.HALF_UP);
            swmAttendanceSummary.setAttendanceAchievementRate(attendanceAchievementRate);
        } else {
            swmAttendanceSummary.setAttendanceAchievementRate(BigDecimal.ZERO);
        }

        super.save(swmAttendanceSummary);
    }

    /**
     * 更新状态
     * 
     * @param swmAttendanceSummary
     */
    @Override
    @Transactional(readOnly = false)
    public void updateStatus(SwmAttendanceSummary swmAttendanceSummary) {
        super.updateStatus(swmAttendanceSummary);
    }

    /**
     * 删除数据
     * 
     * @param swmAttendanceSummary
     */
    @Override
    @Transactional(readOnly = false)
    public void delete(SwmAttendanceSummary swmAttendanceSummary) {
        super.delete(swmAttendanceSummary);
    }

    /**
     * 根据月份查询考勤统计记录
     * 
     * @param month 统计月份 格式(YYYY-MM)
     * @return 考勤统计记录列表
     */
    public List<SwmAttendanceSummary> findByMonth(String month) {
        return dao.findByMonth(month);
    }

    /**
     * 根据员工姓名和月份查询考勤统计记录
     * 
     * @param employeeName 员工姓名
     * @param month        统计月份 格式(YYYY-MM)
     * @return 考勤统计记录
     */
    public SwmAttendanceSummary findByEmployeeAndMonth(String employeeName, String month) {
        return dao.findByEmployeeAndMonth(employeeName, month);
    }

    /**
     * 根据部门和月份查询考勤统计记录
     * 
     * @param department 部门名称
     * @param month      统计月份 格式(YYYY-MM)
     * @return 考勤统计记录列表
     */
    public List<SwmAttendanceSummary> findByDepartmentAndMonth(String department, String month) {
        return dao.findByDepartmentAndMonth(department, month);
    }

    /**
     * 根据员工ID和月份查询考勤统计记录
     * 
     * @param employeeId 员工ID
     * @param month      统计月份 格式(YYYY-MM)
     * @return 考勤统计记录
     */
    public SwmAttendanceSummary findByEmployeeIdAndMonth(String employeeId, String month,String corpCode) {
        return dao.findByEmployeeIdAndMonth(employeeId, month, corpCode);
    }

    /**
     * 根据实体查询考勤统计记录
     * 
     * @param entity
     * @return
     */
    public SwmAttendanceSummary getByEntity(SwmAttendanceSummary entity) {
        return dao.getByEntity(entity);
    }

    /**
     * 根据身份证号和月份查询考勤统计记录
     *
     * @param identityCard 身份证号
     * @param month        统计月份 格式(YYYY-MM)
     * @return 考勤统计记录列表
     * @author Shawn
     * @date 2025-08-21
     */
    public List<SwmAttendanceSummary> findByIdentityCardAndMonth(String identityCard, String month) {
        SwmAttendanceSummary query = new SwmAttendanceSummary();
        query.setIdentityCard(identityCard);
        query.setMonth(month);
        return this.findList(query);
    }

    /**
     * 根据身份证号统计月度考勤数据
     * 
     * @param identityCard 身份证号
     * @param month        统计月份(格式:yyyy-MM)，为空时默认当前月
     * @return 考勤统计对象
     * @author Shawn
     * @date 2025-08-21
     */
    public SwmAttendanceSummary calculateAttendanceSummaryByIdentityCard(String identityCard, String month) {
        // 如果没有传入月份，使用当前月份
        if (month == null || month.trim().isEmpty()) {
            YearMonth currentMonth = YearMonth.now();
            month = currentMonth.format(DateTimeFormatter.ofPattern("yyyy-MM"));
        }

        // 创建返回对象
        SwmAttendanceSummary summary = new SwmAttendanceSummary();
        summary.setIdentityCard(identityCard);
        summary.setMonth(month);

        // 一次查询该月所有日考勤记录，避免重复查询
        List<SwmDailyAttendance> dailyAttendanceList = swmDailyAttendanceService
                .findByIdentityCardAndMonth(identityCard, month);

        // 计算应出勤天数（基于排班配置）
        BigDecimal scheduledDays = calculateScheduledDays(identityCard, month);
        summary.setScheduledDays(scheduledDays);

        // 基于查询结果计算各项指标，实际出勤天数
        BigDecimal actualDays = calculateActualDaysFromList(dailyAttendanceList);
        summary.setActualDays(actualDays);

        // 怠工时长(小时)
        BigDecimal idleHours = calculateIdleHoursFromList(dailyAttendanceList);
        summary.setIdleHours(idleHours);

        // 应考勤时长(小时)  去排班时间管理查出那些天是休息日，然后日考勤去掉这几天
        BigDecimal scheduledHours = calculateScheduledHoursFromList(dailyAttendanceList);
        summary.setScheduledHours(scheduledHours);

        // 实际工作时长(小时)
        BigDecimal actualHours = calculateActualHoursFromList(dailyAttendanceList);
        summary.setActualHours(actualHours);

        // 计算出勤率
        BigDecimal attendanceRate = calculateAttendanceRate(actualDays, scheduledDays);
        summary.setAttendanceRate(attendanceRate);

        // 计算考勤达成率  ：实际正常考勤天数/应考勤人员天数
        //先计算实际正常考勤天数
        BigDecimal actualNormalAttendanceDays = calculateActualNormalAttendanceDaysFromList(dailyAttendanceList);
        BigDecimal attendanceAchievementRate = calculateAttendanceAchievementRate(actualNormalAttendanceDays, scheduledDays);
        summary.setAttendanceAchievementRate(attendanceAchievementRate);

        // 计算实际考勤时长
        BigDecimal actualAttendanceHours = calculateActualAttendanceHoursFromList(dailyAttendanceList);

        // 计算工效，实际考勤时长/应该考勤时长
        BigDecimal efficiency = calculateEfficiency(actualAttendanceHours, scheduledHours);
        summary.setEfficiency(efficiency);

        // 计算实际考勤天数
        BigDecimal actualAttendanceDays = calculateActualAttendanceDaysFromList(dailyAttendanceList);
        summary.setActualAttendanceDays(actualAttendanceDays);

        // 计算本月考勤率
        BigDecimal monthlyAttendanceRate = calculateMonthlyAttendanceRate(actualAttendanceDays, scheduledDays);
        summary.setMonthlyAttendanceRate(monthlyAttendanceRate);

        // TODO: 后续添加其他指标的计算

        return summary;
    }

    /**
     * 计算应出勤天数
     * 应出勤天数 = 查询月的总天数 - 排班里面的休息日
     * 
     * @param identityCard 身份证号
     * @param month        月份(格式:yyyy-MM)
     * @return 应出勤天数
     * @author Shawn
     * @date 2025-08-21
     */
    private BigDecimal calculateScheduledDays(String identityCard, String month) {
        try {
            // 1. 获取该月份的总天数
            YearMonth yearMonth = YearMonth.parse(month, DateTimeFormatter.ofPattern("yyyy-MM"));
            int totalDaysInMonth = yearMonth.lengthOfMonth();

            // 2. 根据身份证号查询该月的排班信息
            List<SwmPersonSchedule> personScheduleList = swmPersonScheduleService.findByIdCardAndMonth(identityCard,
                    null);

            if (personScheduleList == null || personScheduleList.isEmpty()) {
                // 没有排班信息，应出勤天数为0
                return BigDecimal.ZERO;
            }

            // 取第一条排班记录（一般一个月一个人只有一个班次）
            SwmPersonSchedule personSchedule = personScheduleList.get(0);
            String classes = personSchedule.getClasses();

            if (classes == null || classes.trim().isEmpty()) {
                // 班次为空，应出勤天数为0
                return BigDecimal.ZERO;
            }

            // 3. 根据班次查询休息日配置
            SwmScheduleTime scheduleTimeQuery = new SwmScheduleTime();
            scheduleTimeQuery.setShiftType(classes);
            List<SwmScheduleTime> scheduleTimeList = swmScheduleTimeService.findList(scheduleTimeQuery);

            if (scheduleTimeList == null || scheduleTimeList.isEmpty()) {
                // 没有找到班次时间配置，应出勤天数为0
                return BigDecimal.ZERO;
            }

            SwmScheduleTime scheduleTime = scheduleTimeList.get(0);
            String restDays = scheduleTime.getRestDays();

            if (restDays == null || restDays.trim().isEmpty()) {
                // 没有休息日配置，应出勤天数等于总天数
                return new BigDecimal(totalDaysInMonth);
            }

            // 4. 解析休息日配置并计算休息日天数
            int restDayCount = calculateRestDayCount(yearMonth, restDays);

            // 5. 计算应出勤天数 = 总天数 - 休息日天数
            int scheduledDayCount = totalDaysInMonth - restDayCount;
            return new BigDecimal(Math.max(0, scheduledDayCount));

        } catch (Exception e) {
            // 发生异常时返回0
            return BigDecimal.ZERO;
        }
    }

    /**
     * 计算指定月份中休息日的天数
     * 
     * @param yearMonth 年月
     * @param restDays  休息日配置(如"6,7"表示周六日)
     * @return 休息日天数
     * @author Shawn
     * @date 2025-08-21
     */
    private int calculateRestDayCount(YearMonth yearMonth, String restDays) {
        // 解析休息日配置
        String[] restDayArray = restDays.split(",");
        int[] restDayNumbers = Arrays.stream(restDayArray)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .mapToInt(Integer::parseInt)
                .toArray();

        int restDayCount = 0;
        int totalDays = yearMonth.lengthOfMonth();

        // 遍历该月的每一天，检查是否为休息日
        for (int day = 1; day <= totalDays; day++) {
            LocalDate date = yearMonth.atDay(day);
            int dayOfWeek = date.getDayOfWeek().getValue(); // 1=周一, 7=周日

            // 检查当天是否为休息日
            for (int restDay : restDayNumbers) {
                if (dayOfWeek == restDay) {
                    restDayCount++;
                    break;
                }
            }
        }

        return restDayCount;
    }

    /**
     * 基于日考勤记录列表计算实际出勤天数
     * 只要有上班打卡时间或下班打卡时间就算出勤一天
     * 
     * @param dailyAttendanceList 日考勤记录列表
     * @return 实际出勤天数
     * @author Shawn
     * @date 2025-08-21
     */
    private BigDecimal calculateActualDaysFromList(List<SwmDailyAttendance> dailyAttendanceList) {
        if (dailyAttendanceList == null || dailyAttendanceList.isEmpty()) {
            return BigDecimal.ZERO;
        }

        // 统计有打卡记录的天数
        long actualDayCount = dailyAttendanceList.stream()
                .filter(attendance -> attendance.getClockInDate() != null || attendance.getClockOutDate() != null)
                .count();

        return new BigDecimal(actualDayCount);
    }

    /**
     * 基于日考勤记录列表计算怠工时长
     * 累加所有日考勤记录的怠工时长
     * 
     * @param dailyAttendanceList 日考勤记录列表
     * @return 怠工时长(小时)
     * @author Shawn
     * @date 2025-08-21
     */
    private BigDecimal calculateIdleHoursFromList(List<SwmDailyAttendance> dailyAttendanceList) {
        if (dailyAttendanceList == null || dailyAttendanceList.isEmpty()) {
            return BigDecimal.ZERO;
        }

        // 累加所有日考勤记录的怠工时长
        return dailyAttendanceList.stream()
                .map(SwmDailyAttendance::getIdleHours)
                .filter(idleHours -> idleHours != null) // 过滤null值
                .reduce(BigDecimal.ZERO, BigDecimal::add); // 累加
    }

    /**
     * 基于日考勤记录列表计算应考勤时长
     * 累加所有日考勤记录的应考勤时长
     *
     * @param dailyAttendanceList 日考勤记录列表
     * @return 应考勤时长(小时)
     * @author Shawn
     * @date 2025-08-21
     */
    private BigDecimal calculateScheduledHoursFromList(List<SwmDailyAttendance> dailyAttendanceList) {
        if (dailyAttendanceList == null || dailyAttendanceList.isEmpty()) {
            return BigDecimal.ZERO;
        }
        BigDecimal scheduledHours = BigDecimal.ZERO;

        for (SwmDailyAttendance attendance : dailyAttendanceList) {
            if ("2".equals(attendance.getAttendanceNormal())){
                continue;
            }
            scheduledHours = scheduledHours.add(attendance.getScheduledHours());
        }
        return scheduledHours;

//        // 累加所有日考勤记录的应考勤时长
//        return dailyAttendanceList.stream()
//                .map(SwmDailyAttendance::getScheduledHours)
//                .filter(scheduledHours -> scheduledHours != null) // 过滤null值
//                .reduce(BigDecimal.ZERO, BigDecimal::add); // 累加
    }

    /**
     * 基于日考勤记录列表计算实际工作时长
     * 累加所有日考勤记录的实际工作时长
     *
     * @param dailyAttendanceList 日考勤记录列表
     * @return 实际工作时长(小时)
     * @author Shawn
     * @date 2025-08-21
     */
    private BigDecimal calculateActualHoursFromList(List<SwmDailyAttendance> dailyAttendanceList) {
        if (dailyAttendanceList == null || dailyAttendanceList.isEmpty()) {
            return BigDecimal.ZERO;
        }

        // 累加所有日考勤记录的实际工作时长
        return dailyAttendanceList.stream()
                .map(SwmDailyAttendance::getEffectiveWorkHours)
                .filter(effectiveWorkHours -> effectiveWorkHours != null) // 过滤null值
                .reduce(BigDecimal.ZERO, BigDecimal::add); // 累加
    }

    /**
     * 基于日考勤记录列表计算实际考勤时长
     * 累加所有日考勤记录的实际考勤时长
     *
     * @param dailyAttendanceList 日考勤记录列表
     * @return 实际考勤时长(小时)
     * @author Shawn
     * @date 2025-08-21
     */
    private BigDecimal calculateActualAttendanceHoursFromList(List<SwmDailyAttendance> dailyAttendanceList) {
        if (dailyAttendanceList == null || dailyAttendanceList.isEmpty()) {
            return BigDecimal.ZERO;
        }

        // 累加所有日考勤记录的实际考勤时长
        return dailyAttendanceList.stream()
                .map(SwmDailyAttendance::getActualHours)
                .filter(actualHours -> actualHours != null) // 过滤null值
                .reduce(BigDecimal.ZERO, BigDecimal::add); // 累加
    }

    /**
     * 基于日考勤记录列表计算实际考勤天数
     * 统计打卡状态为正常的天数（attendance_normal = "0"）
     *
     * @param dailyAttendanceList 日考勤记录列表
     * @return 实际考勤天数
     * @author Shawn
     * @date 2025-08-21
     */
    private BigDecimal calculateActualAttendanceDaysFromList(List<SwmDailyAttendance> dailyAttendanceList) {
        if (dailyAttendanceList == null || dailyAttendanceList.isEmpty()) {
            return BigDecimal.ZERO;
        }

        // 统计打卡状态为正常的天数
        long normalAttendanceDays = dailyAttendanceList.stream()
                .filter(attendance -> "0".equals(attendance.getAttendanceNormal()))
                .count();

        return new BigDecimal(normalAttendanceDays);
    }

    /**
     * 计算出勤率
     * 出勤率 = 实际出勤天数 ÷ 应出勤天数（返回小数形式，如0.8696表示86.96%）
     *
     * @param actualDays    实际出勤天数
     * @param scheduledDays 应出勤天数
     * @return 出勤率(小数形式，保留4位小数)
     * @author Shawn
     * @date 2025-08-21
     */
    private BigDecimal calculateAttendanceRate(BigDecimal actualDays, BigDecimal scheduledDays) {
        try {
            // 应出勤天数为0或null时，出勤率为0
            if (scheduledDays == null || scheduledDays.compareTo(BigDecimal.ZERO) == 0) {
                return BigDecimal.ZERO;
            }

            // 实际出勤天数为null时，按0处理
            if (actualDays == null) {
                return BigDecimal.ZERO;
            }

            // 计算出勤率：实际出勤天数 ÷ 应出勤天数（返回小数形式）
            return actualDays.divide(scheduledDays, 4, RoundingMode.HALF_UP);

        } catch (Exception e) {
            // 发生异常时返回0
            return BigDecimal.ZERO;
        }
    }

    /**
     * 计算考勤达成率
     * 考勤达成率=实际正常考勤天数（正常上班且没有迟早早退）/应考勤人员天数
     *
     * @param actualNormalAttendanceDays    实际正常考勤天数
     * @param scheduledDays 应考勤人员天数
     * @return 考勤达成率(小数形式，保留4位小数)
     * @author Shawn
     * @date 2025-08-21
     */
    private BigDecimal calculateAttendanceAchievementRate(BigDecimal actualNormalAttendanceDays, BigDecimal scheduledDays) {
        try {
            // 应考勤时长为0或null时，考勤达成率为0
            if (scheduledDays == null || scheduledDays.compareTo(BigDecimal.ZERO) == 0) {
                return BigDecimal.ZERO;
            }

            // 实际工作时长为null时，按0处理
            if (actualNormalAttendanceDays == null) {
                return BigDecimal.ZERO;
            }

            // 计算考勤达成率：实际工作时长 ÷ 应考勤时长（返回小数形式）
            return actualNormalAttendanceDays.divide(scheduledDays, 4, RoundingMode.HALF_UP);

        } catch (Exception e) {
            // 发生异常时返回0
            return BigDecimal.ZERO;
        }
    }


    /**
     * 正常考勤天数（无迟到、无早退即为正常）
     * @author Shawn
     * @date 2025-08-21
     */
    private BigDecimal calculateActualNormalAttendanceDaysFromList(List<SwmDailyAttendance> dailyAttendanceList) {

        if (dailyAttendanceList == null || dailyAttendanceList.isEmpty()) {
            return BigDecimal.ZERO;
        }

        BigDecimal normalAttendanceDays = BigDecimal.ZERO;

        for (SwmDailyAttendance attendance : dailyAttendanceList) {

            // ===== 1. 基础校验 =====
            if (attendance.getClockInTime() == null || attendance.getClockOutTime() == null || com.jeesite.common.lang.StringUtils.isBlank(attendance.getWorkTimeRange())) {
                continue;
            }

            try {
                // ===== 2. 解析工作时间段 =====
                String[] timeRange = attendance.getWorkTimeRange().split("-");
                if (timeRange.length != 2) {
                    continue;
                }

                Date clockInTime = attendance.getClockInTime();
                Date clockOutTime = attendance.getClockOutTime();

                // 标准上班时间
                Date standardStart = buildStandardTime(clockInTime, timeRange[0]);
                // 标准下班时间
                Date standardEnd = buildStandardTime(clockOutTime, timeRange[1]);

                boolean late = clockInTime.after(standardStart);
                boolean earlyLeave = clockOutTime.before(standardEnd);

                // ===== 3. 设置考勤状态 =====
                if (!late && !earlyLeave) {
                    normalAttendanceDays = normalAttendanceDays.add(BigDecimal.ONE);
                }
            } catch (Exception e) {
                logger.error("计算考勤状态失败，attendanceId={}", attendance.getId(), e);
            }
        }
        return normalAttendanceDays;
    }


    /**
     * 构建标准时间
     * @param baseDate
     * @param timeStr
     * @return
     */
    private Date buildStandardTime(Date baseDate, String timeStr) {
        String[] parts = timeStr.trim().split(":");

        Calendar cal = Calendar.getInstance();
        cal.setTime(baseDate);
        cal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(parts[0]));
        cal.set(Calendar.MINUTE, Integer.parseInt(parts[1]));
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        return cal.getTime();
    }



    /**
     * 计算工效
     * 工效 = 实际考勤时长 ÷ 应考勤时长（返回小数形式）
     *
     * @param actualAttendanceHours 实际考勤时长
     * @param scheduledHours        应考勤时长
     * @return 工效(小数形式，保留4位小数)
     * @author Shawn
     * @date 2025-08-21
     */
    private BigDecimal calculateEfficiency(BigDecimal actualAttendanceHours, BigDecimal scheduledHours) {
        try {
            // 应考勤时长为0或null时，工效为0
            if (scheduledHours == null || scheduledHours.compareTo(BigDecimal.ZERO) == 0) {
                return BigDecimal.ZERO;
            }

            // 实际考勤时长为null时，按0处理
            if (actualAttendanceHours == null) {
                return BigDecimal.ZERO;
            }

            // 计算工效：实际考勤时长 ÷ 应考勤时长（返回小数形式）
            return actualAttendanceHours.divide(scheduledHours, 4, RoundingMode.HALF_UP);

        } catch (Exception e) {
            // 发生异常时返回0
            return BigDecimal.ZERO;
        }
    }

    /**
     * 计算本月考勤率
     * 本月考勤率 = 实际考勤天数 ÷ 应考勤天数（返回小数形式）
     *
     * @param actualAttendanceDays 实际考勤天数
     * @param scheduledDays        应考勤天数
     * @return 本月考勤率(小数形式，保留4位小数)
     * @author Shawn
     * @date 2025-08-21
     */
    private BigDecimal calculateMonthlyAttendanceRate(BigDecimal actualAttendanceDays, BigDecimal scheduledDays) {
        try {
            // 应考勤天数为0或null时，本月考勤率为0
            if (scheduledDays == null || scheduledDays.compareTo(BigDecimal.ZERO) == 0) {
                return BigDecimal.ZERO;
            }

            // 实际考勤天数为null时，按0处理
            if (actualAttendanceDays == null) {
                return BigDecimal.ZERO;
            }

            // 计算本月考勤率：实际考勤天数 ÷ 应考勤天数（返回小数形式）
            return actualAttendanceDays.divide(scheduledDays, 4, RoundingMode.HALF_UP);

        } catch (Exception e) {
            // 发生异常时返回0
            return BigDecimal.ZERO;
        }
    }

}
