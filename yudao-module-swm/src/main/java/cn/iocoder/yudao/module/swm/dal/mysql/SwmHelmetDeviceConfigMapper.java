package cn.iocoder.yudao.module.swm.dal.mysql;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.swm.dal.dataobject.SwmHelmetDeviceConfigDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface SwmHelmetDeviceConfigMapper extends BaseMapperX<SwmHelmetDeviceConfigDO> {

    void saveOrUpdateConfig(SwmHelmetDeviceConfigDO config);
}

