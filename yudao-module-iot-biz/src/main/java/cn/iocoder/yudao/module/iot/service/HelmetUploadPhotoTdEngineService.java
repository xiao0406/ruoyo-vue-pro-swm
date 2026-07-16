package cn.iocoder.yudao.module.iot.service;

import cn.hutool.json.JSONObject;
import cn.iocoder.yudao.module.iot.util.R;

import java.util.Map;

/**
 * Helmet upload-photo TDengine service.
 */
public interface HelmetUploadPhotoTdEngineService {

    R<JSONObject> saveHelmetUploadPhotoData(String deviceId, Map<String, Object> data);
}
