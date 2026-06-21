package cn.iocoder.yudao.module.swm.service;

import cn.iocoder.yudao.module.swm.dal.dataobject.SwmDifyDO;
import java.time.LocalDateTime;

/**
 * AI Dify Service
 */
public interface SwmDifyService {

    SwmDifyDO getSwmDify(String id);

    /**
     * 根据时间范围查询 Dify 记录
     */
    SwmDifyDO getEntity(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 保存 Dify 记录
     */
    void save(SwmDifyDO difyDO);
}
