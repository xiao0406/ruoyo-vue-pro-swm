package cn.iocoder.yudao.module.swm.service;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.swm.controller.admin.schedulelog.vo.SwmPersonScheduleLogPageReqVO;
import cn.iocoder.yudao.module.swm.dal.dataobject.SwmPersonScheduleLogDO;
import cn.iocoder.yudao.module.swm.dal.mysql.SwmPersonScheduleLogMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.swm.enums.ErrorCodeConstants.PERSON_SCHEDULE_NOT_EXISTS;

/**
 * 排班日志 Service 实现类
 */
@Service
@Validated
@Slf4j
public class SwmPersonScheduleLogServiceImpl implements SwmPersonScheduleLogService {

    @Resource
    private SwmPersonScheduleLogMapper swmPersonScheduleLogMapper;

    @Override
    public SwmPersonScheduleLogDO getPersonScheduleLog(String id) {
        SwmPersonScheduleLogDO log = swmPersonScheduleLogMapper.selectById(id);
        if (log == null) {
            throw exception(PERSON_SCHEDULE_NOT_EXISTS);
        }
        return log;
    }

    @Override
    public PageResult<SwmPersonScheduleLogDO> getPersonScheduleLogPage(SwmPersonScheduleLogPageReqVO pageReqVO) {
        return swmPersonScheduleLogMapper.selectPage(pageReqVO, new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>());
    }

}
