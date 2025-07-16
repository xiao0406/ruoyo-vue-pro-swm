package com.jeesite.modules.swm.dao;

import com.jeesite.common.dao.CrudDao;
import com.jeesite.common.mybatis.annotation.MyBatisDao;
import com.jeesite.modules.swm.entity.SwmInspectionList;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 巡检列表DAO接口
 *
 * @author Shawn
 * @version 2025-05-21
 */
@MyBatisDao
public interface SwmInspectionListDao extends CrudDao<SwmInspectionList> {

    /**
     * 查询计划对应的最后一次任务
     * @param planId 计划ID
     * @return
     */
    SwmInspectionList getLastTaskByPlanId(String planId);

    /**
     * 最新的巡检记录
     * @param limit 查询数量
     * @return
     */
    List<SwmInspectionList> latestInspectionRecord(@Param("limit") int limit);
    
    /**
     * 根据计划ID列表查询巡检记录
     * @param planIds 计划ID列表
     * @return 巡检记录列表
     */
    List<Map<String, Object>> findByPlanIds(@Param("planIds") List<String> planIds);
}
