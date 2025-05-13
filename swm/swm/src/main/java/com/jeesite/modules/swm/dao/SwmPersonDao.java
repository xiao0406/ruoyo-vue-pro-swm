/**
 * @author Shawn
 * @date 2025-05-13
 */
package com.jeesite.modules.swm.dao;

import com.jeesite.common.dao.CrudDao;
import com.jeesite.common.mybatis.annotation.MyBatisDao;
import com.jeesite.modules.swm.entity.SwmPerson;

/**
 * 人员登记表DAO接口
 * 
 * @author Shawn
 */
@MyBatisDao
public interface SwmPersonDao extends CrudDao<SwmPerson> {

}