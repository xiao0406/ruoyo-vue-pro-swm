package cn.iocoder.yudao.module.swm.service;

/**
 * TCP 报文转安全帽数据转换器接口
 */
public interface TcpToHelmetDataConverter {

    /**
     * 将 TCP 原始报文转换为安全帽设备数据
     *
     * @param rawTcpData TCP 原始报文
     * @return 转换后的设备数据 JSON
     */
    String convert(String rawTcpData);

}
