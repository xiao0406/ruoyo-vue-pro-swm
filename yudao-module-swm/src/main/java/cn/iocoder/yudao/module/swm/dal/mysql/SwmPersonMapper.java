package cn.iocoder.yudao.module.swm.dal.mysql;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.swm.controller.admin.area.vo.AreaTrajectoryVO;
import cn.iocoder.yudao.module.swm.controller.admin.dashboard.vo.DashboardPersonVO;
import cn.iocoder.yudao.module.swm.dal.dataobject.*;
import cn.iocoder.yudao.module.swm.dal.dataobject.dto.AiDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 浜哄憳绠＄悊 Mapper
 */
@Mapper
public interface SwmPersonMapper extends BaseMapperX<SwmPersonDO> {

    List<SwmPersonDO> findList(@Param("company") String company, @Param("personNumber") String personNumber,
            @Param("id") String id, @Param("department") String department, @Param("prodLine") String prodLine,
            @Param("team") String team, @Param("jobType") String jobType, @Param("name") String name,
            @Param("identityCard") String identityCard, @Param("phoneNumber") String phoneNumber,
            @Param("personnelStatus") String personnelStatus, @Param("status") String status,
            @Param("personType") String personType, @Param("gender") String gender,
            @Param("safetyHelmetId") String safetyHelmetId, @Param("safetyEducation") String safetyEducation,
            @Param("helmetReturned") String helmetReturned, @Param("departureType") String departureType,
            @Param("isExternalPersonnel") String isExternalPersonnel, @Param("powerOnStatus") String powerOnStatus,
            @Param("todayOnSiteIdCards") List<String> todayOnSiteIdCards, @Param("random") Integer random,
            @Param("tenantId") Long tenantId);

    SwmPersonDO findDepartedByIdentityCard(@Param("identityCard") String identityCard,
            @Param("personnelStatus") String personnelStatus);

    List<SwmPersonDO> findListByIds(@Param("employeeIds") List<String> employeeIds);

    List<SwmPersonDO> findPersonsByDepartmentCondition(@Param("departmentCondition") String departmentCondition);

    List<SwmPersonDO> searchPersonsByKeyword(@Param("keyword") String keyword);

    List<Map<String, Object>> findActivePersonsWithIds(@Param("idCard") String idCard,
            @Param("random") Integer random);

    List<SwmPersonDO> findByIdCards(@Param("idCards") List<String> idCards);

    int countDistinctByIdentityCard();

    List<SwmPersonDO> findActivePersonsByIdentityCards(@Param("identityCards") List<String> identityCards);

    List<SwmPersonDO> findActivePersonsByPhoneNumbers(@Param("phoneNumbers") List<String> phoneNumbers);

    List<SwmPersonDO> findByIdCardsPage(@Param("idCards") List<String> idCards);

    Long findByIdCardsCount(@Param("idCards") List<String> idCards);

    List<AiDto.RiskStatistics> findTableName();

    List<AreaTrajectoryVO> findPersonList();

    List<DashboardPersonVO> findTodayAttendance(@Param("date") String date,
            @Param("personTypeList") List<String> personTypeList);

    List<DashboardPersonVO> findManageTodayList(@Param("personTypeList") List<String> personTypeList);

    List<SwmPersonDO> peronsList(@Param("personnelStatus") String personnelStatus,
            @Param("status") String status, @Param("tenantId") Long tenantId,
            @Param("random") Integer random);

    List<SwmPersonDO> findListByJobTypeList(@Param("jobtypeList") List<String> jobtypeList);

    List<FmsPositionArchiveDO> selectByNames(@Param("tenantId") Long tenantId,
            @Param("departments") List<String> departments);

    List<FmsProdLineDO> selectProdLineByNames(@Param("tenantId") Long tenantId,
            @Param("productionLines") List<String> productionLines);

    List<FmsWorkGroupDO> selectWorkGroupByNames(@Param("tenantId") Long tenantId,
            @Param("teams") List<String> teams);

    List<SwmPersonDO> findListByPersonNames(@Param("personNames") List<String> personNames);

    List<SwmPersonDO> findListWithoutTenantId(@Param("status") String status,
            @Param("personnelStatus") String personnelStatus);

    void updateBatch(@Param("list") List<SwmPersonDO> list);

    void updateTeamBatch(@Param("list") List<SwmPersonDO> list);

    void updateBatchUrgentPerson(@Param("updateList") List<SwmPersonDO> updateList);
}

