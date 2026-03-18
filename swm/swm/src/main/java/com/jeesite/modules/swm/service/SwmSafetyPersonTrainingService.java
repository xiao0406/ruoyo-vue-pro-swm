package com.jeesite.modules.swm.service;

import java.util.Date;
import java.util.List;
import java.util.Set;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import com.jeesite.common.lang.DateUtils;
import com.jeesite.common.lang.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jeesite.common.entity.Page;
import com.jeesite.common.service.CrudService;
import com.jeesite.modules.entity.SwmSafetyPersonTraining;
import com.jeesite.modules.swm.dao.SwmSafetyPersonTrainingDao;

/**
 * 视频培训记录（人员）Service
 * @author wxy
 * @version 2026-01-19
 */
@Service
@Transactional(readOnly=true)
public class SwmSafetyPersonTrainingService extends CrudService<SwmSafetyPersonTrainingDao, SwmSafetyPersonTraining> {
	
	/**
	 * 获取单条数据
	 * @param swmSafetyPersonTraining
	 * @return
	 */
	@Override
	public SwmSafetyPersonTraining get(SwmSafetyPersonTraining swmSafetyPersonTraining) {
		return super.get(swmSafetyPersonTraining);
	}
	
	/**
	 * 查询分页数据
	 * @param swmSafetyPersonTraining 查询条件
	 * @return
	 */
	@Override
	public Page<SwmSafetyPersonTraining> findPage(SwmSafetyPersonTraining swmSafetyPersonTraining) {
		Date phshDate = swmSafetyPersonTraining.getPushDate();
		String pushDateStr = DateUtil.format(phshDate, DatePattern.NORM_DATE_PATTERN);
		swmSafetyPersonTraining.setPushDateStr(pushDateStr);
		return super.findPage(swmSafetyPersonTraining);
	}
	
	/**
	 * 查询列表数据
	 * @param swmSafetyPersonTraining
	 * @return
	 */
	@Override
	public List<SwmSafetyPersonTraining> findList(SwmSafetyPersonTraining swmSafetyPersonTraining) {
		return super.findList(swmSafetyPersonTraining);
	}
	
	/**
	 * 保存数据（插入或更新）
	 * @param swmSafetyPersonTraining
	 */
	@Override
	@Transactional(readOnly=false)
	public void save(SwmSafetyPersonTraining swmSafetyPersonTraining) {
		super.save(swmSafetyPersonTraining);
	}
	
	/**
	 * 更新状态
	 * @param swmSafetyPersonTraining
	 */
	@Override
	@Transactional(readOnly=false)
	public void updateStatus(SwmSafetyPersonTraining swmSafetyPersonTraining) {
		super.updateStatus(swmSafetyPersonTraining);
	}
	
	/**
	 * 删除数据
	 * @param swmSafetyPersonTraining
	 */
	@Override
	@Transactional(readOnly=false)
	public void delete(SwmSafetyPersonTraining swmSafetyPersonTraining) {
		super.delete(swmSafetyPersonTraining);
	}

	public Page<SwmSafetyPersonTraining> appPageList(SwmSafetyPersonTraining swmSafetyPersonTraining) {
		Page<SwmSafetyPersonTraining> page = swmSafetyPersonTraining.getPage();
		page.setPageNo(swmSafetyPersonTraining.getPageNo());
		page.setPageSize(swmSafetyPersonTraining.getPageSize());
		swmSafetyPersonTraining.getSqlMap().getWhere().disableAutoAddCorpCodeWhere();
		List<SwmSafetyPersonTraining> list = this.dao.appPageList(swmSafetyPersonTraining);
		page.setList(list);
		return page;
	}

	@Transactional(readOnly=false)
	public void appUpdate(SwmSafetyPersonTraining swmSafetyPersonTraining) {
		swmSafetyPersonTraining.getSqlMap().getWhere().disableAutoAddCorpCodeWhere();
		if (StringUtils.isNotBlank(swmSafetyPersonTraining.getCompleteStatus()) && "1".equals(swmSafetyPersonTraining.getCompleteStatus())){
			swmSafetyPersonTraining.setCompleteDate(new Date());
		}
		this.dao.appUpdate(swmSafetyPersonTraining);
	}

	public Set<String> findListByIdCard(List<String> identityCards, DateTime startMonth, DateTime endMonth) {
		return this.dao.findListByIdCard(identityCards, startMonth,endMonth);
	}
}