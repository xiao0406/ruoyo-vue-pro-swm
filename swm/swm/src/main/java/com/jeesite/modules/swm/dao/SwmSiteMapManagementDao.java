package com.jeesite.modules.swm.dao;

import java.util.List;

import com.jeesite.common.dao.CrudDao;
import com.jeesite.common.mybatis.annotation.MyBatisDao;
import com.jeesite.modules.swm.entity.SwmSiteMapManagement;
import org.apache.ibatis.annotations.Param;

/**
 * 场地底图管理表DAO接口
 * 
 * @author zwf
 * @version 2025-05-30
 */
@MyBatisDao
public interface SwmSiteMapManagementDao extends CrudDao<SwmSiteMapManagement> {
    
    /**
     * 更新底图状态
     * @param swmSiteMapManagement 实体对象
     * @return 影响行数
     */
    long updateMapStatus(SwmSiteMapManagement swmSiteMapManagement);
    
    /**
     * 物理删除底图
     * @param swmSiteMapManagement 实体对象
     * @return 影响行数
     */
    long physicalDelete(SwmSiteMapManagement swmSiteMapManagement);
    
    /**
     * 查询启用状态的地图
     * @return 启用状态的地图实体
     */
    SwmSiteMapManagement findActiveMap();

    /**
     * 查询指定父节点下的直接子节点列表
     * @param parentId 父节点ID
     * @return 子节点列表，按sort_order升序
     * @author Shawn @date 2026-04-02
     */
    List<SwmSiteMapManagement> findChildren(@Param("parentId") String parentId);

    /**
     * 统计指定父节点下的子节点数量（删除前校验用）
     * @param parentId 父节点ID
     * @return 子节点数量
     * @author Shawn @date 2026-04-02
     */
    int countChildren(@Param("parentId") String parentId);
}