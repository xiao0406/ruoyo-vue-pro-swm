package com.jeesite.modules.swm.web;

import com.jeesite.common.config.Global;
import com.jeesite.common.entity.Page;
import com.jeesite.common.web.BaseController;
import com.jeesite.modules.swm.entity.SwmInspectionList;
import com.jeesite.modules.swm.service.SwmInspectionListService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.List;

/**
 * 巡检列表Controller
 *
 * @author Shawn
 * @version 2024-06-22
 */
@Controller
@RequestMapping(value = "${adminPath}/swmInspectionList")
public class SwmInspectionListController extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(SwmInspectionListController.class);

    @Autowired
    private SwmInspectionListService swmInspectionListService;

    /**
     * 获取数据
     */
    @ModelAttribute
    public SwmInspectionList get(String id, boolean isNewRecord) {
        return swmInspectionListService.get(id, isNewRecord);
    }

    /**
     * 查询列表
     */
    @RequestMapping(value = { "list", "" })
    public String list(SwmInspectionList swmInspectionList, Model model) {
        model.addAttribute("swmInspectionList", swmInspectionList);
        return "modules/swm/swmInspectionListList";
    }

    /**
     * 查询列表数据
     */
    @RequestMapping(value = "listData")
    @ResponseBody
    public Page<SwmInspectionList> listData(SwmInspectionList swmInspectionList, HttpServletRequest request,
            HttpServletResponse response) {
        swmInspectionList.setPage(new Page<>(request, response));
        Page<SwmInspectionList> page = swmInspectionListService.findPage(swmInspectionList);
        return page;
    }

    /**
     * 查看编辑表单
     */
    @RequestMapping(value = "form")
    public String form(SwmInspectionList swmInspectionList, Model model) {
        model.addAttribute("swmInspectionList", swmInspectionList);
        return "modules/swm/swmInspectionListForm";
    }

    /**
     * 保存巡检列表
     */
    @PostMapping(value = "save")
    @ResponseBody
    public String save(@Validated SwmInspectionList swmInspectionList) {
        swmInspectionListService.save(swmInspectionList);
        return renderResult(Global.TRUE, text("保存巡检列表成功！"));
    }

    /**
     * 删除巡检列表
     */
    @RequestMapping(value = "delete")
    @ResponseBody
    public String delete(SwmInspectionList swmInspectionList) {
        swmInspectionListService.delete(swmInspectionList);
        return renderResult(Global.TRUE, text("删除巡检列表成功！"));
    }

    /**
     * 热力图-最新巡检记录
     */
    @RequestMapping(value = "latestInspectionRecord")
    @ResponseBody
    public List<SwmInspectionList> latestInspectionRecord() {
        return swmInspectionListService.latestInspectionRecord(10);
    }

    /**
     * 开始巡检任务
     * 将任务状态从"待处理"改为"进行中"
     */
    @PostMapping(value = "startTask")
    @ResponseBody
    public String startTask(@RequestBody SwmInspectionList taskData) {
        String id = taskData.getId();
        logger.info("开始巡检任务，任务ID: {}", id);
        
        SwmInspectionList inspectionList = swmInspectionListService.startInspectionTask(id);
        if (inspectionList == null) {
            return renderResult(Global.FALSE, text("开始巡检任务失败，请检查任务状态"));
        }

        return renderResult(Global.TRUE, text("巡检任务已开始"));
    }

    /**
     * 完成巡检任务
     * 将任务状态从"进行中"改为"已完成"，并更新结束时间和附件
     */
    @PostMapping(value = "completeTask")
    @ResponseBody
    public String completeTask(@RequestBody SwmInspectionList completeData) {
        String id = completeData.getId();
        logger.info("完成巡检任务，任务ID: {}", id);
        
        SwmInspectionList inspectionList = swmInspectionListService.completeInspectionTask(completeData);
        if (inspectionList == null) {
            return renderResult(Global.FALSE, text("完成巡检任务失败，请检查任务状态"));
        }

        return renderResult(Global.TRUE, text("巡检任务已完成"));
    }
}
