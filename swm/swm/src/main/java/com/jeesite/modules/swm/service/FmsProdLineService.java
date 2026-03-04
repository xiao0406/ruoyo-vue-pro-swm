package com.jeesite.modules.swm.service;

import java.util.List;

import com.alibaba.cloud.commons.lang.StringUtils;
import com.jeesite.modules.config.TenantContext;
import com.jeesite.modules.fms.entity.FmsPositionArchive;
import com.jeesite.modules.fms.entity.FmsProdLine;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jeesite.common.entity.Page;
import com.jeesite.common.service.CrudService;
import com.jeesite.modules.swm.dao.FmsProdLineDao;

/**
 * 生产线档案Service
 * @author wxy
 * @version 2026-03-02
 */
@Service
@Transactional(readOnly=true)
public class FmsProdLineService extends CrudService<FmsProdLineDao, FmsProdLine> {

	
	/**
	 * 获取单条数据
	 * @param fmsProdLine
	 * @return
	 */
	@Override
	public FmsProdLine get(FmsProdLine fmsProdLine) {
		return super.get(fmsProdLine);
	}
	
	/**
	 * 查询分页数据
	 * @param fmsProdLine 查询条件
	 * @return
	 */
	@Override
	public Page<FmsProdLine> findPage(FmsProdLine fmsProdLine) {
		String corpCode = TenantContext.get();
		fmsProdLine.setCorpCode(corpCode);
		return super.findPage(fmsProdLine);
	}
	
	/**
	 * 查询列表数据
	 * @param fmsProdLine
	 * @return
	 */
	@Override
	public List<FmsProdLine> findList(FmsProdLine fmsProdLine) {
		return super.findList(fmsProdLine);
	}
	
	/**
	 * 保存数据（插入或更新）
	 * @param fmsProdLine
	 */
	@Override
	@Transactional(readOnly=false)
	public void save(FmsProdLine fmsProdLine) {
		if(StringUtils.isBlank(fmsProdLine.getId())){
			String prodLineCode = fmsProdLine.getProdLineCode();
			FmsProdLine line = dao.getByProdLineCode(prodLineCode);
			if(line != null){
				throw new RuntimeException("产线编号已存在！");
			}
		}
		super.save(fmsProdLine);
	}
	
	/**
	 * 更新状态
	 * @param fmsProdLine
	 */
	@Override
	@Transactional(readOnly=false)
	public void updateStatus(FmsProdLine fmsProdLine) {
		super.updateStatus(fmsProdLine);
	}
	
	/**
	 * 删除数据
	 * @param fmsProdLine
	 */
	@Override
	@Transactional(readOnly=false)
	public void delete(FmsProdLine fmsProdLine) {
		super.delete(fmsProdLine);
	}
	
}