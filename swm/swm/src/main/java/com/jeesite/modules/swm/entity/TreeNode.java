package com.jeesite.modules.swm.entity;

import java.io.Serializable;

/**
 * 树节点实体类
 * 
 * @author zwf
 * @version 2025-05-28
 */
public class TreeNode implements Serializable {

    private static final long serialVersionUID = 1L;

    private String id;        // 节点ID
    private String parentId;  // 父节点ID
    private String value;     // 节点值
    private String title;     // 节点标题
    private String key;       // 节点键
    private String nodeType;  // 节点类型: office(厂间), workshop(车间), prodLine(产线), workGroup(班组), worker(员工)
    private boolean isLeaf;   // 是否叶子节点
    private String idCard;    // 身份证号码（仅用于员工节点）

    public TreeNode() {
    }

    public TreeNode(String id, String parentId, String title, String nodeType) {
        this.id = id;
        this.parentId = parentId;
        this.title = title;
        this.value = id;
        this.key = id;
        this.nodeType = nodeType;

    }



    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getNodeType() {
        return nodeType;
    }

    public void setNodeType(String nodeType) {
        this.nodeType = nodeType;
    }

    public boolean isLeaf() {
        return isLeaf;
    }

    public void setLeaf(boolean leaf) {
        isLeaf = leaf;
    }
    
    public String getIdCard() {
        return idCard;
    }

    public void setIdCard(String idCard) {
        this.idCard = idCard;
    }
} 