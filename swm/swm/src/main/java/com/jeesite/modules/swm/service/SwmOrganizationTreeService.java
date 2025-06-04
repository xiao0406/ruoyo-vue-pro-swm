package com.jeesite.modules.swm.service;

import com.jeesite.common.service.CrudService;
import com.jeesite.modules.swm.entity.TreeNode;
import com.jeesite.modules.swm.dao.SwmOrganizationTreeDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 组织树服务
 * 
 * @author zwf
 * @version 2025-05-28
 */
@Service
@Transactional(readOnly = true)
public class SwmOrganizationTreeService {

    @Autowired
    private SwmOrganizationTreeDao swmOrganizationTreeDao;

    /**
     * 获取组织树节点
     * 
     * @param nodeType 节点类型: root, office, workshop, prodLine, workGroup, worker
     * @param parentId 父节点ID
     * @return 树节点列表
     */
    public List<TreeNode> getNodes(String nodeType, String parentId) {
        List<TreeNode> nodes = new ArrayList<>();
        
        switch (nodeType) {
            case "root":
                // 当请求root节点时，返回厂间数据，厂间是最高级父节点
                nodes = getOfficeNodes();
                break;
            case "office":
                // 请求厂间的下级节点，返回车间数据
                nodes = getWorkshopNodes(parentId);
                break;
            case "workshop":
                // 请求车间的下级节点，返回产线数据
                nodes = getProdLineNodes(parentId);
                break;
            case "prodLine":
                // 请求产线的下级节点，返回班组数据
                nodes = getWorkGroupNodes(parentId);
                break;
            case "workGroup":
                // 请求班组的下级节点，返回员工数据
                nodes = getWorkerNodes(parentId);
                break;
            default:
                break;
        }
        
        return nodes;
    }

    /**
     * 获取厂间节点 - 这是组织树的最高级节点
     * 
     * @return 厂间节点列表
     */
    private List<TreeNode> getOfficeNodes() {
        List<TreeNode> nodes = swmOrganizationTreeDao.getOfficeNodes();
        
        // 设置节点属性
        for (TreeNode node : nodes) {
            node.setValue(node.getId());
            node.setKey(node.getId());
            node.setNodeType("office");
            node.setLeaf(false);
        }
        
        return nodes;
    }

    /**
     * 获取车间节点
     * 
     * @param officeId 厂间ID
     * @return 车间节点列表
     */
    private List<TreeNode> getWorkshopNodes(String officeId) {
        List<TreeNode> nodes = swmOrganizationTreeDao.getWorkshopNodes(officeId);
        
        // 设置节点属性
        for (TreeNode node : nodes) {
            node.setValue(node.getId());
            node.setKey(node.getId());
            node.setNodeType("workshop");
            node.setLeaf(false);
        }
        
        return nodes;
    }

    /**
     * 获取产线节点
     * 
     * @param workshopId 车间ID
     * @return 产线节点列表
     */
    private List<TreeNode> getProdLineNodes(String workshopId) {
        System.out.println("正在获取产线节点，车间ID: " + workshopId);
        
        List<TreeNode> nodes = swmOrganizationTreeDao.getProdLineNodes(workshopId);
        
        // 设置节点属性
        for (TreeNode node : nodes) {
            node.setValue(node.getId());
            node.setKey(node.getId());
            node.setNodeType("prodLine");
            node.setLeaf(false);
            
            System.out.println("创建产线节点: id=" + node.getId() + ", parentId=" + node.getParentId() + ", name=" + node.getTitle());
        }
        
        System.out.println("已获取产线节点数量: " + nodes.size());
        return nodes;
    }

    /**
     * 获取班组节点
     * 
     * @param prodLineId 产线ID
     * @return 班组节点列表
     */
    private List<TreeNode> getWorkGroupNodes(String prodLineId) {
        System.out.println("正在获取班组节点，产线ID: " + prodLineId);
        
        List<TreeNode> nodes = swmOrganizationTreeDao.getWorkGroupNodes(prodLineId);
        
        // 设置节点属性
        for (TreeNode node : nodes) {
            node.setValue(node.getId());
            node.setKey(node.getId());
            node.setNodeType("workGroup");
            node.setLeaf(false);  // 班组不再是叶子节点，因为下面还有员工
            
            System.out.println("创建班组节点: id=" + node.getId() + ", parentId=" + node.getParentId() + ", name=" + node.getTitle());
        }
        
        System.out.println("已获取班组节点数量: " + nodes.size());
        return nodes;
    }
    
    /**
     * 获取员工节点
     * 
     * @param workGroupId 班组ID
     * @return 员工节点列表
     */
    private List<TreeNode> getWorkerNodes(String workGroupId) {
        System.out.println("正在获取员工节点，班组ID: " + workGroupId);
        
        List<TreeNode> nodes = swmOrganizationTreeDao.getWorkerNodes(workGroupId);
        
        // 设置节点属性
        for (TreeNode node : nodes) {
            node.setValue(node.getId());
            node.setKey(node.getId());
            node.setNodeType("worker");
            node.setLeaf(true);  // 员工是叶子节点
            
            System.out.println("创建员工节点: id=" + node.getId() + ", parentId=" + node.getParentId() + ", name=" + node.getTitle());
        }
        
        System.out.println("已获取员工节点数量: " + nodes.size());
        return nodes;
    }
} 