package com.jeesite.modules.swm.dao;

import com.jeesite.common.dao.CrudDao;
import com.jeesite.common.mybatis.annotation.MyBatisDao;
import com.jeesite.modules.swm.entity.SwmDictType;

/**
 * 字典类型表DAO接口
 * @author wxy
 * @version 2026-04-24
 */
@MyBatisDao
public interface SwmDictTypeDao extends CrudDao<SwmDictType> {
	
}