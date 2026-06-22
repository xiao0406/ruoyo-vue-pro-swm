package cn.iocoder.yudao.module.iot.job.task.zt;

import cn.iocoder.yudao.module.iot.cache.service.RedisService;
import cn.iocoder.yudao.module.swm.api.constant.SwmRedisKeyConstants;
import cn.iocoder.yudao.module.swm.api.enums.CorpDbEnum;
import cn.iocoder.yudao.module.iot.tcp.model.TcpMessageData;
import cn.iocoder.yudao.module.iot.tcp.processor.AlarmZeroTcpProcessor;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 中泰设备专用定时任务
 */
@Component
public class ZtDeviceMessageTask {

    private static final String LOG_PREFIX = "【中泰-定时任务消息】- ";

    @Resource
    private RedisService redisService;
    @Resource
    private AlarmZeroTcpProcessor alarmZeroTcpProcessor;

    /**
     * 定时下发蓝牙数据
     */
    @XxlJob("sendIbeaconMessage")
    public void sendIbeaconMessage() {
        XxlJobHelper.log("{}开始执行定时下发蓝牙数据", LOG_PREFIX);

        Map<String, String> corpMap = CorpDbEnum.getAllCorpDbMapping();
        for (Map.Entry<String, String> entry : corpMap.entrySet()) {
            String corpCode = entry.getKey();
            // 拼接你的 Redis Key
            String key = corpCode + SwmRedisKeyConstants.SwmKey.ZT_DEVICE_MESSAGE_DATA;

            // 获取这个 Hash 里的 所有 deviceId -> messageData
            Map<Object, Object> deviceInfoMap = redisService.hmget(key);
            if (deviceInfoMap == null && deviceInfoMap.isEmpty()) {
                XxlJobHelper.log("{} ，租户{}没有数据，跳过", LOG_PREFIX, corpCode);
                continue;
            }

            //查询设备在线数量
            List<String> deviceIdList = new ArrayList<>();
                Set<Object> deviceIds = redisService.sGet(corpCode + SwmRedisKeyConstants.IotKey.ONLINE_DEVICES_KEY);
                if (deviceIds != null) {
                    deviceIdList = deviceIds.stream()
                            .filter(Objects::nonNull)
                            .map(Object::toString)
                            .collect(Collectors.toList());
                }


            for (Map.Entry<Object, Object> deviceInfo : deviceInfoMap.entrySet()) {
                String deviceId = deviceInfo.getKey().toString();
                //如果设备不在线，那么清理掉这个缓存
                if (!deviceIdList.contains(deviceId)) {
                    XxlJobHelper.log("{}，设备{}不在线，跳过", LOG_PREFIX, deviceId);
                    //并且清理到这个缓存
                    redisService.hdel(key, deviceId);
                    continue;
                }
                TcpMessageData messageData = (TcpMessageData)deviceInfo.getValue();
                messageData.setScanTimestamp(System.currentTimeMillis());
                //调用定位算法
                XxlJobHelper.log("{},租户{}开始处理设备：{}", LOG_PREFIX,corpCode, deviceId);
                XxlJobHelper.log("{},租户{}处理设备请求体：{}", LOG_PREFIX,corpCode, messageData);
                alarmZeroTcpProcessor.process(messageData);
                XxlJobHelper.log("==========================================================");

            }
        }

    }
}
