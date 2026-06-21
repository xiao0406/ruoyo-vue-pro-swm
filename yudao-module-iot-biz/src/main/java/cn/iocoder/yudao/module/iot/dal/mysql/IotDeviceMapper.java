package cn.iocoder.yudao.module.iot.dal.mysql;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.iot.dal.dataobject.IotDeviceDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface IotDeviceMapper extends BaseMapperX<IotDeviceDO> {

    /**
     * 根据设备ID更新会话ID
     *
     * @param deviceId  设备ID
     * @param sessionId 会话ID
     */
    @Update("UPDATE iot_device SET session_id = #{sessionId} WHERE device_id = #{deviceId}")
    void updateSessionIdByDeviceId(@Param("deviceId") String deviceId, @Param("sessionId") String sessionId);

    /**
     * 插入设备
     *
     * @param device 设备DO
     */
    void insertDevice(IotDeviceDO device);

}
