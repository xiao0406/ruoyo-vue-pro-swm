package com.jeesite.modules.swm.dao;

import com.jeesite.common.dao.CrudDao;
import com.jeesite.common.mybatis.annotation.MyBatisDao;
import com.jeesite.modules.swm.entity.SwmHelmetSubitem;

import java.util.List;

/**
 * 安全帽子项颜色表DAO接口
 * 
 * @author zwf
 * @version 2025-05-20
 */
@MyBatisDao
public interface SwmHelmetSubitemDao extends CrudDao<SwmHelmetSubitem> {
    
    /**
     * 根据父ID查询子项列表
     * @param entity 包含父ID的实体对象
     * @return 子项列表
     */
    List<SwmHelmetSubitem> findByParentId(String parentId);
    
    /**
     * 根据父ID删除子项
     * @param parentId 父ID
     * @return 影响行数
     */
    int deleteByParentId(String parentId);
    
    /**
     * 获取父ID关联的子项数量
     * @param parentId 父ID
     * @return 子项数量
     */
    int getCountByParentId(String parentId);
} 