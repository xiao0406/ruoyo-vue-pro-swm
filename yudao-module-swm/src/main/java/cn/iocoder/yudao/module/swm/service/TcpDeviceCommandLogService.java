package cn.iocoder.yudao.module.swm.service;

/**
 * TCP 设备指令日志 Service 接口
 */
public interface TcpDeviceCommandLogService {

    /**
     * 记录设备指令日志
     *
     * @param deviceCode 设备编号
     * @param command    指令内容
     * @param direction  方向（SEND/RECEIVE）
     */
    void logCommand(String deviceCode, String command, String direction);

    default void saveTcpCommandLog(String deviceId, String command, String direction) {
        logCommand(deviceId, command, direction);
    }

}
