package com.jeesite.modules.swm.dao;

import com.jeesite.common.dao.CrudDao;
import com.jeesite.common.mybatis.annotation.MyBatisDao;
import com.jeesite.modules.swm.entity.SwmSafetyEducation;
import org.apache.ibatis.annotations.Param;

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
    
    /**
     * 自定义条件查询安全教育记录
     * @param theme 主题（模糊查询）
     * @param safetyEducationType 安全教育类型
     * @param participationType 参与类型
     * @param safetyStatus 状态
     * @return 符合条件的记录列表
     */
    List<SwmSafetyEducation> findByCustomConditions(
        @Param("theme") String theme,
        @Param("safetyEducationType") String safetyEducationType,
        @Param("participationType") String participationType,
        @Param("safetyStatus") String safetyStatus
    );
} 