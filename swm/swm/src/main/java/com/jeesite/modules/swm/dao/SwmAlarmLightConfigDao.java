/**
 * Copyright (c) 2013-Now http://jeesite.com All rights reserved.
 * No deletion without permission, or be held responsible to law.
 */
package com.jeesite.modules.swm.dao;

import com.jeesite.common.dao.CrudDao;
import com.jeesite.common.mybatis.annotation.MyBatisDao;
import com.jeesite.modules.swm.entity.SwmAlarmLightConfig;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 报警灯配置关联DAO接口
 * @author Shawn
 * @version 2025-01-19
 */
@MyBatisDao
public interface SwmAlarmLightConfigDao extends CrudDao<SwmAlarmLightConfig> {
	
	/**
	 * 根据报警灯ID查询配置列表
	 */
	List<SwmAlarmLightConfig> findListByLightId(@Param("lightId") String lightId);
	
	/**
	 * 根据报警灯ID软删除所有配置
	 */
	void deleteByLightId(@Param("lightId") String lightId, @Param("updateBy") String updateBy);
	
}