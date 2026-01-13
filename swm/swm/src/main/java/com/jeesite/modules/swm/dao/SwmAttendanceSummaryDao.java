package com.jeesite.modules.swm.dao;

import com.jeesite.common.dao.CrudDao;
import com.jeesite.common.entity.Page;
import com.jeesite.common.mybatis.annotation.MyBatisDao;
import com.jeesite.modules.swm.entity.SwmAttendanceSummary;

import java.util.List;
import java.util.Map;

/**
 * 考勤月统计表DAO接口
 * @author  zwf
 * @version 2025-05-20
 */
@MyBatisDao
public interface SwmAttendanceSummaryDao extends CrudDao<SwmAttendanceSummary> {
    
    /**
     * 根据月份查询考勤统计记录
     * @param month 统计月份 格式(YYYY-MM)
     * @return 考勤统计记录列表
     */
    List<SwmAttendanceSummary> findByMonth(String month);
    
    /**
     * 根据员工ID和月份查询考勤统计记录
     * @param employeeId 员工ID
     * @param month 统计月份 格式(YYYY-MM)
     * @return 考勤统计记录
     */
    SwmAttendanceSummary findByEmployeeIdAndMonth(String employeeId, String month,String corpCode);
    
    /**
     * 根据员工姓名和月份查询考勤统计记录
     * @param employeeName 员工姓名
     * @param month 统计月份 格式(YYYY-MM)
     * @return 考勤统计记录
     */
    SwmAttendanceSummary findByEmployeeAndMonth(String employeeName, String month);
    
    /**
     * 根据部门和月份查询考勤统计记录
     * @param department 部门名称
     * @param month 统计月份 格式(YYYY-MM)
     * @return 考勤统计记录列表
     */
    List<SwmAttendanceSummary> findByDepartmentAndMonth(String department, String month);
    
    /**
     * 查询所有状态的记录（不受状态限制）
     * @param params 查询参数
     * @return 记录列表
     */
    List<SwmAttendanceSummary> findAllWithoutStatusFilter(Map<String, Object> params);
    
    /**
     * 计算符合条件的记录总数
     * @param params 查询参数
     * @return 记录总数
     */
    int countAllWithoutStatusFilter(Map<String, Object> params);
} 