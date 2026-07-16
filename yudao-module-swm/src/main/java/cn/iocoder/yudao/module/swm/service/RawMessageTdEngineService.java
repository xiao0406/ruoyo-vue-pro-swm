package cn.iocoder.yudao.module.swm.service;

import cn.hutool.json.JSONObject;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;

/**
 * 原始报文 TDengine 存储 Service 接口
 */
public interface RawMessageTdEngineService {

    /**
     * 保存原始报文到 TDengine
     *
     * @param deviceCode 设备编号
     * @param message    原始报文
     */
    void saveRawMessage(String deviceCode, String message);

    default CommonResult<JSONObject> saveRawMessageData(String deviceId, String sessionId, String message) {
        saveRawMessage(deviceId, message);
        return CommonResult.success(new JSONObject());
    }

}
