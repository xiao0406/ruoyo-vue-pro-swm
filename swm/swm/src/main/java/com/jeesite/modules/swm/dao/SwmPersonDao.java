/**
 * @author Shawn
 * @date 2025-05-13
 */
package com.jeesite.modules.swm.dao;

import com.jeesite.common.dao.CrudDao;
import com.jeesite.common.mybatis.annotation.MyBatisDao;
import com.jeesite.modules.swm.entity.SwmPerson;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Set;

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

    /**
     * 根据员工ID查询人员信息
     *
     * @param employeeIds 员工ID集合
     * @return 人员信息
     */
    List<SwmPerson> findListByIds(@Param("employeeIds") Set<String> employeeIds);
}
