/**
 * 语音模板表控制器
 * @author zwf
 * @date 2024-05-29
 */
package com.jeesite.modules.swm.web;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.jeesite.modules.swm.cache.SwmVoiceTemplateCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.jeesite.common.config.Global;
import com.jeesite.common.entity.Page;
import com.jeesite.common.web.BaseController;
import com.jeesite.modules.swm.entity.SwmPerson;
import com.jeesite.modules.swm.entity.SwmVoiceTemplate;
import com.jeesite.modules.swm.service.SwmPersonService;
import com.jeesite.modules.swm.service.SwmVoiceTemplateService;

/**
 * 语音模板表控制器
 * 
 * @author zwf
 */
@Controller
@RequestMapping(value = "${adminPath}/swmVoiceTemplate")
public class SwmVoiceTemplateController extends BaseController {

    @Autowired
    private SwmVoiceTemplateService swmVoiceTemplateService;
    
    @Autowired
    private SwmPersonService swmPersonService;
    @Autowired
    @Lazy
    private SwmVoiceTemplateCache swmVoiceTemplateCache;

    /**
     * 获取数据
     */
    @ModelAttribute
    public SwmVoiceTemplate get(String id, boolean isNewRecord) {
        SwmVoiceTemplate voiceTemplate = null;
        if (id != null && !id.isEmpty()) {
            voiceTemplate = swmVoiceTemplateService.get(id);
        }
        if (voiceTemplate == null) {
            voiceTemplate = new SwmVoiceTemplate();
        }
        return voiceTemplate;
    }

    /**
     * 查询列表
     */
    @RequestMapping(value = { "list", "" })
    public String list(SwmVoiceTemplate swmVoiceTemplate, Model model) {
        model.addAttribute("swmVoiceTemplate", swmVoiceTemplate);
        return "modules/swm/swmVoiceTemplateList";
    }

    /**
     * 查询列表数据
     */
    @RequestMapping(value = "listData")
    @ResponseBody
    public Page<SwmVoiceTemplate> listData(SwmVoiceTemplate swmVoiceTemplate, HttpServletRequest request, HttpServletResponse response) {
        // 如果前端没有传status参数，则查询所有状态的数据
        String statusParam = request.getParameter("status");
        if (statusParam == null || statusParam.isEmpty()) {
            // 彻底禁用status过滤，使用自定义SQL查询
            return swmVoiceTemplateService.findPageWithoutStatusFilter(new Page<>(request, response), swmVoiceTemplate);
        }
        // 否则使用前端传递的status值进行查询
        Page<SwmVoiceTemplate> page = swmVoiceTemplateService.findPage(new Page<>(request, response), swmVoiceTemplate);
        return page;
    }
    
    /**
     * 获取所有模板名称和ID
     */
    @RequestMapping(value = "getAllTemplateNames")
    @ResponseBody
    public List<Map<String, String>> getAllTemplateNames() {
        // 创建空的模板对象，用于查询所有模板
        SwmVoiceTemplate template = new SwmVoiceTemplate();
        // 获取所有模板数据
        List<SwmVoiceTemplate> list = swmVoiceTemplateService.findList(template);
        // 提取templateName和id
        return list.stream()
                .map(item -> {
                    Map<String, String> map = new HashMap<>();
                    map.put("id", item.getId());
                    map.put("templateName", item.getTemplateName());
                    return map;
                })
                .collect(java.util.stream.Collectors.toList());
    }
    
    /**
     * 查询状态为0的所有数据
     */
    @RequestMapping(value = "findStatusZero")
    @ResponseBody
    public List<SwmVoiceTemplate> findStatusZero(SwmVoiceTemplate swmVoiceTemplate) {
        return swmVoiceTemplateService.findListWithStatusZero(swmVoiceTemplate);
    }
    
    /**
     * 获取所有人员数据
     */
    @RequestMapping(value = "getAllPersons")
    @ResponseBody
    public Map<String, Object> getAllPersons() {
        Map<String, Object> result = new HashMap<>();
        try {
            // 创建空的SwmPerson对象用于查询所有数据
            SwmPerson query = new SwmPerson();
            // 查询所有人员数据
            List<SwmPerson> personList = swmPersonService.findList(query);
            
            // 处理每个人员对象，确保包含所有字段及文本显示值
            List<Map<String, Object>> enhancedList = personList.stream()
                    .map(person -> {
                        Map<String, Object> personMap = new HashMap<>();
                        // 基本属性
                        personMap.put("id", person.getId());
                        personMap.put("name", person.getName());
                        personMap.put("personType", person.getPersonType());
                        personMap.put("gender", person.getGender());
                        personMap.put("company", person.getCompany());
                        personMap.put("department", person.getDepartment());
                        personMap.put("workProcess", person.getWorkProcess());
                        personMap.put("team", person.getTeam());
                        personMap.put("jobType", person.getJobType());
                        personMap.put("safetyHelmetId", person.getSafetyHelmetId());
                        personMap.put("personnelStatus", person.getPersonnelStatus());
                        personMap.put("safetyEducation", person.getSafetyEducation());
                        personMap.put("identityCard", person.getIdentityCard());
                        personMap.put("phoneNumber", person.getPhoneNumber());
                        personMap.put("helmetReturned", person.getHelmetReturned());
                        personMap.put("departureType", person.getDepartureType());
                        personMap.put("departureReason", person.getDepartureReason());
                        personMap.put("departureDate", person.getDepartureDate());
                        
                        // 日期属性
                        personMap.put("createDate", person.getCreateDate());
                        personMap.put("updateDate", person.getUpdateDate());
                        
                        // 创建者/更新者
                        personMap.put("createBy", person.getCreateBy());
                        personMap.put("updateBy", person.getUpdateBy());
                        
                        // 备注
                        personMap.put("remarks", person.getRemarks());
                        
                        // 状态
                        personMap.put("status", person.getStatus());
                        
                        // 枚举文本值
                        personMap.put("personnelStatusText", person.getPersonnelStatusText());
                        personMap.put("safetyEducationText", person.getSafetyEducationText());
                        personMap.put("helmetReturnedText", person.getHelmetReturnedText());
                        if (person.getDepartureType() != null) {
                            personMap.put("departureTypeText", person.getDepartureTypeText());
                        }
                        
                        return personMap;
                    })
                    .collect(java.util.stream.Collectors.toList());
            
            result.put("success", true);
            result.put("data", enhancedList);
            result.put("total", enhancedList.size());
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "获取人员数据失败：" + e.getMessage());
            logger.error("获取人员数据失败", e);
        }
        return result;
    }

    /**
     * 查看编辑表单
     */
    @RequestMapping(value = "form")
    public String form(SwmVoiceTemplate swmVoiceTemplate, Model model) {
        model.addAttribute("swmVoiceTemplate", swmVoiceTemplate);
        return "modules/swm/swmVoiceTemplateForm";
    }

    /**
     * 保存语音模板
     */
    @PostMapping(value = "save")
    @ResponseBody
    public String save(@Validated SwmVoiceTemplate swmVoiceTemplate) {
        // 检查模板名称是否存在（而非模板代码）
        SwmVoiceTemplate query = new SwmVoiceTemplate();
        query.setTemplateName(swmVoiceTemplate.getTemplateName());
        List<SwmVoiceTemplate> existList = swmVoiceTemplateService.findList(query);
        for (SwmVoiceTemplate exist : existList) {
            if (!exist.getId().equals(swmVoiceTemplate.getId())) {
                return renderResult(Global.FALSE, text("保存失败！模板名称 [" + swmVoiceTemplate.getTemplateName() + "] 已存在"));
            }
        }
        // 执行保存
        swmVoiceTemplateService.save(swmVoiceTemplate);
        return renderResult(Global.TRUE, text("保存语音模板成功！"));
    }

    /**
     * 删除语音模板
     */
    @RequestMapping(value = "delete")
    @ResponseBody
    public String delete(SwmVoiceTemplate swmVoiceTemplate) {
        System.out.println("11111"+swmVoiceTemplate.getId());
        swmVoiceTemplateService.deletePhysical(swmVoiceTemplate);

        //清理缓存
        swmVoiceTemplateCache.delete(swmVoiceTemplate);
        return renderResult(Global.TRUE, text("删除语音模板成功！"));
    }
    
    /**
     * 根据模板代码获取模板
     */
    @RequestMapping(value = "getByTemplateCode")
    @ResponseBody
    public Map<String, Object> getByTemplateCode(@RequestParam("templateCode") String templateCode) {
        Map<String, Object> result = new HashMap<>();
        
        SwmVoiceTemplate template = swmVoiceTemplateService.getByTemplateCode(templateCode);
        if (template != null) {
            result.put("success", true);
            result.put("data", template);
        } else {
            result.put("success", false);
            result.put("message", "未找到对应模板");
        }
        
        return result;
    }
    
    /**
     * 检查模板代码是否已存在
     */
    @RequestMapping(value = "checkTemplateCodeExists")
    @ResponseBody
    public Map<String, Object> checkTemplateCodeExists(@RequestParam("templateCode") String templateCode, @RequestParam(value = "id", required = false) String id) {
        Map<String, Object> result = new HashMap<>();
        
        SwmVoiceTemplate existingTemplate = swmVoiceTemplateService.getByTemplateCode(templateCode);
        if (existingTemplate != null && !existingTemplate.getId().equals(id)) {
            result.put("exists", true);
            result.put("message", "模板代码已存在");
        } else {
            result.put("exists", false);
        }
        
        return result;
    }
    
    /**
     * 更新模板状态（启用/禁用）
     */
    @RequestMapping(value = "updateStatus")
    @ResponseBody
    public String updateStatus(@RequestParam("id") String id, @RequestParam("status") String status) {
        if (id == null || id.isEmpty()) {
            return renderResult(Global.FALSE, text("更新状态失败！ID不能为空"));
        }
        
        if (!"0".equals(status) && !"1".equals(status)) {
            return renderResult(Global.FALSE, text("更新状态失败！状态值无效"));
        }
        
        try {
            // 获取现有模板
            SwmVoiceTemplate template = swmVoiceTemplateService.get(id);
            if (template == null) {
                return renderResult(Global.FALSE, text("更新状态失败！未找到对应模板"));
            }
            
            // 直接使用SQL更新状态
            swmVoiceTemplateService.updateStatus(id, status);

            // 清理缓存
            if ("1".equals(status)){
                swmVoiceTemplateCache.delete(template);
            }else {
                swmVoiceTemplateCache.initAreaCache();
            }

            
            return renderResult(Global.TRUE, text((status.equals("0") ? "启用" : "禁用") + "模板成功！"));
        } catch (Exception e) {
            logger.error("更新模板状态失败", e);
            return renderResult(Global.FALSE, text("更新状态失败！" + e.getMessage()));
        }
    }
} 