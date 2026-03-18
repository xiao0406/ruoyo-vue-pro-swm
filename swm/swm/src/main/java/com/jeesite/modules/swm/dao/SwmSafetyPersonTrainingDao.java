package com.jeesite.modules.swm.dao;

import cn.hutool.core.date.DateTime;
import com.jeesite.common.dao.CrudDao;
import com.jeesite.common.mybatis.annotation.MyBatisDao;
import com.jeesite.modules.entity.SwmSafetyPersonTraining;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * 视频培训记录（人员）DAO接口
 * @author wxy
 * @version 2026-01-19
 */
@MyBatisDao
public interface SwmSafetyPersonTrainingDao extends CrudDao<SwmSafetyPersonTraining> {

    List<SwmSafetyPersonTraining> appPageList(SwmSafetyPersonTraining swmSafetyPersonTraining);

    void appUpdate(SwmSafetyPersonTraining swmSafetyPersonTraining);

    Set<String> findListByIdCard(@Param("identityCards") List<String> identityCards, @Param("startMonth") DateTime startMonth, @Param("endMonth") DateTime endMonth);
}