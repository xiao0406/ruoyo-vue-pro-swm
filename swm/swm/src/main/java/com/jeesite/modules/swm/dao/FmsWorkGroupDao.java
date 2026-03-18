package com.jeesite.modules.swm.dao;

import com.jeesite.common.dao.CrudDao;
import com.jeesite.common.mybatis.annotation.MyBatisDao;
import com.jeesite.modules.fms.entity.FmsWorkGroup;

/**
 * 班组档案DAO接口
 * @author wxy
 * @version 2026-03-02
 */
@MyBatisDao
public interface FmsWorkGroupDao extends CrudDao<FmsWorkGroup> {

    FmsWorkGroup getEntityByCode(String workGroupCode);
}