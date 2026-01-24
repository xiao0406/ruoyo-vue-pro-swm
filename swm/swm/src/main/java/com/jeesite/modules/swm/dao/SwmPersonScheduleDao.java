package com.jeesite.modules.swm.dao;

import com.jeesite.common.dao.CrudDao;
import com.jeesite.common.mybatis.annotation.MyBatisDao;
import com.jeesite.modules.entity.SwmPersonScheduleExport;
import com.jeesite.modules.swm.entity.SwmPersonSchedule;
import com.jeesite.modules.swm.entity.dto.SwmPersonScheduleDto;
import org.apache.ibatis.annotations.Param;

import javax.validation.constraints.NotBlank;
import java.util.List;
import java.util.Map;

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
     * @param month  月份
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

    /**
     * 获取所有班组列表
     * 
     * @return 班组列表，包含id和名称
     */
    List<Map<String, Object>> findWorkGroupList();

    /**
     * 批量获取多个人员的班组信息
     * 
     * @param idCards 身份证号码列表
     * @return 包含身份证号和班组名称的对象列表
     */
    List<Map<String, Object>> batchGetWorkGroupNameByIdCards(List<String> idCards);

    /**
     * 根据年份和月份获取不重复身份证的排班人数
     * 
     * @param yearMonth 年月格式，例如："2025-06"
     * @return 排班人数
     */
    int countDistinctPersonByYearAndMonth(@Param("yearMonth") String yearMonth);

    List<SwmPersonSchedule> scheduleList(SwmPersonSchedule swmPersonSchedule);

    void updateBatch(@Param("list") List<SwmPersonScheduleExport> list1);

    /**
     * 批量修改人员排班班次
     *
     * @param dto      包含人员ID列表和目标班次
     * @return 影响行数
     */
    int batchUpdateClasses(@Param("dto") SwmPersonScheduleDto dto);

    List<String> findIdCardsByIds(@Param("ids") List<String> ids);
}