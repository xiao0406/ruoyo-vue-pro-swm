/**
 * 危险源信息DAO接口
 * @author Shawn
 * @version 2024-05-20
 */
package com.jeesite.modules.swm.dao;

import com.jeesite.common.dao.CrudDao;
import com.jeesite.common.mybatis.annotation.MyBatisDao;
import com.jeesite.modules.swm.entity.SwmHazardSource;

/**
 * 危险源信息DAO接口
 */
@MyBatisDao
public interface SwmHazardSourceDao extends CrudDao<SwmHazardSource> {

}