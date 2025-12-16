package com.jeesite.modules.swm.service.impl;

import java.util.*;
import java.util.stream.Collectors;

import com.alibaba.fastjson.JSON;
import com.jeesite.common.constant.RabbitMQConstant;
import com.jeesite.modules.swm.mq.SwmQueueKey;
import com.jeesite.modules.swm.mq.producer.RabbitMqSender;
import com.jeesite.modules.swm.param.SwmOneClickRecallSaveParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jeesite.common.entity.Page;
import com.jeesite.common.service.CrudService;
import com.jeesite.modules.swm.dao.SwmOneClickRecallDao;
import com.jeesite.modules.swm.entity.SwmOneClickRecall;
import com.jeesite.modules.swm.service.SwmOneClickRecallService;

/**
 * 一键召回记录表服务实现类
 * 
 * @author zwf
 */
@Service
@Transactional(readOnly = true)
public class SwmOneClickRecallServiceImpl extends CrudService<SwmOneClickRecallDao, SwmOneClickRecall> implements SwmOneClickRecallService {

    @Autowired
    private SwmOneClickRecallDao swmOneClickRecallDao;
    @Autowired
    private RabbitMqSender rabbitMqSender;

    @Override
    public SwmOneClickRecall get(String id) {
        return super.get(id);
    }
    
    @Override
    public SwmOneClickRecall get(SwmOneClickRecall oneClickRecall) {
        return super.get(oneClickRecall);
    }

    /**
     * 查询分页数据
     */
    @Override
    public Page<SwmOneClickRecall> findPage(SwmOneClickRecall oneClickRecall) {
        return super.findPage(oneClickRecall);
    }

    /**
     * 查询分页数据（带页面参数）
     */
    @Override
    public Page<SwmOneClickRecall> findPage(Page<SwmOneClickRecall> page, SwmOneClickRecall oneClickRecall) {
        // 设置分页参数
        oneClickRecall.setPage(page);
        // 执行查询
        return this.findPage(oneClickRecall);
    }

    @Override
    public List<SwmOneClickRecall> findList(SwmOneClickRecall oneClickRecall) {
        return super.findList(oneClickRecall);
    }

    @Override
    @Transactional(readOnly = false)
    public Map<String, Object> addRecallRecord(SwmOneClickRecallSaveParam param) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "一键召回指令下发成功");
        // 保存召回记录
        String evacuationPlan = param.getEvacuationPlan();
        SwmOneClickRecall swmOneClickRecall = new SwmOneClickRecall();
        swmOneClickRecall.setVoiceText(param.getVoiceText());
        swmOneClickRecall.setTemplateName(param.getTemplateName());
        swmOneClickRecall.setTemplateContent(param.getTemplateContent());
        swmOneClickRecall.setEvacuationPlan(evacuationPlan);
        swmOneClickRecall.setRecallTime(new Date());
        swmOneClickRecall.setRecallResult(param.getRecallResult());
        swmOneClickRecall.setPushMethod(param.getPushMethod());
        swmOneClickRecall.setRecallFrequency(param.getRecallFrequency());
        swmOneClickRecall.setRecallCount(param.getRecallCount());
        swmOneClickRecall.setRecallSuccessCount(0);
        swmOneClickRecall.setRecallFailCount(0);
        // 非全体人员撤离
        if(!evacuationPlan.equals(SwmOneClickRecall.EvacuationPlanEnum.ALL) && !evacuationPlan.equals(SwmOneClickRecall.EvacuationPlanEnum.BY_PERSON_TYPE)){
            List<String> selectedTargets = param.getSelectedTargets();
            List<Map<String, Object>> originalTreeData = param.getOriginalTreeData();
            List<Map<String, Object>> deviceList = sendRecallToSelectedTargets(selectedTargets, originalTreeData);
            if(Objects.isNull(deviceList) || deviceList.isEmpty()){
                result.put("success", false);
                result.put("message", "未找到推送目标");
                return result;
            }else{
                swmOneClickRecall.setDeviceList(JSON.toJSONString(deviceList));
                swmOneClickRecall.setEvacueeCount(deviceList.size());
            }
        }else if (!evacuationPlan.equals(SwmOneClickRecall.EvacuationPlanEnum.BY_PERSON_TYPE)){
            // 全体人员撤离
            List<Map<String, Object>> allTargetPersonnel = swmOneClickRecallDao.findAllTargetPersonnelForBroadcast();
            if(Objects.isNull(allTargetPersonnel) || allTargetPersonnel.isEmpty()){
                result.put("success", false);
                result.put("message", "未找到推送目标");
                return result;
            }else {
                swmOneClickRecall.setDeviceList(JSON.toJSONString(allTargetPersonnel));
                swmOneClickRecall.setEvacueeCount(allTargetPersonnel.size());
            }
        }

        //人员类型撤回
        if(evacuationPlan.equals(SwmOneClickRecall.EvacuationPlanEnum.BY_PERSON_TYPE)){
            List<String> personTypeList = param.getSelectedTargets();
            List<Map<String, Object>> allTargetPersonnel = swmOneClickRecallDao.findAllTargetPersonnelForBroadcastByPersonType(personTypeList);
            if(Objects.isNull(allTargetPersonnel) || allTargetPersonnel.isEmpty()){
                result.put("success", false);
                result.put("message", "未找到推送目标");
                return result;
            }else {
                swmOneClickRecall.setDeviceList(JSON.toJSONString(allTargetPersonnel));
                swmOneClickRecall.setEvacueeCount(allTargetPersonnel.size());
            }
        }

        super.save(swmOneClickRecall);

        String pushMethods = swmOneClickRecall.getPushMethod();
        String[] methods = pushMethods.split(",");
        for (String method : methods) {
            if (method.equals(SwmOneClickRecall.PushMethodEnum.DEVICE)){
                // 设备推送，发送一条消息，用于召回消息推送
                rabbitMqSender.sendMessage(SwmQueueKey.SWM_RECALL_MESSAGE_PUSH, UUID.randomUUID().toString(), swmOneClickRecall);
            } else if (method.equals(SwmOneClickRecall.PushMethodEnum.SMS)) {
                // TODO 短信推送待实现
            }
        }
        return result;
    }

    @Override
    @Transactional(readOnly = false)
    public void save(SwmOneClickRecall oneClickRecall) {
        super.save(oneClickRecall);
    }

    @Override
    @Transactional(readOnly = false)
    public void delete(SwmOneClickRecall oneClickRecall) {
        super.delete(oneClickRecall);
    }
    
    @Override
    @Transactional(readOnly = false)
    public void deleteAll(String[] ids) {
        for (String id : ids) {
            SwmOneClickRecall oneClickRecall = new SwmOneClickRecall(id);
            this.delete(oneClickRecall);
        }
    }

    /**
     * 根据选中的节点向对应人员的设备发送召回消息
     *
     * @param selectedTargets 选中的目标节点列表
     */
    public List<Map<String, Object>> sendRecallToSelectedTargets(List<String> selectedTargets,
                                                           List<Map<String, Object>> originalTreeData) {
        logger.info("开始处理选中目标召回，目标数量: {}", selectedTargets.size());
        // 1. 解析选中的节点，获取节点信息
        List<Map<String, Object>> selectedNodes = parseSelectedNodes(selectedTargets, originalTreeData);
        logger.info("解析到的节点信息: {}", selectedNodes);

        // 2. 根据节点信息查询对应的人员
        List<Map<String, Object>> personnelList = swmOneClickRecallDao.findPersonnelByNodes(selectedNodes);
        logger.info("查询到的人员数量: {}", personnelList.size());
        if (personnelList.isEmpty()) {
            return null;
        }

        // 3. 提取身份证号码列表
        List<String> identityCards = personnelList.stream()
                .map(person -> (String) person.get("identityCard"))
                .filter(idCard -> idCard != null && !idCard.trim().isEmpty())
                .collect(Collectors.toList());
        logger.info("提取到身份证号码数量: {}", identityCards.size());
        if (identityCards.isEmpty()) {
            return null;
        }

        // 4. 根据身份证号码查询对应的设备
        List<Map<String, Object>> deviceList = swmOneClickRecallDao.findDevicesByIdentityCards(identityCards);
        logger.info("查询到的设备数量: {}", deviceList.size());
        if (deviceList.isEmpty()) {
            return null;
        }
        return deviceList;
    }

    /**
     * 解析选中的节点，从原始树数据中获取节点的详细信息
     *
     * @param selectedTargets  选中的目标ID列表
     * @param originalTreeData 原始树形数据
     * @return 节点信息列表，包含nodeType、id、idCard等信息
     */
    private List<Map<String, Object>> parseSelectedNodes(List<String> selectedTargets,
                                                         List<Map<String, Object>> originalTreeData) {
        List<Map<String, Object>> selectedNodes = new ArrayList<>();
        for (String targetId : selectedTargets) {
            Map<String, Object> nodeInfo = findNodeById(targetId, originalTreeData);
            if (nodeInfo != null) {
                selectedNodes.add(nodeInfo);
            }
        }
        return selectedNodes;
    }

    /**
     * 递归查找指定ID的节点信息
     *
     * @param targetId 目标节点ID
     * @param treeData 树形数据
     * @return 节点信息
     */
    private Map<String, Object> findNodeById(String targetId, List<Map<String, Object>> treeData) {
        if (treeData == null) {
            return null;
        }

        for (Map<String, Object> node : treeData) {
            String nodeId = (String) node.get("id");
            if (targetId.equals(nodeId)) {
                // 构建节点信息
                Map<String, Object> nodeInfo = new HashMap<>();
                nodeInfo.put("id", node.get("id"));
                nodeInfo.put("nodeType", node.get("nodeType"));
                nodeInfo.put("title", node.get("title"));
                nodeInfo.put("idCard", node.get("idCard"));
                return nodeInfo;
            }

            // 递归查找子节点
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> children = (List<Map<String, Object>>) node.get("children");
            if (children != null) {
                Map<String, Object> childResult = findNodeById(targetId, children);
                if (childResult != null) {
                    return childResult;
                }
            }
        }
        return null;
    }





} 