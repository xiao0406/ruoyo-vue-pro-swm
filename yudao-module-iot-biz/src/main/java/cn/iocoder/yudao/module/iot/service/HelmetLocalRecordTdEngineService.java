package cn.iocoder.yudao.module.iot.service;

import cn.hutool.json.JSONObject;
import cn.iocoder.yudao.module.iot.util.R;

import java.util.Map;

/**
 * Helmet local-record TDengine service.
 */
public interface HelmetLocalRecordTdEngineService {

    R<JSONObject> saveHelmetLocalRecordData(String deviceId, Map<String, Object> data);
}
