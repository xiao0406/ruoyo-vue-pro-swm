package com.jeesite.modules.swm.dao;

import com.jeesite.common.dao.CrudDao;
import com.jeesite.common.mybatis.annotation.MyBatisDao;
import com.jeesite.modules.swm.entity.SwmPersonSchedule;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 人员排班表DAO接口
 * 
 * @author zwf
 * @version 2025-05-15
 */
@MyBatisDao
public interface SwmPersonScheduleDao extends CrudDao<SwmPersonSchedule> {
    
    /**
     * 根据身份证号查询排班记录
     * 
     * @param idCard 身份证号码
     * @return 排班记录列表
     */
    List<SwmPersonSchedule> findByIdCard(String idCard);
    
    /**
     * 根据身份证号和月份查询排班记录
     * 
     * @param idCard 身份证号码
     * @param month 月份
     * @return 排班记录列表
     */
    List<SwmPersonSchedule> findByIdCardAndMonth(@Param("idCard") String idCard, @Param("month") String month);
    
    /**
     * 根据身份证号获取班组信息
     * 
     * @param idCard 身份证号码
     * @return 班组名称
     */
    String getWorkGroupNameByIdCard(String idCard);
} 