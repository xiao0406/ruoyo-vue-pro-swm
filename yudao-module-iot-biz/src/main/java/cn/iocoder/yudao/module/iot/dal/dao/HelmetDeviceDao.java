package cn.iocoder.yudao.module.iot.dal.dao;

import cn.iocoder.yudao.module.iot.dal.mysql.HelmetDeviceMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

/**
 * 安全帽设备 DAO
 * 桥接 MyBatis-Plus Mapper，保持旧代码兼容
 */
@Repository
@RequiredArgsConstructor
public class HelmetDeviceDao {

    private final HelmetDeviceMapper helmetDeviceMapper;

    /**
     * 根据设备ID获取绑定的人员身份证号
     */
    public String getAssignedPersonByDeviceId(String deviceId) {
        return helmetDeviceMapper.selectAssignedPersonByDeviceId(deviceId);
    }
}
