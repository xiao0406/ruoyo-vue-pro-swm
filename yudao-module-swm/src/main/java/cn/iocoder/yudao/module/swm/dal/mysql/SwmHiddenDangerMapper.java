package cn.iocoder.yudao.module.swm.dal.mysql;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.swm.dal.dataobject.SwmHiddenDangerDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SwmHiddenDangerMapper extends BaseMapperX<SwmHiddenDangerDO> {

    List<SwmHiddenDangerDO> findListWithPlanName(@Param("dangerName") String dangerName,
                                                  @Param("location") String location,
                                                  @Param("inspectionPlanId") String inspectionPlanId,
                                                  @Param("isBeaconDeployed") String isBeaconDeployed,
                                                  @Param("isHandled") String isHandled,
                                                  @Param("status") String status);
}
