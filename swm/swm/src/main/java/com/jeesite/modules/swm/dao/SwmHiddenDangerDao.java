/**
 * 隐患信息DAO接口
 * @author Shawn
 * @date 2025-05-21
 */
package com.jeesite.modules.swm.dao;

import com.jeesite.common.dao.CrudDao;
import com.jeesite.common.mybatis.annotation.MyBatisDao;
import com.jeesite.modules.swm.entity.SwmHiddenDanger;

import java.util.List;

/**
 * 隐患信息DAO接口
 * 
 * @author Shawn
 * @date 2025-05-21
 */
@MyBatisDao
public interface SwmHiddenDangerDao extends CrudDao<SwmHiddenDanger> {
    
    /**
     * 查询隐患列表，包含巡检计划名称
     * @param swmHiddenDanger 查询条件
     * @return 隐患列表
     */
    List<SwmHiddenDanger> findListWithPlanName(SwmHiddenDanger swmHiddenDanger);
}