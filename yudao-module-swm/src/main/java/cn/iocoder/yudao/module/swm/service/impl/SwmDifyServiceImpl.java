package cn.iocoder.yudao.module.swm.service.impl;

import cn.iocoder.yudao.module.swm.dal.dataobject.SwmDifyDO;
import cn.iocoder.yudao.module.swm.dal.mysql.SwmDifyMapper;
import cn.iocoder.yudao.module.swm.service.SwmDifyService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@Slf4j
public class SwmDifyServiceImpl implements SwmDifyService {

    @Resource
    private SwmDifyMapper mapper;

    @Override
    public SwmDifyDO getSwmDify(String id) {
        return mapper.selectById(id);
    }

    @Override
    public SwmDifyDO getEntity(LocalDateTime startTime, LocalDateTime endTime) {
        return mapper.selectOne(new LambdaQueryWrapper<SwmDifyDO>()
                .ge(startTime != null, SwmDifyDO::getDate, startTime)
                .le(endTime != null, SwmDifyDO::getDate, endTime)
                .orderByDesc(SwmDifyDO::getDate)
                .last("LIMIT 1"));
    }

    @Override
    public void save(SwmDifyDO difyDO) {
        if (difyDO.getId() == null) {
            mapper.insert(difyDO);
        } else {
            mapper.updateById(difyDO);
        }
    }

    /**
     * 兼容旧接口
     */
    public String chat(String query, String userId, String conversationId) {
        log.info("SwmDifyService.chat local fallback, userId={}, conversationId={}, query={}", userId, conversationId, query);
        return query == null ? "" : query;
    }
}
