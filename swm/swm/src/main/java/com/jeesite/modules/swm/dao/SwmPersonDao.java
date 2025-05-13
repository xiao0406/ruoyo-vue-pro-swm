/**
 * @author Shawn
 * @date 2025-05-13
 */
package com.jeesite.modules.swm.dao;

import com.jeesite.common.dao.CrudDao;
import com.jeesite.common.mybatis.annotation.MyBatisDao;
import com.jeesite.modules.swm.entity.SwmPerson;

import java.util.List;

/**
 * 人员登记表DAO接口
 * 
 * @author Shawn
 */
@MyBatisDao
public interface SwmPersonDao extends CrudDao<SwmPerson> {

    /**
     * 根据身份证号码查询离职人员
     * 
     * @param swmPerson 查询条件，包含身份证号码
     * @return 离职人员列表
     */
    List<SwmPerson> findDepartedByIdentityCard(SwmPerson swmPerson);
}