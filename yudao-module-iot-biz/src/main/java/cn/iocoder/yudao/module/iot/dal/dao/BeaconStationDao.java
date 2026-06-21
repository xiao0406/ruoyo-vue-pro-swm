package cn.iocoder.yudao.module.iot.dal.dao;

import cn.iocoder.yudao.module.iot.dal.mysql.BeaconStationMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

/**
 * Beacon 基站 DAO
 * 桥接 MyBatis-Plus Mapper，保持旧代码兼容
 */
@Repository
@RequiredArgsConstructor
public class BeaconStationDao {

    private final BeaconStationMapper beaconStationMapper;
}
