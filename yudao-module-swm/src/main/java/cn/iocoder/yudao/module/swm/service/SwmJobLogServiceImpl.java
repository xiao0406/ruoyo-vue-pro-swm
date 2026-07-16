package cn.iocoder.yudao.module.swm.service;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.swm.controller.admin.joblog.vo.SwmJobLogPageReqVO;
import cn.iocoder.yudao.module.swm.dal.dataobject.SwmJobLogDO;
import cn.iocoder.yudao.module.swm.dal.mysql.SwmJobLogMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.swm.enums.ErrorCodeConstants.COMMON_OPTIONS_NOT_EXISTS;

/**
 * 任务日志 Service 实现类
 */
@Service
@Validated
@Slf4j
public class SwmJobLogServiceImpl implements SwmJobLogService {

    @Resource
    private SwmJobLogMapper swmJobLogMapper;

    @Override
    public SwmJobLogDO getJobLog(String id) {
        SwmJobLogDO log = swmJobLogMapper.selectById(id);
        if (log == null) {
            throw exception(COMMON_OPTIONS_NOT_EXISTS);
        }
        return log;
    }

    @Override
    public PageResult<SwmJobLogDO> getJobLogPage(SwmJobLogPageReqVO pageReqVO) {
        return swmJobLogMapper.selectPage(pageReqVO,
                new cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX<SwmJobLogDO>()
                        .likeIfPresent(SwmJobLogDO::getJobName, pageReqVO.getJobName())
                        .eqIfPresent(SwmJobLogDO::getExecuteStatus, pageReqVO.getExecuteStatus())
                        .orderByDesc(SwmJobLogDO::getStartTime));
    }

    @Override
    public void save(SwmJobLogDO jobLog) {
        if (jobLog.getId() == null) {
            swmJobLogMapper.insert(jobLog);
        } else {
            swmJobLogMapper.updateById(jobLog);
        }
    }

}
