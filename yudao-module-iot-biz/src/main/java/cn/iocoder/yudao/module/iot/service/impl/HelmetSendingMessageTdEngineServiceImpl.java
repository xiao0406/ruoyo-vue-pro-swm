package cn.iocoder.yudao.module.iot.service.impl;

import cn.hutool.json.JSONObject;
import cn.iocoder.yudao.module.iot.service.HelmetSendingMessageTdEngineService;
import cn.iocoder.yudao.module.iot.util.R;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.Map;

/**
 * Helmet sending-message TDengine writer.
 */
@Service
@Validated
public class HelmetSendingMessageTdEngineServiceImpl implements HelmetSendingMessageTdEngineService {

    @Resource
    private TdengineJsonPayloadWriter payloadWriter;

    @Override
    public R<JSONObject> saveHelmetSendingMessageData(String deviceId, Map<String, Object> data) {
        return payloadWriter.savePayload("helmet_sending_message", deviceId, "default", data);
    }
}
