package cn.iocoder.yudao.module.iot.service;

import cn.hutool.json.JSONObject;
import cn.iocoder.yudao.module.iot.util.R;

import java.util.Map;

/**
 * Helmet push-to-client TDengine service.
 */
public interface HelmetPushToClientTdEngineService {

    R<JSONObject> saveHelmetPushToClientData(String deviceId, Map<String, Object> data);
}
