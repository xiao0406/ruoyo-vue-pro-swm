package com.jeesite.modules.swm.service;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jeesite.common.entity.Page;
import com.jeesite.common.service.TreeService;
import com.jeesite.modules.swm.entity.SwmDictData;
import com.jeesite.modules.swm.dao.SwmDictDataDao;

/**
 * 字典数据表Service
 * @author wxy
 * @version 2026-04-24
 */
@Service
@Transactional(readOnly=true)
public class SwmDictDataService extends TreeService<SwmDictDataDao, SwmDictData> {
	
	/**
	 * 获取单条数据
	 * @param swmDictData
	 * @return
	 */
	@Override
	public SwmDictData get(SwmDictData swmDictData) {
		SwmDictData swmDictData1 = super.get(swmDictData);
		return swmDictData1;
	}
	
	/**
	 * 查询分页数据
	 * @param swmDictData 查询条件
	 * @return
	 */
	@Override
	public Page<SwmDictData> findPage(SwmDictData swmDictData) {
		return super.findPage(swmDictData);
	}
	
	/**
	 * 查询列表数据
	 * @param swmDictData
	 * @return
	 */
	@Override
	public List<SwmDictData> findList(SwmDictData swmDictData) {
		return super.findList(swmDictData);
	}
	
	/**
	 * 保存数据（插入或更新）
	 * @param swmDictData
	 */
	@Override
	@Transactional(readOnly=false)
	public void save(SwmDictData swmDictData) {
		super.save(swmDictData);
	}
	
	/**
	 * 更新状态
	 * @param swmDictData
	 */
	@Override
	@Transactional(readOnly=false)
	public void updateStatus(SwmDictData swmDictData) {
		super.updateStatus(swmDictData);
	}
	
	/**
	 * 删除数据
	 * @param swmDictData
	 */
	@Override
	@Transactional(readOnly=false)
	public void delete(SwmDictData swmDictData) {
		super.delete(swmDictData);
	}


}