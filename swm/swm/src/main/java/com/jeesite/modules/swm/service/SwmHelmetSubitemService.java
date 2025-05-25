package com.jeesite.modules.swm.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jeesite.common.entity.Page;
import com.jeesite.common.service.CrudService;
import com.jeesite.modules.swm.dao.SwmHelmetSubitemDao;
import com.jeesite.modules.swm.entity.SwmHelmetSubitem;

/**
 * 安全帽子项颜色表Service
 * 
 * @author zwf
 * @version 2025-05-20
 */
@Service
@Transactional(readOnly = true)
public class SwmHelmetSubitemService extends CrudService<SwmHelmetSubitemDao, SwmHelmetSubitem> {
	
	@Autowired
	private SwmHelmetConfigService swmHelmetConfigService;
	
	/**
	 * 获取单条数据
	 * @param swmHelmetSubitem 查询条件
	 * @return 安全帽子项信息
	 */
	@Override
	public SwmHelmetSubitem get(SwmHelmetSubitem swmHelmetSubitem) {
		return super.get(swmHelmetSubitem);
	}
	
	/**
	 * 查询分页数据
	 * @param swmHelmetSubitem 查询条件
	 * @return 分页数据
	 */
	public Page<SwmHelmetSubitem> findPage(SwmHelmetSubitem swmHelmetSubitem) {
		return super.findPage(swmHelmetSubitem);
	}
	
	/**
	 * 查询分页数据
	 * @param page 分页对象
	 * @param swmHelmetSubitem 查询条件
	 * @return 分页数据
	 */
	public Page<SwmHelmetSubitem> findPage(Page<SwmHelmetSubitem> page, SwmHelmetSubitem swmHelmetSubitem) {
		return this.findPage(swmHelmetSubitem);
	}
	
	/**
	 * 查询列表数据
	 * @param swmHelmetSubitem 查询条件
	 * @return 列表数据
	 */
	@Override
	public List<SwmHelmetSubitem> findList(SwmHelmetSubitem swmHelmetSubitem) {
		return super.findList(swmHelmetSubitem);
	}
	
	/**
	 * 根据父ID查询子项列表
	 * @param parentId 父ID
	 * @return 子项列表
	 */
	public List<SwmHelmetSubitem> findByParentId(String parentId) {
		SwmHelmetSubitem entity = new SwmHelmetSubitem();
		entity.setParentId(parentId);
		entity.getSqlMap().getWhere().disableAutoAddStatusWhere(); // 禁用自动添加状态条件
		return dao.findByParentId(parentId);
	}
	
	/**
	 * 保存数据（插入或更新）
	 * @param swmHelmetSubitem 实体对象
	 */
	@Override
	@Transactional(readOnly = false)
	public void save(SwmHelmetSubitem swmHelmetSubitem) {
		super.save(swmHelmetSubitem);
		// 更新父表子项数量
		swmHelmetConfigService.updateSubitemCount(swmHelmetSubitem.getParentId());
	}
	
	/**
	 * 删除数据
	 * @param swmHelmetSubitem 实体对象
	 */
	@Override
	@Transactional(readOnly = false)
	public void delete(SwmHelmetSubitem swmHelmetSubitem) {
		super.delete(swmHelmetSubitem);
		// 更新父表子项数量
		swmHelmetConfigService.updateSubitemCount(swmHelmetSubitem.getParentId());
	}
	
	/**
	 * 批量删除数据
	 * @param parentId 父ID
	 */
	@Transactional(readOnly = false)
	public void deleteByParentId(String parentId) {
		dao.deleteByParentId(parentId);
	}
} 