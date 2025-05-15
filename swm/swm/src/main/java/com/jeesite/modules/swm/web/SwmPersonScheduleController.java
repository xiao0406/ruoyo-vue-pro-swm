package com.jeesite.modules.swm.web;

import com.jeesite.common.config.Global;
import com.jeesite.common.entity.Page;
import com.jeesite.common.web.BaseController;
import com.jeesite.modules.swm.entity.SwmPersonSchedule;
import com.jeesite.modules.swm.entity.SwmScheduleTime;
import com.jeesite.modules.swm.service.SwmPersonScheduleService;
import com.jeesite.modules.swm.service.SwmScheduleTimeService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 人员排班Controller
 * 
 * @author zwf
 * @version 2025-05-15
 */
@Controller
    @RequestMapping(value = "${adminPath}/personSchedule")
@Api(value = "人员排班管理接口", tags = "人员排班管理接口")
public class SwmPersonScheduleController extends BaseController {

    @Autowired
    private SwmPersonScheduleService swmPersonScheduleService;
    
    @Autowired
    private SwmScheduleTimeService swmScheduleTimeService;
    
    /**
     * 获取数据
     */
    @ModelAttribute
    public SwmPersonSchedule get(String id, boolean isNewRecord) {
        return swmPersonScheduleService.get(id, isNewRecord);
    }
    
    /**
     * 查询列表
     */
    @RequestMapping(value = {"list", ""})
    @ApiOperation("查询列表")
    public String list(SwmPersonSchedule swmPersonSchedule, Model model) {
        model.addAttribute("swmPersonSchedule", swmPersonSchedule);
        return "modules/swm/personScheduleList";
    }
    
    /**
     * 查询列表数据
     */
    @RequestMapping(value = "listData")
    @ResponseBody
    @ApiOperation("查询列表数据")
    public Map<String, Object> listData(SwmPersonSchedule swmPersonSchedule, HttpServletRequest request, HttpServletResponse response) {
        Page<SwmPersonSchedule> page = swmPersonScheduleService.findPage(new Page<>(request, response), swmPersonSchedule);
        
        // 构建包含额外字段的响应数据
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> enhancedList = new ArrayList<>();
        
        // 处理每个对象，添加枚举的文本显示
        for (SwmPersonSchedule schedule : page.getList()) {
            Map<String, Object> scheduleMap = new HashMap<>();
            
            // 复制基本属性
            scheduleMap.put("id", schedule.getId());
            scheduleMap.put("createBy", schedule.getCreateBy());
            scheduleMap.put("createDate", schedule.getCreateDate());
            scheduleMap.put("updateBy", schedule.getUpdateBy());
            scheduleMap.put("updateDate", schedule.getUpdateDate());
            scheduleMap.put("remarks", schedule.getRemarks());
            scheduleMap.put("status", schedule.getStatus());
            
            // 复制业务属性
            scheduleMap.put("personName", schedule.getPersonName());
            scheduleMap.put("month", schedule.getMonth());
            scheduleMap.put("classes", schedule.getClasses());
            scheduleMap.put("isNewRecord", schedule.getIsNewRecord());
            
            // 添加枚举文本显示值
            scheduleMap.put("classesText", schedule.getClassesText());
            
            // 添加到列表
            enhancedList.add(scheduleMap);
        }
        
        // 构建分页结果
        result.put("list", enhancedList);
        result.put("count", page.getCount());
        result.put("pageNo", page.getPageNo());
        result.put("pageSize", page.getPageSize());
        
        return result;
    }
    
    /**
     * 查看编辑表单
     */
    @RequestMapping(value = "form")
    @ResponseBody
    @ApiOperation("查看编辑表单")
    public Map<String, Object> form(SwmPersonSchedule swmPersonSchedule) {
        Map<String, Object> result = new HashMap<>();
        if (swmPersonSchedule != null) {
            Map<String, Object> scheduleData = new HashMap<>();
            
            // 复制基本属性
            scheduleData.put("id", swmPersonSchedule.getId());
            scheduleData.put("personName", swmPersonSchedule.getPersonName());
            scheduleData.put("month", swmPersonSchedule.getMonth());
            scheduleData.put("classes", swmPersonSchedule.getClasses());
            scheduleData.put("remarks", swmPersonSchedule.getRemarks());
            
            // 处理枚举值
            scheduleData.put("classesText", swmPersonSchedule.getClassesText());
            
            result.putAll(scheduleData);
        }
        return result;
    }
    
    /**
     * 保存数据
     */
    @PostMapping(value = "save")
    @ResponseBody
    @ApiOperation("保存数据")
    public String save(@Validated SwmPersonSchedule swmPersonSchedule) {
        swmPersonScheduleService.save(swmPersonSchedule);
        return renderResult(Global.TRUE, text("保存人员排班成功！"));
    }
    
    /**
     * 批量保存数据
     */
    @PostMapping(value = "batchSave")
    @ResponseBody
    @ApiOperation("批量保存数据")
    public String batchSave(@RequestBody List<SwmPersonSchedule> scheduleList) {
        swmPersonScheduleService.batchSave(scheduleList);
        return renderResult(Global.TRUE, text("批量保存人员排班成功！"));
    }
    
    /**
     * 删除数据
     */
    @PostMapping(value = "delete")
    @ResponseBody
    @ApiOperation("删除数据")
    public String delete(SwmPersonSchedule swmPersonSchedule) {
        swmPersonScheduleService.delete(swmPersonSchedule);
        return renderResult(Global.TRUE, text("删除人员排班成功！"));
    }
    
    /**
     * 批量删除数据
     */
    @PostMapping(value = "deleteAll")
    @ResponseBody
    @ApiOperation("批量删除数据")
    public String deleteAll(String ids) {
        String[] idArray = ids.split(",");
        for (String id : idArray) {
            SwmPersonSchedule swmPersonSchedule = swmPersonScheduleService.get(id);
            if (swmPersonSchedule != null) {
                swmPersonScheduleService.delete(swmPersonSchedule);
            }
        }
        return renderResult(Global.TRUE, text("批量删除人员排班成功！"));
    }
    
    /**
     * 获取班次类型选项
     */
    @GetMapping(value = "getShiftOptions")
    @ResponseBody
    @ApiOperation("获取班次类型选项")
    public Map<String, Object> getShiftOptions() {
        Map<String, Object> result = new HashMap<>();
        
        // 查询所有班次时间设置
        SwmScheduleTime query = new SwmScheduleTime();
        List<SwmScheduleTime> scheduleTimeList = swmScheduleTimeService.findList(query);
        
        // 转换为选项格式
        Map<String, Map<String, String>> shiftOptions = new HashMap<>();
        for (SwmScheduleTime scheduleTime : scheduleTimeList) {
            Map<String, String> option = new HashMap<>();
            option.put("startTime", scheduleTime.getStartTime());
            option.put("endTime", scheduleTime.getEndTime());
            option.put("text", scheduleTime.getShiftTypeText());
            
            shiftOptions.put(scheduleTime.getShiftType(), option);
        }
        
        result.put("shiftOptions", shiftOptions);
        
        return result;
    }
} 