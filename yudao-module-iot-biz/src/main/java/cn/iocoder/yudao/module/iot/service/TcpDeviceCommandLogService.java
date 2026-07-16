package cn.iocoder.yudao.module.iot.service;

/**
 * TCP 设备指令日志服务接口
 */
public interface TcpDeviceCommandLogService {

    default void saveTcpCommandLog(String deviceId, String command, String direction) {
    }
}
