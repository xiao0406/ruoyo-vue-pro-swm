package cn.iocoder.yudao.module.iot.job.task.zt;

import cn.iocoder.yudao.module.iot.cache.service.RedisService;
import cn.iocoder.yudao.module.iot.tcp.model.TcpMessageData;
import cn.iocoder.yudao.module.iot.tcp.processor.AlarmZeroTcpProcessor;
import cn.iocoder.yudao.module.swm.api.constant.SwmRedisKeyConstants;
import cn.iocoder.yudao.module.swm.api.enums.TenantDbEnum;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Component
public class ZtDeviceMessageTask {

    private static final String LOG_PREFIX = "ZT device message task";

    @Resource
    private RedisService redisService;
    @Resource
    private AlarmZeroTcpProcessor alarmZeroTcpProcessor;

    @XxlJob("sendIbeaconMessage")
    public void sendIbeaconMessage() {
        XxlJobHelper.log("{} started", LOG_PREFIX);

        Map<Long, String> tenantDbMap = TenantDbEnum.getAllTenantDbMapping();
        for (Map.Entry<Long, String> entry : tenantDbMap.entrySet()) {
            String tenantKey = String.valueOf(entry.getKey());
            String key = tenantKey + SwmRedisKeyConstants.SwmKey.ZT_DEVICE_MESSAGE_DATA;

            Map<Object, Object> deviceInfoMap = redisService.hmget(key);
            if (deviceInfoMap == null || deviceInfoMap.isEmpty()) {
                XxlJobHelper.log("{} tenant {} has no cached message", LOG_PREFIX, tenantKey);
                continue;
            }

            List<String> onlineDeviceIds = new ArrayList<>();
            Set<Object> deviceIds = redisService.sGet(tenantKey + SwmRedisKeyConstants.IotKey.ONLINE_DEVICES_KEY);
            if (deviceIds != null) {
                onlineDeviceIds = deviceIds.stream()
                        .filter(Objects::nonNull)
                        .map(Object::toString)
                        .collect(Collectors.toList());
            }

            for (Map.Entry<Object, Object> deviceInfo : deviceInfoMap.entrySet()) {
                String deviceId = String.valueOf(deviceInfo.getKey());
                if (!onlineDeviceIds.contains(deviceId)) {
                    XxlJobHelper.log("{} device {} is offline, remove cached message", LOG_PREFIX, deviceId);
                    redisService.hdel(key, deviceId);
                    continue;
                }
                TcpMessageData messageData = (TcpMessageData) deviceInfo.getValue();
                messageData.setScanTimestamp(System.currentTimeMillis());
                XxlJobHelper.log("{} tenant {} processing device {}", LOG_PREFIX, tenantKey, deviceId);
                alarmZeroTcpProcessor.process(messageData);
            }
        }
    }
}
