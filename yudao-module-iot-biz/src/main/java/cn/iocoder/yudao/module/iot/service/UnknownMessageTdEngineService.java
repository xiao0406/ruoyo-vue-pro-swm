package cn.iocoder.yudao.module.iot.service;

import cn.hutool.json.JSONObject;
import cn.iocoder.yudao.module.iot.util.R;

/**
 * TDengine persistence service for messages that cannot be classified.
 */
public interface UnknownMessageTdEngineService {

    R<JSONObject> saveUnknownMessageData(String deviceId, String sessionId, String messageContent);

    String getUnknownMessageSuperTableName();

}
