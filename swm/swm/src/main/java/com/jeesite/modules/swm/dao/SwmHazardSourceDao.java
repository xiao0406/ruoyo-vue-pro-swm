/**
 * 危险源信息DAO接口
 * @author Shawn
 * @version 2025-05-21
 */
package com.jeesite.modules.swm.dao;

import com.jeesite.common.dao.CrudDao;
import com.jeesite.common.mybatis.annotation.MyBatisDao;
import com.jeesite.modules.entity.SwmHazardSource;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 危险源信息DAO接口
 */
@MyBatisDao
public interface SwmHazardSourceDao extends CrudDao<SwmHazardSource> {

    /**
     * 根据状态统计数量
     * @param status 状态
     * @param year 年
     * @param month 月
     * @return
     */
    int countByStatusAndMonth(@Param("status") String status, @Param("year") String year, @Param("month") String month);

    /**
     * 按危险源类别统计数量
     */
    List<Map<String, Object>> countByCategoryAndMonth(@Param("year") String year, @Param("month") String month);

    /**
     * 获取危险源类别排名前10
     */
    List<Map<String, Object>> getTop10Categories(@Param("year") String year, @Param("month") String month);

    /**
     * 统计危险源数量
     * @param year  年
     * @param month  月
     * @return
     */
    int countByYearAndMonth(@Param("year") String year,@Param("month") String month);

    /**
     * 按日期范围统计危险源数量
     * @param beginDate 开始日期
     * @param endDate 结束日期
     * @return
     */
    List<Map<String, Object>> countByDateRangeGroupByDay(@Param("beginDate") Date beginDate,@Param("endDate") Date endDate);

    /**
     * 查询TOP N危险源类别
     * @param beginDate 开始日期(格式：yyyy-MM-dd)
     * @param endDate 结束日期(格式：yyyy-MM-dd)
     * @param limit 返回数量
     * @return 包含category(类别)和count(数量)的Map列表
     */
    List<Map<String, Object>> findTopCategories(@Param("beginDate") String beginDate, @Param("endDate") String endDate, @Param("limit") int limit);

    /**
     * 查询危险源类别分布
     * @param beginDate 危险源开始日期(格式：yyyy-MM-dd)
     * @param endDate 危险源结束日期(格式：yyyy-MM-dd)
     * @return 危险源类别分布
     */
    List<Map<String, Object>> findCategoryDistribution(@Param("beginDate") String beginDate,@Param("endDate") String endDate);

    /**
     * 按日期范围统计危险源类别趋势
     * @param beginDate 开始日期
     * @param endDate 结束日期
     * @return 危险源类别趋势
     */
    List<Map<String, Object>> countCategoryTrendByDateRange(@Param("beginDate") Date beginDate,@Param("endDate") Date endDate);

    /**
     * 按日期范围和状态统计数量
     * @param beginDate 开始日期
     * @param endDate 结束日期
     * @param statusList 状态列表（null表示不限制状态）
     */
    int countByDateRange(@Param("beginDate") Date beginDate,@Param("endDate") Date endDate,@Param("statusList") List<String> statusList);

    /**
     * 统计未制定巡检计划的数量
     */
    int countNoInspectionPlan(@Param("beginDate") Date beginDate,@Param("endDate") Date endDate);

    /**
     * 统计所有数量
     */
    int countAll();
}
