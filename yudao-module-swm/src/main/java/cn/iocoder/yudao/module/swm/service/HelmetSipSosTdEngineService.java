package cn.iocoder.yudao.module.swm.service;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;

import java.util.Map;

/**
 * 安全帽设备SIP SOS数据服务接口
 * 
 * @author Shawn
 * @date 2025-06-02
 */
public interface HelmetSipSosTdEngineService {

    /**
     * 保存安全帽设备SIP SOS数据
     * 
     * @param deviceId   设备ID
     * @param sipSosData SIP SOS数据
     * @return 保存结果
     */
    CommonResult<cn.hutool.json.JSONObject> saveHelmetSipSosData(String deviceId, Map<String, Object> sipSosData);

    /**
     * 获取安全帽SIP SOS超级表名称
     * 
     * @return 超级表名称
     */
    String getHelmetSipSosSuperTableName();
}