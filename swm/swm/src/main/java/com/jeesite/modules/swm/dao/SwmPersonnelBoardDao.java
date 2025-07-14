package com.jeesite.modules.swm.dao;

import com.jeesite.common.dao.CrudDao;
import com.jeesite.common.mybatis.annotation.MyBatisDao;
import com.jeesite.modules.swm.entity.SwmPersonnelBoard;

import java.util.List;

/**
 * 人员看板表DAO接口
 * 
 * @author zwf
 * @version 2025-05-15
 */
@MyBatisDao
public interface SwmPersonnelBoardDao extends CrudDao<SwmPersonnelBoard> {
    
    /**
     * 查询休闲区域ID列表（area_type=3的区域）
     * 
     * @return 休闲区域ID列表
     */
    List<String> findLeisureAreaIds();
} 