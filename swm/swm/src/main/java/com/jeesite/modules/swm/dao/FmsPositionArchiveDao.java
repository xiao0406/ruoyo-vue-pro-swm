package com.jeesite.modules.swm.dao;

import com.jeesite.common.dao.CrudDao;
import com.jeesite.common.mybatis.annotation.MyBatisDao;
import com.jeesite.modules.fms.entity.FmsPositionArchive;

/**
 * 位置档案定义DAO接口
 * @author wxy
 * @version 2026-03-02
 */
@MyBatisDao
public interface FmsPositionArchiveDao extends CrudDao<FmsPositionArchive> {
	
}