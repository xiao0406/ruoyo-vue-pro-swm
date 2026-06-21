package cn.iocoder.yudao.module.swm.service;

import java.util.Map;

/**
 * 站点地图匹配 Service 接口
 */
public interface SiteMapMatchService {

    /**
     * 根据坐标匹配站点地图位置
     *
     * @param mapId     地图ID
     * @param longitude 经度
     * @param latitude  纬度
     * @return 匹配结果
     */
    Map<String, Object> matchLocation(String mapId, Double longitude, Double latitude);

    /**
     * 根据像素坐标匹配站点地图位置
     *
     * @param mapId 地图ID
     * @param x     像素X坐标
     * @param y     像素Y坐标
     * @return 匹配结果
     */
    MapPixelMatchResult matchPixel(String mapId, Double x, Double y);

    /**
     * 地图像素匹配结果
     */
    class MapPixelMatchResult {
        private boolean matched;
        private String areaId;
        private String areaName;
        private Double x;
        private Double y;

        public boolean isMatched() {
            return matched;
        }

        public void setMatched(boolean matched) {
            this.matched = matched;
        }

        public String getAreaId() {
            return areaId;
        }

        public void setAreaId(String areaId) {
            this.areaId = areaId;
        }

        public String getAreaName() {
            return areaName;
        }

        public void setAreaName(String areaName) {
            this.areaName = areaName;
        }

        public Double getX() {
            return x;
        }

        public void setX(Double x) {
            this.x = x;
        }

        public Double getY() {
            return y;
        }

        public void setY(Double y) {
            this.y = y;
        }
    }

}
