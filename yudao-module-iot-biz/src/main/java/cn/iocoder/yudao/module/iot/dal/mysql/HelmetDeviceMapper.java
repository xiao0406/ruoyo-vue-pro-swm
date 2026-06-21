package cn.iocoder.yudao.module.iot.dal.mysql;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.iot.dal.dataobject.IotDeviceDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 安全帽设备 Mapper（复用 IotDeviceDO）
 */
@Mapper
public interface HelmetDeviceMapper extends BaseMapperX<IotDeviceDO> {

    /**
     * 根据设备ID查询已分配的人员
     *
     * @param deviceId 设备ID
     * @return 人员ID
     */
    @Select("SELECT person_id FROM iot_device WHERE device_id = #{deviceId} AND person_id IS NOT NULL")
    String selectAssignedPersonByDeviceId(@Param("deviceId") String deviceId);

}
