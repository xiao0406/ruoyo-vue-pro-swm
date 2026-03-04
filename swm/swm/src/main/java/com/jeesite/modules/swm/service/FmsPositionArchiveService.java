package com.jeesite.modules.swm.service;

import java.util.List;

import com.jeesite.modules.config.TenantContext;
import com.jeesite.modules.fms.entity.FmsPositionArchive;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jeesite.common.entity.Page;
import com.jeesite.common.service.CrudService;
import com.jeesite.modules.swm.dao.FmsPositionArchiveDao;

/**
 * 位置档案定义Service
 * @author wxy
 * @version 2026-03-02
 */
@Service
@Transactional(readOnly=true)
public class FmsPositionArchiveService extends CrudService<FmsPositionArchiveDao, FmsPositionArchive> {
	
	/**
	 * 获取单条数据
	 * @param fmsPositionArchive
	 * @return
	 */
	@Override
	public FmsPositionArchive get(FmsPositionArchive fmsPositionArchive) {
		return super.get(fmsPositionArchive);
	}
	
	/**
	 * 查询分页数据
	 * @param fmsPositionArchive 查询条件
	 * @return
	 */
	@Override
	public Page<FmsPositionArchive> findPage(FmsPositionArchive fmsPositionArchive) {
		fmsPositionArchive.setType(FmsPositionArchive.CJ);
		String corpCode = TenantContext.get();
		fmsPositionArchive.setCorpCode(corpCode);
		return super.findPage(fmsPositionArchive);
	}
	
	/**
	 * 查询列表数据
	 * @param fmsPositionArchive
	 * @return
	 */
	@Override
	public List<FmsPositionArchive> findList(FmsPositionArchive fmsPositionArchive) {
		return super.findList(fmsPositionArchive);
	}
	
	/**
	 * 保存数据（插入或更新）
	 * @param fmsPositionArchive
	 */
	@Override
	@Transactional(readOnly=false)
	public void save(FmsPositionArchive fmsPositionArchive) {
		fmsPositionArchive.setType(FmsPositionArchive.CJ);
		super.save(fmsPositionArchive);
	}
	
	/**
	 * 更新状态
	 * @param fmsPositionArchive
	 */
	@Override
	@Transactional(readOnly=false)
	public void updateStatus(FmsPositionArchive fmsPositionArchive) {
		super.updateStatus(fmsPositionArchive);
	}
	
	/**
	 * 删除数据
	 * @param fmsPositionArchive
	 */
	@Override
	@Transactional(readOnly=false)
	public void delete(FmsPositionArchive fmsPositionArchive) {
		super.delete(fmsPositionArchive);
	}
	
}