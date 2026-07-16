package cn.iocoder.yudao.module.iot.service.impl;

import cn.hutool.json.JSONObject;
import cn.iocoder.yudao.module.iot.service.HelmetLocalRecordTdEngineService;
import cn.iocoder.yudao.module.iot.util.R;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.Map;

/**
 * Helmet local-record TDengine writer.
 */
@Service
@Validated
public class HelmetLocalRecordTdEngineServiceImpl implements HelmetLocalRecordTdEngineService {

    @Resource
    private TdengineJsonPayloadWriter payloadWriter;

    @Override
    public R<JSONObject> saveHelmetLocalRecordData(String deviceId, Map<String, Object> data) {
        return payloadWriter.savePayload("helmet_local_record", deviceId, "default", data);
    }
}
