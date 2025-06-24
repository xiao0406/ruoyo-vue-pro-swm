package com.jeesite.modules.swm.service;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jeesite.common.entity.Page;
import com.jeesite.common.service.CrudService;
import com.jeesite.modules.swm.dao.SwmHelmetConfigDao;
import com.jeesite.modules.swm.dao.SwmHelmetSubitemDao;
import com.jeesite.modules.swm.entity.SwmHelmetConfig;
import com.jeesite.modules.swm.entity.SwmHelmetSubitem;

/**
 * 安全帽主配置表Service
 * 
 * @author zwf
 * @version 2025-05-20
 * @update 2025/06/24 by Shawn - 添加获取车间数据方法
 */
@Service
@Transactional(readOnly = true)
public class SwmHelmetConfigService extends CrudService<SwmHelmetConfigDao, SwmHelmetConfig> {

	@Autowired
	private SwmHelmetSubitemDao swmHelmetSubitemDao;

	/**
	 * 获取单条数据
	 * 
	 * @param swmHelmetConfig 查询条件
	 * @return 安全帽配置信息
	 */
	@Override
	public SwmHelmetConfig get(SwmHelmetConfig swmHelmetConfig) {
		return super.get(swmHelmetConfig);
	}

	/**
	 * 查询分页数据
	 * 
	 * @param swmHelmetConfig 查询条件
	 * @return 分页数据
	 */
	public Page<SwmHelmetConfig> findPage(SwmHelmetConfig swmHelmetConfig) {
		return super.findPage(swmHelmetConfig);
	}

	/**
	 * 查询分页数据
	 * 
	 * @param page            分页对象
	 * @param swmHelmetConfig 查询条件
	 * @return 分页数据
	 */
	public Page<SwmHelmetConfig> findPage(Page<SwmHelmetConfig> page, SwmHelmetConfig swmHelmetConfig) {
		return this.findPage(swmHelmetConfig);
	}

	/**
	 * 查询列表数据
	 * 
	 * @param swmHelmetConfig 查询条件
	 * @return 列表数据
	 */
	@Override
	public List<SwmHelmetConfig> findList(SwmHelmetConfig swmHelmetConfig) {
		return super.findList(swmHelmetConfig);
	}

	/**
	 * 保存数据（插入或更新）
	 * 
	 * @param swmHelmetConfig 实体对象
	 */
	@Override
	@Transactional(readOnly = false)
	public void save(SwmHelmetConfig swmHelmetConfig) {
		super.save(swmHelmetConfig);
		// 更新子项数量
		updateSubitemCount(swmHelmetConfig.getId());
	}

	/**
	 * 更新子项数量
	 * 
	 * @param configId 配置ID
	 */
	@Transactional(readOnly = false)
	public void updateSubitemCount(String configId) {
		int count = swmHelmetSubitemDao.getCountByParentId(configId);
		SwmHelmetConfig config = new SwmHelmetConfig(configId);
		config.setSubitemCount(count);
		super.update(config);
	}

	/**
	 * 删除数据
	 * 
	 * @param swmHelmetConfig 实体对象
	 */
	@Override
	@Transactional(readOnly = false)
	public void delete(SwmHelmetConfig swmHelmetConfig) {
		// 先删除子项
		swmHelmetSubitemDao.deleteByParentId(swmHelmetConfig.getId());
		System.out.println("父表id" + swmHelmetConfig.getId());
		// 再删除主表
		super.delete(swmHelmetConfig);
	}

	/**
	 * 获取车间数据
	 * 
	 * @author Shawn
	 * @date 2025/06/24
	 * @description 查询fms_position_archive表中type='CJ'的车间数据
	 * @return 车间数据列表
	 */
	public List<Map<String, Object>> getWorkshopData() {
		return dao.getWorkshopData();
	}

}