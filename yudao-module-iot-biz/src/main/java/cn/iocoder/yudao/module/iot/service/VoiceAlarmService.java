package cn.iocoder.yudao.module.iot.service;

public interface VoiceAlarmService {

    boolean sendVoiceAlarm(String deviceId, String content);
}
