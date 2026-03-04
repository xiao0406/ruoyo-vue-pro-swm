package com.jeesite.modules.swm.service;

import java.util.List;

import com.alibaba.cloud.commons.lang.StringUtils;
import com.jeesite.modules.config.TenantContext;
import com.jeesite.modules.fms.entity.FmsWorkGroup;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jeesite.common.entity.Page;
import com.jeesite.common.service.CrudService;
import com.jeesite.modules.swm.dao.FmsWorkGroupDao;

/**
 * 班组档案Service
 * @author wxy
 * @version 2026-03-02
 */
@Service
@Transactional(readOnly=true)
public class FmsWorkGroupService extends CrudService<FmsWorkGroupDao, FmsWorkGroup> {
	
	/**
	 * 获取单条数据
	 * @param fmsWorkGroup
	 * @return
	 */
	@Override
	public FmsWorkGroup get(FmsWorkGroup fmsWorkGroup) {
		return super.get(fmsWorkGroup);
	}
	
	/**
	 * 查询分页数据
	 * @param fmsWorkGroup 查询条件
	 * @return
	 */
	@Override
	public Page<FmsWorkGroup> findPage(FmsWorkGroup fmsWorkGroup) {
		String corpCode = TenantContext.get();
		fmsWorkGroup.setCorpCode(corpCode);
		return super.findPage(fmsWorkGroup);
	}
	
	/**
	 * 查询列表数据
	 * @param fmsWorkGroup
	 * @return
	 */
	@Override
	public List<FmsWorkGroup> findList(FmsWorkGroup fmsWorkGroup) {
		return super.findList(fmsWorkGroup);
	}
	
	/**
	 * 保存数据（插入或更新）
	 * @param fmsWorkGroup
	 */
	@Override
	@Transactional(readOnly=false)
	public void save(FmsWorkGroup fmsWorkGroup) {
		if (StringUtils.isBlank(fmsWorkGroup.getId())){
			String workGroupCode = fmsWorkGroup.getWorkGroupCode();
			FmsWorkGroup group =  this.dao.getEntityByCode(workGroupCode);
			if(group != null){
				throw new RuntimeException("班组编号已存在！");
			}
		}
		super.save(fmsWorkGroup);
	}
	
	/**
	 * 更新状态
	 * @param fmsWorkGroup
	 */
	@Override
	@Transactional(readOnly=false)
	public void updateStatus(FmsWorkGroup fmsWorkGroup) {
		super.updateStatus(fmsWorkGroup);
	}
	
	/**
	 * 删除数据
	 * @param fmsWorkGroup
	 */
	@Override
	@Transactional(readOnly=false)
	public void delete(FmsWorkGroup fmsWorkGroup) {
		super.delete(fmsWorkGroup);
	}
	
}