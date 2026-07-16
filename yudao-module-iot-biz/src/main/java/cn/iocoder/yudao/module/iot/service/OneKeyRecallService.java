package cn.iocoder.yudao.module.iot.service;

import cn.iocoder.yudao.module.iot.mq.handler.OneKeyRecall;

/**
 * 一键召回服务接口（IoT 版）
 */
public interface OneKeyRecallService {

    default void updateResult(String recallId, OneKeyRecall.RecallResultEnum result,
            int totalCount, int successCount, int failCount) {
    }
}
