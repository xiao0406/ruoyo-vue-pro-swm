package com.jeesite.modules.swm.dao;

import com.jeesite.common.dao.TreeDao;
import com.jeesite.common.mybatis.annotation.MyBatisDao;
import com.jeesite.modules.swm.entity.SwmDictData;

/**
 * 字典数据表DAO接口
 * @author wxy
 * @version 2026-04-24
 */
@MyBatisDao
public interface SwmDictDataDao extends TreeDao<SwmDictData> {
	
}