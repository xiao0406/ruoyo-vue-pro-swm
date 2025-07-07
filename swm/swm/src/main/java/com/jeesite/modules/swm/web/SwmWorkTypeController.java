package com.jeesite.modules.swm.web;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.jeesite.common.config.Global;
import com.jeesite.common.entity.Page;
import com.jeesite.common.web.BaseController;
import com.jeesite.modules.swm.entity.SwmWorkType;
import com.jeesite.modules.swm.service.SwmWorkTypeService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * 工种表Controller
 * 
 * @author Shawn
 * @version 2025-07-04
 */
@Controller
@RequestMapping(value = "${adminPath}/swmWorkType")
@Api(value = "工种管理接口", tags = "工种管理接口")
public class SwmWorkTypeController extends BaseController {

    @Autowired
    private SwmWorkTypeService swmWorkTypeService;

    /**
     * 获取数据
     */
    @ModelAttribute
    public SwmWorkType get(String id, boolean isNewRecord) {
        return swmWorkTypeService.get(id, isNewRecord);
    }

    /**
     * 查询列表
     */
    @RequestMapping(value = { "list", "" })
    @ApiOperation("查询列表")
    public String list(SwmWorkType swmWorkType, Model model) {
        model.addAttribute("swmWorkType", swmWorkType);
        return "modules/swm/swmWorkTypeList";
    }

    /**
     * 查询列表数据
     */
    @RequestMapping(value = "listData")
    @ResponseBody
    @ApiOperation("查询列表数据")
    public Map<String, Object> listData(SwmWorkType swmWorkType, HttpServletRequest request,
            HttpServletResponse response) {
        Map<String, Object> result = new HashMap<>();
        
        // 设置分页参数
        swmWorkType.setPage(new Page<>(request, response));
        
        // 查询数据
        Page<SwmWorkType> page = swmWorkTypeService.findPage(swmWorkType);
        
        result.put("success", true);
        result.put("list", page.getList());
        result.put("page", page);
        result.put("total", page.getCount());
        
        return result;
    }

    /**
     * 查看编辑表单
     */
    @RequestMapping(value = "form")
    @ApiOperation("查看编辑表单")
    public String form(SwmWorkType swmWorkType, Model model) {
        model.addAttribute("swmWorkType", swmWorkType);
        return "modules/swm/swmWorkTypeForm";
    }

    /**
     * 保存工种
     */
    @PostMapping(value = "save")
    @ResponseBody
    @ApiOperation("保存工种")
    public String save(SwmWorkType swmWorkType) {
        swmWorkTypeService.save(swmWorkType);
        return renderResult(Global.TRUE, text("保存工种成功！"));
    }

    /**
     * 停用工种
     */
    @RequestMapping(value = "disable")
    @ResponseBody
    @ApiOperation("停用工种")
    public String disable(SwmWorkType swmWorkType) {
        swmWorkType.setStatus(SwmWorkType.STATUS_DISABLE);
        swmWorkTypeService.updateStatus(swmWorkType);
        return renderResult(Global.TRUE, text("停用工种成功"));
    }

    /**
     * 启用工种
     */
    @RequestMapping(value = "enable")
    @ResponseBody
    @ApiOperation("启用工种")
    public String enable(SwmWorkType swmWorkType) {
        swmWorkType.setStatus(SwmWorkType.STATUS_NORMAL);
        swmWorkTypeService.updateStatus(swmWorkType);
        return renderResult(Global.TRUE, text("启用工种成功"));
    }

    /**
     * 删除工种
     */
    @RequestMapping(value = "delete")
    @ResponseBody
    @ApiOperation("删除工种")
    public String delete(SwmWorkType swmWorkType) {
        swmWorkTypeService.delete(swmWorkType);
        return renderResult(Global.TRUE, text("删除工种成功！"));
    }

    /**
     * 获取有效工种列表（用于下拉框）
     */
    @RequestMapping(value = "activeList")
    @ResponseBody
    @ApiOperation("获取有效工种列表")
    public Map<String, Object> activeList() {
        Map<String, Object> result = new HashMap<>();
        List<SwmWorkType> list = swmWorkTypeService.findActiveList();
        result.put("success", true);
        result.put("list", list);
        return result;
    }
}
