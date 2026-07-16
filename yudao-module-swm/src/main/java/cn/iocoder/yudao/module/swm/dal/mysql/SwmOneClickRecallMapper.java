package cn.iocoder.yudao.module.swm.dal.mysql;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.swm.dal.dataobject.SwmOneClickRecallDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface SwmOneClickRecallMapper extends BaseMapperX<SwmOneClickRecallDO> {

    List<SwmOneClickRecallDO> findList();

    List<Map<String, Object>> findPersonnelByNodes(@Param("selectedNodes") List<Map<String, Object>> selectedNodes);

    List<Map<String, Object>> findDevicesByIdentityCards(@Param("identityCards") List<String> identityCards);

    List<Map<String, Object>> findPersonnelByNodesWithOr(@Param("selectedNodes") List<Map<String, Object>> selectedNodes);

    List<Map<String, Object>> findPersonnelByDeviceIds(@Param("deviceIds") List<String> deviceIds);

    List<Map<String, Object>> findAllTargetPersonnelForBroadcast();

    List<Map<String, Object>> findAllTargetPersonnelForBroadcastByPersonType(@Param("personTypeList") List<String> personTypeList);
}

