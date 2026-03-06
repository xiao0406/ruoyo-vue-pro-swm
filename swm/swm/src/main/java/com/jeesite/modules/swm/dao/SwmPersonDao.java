/**
 * @author Shawn
 * @date 2025-05-13
 */
package com.jeesite.modules.swm.dao;

import com.jeesite.common.dao.CrudDao;
import com.jeesite.common.mybatis.annotation.MyBatisDao;
import com.jeesite.modules.entity.SwmPersonExport;
import com.jeesite.modules.fms.entity.FmsPositionArchive;
import com.jeesite.modules.fms.entity.FmsProdLine;
import com.jeesite.modules.fms.entity.FmsWorkGroup;
import com.jeesite.modules.swm.entity.PersonnelOrganizationQueryParam;
import com.jeesite.modules.swm.entity.SwmPerson;
import com.jeesite.modules.entity.AiDto;
import com.jeesite.modules.swm.web.SwmDashboardNewController;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;
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

    /**
     * 根据部门条件查询在职人员
     *
     * @param departmentCondition 部门条件参数，可以是车间ID、班组ID、产线ID、组织编码或身份证号
     * @return 符合条件的在职人员列表
     */
    List<SwmPerson> findPersonsByDepartmentCondition(@Param("departmentCondition") String departmentCondition);

    /**
     * 根据关键词搜索人员（支持姓名、身份证、电话多字段搜索）
     *
     * @param keyword 搜索关键词
     * @return 符合条件的在职人员列表
     */
    List<SwmPerson> searchPersonsByKeyword(@Param("keyword") String keyword);

    /**
     * 查询所有在职人员的详细信息（包含关联表ID）
     * 用于缓存初始化
     *
     * @return 包含所有关联表ID的在职人员信息
     * @author Shawn
     * @date 2025/06/23
     */
    List<Map<String, Object>> findActivePersonsWithIds(int random, String idCard);

    /**
     * 根据身份证号列表查询人员
     *
     * @param idCards 身份证号列表
     * @return 人员列表
     */
    List<SwmPerson> findByIdCards(List<String> idCards);

    /**
     * 统计不重复身份证的人员数量
     *
     * @return 不重复身份证的人员数量
     */
    int countDistinctByIdentityCard();

    /**
     * 通过组织和工种查询人员
     */
    List<SwmPerson> listByOrgAndWorkType(@Param("query") PersonnelOrganizationQueryParam query);

    /**
     * 根据身份证号列表查询在职人员
     * 
     * @param identityCards 身份证号列表
     * @return 在职人员列表
     * @author Shawn
     * @date 2025-01-15
     */
    List<SwmPerson> findActivePersonsByIdentityCards(@Param("identityCards") List<String> identityCards);

    /**
     * 根据手机号列表查询在职人员
     * 
     * @param phoneNumbers 手机号列表
     * @return 在职人员列表
     * @author Shawn
     * @date 2025-01-15
     */
    List<SwmPerson> findActivePersonsByPhoneNumbers(@Param("phoneNumbers") List<String> phoneNumbers);

    List<SwmPerson> findByIdCardsPage(SwmPerson swmPersonPage);

    Long findByIdCardsCount(SwmPerson swmPersonPage);

    List<AiDto.RiskStatistics> findTableName();

    List<AiDto.Trajectory> findPersonList();

    List<SwmDashboardNewController.Person> findTodayAttendance(SwmDashboardNewController.Person vo);

    List<SwmPerson> peronsList(SwmPerson swmPerson);


    List<SwmDashboardNewController.Person> findManageTodayList(SwmDashboardNewController.Person vo);

    void updateBatch(List<SwmPersonExport> list);

    List<SwmPerson> findListByJobTypeList(List<String> jobtypeList);

    List<FmsPositionArchive> selectByNames(@Param("departments") List<String> departments, @Param("corpCode") String corpCode);

    List<FmsProdLine> selectProdLineByNames(@Param("productionLines") List<String> productionLines, @Param("corpCode") String corpCode);

    List<FmsWorkGroup> selectWorkGroupByNames(@Param("teams") List<String> teams, @Param("corpCode") String corpCode);

    List<SwmPerson> findListByPersonNames(@Param("personNames") List<String> personNames);

    void updateTeamBatch(List<SwmPerson> updatePersons);
}
