package cn.iocoder.yudao.module.swm.controller.admin.organization.vo;

import lombok.Data;
import java.util.List;

/**
 * 组织树节点 VO
 */
@Data
public class TreeNode {
    private String id;
    private String name;
    private String parentId;
    private String type;
    private List<TreeNode> children;
}
