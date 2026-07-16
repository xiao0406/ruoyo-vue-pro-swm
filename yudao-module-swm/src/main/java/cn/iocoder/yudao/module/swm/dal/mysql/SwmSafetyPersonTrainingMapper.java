package cn.iocoder.yudao.module.swm.dal.mysql;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.swm.dal.dataobject.SwmSafetyPersonTrainingDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface SwmSafetyPersonTrainingMapper extends BaseMapperX<SwmSafetyPersonTrainingDO> {

    void appUpdate(@Param("id") String id,
                    @Param("progress") String progress,
                    @Param("completeDate") LocalDateTime completeDate,
                    @Param("completeStatus") String completeStatus);

    List<SwmSafetyPersonTrainingDO> findList(@Param("personName") String personName,
                                              @Param("pushDateStr") String pushDateStr,
                                              @Param("title") String title,
                                              @Param("department") String department,
                                              @Param("prodLine") String prodLine,
                                              @Param("team") String team,
                                              @Param("company") String company,
                                              @Param("tenantId") Long tenantId);

    List<SwmSafetyPersonTrainingDO> appPageList(@Param("completeStatus") String completeStatus,
                                                  @Param("identityCard") String identityCard,
                                                  @Param("phoneNumber") String phoneNumber,
                                                  @Param("phone") String phone,
                                                  @Param("id") String id);

    List<String> findListByIdCard(@Param("startMonth") String startMonth,
                                   @Param("endMonth") String endMonth);
}

