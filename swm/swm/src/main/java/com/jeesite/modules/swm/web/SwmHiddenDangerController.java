/**
 * 隐患信息Controller
 * @author Shawn
 * @date 2023-11-16
 */
package com.jeesite.modules.swm.web;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.GetMapping;

import com.jeesite.common.config.Global;
import com.jeesite.common.entity.Page;
import com.jeesite.common.web.BaseController;
import com.jeesite.modules.swm.entity.SwmHiddenDanger;
import com.jeesite.modules.swm.service.SwmHiddenDangerService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * 隐患信息Controller
 * 
 * @author Shawn
 * @date 2023-11-16
 */
@Controller
@RequestMapping(value = "${adminPath}/hiddenDanger")
@Api(value = "隐患信息管理接口", tags = "隐患信息管理接口")
public class SwmHiddenDangerController extends BaseController {

    @Autowired
    private SwmHiddenDangerService swmHiddenDangerService;

    /**
     * 获取数据
     */
    @ModelAttribute
    public SwmHiddenDanger get(String id, boolean isNewRecord) {
        return swmHiddenDangerService.get(id, isNewRecord);
    }

    /**
     * 查询列表
     */
    @RequestMapping(value = { "list", "" })
    @ApiOperation("查询列表")
    public String list(SwmHiddenDanger swmHiddenDanger, Model model) {
        model.addAttribute("swmHiddenDanger", swmHiddenDanger);
        return "modules/swm/swmHiddenDangerList";
    }

    /**
     * 查询列表数据
     */
    @RequestMapping(value = "listData")
    @ResponseBody
    @ApiOperation("查询列表数据")
    public Page<SwmHiddenDanger> listData(SwmHiddenDanger swmHiddenDanger, HttpServletRequest request,
            HttpServletResponse response) {
        Page<SwmHiddenDanger> page = swmHiddenDangerService.findPage(new Page<>(request, response), swmHiddenDanger);
        return page;
    }

    /**
     * 查看编辑表单
     */
    @RequestMapping(value = "form")
    @ResponseBody
    @ApiOperation("查看编辑表单")
    public Map<String, Object> form(SwmHiddenDanger swmHiddenDanger) {
        Map<String, Object> result = new HashMap<>();
        result.put("hiddenDanger", swmHiddenDanger);
        return result;
    }

    /**
     * 保存隐患信息
     */
    @PostMapping(value = "save")
    @ResponseBody
    @ApiOperation("保存隐患信息")
    public Map<String, Object> save(@Validated SwmHiddenDanger swmHiddenDanger) {
        Map<String, Object> result = new HashMap<>();
        swmHiddenDangerService.save(swmHiddenDanger);
        result.put("status", "success");
        result.put("message", "保存隐患信息成功");
        return result;
    }

    /**
     * 删除隐患信息
     */
    @RequestMapping(value = "delete")
    @ResponseBody
    @ApiOperation("删除隐患信息")
    public Map<String, Object> delete(SwmHiddenDanger swmHiddenDanger) {
        Map<String, Object> result = new HashMap<>();
        swmHiddenDangerService.delete(swmHiddenDanger);
        result.put("status", "success");
        result.put("message", "删除隐患信息成功");
        return result;
    }
}