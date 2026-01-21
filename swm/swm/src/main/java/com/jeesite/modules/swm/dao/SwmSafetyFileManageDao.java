package com.jeesite.modules.swm.dao;

import com.jeesite.common.dao.CrudDao;
import com.jeesite.common.mybatis.annotation.MyBatisDao;
import com.jeesite.modules.entity.SwmSafetyFileManage;

/**
 * 安全教育视频管理DAO接口
 * @author wxy
 * @version 2026-01-19
 */
@MyBatisDao
public interface SwmSafetyFileManageDao extends CrudDao<SwmSafetyFileManage> {
	
}