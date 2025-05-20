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
import com.jeesite.modules.swm.entity.SwmInspectionList;
import com.jeesite.modules.swm.service.SwmInspectionListService;

/**
 * 巡检列表Controller
 * 
 * @author Shawn
 * @version 2024-06-22
 */
@Controller
@RequestMapping(value = "${adminPath}/swmInspectionList")
public class SwmInspectionListController extends BaseController {

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

}