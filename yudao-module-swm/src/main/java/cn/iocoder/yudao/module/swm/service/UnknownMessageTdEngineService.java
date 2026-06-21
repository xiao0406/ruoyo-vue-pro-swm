package cn.iocoder.yudao.module.swm.service;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;

/**
 * 未知消息类型数据服务接口
 * 
 * @author Shawn
 * @date 2025-01-31
 */
public interface UnknownMessageTdEngineService {

    /**
     * 保存未知消息类型数据
     * 
     * @param deviceId       设备ID（可为空）
     * @param sessionId      会话ID
     * @param messageContent 消息内容
     * @return 保存结果
     */
    CommonResult<cn.hutool.json.JSONObject> saveUnknownMessageData(String deviceId, String sessionId, String messageContent);

    /**
     * 获取未知消息超级表名称
     * 
     * @return 超级表名称
     */
    String getUnknownMessageSuperTableName();
}