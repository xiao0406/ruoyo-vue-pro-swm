package com.jeesite.modules.swm.web;

import com.jeesite.common.web.BaseController;
import com.jeesite.modules.swm.service.SwmCommonOptionsService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 通用选项数据控制器
 *
 * @author zwf
 * @version 2025-06-21
 */
@RestController
@RequestMapping(value = "${adminPath}/common/options")
@Api(value = "通用选项接口", tags = "通用选项数据接口")
public class SwmCommonOptionsController extends BaseController {

    @Autowired
    private SwmCommonOptionsService swmCommonOptionsService;

    /**
     * 获取单位选项
     */
    @GetMapping("/companies")
    @ApiOperation("获取单位选项")
    public List<Map<String, Object>> getCompanyOptions() {
        return swmCommonOptionsService.getCompanyOptions();
    }

    /**
     * 获取车间选项
     */
    @GetMapping("/departments")
    @ApiOperation("获取车间选项")
    public List<Map<String, Object>> getDepartmentOptions() {
        return swmCommonOptionsService.getDepartmentOptions();
    }

    /**
     * 获取产线选项
     */
    @GetMapping("/prodLines")
    @ApiOperation("获取产线选项")
    public List<Map<String, Object>> getProdLineOptions() {
        return swmCommonOptionsService.getProdLineOptions();
    }

    /**
     * 获取班组选项
     */
    @GetMapping("/workGroups")
    @ApiOperation("获取班组选项")
    public List<Map<String, Object>> getWorkGroupOptions() {
        return swmCommonOptionsService.getWorkGroupOptions();
    }

    /**
     * 获取工种选项
     */
    @GetMapping("/workTypes")
    @ApiOperation("获取工种选项")
    public List<Map<String, Object>> getWorkTypeOptions() {
        return swmCommonOptionsService.getWorkTypeOptions();
    }
} 