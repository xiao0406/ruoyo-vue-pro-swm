package com.jeesite.modules.swm.dao;

import cn.hutool.core.date.DateTime;
import com.jeesite.common.dao.CrudDao;
import com.jeesite.common.mybatis.annotation.MyBatisDao;
import com.jeesite.modules.swm.entity.SwmWarningManagement;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;
import java.util.Map;

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

    /**
     * 按日期范围统计危险源数量
     * @param beginDate 开始日期
     * @param endDate 结束日期
     * @return
     */
    List<Map<String, Object>> countByDateRangeGroupByDay(@Param("beginDate") Date beginDate,@Param("endDate") Date endDate,@Param("isHazardSourceAlarm") Boolean isHazardSourceAlarm);

    /**
     * 查询TOP N危险源类别 warning_content = '危险源报警'
     * @param beginDate 开始日期(格式：yyyy-MM-dd)
     * @param endDate 结束日期(格式：yyyy-MM-dd)
     * @param limit 返回数量
     * @return 包含category(类别)和count(数量)的Map列表
     */
    List<Map<String, Object>> findTopCategories(@Param("beginDate") String beginDate, @Param("endDate") String endDate, @Param("limit") int limit);

    /**
     * 查询TOP N 预警内容（warning_content != '危险源报警')
     * @param beginDate 开始日期(格式：yyyy-MM-dd)
     * @param endDate 结束日期(格式：yyyy-MM-dd)
     * @param limit 返回数量
     * @return 包含category(类别)和count(数量)的Map列表
     */
    List<Map<String, Object>> findTopWarningContent(@Param("beginDate") String beginDate, @Param("endDate") String endDate, @Param("limit") int limit);


    /**
     * 查询TOP N违规人员
     * @param beginDate 开始日期(格式：yyyy-MM-dd)
     * @param endDate 结束日期(格式：yyyy-MM-dd)
     * @param limit 返回数量
     * @return
     */
    List<Map<String, Object>> findViolationPersonTop10(@Param("beginDate") String beginDate,@Param("endDate") String endDate,@Param("limit") int limit);

    /**
     * 按日期范围统计危险源类别趋势
     * @param beginDate 开始日期
     * @param endDate 结束日期
     * @return 危险源类别趋势
     */
    List<Map<String, Object>> countHazardCategoryCategoryTrendByDateRange(@Param("beginDate") Date beginDate,@Param("endDate") Date endDate);

    /**
     * 按时间范围统计数量
     * @param beginDate 开始日期
     * @param endDate 结束日期
     * @param isHazardSourceAlarm 是否是危险源报警
     */
    Long countByDateRange(@Param("beginDate") Date beginDate, @Param("endDate") Date endDate,@Param("isHazardSourceAlarm") Boolean isHazardSourceAlarm);

    /**
     * 按预警内容统计总数
     * @param isHazardSourceAlarm 是否是危险源报警
     */
    Long countTotal(@Param("isHazardSourceAlarm") Boolean isHazardSourceAlarm);

    /**
     * 统计不同违规人员数量
     */
    Long countDistinctPersonByWarningContentAndDateRange(@Param("beginDate") Date beginDate, @Param("endDate") Date endDate,@Param("isHazardSourceAlarm") Boolean isHazardSourceAlarm);

    /**
     * 获取最新的危险源报警记录
     * @param limit 返回数量
     */
    List<SwmWarningManagement> latestHazardSourceRecord(int limit);

    /**
     * 获取处理效率TOP10
     * @param beginDate 开始时间
     * @param endDate 结束时间
     * @param limit 返回数量
     * @return
     */
    List<Map<String,Object>> findHandleEfficiencyTop10(@Param("beginDate") String beginDate,@Param("endDate") String endDate,@Param("limit") int limit);

    /**
     * 统计对应时间范围内非危险源报警的处置记录
     * @param beginDate 开始时间
     * @param endDate 结束时间
     * @return
     */
    Long countHandledByDateRange(@Param("beginDate") Date beginDate,@Param("endDate") Date endDate);

    /**
     * 按时间范围统计非危险源报警的趋势
     * @param beginDate 开始时间
     * @param endDate 结束时间
     * @return
     */
    List<Map<String, Object>> countWarningContentTrendByDateRange(Date beginDate, Date endDate);

    /**
     * 统计今日非危险源报警的预警数量
     * @return 今日非危险源报警的数量
     */
    long countTodayNonHazardSource();
    
    /**
     * 统计当日已处理的预警数（排除考勤打卡和进入大门）
     * 
     * @return 当日已处理的预警数量
     * @author Shawn
     * @date 2025-01-24
     */
    long countTodayHandledWarnings();

    List<SwmWarningManagement> getWarningManagementList();

    Long theAlarmHasBeenDealtWith(String startTime, String endTime);

    List<String> getWarnIdCardByFiveMinute(DateTime dateTime);
}
