/**
 * @author Shawn
 * @date 2025/06/26
 */
package com.jeesite.modules.swm.web;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.jeesite.common.config.Global;
import com.jeesite.common.entity.Page;
import com.jeesite.common.web.BaseController;
import com.jeesite.modules.swm.entity.SwmJobLog;
import com.jeesite.modules.swm.service.SwmJobLogService;

/**
 * 定时任务调度日志表Controller
 * 
 * @author Shawn
 * @version 2025-06-26
 */
@Controller
@RequestMapping(value = "${adminPath}/swm/swmJobLog")
public class SwmJobLogController extends BaseController {

    @Autowired
    private SwmJobLogService swmJobLogService;

    /**
     * 获取数据
     */
    @ModelAttribute
    public SwmJobLog get(String id, boolean isNewRecord) {
        return swmJobLogService.get(id, isNewRecord);
    }

    /**
     * 查询列表
     */
    @RequestMapping(value = { "list", "" })
    public String list(SwmJobLog swmJobLog, Model model) {
        model.addAttribute("swmJobLog", swmJobLog);
        return "swm/swmJobLogList";
    }

    /**
     * 查询列表数据
     */
    @RequestMapping(value = "listData")
    @ResponseBody
    public Page<SwmJobLog> listData(SwmJobLog swmJobLog, HttpServletRequest request, HttpServletResponse response) {
        swmJobLog.setPage(new Page<SwmJobLog>(request, response));
        Page<SwmJobLog> page = swmJobLogService.findPage(swmJobLog);
        return page;
    }

    /**
     * 查看编辑表单
     */
    @RequestMapping(value = "form")
    public String form(SwmJobLog swmJobLog, Model model) {
        model.addAttribute("swmJobLog", swmJobLog);
        return "swm/swmJobLogForm";
    }

    /**
     * 保存数据
     */
    @PostMapping(value = "save")
    @ResponseBody
    public String save(@Validated SwmJobLog swmJobLog) {
        swmJobLogService.save(swmJobLog);
        return renderResult(Global.TRUE, text("保存定时任务调度日志成功"));
    }

    /**
     * 删除数据
     */
    @RequestMapping(value = "delete")
    @ResponseBody
    public String delete(SwmJobLog swmJobLog) {
        swmJobLogService.delete(swmJobLog);
        return renderResult(Global.TRUE, text("删除定时任务调度日志成功"));
    }

}