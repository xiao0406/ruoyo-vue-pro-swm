package com.jeesite.modules.swm.service;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import com.jeesite.common.entity.Page;
import com.jeesite.common.service.CrudService;
import com.jeesite.modules.job.task.AttendanceTask;
import com.jeesite.modules.swm.dao.SwmDailyAttendanceDao;
import com.jeesite.modules.swm.entity.*;

import com.jeesite.modules.swm.entity.dto.SwmDashboardDto;
import com.jeesite.modules.swm.web.SwmDashboardNewController;
import com.jeesite.modules.entity.AiDto;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.jeesite.common.lang.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 日考勤统计表Service
 *
 * @author zwf
 * @version 2025-05-20
 */
@Service
@Transactional(readOnly = true)
public class SwmDailyAttendanceService extends CrudService<SwmDailyAttendanceDao, SwmDailyAttendance> {

    @Autowired
    private AreaFenceDataService areaFenceDataService;

    @Autowired
    private SwmPersonCacheService swmPersonCacheService;

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

    /**
     * 获取单条数据
     *
     * @param swmDailyAttendance
     * @return
     */
    @Override
    public SwmDailyAttendance get(SwmDailyAttendance swmDailyAttendance) {
        return super.get(swmDailyAttendance);
    }

    /**
     * 查询分页数据
     *
     * @param swmDailyAttendance
     * @return
     */
    @Override
    public Page<SwmDailyAttendance> findPage(SwmDailyAttendance swmDailyAttendance) {
        // 处理日期，去除时间部分
        if (swmDailyAttendance.getAttendanceDate() != null) {
            swmDailyAttendance.setAttendanceDate(truncateTime(swmDailyAttendance.getAttendanceDate()));
        }
        if (swmDailyAttendance.getBeginAttendanceDate() != null) {
            swmDailyAttendance.setBeginAttendanceDate(truncateTime(swmDailyAttendance.getBeginAttendanceDate()));
        }
        if (swmDailyAttendance.getEndAttendanceDate() != null) {
            swmDailyAttendance.setEndAttendanceDate(truncateTime(swmDailyAttendance.getEndAttendanceDate()));
        }
        return super.findPage(swmDailyAttendance);
    }

    /**
     * 查询列表数据
     *
     * @param swmDailyAttendance
     * @return
     */
    @Override
    public List<SwmDailyAttendance> findList(SwmDailyAttendance swmDailyAttendance) {
        // 处理日期，去除时间部分
        if (swmDailyAttendance.getAttendanceDate() != null) {
            swmDailyAttendance.setAttendanceDate(truncateTime(swmDailyAttendance.getAttendanceDate()));
        }
        if (swmDailyAttendance.getBeginAttendanceDate() != null) {
            swmDailyAttendance.setBeginAttendanceDate(truncateTime(swmDailyAttendance.getBeginAttendanceDate()));
        }
        if (swmDailyAttendance.getEndAttendanceDate() != null) {
            swmDailyAttendance.setEndAttendanceDate(truncateTime(swmDailyAttendance.getEndAttendanceDate()));
        }
        return super.findList(swmDailyAttendance);
    }

    /**
     * 保存数据（插入或更新）
     * 保存时自动计算：实际考勤时长、怠工时长、今日达成率
     *
     * @param swmDailyAttendance
     */
    @Override
    @Transactional(readOnly = false)
    public void save(SwmDailyAttendance swmDailyAttendance) {
        // 如果是新记录，进行数据初始化
        if (swmDailyAttendance.getIsNewRecord()) {
            // 设置默认值，避免空指针异常
            if (swmDailyAttendance.getScheduledHours() == null) {
                swmDailyAttendance.setScheduledHours(BigDecimal.ZERO);
            }
            if (swmDailyAttendance.getActualHours() == null) {
                swmDailyAttendance.setActualHours(BigDecimal.ZERO);
            }
            if (swmDailyAttendance.getIdleHours() == null) {
                swmDailyAttendance.setIdleHours(BigDecimal.ZERO);
            }
        }

        // 计算实际考勤时长（如果上下班打卡时间都存在）
        if (swmDailyAttendance.getClockInTime() != null && swmDailyAttendance.getClockOutTime() != null) {
            // 计算上下班打卡时间的时间差（毫秒）
            long timeDiffMs = swmDailyAttendance.getClockOutTime().getTime()
                    - swmDailyAttendance.getClockInTime().getTime();

            // 转换为小时（四舍五入到2位小数）
            BigDecimal actualHours = new BigDecimal(timeDiffMs / (1000.0 * 60 * 60))
                    .setScale(2, RoundingMode.HALF_UP);

            // 设置实际考勤时长
            swmDailyAttendance.setActualHours(actualHours);

            // 计算怠工时长 = 应考勤时长 - 实际考勤时长（如果为负，则怠工时长为0）
            if (swmDailyAttendance.getScheduledHours() != null) {
                BigDecimal idleHours = swmDailyAttendance.getScheduledHours().subtract(actualHours);
                if (idleHours.compareTo(BigDecimal.ZERO) > 0) {
                    swmDailyAttendance.setIdleHours(idleHours);
                } else {
                    swmDailyAttendance.setIdleHours(BigDecimal.ZERO);
                }
            }
        }

        // 计算今日达成率 = 实际考勤时长 / 应考勤时长
        if (swmDailyAttendance.getScheduledHours() != null &&
                swmDailyAttendance.getActualHours() != null &&
                swmDailyAttendance.getScheduledHours().compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal dailyAchievementRate = swmDailyAttendance.getActualHours()
                    .divide(swmDailyAttendance.getScheduledHours(), 4, RoundingMode.HALF_UP);
            swmDailyAttendance.setDailyAchievementRate(dailyAchievementRate);
        } else {
            swmDailyAttendance.setDailyAchievementRate(BigDecimal.ZERO);
        }

        // 计算当日功效 = 1 - 怠工时长 / 实际工作时长
        if (swmDailyAttendance.getActualHours() != null &&
                swmDailyAttendance.getIdleHours() != null &&
                swmDailyAttendance.getActualHours().compareTo(BigDecimal.ZERO) > 0) {

            // 计算怠工时长占比
            BigDecimal idleRatio = swmDailyAttendance.getIdleHours()
                    .divide(swmDailyAttendance.getActualHours(), 4, RoundingMode.HALF_UP);

            // 功效 = 1 - 怠工占比
            BigDecimal dailyEfficiency = BigDecimal.ONE.subtract(idleRatio)
                    .setScale(4, RoundingMode.HALF_UP);

            // 确保功效在0-1之间
            if (dailyEfficiency.compareTo(BigDecimal.ZERO) < 0) {
                dailyEfficiency = BigDecimal.ZERO;
            } else if (dailyEfficiency.compareTo(BigDecimal.ONE) > 0) {
                dailyEfficiency = BigDecimal.ONE;
            }

            swmDailyAttendance.setDailyEfficiency(dailyEfficiency);
        } else {
            // 默认功效为0
            swmDailyAttendance.setDailyEfficiency(BigDecimal.ZERO);
        }

        // 检查是否为更新操作且打卡时间不为空
        if (!swmDailyAttendance.getIsNewRecord() &&
                (swmDailyAttendance.getClockInTime() != null || swmDailyAttendance.getClockOutTime() != null)) {
            // 使用自定义更新方法确保打卡时间字段被更新
            logger.info("使用自定义更新方法，确保打卡时间字段被更新");
            dao.updateWithClockTime(swmDailyAttendance);
        } else {
            // 否则使用标准保存方法
            super.save(swmDailyAttendance);
        }
    }

    /**
     * 更新状态
     *
     * @param swmDailyAttendance
     */
    @Override
    @Transactional(readOnly = false)
    public void updateStatus(SwmDailyAttendance swmDailyAttendance) {
        super.updateStatus(swmDailyAttendance);
    }

    /**
     * 删除数据
     *
     * @param swmDailyAttendance
     */
    @Override
    @Transactional(readOnly = false)
    public void delete(SwmDailyAttendance swmDailyAttendance) {
        super.delete(swmDailyAttendance);
    }

    /**
     * 处理日期，去除时间部分，只保留日期部分
     *
     * @param date 需要处理的日期
     * @return 只包含日期部分的Date对象
     */
    private Date truncateTime(Date date) {
        if (date == null) {
            return null;
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

    /**
     * 根据员工姓名和日期查询考勤记录
     *
     * @param employeeName   员工姓名
     * @param attendanceDate 考勤日期
     * @return 考勤记录
     */
    public SwmDailyAttendance findByEmployeeAndDate(String employeeName, Date attendanceDate) {
        // 处理日期，去除时间部分
        attendanceDate = truncateTime(attendanceDate);
        return dao.findByEmployeeAndDate(employeeName, attendanceDate);
    }

    /**
     * 根据员工姓名和日期范围查询考勤记录
     *
     * @param employeeName 员工姓名
     * @param beginDate    开始日期
     * @param endDate      结束日期
     * @return 考勤记录列表
     */
    public List<SwmDailyAttendance> findByEmployeeAndDateRange(String employeeName, Date beginDate, Date endDate) {
        // 处理日期，去除时间部分
        beginDate = truncateTime(beginDate);
        endDate = truncateTime(endDate);
        return dao.findByEmployeeAndDateRange(employeeName, beginDate, endDate);
    }

    /**
     * 根据日期查询所有考勤记录
     *
     * @param attendanceDate 考勤日期
     * @return 考勤记录列表
     */
    public List<SwmDailyAttendance> findByDate(Date attendanceDate) {
        // 处理日期，去除时间部分
        attendanceDate = truncateTime(attendanceDate);
        return dao.findByDate(attendanceDate);
    }

    /**
     * 根据员工姓名和月份查询考勤记录
     *
     * @param employeeName 员工姓名
     * @param year         年份
     * @param month        月份 (1-12)
     * @return 考勤记录列表
     */
    public List<SwmDailyAttendance> findByEmployeeAndMonth(String employeeName, int year, int month) {
        return dao.findByEmployeeAndMonth(employeeName, year, month);
    }

    /**
     * 计算员工某月的统计数据
     *
     * @param employeeName 员工姓名
     * @param year         年份
     * @param month        月份 (1-12)
     * @return 月统计数据
     */
    public SwmAttendanceSummary calculateMonthlyStats(String employeeName, int year, int month) {
        // 获取该员工该月所有日考勤记录
        List<SwmDailyAttendance> dailyRecords = findByEmployeeAndMonth(employeeName, year, month);

        SwmAttendanceSummary summary = new SwmAttendanceSummary();
        summary.setEmployeeName(employeeName);
        summary.setMonth(String.format("%04d-%02d", year, month));

        // 初始化统计数据
        BigDecimal totalScheduledDays = BigDecimal.ZERO;
        BigDecimal totalActualDays = BigDecimal.ZERO;
        BigDecimal totalScheduledHours = BigDecimal.ZERO;
        BigDecimal totalEffectiveWorkHours = BigDecimal.ZERO;
        BigDecimal totalIdleHours = BigDecimal.ZERO;

        for (SwmDailyAttendance record : dailyRecords) {
            // 累计应出勤天数（每条记录算1天）
            totalScheduledDays = totalScheduledDays.add(BigDecimal.ONE);

            // 累计实际出勤天数（根据实际考勤时长判断，大于0即为出勤）
            if (record.getActualHours() != null && record.getActualHours().compareTo(BigDecimal.ZERO) > 0) {
                totalActualDays = totalActualDays.add(BigDecimal.ONE);
            }

            // 累计应考勤时间
            if (record.getScheduledHours() != null) {
                totalScheduledHours = totalScheduledHours.add(record.getScheduledHours());
            }

            // 累计实际工作时间
            if (record.getEffectiveWorkHours() != null) {
                totalEffectiveWorkHours = totalEffectiveWorkHours.add(record.getEffectiveWorkHours());
            }

            // 累计怠工时长
            if (record.getIdleHours() != null) {
                totalIdleHours = totalIdleHours.add(record.getIdleHours());
            }
        }

        // 设置统计结果
        summary.setScheduledDays(totalScheduledDays);
        summary.setActualDays(totalActualDays);
        summary.setScheduledHours(totalScheduledHours);
        summary.setActualHours(totalEffectiveWorkHours);
        summary.setIdleHours(totalIdleHours);

        // 计算出勤率 = 实际出勤天数 / 应出勤天数
        if (totalScheduledDays.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal attendanceRate = totalActualDays.divide(totalScheduledDays, 4, RoundingMode.HALF_UP);
            summary.setAttendanceRate(attendanceRate);
        } else {
            summary.setAttendanceRate(BigDecimal.ZERO);
        }

        // 计算考勤达成率 = 实际工作时间 / 应考勤时间
        if (totalScheduledHours.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal attendanceAchievementRate = totalEffectiveWorkHours.divide(totalScheduledHours, 4,
                    RoundingMode.HALF_UP);
            summary.setAttendanceAchievementRate(attendanceAchievementRate);
        } else {
            summary.setAttendanceAchievementRate(BigDecimal.ZERO);
        }

        return summary;
    }

    /**
     * 根据员工ID和日期查询考勤记录
     *
     * @param employeeId     员工ID
     * @param attendanceDate 考勤日期
     * @return 考勤记录
     */
    public SwmDailyAttendance findByEmployeeIdAndDate(String employeeId, Date attendanceDate) {
        // 处理日期，去除时间部分
        attendanceDate = truncateTime(attendanceDate);
        return dao.findByEmployeeIdAndDate(employeeId, attendanceDate);
    }

    /**
     * 根据员工ID和日期范围查询考勤记录
     *
     * @param employeeId 员工ID
     * @param beginDate  开始日期
     * @param endDate    结束日期
     * @return 考勤记录列表
     */
    public List<SwmDailyAttendance> findByEmployeeIdAndDateRange(String employeeId, Date beginDate, Date endDate) {
        // 处理日期，去除时间部分
        beginDate = truncateTime(beginDate);
        endDate = truncateTime(endDate);
        return dao.findByEmployeeIdAndDateRange(employeeId, beginDate, endDate);
    }

    /**
     * 根据身份证和日期查询考勤记录
     * 
     * @param identityCard   身份证号
     * @param attendanceDate 考勤日期
     * @return 考勤记录
     * @author Shawn
     * @date 2025-08-12
     */
    public SwmDailyAttendance findByIdentityCardAndDate(String identityCard, Date attendanceDate) {
        if (StringUtils.isBlank(identityCard)) {
            return null;
        }
        // 处理日期，去除时间部分
        attendanceDate = truncateTime(attendanceDate);
        return dao.findByIdentityCardAndDate(identityCard, attendanceDate);
    }

    /**
     * 根据身份证号和月份查询考勤记录
     * 
     * @param identityCard 身份证号
     * @param month        月份(格式: yyyy-MM)
     * @return 考勤记录列表
     * @author Shawn
     * @date 2025-08-21
     */
    public List<SwmDailyAttendance> findByIdentityCardAndMonth(String identityCard, String month) {
        if (StringUtils.isBlank(identityCard) || StringUtils.isBlank(month)) {
            return new ArrayList<>();
        }
        return dao.findByIdentityCardAndMonth(identityCard, month);
    }

    /**
     * 根据员工ID和月份查询考勤记录
     *
     * @param employeeId 员工ID
     * @param year       年份
     * @param month      月份 (1-12)
     * @return 考勤记录列表
     */
    public List<SwmDailyAttendance> findByEmployeeIdAndMonth(String employeeId, int year, int month) {
        return dao.findByEmployeeIdAndMonth(employeeId, year, month);
    }

    /**
     * 计算员工某月的统计数据
     *
     * @param employeeId   员工ID
     * @param employeeName 员工姓名
     * @param year         年份
     * @param month        月份 (1-12)
     * @return 月统计数据
     */
    public SwmAttendanceSummary calculateMonthlyStatsById(String employeeId, String employeeName, int year, int month) {
        // 获取该员工该月所有日考勤记录
        List<SwmDailyAttendance> dailyRecords = findByEmployeeIdAndMonth(employeeId, year, month);

        SwmAttendanceSummary summary = new SwmAttendanceSummary();
        summary.setEmployeeId(employeeId);
        summary.setEmployeeName(employeeName);
        summary.setMonth(String.format("%04d-%02d", year, month));

        // 初始化统计数据
        BigDecimal totalScheduledDays = BigDecimal.ZERO;
        BigDecimal totalActualDays = BigDecimal.ZERO;
        BigDecimal totalScheduledHours = BigDecimal.ZERO;
        BigDecimal totalEffectiveWorkHours = BigDecimal.ZERO;
        BigDecimal totalIdleHours = BigDecimal.ZERO;

        for (SwmDailyAttendance record : dailyRecords) {
            // 累计应出勤天数（每条记录算1天）
            totalScheduledDays = totalScheduledDays.add(BigDecimal.ONE);

            // 累计实际出勤天数（根据实际考勤时长判断，大于0即为出勤）
            if (record.getActualHours() != null && record.getActualHours().compareTo(BigDecimal.ZERO) > 0) {
                totalActualDays = totalActualDays.add(BigDecimal.ONE);
            }

            // 累计应考勤时间
            if (record.getScheduledHours() != null) {
                totalScheduledHours = totalScheduledHours.add(record.getScheduledHours());
            }

            // 累计实际工作时间
            if (record.getEffectiveWorkHours() != null) {
                totalEffectiveWorkHours = totalEffectiveWorkHours.add(record.getEffectiveWorkHours());
            }

            // 累计怠工时长
            if (record.getIdleHours() != null) {
                totalIdleHours = totalIdleHours.add(record.getIdleHours());
            }
        }

        // 设置统计结果
        summary.setScheduledDays(totalScheduledDays);
        summary.setActualDays(totalActualDays);
        summary.setScheduledHours(totalScheduledHours);
        summary.setActualHours(totalEffectiveWorkHours);
        summary.setIdleHours(totalIdleHours);

        // 计算出勤率 = 实际出勤天数 / 应出勤天数
        if (totalScheduledDays.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal attendanceRate = totalActualDays.divide(totalScheduledDays, 4, RoundingMode.HALF_UP);
            summary.setAttendanceRate(attendanceRate);
        } else {
            summary.setAttendanceRate(BigDecimal.ZERO);
        }

        // 计算考勤达成率 = 实际工作时间 / 应考勤时间
        if (totalScheduledHours.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal attendanceAchievementRate = totalEffectiveWorkHours.divide(totalScheduledHours, 4,
                    RoundingMode.HALF_UP);
            summary.setAttendanceAchievementRate(attendanceAchievementRate);
        } else {
            summary.setAttendanceAchievementRate(BigDecimal.ZERO);
        }

        return summary;
    }

    /**
     * 获取员工月度考勤图表数据
     * 根据员工ID和月份查询日考勤数据，返回适合图表展示的格式
     *
     * @param employeeId 员工ID
     * @param year       年份
     * @param month      月份 (1-12)
     * @return 包含图表数据的Map，包括xAxis(日期)、scheduledHours(应考勤时长)、idleHours(怠工时长)、efficiency(功效)
     */
    public Map<String, Object> getMonthlyChartData(String employeeId, int year, int month) {
        // 获取该员工该月的所有考勤记录
        List<SwmDailyAttendance> records = findByEmployeeIdAndMonth(employeeId, year, month);

        // 获取该月的天数
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month - 1, 1); // 月份从0开始，所以减1
        int daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);

        // 准备返回数据
        List<String> xAxis = new ArrayList<>(); // 日期 (1-31)
        List<BigDecimal> scheduledHours = new ArrayList<>(); // 应考勤时长
        List<BigDecimal> idleHours = new ArrayList<>(); // 怠工时长
        List<BigDecimal> efficiency = new ArrayList<>(); // 功效

        // 按照日期将记录映射到对应天数
        Map<Integer, SwmDailyAttendance> recordsByDay = new HashMap<>();
        for (SwmDailyAttendance record : records) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(record.getAttendanceDate());
            int day = cal.get(Calendar.DAY_OF_MONTH);
            recordsByDay.put(day, record);
        }

        // 填充数据
        for (int day = 1; day <= daysInMonth; day++) {
            // 添加X轴日期
            xAxis.add(String.valueOf(day));

            // 如果该天有记录，则添加对应的数据；否则添加0
            SwmDailyAttendance record = recordsByDay.get(day);
            if (record != null) {
                scheduledHours.add(record.getScheduledHours() != null ? record.getScheduledHours() : BigDecimal.ZERO);
                idleHours.add(record.getIdleHours() != null ? record.getIdleHours() : BigDecimal.ZERO);
                efficiency.add(record.getDailyEfficiency() != null ? record.getDailyEfficiency() : BigDecimal.ZERO);
            } else {
                scheduledHours.add(BigDecimal.ZERO);
                idleHours.add(BigDecimal.ZERO);
                efficiency.add(BigDecimal.ZERO);
            }
        }

        // 构建结果
        Map<String, Object> data = new HashMap<>();
        data.put("xAxis", xAxis);
        data.put("scheduledHours", scheduledHours);
        data.put("idleHours", idleHours);
        data.put("efficiency", efficiency);

        return data;
    }

    /**
     * 获取员工月度考勤时长数据
     * 根据员工ID和月份查询日考勤数据，返回应考勤时长和实际考勤时长
     *
     * @param employeeId 员工ID
     * @param year       年份
     * @param month      月份 (1-12)
     * @return 包含图表数据的Map，包括xAxis(日期)、scheduledHours(应考勤时长)、actualHours(实际考勤时长)
     */
    public Map<String, Object> getMonthlyAttendanceData(String employeeId, int year, int month) {
        // 获取该员工该月的所有考勤记录
        List<SwmDailyAttendance> records = findByEmployeeIdAndMonth(employeeId, year, month);

        // 获取该月的天数
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month - 1, 1); // 月份从0开始，所以减1
        int daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);

        // 准备返回数据
        List<String> xAxis = new ArrayList<>(); // 日期 (1-31)
        List<BigDecimal> scheduledHours = new ArrayList<>(); // 应考勤时长
        List<BigDecimal> actualHours = new ArrayList<>(); // 实际考勤时长

        // 按照日期将记录映射到对应天数
        Map<Integer, SwmDailyAttendance> recordsByDay = new HashMap<>();
        for (SwmDailyAttendance record : records) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(record.getAttendanceDate());
            int day = cal.get(Calendar.DAY_OF_MONTH);
            recordsByDay.put(day, record);
        }

        // 填充数据
        for (int day = 1; day <= daysInMonth; day++) {
            // 添加X轴日期
            xAxis.add(String.valueOf(day));

            // 如果该天有记录，则添加对应的数据；否则添加0
            SwmDailyAttendance record = recordsByDay.get(day);
            if (record != null) {
                scheduledHours.add(record.getScheduledHours() != null ? record.getScheduledHours() : BigDecimal.ZERO);
                actualHours.add(record.getActualHours() != null ? record.getActualHours() : BigDecimal.ZERO);
            } else {
                scheduledHours.add(BigDecimal.ZERO);
                actualHours.add(BigDecimal.ZERO);
            }
        }

        // 构建结果
        Map<String, Object> data = new HashMap<>();
        data.put("xAxis", xAxis);
        data.put("scheduledHours", scheduledHours);
        data.put("actualHours", actualHours);
        data.put("records", records);
        return data;
    }

    /**
     * 查询指定月份的日考勤记录
     *
     * @param employeeIds 员工ID(可选)
     * @param month       月份(格式: yyyy-MM)
     * @return 日考勤记录列表
     */
    public List<SwmDailyAttendance> findByMonth(List<String> employeeIds, String month) {
        return dao.findByMonth(employeeIds, month);
    }

    /**
     * 根据月份查询所有员工的日考勤记录（用于月度数据批量加载）
     * 
     * @param month 月份(格式: yyyy-MM)
     * @return 日考勤记录列表
     * @author Shawn
     * @date 2025-08-21
     */
    public List<SwmDailyAttendance> findByMonth(String month) {
        return this.findByMonth(null, month);
    }

    /**
     * 根据身份证号计算怠工时长
     *
     * @param idCard        身份证号
     * @param date          日期
     * @param workTimeRange 工作时间范围
     * @return 怠工时长
     * @author Shawn
     * @date 2025/6/20
     */
    public double calculateIdleTimeByIdCard(String idCard, String date, String workTimeRange) {
        return areaFenceDataService.calculateIdleTimeByIdCard(idCard, date, workTimeRange);
    }

    /**
     * 根据身份证号计算怠工时长
     *
     * @param idCard 身份证号
     * @param date   日期
     * @return 怠工时长
     * @author Shawn
     * @date 2025-01-27
     */
    public double calculateIdleTimeByIdCard(String idCard, String date) {
        return areaFenceDataService.calculateIdleTimeByIdCard(idCard, date);
    }

    /**
     * 根据员工ID获取身份证号
     *
     * @param employeeId 员工ID
     * @return 身份证号
     * @author Shawn
     * @date 2025-01-27
     */
    public String getIdCardByEmployeeId(String employeeId) {
        try {
            if (StringUtils.isBlank(employeeId)) {
                return null;
            }

            // 从Redis缓存中查询人员信息
            Map<String, Object> personInfo = swmPersonCacheService.getActivePersonById(employeeId);

            if (personInfo != null) {
                return (String) personInfo.get("identityCard");
            }

            return null;
        } catch (Exception e) {
            logger.error("根据员工ID {} 从Redis查询身份证号失败", employeeId, e);
            return null;
        }
    }

    /**
     * 根据身份证号计算实际工作时长
     *
     * @param idCard        身份证号
     * @param date          日期
     * @param workTimeRange 工作时间范围
     * @return 实际工作时长
     * @author Shawn
     * @date 2025-01-27
     */
    public double calculateEffectiveWorkHoursByIdCard(String idCard, String date, String workTimeRange) {
        return areaFenceDataService.calculateEffectiveWorkHoursByIdCard(idCard, date, workTimeRange);
    }

    /**
     * 根据身份证号计算实际工作时长
     *
     * @param idCard 身份证号
     * @param date   日期
     * @return 实际工作时长
     * @author Shawn
     * @date 2025-01-27
     */
    public double calculateEffectiveWorkHoursByIdCard(String idCard, String date) {
        return areaFenceDataService.calculateEffectiveWorkHoursByIdCard(idCard, date);
    }

    /**
     * 查询导出列表数据（不分页）
     *
     * @param swmDailyAttendance 查询条件
     * @return 考勤记录列表
     */
    public List<SwmDailyAttendance> findExportList(SwmDailyAttendance swmDailyAttendance) {
        // 确保有日期条件，如果没有则默认当天
        if (swmDailyAttendance.getAttendanceDate() == null) {
            swmDailyAttendance.setAttendanceDate(new Date());
        }

        // 不设置分页，查询所有数据
        return dao.findList(swmDailyAttendance);
    }

    /**
     * 将SwmDailyAttendance列表转换为导出实体列表
     *
     * @param attendanceList 考勤记录列表
     * @return 导出实体列表
     */
    public List<SwmDailyAttendanceExportEntity> convertToExportList(List<SwmDailyAttendance> attendanceList) {
        List<SwmDailyAttendanceExportEntity> exportList = new ArrayList<>();

        for (SwmDailyAttendance attendance : attendanceList) {
            SwmDailyAttendanceExportEntity exportEntity = new SwmDailyAttendanceExportEntity();

            // 复制基本字段
            exportEntity.setEmployeeId(attendance.getEmployeeId());
            exportEntity.setEmployeeName(attendance.getEmployeeName());
            exportEntity.setPersonType(attendance.getPersonType());
            exportEntity.setAttendanceDate(attendance.getAttendanceDate());
            exportEntity.setWorkTimeRange(attendance.getWorkTimeRange());
            exportEntity.setClockInTime(attendance.getClockInTime());
            exportEntity.setClockOutTime(attendance.getClockOutTime());
            exportEntity.setScheduledHours(attendance.getScheduledHours());
            exportEntity.setActualHours(attendance.getActualHours());
            exportEntity.setIdleHours(attendance.getIdleHours());
            exportEntity.setEffectiveWorkHours(attendance.getEffectiveWorkHours());
            exportEntity.setDailyEfficiency(attendance.getDailyEfficiency());
            exportEntity.setDailyAchievementRate(attendance.getDailyAchievementRate());
            exportEntity.setAttendanceNormal(attendance.getAttendanceNormal());
            exportEntity.setPowerOnStatus(attendance.getPowerOnStatus());

            // 获取实时位置而不是使用数据库中的旧数据
            String realTimePosition = getRealTimePosition(attendance.getEmployeeId());
            exportEntity.setCurrentPosition(realTimePosition);

            exportEntity.setRemarks(attendance.getRemarks());

            exportList.add(exportEntity);
        }

        return exportList;
    }

    /**
     * 查询导出列表数据（不分页）通过月份
     *
     * @param swmMonthlyAttendance 查询条件
     * @return 考勤记录列表
     */
    public List<SwmMonthlyAttendance> findExportListByMonth(SwmMonthlyAttendance swmMonthlyAttendance) {
        // 确保有日期条件，如果没有则默认当月
        if (swmMonthlyAttendance.getCurrentMonth() == null) {
            swmMonthlyAttendance.setCurrentMonth(new Date());
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM");
        String month = sdf.format(swmMonthlyAttendance.getCurrentMonth());

        // 不设置分页，查询所有数据
        return dao.findStatisticsByMonth(month);
    }

    /**
     * 将SwmMonthlyAttendance列表转换为导出实体列表
     *
     * @param attendanceList 考勤记录列表
     * @return 导出实体列表
     */
    public List<SwmMonthlyAttendanceExportEntity> monthlyConvertToExportList(List<SwmMonthlyAttendance> attendanceList) {
        List<SwmMonthlyAttendanceExportEntity> exportList = new ArrayList<>();

        for (SwmMonthlyAttendance attendance : attendanceList) {
            SwmMonthlyAttendanceExportEntity exportEntity = new SwmMonthlyAttendanceExportEntity();

            // 复制基本字段
            exportEntity.setMonthly(attendance.getMonthly());
            exportEntity.setEmployeeName(attendance.getEmployeeName());
            exportEntity.setPhoneNumber(attendance.getPhoneNumber());
            exportEntity.setTeam(attendance.getTeam());
            exportEntity.setJobType(attendance.getJobType());
            exportEntity.setAttendanceDay(attendance.getAttendanceDay());
            exportEntity.setMonthlyAttendanceRate(attendance.getMonthlyAttendanceRate());
            exportEntity.setValidAttendanceDays(attendance.getValidAttendanceDays());
            exportEntity.setActualHours(attendance.getActualHours());
            exportEntity.setIdleHours(attendance.getIdleHours());

            exportList.add(exportEntity);
        }

        return exportList;
    }

    public Page<SwmMonthlyAttendance> findMonthlyByPage(SwmMonthlyAttendance swmMonthlyAttendance) {
        // 处理日期，去除时间部分
        String month ="";
        if (swmMonthlyAttendance.getCurrentMonth() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM");
            month = sdf.format(swmMonthlyAttendance.getCurrentMonth());
        }
        //分页查询处理
        List<SwmMonthlyAttendance> list = dao.findStatisticsByMonthWithPage(swmMonthlyAttendance.getTeam(), swmMonthlyAttendance.getEmployeeName(),
                month, swmMonthlyAttendance.getPageNo(), swmMonthlyAttendance.getPageSize());
        //查询对应的总数据量
        Long statisticsCount = dao.findStatisticsTotalByMonth(swmMonthlyAttendance.getTeam(), swmMonthlyAttendance.getEmployeeName(), month);
        //组装返回结果
        Page<SwmMonthlyAttendance> result = new Page<>();
        result.setCount(statisticsCount);
        result.setPageNo(swmMonthlyAttendance.getPageNo());
        result.setPageSize(swmMonthlyAttendance.getPageSize());
        result.setList(list);
        return result;
    }

    /**
     * 获取员工的实时位置
     *
     * @param employeeId 员工ID
     * @return "0"-工作区, "1"-休息区, "3"-未知
     * @author Shawn
     * @date 2025-01-28
     */
    private String getRealTimePosition(String employeeId) {
        if (StringUtils.isBlank(employeeId)) {
            return "3";
        }
        try {
            // 1. 获取员工身份证号
            String idCard = getIdCardByEmployeeId(employeeId);
            if (idCard == null) {
                logger.warn("无法根据员工ID {} 找到身份证号", employeeId);
                return "3";
            }
            // 2. 调用服务查询实时位置
            return areaFenceDataService.getCurrentLocationByIdCard(idCard);
        } catch (Exception e) {
            logger.error("获取员工 {} 实时位置失败", employeeId, e);
            return "3";
        }
    }

    public List<SwmDashboardNewController.JobTypeCount> statisticsPersonJobType(String date) {
        return dao.statisticsPersonJobType(date);
    }

    public Long countAttendanceByWorkshop(String companyName, String workshopName, String date) {
        return dao.countAttendanceByWorkshop(companyName, workshopName, date);
    }

    public Long countAttendanceByTeam(String companyName, String workshopName, String lineName, String teamName, String date) {
        return dao.countAttendanceByTeam(companyName, workshopName, lineName, teamName, date);
    }

    public Page<SwmDashboardNewController.Person> attendanceList(SwmDashboardNewController.Person person) {
        SwmDashboardNewController.Person personCount = new SwmDashboardNewController.Person();
        // 调用DAO层进行分页查询
        Page<SwmDashboardNewController.Person> page = person.getPage();
        if (page == null) {
            page = new Page<>();
        }
        // 从DAO获取分页数据
        List<SwmDashboardNewController.Person> list = dao.attendanceListPage(person);
        personCount.setDate(person.getDate());
        // 获取总记录数
        Long count = dao.attendanceListCount(personCount);

        // 设置分页结果
        page.setList(list);
        page.setCount(count);

        return page;
    }

    public List<SwmDailyAttendance> findByDateRange(Date beginDate, Date endDate) {
        // 处理日期，去除时间部分
        beginDate = truncateTime(beginDate);
        endDate = truncateTime(endDate);
        return dao.findByDateRange(beginDate, endDate);
    }

    public List<SwmDailyAttendance> findByEmployeeIdAndDateBatch(List<String> employeeIds, Date attendanceDate) {
        // 处理日期，去除时间部分
        attendanceDate = truncateTime(attendanceDate);
//        return dao.findByEmployeeIdAndDate(employeeId, attendanceDate);
        return dao.findByEmployeeIdAndDateBatch(employeeIds, attendanceDate);
    }

    public void updateBatch(List<SwmDailyAttendance> records) {
        dao.updateBatch(records);
    }

    public List<SwmDailyAttendance> findAllList(String startDate, String endDate) {
        return dao.findAllList(startDate, endDate);
    }

    public List<AiDto.WorkerFatigue> workerFatigue(AiDto.WorkerFatigue vo) {
        return dao.workerFatigue(vo);
    }

    public List<SwmDailyAttendance> findClockInCardList(String date) {
        return dao.findClockInCardList(date);
    }

    public List<SwmDailyAttendance> findClockOutCardList(String yestDay, String nowDate) {
        return dao.findClockOutCardList(yestDay, nowDate);
    }

    public List<SwmDashboardNewController.AttendanceAnalysis> getAllTeamNumber(String companyCode) {
        return dao.getAllTeamNumber(companyCode);
    }

    public List<SwmDashboardNewController.AttendanceAnalysis> getTeamAttendance(String companyCode, String date) {
        return dao.getTeamAttendance(companyCode, date);
    }

    public Page<SwmDashboardDto.IdleHoursRankingDto> idleHoursRanking(SwmDashboardDto.IdleHoursRankingDto  vo) {
        Page<SwmDashboardDto.IdleHoursRankingDto> page = vo.getPage();
        Date date = new Date();
        vo.setStartDate(DateUtil.beginOfMonth( date));
        vo.setEndDate(DateUtil.endOfMonth(date));
        List<SwmDashboardDto.IdleHoursRankingDto> list = dao.idleHoursRankingList(vo);
        page.setList(list);
        return page;
    }

    public Page<SwmDashboardDto.ManagementOnDutyDto> managementOnDuty(SwmDashboardDto.ManagementOnDutyDto vo) {
        Page<SwmDashboardDto.ManagementOnDutyDto> page = vo.getPage();
        Date date = new Date();
        vo.setStartDate(DateUtil.beginOfMonth( date));
        vo.setEndDate(DateUtil.endOfMonth(date));
        List<SwmDashboardDto.ManagementOnDutyDto> list = dao.managementOnDuty(vo);
        page.setList(list);
        return page;
    }

    public List<SwmDashboardDto.TeamAttendanceAnalysis> teamAttendanceAnalysis(
            SwmDashboardDto.TeamAttendanceAnalysis vo) {

        Date date = new Date();
        vo.setStartDate(DateUtil.beginOfMonth(date));
        vo.setEndDate(DateUtil.endOfMonth(date));
        vo.setPage(null);
        List<SwmDashboardDto.TeamAttendanceAnalysis> list = dao.teamAttendanceAnalysis(vo);
        if (CollectionUtils.isEmpty(list)) {
            return Collections.emptyList();
        }

        // 统计当天（yyyy-MM-dd）
        String statDate = new SimpleDateFormat("yyyy-MM-dd").format(date);

        // 按班组分组（兜底 null）
        Map<String, List<SwmDashboardDto.TeamAttendanceAnalysis>> teamMap =
                list.stream().collect(Collectors.groupingBy(
                        e -> Optional.ofNullable(e.getTeamName()).orElse("未分配班组")
                ));

        List<SwmDashboardDto.TeamAttendanceAnalysis> result = new ArrayList<>();

        for (Map.Entry<String, List<SwmDashboardDto.TeamAttendanceAnalysis>> entry : teamMap.entrySet()) {

            String teamName = entry.getKey();
            List<SwmDashboardDto.TeamAttendanceAnalysis> records = entry.getValue();

            /* -------------------- 当天应出勤人数 -------------------- */
            int dayShouldAttendance = (int) records.stream()
                    .filter(r -> statDate.equals(r.getAttendanceDate()))
                    .map(SwmDashboardDto.TeamAttendanceAnalysis::getEmployeeId)
                    .filter(Objects::nonNull)
                    .distinct()
                    .count();

            /* -------------------- 当天实际出勤人数 -------------------- */
            int dayActualAttendance = (int) records.stream()
                    .filter(r -> statDate.equals(r.getAttendanceDate()))
                    .filter(r -> r.getClockInDate() != null)
                    .map(SwmDashboardDto.TeamAttendanceAnalysis::getEmployeeId)
                    .filter(Objects::nonNull)
                    .distinct()
                    .count();

            /* -------------------- 日出勤率 -------------------- */
            BigDecimal dailyRate = dayShouldAttendance == 0
                    ? BigDecimal.ZERO
                    : BigDecimal.valueOf(dayActualAttendance)
                    .divide(BigDecimal.valueOf(dayShouldAttendance), 4, RoundingMode.HALF_UP);

            /* -------------------- 月出勤率（人 × 天） -------------------- */
            long monthShould = records.stream()
                    .map(r -> r.getEmployeeId() + "_" + r.getAttendanceDate())
                    .distinct()
                    .count();

            long monthActual = records.stream()
                    .filter(r -> r.getClockInDate() != null)
                    .map(r -> r.getEmployeeId() + "_" + r.getAttendanceDate())
                    .distinct()
                    .count();

            BigDecimal monthlyRate = monthShould == 0
                    ? BigDecimal.ZERO
                    : BigDecimal.valueOf(monthActual)
                    .divide(BigDecimal.valueOf(monthShould), 4, RoundingMode.HALF_UP);

            /* -------------------- 封装返回 -------------------- */
            SwmDashboardDto.TeamAttendanceAnalysis dto = new SwmDashboardDto.TeamAttendanceAnalysis();
            dto.setTeamName(teamName);
            dto.setShouldAttendance(dayShouldAttendance);
            dto.setActualAttendance(dayActualAttendance);
            dto.setDailyAttendanceRate(dailyRate.multiply(BigDecimal.valueOf(100)));
            dto.setMonthlyAttendanceRate(monthlyRate.multiply(BigDecimal.valueOf(100)));

            result.add(dto);
        }

        /* -------------------- 排序：日出勤率倒序 -------------------- */
        result.sort(Comparator.comparing(
                SwmDashboardDto.TeamAttendanceAnalysis::getDailyAttendanceRate
        ).reversed());

        return result;
    }

    public List<SwmDashboardDto.TeamAttendanceAnalysis> departmentAttendanceAnalysis(
            SwmDashboardDto.TeamAttendanceAnalysis vo) {

        Date date = new Date();
        vo.setStartDate(DateUtil.beginOfMonth(date));
        vo.setEndDate(DateUtil.endOfMonth(date));
        vo.setPage(null);
        List<SwmDashboardDto.TeamAttendanceAnalysis> list = dao.departmentAttendanceAnalysis(vo);
        if (CollectionUtils.isEmpty(list)) {
            return Collections.emptyList();
        }

        // 统计当天（yyyy-MM-dd）
        String statDate = new SimpleDateFormat("yyyy-MM-dd").format(date);

        // 按班组分组（兜底 null）
        Map<String, List<SwmDashboardDto.TeamAttendanceAnalysis>> departmentMap =
                list.stream().collect(Collectors.groupingBy(
                        e -> Optional.ofNullable(e.getDepartmentName()).orElse("未分配车间")
                ));

        List<SwmDashboardDto.TeamAttendanceAnalysis> result = new ArrayList<>();

        for (Map.Entry<String, List<SwmDashboardDto.TeamAttendanceAnalysis>> entry : departmentMap.entrySet()) {

            String departmentName = entry.getKey();
            List<SwmDashboardDto.TeamAttendanceAnalysis> records = entry.getValue();

            /* -------------------- 当天应出勤人数 -------------------- */
            int dayShouldAttendance = (int) records.stream()
                    .filter(r -> statDate.equals(r.getAttendanceDate()))
                    .map(SwmDashboardDto.TeamAttendanceAnalysis::getEmployeeId)
                    .filter(Objects::nonNull)
                    .distinct()
                    .count();

            /* -------------------- 当天实际出勤人数 -------------------- */
            int dayActualAttendance = (int) records.stream()
                    .filter(r -> statDate.equals(r.getAttendanceDate()))
                    .filter(r -> r.getClockInDate() != null)
                    .map(SwmDashboardDto.TeamAttendanceAnalysis::getEmployeeId)
                    .filter(Objects::nonNull)
                    .distinct()
                    .count();

            /* -------------------- 日出勤率 -------------------- */
            BigDecimal dailyRate = dayShouldAttendance == 0
                    ? BigDecimal.ZERO
                    : BigDecimal.valueOf(dayActualAttendance)
                    .divide(BigDecimal.valueOf(dayShouldAttendance), 4, RoundingMode.HALF_UP);

            /* -------------------- 月出勤率（人 × 天） -------------------- */
            long monthShould = records.stream()
                    .map(r -> r.getEmployeeId() + "_" + r.getAttendanceDate())
                    .distinct()
                    .count();

            long monthActual = records.stream()
                    .filter(r -> r.getClockInDate() != null)
                    .map(r -> r.getEmployeeId() + "_" + r.getAttendanceDate())
                    .distinct()
                    .count();

            BigDecimal monthlyRate = monthShould == 0
                    ? BigDecimal.ZERO
                    : BigDecimal.valueOf(monthActual)
                    .divide(BigDecimal.valueOf(monthShould), 4, RoundingMode.HALF_UP);

            /* -------------------- 封装返回 -------------------- */
            SwmDashboardDto.TeamAttendanceAnalysis dto = new SwmDashboardDto.TeamAttendanceAnalysis();
            dto.setDepartmentName(departmentName);
            dto.setShouldAttendance(dayShouldAttendance);
            dto.setActualAttendance(dayActualAttendance);
            dto.setDailyAttendanceRate(dailyRate.multiply(BigDecimal.valueOf(100)));
            dto.setMonthlyAttendanceRate(monthlyRate.multiply(BigDecimal.valueOf(100)));

            result.add(dto);
        }

        /* -------------------- 排序：日出勤率倒序 -------------------- */
        result.sort(Comparator.comparing(
                SwmDashboardDto.TeamAttendanceAnalysis::getDailyAttendanceRate
        ).reversed());

        return result;
    }

    public Page<SwmDashboardDto.NoAttendancePerson> noAttendancePerson(SwmDashboardDto.NoAttendancePerson vo) {
        Page<SwmDashboardDto.NoAttendancePerson> page = vo.getPage();
        List<SwmDashboardDto.NoAttendancePerson> list = dao.noAttendancePerson(vo);
        page.setList(list);
        return page;
    }

    public Page<SwmDashboardDto.NoAttendancePerson> beLatePerson(SwmDashboardDto.NoAttendancePerson vo) {
        Page<SwmDashboardDto.NoAttendancePerson> page = vo.getPage();
        Date date = new Date();
        vo.setStartDate(DateUtil.beginOfDay(date));
        vo.setEndDate(DateUtil.endOfDay(date));
        List<SwmDashboardDto.NoAttendancePerson> list = dao.beLatePerson(vo);
        page.setList(list);
        return page;
    }

    public Page<SwmDashboardDto.NoAttendancePerson> leaveEarlyPerson(SwmDashboardDto.NoAttendancePerson vo) {
        Page<SwmDashboardDto.NoAttendancePerson> page = vo.getPage();
        Date date = new Date();
        DateTime dateTime = DateUtil.offsetDay(date, -1);
        vo.setStartDate(DateUtil.beginOfDay(dateTime));
        vo.setEndDate(DateUtil.endOfDay(dateTime));
        List<SwmDashboardDto.NoAttendancePerson> list = dao.leaveEarlyPerson(vo);
        page.setList(list);
        return page;
    }
}
