package cn.iocoder.yudao.module.iot.mqtt.handler.zhongtai;

import cn.hutool.json.JSONUtil;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import cn.hutool.core.util.ObjectUtil;
import cn.iocoder.yudao.module.iot.cache.DeviceTenantMappingCache;
import cn.iocoder.yudao.module.iot.cache.service.RedisService;
import cn.iocoder.yudao.module.iot.mqtt.constant.MqttConstants;
import cn.iocoder.yudao.module.iot.mqtt.handler.MqttBusinessHandler;
import cn.iocoder.yudao.module.iot.tcp.model.TcpMessageData;
import cn.iocoder.yudao.module.iot.tcp.processor.AlarmZeroTcpProcessor;
import lombok.Data;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.annotation.Resource;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 设备定位信息处理器
 * 处理设备GPS定位信息MQTT消息
 *
 * @author wxy
 * @date 2026-04-03
 */
@Component
@ConditionalOnProperty(prefix = "iot.mqtt", name = "enabled", havingValue = "true")
public class DeviceGpsLocationHandler implements MqttBusinessHandler {


    private static final Logger logger = LoggerFactory.getLogger(DeviceGpsLocationHandler.class);
    private static final String LOG_PREFIX = "【中泰-MQTT消息-GPS定位信息】- ";
    // 缓存：key = sipAccount，value = 上一次发送时间戳
    private static final ConcurrentHashMap<String, Long> gpsLocationMap = new ConcurrentHashMap<>();

    @Resource
    private RedisService redisService;
    @Resource
    private DeviceTenantMappingCache deviceTenantMappingCache;
    @Resource
    private AlarmZeroTcpProcessor alarmZeroTcpProcessor;


    @Override
    public boolean canHandle(String topic, MqttMessage message) {
        // 检查主题是否包含 /location/gps

        return topic != null && topic.contains(MqttConstants.LOCATION + "/" + MqttConstants.GPS);
    }

    @Override
    public void handle(String topic, MqttMessage message) {

        // 解析消息内容
        String payload = new String(message.getPayload(), StandardCharsets.UTF_8);
        logger.info(LOG_PREFIX+"处理设备GPS定位信息消息: topic={}, payload={}", topic, payload);
        try {
            //定位数据实体类
            TcpMessageData messageData = new TcpMessageData();
            DeviceGpsPosition gpsPosition = null;

            // 1.先校验设备是否入库
            if (!MqttConstants.verifyDeviceExists(topic, messageData, redisService, deviceTenantMappingCache)){
                return;
            }
            String deviceId = messageData.getDeviceId();

            // 2. 解析消息内容
            // 2.1. 解析 MQTT 消息体
            payload = new String(message.getPayload(), StandardCharsets.UTF_8);
            // 2.2. 转换为GPS DTO
            gpsPosition = JSONUtil.toBean(payload, DeviceGpsPosition.class);
            if (ObjectUtil.isEmpty(gpsPosition)){
                return;
            }

            // 3.检验数据是否重复发送
            Long now = gpsPosition.getTime();
            Long lastTime = gpsLocationMap.get(deviceId);
            if (lastTime != null && lastTime.equals(now)){
                logger.info(LOG_PREFIX+"处理设备GPS定位消息: 设备ID={}，数据重复发送，忽略", deviceId);
                return ;
            }else{
                // 添加到缓存
                gpsLocationMap.put(deviceId, now);
            }

            //4.判断是否为GPS定位,不是则返回出去
            if (!gpsPosition.getType().equals("ugps")){
                return;
            }
            //5.设置数据
            messageData.setScanTimestamp(now);
            //拿到电量
            Integer batteryLevel = MqttConstants.getBatteryLevel(deviceId, redisService, deviceTenantMappingCache);
            messageData.setBatteryLevel(batteryLevel);
            messageData.setZTDevice( true);
            messageData.setGpsUtcTimestamp( now);
            //设置经纬度
            String lat = gpsPosition.getLat();
            messageData.setLat(Double.parseDouble(lat));
            String lon = gpsPosition.getLon();
            messageData.setLng(Double.parseDouble( lon));

            //6.调用定位算法
            alarmZeroTcpProcessor.process(messageData);


        }catch (Exception e){
            logger.error(LOG_PREFIX+"处理设备定位信息消息异常: topic={}, payload={}", topic, message.toString(), e);
        }

    }



    /**
     * 设备定位上报实体类
     * 对应 ugps / ibeacon 定位接口报文
     */
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true) // 忽略报文中多余字段
    public class DeviceGpsPosition {

        //类型：ugps=GPS接口
        private String type;
        //用户标识（SIP号）
        private String user;
        //信息发送时间（时间戳 毫秒）
        private Long time;
        //经度
        private String lon;
        //纬度
        private String lat;
        //高度（米）
        private String hig;
        //定位类型 1为普通定位2为定点普通定位，4为定点RTK，5为浮点RTK，6 基站定位，7 蓝牙，8 UWB
        private String chafenStatus;
        //信号强度（蓝牙）
        private String rssi;
        //类型（蓝牙）：ibeacon
        private String type2;
        //编号
        private String minor;
        //组号
        private String major;
        //电量（蓝牙）
        private String battery;
        //卫星数
        private String satellites;
        //定位状态：1=有定位，0=无定位
        private String dingweiStatus;
        //速度和方向（JSON字符串：{"speed":"0","direction":"0"}）
        private String info;
        //用户ID（智能柜）
        private Long userId;
        //设备ID/事件ID
        private String es_id;
        //坐标X
        private String x;
        //坐标Y
        private String y;
        //服务器时间戳
        private String serverTime;
        //地图ID
        private String mapId;
    }
}