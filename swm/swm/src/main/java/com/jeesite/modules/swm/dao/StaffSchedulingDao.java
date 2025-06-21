package com.jeesite.modules.swm.dao;

import com.jeesite.common.dao.CrudDao;
import com.jeesite.common.mybatis.annotation.MyBatisDao;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 人员排班DAO接口
 * 
 * @author Swm
 * @version 2023-07-01
 */
@MyBatisDao
public interface StaffSchedulingDao {

    /**
     * 根据办公室ID获取人员列表
     * 
     * @param officeId 办公室ID
     * @return 人员列表
     */
    List<Map<String, Object>> getPersonsByOffice(@Param("officeId") String officeId);
    
    /**
     * 根据车间ID获取人员列表
     * 
     * @param workshopId 车间ID
     * @return 人员列表
     */
    List<Map<String, Object>> getPersonsByWorkshop(@Param("workshopId") String workshopId);
    
    /**
     * 根据产线ID获取人员列表
     * 
     * @param prodLineId 产线ID
     * @return 人员列表
     */
    List<Map<String, Object>> getPersonsByProdLine(@Param("prodLineId") String prodLineId);
    
    /**
     * 根据班组ID获取人员列表
     * 
     * @param workGroupId 班组ID
     * @return 人员列表
     */
    List<Map<String, Object>> getPersonsByWorkGroup(@Param("workGroupId") String workGroupId);
} 