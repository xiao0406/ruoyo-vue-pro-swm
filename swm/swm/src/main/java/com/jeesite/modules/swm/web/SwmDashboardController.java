package com.jeesite.modules.swm.web;

import com.jeesite.common.web.BaseController;
import com.jeesite.modules.swm.entity.SwmPerson;
import com.jeesite.modules.swm.entity.SwmWarningManagement;
import com.jeesite.modules.swm.service.SwmDashboardService;
import com.jeesite.modules.swm.service.SwmPersonService;
import com.jeesite.modules.swm.service.SwmWarningManagementService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
    @Autowired
    private SwmWarningManagementService swmWarningManagementService;
    @Autowired
    private SwmPersonService swmPersonService;

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

    /**
     * 近七日预警报警统计
     */
    @GetMapping(value = "warningStatisticsForPast7Days")
    @ResponseBody
    @ApiOperation("近七日预警报警统计")
    public Map<String, Object> warningStatisticsForPast7Days() {
        Map<String, Object> map = new HashMap<>();
        List<SwmWarningManagement> warnings = swmWarningManagementService.listPast7DaysWarning();
        map.put("record", warnings);
        Map<String, Long> warningMap = warnings.stream()
                .collect(Collectors.groupingBy(
                        SwmWarningManagement::getWarningContent,
                        Collectors.counting()
                ));
        map.put("warning", warningMap);
        return map;
    }

    /**
     * 今日预警统计
     */
    @GetMapping(value = "warningStatisticsForToday")
    @ResponseBody
    @ApiOperation("今日预警统计")
    public Map<String, Object> warningStatisticsForToday() {
        Map<String, Object> map = new HashMap<>();
        List<SwmWarningManagement> warnings = swmWarningManagementService.listTodayWarning();
        map.put("record", warnings);
        Map<String, Long> warningMap = warnings.stream()
                .collect(Collectors.groupingBy(
                        SwmWarningManagement::getWarningContent,
                        Collectors.counting()
                ));
        map.put("warning", warningMap);
        return map;
    }

    /**
     * 告警总数
     */
    @GetMapping("/warning/count")
    @ResponseBody
    @ApiOperation("报警预警总数")
    public Map<String, Object> warningCount() {
        Map<String, Object> map = new HashMap<>();
        SwmPerson swmPerson = new SwmPerson();
        swmPerson.setStatus("0");
        swmPerson.setPersonnelStatus(SwmPerson.PersonStatusEnum.ACTIVE);
        map.put("personCount", swmPersonService.findCount(swmPerson));
        SwmWarningManagement alarmQuery = new SwmWarningManagement();
        alarmQuery.setStatus("0");
        alarmQuery.setAlarmTimeIsNotNull(true);
        map.put("alarmCount", swmWarningManagementService.findCount(alarmQuery));
        SwmWarningManagement warningQuery = new SwmWarningManagement();
        warningQuery.setStatus("0");
        warningQuery.setWarningTimeIsNotNull(true);
        map.put("warningCount", swmWarningManagementService.findCount(warningQuery));
        return map;
    }

}
