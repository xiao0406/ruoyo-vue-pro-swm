package com.jeesite.modules.swm.dao;

import com.jeesite.common.dao.CrudDao;
import com.jeesite.common.mybatis.annotation.MyBatisDao;
import com.jeesite.modules.swm.entity.SwmInspectionList;

/**
 * 巡检列表DAO接口
 * 
 * @author Shawn
 * @version 2024-06-22
 */
@MyBatisDao
public interface SwmInspectionListDao extends CrudDao<SwmInspectionList> {

}