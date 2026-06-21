package cn.iocoder.yudao.module.iot.service;

/**
 * 外部坐标服务接口
 */
public interface ExternalCoordinateService {

    /**
     * 获取外部坐标数据
     *
     * @param args 参数
     * @return 坐标结果
     */
    Object getExternalCoordinate(Object... args);
}
