package cn.iocoder.yudao.module.iot.service;

import cn.iocoder.yudao.module.iot.util.R;
import cn.hutool.json.JSONObject;

import java.util.Map;

/**
 * 安全帽 Runde CA 上报位置 TDengine 服务接口
 * 注意：类名拼写为 Engin（非 Engine），与原有代码保持一致
 */
public interface HelmetRundeCaReportLocationTdEnginService {

    /**
     * 保存安全帽数据
     */
    R<JSONObject> saveHelmetData(String deviceId, Map<String, Object> data);
}
