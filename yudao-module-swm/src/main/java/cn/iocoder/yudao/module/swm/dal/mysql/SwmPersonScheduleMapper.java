package cn.iocoder.yudao.module.swm.dal.mysql;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.swm.dal.dataobject.SwmPersonScheduleDO;
import cn.iocoder.yudao.module.swm.dal.dataobject.dto.SwmBatchUpdateClassesDTO;
import cn.iocoder.yudao.module.swm.dal.dataobject.dto.SwmPersonDOScheduleExportDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface SwmPersonScheduleMapper extends BaseMapperX<SwmPersonScheduleDO> {

    List<SwmPersonScheduleDO> findByIdCard(@Param("idCard") String idCard);

    String getWorkGroupNameByIdCard(@Param("idCard") String idCard);

    List<SwmPersonScheduleDO> findByIdCardAndMonth(@Param("idCard") String idCard, @Param("month") String month);

    List<Map<String, Object>> findWorkGroupList();

    List<Map<String, Object>> batchGetWorkGroupNameByIdCards(@Param("list") List<String> list);

    int countDistinctPersonByYearAndMonth(@Param("yearMonth") String yearMonth);

    List<SwmPersonScheduleDO> findList(@Param("personName") String personName,
                                        @Param("month") String month,
                                        @Param("classes") String classes,
                                        @Param("idCard") String idCard,
                                        @Param("organization") String organization,
                                        @Param("workshop") String workshop,
                                        @Param("process") String process,
                                        @Param("workGroupName") String workGroupName);

    List<SwmPersonScheduleDO> scheduleList(@Param("month") String month,
                                            @Param("corpCode") String corpCode,
                                            @Param("random") Integer random);

    List<String> findIdCardsByIds(@Param("ids") List<String> ids);

    List<SwmPersonDOScheduleExportDO> findPersonIdsByNames(@Param("personNames") List<String> personNames);

    void updateBatch(@Param("list") List<SwmPersonScheduleDO> list);

    void batchUpdateClasses(@Param("dto") SwmBatchUpdateClassesDTO dto);

    List<SwmPersonScheduleDO> findListByCorpCode(@Param("corpCode") String corpCode);
}
