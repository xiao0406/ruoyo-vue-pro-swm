package com.jeesite.modules.swm.web;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.jeesite.common.config.Global;
import com.jeesite.common.entity.Page;
import com.jeesite.common.web.BaseController;
import com.jeesite.modules.swm.entity.SwmSiteMapManagement;
import com.jeesite.modules.swm.service.SwmSiteMapManagementService;

/**
 * 场地底图管理表Controller
 * 
 * @author zwf
 * @version 2025-05-30
 */
@Controller
@RequestMapping(value = "${adminPath}/swmSiteMapManagement")
public class SwmSiteMapManagementController extends BaseController {

    @Autowired
    private SwmSiteMapManagementService swmSiteMapManagementService;
    
    /**
     * 获取数据
     */
    @ModelAttribute
    public SwmSiteMapManagement get(String id, boolean isNewRecord) {
        return swmSiteMapManagementService.get(id, isNewRecord);
    }
    
    /**
     * 查询列表
     */
    @RequestMapping(value = { "list", "" })
    public String list(SwmSiteMapManagement swmSiteMapManagement, Model model) {
        model.addAttribute("swmSiteMapManagement", swmSiteMapManagement);
        return "modules/swm/swmSiteMapManagementList";
    }
    
    /**
     * 查询列表数据
     */
    @RequestMapping(value = "listData")
    @ResponseBody
    public Page<SwmSiteMapManagement> listData(SwmSiteMapManagement swmSiteMapManagement, HttpServletRequest request, HttpServletResponse response) {
        swmSiteMapManagement.setPage(new Page<>(request, response));
        // 完全移除状态过滤条件
        swmSiteMapManagement.setStatus(null);
        // 设置为不使用全局状态过滤
        swmSiteMapManagement.getSqlMap().getWhere().disableAutoAddStatusWhere();
        Page<SwmSiteMapManagement> page = swmSiteMapManagementService.findPage(swmSiteMapManagement);
        return page;
    }

    /**
     * 查看编辑表单
     */
    @RequestMapping(value = "form")
    public String form(SwmSiteMapManagement swmSiteMapManagement, Model model) {
        model.addAttribute("swmSiteMapManagement", swmSiteMapManagement);
        return "modules/swm/swmSiteMapManagementForm";
    }

    /**
     * 保存场地底图管理
     */
    @PostMapping(value = "save")
    @ResponseBody
    public String save(@Validated SwmSiteMapManagement swmSiteMapManagement) {
        swmSiteMapManagementService.save(swmSiteMapManagement);
        return renderResult(Global.TRUE, text("保存场地底图管理成功！"));
    }
    
    /**
     * 删除场地底图管理
     */
    @DeleteMapping(value = "delete")
    @ResponseBody
    public String delete(SwmSiteMapManagement swmSiteMapManagement) {
        swmSiteMapManagementService.delete(swmSiteMapManagement);
        return renderResult(Global.TRUE, text("删除场地底图管理成功！"));
    }
    
    /**
     * 删除场地底图管理（JSON方式）
     */
    @RequestMapping(value = "deleteJson", method = {RequestMethod.DELETE, RequestMethod.POST})
    @ResponseBody
    public String deleteJson(@RequestBody(required = false) Map<String, Object> params, @RequestParam(required = false) String id, HttpServletRequest request) {
        // 尝试从JSON请求体获取ID
        if (params != null && params.get("id") != null) {
            id = params.get("id").toString();
        }
        
        // 如果JSON中没有ID，尝试从URL参数获取
        if ((id == null || id.isEmpty()) && request != null) {
            id = request.getParameter("id");
        }
        
        if (id == null || id.isEmpty()) {
            return renderResult(Global.FALSE, text("删除失败：ID不能为空！"));
        }
        
        SwmSiteMapManagement swmSiteMapManagement = new SwmSiteMapManagement(id);
        swmSiteMapManagementService.delete(swmSiteMapManagement);
        return renderResult(Global.TRUE, text("删除场地底图管理成功！"));
    }
    
    /**
     * 批量删除场地底图管理
     */
    @RequestMapping(value = "deleteAll")
    @ResponseBody
    public String deleteAll(String ids) {
        String[] idArray = ids.split(",");
        for (String id : idArray) {
            SwmSiteMapManagement swmSiteMapManagement = swmSiteMapManagementService.get(id);
            if (swmSiteMapManagement != null) {
                swmSiteMapManagementService.delete(swmSiteMapManagement);
            }
        }
        return renderResult(Global.TRUE, text("批量删除场地底图管理成功！"));
    }
    
    /**
     * 获取场地底图管理列表（用于下拉选择）
     */
    @RequestMapping(value = "getMapList")
    @ResponseBody
    public List<Map<String, Object>> getMapList() {
        List<Map<String, Object>> result = new ArrayList<>();
        
        List<SwmSiteMapManagement> mapList = swmSiteMapManagementService.findList(new SwmSiteMapManagement());
        for (SwmSiteMapManagement map : mapList) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", map.getId());
            item.put("mapName", map.getMapName());
            item.put("projectId", map.getProjectId());
            item.put("filePath", map.getFilePath());
            
            result.add(item);
        }
        
        return result;
    }
    
    /**
     * 启用底图
     */
    @RequestMapping(value = "enable", method = {RequestMethod.POST})
    @ResponseBody
    public String enable(@RequestParam(required = false) String id, 
                          @RequestParam(required = false) String status,
                          HttpServletRequest request) {

        // 如果参数中没有ID，尝试从请求中获取
        if ((id == null || id.isEmpty()) && request != null) {
            id = request.getParameter("id");
        }
        
        if (id == null || id.isEmpty()) {
            return renderResult(Global.FALSE, text("操作失败：ID不能为空！"));
        }
        
        try {
            // 调用服务层方法启用底图（启用该底图同时会禁用其他底图）
            swmSiteMapManagementService.changeStatus(id, "0");
            return renderResult(Global.TRUE, text("启用场地底图成功！"));
        } catch (Exception e) {
            return renderResult(Global.FALSE, text("操作失败：" + e.getMessage()));
        }
    }
    
    /**
     * 禁用底图
     */
    @RequestMapping(value = "disable", method = {RequestMethod.POST})
    @ResponseBody
    public String disable(@RequestParam(required = false) String id, 
                           @RequestParam(required = false) String status,
                           HttpServletRequest request) {
        // 如果参数中没有ID，尝试从请求中获取
        if ((id == null || id.isEmpty()) && request != null) {
            id = request.getParameter("id");
        }
        
        if (id == null || id.isEmpty()) {
            return renderResult(Global.FALSE, text("操作失败：ID不能为空！"));
        }
        
        try {
            // 调用服务层方法禁用底图
            swmSiteMapManagementService.changeStatus(id, "1");
            return renderResult(Global.TRUE, text("禁用场地底图成功！"));
        } catch (Exception e) {
            return renderResult(Global.FALSE, text("操作失败：" + e.getMessage()));
        }
    }
    
    /**
     * 获取已启用底图
     */
    @RequestMapping(value = "getEnabledMap")
    @ResponseBody
    public Map<String, Object> getEnabledMap() {
        Map<String, Object> result = new HashMap<>();
        
        // 查询系统中启用的底图
        SwmSiteMapManagement query = new SwmSiteMapManagement();
        query.setStatus("0"); // 0表示启用
        List<SwmSiteMapManagement> maps = swmSiteMapManagementService.findList(query);
        
        if (maps != null && !maps.isEmpty()) {
            SwmSiteMapManagement map = maps.get(0);
            result.put("success", true);
            result.put("id", map.getId());
            result.put("mapName", map.getMapName());
            result.put("projectId", map.getProjectId());
            result.put("filePath", map.getFilePath());
            result.put("mapSize", map.getMapSize());
            result.put("scale", map.getScale());
        } else {
            result.put("success", false);
            result.put("message", "系统中没有启用的底图");
        }
        
        return result;
    }
} 