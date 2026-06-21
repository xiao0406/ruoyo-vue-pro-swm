package cn.iocoder.yudao.module.iot.service;

import java.util.List;
import java.util.Map;

/**
 * GeoPixel 比对数据服务接口
 */
public interface GeoPixelCompareDataService {

    /**
     * 判断指定 Beacon MAC 是否有近期 BLE 数据源（TCP 关联）
     */
    boolean hasRecentBleSourceForTcp(String beaconMac, long timestamp);

    /**
     * 获取最近的决策记录
     */
    List<RecentDecisionRecord> getRecentDecisionRecordsForTcp(String deviceId, int limit);

    /**
     * 保存比对数据（TCP专用）
     */
    void saveCompareDataForTcp(Object messageData, Object bleRawLocationResult,
                               Object mapPixelMatchResult, String finalPixelSource,
                               Long businessTimestamp);

    /**
     * 最近决策记录
     */
    class RecentDecisionRecord {
        private String decisionSource;
        private Long timestamp;

        public String getDecisionSource() { return decisionSource; }
        public void setDecisionSource(String decisionSource) { this.decisionSource = decisionSource; }
        public Long getTimestamp() { return timestamp; }
        public void setTimestamp(Long timestamp) { this.timestamp = timestamp; }
    }
}
