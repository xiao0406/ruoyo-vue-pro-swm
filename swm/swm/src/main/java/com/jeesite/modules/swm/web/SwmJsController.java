package com.jeesite.modules.swm.web;

import com.jeesite.common.config.Global;
import com.jeesite.common.lang.DateUtils;
import com.jeesite.common.web.BaseController;
import com.jeesite.modules.swm.entity.SwmWarningManagement;
import com.jeesite.modules.swm.service.SwmWarningManagementService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Date;

/**
 * 前端JS API控制器
 * @author auto create
 * @version 2025-05-16
 */
@Controller
@RequestMapping(value = "/js/swm")
@Api(value = "前端JS API", tags = "前端JS API")
public class SwmJsController extends BaseController {

    @Autowired
    private SwmWarningManagementService swmWarningManagementService;
    
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
} 