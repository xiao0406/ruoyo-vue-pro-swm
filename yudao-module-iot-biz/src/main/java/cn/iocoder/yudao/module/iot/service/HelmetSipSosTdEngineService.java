package cn.iocoder.yudao.module.iot.service;

import cn.iocoder.yudao.module.iot.util.R;
import cn.hutool.json.JSONObject;

import java.util.Map;

/**
 * 安全帽 SIP SOS TDengine 服务接口
 */
public interface HelmetSipSosTdEngineService {

    /**
     * 保存安全帽 SIP SOS 数据
     */
    R<JSONObject> saveHelmetSipSosData(String deviceId, Map<String, Object> data);
}
