/**
 * Copyright (c) 2013-Now http://jeesite.com All rights reserved.
 * No deletion without permission, or be held responsible to law.
 */
package com.jeesite.modules.swm.service;

import com.jeesite.modules.sys.utils.CorpUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jeesite.common.entity.DataEntity;
import com.jeesite.common.entity.Page;
import com.jeesite.common.idgen.IdGen;
import com.jeesite.common.lang.StringUtils;
import com.jeesite.common.service.CrudService;
import com.jeesite.modules.swm.entity.SwmAlarmLight;
import com.jeesite.modules.swm.dao.SwmAlarmLightDao;

/**
 * 报警灯设备Service
 * @author Shawn
 * @version 2025-01-19
 */
@Service
@Transactional(readOnly = true)
public class SwmAlarmLightService extends CrudService<SwmAlarmLightDao, SwmAlarmLight> {
	
	@Autowired
	private SwmAlarmLightConfigService swmAlarmLightConfigService;
	
	/**
	 * 获取单条数据
	 * @param swmAlarmLight
	 * @return
	 */
	@Override
	public SwmAlarmLight get(SwmAlarmLight swmAlarmLight) {
		return super.get(swmAlarmLight);
	}
	
	/**
	 * 查询分页数据
	 * @param swmAlarmLight 查询条件
	 * @param swmAlarmLight page 分页对象
	 * @return
	 */
	@Override
	public Page<SwmAlarmLight> findPage(SwmAlarmLight swmAlarmLight) {
		return super.findPage(swmAlarmLight);
	}
	
	/**
	 * 保存数据（插入或更新）
	 * @param swmAlarmLight
	 */
	@Override
	@Transactional(readOnly = false)
	public void save(SwmAlarmLight swmAlarmLight) {
		String corpCode = CorpUtils.getCurrentCorpCode();
		String currentCorpName = CorpUtils.getCurrentCorpName();
		// 如果是新记录且ID为空，则自动生成ID
		if (swmAlarmLight.getIsNewRecord() && StringUtils.isBlank(swmAlarmLight.getId())) {
			swmAlarmLight.setId(IdGen.nextId());
		}

		// 如果是新记录且状态为空，则设置为正常状态
		if (swmAlarmLight.getIsNewRecord() && StringUtils.isBlank(swmAlarmLight.getStatus())) {
			swmAlarmLight.setStatus(DataEntity.STATUS_NORMAL);
		}
		super.save(swmAlarmLight);
	}
	
	/**
	 * 更新状态
	 * @param swmAlarmLight
	 */
	@Override
	@Transactional(readOnly = false)
	public void updateStatus(SwmAlarmLight swmAlarmLight) {
		super.updateStatus(swmAlarmLight);
	}
	
	/**
	 * 删除数据
	 * @param swmAlarmLight
	 */
	@Override
	@Transactional(readOnly = false)
	public void delete(SwmAlarmLight swmAlarmLight) {
		super.delete(swmAlarmLight);
		// 同时软删除该报警灯的所有配置
		swmAlarmLightConfigService.deleteByLightId(swmAlarmLight.getId(), swmAlarmLight.getCurrentUser().getId());
	}
	
}