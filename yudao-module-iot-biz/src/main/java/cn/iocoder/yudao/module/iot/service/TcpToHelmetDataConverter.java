package cn.iocoder.yudao.module.iot.service;

import java.util.List;
import java.util.Map;

/**
 * TCP数据到安全帽数据转换器
 */
public interface TcpToHelmetDataConverter {

    /**
     * 将TCP蓝牙信标数据转换为WebSocket格式
     */
    List<Map<String, Object>> convertBeaconData(List<Map<String, Object>> tcpBeaconData);
}
