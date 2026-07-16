package cn.iocoder.yudao.module.iot.service.impl;

import cn.hutool.json.JSONObject;
import cn.iocoder.yudao.module.iot.service.RawMessageTdEngineService;
import cn.iocoder.yudao.module.iot.util.R;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

/**
 * Raw message TDengine writer.
 */
@Service
@Validated
public class RawMessageTdEngineServiceImpl implements RawMessageTdEngineService {

    @Resource
    private TdengineJsonPayloadWriter payloadWriter;

    @Override
    public R<JSONObject> saveRawMessageData(String deviceId, String sessionId, String message) {
        return payloadWriter.savePayload("iot_raw_message", deviceId, sessionId, message);
    }
}
