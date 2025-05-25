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
import com.jeesite.modules.swm.entity.SwmBeaconColorConfig;
import com.jeesite.modules.swm.service.SwmBeaconColorConfigService;

/**
 * 信标颜色配置表Controller
 * 
 * @author zwf
 * @version 2025-05-23
 */
@Controller
@RequestMapping(value = "${adminPath}/swmBeaconColorConfig")
public class SwmBeaconColorConfigController extends BaseController {

    @Autowired
    private SwmBeaconColorConfigService swmBeaconColorConfigService;
    
    /**
     * 获取数据
     */
    @ModelAttribute
    public SwmBeaconColorConfig get(String id, boolean isNewRecord) {
        return swmBeaconColorConfigService.get(id, isNewRecord);
    }
    
    /**
     * 查询列表
     */
    @RequestMapping(value = { "list", "" })
    public String list(SwmBeaconColorConfig swmBeaconColorConfig, Model model) {
        model.addAttribute("swmBeaconColorConfig", swmBeaconColorConfig);
        return "modules/swm/swmBeaconColorConfigList";
    }
    
    /**
     * 查询列表数据
     */
    @RequestMapping(value = "listData")
    @ResponseBody
    public Page<SwmBeaconColorConfig> listData(SwmBeaconColorConfig swmBeaconColorConfig, HttpServletRequest request, HttpServletResponse response) {
        swmBeaconColorConfig.setPage(new Page<>(request, response));
        Page<SwmBeaconColorConfig> page = swmBeaconColorConfigService.findPage(swmBeaconColorConfig);
        return page;
    }

    /**
     * 查看编辑表单
     */
    @RequestMapping(value = "form")
    public String form(SwmBeaconColorConfig swmBeaconColorConfig, Model model) {
        model.addAttribute("swmBeaconColorConfig", swmBeaconColorConfig);
        return "modules/swm/swmBeaconColorConfigForm";
    }

    /**
     * 保存信标颜色配置
     */
    @PostMapping(value = "save")
    @ResponseBody
    public String save(@Validated SwmBeaconColorConfig swmBeaconColorConfig) {
        swmBeaconColorConfigService.save(swmBeaconColorConfig);
        return renderResult(Global.TRUE, text("保存信标颜色配置成功！"));
    }
    
    /**
     * 删除信标颜色配置
     */
    @DeleteMapping(value = "delete")
    @ResponseBody
    public String delete(@RequestParam(required = false) String id, SwmBeaconColorConfig swmBeaconColorConfig, HttpServletRequest request) {
        System.out.println("===== 删除接口被调用 =====");
        System.out.println("请求方法: " + request.getMethod());
        System.out.println("请求URL: " + request.getRequestURL() + "?" + request.getQueryString());
        System.out.println("@RequestParam接收到的id参数: " + id);
        
        // 确保获取到ID参数
        if ((id == null || id.isEmpty()) && request != null) {
            id = request.getParameter("id");
            System.out.println("从request.getParameter获取的id: " + id);
        }
        
        if (id != null && !id.isEmpty()) {
            swmBeaconColorConfig = new SwmBeaconColorConfig(id);
        }
        
        // 确保对象不为空且id不为空
        if (swmBeaconColorConfig == null || swmBeaconColorConfig.getId() == null || swmBeaconColorConfig.getId().isEmpty()) {
            return renderResult(Global.FALSE, text("删除失败：ID不能为空！"));
        }
        
        System.out.println("即将删除的ID: " + swmBeaconColorConfig.getId());
        swmBeaconColorConfigService.delete(swmBeaconColorConfig);
        return renderResult(Global.TRUE, text("删除信标颜色配置成功！"));
    }
    
    /**
     * 删除信标颜色配置 - 专门处理JSON格式请求
     */
    @RequestMapping(value = "deleteJson", method = {RequestMethod.DELETE, RequestMethod.POST})
    @ResponseBody
    public String deleteJson(@RequestBody(required = false) Map<String, Object> params, @RequestParam(required = false) String id, HttpServletRequest request) {
        // 尝试从JSON请求体获取ID
        if (params != null && params.get("id") != null) {
            id = params.get("id").toString();
            System.out.println("JSON请求中接收到的ID: " + id);
        }
        
        // 如果JSON中没有ID，尝试从URL参数获取
        if ((id == null || id.isEmpty()) && request != null) {
            id = request.getParameter("id");
            System.out.println("从URL参数获取的ID: " + id);
        }
        
        if (id == null || id.isEmpty()) {
            return renderResult(Global.FALSE, text("删除失败：ID不能为空！"));
        }
        
        SwmBeaconColorConfig swmBeaconColorConfig = new SwmBeaconColorConfig(id);
        swmBeaconColorConfigService.delete(swmBeaconColorConfig);
        return renderResult(Global.TRUE, text("删除信标颜色配置成功！"));
    }
    
    /**
     * 批量删除信标颜色配置
     */
    @RequestMapping(value = "deleteAll")
    @ResponseBody
    public String deleteAll(String ids) {
        String[] idArray = ids.split(",");
        for (String id : idArray) {
            SwmBeaconColorConfig swmBeaconColorConfig = swmBeaconColorConfigService.get(id);
            if (swmBeaconColorConfig != null) {
                swmBeaconColorConfigService.delete(swmBeaconColorConfig);
            }
        }
        return renderResult(Global.TRUE, text("批量删除信标颜色配置成功！"));
    }
    
    /**
     * 获取信标颜色配置列表（用于下拉选择）
     */
    @RequestMapping(value = "getConfigList")
    @ResponseBody
    public List<Map<String, Object>> getConfigList() {
        List<Map<String, Object>> result = new ArrayList<>();
        
        List<SwmBeaconColorConfig> configList = swmBeaconColorConfigService.findList(new SwmBeaconColorConfig());
        for (SwmBeaconColorConfig config : configList) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", config.getId());
            map.put("name", config.getName());
            map.put("color", config.getColor());
            
            result.add(map);
        }
        
        return result;
    }
} 