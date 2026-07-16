package cn.iocoder.yudao.module.iot.service;

import java.util.List;
import java.util.Map;

/**
 * 定位引擎服务接口
 */
public interface LocationEngineService {

    /**
     * 获取坐标
     *
     * @param args 参数
     * @return 坐标结果
     */
    Object getCoordinate(Object... args);

    /**
     * 调用定位引擎接口
     *
     * @param deviceId       设备ID
     * @param beaconDataList 蓝牙信标数据列表
     * @param scanTimestamp  扫描时间戳（毫秒），为null时使用当前时间
     * @return 定位结果
     */
    Map<String, Object> callLocationEngine(String deviceId, List<Map<String, Object>> beaconDataList, Long scanTimestamp);

    /**
     * 调用定位引擎接口（带原始消息）
     *
     * @param deviceId       设备ID
     * @param beaconDataList 蓝牙信标数据列表
     * @param scanTimestamp  扫描时间戳（毫秒），为null时使用当前时间
     * @param rawMessage     原始消息
     * @return 定位结果
     */
    Map<String, Object> callLocationEngine(String deviceId, List<Map<String, Object>> beaconDataList, Long scanTimestamp, String rawMessage);

    default ExternalWriteResult saveLocationToExternalTableForTcp(String deviceId, Map<String, Object> helmetData,
                                                                  Long businessTimestamp) {
        return ExternalWriteResult.notWritten("not implemented");
    }

    /**
     * 获取当前配置的引擎类型
     */
    String getEngineType();

    /**
     * 检查Chat引擎是否可用
     */
    boolean isChatEngineAvailable();

    /**
     * 检查历史坐标查询是否启用
     */
    boolean isHistoryEnabled();

    /**
     * 获取历史坐标查询的时间间隔
     */
    int getHistorySeconds();

    /**
     * external_coordinate_data 写入结果
     */
    final class ExternalWriteResult {
        private final boolean written;
        private final String source;
        private final String reason;

        private ExternalWriteResult(boolean written, String source, String reason) {
            this.written = written;
            this.source = source;
            this.reason = reason;
        }

        public static ExternalWriteResult written(String source) {
            return new ExternalWriteResult(true, source, null);
        }

        public static ExternalWriteResult notWritten(String reason) {
            return new ExternalWriteResult(false, null, reason);
        }

        public static ExternalWriteResult writeFailed(String source, String reason) {
            return new ExternalWriteResult(false, source, reason);
        }

        public boolean isWritten() { return written; }
        public String getSource() { return source; }
        public String getReason() { return reason; }
    }

}
