package com.jeesite.modules.swm.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jeesite.modules.swm.entity.SwmSiteMapManagement;
import com.jeesite.modules.swm.service.SwmSiteMapManagementService;

/**
 * 大屏数据看板Service
 * 
 * @author zwf
 * @version 2025-06-03
 */
@Service
@Transactional(readOnly = true)
public class SwmDashboardService {

    @Autowired
    private SwmSiteMapManagementService swmSiteMapManagementService;
    
    /**
     * 获取启用状态的地图路径
     * 
     * @return 地图文件路径
     */
    public String getActiveMapPath() {
        // 直接使用专门查询启用状态地图的方法
        SwmSiteMapManagement map = swmSiteMapManagementService.findActiveMap();
        
        if (map != null && map.getFilePath() != null) {
            return map.getFilePath();
        }
        
        return null;
    }
} 