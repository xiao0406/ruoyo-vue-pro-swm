package com.jeesite.modules.swm.service;

import java.util.List;

import com.jeesite.common.lang.StringUtils;
import com.jeesite.modules.sys.entity.DictType;
import com.jeesite.modules.sys.utils.DictUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jeesite.common.entity.Page;
import com.jeesite.common.service.CrudService;
import com.jeesite.modules.swm.entity.SwmDictType;
import com.jeesite.modules.swm.dao.SwmDictTypeDao;
import com.jeesite.modules.swm.entity.SwmDictData;
import com.jeesite.modules.swm.dao.SwmDictDataDao;

/**
 * 字典类型表Service
 * @author wxy
 * @version 2026-04-24
 */
@Service
@Transactional(readOnly=true)
public class SwmDictTypeService extends CrudService<SwmDictTypeDao, SwmDictType> {
	
	@Autowired
	private SwmDictDataDao swmDictDataDao;
	
	/**
	 * 获取单条数据
	 * @param swmDictType
	 * @return
	 */
	@Override
	public SwmDictType get(SwmDictType swmDictType) {
		SwmDictType entity = super.get(swmDictType);
		return entity;
	}
	
	/**
	 * 查询分页数据
	 * @param swmDictType 查询条件
	 * @return
	 */
	@Override
	public Page<SwmDictType> findPage(SwmDictType swmDictType) {
		return super.findPage(swmDictType);
	}
	
	/**
	 * 查询列表数据
	 * @param swmDictType
	 * @return
	 */
	@Override
	public List<SwmDictType> findList(SwmDictType swmDictType) {
		return super.findList(swmDictType);
	}
	
	/**
	 * 查询子表分页数据
	 * @param swmDictData
	 * @return
	 */
	public Page<SwmDictData> findSubPage(SwmDictData swmDictData) {
		Page<SwmDictData> page = swmDictData.getPage();
		page.setList(swmDictDataDao.findList(swmDictData));
		return page;
	}
	
	/**
	 * 保存数据（插入或更新）
	 * @param
	 */
	@Override
	@Transactional(readOnly=false)
	public void save(SwmDictType dictType) {
		super.save(dictType);
	}
	
	/**
	 * 更新状态
	 * @param swmDictType
	 */
	@Override
	@Transactional(readOnly=false)
	public void updateStatus(SwmDictType swmDictType) {
		super.updateStatus(swmDictType);
	}
	
	/**
	 * 删除数据
	 * @param swmDictType
	 */
	@Override
	@Transactional(readOnly=false)
	public void delete(SwmDictType swmDictType) {
		super.delete(swmDictType);
		SwmDictData swmDictData = new SwmDictData();
		swmDictDataDao.deleteByEntity(swmDictData);
	}
	
}