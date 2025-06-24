package com.jeesite.modules.swm.dao;

import com.jeesite.common.dao.CrudDao;
import com.jeesite.common.mybatis.annotation.MyBatisDao;
import com.jeesite.modules.swm.entity.SwmWarningManagement;

import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Param;

/**
 * 预警管理DAO接口
 *
 * @author zwf
 * @version 2025-05-16
 */
@MyBatisDao
public interface SwmWarningManagementDao extends CrudDao<SwmWarningManagement> {

    /**
     * 查询所有记录，不带默认的状态过滤
     */
    List<SwmWarningManagement> findAllWithoutStatusFilter();

    /**
     * 向MySQL数据库插入预警处置记录
     *
     * @param swmWarningManagement 预警管理实体
     * @return 影响的行数
     */
    int insertToMySql(SwmWarningManagement swmWarningManagement);

    /**
     * 根据ID集合批量查询MySQL数据库中的预警记录
     *
     * @param idList 预警ID集合
     * @return 预警记录列表
     */
    List<SwmWarningManagement> findInMySqlByIds(@Param("idList") List<String> idList);

    /**
     * 根据ID和身份证号查询MySQL数据库中的预警记录
     *
     * @param id 预警ID
     * @param idCard 身份证号
     * @return 预警记录
     */
    SwmWarningManagement findInMySqlByIdAndIdCard(@Param("id") String id, @Param("idCard") String idCard);

    /**
     * 在MySQL中按条件查询已处置的预警数据
     *
     * @param swmWarningManagement 包含查询条件的预警管理对象
     * @return 符合条件的已处置预警记录列表
     */
    List<SwmWarningManagement> findProcessedInMySql(SwmWarningManagement swmWarningManagement);

    /**
     * 查询所有已处置记录的ID
     *
     * @return 已处置记录ID列表
     */
    List<String> findAllProcessedIds();
    
    /**
     * 获取所有已处置的记录（handle_status为1）
     * 
     * @return 已处置的预警记录列表
     */
    List<SwmWarningManagement> findAllProcessedWarnings();

    /**
     * 查询近7天的预警记录
     */
    List<SwmWarningManagement> listPast7DaysWarning();

    /**
     * 获取今日的预警记录
     */
    List<SwmWarningManagement> listTodayWarning();
    
    /**
     * 获取当月的预警记录
     */
    List<SwmWarningManagement> listCurrentMonthWarning();
    
    /**
     * 获取当月已处理的预警记录
     */
    List<SwmWarningManagement> listCurrentMonthHandledWarning();

    /**
     * 根据身份证号列表批量查询班组名称
     * 
     * @param idCards 身份证号列表
     * @return 身份证号与班组名称的映射列表
     */
    List<Map<String, String>> findWorkGroupNamesByIdCards(@Param("idCards") List<String> idCards);
}
