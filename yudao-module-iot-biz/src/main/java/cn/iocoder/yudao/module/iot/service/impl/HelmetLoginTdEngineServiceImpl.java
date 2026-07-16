package cn.iocoder.yudao.module.iot.service.impl;

import cn.hutool.json.JSONObject;
import cn.iocoder.yudao.module.iot.service.HelmetLoginTdEngineService;
import cn.iocoder.yudao.module.iot.util.R;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.Map;

/**
 * Helmet login TDengine writer.
 */
@Service
@Validated
public class HelmetLoginTdEngineServiceImpl implements HelmetLoginTdEngineService {

    @Resource
    private TdengineJsonPayloadWriter payloadWriter;

    @Override
    public R<JSONObject> saveHelmetLoginData(String deviceId, String sessionId, Map<String, Object> data) {
        return payloadWriter.savePayload("helmet_login", deviceId, sessionId, data);
    }
}
