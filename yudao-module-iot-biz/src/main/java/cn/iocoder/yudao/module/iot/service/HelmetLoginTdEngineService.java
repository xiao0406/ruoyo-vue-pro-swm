package cn.iocoder.yudao.module.iot.service;

import cn.hutool.json.JSONObject;
import cn.iocoder.yudao.module.iot.util.R;

import java.util.Map;

/**
 * Helmet login TDengine service.
 */
public interface HelmetLoginTdEngineService {

    R<JSONObject> saveHelmetLoginData(String deviceId, String sessionId, Map<String, Object> data);
}
