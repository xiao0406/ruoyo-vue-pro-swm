package cn.iocoder.yudao.module.iot.service.impl;

import cn.iocoder.yudao.module.iot.service.VoiceAlarmService;
import cn.iocoder.yudao.module.iot.tcp.server.NettyTcpServer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.nio.charset.StandardCharsets;

@Slf4j
@Service
@Validated
public class VoiceAlarmServiceImpl implements VoiceAlarmService {

    @Override
    public boolean sendVoiceAlarm(String deviceId, String content) {
        if (deviceId == null || deviceId.isBlank() || content == null || content.isBlank()) {
            return false;
        }
        String command = buildVoiceCommand(content);
        boolean success = NettyTcpServer.getInstance().getSessionManager().sendMessageToDevice(deviceId, command);
        if (!success) {
            log.warn("Failed to send voice alarm, deviceId={}", deviceId);
        }
        return success;
    }

    private String buildVoiceCommand(String text) {
        String dataSection = "PS,0,TTSD," + text + ",#";
        String hexLength = Integer.toHexString(dataSection.getBytes(StandardCharsets.UTF_8).length).toUpperCase();
        return "$" + hexLength + "," + dataSection + "\n";
    }
}
