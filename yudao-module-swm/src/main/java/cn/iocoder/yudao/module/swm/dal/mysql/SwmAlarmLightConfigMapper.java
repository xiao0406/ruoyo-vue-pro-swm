package cn.iocoder.yudao.module.swm.dal.mysql;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.swm.dal.dataobject.SwmAlarmLightConfigDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SwmAlarmLightConfigMapper extends BaseMapperX<SwmAlarmLightConfigDO> {

    SwmAlarmLightConfigDO get(@Param("id") String id);

    List<SwmAlarmLightConfigDO> findList(@Param("STATUS_NORMAL") String STATUS_NORMAL,
                                          @Param("lightId") String lightId,
                                          @Param("alarmConfigId") String alarmConfigId);

    List<SwmAlarmLightConfigDO> findListByLightId(@Param("lightId") String lightId);

    List<SwmAlarmLightConfigDO> findAllList(@Param("STATUS_NORMAL") String STATUS_NORMAL);

    void deleteByLightId(@Param("lightId") String lightId,
                          @Param("updateBy") String updateBy);
}
