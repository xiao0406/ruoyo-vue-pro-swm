package com.jeesite.modules.swm.service;

import java.util.Date;
import java.util.List;

import cn.hutool.core.date.DateTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jeesite.common.entity.Page;
import com.jeesite.common.service.CrudService;
import com.jeesite.modules.swm.entity.SwmDify;
import com.jeesite.modules.swm.dao.SwmDifyDao;

/**
 * 安全帽：ai日报表Service
 * @author wxy
 * @version 2025-11-27
 */
@Service
@Transactional(readOnly=true)
public class SwmDifyService extends CrudService<SwmDifyDao, SwmDify> {
	
	/**
	 * 获取单条数据
	 * @param swmDify
	 * @return
	 */
	@Override
	public SwmDify get(SwmDify swmDify) {
		return super.get(swmDify);
	}
	
	/**
	 * 查询分页数据
	 * @param swmDify 查询条件
	 * @return
	 */
	@Override
	public Page<SwmDify> findPage(SwmDify swmDify) {
		return super.findPage(swmDify);
	}
	
	/**
	 * 查询列表数据
	 * @param swmDify
	 * @return
	 */
	@Override
	public List<SwmDify> findList(SwmDify swmDify) {
		return super.findList(swmDify);
	}
	
	/**
	 * 保存数据（插入或更新）
	 * @param swmDify
	 */
	@Override
	@Transactional(readOnly=false)
	public void save(SwmDify swmDify) {
		super.save(swmDify);
	}
	
	/**
	 * 更新状态
	 * @param swmDify
	 */
	@Override
	@Transactional(readOnly=false)
	public void updateStatus(SwmDify swmDify) {
		super.updateStatus(swmDify);
	}
	
	/**
	 * 删除数据
	 * @param swmDify
	 */
	@Override
	@Transactional(readOnly=false)
	public void delete(SwmDify swmDify) {
		super.delete(swmDify);
	}


	public SwmDify getEntity(DateTime startYesterday, DateTime endYesterDay) {
		return this.dao.getEntity(startYesterday, endYesterDay);
	}
}