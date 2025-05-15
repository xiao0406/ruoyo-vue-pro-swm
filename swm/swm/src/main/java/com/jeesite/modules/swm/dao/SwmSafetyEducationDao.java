package com.jeesite.modules.swm.dao;

import com.jeesite.common.dao.CrudDao;
import com.jeesite.common.mybatis.annotation.MyBatisDao;
import com.jeesite.modules.swm.entity.SwmSafetyEducation;

import java.util.List;

/**
 * 安全教育DAO接口
 * @author zwf
 * @version 2025-05-14
 */
@MyBatisDao
public interface SwmSafetyEducationDao extends CrudDao<SwmSafetyEducation> {
    
    /**
     * 查询所有记录，不带默认的状态过滤
     * @return 所有记录列表
     */
    List<SwmSafetyEducation> findAllWithoutStatusFilter();
    
} 