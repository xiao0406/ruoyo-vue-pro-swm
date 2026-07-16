package cn.iocoder.yudao.module.iot.service;

import cn.hutool.json.JSONObject;
import cn.iocoder.yudao.module.iot.util.R;
import java.util.Map;

/**
 * 安全帽 SOS TDengine 服务接口
 */
public interface HelmetSosTdEngineService {

    R<JSONObject> saveHelmetSosData(String deviceId, Map<String, Object> data);
}
