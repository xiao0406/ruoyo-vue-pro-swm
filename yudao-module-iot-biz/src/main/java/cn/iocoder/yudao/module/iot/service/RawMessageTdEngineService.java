package cn.iocoder.yudao.module.iot.service;

import cn.hutool.json.JSONObject;
import cn.iocoder.yudao.module.iot.util.R;

/**
 * Raw TCP/MQTT message TDengine service.
 */
public interface RawMessageTdEngineService {

    R<JSONObject> saveRawMessageData(String deviceId, String sessionId, String message);
}
