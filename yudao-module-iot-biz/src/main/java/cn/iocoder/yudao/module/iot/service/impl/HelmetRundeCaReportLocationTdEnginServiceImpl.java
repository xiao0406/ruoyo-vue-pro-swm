package cn.iocoder.yudao.module.iot.service.impl;

import cn.hutool.json.JSONObject;
import cn.iocoder.yudao.module.iot.service.HelmetRundeCaReportLocationTdEnginService;
import cn.iocoder.yudao.module.iot.util.R;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.Map;

/**
 * Helmet Runde CA report-location TDengine writer.
 */
@Service
@Validated
public class HelmetRundeCaReportLocationTdEnginServiceImpl implements HelmetRundeCaReportLocationTdEnginService {

    @Resource
    private TdengineJsonPayloadWriter payloadWriter;

    @Override
    public R<JSONObject> saveHelmetData(String deviceId, Map<String, Object> data) {
        return payloadWriter.savePayload("helmet_runde_ca_report_location", deviceId, "default", data);
    }

    @Override
    public R<JSONObject> saveHelmetRundeData(String deviceId, Object messageData, Map<String, Object> data) {
        return saveHelmetData(deviceId, data);
    }
}
