package cn.iocoder.yudao.module.swm.dal.mysql;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.swm.dal.dataobject.SwmAlarmConfigDetailDO;
import cn.iocoder.yudao.module.swm.dal.dataobject.SwmAlarmConfigDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SwmAlarmConfigDetailMapper extends BaseMapperX<SwmAlarmConfigDetailDO> {

    SwmAlarmConfigDetailDO findByMainId(@Param("mainId") String mainId, @Param("id") String id);

    List<String> findByUserIds(@Param("users") List<String> users);

    List<String> findByRoleIds(@Param("roles") List<String> roles);

    SwmAlarmConfigDO getByAlarmKey(@Param("alarmKey") String alarmKey);
}

