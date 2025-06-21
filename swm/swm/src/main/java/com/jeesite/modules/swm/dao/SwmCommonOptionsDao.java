package com.jeesite.modules.swm.dao;

import com.jeesite.common.dao.CrudDao;
import com.jeesite.common.mybatis.annotation.MyBatisDao;

import java.util.List;
import java.util.Map;

/**
 * 通用选项数据DAO接口
 * 
 * @author zwf
 * @version 2025-06-21
 */
@MyBatisDao
public interface SwmCommonOptionsDao {
    
    /**
     * 获取单位选项
     */
    List<Map<String, Object>> getCompanyOptions();
    
    /**
     * 获取车间选项
     */
    List<Map<String, Object>> getDepartmentOptions();
    
    /**
     * 获取产线选项
     */
    List<Map<String, Object>> getProdLineOptions();
    
    /**
     * 获取班组选项
     */
    List<Map<String, Object>> getWorkGroupOptions();
} 