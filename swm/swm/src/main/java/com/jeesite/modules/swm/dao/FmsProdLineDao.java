package com.jeesite.modules.swm.dao;

import com.jeesite.common.dao.CrudDao;
import com.jeesite.common.mybatis.annotation.MyBatisDao;
import com.jeesite.modules.fms.entity.FmsProdLine;

/**
 * 生产线档案DAO接口
 * @author wxy
 * @version 2026-03-02
 */
@MyBatisDao
public interface FmsProdLineDao extends CrudDao<FmsProdLine> {

    FmsProdLine getByProdLineCode(String prodLineCode);
}