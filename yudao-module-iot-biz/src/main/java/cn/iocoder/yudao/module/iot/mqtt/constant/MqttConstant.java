package cn.iocoder.yudao.module.iot.mqtt.constant;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.iocoder.yudao.module.iot.cache.DeviceCorpMappingCache;
import cn.iocoder.yudao.module.iot.cache.service.RedisService;
import cn.iocoder.yudao.module.swm.api.constant.SwmRedisKeyConstants;
import cn.iocoder.yudao.module.iot.tcp.model.TcpMessageData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URLEncoder;

import java.util.*;


/**
 * Mqtt常量
 * 基于服务器后台信息接口_MQTT订阅文档定义
 */
public class MqttConstant {

    private static final Logger logger = LoggerFactory.getLogger(MqttConstant.class);

    private static final String LOG_PREFIX = "【中泰MQTT消息-】- ";

    /*************************** MQTT连接基础配置 ***************************/
     //MQTT服务端地址
    public static final String MQTT_HOST = "tcp://aqgk.imdm.com.cn:1883";


    //MQTT用户名
    public static final String MQTT_USER_NAME = "ceict_admin";

    //MQTT密码
    public static final String MQTT_PASSWORD = "ceict_2020xx";

    //MQTT超时时间(秒)
    public static final int MQTT_TIMEOUT = 10;

    //MQTT心跳时间(秒)
    public static final int MQTT_KEEP_ALIVE = 20;


    /*************************** MQTT主题通用格式 ***************************/
    //MQTT主题根路径
    public static final String MQTT_TOPIC_ROOT = "/cmt/IoT/pub/";


    /*************************** MQTT主题各层级固定标识 ***************************/
    //主题-事件类型：状态类
    public static final String STATUS = "status";

    //主题-事件类型：传感器类
    public static final String SENSOR = "sensor";

    //主题-事件类型：定位类
    public static final String LOCATION = "location";

    //主题-事件类型：告警类
    public static final String ALARM = "alarm";

    //主题-事件类型：UWB围栏告警
    public static final String FENCE_ALARM = "fenceAlarm";

    /*************************** 状态类型-事件子类型 ***************************/
    //状态-子类型：静默状态
    public static final String SILENT = "silent";

    //状态-子类型：在线状态
    public static final String ONLINE = "online";

    //状态-子类型：设备状态
    public static final String RUN_STATUS = "runStatus";

    //状态-子类型：登高状态
    public static final String CLIMB = "climb";

    //状态-子类型：登高相对高度
    public static final String CLIMB_HIG = "climbHig";

    //状态-子类型：人员绑定状态
    public static final String BIND = "bind";


    /*************************** 传感器类型-事件子类型 ***************************/
    //传感器-子类型：电量信息
    public static final String BATTERY = "battery";
    //传感器-子类型：蓝牙iBeacon信息
    public static final String IBEACON = "ibeacon";


    /*************************** 定位类型-事件子类型 ***************************/
    //定位-子类型：UWB定位
    public static final String UWB = "uwb";
    //定位-子类型：GPS定位
    public static final String GPS = "gps";


    /*************************** 告警类型-事件子类型 ***************************/
    //SOS告警
    public static final String SOS = "SOS";
    //脱帽/戴帽告警
    public static final String UNBONNET = "Unbonnet";
    //跌倒告警
    public static final String FALL = "Fall";
    //近电告警
    public static final String JINDIAN = "JinDian";
    //围栏告警
    public static final String INOUT = "inout";
    //脑电告警
    public static final String BRAIN = "brain";
    //低电量告警
    public static final String SUBTYPE_BATTERY = "Battery";
    //AI视频告警
    public static final String AI_ALARM = "AiAlarm";
    //入侵告警
    public static final String RU_QIN = "ru_qin";



    /**
     * 私有化构造器，禁止实例化
     */
    private MqttConstant() {
        throw new UnsupportedOperationException("This is a constant class and cannot be instantiated");
    }


    /*************************** 动态订阅主题生成方法 ***************************/
    /**
     * 生成设备在线状态通配符主题
     * 格式：/cmt/IoT/pub/+/+/status/online
     * 说明：第一个 + 表示任意部门路径，第二个 + 表示任意用户 SIP
     * @return 在线状态通配符主题
     */
    public static String getOnlineStatusWildcardTopic() {
        return MqttConstant.MQTT_TOPIC_ROOT + "+/+/" + STATUS + "/" + ONLINE;
    }

    /**
     * 生成指定设备的在线状态主题
     * @param userSip 用户 SIP
     * @return 在线状态主题
     */
    public static String getOnlineStatusTopic(String userSip) {
        return MqttConstant.MQTT_TOPIC_ROOT + "+/" + userSip + "/" + STATUS + "/" + ONLINE;
    }

    /**
     * 生成设备运行状态通配符主题
     * 格式：/cmt/IoT/pub/+/+/status/runStatus
     * @return 运行状态通配符主题
     */
    public static String getRunStatusWildcardTopic() {
        return MqttConstant.MQTT_TOPIC_ROOT + "+/+/" + STATUS + "/" + RUN_STATUS;
    }

    /**
     * 生成传感器数据通配符主题
     * 格式：/cmt/IoT/pub/+/+/sensor/{subtype}
     * 
     * @param subtype 传感器子类型（如 battery, ibeacon 等）
     * @return 传感器数据通配符主题
     */
    public static String getSensorWildcardTopic(String subtype) {
        return MqttConstant.MQTT_TOPIC_ROOT + "+/+/" + SENSOR + "/" + subtype;
    }

    /**
     * 生成定位数据通配符主题
     * 格式：/cmt/IoT/pub/+/+/location/{type}
     * 
     * @param type 定位类型（如 uwb, gps 等）
     * @return 定位数据通配符主题
     */
    public static String getLocationWildcardTopic(String type) {
        return MqttConstant.MQTT_TOPIC_ROOT + "+/+/" + LOCATION + "/" + type;
    }

    /**
     * 生成告警数据通配符主题
     * 格式：/cmt/IoT/pub/+/+/alarm/{alarmType}
     * 
     * @param alarmType 告警类型（如 SOS, Fall, JinDian 等）
     * @return 告警数据通配符主题
     */
    public static String getAlarmWildcardTopic(String alarmType) {
        return MqttConstant.MQTT_TOPIC_ROOT + "+/+/"  + ALARM + "/" + alarmType;
    }

    // 拼接蓝牙信标传感器数据订阅主题
    public static String getIbeaconTopic(String deptPath, String userSip,String topicType,String subtype) {
        return MqttConstant.MQTT_TOPIC_ROOT + deptPath + "/" + userSip
                + "/" + topicType
                + "/" + subtype;
    }



//    =============================================================================



    /**
     * 校验设备是否存在
     */
    public static boolean verifyDeviceExists(String topic, TcpMessageData messageData, RedisService redisService, DeviceCorpMappingCache deviceCorpMappingCache){
        try {
            String[] topicParts = topic.split("/");
            if (topicParts.length < 6) {
                logger.info("Topic格式不符合预期，长度不足6段: {}", topic);
                return false;  // 格式错误直接拒绝
            }
            String deviceId = topicParts[5];
            Set<Object> set = redisService.sGet(SwmRedisKeyConstants.GlobalKey.ZT_DEVICE_ID_ALL);
            List<String> list = new ArrayList<>();
            if (set != null) {
                for (Object obj : set) { list.add(String.valueOf(obj)); }
            }
            if (!list.contains(deviceId)) {
                logger.info("中泰MQTT消息设备校验未通过，设备不在缓存中: topic={}, deviceId={}, cacheSize={}", topic, deviceId, list.size());
                return false;
            }

            messageData.setDeviceId(deviceId);
            bindDeviceDbInfo(messageData, deviceCorpMappingCache);
            return true;

        } catch (Exception e) {
            logger.error("校验设备是否存在异常：{}", e.getMessage());
            return false;  // 异常时拒绝，而非放行
        }
    }


    /**
     * 绑定设备库信息
     */
    private static void bindDeviceDbInfo(TcpMessageData messageData, DeviceCorpMappingCache deviceCorpMappingCache) {
        String deviceId = messageData.getDeviceId();
        if (deviceId == null){
            return;
        }
        String dbName = deviceCorpMappingCache.getDbName(deviceId);
        messageData.setDbName(dbName);
    }

    /**
     * 判断是否需要报警
     * @param alarmKey
     * @return
     */
    public static boolean isAlarm(String alarmKey, String deviceId, RedisService redisService, DeviceCorpMappingCache deviceCorpMappingCache){
        boolean isAlarm = true;
        String corpCode = deviceCorpMappingCache.getCorpCode(deviceId);
        String redisKey = corpCode + SwmRedisKeyConstants.SwmKey.ALARM_CONFIG;

        //1 -是  0-否
        Object hget = redisService.hget(redisKey, alarmKey);
        if (hget != null){
            Integer enableAlarm = (Integer) hget;
            if (enableAlarm == 0){
                isAlarm = false;
            }
        }
        return isAlarm ;
    }

    /**
     * 获取设备电量
     * @param deviceId
     * @param redisService
     * @param deviceCorpMappingCache
     * @return
     */
    public static Integer getBatteryLevel(String deviceId, RedisService redisService, DeviceCorpMappingCache deviceCorpMappingCache){
        String corpCode = deviceCorpMappingCache.getCorpCode(deviceId);
        Integer batteryLevel = (Integer) redisService.hget(corpCode + SwmRedisKeyConstants.SwmKey.ZT_DEVICE_BATTERY, deviceId);
        return batteryLevel;
    }

}
