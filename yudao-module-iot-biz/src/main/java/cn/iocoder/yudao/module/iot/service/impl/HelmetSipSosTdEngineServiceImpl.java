package cn.iocoder.yudao.module.iot.service.impl;

import cn.hutool.json.JSONObject;
import cn.iocoder.yudao.module.iot.service.HelmetSipSosTdEngineService;
import cn.iocoder.yudao.module.iot.util.R;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.Map;

/**
 * Helmet SIP SOS TDengine writer.
 */
@Service
@Validated
public class HelmetSipSosTdEngineServiceImpl implements HelmetSipSosTdEngineService {

    @Resource
    private TdengineJsonPayloadWriter payloadWriter;

    @Override
    public R<JSONObject> saveHelmetSipSosData(String deviceId, Map<String, Object> data) {
        return payloadWriter.savePayload("helmet_sip_sos", deviceId, "default", data);
    }
}
