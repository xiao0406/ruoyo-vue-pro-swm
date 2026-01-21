package com.jeesite.modules.swm.service;

import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import com.alibaba.cloud.commons.lang.StringUtils;
import com.jeesite.common.lang.ObjectUtils;
import com.jeesite.common.mybatis.mapper.query.QueryType;
import com.jeesite.modules.entity.SwmSafetyPersonTraining;
import com.jeesite.modules.job.task.SafetyManageTask;
import com.jeesite.modules.swm.dao.SwmSafetyPersonTrainingDao;
import com.jeesite.modules.swm.entity.SwmPerson;
import com.jeesite.modules.utils.BatchOperationsUtil;
import jdk.nashorn.internal.ir.annotations.Reference;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jeesite.common.entity.Page;
import com.jeesite.common.service.CrudService;
import com.jeesite.modules.entity.SwmSafetyFileManage;
import com.jeesite.modules.swm.dao.SwmSafetyFileManageDao;

import javax.annotation.Resource;

/**
 * 安全教育视频管理Service
 * @author wxy
 * @version 2026-01-19
 */
@Service
@Transactional(readOnly=true)
public class SwmSafetyFileManageService extends CrudService<SwmSafetyFileManageDao, SwmSafetyFileManage> {

	@Resource
	private SwmPersonService swmPersonService;
	@Resource
	private SwmSafetyPersonTrainingDao swmSafetyPersonTrainingDao;
	
	/**
	 * 获取单条数据
	 * @param swmSafetyFileManage
	 * @return
	 */
	@Override
	public SwmSafetyFileManage get(SwmSafetyFileManage swmSafetyFileManage) {
		return super.get(swmSafetyFileManage);
	}
	
	/**
	 * 查询分页数据
	 * @param swmSafetyFileManage 查询条件
	 * @return
	 */
	@Override
	public Page<SwmSafetyFileManage> findPage(SwmSafetyFileManage swmSafetyFileManage) {
		if (ObjectUtils.isNotEmpty(swmSafetyFileManage.getPushDate())){
			String format = DateUtil.format(swmSafetyFileManage.getPushDate(), DatePattern.NORM_DATE_PATTERN);
			swmSafetyFileManage.getSqlMap().getWhere().and("push_date", QueryType.EQ,format);
			swmSafetyFileManage.setPushDate(null);
		}
		return super.findPage(swmSafetyFileManage);
	}
	
	/**
	 * 查询列表数据
	 * @param swmSafetyFileManage
	 * @return
	 */
	@Override
	public List<SwmSafetyFileManage> findList(SwmSafetyFileManage swmSafetyFileManage) {
		return super.findList(swmSafetyFileManage);
	}
	
	/**
	 * 保存数据（插入或更新）
	 * @param swmSafetyFileManage
	 */
	@Override
	@Transactional(readOnly=false)
	public void save(SwmSafetyFileManage swmSafetyFileManage) {
		//默认推送状态未推送
		if (StringUtils.isBlank(swmSafetyFileManage.getPushStatus())){
			swmSafetyFileManage.setPushStatus("0");
		}
		//判断推送日期是否为今天，是则立即推送
		Date pushDate = swmSafetyFileManage.getPushDate();
		String pushDateStr = DateUtil.format(pushDate, DatePattern.NORM_DATE_PATTERN);
		String nowDateStr = DateUtil.format(new Date(), DatePattern.NORM_DATE_PATTERN);
		if (pushDateStr.equals(nowDateStr) ){
			this.pushDate(swmSafetyFileManage);
		}

		super.save(swmSafetyFileManage);
	}
	
	/**
	 * 更新状态
	 * @param swmSafetyFileManage
	 */
	@Override
	@Transactional(readOnly=false)
	public void updateStatus(SwmSafetyFileManage swmSafetyFileManage) {
		super.updateStatus(swmSafetyFileManage);
	}
	
	/**
	 * 删除数据
	 * @param swmSafetyFileManage
	 */
	@Override
	@Transactional(readOnly=false)
	public void delete(SwmSafetyFileManage swmSafetyFileManage) {
		super.delete(swmSafetyFileManage);
	}


	/**
	 * 推送视频
	 * @param fileManage
	 */
	private void pushDate(SwmSafetyFileManage fileManage) {
		//视频培训记录（安全管理）信息
		List<SwmSafetyPersonTraining> insertTrainingList = new ArrayList<>();
		String jobType = fileManage.getJobType();
		//切割工种：”，“
		String[] jobTypes = jobType.split(",");

		// 2.根据工种去人员表查出对应的人员
		List<String> jobtypeList = new ArrayList<>(Arrays.asList(jobTypes));
		List<SwmPerson> jobPersons = swmPersonService.findListByJobTypeList(jobtypeList);

		//查出视频培训记录（安全管理）信息，看看是否存在，存在则跳过
		SwmSafetyPersonTraining safetyPersonTraining = new SwmSafetyPersonTraining();
		safetyPersonTraining.setSafetyManageId(fileManage.getId());
		List<SwmSafetyPersonTraining> list = swmSafetyPersonTrainingDao.findList(safetyPersonTraining);
		Map<String, List<SwmSafetyPersonTraining>> traningMap = list.stream().collect(Collectors.groupingBy(SwmSafetyPersonTraining::getPhoneNumber));

		//3.生成视频培训记录（人员）信息
		for (SwmPerson person : jobPersons) {
			// 3.1生成之前需要先查一下这一条视频是否生成过，生成过就不能生成
			SwmSafetyPersonTraining training = new SwmSafetyPersonTraining();
			training.setSafetyManageId(fileManage.getId());
			training.setIdentityCard(person.getIdentityCard());
			List<SwmSafetyPersonTraining> swmSafetyPersonTrainings = traningMap.get(person.getPhoneNumber());
			if(CollectionUtils.isNotEmpty(swmSafetyPersonTrainings)){
				continue;
			}
			//3.2 表里没生成过则开始生成数据
			training.setPhoneNumber(person.getPhoneNumber());
			training.setCompleteStatus("0");
			training.setProgress("0");
			insertTrainingList.add(training);
		}
		//4.修改安全管理表状态，未推送改为已推送
		fileManage.setPushStatus("1");
		this.update(fileManage);

		List<List<SwmSafetyPersonTraining>> lists = BatchOperationsUtil.batchCutting(insertTrainingList, 100);
		for (List<SwmSafetyPersonTraining> list1 : lists) {
			swmSafetyPersonTrainingDao.insertBatch(list1);
		}
	}
	
}