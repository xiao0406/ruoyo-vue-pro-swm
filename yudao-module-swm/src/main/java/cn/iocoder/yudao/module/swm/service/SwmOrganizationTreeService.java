package cn.iocoder.yudao.module.swm.service;

import cn.iocoder.yudao.module.swm.dal.dataobject.TreeNode;

import java.util.List;

/**
 * SWM 组织树服务。
 *
 * <p>用于兼容旧 JeeSite 前端迁移过来的组织树接口，节点层级为：
 * 厂间 -> 车间 -> 产线 -> 班组 -> 人员。</p>
 */
public interface SwmOrganizationTreeService {

    /**
     * 获取指定层级的组织树节点。
     *
     * @param nodeType 节点类型：root、office、workshop、prodLine、workGroup
     * @param parentId 父节点 ID，查询 root 时可为空
     * @param personTypeList 人员类型过滤，仅查询人员节点时使用
     * @return 组织树节点列表
     */
    List<TreeNode> getNodes(String nodeType, String parentId, List<String> personTypeList);

}
