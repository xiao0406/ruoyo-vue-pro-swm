/**
 * 隐患处置信息Controller
 * @author Shawn
 * @date 2024-06-28
 */
package com.jeesite.modules.swm.web;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.List;
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
import org.springframework.web.bind.annotation.RequestParam;

import com.jeesite.common.config.Global;
import com.jeesite.common.entity.Page;
import com.jeesite.common.web.BaseController;
import com.jeesite.modules.swm.entity.SwmDangerDisposal;
import com.jeesite.modules.swm.service.SwmDangerDisposalService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * 隐患处置信息Controller
 * 
 * @author Shawn
 * @date 2024-06-28
 */
@Controller
@RequestMapping(value = "${adminPath}/dangerDisposal")
@Api(tags = "隐患处置信息")
public class SwmDangerDisposalController extends BaseController {

    @Autowired
    private SwmDangerDisposalService swmDangerDisposalService;

    /**
     * 获取单条数据
     */
    @GetMapping(value = "get")
    @ResponseBody
    @ApiOperation("获取隐患处置信息")
    public SwmDangerDisposal get(SwmDangerDisposal swmDangerDisposal) {
        return swmDangerDisposalService.get(swmDangerDisposal);
    }

    /**
     * 查询分页数据
     */
    @RequestMapping(value = "list")
    @ResponseBody
    @ApiOperation("查询隐患处置信息列表")
    public Page<SwmDangerDisposal> list(SwmDangerDisposal swmDangerDisposal, HttpServletRequest request,
            HttpServletResponse response) {
        Page<SwmDangerDisposal> page = swmDangerDisposalService.findPage(new Page<>(request, response),
                swmDangerDisposal);
        return page;
    }

    /**
     * 保存隐患处置信息
     */
    @PostMapping(value = "save")
    @ResponseBody
    @ApiOperation("保存隐患处置信息")
    public Map<String, Object> save(@Validated SwmDangerDisposal swmDangerDisposal) {
        Map<String, Object> result = new HashMap<>();
        swmDangerDisposalService.save(swmDangerDisposal);
        result.put("status", "success");
        result.put("message", swmDangerDisposal.getIsNewRecord() ? "新增隐患处置信息成功" : "更新隐患处置信息成功");
        return result;
    }

    /**
     * 删除隐患处置信息
     */
    @RequestMapping(value = "delete")
    @ResponseBody
    @ApiOperation("删除隐患处置信息")
    public Map<String, Object> delete(SwmDangerDisposal swmDangerDisposal) {
        Map<String, Object> result = new HashMap<>();
        swmDangerDisposalService.delete(swmDangerDisposal);
        result.put("status", "success");
        result.put("message", "删除隐患处置信息成功");
        return result;
    }

    /**
     * 根据隐患ID查询处置记录
     */
    @GetMapping(value = "listByHiddenDangerId")
    @ResponseBody
    @ApiOperation("根据隐患ID查询处置记录")
    public List<SwmDangerDisposal> listByHiddenDangerId(@RequestParam("hiddenDangerId") String hiddenDangerId) {
        return swmDangerDisposalService.findByHiddenDangerId(hiddenDangerId);
    }
}