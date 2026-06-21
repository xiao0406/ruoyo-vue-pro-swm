package cn.iocoder.yudao.module.iot.service;

import cn.iocoder.yudao.module.iot.util.R;
import cn.hutool.json.JSONObject;

import java.util.Map;

/**
 * 安全帽发送消息 TDengine 服务接口
 */
public interface HelmetSendingMessageTdEngineService {

    /**
     * 保存安全帽发送消息数据
     */
    R<JSONObject> saveHelmetSendingMessageData(String deviceId, Map<String, Object> data);
}
