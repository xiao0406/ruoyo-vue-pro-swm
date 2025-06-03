package com.jeesite.modules.swm.web;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.jeesite.common.web.BaseController;
import com.jeesite.modules.swm.service.SwmDashboardService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * 大屏数据看板Controller
 * 
 * @author zwf
 * @version 2025-06-03
 */
@Controller
@RequestMapping(value = "${adminPath}/dashboard")
@Api(value = "大屏数据看板接口", tags = "大屏数据看板接口")
public class SwmDashboardController extends BaseController {

    @Autowired
    private SwmDashboardService swmDashboardService;
    
    /**
     * 获取启用状态的地图路径
     */
    @GetMapping(value = "getActiveMapPath")
    @ResponseBody
    @ApiOperation("获取启用状态的地图路径")
    public Map<String, Object> getActiveMapPath() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 调用服务层方法获取地图路径
            String filePath = swmDashboardService.getActiveMapPath();
            
            if (filePath != null && !filePath.isEmpty()) {
                result.put("success", true);
                result.put("filePath", filePath);
            } else {
                result.put("success", false);
                result.put("message", "未找到启用状态的地图");
            }
        } catch (Exception e) {
            logger.error("获取地图路径失败", e);
            result.put("success", false);
            result.put("message", "获取地图路径失败: " + e.getMessage());
        }
        
        return result;
    }
} 