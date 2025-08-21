package com.jeesite.modules.swm.service;

import com.jeesite.common.entity.Page;
import com.jeesite.common.service.CrudService;
import com.jeesite.modules.swm.dao.SwmAttendanceSummaryDao;
import com.jeesite.modules.swm.entity.SwmAttendanceSummary;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * 考勤月统计表Service
 * @author  zwf
 * @version 2025-05-20
 */
@Service
@Transactional(readOnly = true)
public class SwmAttendanceSummaryService extends CrudService<SwmAttendanceSummaryDao, SwmAttendanceSummary> {

    /**
     * 获取单条数据
     * @param swmAttendanceSummary
     * @return
     */
    @Override
    public SwmAttendanceSummary get(SwmAttendanceSummary swmAttendanceSummary) {
        return super.get(swmAttendanceSummary);
    }

    /**
     * 查询分页数据
     * @param swmAttendanceSummary
     * @return
     */
    @Override
    public Page<SwmAttendanceSummary> findPage(SwmAttendanceSummary swmAttendanceSummary) {
        return super.findPage(swmAttendanceSummary);
    }

    /**
     * 查询列表数据
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
     * @param swmAttendanceSummary
     */
    @Override
    @Transactional(readOnly = false)
    public void updateStatus(SwmAttendanceSummary swmAttendanceSummary) {
        super.updateStatus(swmAttendanceSummary);
    }

    /**
     * 删除数据
     * @param swmAttendanceSummary
     */
    @Override
    @Transactional(readOnly = false)
    public void delete(SwmAttendanceSummary swmAttendanceSummary) {
        super.delete(swmAttendanceSummary);
    }

    /**
     * 根据月份查询考勤统计记录
     * @param month 统计月份 格式(YYYY-MM)
     * @return 考勤统计记录列表
     */
    public List<SwmAttendanceSummary> findByMonth(String month) {
        return dao.findByMonth(month);
    }

    /**
     * 根据员工姓名和月份查询考勤统计记录
     * @param employeeName 员工姓名
     * @param month 统计月份 格式(YYYY-MM)
     * @return 考勤统计记录
     */
    public SwmAttendanceSummary findByEmployeeAndMonth(String employeeName, String month) {
        return dao.findByEmployeeAndMonth(employeeName, month);
    }

    /**
     * 根据部门和月份查询考勤统计记录
     * @param department 部门名称
     * @param month 统计月份 格式(YYYY-MM)
     * @return 考勤统计记录列表
     */
    public List<SwmAttendanceSummary> findByDepartmentAndMonth(String department, String month) {
        return dao.findByDepartmentAndMonth(department, month);
    }

    /**
     * 根据员工ID和月份查询考勤统计记录
     * @param employeeId 员工ID
     * @param month 统计月份 格式(YYYY-MM)
     * @return 考勤统计记录
     */
    public SwmAttendanceSummary findByEmployeeIdAndMonth(String employeeId, String month) {
        return dao.findByEmployeeIdAndMonth(employeeId, month);
    }

    /**
     * 根据实体查询考勤统计记录
     * @param entity
     * @return
     */
    public SwmAttendanceSummary getByEntity(SwmAttendanceSummary entity) {
        return dao.getByEntity(entity);
    }

    /**
     * 根据身份证号和月份查询考勤统计记录
     * @param identityCard 身份证号
     * @param month 统计月份 格式(YYYY-MM)
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
}
