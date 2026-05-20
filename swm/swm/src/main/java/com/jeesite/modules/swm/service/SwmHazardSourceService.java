/**
 * 危险源信息Service
 *
 * @author Shawn
 * @version 2025-05-21
 */
package com.jeesite.modules.swm.service;

import com.jeesite.common.entity.Page;
import com.jeesite.common.service.CrudService;
import com.jeesite.modules.swm.dao.SwmHazardSourceDao;
import com.jeesite.modules.entity.SwmHazardSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 危险源信息Service
 */
@Service
@Transactional(readOnly = true)
public class SwmHazardSourceService extends CrudService<SwmHazardSourceDao, SwmHazardSource> {

    /**
     * 获取单条数据
     *
     * @param swmHazardSource
     * @return
     */
    @Override
    public SwmHazardSource get(SwmHazardSource swmHazardSource) {
        return super.get(swmHazardSource);
    }

    /**
     * 查询分页数据
     *
     * @param swmHazardSource
     * @return
     */
    public Page<SwmHazardSource> findPage(SwmHazardSource swmHazardSource) {
        return super.findPage(swmHazardSource);
    }

    /**
     * 保存数据（插入或更新）
     *
     * @param swmHazardSource
     */
    @Override
    @Transactional(readOnly = false)
    public void save(SwmHazardSource swmHazardSource) {
        super.save(swmHazardSource);
    }

    /**
     * 更新状态
     *
     * @param swmHazardSource
     */
    @Override
    @Transactional(readOnly = false)
    public void updateStatus(SwmHazardSource swmHazardSource) {
        super.updateStatus(swmHazardSource);
    }

    /**
     * 删除数据
     *
     * @param swmHazardSource
     */
    @Override
    @Transactional(readOnly = false)
    public void delete(SwmHazardSource swmHazardSource) {
        super.delete(swmHazardSource);
    }

    /**
     * 根据状态统计数量
     * @param status 状态
     * @param year 年
     * @param month 月
     * @return
     */
    public int countByStatusAndMonth(String status, String year, String month) {
        return dao.countByStatusAndMonth(status, year, month);
    }

    /**
     * 根据类别统计数量
     * @param year 年
     * @param month 月
     * @return
     */
    public List<Map<String, Object>> countByCategoryAndMonth(String year, String month) {
        return dao.countByCategoryAndMonth(year, month);
    }

    /**
     * 获取Top10 dangerous categories
     * @param year 年
     * @param month 月
     * @return
     */
    public List<Map<String, Object>> getTop10Categories(String year, String month) {
        return dao.getTop10Categories(year, month);
    }

    /**
     * 获取危险源数量
     * @param year
     * @param month
     * @return
     */
    public int countByYearAndMonth(String year, String month) {
        return dao.countByYearAndMonth(year, month);
    }

    /**
     * 查询TOP N危险源类别
     */
    public List<Map<String, Object>> findTopCategories(String beginDate, String endDate, int limit) {
        return dao.findTopCategories(beginDate, endDate, limit);
    }

    /**
     * 查询危险源类别分布
     */
    public List<Map<String, Object>> findCategoryDistribution(String beginDate, String endDate) {
        return dao.findCategoryDistribution(beginDate, endDate);
    }

    /**
     * 按日期范围分组统计
     */
    public List<Map<String, Object>> countByDateRangeGroupByDay(Date beginDate, Date endDate) {
        return dao.countByDateRangeGroupByDay(beginDate, endDate);
    }

    /**
     * 按类别和日期范围分组统计
     */
    public List<Map<String, Object>> countCategoryTrendByDateRange(Date beginDate, Date endDate) {
        return dao.countCategoryTrendByDateRange(beginDate, endDate);
    }

    /**
     * 按日期范围和状态统计数量
     * @param beginDate 开始日期
     * @param endDate 结束日期
     * @param statusList 状态列表（null表示不限制状态）
     */
    public int countByDateRange(Date beginDate, Date endDate, List<String> statusList){
        return dao.countByDateRange(beginDate, endDate, statusList);
    }

    /**
     * 统计未制定巡检计划的数量
     */
    public int countNoInspectionPlan(Date beginDate, Date endDate){
        return dao.countNoInspectionPlan(beginDate, endDate);
    }

    /**
     * 统计所有数量
     * @return
     */
    public int countAll() {
        return dao.countAll();
    }
}
