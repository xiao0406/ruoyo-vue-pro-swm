/**
 * 一键召回记录表DAO接口
 * @author auto
 * @date 2024-05-30
 */
package com.jeesite.modules.swm.dao;

import com.jeesite.common.dao.CrudDao;
import com.jeesite.common.mybatis.annotation.MyBatisDao;
import com.jeesite.modules.swm.entity.SwmOneClickRecall;

/**
 * 一键召回记录表DAO接口
 * 
 * @author auto
 */
@MyBatisDao
public interface SwmOneClickRecallDao extends CrudDao<SwmOneClickRecall> {
    
} 