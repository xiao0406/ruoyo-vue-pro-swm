package cn.iocoder.yudao.module.iot.dao;

import cn.iocoder.yudao.module.iot.dal.dataobject.IotDeviceDO;
import cn.iocoder.yudao.module.iot.dal.mysql.IotDeviceMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

/**
 * IoT 设备 DAO
 * 桥接 MyBatis-Plus Mapper，保持旧代码兼容
 */
@Repository
@RequiredArgsConstructor
public class IotDeviceDao {

    private final IotDeviceMapper iotDeviceMapper;

    public int updateSessionId(String deviceId, String sessionId) {
        iotDeviceMapper.updateSessionIdByDeviceId(deviceId, sessionId);
        return 1;
    }

    public int insertDevice(IotDeviceDO device) {
        return iotDeviceMapper.insert(device);
    }
}
