package com.jeesite.modules.swm.dao;

import com.jeesite.common.dao.CrudDao;
import com.jeesite.common.mybatis.annotation.MyBatisDao;
import com.jeesite.modules.entity.SwmSafetyPersonTraining;

import java.util.List;

/**
 * 视频培训记录（人员）DAO接口
 * @author wxy
 * @version 2026-01-19
 */
@MyBatisDao
public interface SwmSafetyPersonTrainingDao extends CrudDao<SwmSafetyPersonTraining> {

    List<SwmSafetyPersonTraining> appPageList(SwmSafetyPersonTraining swmSafetyPersonTraining);

    void appUpdate(SwmSafetyPersonTraining swmSafetyPersonTraining);
}