package cn.iocoder.yudao.module.swm.service;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;

import java.util.Map;

/**
 * 安全帽设备SOS数据服务接口
 * 
 * @author Shawn
 * @date 2025-01-31
 */
public interface HelmetSosTdEngineService {

    /**
     * 保存安全帽设备SOS数据
     * 
     * @param deviceId 设备ID
     * @param sosData  SOS数据
     * @return 保存结果
     */
    CommonResult<cn.hutool.json.JSONObject> saveHelmetSosData(String deviceId, Map<String, Object> sosData);

    /**
     * 获取安全帽SOS超级表名称
     * 
     * @return 超级表名称
     */
    String getHelmetSosSuperTableName();
}