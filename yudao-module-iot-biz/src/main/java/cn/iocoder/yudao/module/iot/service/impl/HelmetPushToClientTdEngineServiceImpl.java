package cn.iocoder.yudao.module.iot.service.impl;

import cn.hutool.json.JSONObject;
import cn.iocoder.yudao.module.iot.service.HelmetPushToClientTdEngineService;
import cn.iocoder.yudao.module.iot.util.R;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.Map;

/**
 * Helmet push-to-client TDengine writer.
 */
@Service
@Validated
public class HelmetPushToClientTdEngineServiceImpl implements HelmetPushToClientTdEngineService {

    @Resource
    private TdengineJsonPayloadWriter payloadWriter;

    @Override
    public R<JSONObject> saveHelmetPushToClientData(String deviceId, Map<String, Object> data) {
        return payloadWriter.savePayload("helmet_push_to_client", deviceId, "default", data);
    }
}
