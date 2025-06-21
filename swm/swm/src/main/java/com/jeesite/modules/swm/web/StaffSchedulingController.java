package com.jeesite.modules.swm.web;

import com.jeesite.common.web.BaseController;
import com.jeesite.modules.swm.entity.SwmPerson;
import com.jeesite.modules.swm.service.StaffSchedulingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 人员排班Controller
 * 
 * @author Swm
 * @version 2023-07-01
 */
@Controller
@RequestMapping(value = "${adminPath}/staffScheduling")
public class StaffSchedulingController extends BaseController {

    @Autowired
    private StaffSchedulingService staffSchedulingService;
    
    /**
     * 根据节点类型和ID获取人员列表
     * 
     * @param nodeType 节点类型(office, workshop, prodLine, workGroup)
     * @param id 节点ID
     * @return 人员列表数据
     */
    @RequestMapping(value = "getPersonsByNodeType")
    @ResponseBody
    public Map<String, Object> getPersonsByNodeType(
            @RequestParam("nodeType") String nodeType,
            @RequestParam("id") String id) {
        Map<String, Object> result = new HashMap<>();
        
        // 不处理worker类型的节点
        if ("worker".equals(nodeType)) {
            result.put("success", false);
            result.put("message", "不支持worker类型节点");
            return result;
        }
        
        try {
            List<Map<String, Object>> personList = staffSchedulingService.getPersonsByNodeType(nodeType, id);
            result.put("success", true);
            result.put("list", personList);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", e.getMessage());
        }
        
        return result;
    }
    
    /**
     * 批量获取多个节点的人员列表
     * 
     * @param nodeTypes 节点类型数组，逗号分隔 (office,workshop,prodLine,workGroup)
     * @param ids 节点ID数组，逗号分隔
     * @return 人员列表数据
     */
    @RequestMapping(value = "batchGetPersonsByNodeTypes")
    @ResponseBody
    public Map<String, Object> batchGetPersonsByNodeTypes(
            @RequestParam("nodeTypes") String nodeTypes,
            @RequestParam("ids") String ids) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 分割节点类型和ID字符串
            String[] nodeTypeArray = nodeTypes.split(",");
            String[] idArray = ids.split(",");
            
            // 确保数组长度匹配
            if (nodeTypeArray.length != idArray.length) {
                result.put("success", false);
                result.put("message", "节点类型和ID数量不匹配");
                return result;
            }
            
            // 批量获取人员列表并合并结果
            List<Map<String, Object>> personList = staffSchedulingService.batchGetPersonsByNodeTypes(nodeTypeArray, idArray);
            result.put("success", true);
            result.put("list", personList);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", e.getMessage());
        }
        
        return result;
    }
} 