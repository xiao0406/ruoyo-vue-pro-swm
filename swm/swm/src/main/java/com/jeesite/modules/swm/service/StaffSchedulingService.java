package com.jeesite.modules.swm.service;

import com.jeesite.modules.swm.dao.StaffSchedulingDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 人员排班Service
 * 
 * @author Swm
 * @version 2023-07-01
 */
@Service
public class StaffSchedulingService {

    @Autowired
    private StaffSchedulingDao staffSchedulingDao;
    
    /**
     * 根据节点类型和ID获取人员列表
     * 
     * @param nodeType 节点类型(office, workshop, prodLine, workGroup)
     * @param id 节点ID
     * @return 人员列表数据
     */
    public List<Map<String, Object>> getPersonsByNodeType(String nodeType, String id) {
        List<Map<String, Object>> rawResult = new ArrayList<>();
        
        switch (nodeType) {
            case "office":
                // 查询office_code与id相等的数据
                rawResult = staffSchedulingDao.getPersonsByOffice(id);
                break;
            case "workshop":
                // 查询work_shop_code与id相等的数据
                rawResult = staffSchedulingDao.getPersonsByWorkshop(id);
                break;
            case "prodLine":
                // 处理产线相关逻辑
                rawResult = staffSchedulingDao.getPersonsByProdLine(id);
                break;
            case "workGroup":
                // 查询work_group_id与id相等的数据
                rawResult = staffSchedulingDao.getPersonsByWorkGroup(id);
                break;
            default:
                break;
        }
        
        // 处理数据，去除身份证号重复的记录，保留最新的
        return filterDuplicateIdentityCard(rawResult);
    }
    
    /**
     * 批量获取多个节点的人员列表
     * 
     * @param nodeTypes 节点类型数组
     * @param ids 节点ID数组
     * @return 合并后的人员列表数据
     */
    public List<Map<String, Object>> batchGetPersonsByNodeTypes(String[] nodeTypes, String[] ids) {
        List<Map<String, Object>> allPersons = new ArrayList<>();
        
        // 处理每个节点类型和ID
        for (int i = 0; i < nodeTypes.length; i++) {
            if (nodeTypes[i] != null && ids[i] != null) {
                // 跳过worker类型节点
                if ("worker".equals(nodeTypes[i])) {
                    continue;
                }
                
                // 获取当前节点的人员列表
                List<Map<String, Object>> currentPersons = getPersonsByNodeType(nodeTypes[i], ids[i]);
                if (currentPersons != null && !currentPersons.isEmpty()) {
                    allPersons.addAll(currentPersons);
                }
            }
        }
        
        // 处理数据，去除身份证号重复的记录，保留最新的
        return filterDuplicateIdentityCard(allPersons);
    }
    
    /**
     * 过滤重复身份证的数据，只保留每个身份证号最新的一条记录
     * 
     * @param personList 原始人员列表
     * @return 过滤后的人员列表
     */
    private List<Map<String, Object>> filterDuplicateIdentityCard(List<Map<String, Object>> personList) {
        // 使用Map来跟踪每个身份证号对应的最新记录
        Map<String, Map<String, Object>> identityCardMap = new HashMap<>();
        
        // 遍历所有人员数据
        for (Map<String, Object> person : personList) {
            String identityCard = (String) person.get("identityCard");
            
            // 如果身份证为空，则跳过
            if (identityCard == null || identityCard.trim().isEmpty()) {
                continue;
            }
            
            // 将数据放入Map中，如果有重复会覆盖之前的记录，从而保留最新的
            identityCardMap.put(identityCard, person);
        }
        
        // 将Map中的值转换回List
        return new ArrayList<>(identityCardMap.values());
    }
} 