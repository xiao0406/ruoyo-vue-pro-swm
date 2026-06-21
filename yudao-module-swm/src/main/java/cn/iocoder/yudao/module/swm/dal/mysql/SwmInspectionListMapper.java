package cn.iocoder.yudao.module.swm.dal.mysql;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.swm.dal.dataobject.SwmInspectionListDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.HashMap;
import java.util.List;

@Mapper
public interface SwmInspectionListMapper extends BaseMapperX<SwmInspectionListDO> {

    SwmInspectionListDO get(@Param("id") String id);

    List<SwmInspectionListDO> findList(@Param("status") String status,
                                        @Param("planCode") String planCode,
                                        @Param("planName") String planName,
                                        @Param("inspector") String inspector,
                                        @Param("inspectionType") String inspectionType,
                                        @Param("inspectionListStatus") String inspectionListStatus);

    SwmInspectionListDO getLastTaskByPlanId(@Param("planId") String planId);

    List<SwmInspectionListDO> latestInspectionRecord(@Param("limit") Integer limit);

    List<HashMap<String, Object>> findByPlanIds(@Param("planIds") List<String> planIds);

    int countByPlanIdAndDate(@Param("planId") String planId, @Param("dateStr") String dateStr);
}
