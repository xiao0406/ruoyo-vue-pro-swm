package com.jeesite.modules.swm.service;

import com.jeesite.modules.swm.dao.SwmCommonOptionsDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * 通用选项数据Service
 * 
 * @author zwf
 * @version 2025-06-21
 */
@Service
@Transactional(readOnly = true)
public class SwmCommonOptionsService {
    
    @Autowired
    private SwmCommonOptionsDao swmCommonOptionsDao;
    
    /**
     * 获取单位选项
     */
    public List<Map<String, Object>> getCompanyOptions() {
        return swmCommonOptionsDao.getCompanyOptions();
    }
    
    /**
     * 获取车间选项
     */
    public List<Map<String, Object>> getDepartmentOptions() {
        return swmCommonOptionsDao.getDepartmentOptions();
    }
    
    /**
     * 获取产线选项
     */
    public List<Map<String, Object>> getProdLineOptions() {
        return swmCommonOptionsDao.getProdLineOptions();
    }
    
    /**
     * 获取班组选项
     */
    public List<Map<String, Object>> getWorkGroupOptions() {
        return swmCommonOptionsDao.getWorkGroupOptions();
    }
} 