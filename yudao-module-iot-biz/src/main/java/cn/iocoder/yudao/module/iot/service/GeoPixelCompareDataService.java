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
    default List<RecentDecisionRecord> getRecentDecisionRecordsForTcp(String deviceId, int limit) {
        return List.of();
    }

    /**
     * 保存比对数据（TCP专用）
     */
    default void saveCompareDataForTcp(Object messageData, Object bleRawLocationResult,
                                       Object mapPixelMatchResult, String finalPixelSource,
                                       Long businessTimestamp) {
    }

    default void saveCompareDataForTcp(Object messageData, Object helmetData,
                                       Object mapPixelMatchResult, boolean indoorCandidate,
                                       int beaconCount, String finalPixelSource,
                                       Double finalX, Double finalY, String reason) {
        saveCompareDataForTcp(messageData, helmetData, mapPixelMatchResult, finalPixelSource, null);
    }

    /**
     * 最近决策记录
     */
    class RecentDecisionRecord {
        private String decisionSource;
        private Long timestamp;
        private Long businessTimestamp;
        private Boolean indoorCandidate;

        public String getDecisionSource() { return decisionSource; }
        public void setDecisionSource(String decisionSource) { this.decisionSource = decisionSource; }
        public Long getTimestamp() { return timestamp; }
        public void setTimestamp(Long timestamp) { this.timestamp = timestamp; }
        public Long getBusinessTimestamp() { return businessTimestamp != null ? businessTimestamp : timestamp; }
        public void setBusinessTimestamp(Long businessTimestamp) { this.businessTimestamp = businessTimestamp; }
        public Boolean getIndoorCandidate() { return indoorCandidate; }
        public void setIndoorCandidate(Boolean indoorCandidate) { this.indoorCandidate = indoorCandidate; }
    }
}
