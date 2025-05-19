/**
 * 隐患信息DAO接口
 * @author Shawn
 * @date 2023-11-16
 */
package com.jeesite.modules.swm.dao;

import com.jeesite.common.dao.CrudDao;
import com.jeesite.common.mybatis.annotation.MyBatisDao;
import com.jeesite.modules.swm.entity.SwmHiddenDanger;

/**
 * 隐患信息DAO接口
 * 
 * @author Shawn
 * @date 2023-11-16
 */
@MyBatisDao
public interface SwmHiddenDangerDao extends CrudDao<SwmHiddenDanger> {

}