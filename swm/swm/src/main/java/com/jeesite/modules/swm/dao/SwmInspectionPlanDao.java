package com.jeesite.modules.swm.dao;

import com.jeesite.common.dao.CrudDao;
import com.jeesite.common.mybatis.annotation.MyBatisDao;
import com.jeesite.modules.swm.entity.SwmInspectionPlan;

/**
 * 巡检计划DAO接口
 * 
 * @author Shawn
 * @version 2025-05-21
 */
@MyBatisDao
public interface SwmInspectionPlanDao extends CrudDao<SwmInspectionPlan> {

    /**
     * 根据前缀查询最大编号
     * 
     * @param prefix 编号前缀
     * @return 最大编号
     */
    String getMaxPlanCodeByPrefix(String prefix);

}