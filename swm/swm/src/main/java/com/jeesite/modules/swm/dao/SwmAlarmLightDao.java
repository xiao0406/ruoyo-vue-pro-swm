/**
 * Copyright (c) 2013-Now http://jeesite.com All rights reserved.
 * No deletion without permission, or be held responsible to law.
 */
package com.jeesite.modules.swm.dao;

import com.jeesite.common.dao.CrudDao;
import com.jeesite.common.mybatis.annotation.MyBatisDao;
import com.jeesite.modules.swm.entity.SwmAlarmLight;

/**
 * 报警灯设备DAO接口
 * @author Shawn
 * @version 2025-01-19
 */
@MyBatisDao
public interface SwmAlarmLightDao extends CrudDao<SwmAlarmLight> {
	
}