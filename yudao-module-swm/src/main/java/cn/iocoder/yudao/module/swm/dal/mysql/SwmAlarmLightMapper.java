package cn.iocoder.yudao.module.swm.dal.mysql;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.swm.dal.dataobject.SwmAlarmLightDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SwmAlarmLightMapper extends BaseMapperX<SwmAlarmLightDO> {

    SwmAlarmLightDO get(@Param("id") String id);

    List<SwmAlarmLightDO> findList(@Param("STATUS_NORMAL") String STATUS_NORMAL,
                                    @Param("lightName") String lightName,
                                    @Param("snCode") String snCode,
                                    @Param("enableAlarm") String enableAlarm,
                                    @Param("alarmTypeName") String alarmTypeName);

    List<SwmAlarmLightDO> findAllList(@Param("STATUS_NORMAL") String STATUS_NORMAL);
}

