/**
 * 一键召回记录表控制器
 * @author zwf
 * @date 2024-05-30
 */
package com.jeesite.modules.swm.web;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.jeesite.common.config.Global;
import com.jeesite.common.entity.Page;
import com.jeesite.common.web.BaseController;
import com.jeesite.modules.swm.entity.SwmOneClickRecall;
import com.jeesite.modules.swm.entity.SwmVoiceTemplate;
import com.jeesite.modules.swm.service.SwmOneClickRecallService;
import com.jeesite.modules.swm.service.SwmVoiceTemplateService;

/**
 * 一键召回记录表控制器
 * 
 * @author auto
 */
@Controller
@RequestMapping(value = "${adminPath}/swmOneClickRecall")
public class SwmOneClickRecallController extends BaseController {

    @Autowired
    private SwmOneClickRecallService swmOneClickRecallService;
    
    @Autowired
    private SwmVoiceTemplateService swmVoiceTemplateService;

    /**
     * 获取数据
     */
    @ModelAttribute
    public SwmOneClickRecall get(String id, boolean isNewRecord) {
        SwmOneClickRecall oneClickRecall = null;
        if (id != null && !id.isEmpty()) {
            oneClickRecall = swmOneClickRecallService.get(id);
        }
        if (oneClickRecall == null) {
            oneClickRecall = new SwmOneClickRecall();
        }
        return oneClickRecall;
    }

    /**
     * 查询列表
     */
    @RequestMapping(value = { "list", "" })
    public String list(SwmOneClickRecall swmOneClickRecall, Model model) {
        model.addAttribute("swmOneClickRecall", swmOneClickRecall);
        return "modules/swm/swmOneClickRecallList";
    }

    /**
     * 查询列表数据
     */
    @RequestMapping(value = "listData")
    @ResponseBody
    public Page<SwmOneClickRecall> listData(SwmOneClickRecall swmOneClickRecall, HttpServletRequest request, HttpServletResponse response) {
        Page<SwmOneClickRecall> page = swmOneClickRecallService.findPage(new Page<>(request, response), swmOneClickRecall);
        return page;
    }

    /**
     * 查看编辑表单
     */
    @RequestMapping(value = "form")
    public String form(SwmOneClickRecall swmOneClickRecall, Model model) {
        model.addAttribute("swmOneClickRecall", swmOneClickRecall);
        return "modules/swm/swmOneClickRecallForm";
    }

    /**
     * 保存一键召回记录
     */
    @PostMapping(value = "save")
    @ResponseBody
    public String save(@Validated SwmOneClickRecall swmOneClickRecall) {
        // 设置召回时间
        if (swmOneClickRecall.getRecallTime() == null) {
            swmOneClickRecall.setRecallTime(new Date());
        }
        // 如果未设置召回结果，则默认为进行中
        if (swmOneClickRecall.getRecallResult() == null || swmOneClickRecall.getRecallResult().isEmpty()) {
            swmOneClickRecall.setRecallResult(SwmOneClickRecall.RecallResultEnum.IN_PROGRESS);
        }
        
        swmOneClickRecallService.save(swmOneClickRecall);
        return renderResult(Global.TRUE, text("保存一键召回记录成功！"));
    }

    /**
     * 删除一键召回记录
     */
    @RequestMapping(value = "delete")
    @ResponseBody
    public String delete(SwmOneClickRecall swmOneClickRecall) {
        swmOneClickRecallService.delete(swmOneClickRecall);
        return renderResult(Global.TRUE, text("删除一键召回记录成功！"));
    }
    
    /**
     * 批量删除
     */
    @RequestMapping(value = "deleteAll")
    @ResponseBody
    public String deleteAll(String ids) {
        String[] idArray = ids.split(",");
        swmOneClickRecallService.deleteAll(idArray);
        return renderResult(Global.TRUE, text("删除一键召回记录成功！"));
    }
    
    /**
     * 获取语音模板详情
     */
    @RequestMapping(value = "getTemplateDetails")
    @ResponseBody
    public Map<String, Object> getTemplateDetails(@RequestParam("templateId") String templateId) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            SwmVoiceTemplate template = swmVoiceTemplateService.get(templateId);
            if (template != null) {
                result.put("success", true);
                result.put("templateName", template.getTemplateName());
                result.put("content", template.getContent());
            } else {
                result.put("success", false);
                result.put("message", "未找到对应模板");
            }
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "获取模板详情失败: " + e.getMessage());
            logger.error("获取模板详情失败", e);
        }
        
        return result;
    }
    
    /**
     * 获取所有召回记录
     */
    @RequestMapping(value = "getAllRecalls")
    @ResponseBody
    public Map<String, Object> getAllRecalls() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            SwmOneClickRecall query = new SwmOneClickRecall();
            List<SwmOneClickRecall> recallList = swmOneClickRecallService.findList(query);
            
            result.put("success", true);
            result.put("data", recallList);
            result.put("total", recallList.size());
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "获取召回记录失败: " + e.getMessage());
            logger.error("获取召回记录失败", e);
        }
        
        return result;
    }
    
    /**
     * 更新召回结果
     */
    @PostMapping(value = "updateRecallResult")
    @ResponseBody
    public String updateRecallResult(@RequestBody Map<String, String> params) {
        String id = params.get("id");
        String recallResult = params.get("recallResult");
        
        if (id == null || id.isEmpty() || recallResult == null || recallResult.isEmpty()) {
            return renderResult(Global.FALSE, text("参数错误！"));
        }
        
        try {
            SwmOneClickRecall recall = swmOneClickRecallService.get(id);
            if (recall == null) {
                return renderResult(Global.FALSE, text("未找到记录！"));
            }
            
            recall.setRecallResult(recallResult);
            swmOneClickRecallService.save(recall);
            
            return renderResult(Global.TRUE, text("更新召回结果成功！"));
        } catch (Exception e) {
            logger.error("更新召回结果失败", e);
            return renderResult(Global.FALSE, text("更新召回结果失败：" + e.getMessage()));
        }
    }
} 