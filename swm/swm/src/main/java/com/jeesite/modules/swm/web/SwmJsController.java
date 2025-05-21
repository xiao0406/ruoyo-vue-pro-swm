package com.jeesite.modules.swm.web;

import com.jeesite.common.config.Global;
import com.jeesite.common.entity.Page;
import com.jeesite.common.lang.DateUtils;
import com.jeesite.common.lang.StringUtils;
import com.jeesite.common.web.BaseController;
import com.jeesite.modules.swm.entity.SwmHandleRecord;
import com.jeesite.modules.swm.entity.SwmWarningManagement;
import com.jeesite.modules.swm.service.SwmHandleRecordService;
import com.jeesite.modules.swm.service.SwmWarningManagementService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 前端JS API控制器
 * @author zwf
 * @version 2025-05-16
 */
@Controller
@RequestMapping(value = "/js/swm")
@Api(value = "前端JS API", tags = "前端JS API")
public class SwmJsController extends BaseController {

    @Autowired
    private SwmWarningManagementService swmWarningManagementService;
    
    @Autowired
    private SwmHandleRecordService swmHandleRecordService;
    
    /**
     * 处理预警
     */
    @PostMapping(value = "warningManagement/process")
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
    
    /**
     * 查询处置记录列表
     */
    @RequestMapping(value = "handleRecord/listData")
    @ResponseBody
    @ApiOperation("查询处置记录列表")
    public Page<SwmHandleRecord> listHandleRecords(SwmHandleRecord swmHandleRecord, HttpServletRequest request, HttpServletResponse response) {
        // 创建分页对象
        Page<SwmHandleRecord> page = new Page<>(request, response);
        
        // 清除默认的handleStatus值，除非前端明确传入
        if (swmHandleRecord != null && StringUtils.isBlank(request.getParameter("handleStatus"))) {
            swmHandleRecord.setHandleStatus(null);
        }
        
        // 调用服务层方法，获取带文本值的分页数据
        return swmHandleRecordService.findPageWithTextValues(page, swmHandleRecord);
    }
    
    /**
     * 根据预警ID查询处置记录
     */
    @GetMapping(value = "handleRecord/byWarningId")
    @ResponseBody
    @ApiOperation("根据预警ID查询处置记录")
    public Map<String, Object> getHandleRecordsByWarningId(String warningId) {
        Map<String, Object> result = new HashMap<>();
        if (warningId != null && !warningId.isEmpty()) {
            // 创建查询条件
            SwmHandleRecord record = new SwmHandleRecord();
            record.setWarningId(warningId);
            // 清除默认的handleStatus
            record.setHandleStatus(null);
            
            result.put("list", swmHandleRecordService.findList(record));
            result.put("success", true);
        } else {
            result.put("success", false);
            result.put("message", "预警ID不能为空");
        }
        return result;
    }
    
    /**
     * 新增处置记录
     */
    @PostMapping(value = "handleRecord/addByWarningId")
    @ResponseBody
    @ApiOperation("新增处置记录")
    public String addHandleRecord(String warningId, String recordName, String handler, 
                               String handleProcess, String handleStatus, String remarks) {
        if (warningId == null || warningId.isEmpty()) {
            return renderResult(Global.FALSE, text("预警ID不能为空！"));
        }
        
        // 创建新的处置记录
        SwmHandleRecord record = SwmHandleRecord.createNewRecord();
        record.setWarningId(warningId);
        record.setRecordName(recordName);
        record.setHandler(handler);
        record.setHandleTime(new Date()); // 使用当前时间
        record.setHandleProcess(handleProcess);
        if (StringUtils.isNotBlank(handleStatus)) {
            record.setHandleStatus(handleStatus);
        }
        record.setRemarks(remarks);
        
        // 保存记录
        swmHandleRecordService.save(record);
        
        return renderResult(Global.TRUE, text("添加处置记录成功！"));
    }
    
    /**
     * 更新处置记录状态
     */
    @PostMapping(value = "handleRecord/updateStatus")
    @ResponseBody
    @ApiOperation("更新处置记录状态")
    public String updateHandleRecordStatus(String id, String handleStatus, String handler, 
                                         String handleProcess, String remarks) {
        // 获取记录
        SwmHandleRecord record = swmHandleRecordService.get(id);
        if (record == null) {
            return renderResult(Global.FALSE, text("处置记录不存在！"));
        }
        
        // 更新处置信息
        if (handleStatus != null && !handleStatus.isEmpty()) {
            record.setHandleStatus(handleStatus);
        }
        if (handler != null && !handler.isEmpty()) {
            record.setHandler(handler);
        }
        record.setHandleTime(new Date()); // 更新处置时间
        if (handleProcess != null && !handleProcess.isEmpty()) {
            record.setHandleProcess(handleProcess);
        }
        if (remarks != null) {
            record.setRemarks(remarks);
        }
        
        // 保存更新
        swmHandleRecordService.save(record);
        
        return renderResult(Global.TRUE, text("更新处置记录状态成功！"));
    }
} 