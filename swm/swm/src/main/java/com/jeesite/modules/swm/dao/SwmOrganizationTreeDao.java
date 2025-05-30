package com.jeesite.modules.swm.dao;

import com.jeesite.common.dao.CrudDao;
import com.jeesite.common.mybatis.annotation.MyBatisDao;
import com.jeesite.modules.swm.entity.TreeNode;

import java.util.List;
import org.apache.ibatis.annotations.Param;

/**
 * 组织树数据访问接口
 * 
 * @author AI-Generated
 * @version 2025-05-28
 */
@MyBatisDao
public interface SwmOrganizationTreeDao {

    /**
     * 获取厂间节点列表
     * 
     * @return 厂间节点列表
     */
    List<TreeNode> getOfficeNodes();
    
    /**
     * 获取车间节点列表
     * 
     * @param officeId 厂间ID
     * @return 车间节点列表
     */
    List<TreeNode> getWorkshopNodes(@Param("officeId") String officeId);
    
    /**
     * 获取产线节点列表
     * 
     * @param workshopId 车间ID
     * @return 产线节点列表
     */
    List<TreeNode> getProdLineNodes(@Param("workshopId") String workshopId);
    
    /**
     * 获取班组节点列表
     * 
     * @param prodLineId 产线ID
     * @return 班组节点列表
     */
    List<TreeNode> getWorkGroupNodes(@Param("prodLineId") String prodLineId);
} 