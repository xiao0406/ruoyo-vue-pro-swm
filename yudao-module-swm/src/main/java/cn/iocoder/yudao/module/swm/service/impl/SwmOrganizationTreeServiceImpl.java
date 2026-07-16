package cn.iocoder.yudao.module.swm.service.impl;

import cn.iocoder.yudao.module.swm.dal.dataobject.TreeNode;
import cn.iocoder.yudao.module.swm.dal.mysql.SwmOrganizationTreeMapper;
import cn.iocoder.yudao.module.swm.service.SwmOrganizationTreeService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * SWM 组织树服务实现。
 */
@Service
@Transactional(readOnly = true)
public class SwmOrganizationTreeServiceImpl implements SwmOrganizationTreeService {

    @Resource
    private SwmOrganizationTreeMapper organizationTreeMapper;

    @Override
    public List<TreeNode> getNodes(String nodeType, String parentId, List<String> personTypeList) {
        if (nodeType == null) {
            return new ArrayList<>();
        }
        return switch (nodeType) {
            case "root" -> fillNodeFields(organizationTreeMapper.getOfficeNodes(), "office", false);
            case "office" -> fillNodeFields(organizationTreeMapper.getWorkshopNodes(parentId), "workshop", false);
            case "workshop" -> fillNodeFields(organizationTreeMapper.getProdLineNodes(parentId), "prodLine", false);
            case "prodLine" -> fillNodeFields(organizationTreeMapper.getWorkGroupNodes(parentId), "workGroup", false);
            case "workGroup" -> fillNodeFields(organizationTreeMapper.getWorkerNodes(parentId, personTypeList), "worker", true);
            default -> new ArrayList<>();
        };
    }

    private List<TreeNode> fillNodeFields(List<TreeNode> nodes, String nodeType, boolean leaf) {
        if (nodes == null) {
            return new ArrayList<>();
        }
        for (TreeNode node : nodes) {
            node.setValue(node.getId());
            node.setKey(node.getId());
            node.setNodeType(nodeType);
            node.setLeaf(leaf);
            node.setIsLeaf(leaf);
        }
        return nodes;
    }

}
