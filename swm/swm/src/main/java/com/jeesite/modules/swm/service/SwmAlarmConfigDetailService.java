package com.jeesite.modules.swm.service;

import java.util.List;

import com.jeesite.common.lang.ObjectUtils;
import com.jeesite.common.service.ServiceException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;

import com.jeesite.common.entity.Page;
import com.jeesite.common.service.CrudService;
import com.jeesite.modules.swm.entity.SwmAlarmConfigDetail;
import com.jeesite.modules.swm.dao.SwmAlarmConfigDetailDao;

import io.seata.spring.annotation.GlobalTransactional;

/**
 * swm_alarm_config_detailService
 * @author gjl
 * @version 2025-08-11
 */
@Service
@RestController
@Transactional(readOnly=true)
public class SwmAlarmConfigDetailService extends CrudService<SwmAlarmConfigDetailDao, SwmAlarmConfigDetail> {
	
	/**
	 * 获取单条数据
	 * @param swmAlarmConfigDetail
	 * @return
	 */
	@Override
	public SwmAlarmConfigDetail get(SwmAlarmConfigDetail swmAlarmConfigDetail) {
		return super.get(swmAlarmConfigDetail);
	}
	
	/**
	 * 查询分页数据
	 * @param swmAlarmConfigDetail 查询条件
	 * @return
	 */
	@Override
	public Page<SwmAlarmConfigDetail> findPage(SwmAlarmConfigDetail swmAlarmConfigDetail) {
		return super.findPage(swmAlarmConfigDetail);
	}
	
	/**
	 * 查询列表数据
	 * @param swmAlarmConfigDetail
	 * @return
	 */
	@Override
	public List<SwmAlarmConfigDetail> findList(SwmAlarmConfigDetail swmAlarmConfigDetail) {
		return super.findList(swmAlarmConfigDetail);
	}
	
	/**
	 * 保存数据（插入或更新）
	 * @param swmAlarmConfigDetail
	 */
	@Override
	@GlobalTransactional
	@Transactional(readOnly=false)
	public void save(SwmAlarmConfigDetail swmAlarmConfigDetail) {
		if (ObjectUtils.isEmpty(swmAlarmConfigDetail)||ObjectUtils.isEmpty(swmAlarmConfigDetail.getMainId())){
			throw new ServiceException("请选择主配置！");
		}
		if (ObjectUtils.isEmpty(swmAlarmConfigDetail.getRoles())&&ObjectUtils.isEmpty(swmAlarmConfigDetail.getUsers())){
			throw new ServiceException("请配置发送人员人员！");
		}
		//判断是否存在相同配置
		SwmAlarmConfigDetail detail = this.dao.findByMainId(swmAlarmConfigDetail);
		if (ObjectUtils.isNotEmpty(detail)){
			throw new ServiceException("请勿重复配置！");
		}
		super.save(swmAlarmConfigDetail);
	}
	
	/**
	 * 更新状态
	 * @param swmAlarmConfigDetail
	 */
	@Override
	@GlobalTransactional
	@Transactional(readOnly=false)
	public void updateStatus(SwmAlarmConfigDetail swmAlarmConfigDetail) {
		super.updateStatus(swmAlarmConfigDetail);
	}
	
	/**
	 * 删除数据
	 * @param swmAlarmConfigDetail
	 */
	@Override
	@GlobalTransactional
	@Transactional(readOnly=false)
	public void delete(SwmAlarmConfigDetail swmAlarmConfigDetail) {
		super.delete(swmAlarmConfigDetail);
	}

	/**
	 * 根据主单id查询明细
	 * @param swmAlarmConfigDetail
	 * @return
	 */
	public SwmAlarmConfigDetail findByMainId(SwmAlarmConfigDetail swmAlarmConfigDetail) {
		if (ObjectUtils.isEmpty(swmAlarmConfigDetail)||ObjectUtils.isEmpty(swmAlarmConfigDetail.getMainId())){
			throw new ServiceException("请选择主配置！");
		}
		return this.dao.getByEntity(swmAlarmConfigDetail);
	}
}