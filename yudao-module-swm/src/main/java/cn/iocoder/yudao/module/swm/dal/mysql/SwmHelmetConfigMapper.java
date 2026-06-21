package cn.iocoder.yudao.module.swm.dal.mysql;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.swm.dal.dataobject.SwmHelmetConfigDO;
import cn.iocoder.yudao.module.swm.dal.dataobject.SwmHelmetDeviceDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface SwmHelmetConfigMapper extends BaseMapperX<SwmHelmetConfigDO> {

    List<SwmHelmetConfigDO> findList();

    List<Map<String, Object>> getWorkshopData();

    List<Map<String, Object>> getWorkshopLineGroupData();

    List<Map<String, Object>> getPersonTypeEnumData();

    List<Map<String, Object>> getWorkTypeEnumData();

    SwmHelmetDeviceDO getByDeviceId(@Param("deviceId") String deviceId);
}
