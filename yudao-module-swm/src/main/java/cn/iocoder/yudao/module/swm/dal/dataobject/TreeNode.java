package cn.iocoder.yudao.module.swm.dal.dataobject;

import lombok.Data;

/**
 * 组织树节点 DO（用于 MyBatis resultMap 映射）
 */
@Data
public class TreeNode {
    private String id;
    private String parentId;
    private String title;
    private String idCard;
    private String personId;
}
