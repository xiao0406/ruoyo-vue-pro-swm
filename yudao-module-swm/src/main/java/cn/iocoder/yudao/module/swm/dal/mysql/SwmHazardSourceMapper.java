package cn.iocoder.yudao.module.swm.dal.mysql;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.swm.dal.dataobject.SwmHazardSourceDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface SwmHazardSourceMapper extends BaseMapperX<SwmHazardSourceDO> {

    SwmHazardSourceDO get(@Param("id") String id);

    List<SwmHazardSourceDO> findList(@Param("hazardName") String hazardName,
                                      @Param("hazardCategory") String hazardCategory,
                                      @Param("location") String location,
                                      @Param("beaconIdentifier") String beaconIdentifier,
                                      @Param("beaconIdentifierSearchList") List<String> beaconIdentifierSearchList,
                                      @Param("beaconTag") String beaconTag,
                                      @Param("isPatrolIncluded") String isPatrolIncluded,
                                      @Param("hazardStatus") String hazardStatus,
                                      @Param("status") String status);

    int countByStatusAndMonth(@Param("status") String status,
                               @Param("year") String year,
                               @Param("month") String month);

    List<Map<String, Object>> countByCategoryAndMonth(@Param("year") String year,
                                                       @Param("month") String month);

    List<Map<String, Object>> getTop10Categories(@Param("year") String year,
                                                  @Param("month") String month);

    int countByYearAndMonth(@Param("year") String year, @Param("month") String month);

    List<Map<String, Object>> findTopCategories(@Param("beginDate") String beginDate,
                                                 @Param("endDate") String endDate,
                                                 @Param("limit") Integer limit);

    List<Map<String, Object>> countByDateRangeGroupByDay(@Param("beginDate") String beginDate,
                                                          @Param("endDate") String endDate);

    List<Map<String, Object>> countCategoryTrendByDateRange(@Param("beginDate") String beginDate,
                                                             @Param("endDate") String endDate);

    List<Map<String, Object>> findCategoryDistribution(@Param("beginDate") String beginDate,
                                                        @Param("endDate") String endDate);

    int countByDateRange(@Param("beginDate") String beginDate,
                          @Param("endDate") String endDate,
                          @Param("statusList") List<String> statusList);

    int countNoInspectionPlan(@Param("beginDate") String beginDate,
                               @Param("endDate") String endDate);

    int countAll();
}

