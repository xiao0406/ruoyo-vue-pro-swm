package cn.iocoder.yudao.module.iot.service;

/**
 * 站点地图匹配服务接口
 */
public interface SiteMapMatchService {

    /**
     * 地图像素匹配结果
     */
    class MapPixelMatchResult {
        private boolean matched;
        private String areaId;
        private String areaName;
        private Double x;
        private Double y;

        public boolean isMatched() { return matched; }
        public void setMatched(boolean matched) { this.matched = matched; }
        public String getAreaId() { return areaId; }
        public void setAreaId(String areaId) { this.areaId = areaId; }
        public String getAreaName() { return areaName; }
        public void setAreaName(String areaName) { this.areaName = areaName; }
        public Double getX() { return x; }
        public void setX(Double x) { this.x = x; }
        public Double getY() { return y; }
        public void setY(Double y) { this.y = y; }
    }

    /**
     * 根据像素坐标匹配站点地图位置
     */
    MapPixelMatchResult matchPixel(String mapId, Double x, Double y);
}
