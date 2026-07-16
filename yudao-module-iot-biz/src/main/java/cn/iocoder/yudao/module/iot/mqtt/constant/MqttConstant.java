package cn.iocoder.yudao.module.iot.mqtt.constant;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.iocoder.yudao.module.iot.cache.DeviceTenantMappingCache;
import cn.iocoder.yudao.module.iot.cache.service.RedisService;
import cn.iocoder.yudao.module.swm.api.constant.SwmRedisKeyConstants;
import cn.iocoder.yudao.module.iot.tcp.model.TcpMessageData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URLEncoder;

import java.util.*;


/**
 * Mqtt甯搁噺
 * 鍩轰簬鏈嶅姟鍣ㄥ悗鍙颁俊鎭帴鍙MQTT璁㈤槄鏂囨。瀹氫箟
 */
public class MqttConstant {

    private static final Logger logger = LoggerFactory.getLogger(MqttConstant.class);

    private static final String LOG_PREFIX = "銆愪腑娉癕QTT娑堟伅-銆? ";

    /*************************** MQTT杩炴帴鍩虹閰嶇疆 ***************************/
     //MQTT鏈嶅姟绔湴鍧€
    public static final String MQTT_HOST = "tcp://aqgk.imdm.com.cn:1883";


    //MQTT鐢ㄦ埛鍚?
    public static final String MQTT_USER_NAME = "ceict_admin";

    //MQTT瀵嗙爜
    public static final String MQTT_PASSWORD = "ceict_2020xx";

    //MQTT瓒呮椂鏃堕棿(绉?
    public static final int MQTT_TIMEOUT = 10;

    //MQTT蹇冭烦鏃堕棿(绉?
    public static final int MQTT_KEEP_ALIVE = 20;


    /*************************** MQTT涓婚閫氱敤鏍煎紡 ***************************/
    //MQTT涓婚鏍硅矾寰?
    public static final String MQTT_TOPIC_ROOT = "/cmt/IoT/pub/";


    /*************************** MQTT涓婚鍚勫眰绾у浐瀹氭爣璇?***************************/
    //涓婚-浜嬩欢绫诲瀷锛氱姸鎬佺被
    public static final String STATUS = "status";

    //涓婚-浜嬩欢绫诲瀷锛氫紶鎰熷櫒绫?
    public static final String SENSOR = "sensor";

    //涓婚-浜嬩欢绫诲瀷锛氬畾浣嶇被
    public static final String LOCATION = "location";

    //涓婚-浜嬩欢绫诲瀷锛氬憡璀︾被
    public static final String ALARM = "alarm";

    //涓婚-浜嬩欢绫诲瀷锛歎WB鍥存爮鍛婅
    public static final String FENCE_ALARM = "fenceAlarm";

    /*************************** 鐘舵€佺被鍨?浜嬩欢瀛愮被鍨?***************************/
    //鐘舵€?瀛愮被鍨嬶細闈欓粯鐘舵€?
    public static final String SILENT = "silent";

    //鐘舵€?瀛愮被鍨嬶細鍦ㄧ嚎鐘舵€?
    public static final String ONLINE = "online";

    //鐘舵€?瀛愮被鍨嬶細璁惧鐘舵€?
    public static final String RUN_STATUS = "runStatus";

    //鐘舵€?瀛愮被鍨嬶細鐧婚珮鐘舵€?
    public static final String CLIMB = "climb";

    //鐘舵€?瀛愮被鍨嬶細鐧婚珮鐩稿楂樺害
    public static final String CLIMB_HIG = "climbHig";

    //鐘舵€?瀛愮被鍨嬶細浜哄憳缁戝畾鐘舵€?
    public static final String BIND = "bind";


    /*************************** 浼犳劅鍣ㄧ被鍨?浜嬩欢瀛愮被鍨?***************************/
    //浼犳劅鍣?瀛愮被鍨嬶細鐢甸噺淇℃伅
    public static final String BATTERY = "battery";
    //浼犳劅鍣?瀛愮被鍨嬶細钃濈墮iBeacon淇℃伅
    public static final String IBEACON = "ibeacon";


    /*************************** 瀹氫綅绫诲瀷-浜嬩欢瀛愮被鍨?***************************/
    //瀹氫綅-瀛愮被鍨嬶細UWB瀹氫綅
    public static final String UWB = "uwb";
    //瀹氫綅-瀛愮被鍨嬶細GPS瀹氫綅
    public static final String GPS = "gps";


    /*************************** 鍛婅绫诲瀷-浜嬩欢瀛愮被鍨?***************************/
    //SOS鍛婅
    public static final String SOS = "SOS";
    //鑴卞附/鎴村附鍛婅
    public static final String UNBONNET = "Unbonnet";
    //璺屽€掑憡璀?
    public static final String FALL = "Fall";
    //杩戠數鍛婅
    public static final String JINDIAN = "JinDian";
    //鍥存爮鍛婅
    public static final String INOUT = "inout";
    //鑴戠數鍛婅
    public static final String BRAIN = "brain";
    //浣庣數閲忓憡璀?
    public static final String SUBTYPE_BATTERY = "Battery";
    //AI瑙嗛鍛婅
    public static final String AI_ALARM = "AiAlarm";
    //鍏ヤ镜鍛婅
    public static final String RU_QIN = "ru_qin";



    /**
     * 绉佹湁鍖栨瀯閫犲櫒锛岀姝㈠疄渚嬪寲
     */
    private MqttConstant() {
        throw new UnsupportedOperationException("This is a constant class and cannot be instantiated");
    }


    /*************************** 鍔ㄦ€佽闃呬富棰樼敓鎴愭柟娉?***************************/
    /**
     * 鐢熸垚璁惧鍦ㄧ嚎鐘舵€侀€氶厤绗︿富棰?
     * 鏍煎紡锛?cmt/IoT/pub/+/+/status/online
     * 璇存槑锛氱涓€涓?+ 琛ㄧず浠绘剰閮ㄩ棬璺緞锛岀浜屼釜 + 琛ㄧず浠绘剰鐢ㄦ埛 SIP
     * @return 鍦ㄧ嚎鐘舵€侀€氶厤绗︿富棰?
     */
    public static String getOnlineStatusWildcardTopic() {
        return MqttConstant.MQTT_TOPIC_ROOT + "+/+/" + STATUS + "/" + ONLINE;
    }

    /**
     * 鐢熸垚鎸囧畾璁惧鐨勫湪绾跨姸鎬佷富棰?
     * @param userSip 鐢ㄦ埛 SIP
     * @return 鍦ㄧ嚎鐘舵€佷富棰?
     */
    public static String getOnlineStatusTopic(String userSip) {
        return MqttConstant.MQTT_TOPIC_ROOT + "+/" + userSip + "/" + STATUS + "/" + ONLINE;
    }

    /**
     * 鐢熸垚璁惧杩愯鐘舵€侀€氶厤绗︿富棰?
     * 鏍煎紡锛?cmt/IoT/pub/+/+/status/runStatus
     * @return 杩愯鐘舵€侀€氶厤绗︿富棰?
     */
    public static String getRunStatusWildcardTopic() {
        return MqttConstant.MQTT_TOPIC_ROOT + "+/+/" + STATUS + "/" + RUN_STATUS;
    }

    /**
     * 鐢熸垚浼犳劅鍣ㄦ暟鎹€氶厤绗︿富棰?
     * 鏍煎紡锛?cmt/IoT/pub/+/+/sensor/{subtype}
     * 
     * @param subtype 浼犳劅鍣ㄥ瓙绫诲瀷锛堝 battery, ibeacon 绛夛級
     * @return 浼犳劅鍣ㄦ暟鎹€氶厤绗︿富棰?
     */
    public static String getSensorWildcardTopic(String subtype) {
        return MqttConstant.MQTT_TOPIC_ROOT + "+/+/" + SENSOR + "/" + subtype;
    }

    /**
     * 鐢熸垚瀹氫綅鏁版嵁閫氶厤绗︿富棰?
     * 鏍煎紡锛?cmt/IoT/pub/+/+/location/{type}
     * 
     * @param type 瀹氫綅绫诲瀷锛堝 uwb, gps 绛夛級
     * @return 瀹氫綅鏁版嵁閫氶厤绗︿富棰?
     */
    public static String getLocationWildcardTopic(String type) {
        return MqttConstant.MQTT_TOPIC_ROOT + "+/+/" + LOCATION + "/" + type;
    }

    /**
     * 鐢熸垚鍛婅鏁版嵁閫氶厤绗︿富棰?
     * 鏍煎紡锛?cmt/IoT/pub/+/+/alarm/{alarmType}
     * 
     * @param alarmType 鍛婅绫诲瀷锛堝 SOS, Fall, JinDian 绛夛級
     * @return 鍛婅鏁版嵁閫氶厤绗︿富棰?
     */
    public static String getAlarmWildcardTopic(String alarmType) {
        return MqttConstant.MQTT_TOPIC_ROOT + "+/+/"  + ALARM + "/" + alarmType;
    }

    // 鎷兼帴钃濈墮淇℃爣浼犳劅鍣ㄦ暟鎹闃呬富棰?
    public static String getIbeaconTopic(String deptPath, String userSip,String topicType,String subtype) {
        return MqttConstant.MQTT_TOPIC_ROOT + deptPath + "/" + userSip
                + "/" + topicType
                + "/" + subtype;
    }



//    =============================================================================



    /**
     * 鏍￠獙璁惧鏄惁瀛樺湪
     */
    public static boolean verifyDeviceExists(String topic, TcpMessageData messageData, RedisService redisService, DeviceTenantMappingCache deviceTenantMappingCache){
        try {
            String[] topicParts = topic.split("/");
            if (topicParts.length < 6) {
                logger.info("Topic鏍煎紡涓嶇鍚堥鏈燂紝闀垮害涓嶈冻6娈? {}", topic);
                return false;  // 鏍煎紡閿欒鐩存帴鎷掔粷
            }
            String deviceId = topicParts[5];
            Set<Object> set = redisService.sGet(SwmRedisKeyConstants.GlobalKey.ZT_DEVICE_ID_ALL);
            List<String> list = new ArrayList<>();
            if (set != null) {
                for (Object obj : set) { list.add(String.valueOf(obj)); }
            }
            if (!list.contains(deviceId)) {
                logger.info("涓嘲MQTT娑堟伅璁惧鏍￠獙鏈€氳繃锛岃澶囦笉鍦ㄧ紦瀛樹腑: topic={}, deviceId={}, cacheSize={}", topic, deviceId, list.size());
                return false;
            }

            messageData.setDeviceId(deviceId);
            bindDeviceDbInfo(messageData, deviceTenantMappingCache);
            return true;

        } catch (Exception e) {
            logger.error("鏍￠獙璁惧鏄惁瀛樺湪寮傚父锛歿}", e.getMessage());
            return false;  // 寮傚父鏃舵嫆缁濓紝鑰岄潪鏀捐
        }
    }


    /**
     * 缁戝畾璁惧搴撲俊鎭?
     */
    private static void bindDeviceDbInfo(TcpMessageData messageData, DeviceTenantMappingCache deviceTenantMappingCache) {
        String deviceId = messageData.getDeviceId();
        if (deviceId == null){
            return;
        }
        String dbName = deviceTenantMappingCache.getDbName(deviceId);
        messageData.setDbName(dbName);
    }

    /**
     * 鍒ゆ柇鏄惁闇€瑕佹姤璀?
     * @param alarmKey
     * @return
     */
    public static boolean isAlarm(String alarmKey, String deviceId, RedisService redisService, DeviceTenantMappingCache deviceTenantMappingCache){
        boolean isAlarm = true;
        String tenantKey = deviceTenantMappingCache.getTenantKey(deviceId);
        String redisKey = tenantKey + SwmRedisKeyConstants.SwmKey.ALARM_CONFIG;

        //1 -鏄? 0-鍚?
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
     * 鑾峰彇璁惧鐢甸噺
     * @param deviceId
     * @param redisService
     * @param deviceTenantMappingCache
     * @return
     */
    public static Integer getBatteryLevel(String deviceId, RedisService redisService, DeviceTenantMappingCache deviceTenantMappingCache){
        String tenantKey = deviceTenantMappingCache.getTenantKey(deviceId);
        Integer batteryLevel = (Integer) redisService.hget(tenantKey + SwmRedisKeyConstants.SwmKey.ZT_DEVICE_BATTERY, deviceId);
        return batteryLevel;
    }

}

