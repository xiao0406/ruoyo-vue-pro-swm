package cn.iocoder.yudao.module.swm.service;

/**
 * 语音报警 Service 接口
 */
public interface VoiceAlarmService {

    /**
     * 触发语音报警
     *
     * @param alarmType 报警类型
     * @param deviceCode 设备编号
     * @param message    报警内容
     */
    void triggerAlarm(String alarmType, String deviceCode, String message);

    default boolean sendVoiceAlarm(String deviceId, String content) {
        triggerAlarm("voice", deviceId, content);
        return true;
    }

}
