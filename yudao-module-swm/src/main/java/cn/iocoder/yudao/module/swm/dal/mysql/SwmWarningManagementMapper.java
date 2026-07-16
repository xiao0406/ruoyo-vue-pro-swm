package cn.iocoder.yudao.module.swm.dal.mysql;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.swm.dal.dataobject.SwmWarningManagementDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Mapper
public interface SwmWarningManagementMapper extends BaseMapperX<SwmWarningManagementDO> {

    List<SwmWarningManagementDO> findList(@Param("random") Integer random);

    List<SwmWarningManagementDO> findAllWithoutStatusFilter();

    void insertToMySql(@Param("id") String id, @Param("createBy") String createBy,
                       @Param("createDate") Date createDate, @Param("updateBy") String updateBy,
                       @Param("updateDate") Date updateDate, @Param("remarks") String remarks,
                       @Param("status") String status, @Param("personName") String personName,
                       @Param("warningType") String warningType, @Param("warningContent") String warningContent,
                       @Param("warningTime") Date warningTime, @Param("alarmRecord") String alarmRecord,
                       @Param("alarmTime") Date alarmTime, @Param("triggerReason") String triggerReason,
                       @Param("handler") String handler, @Param("handleTime") Date handleTime,
                       @Param("handleProcess") String handleProcess, @Param("handleStatus") String handleStatus,
                       @Param("attachment") String attachment, @Param("deviceId") String deviceId,
                       @Param("idCard") String idCard, @Param("disposalDuration") Double disposalDuration,
                       @Param("location") String location, @Param("area") String area);

    List<SwmWarningManagementDO> findInMySqlByIds(@Param("idList") List<String> idList);

    SwmWarningManagementDO findInMySqlByIdAndIdCard(@Param("id") String id, @Param("idCard") String idCard);

    List<SwmWarningManagementDO> findProcessedInMySql(@Param("personName") String personName,
                                                       @Param("warningType") String warningType,
                                                       @Param("warningContent") String warningContent,
                                                       @Param("excludeSOS") Boolean excludeSOS,
                                                       @Param("excludeAttendance") Boolean excludeAttendance,
                                                       @Param("excludeGateEntry") Boolean excludeGateEntry,
                                                       @Param("warningContentList") List<String> warningContentList);

    List<String> findAllProcessedIds();

    List<SwmWarningManagementDO> findAllProcessedWarnings();

    List<SwmWarningManagementDO> listPast7DaysWarning();

    List<SwmWarningManagementDO> listTodayWarning();

    List<SwmWarningManagementDO> listCurrentMonthWarning();

    List<SwmWarningManagementDO> listCurrentMonthHandledWarning();

    List<Map<String, Object>> findWorkGroupNamesByIdCards(@Param("idCards") List<String> idCards);

    List<Map<String, Object>> countByDateRangeGroupByDay(@Param("beginDate") Date beginDate,
                                                          @Param("endDate") Date endDate,
                                                          @Param("isHazardSourceAlarm") Boolean isHazardSourceAlarm);

    List<Map<String, Object>> findTopCategories(@Param("beginDate") Date beginDate,
                                                 @Param("endDate") Date endDate,
                                                 @Param("limit") Integer limit);

    List<Map<String, Object>> findTopWarningContent(@Param("beginDate") Date beginDate,
                                                     @Param("endDate") Date endDate,
                                                     @Param("limit") Integer limit);

    List<Map<String, Object>> findViolationPersonTop10(@Param("beginDate") Date beginDate,
                                                        @Param("endDate") Date endDate,
                                                        @Param("limit") Integer limit);

    List<Map<String, Object>> countHazardCategoryCategoryTrendByDateRange(@Param("beginDate") Date beginDate,
                                                                           @Param("endDate") Date endDate);

    List<Map<String, Object>> countWarningContentTrendByDateRange(@Param("beginDate") Date beginDate,
                                                                   @Param("endDate") Date endDate);

    long countByDateRange(@Param("beginDate") Date beginDate,
                          @Param("endDate") Date endDate,
                          @Param("isHazardSourceAlarm") Boolean isHazardSourceAlarm);

    long countTotal(@Param("isHazardSourceAlarm") Boolean isHazardSourceAlarm);

    long countDistinctPersonByWarningContentAndDateRange(@Param("beginDate") Date beginDate,
                                                          @Param("endDate") Date endDate,
                                                          @Param("isHazardSourceAlarm") Boolean isHazardSourceAlarm);

    List<SwmWarningManagementDO> latestHazardSourceRecord(@Param("isHazardSourceAlarm") Boolean isHazardSourceAlarm,
                                                           @Param("limit") Integer limit);

    List<Map<String, Object>> findHandleEfficiencyTop10(@Param("beginDate") Date beginDate,
                                                         @Param("endDate") Date endDate,
                                                         @Param("limit") Integer limit);

    long countHandledByDateRange(@Param("beginDate") Date beginDate, @Param("endDate") Date endDate);

    long countTodayNonHazardSource();

    long countTodayHandledWarnings();

    List<SwmWarningManagementDO> getWarningManagementList();

    Long theAlarmHasBeenDealtWith(@Param("startTime") Date startTime, @Param("endTime") Date endTime);

    List<String> getWarnIdCardByFiveMinute(@Param("startTime") Date startTime);

    List<Map<String, Object>> countHandledGroupByWarningContent(@Param("beginDate") Date beginDate,
                                                                 @Param("endDate") Date endDate);
}

