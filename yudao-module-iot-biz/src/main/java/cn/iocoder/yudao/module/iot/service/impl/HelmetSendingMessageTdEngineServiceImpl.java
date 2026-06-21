package cn.iocoder.yudao.module.iot.service.impl;

import cn.iocoder.yudao.module.iot.service.HelmetSendingMessageTdEngineService;
import cn.iocoder.yudao.module.iot.util.R;
import cn.hutool.json.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.Map;

/**
 * 安全帽发送消息 TDengine 服务实现
 */
@Service
@Validated
@Slf4j
public class HelmetSendingMessageTdEngineServiceImpl implements HelmetSendingMessageTdEngineService {

    @Override
    public R<JSONObject> saveHelmetSendingMessageData(String deviceId, Map<String, Object> data) {
        // TODO: 实现保存安全帽发送消息数据到 TDengine
        log.warn("saveHelmetSendingMessageData 尚未实现, deviceId={}", deviceId);
        return R.ok(new JSONObject());
    }
}
