package com.jeesite.modules.swm.web;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.alibaba.cloud.commons.lang.StringUtils;
import com.jeesite.modules.sys.entity.DictType;
import com.jeesite.modules.sys.utils.CorpUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import io.swagger.annotations.*;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.jeesite.common.config.Global;
import com.jeesite.common.entity.Page;
import com.jeesite.common.web.BaseController;
import com.jeesite.modules.swm.entity.SwmDictType;
import com.jeesite.modules.swm.entity.SwmDictData;
import com.jeesite.modules.swm.service.SwmDictTypeService;

/**
 * 字典类型表Controller
 *
 * @author wxy
 * @version 2026-04-24
 */
@Controller
@RequestMapping(value = "${adminPath}/swmDictType")
@Api(value = "字典类型表接口", tags = "字典类型表")
public class SwmDictTypeController extends BaseController {

    @Resource
    private SwmDictTypeService swmDictTypeService;

    /**
     * 获取数据
     */
    @ModelAttribute
    public SwmDictType get(String id, boolean isNewRecord) {
        SwmDictType swmDictType = swmDictTypeService.get(id, isNewRecord);
        return swmDictType;
    }

    /**
     * 校验唯一性
     */
    @RequestMapping(value = "checkDictType")
    @ResponseBody
    public String checkDictType(String oldDictType, String dictType) {
        if (dictType != null && dictType.equals(oldDictType)) {
            return "true";
        }
        SwmDictType param = new SwmDictType();
        param.setDictType(dictType);
        List<SwmDictType> list = swmDictTypeService.findList(param);
        if (list.size() > 0) {
            return "false";
        }
        return "true";

    }

    /**
     * 删除
     */
    @RequestMapping(value = "delete")
    @ResponseBody
    @ApiOperation(value = "删除数据", notes = "删除数据")
    public String delete(SwmDictType swmDictType, HttpServletRequest request) {

        swmDictTypeService.delete(swmDictType);
        return renderResult(Global.TRUE, text("删除成功！"));
    }


    /**
     * 禁用
     */
    @RequestMapping(value = "disable")
    @ResponseBody
    public String disable(SwmDictType swmDictType) {
        swmDictType.setStatus("2");
        swmDictTypeService.updateStatus(swmDictType);
        return renderResult(Global.TRUE, text("禁用成功！"));
    }

    /**
     * 树结构数据
     */
    @RequestMapping(value = "treeData")
    @ResponseBody
    public List<Map<String, Object>> treeData(String excludeId) {

        List<Map<String, Object>> list = new ArrayList<>();
        List<SwmDictType> dictList = swmDictTypeService.findList(new SwmDictType());

        for (SwmDictType e : dictList) {
            if ("0".equals(e.getStatus())
                    && (excludeId == null || !e.getId().equals(excludeId))) {

                Map<String, Object> map = new HashMap<>();
                map.put("id", e.getId());
                map.put("pId", "0");
                map.put("name", e.getDictName());
                map.put("dictType", e.getDictType());
                list.add(map);
            }
        }
        return list;
    }

    /**
     * 查询列表页面
     */
    @RequestMapping(value = {"list", ""})
    @ApiOperation(value = "查询列表", notes = "查询列表")
    public String list(SwmDictType dictType, Model model) {
        if (!dictType.getCurrentUser().isSuperAdmin()) {
            dictType.setIsSys("0");
        }
        model.addAttribute("dictType", dictType);
        return "modules/swm/swmDictTypeList";
    }


    /**
     * 表单
     */
    @RequestMapping(value = "form")
    @ApiOperation(value = "查看编辑表单", notes = "查看编辑表单")
    public DictType form(SwmDictType swmDictType, Model model) {
        if (StringUtils.isBlank(swmDictType.getIsSys())) {
            swmDictType.setIsSys("1");
        }
        SwmDictType swmDictType1 = swmDictTypeService.get(swmDictType);
        if (swmDictType1 == null) {
           return new DictType();
        }
        DictType dictType = new DictType();
        BeanUtils.copyProperties(swmDictType1, dictType);
        return dictType;
    }


    /**
     * 启用
     */
    @RequestMapping(value = "enable")
    @ResponseBody
    public String enable(SwmDictType swmDictType) {
        swmDictType.setStatus("0");
        swmDictTypeService.updateStatus(swmDictType);
        return renderResult(Global.TRUE, text("启用成功！"));
    }

    /**
     * 保存数据
     */
    @PostMapping(value = "save")
    @ResponseBody
    @ApiOperation(value = "保存数据", notes = "保存数据")
    public String save(SwmDictType swmDictType, HttpServletRequest request) {
//        swmDictType.setCorpCode(CorpUtils.getCurrentCorpCode());
//        swmDictType.setCorpName(CorpUtils.getCurrentCorpName());
        swmDictTypeService.save(swmDictType);
        return renderResult(Global.TRUE, text("保存字典类型成功！"));
    }


    /**
     * 查询列表数据（分页）
     */
    @RequestMapping(value = "listData")
    @ResponseBody
    @ApiOperation(value = "查询列表数据", notes = "查询列表数据")
    public Page<SwmDictType> listData(SwmDictType swmDictType,
                                      HttpServletRequest request,
                                      HttpServletResponse response) {
        swmDictType.setPage(new Page<>(request, response));
        return swmDictTypeService.findPage(swmDictType);
    }


}