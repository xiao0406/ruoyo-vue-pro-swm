package cn.iocoder.yudao.module.swm.service;

import java.util.List;

/**
 * 地图像素比对数据 Service 接口
 */
public interface GeoPixelCompareDataService {

    /**
     * 保存比对数据
     *
     * @param mapId     地图ID
     * @param pixelData 像素数据 JSON
     */
    void saveCompareData(String mapId, String pixelData);

    /**
     * 获取最近的决策记录
     *
     * @param deviceId 设备ID
     * @param limit    限制数量
     * @return 决策记录列表
     */
    List<RecentDecisionRecord> getRecentDecisionRecordsForTcp(String deviceId, int limit);

    /**
     * 保存比对数据（TCP专用）
     *
     * @param messageData         消息数据
     * @param bleRawLocationResult BLE原始位置结果
     * @param mapPixelMatchResult  地图像素匹配结果
     * @param finalPixelSource    最终像素来源
     * @param businessTimestamp   业务时间戳
     */
    void saveCompareDataForTcp(Object messageData, Object bleRawLocationResult,
                               Object mapPixelMatchResult, String finalPixelSource,
                               Long businessTimestamp);

    /**
     * 检查是否有最近的BLE来源
     *
     * @param deviceId          设备ID
     * @param currentTimestamp  当前时间戳
     * @return 是否存在
     */
    boolean hasRecentBleSourceForTcp(String deviceId, Long currentTimestamp);

    /**
     * 最近决策记录
     */
    class RecentDecisionRecord {
        private String decisionSource;
        private Long timestamp;

        public String getDecisionSource() {
            return decisionSource;
        }

        public void setDecisionSource(String decisionSource) {
            this.decisionSource = decisionSource;
        }

        public Long getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(Long timestamp) {
            this.timestamp = timestamp;
        }
    }

}
