package cn.iocoder.yudao.module.swm.service;

import java.util.Map;

/**
 * 定位引擎 Service 接口
 */
public interface LocationEngineService {

    /**
     * 解析设备上报的原始定位数据
     *
     * @param rawData 原始数据
     * @return 解析后的坐标信息
     */
    Map<String, Object> resolveLocation(String rawData);

}
