package cn.iocoder.yudao.module.swm.service;

/**
 * 原始报文 TDengine 存储 Service 接口
 */
public interface RawMessageTdEngineService {

    /**
     * 保存原始报文到 TDengine
     *
     * @param deviceCode 设备编号
     * @param message    原始报文
     */
    void saveRawMessage(String deviceCode, String message);

}
