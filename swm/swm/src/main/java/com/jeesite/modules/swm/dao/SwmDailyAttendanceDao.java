package com.jeesite.modules.swm.dao;

import com.jeesite.common.dao.CrudDao;
import com.jeesite.common.mybatis.annotation.MyBatisDao;
import com.jeesite.modules.swm.entity.SwmDailyAttendance;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

/**
 * 日考勤统计表DAO接口
 * @author  zwf
 * @version 2025-05-20
 */
@MyBatisDao
public interface SwmDailyAttendanceDao extends CrudDao<SwmDailyAttendance> {
    
    /**
     * 自定义更新方法，确保包含打卡时间字段
     * @param swmDailyAttendance 日考勤记录
     * @return 影响的行数
     */
    int updateWithClockTime(SwmDailyAttendance swmDailyAttendance);

    /**
     * 根据员工ID和日期查询考勤记录
     * @param employeeId 员工ID
     * @param attendanceDate 考勤日期
     * @return 考勤记录
     */
    SwmDailyAttendance findByEmployeeIdAndDate(String employeeId, Date attendanceDate);

    /**
     * 根据员工姓名和日期查询考勤记录
     * @param employeeName 员工姓名
     * @param attendanceDate 考勤日期
     * @return 考勤记录
     */
    SwmDailyAttendance findByEmployeeAndDate(String employeeName, Date attendanceDate);

    /**
     * 根据员工ID和日期范围查询考勤记录
     * @param employeeId 员工ID
     * @param beginDate 开始日期
     * @param endDate 结束日期
     * @return 考勤记录列表
     */
    List<SwmDailyAttendance> findByEmployeeIdAndDateRange(String employeeId, Date beginDate, Date endDate);

    /**
     * 根据员工姓名和日期范围查询考勤记录
     * @param employeeName 员工姓名
     * @param beginDate 开始日期
     * @param endDate 结束日期
     * @return 考勤记录列表
     */
    List<SwmDailyAttendance> findByEmployeeAndDateRange(String employeeName, Date beginDate, Date endDate);

    /**
     * 根据日期查询所有考勤记录
     * @param attendanceDate 考勤日期
     * @return 考勤记录列表
     */
    List<SwmDailyAttendance> findByDate(Date attendanceDate);

    /**
     * 根据员工ID和月份查询考勤记录
     * @param employeeId 员工ID
     * @param year 年份
     * @param month 月份 (1-12)
     * @return 考勤记录列表
     */
    List<SwmDailyAttendance> findByEmployeeIdAndMonth(String employeeId, int year, int month);

    /**
     * 根据员工姓名和月份查询考勤记录
     * @param employeeName 员工姓名
     * @param year 年份
     * @param month 月份 (1-12)
     * @return 考勤记录列表
     */
    List<SwmDailyAttendance> findByEmployeeAndMonth(String employeeName, int year, int month);

    /**
     * 查询指定月份的日考勤记录
     * @param employeeId 员工ID(可选)
     * @param month 月份(格式: yyyy-MM)
     * @return 日考勤记录列表
     */
    List<SwmDailyAttendance> findByMonth(@Param("employeeId") String employeeId, @Param("month") String month);
}
