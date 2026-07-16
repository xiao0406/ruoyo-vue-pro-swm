package cn.iocoder.yudao.module.iot.service.impl;

import cn.hutool.json.JSONObject;
import cn.iocoder.yudao.module.iot.service.HelmetUploadPhotoTdEngineService;
import cn.iocoder.yudao.module.iot.util.R;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.Map;

/**
 * Helmet upload-photo TDengine writer.
 */
@Service
@Validated
public class HelmetUploadPhotoTdEngineServiceImpl implements HelmetUploadPhotoTdEngineService {

    @Resource
    private TdengineJsonPayloadWriter payloadWriter;

    @Override
    public R<JSONObject> saveHelmetUploadPhotoData(String deviceId, Map<String, Object> data) {
        return payloadWriter.savePayload("helmet_upload_photo", deviceId, "default", data);
    }
}
