package com.jeesite.modules.swm.web;

import com.jeesite.common.config.Global;
import com.jeesite.common.entity.Page;
import com.jeesite.common.lang.DateUtils;
import com.jeesite.common.web.BaseController;
import com.jeesite.modules.swm.entity.SwmWarningManagement;
import com.jeesite.modules.swm.service.SwmWarningManagementService;
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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 预警管理Controller
 * @author auto create
 * @version 2025-05-16
 */
@Controller
@RequestMapping(value = "${adminPath}/warningManagement")
@Api(value = "预警管理接口", tags = "预警管理接口")
public class SwmWarningManagementController extends BaseController {

    @Autowired
    private SwmWarningManagementService swmWarningManagementService;
    
    /**
     * 获取数据
     */
    @ModelAttribute
    public SwmWarningManagement get(String id, boolean isNewRecord) {
        return swmWarningManagementService.get(id, isNewRecord);
    }
    
    /**
     * 查询列表
     */
    @RequestMapping(value = {"list", ""})
    @ApiOperation("查询列表")
    public String list(SwmWarningManagement swmWarningManagement, Model model) {
        model.addAttribute("swmWarningManagement", swmWarningManagement);
        return "modules/swm/swmWarningManagementList";
    }
    
    /**
     * 查询列表数据
     */
    @RequestMapping(value = "listData")
    @ResponseBody
    @ApiOperation("查询列表数据")
    public Page<SwmWarningManagement> listData(SwmWarningManagement swmWarningManagement, HttpServletRequest request, HttpServletResponse response) {
        // 创建分页对象
        Page<SwmWarningManagement> page = new Page<>(request, response);
        
        // 调用服务层方法，获取带文本值的分页数据
        return swmWarningManagementService.findPageWithTextValues(page, swmWarningManagement);
    }
    
    /**
     * 查看编辑表单
     */
    @RequestMapping(value = "form")
    @ResponseBody
    @ApiOperation("查看编辑表单")
    public Map<String, Object> form(SwmWarningManagement swmWarningManagement) {
        Map<String, Object> result = new HashMap<>();
        if (swmWarningManagement != null) {
            Map<String, Object> warningData = new HashMap<>();
            // 复制基本属性
            warningData.put("id", swmWarningManagement.getId());
            warningData.put("personName", swmWarningManagement.getPersonName());
            warningData.put("warningTime", swmWarningManagement.getWarningTime());
            warningData.put("alarmRecord", swmWarningManagement.getAlarmRecord());
            warningData.put("alarmTime", swmWarningManagement.getAlarmTime());
            warningData.put("triggerReason", swmWarningManagement.getTriggerReason());
            warningData.put("handler", swmWarningManagement.getHandler());
            warningData.put("handleTime", swmWarningManagement.getHandleTime());
            warningData.put("handleProcess", swmWarningManagement.getHandleProcess());
            
            // 将handleStatus直接转换为文本值返回
            String handleStatusValue = swmWarningManagement.getHandleStatus();
            warningData.put("handleStatus", SwmWarningManagement.HandleStatusEnum.getText(handleStatusValue));
            
            warningData.put("attachment", swmWarningManagement.getAttachment());
            warningData.put("remarks", swmWarningManagement.getRemarks());
            
            // 处理预警类型枚举值 - 直接使用文本值
            warningData.put("warningType", SwmWarningManagement.WarningTypeEnum.getText(swmWarningManagement.getWarningType()));
            
            // 预警内容直接使用原值，不进行枚举转换
            warningData.put("warningContent", swmWarningManagement.getWarningContent());
            
            result.putAll(warningData);
        }
        return result;
    }

    /**
     * 保存数据
     */
    @PostMapping(value = "save")
    @ResponseBody
    @ApiOperation("保存数据")
    public String save(@Validated SwmWarningManagement swmWarningManagement) {
        swmWarningManagementService.save(swmWarningManagement);
        return renderResult(Global.TRUE, text("保存预警信息成功！"));
    }
    
    /**
     * 删除数据
     */
    @PostMapping(value = "delete")
    @ResponseBody
    @ApiOperation("删除数据")
    public String delete(SwmWarningManagement swmWarningManagement) {
        swmWarningManagementService.delete(swmWarningManagement);
        return renderResult(Global.TRUE, text("删除预警信息成功！"));
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
            SwmWarningManagement swmWarningManagement = swmWarningManagementService.get(id);
            if (swmWarningManagement != null) {
                swmWarningManagementService.delete(swmWarningManagement);
            }
        }
        return renderResult(Global.TRUE, text("批量删除预警信息成功！"));
    }
    
    /**
     * 获取枚举选项
     */
    @GetMapping(value = "enumOptions")
    @ResponseBody
    @ApiOperation("获取枚举选项")
    public Map<String, Object> getEnumOptions() {
        Map<String, Object> result = new HashMap<>();
        
        // 预警类型选项
        Map<String, String> warningTypeOptions = new HashMap<>();
        warningTypeOptions.put(SwmWarningManagement.WarningTypeEnum.ACTIVE, "主动预警");
        warningTypeOptions.put(SwmWarningManagement.WarningTypeEnum.PASSIVE, "被动预警");
        result.put("warningTypeOptions", warningTypeOptions);
        
        // 预警内容选项 - 直接使用固定文本值，不使用枚举
        Map<String, String> warningContentOptions = new HashMap<>();
        warningContentOptions.put("一键SOS", "一键SOS");
        warningContentOptions.put("静默预警", "静默预警");
        warningContentOptions.put("桁车预警", "桁车预警");
        warningContentOptions.put("高空预警", "高空预警");
        warningContentOptions.put("油漆库预警", "油漆库预警");
        result.put("warningContentOptions", warningContentOptions);
        
        // 处置状态选项
        Map<String, String> handleStatusOptions = new HashMap<>();
        handleStatusOptions.put(SwmWarningManagement.HandleStatusEnum.UNHANDLED, "未处置");
        handleStatusOptions.put(SwmWarningManagement.HandleStatusEnum.HANDLED, "已处置");
        result.put("handleStatusOptions", handleStatusOptions);
        
        return result;
    }

    /**
     * 处理预警
     */
    @PostMapping(value = "process")
    @ResponseBody
    @ApiOperation("处理预警")
    public String process(String id, String handler, String handleTime, String handleProcess, String handleStatus) {
        // 获取预警记录
        SwmWarningManagement swmWarningManagement = swmWarningManagementService.get(id);
        if (swmWarningManagement == null) {
            return renderResult(Global.FALSE, text("预警记录不存在！"));
        }
        
        // 更新处置信息
        swmWarningManagement.setHandler(handler);
        if (handleTime != null && !handleTime.isEmpty()) {
            swmWarningManagement.setHandleTime(DateUtils.parseDate(handleTime));
        } else {
            swmWarningManagement.setHandleTime(new Date());
        }
        swmWarningManagement.setHandleProcess(handleProcess);
        swmWarningManagement.setHandleStatus(handleStatus);
        
        // 保存更新
        swmWarningManagementService.save(swmWarningManagement);
        
        return renderResult(Global.TRUE, text("预警处置成功！"));
    }
} 