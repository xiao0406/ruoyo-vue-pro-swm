/**
 * Copyright (c) 2013-Now http://jeesite.com All rights reserved.
 * No deletion without permission, or be held responsible to law.
 */
package com.jeesite.modules.swm.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jeesite.common.entity.DataEntity;
import com.jeesite.common.entity.Page;
import com.jeesite.common.idgen.IdGen;
import com.jeesite.common.lang.StringUtils;
import com.jeesite.common.service.CrudService;
import com.jeesite.modules.swm.entity.SwmAlarmLightConfig;
import com.jeesite.modules.swm.dao.SwmAlarmLightConfigDao;

/**
 * 报警灯配置关联Service
 * @author Shawn
 * @version 2025-01-19
 */
@Service
@Transactional(readOnly = true)
public class SwmAlarmLightConfigService extends CrudService<SwmAlarmLightConfigDao, SwmAlarmLightConfig> {
	
	/**
	 * 获取单条数据
	 * @param swmAlarmLightConfig
	 * @return
	 */
	@Override
	public SwmAlarmLightConfig get(SwmAlarmLightConfig swmAlarmLightConfig) {
		return super.get(swmAlarmLightConfig);
	}
	
	/**
	 * 查询分页数据
	 * @param swmAlarmLightConfig 查询条件
	 * @param swmAlarmLightConfig page 分页对象
	 * @return
	 */
	@Override
	public Page<SwmAlarmLightConfig> findPage(SwmAlarmLightConfig swmAlarmLightConfig) {
		return super.findPage(swmAlarmLightConfig);
	}
	
	/**
	 * 保存数据（插入或更新）
	 * @param swmAlarmLightConfig
	 */
	@Override
	@Transactional(readOnly = false)
	public void save(SwmAlarmLightConfig swmAlarmLightConfig) {
		// 如果是新记录且ID为空，则自动生成ID
		if (swmAlarmLightConfig.getIsNewRecord() && StringUtils.isBlank(swmAlarmLightConfig.getId())) {
			swmAlarmLightConfig.setId(IdGen.nextId());
		}

		// 如果是新记录且状态为空，则设置为正常状态
		if (swmAlarmLightConfig.getIsNewRecord() && StringUtils.isBlank(swmAlarmLightConfig.getStatus())) {
			swmAlarmLightConfig.setStatus(DataEntity.STATUS_NORMAL);
		}

		super.save(swmAlarmLightConfig);
	}
	
	/**
	 * 更新状态
	 * @param swmAlarmLightConfig
	 */
	@Override
	@Transactional(readOnly = false)
	public void updateStatus(SwmAlarmLightConfig swmAlarmLightConfig) {
		super.updateStatus(swmAlarmLightConfig);
	}
	
	/**
	 * 删除数据
	 * @param swmAlarmLightConfig
	 */
	@Override
	@Transactional(readOnly = false)
	public void delete(SwmAlarmLightConfig swmAlarmLightConfig) {
		super.delete(swmAlarmLightConfig);
	}
	
	/**
	 * 根据报警灯ID查询配置列表
	 * @param lightId
	 * @return
	 */
	public List<SwmAlarmLightConfig> findListByLightId(String lightId) {
		logger.debug("Service层查询报警灯配置，lightId: {}", lightId);
		List<SwmAlarmLightConfig> result = dao.findListByLightId(lightId);
		logger.debug("Service层查询结果数量: {}", result != null ? result.size() : 0);
		return result;
	}
	
	/**
	 * 根据报警灯ID软删除所有配置
	 * @param lightId
	 * @param updateBy
	 */
	@Transactional(readOnly = false)
	public void deleteByLightId(String lightId, String updateBy) {
		dao.deleteByLightId(lightId, updateBy);
	}
	
}