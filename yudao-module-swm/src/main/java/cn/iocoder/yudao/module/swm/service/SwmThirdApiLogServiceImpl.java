package cn.iocoder.yudao.module.swm.service;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.swm.controller.admin.thirdapilog.vo.SwmThirdApiLogPageReqVO;
import cn.iocoder.yudao.module.swm.dal.dataobject.SwmThirdApiLogDO;
import cn.iocoder.yudao.module.swm.dal.mysql.SwmThirdApiLogMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.swm.enums.ErrorCodeConstants.COMMON_OPTIONS_NOT_EXISTS;

/**
 * 第三方API日志 Service 实现类
 */
@Service
@Validated
@Slf4j
public class SwmThirdApiLogServiceImpl implements SwmThirdApiLogService {

    @Resource
    private SwmThirdApiLogMapper swmThirdApiLogMapper;

    @Override
    public SwmThirdApiLogDO getThirdApiLog(String id) {
        SwmThirdApiLogDO log = swmThirdApiLogMapper.selectById(id);
        if (log == null) {
            throw exception(COMMON_OPTIONS_NOT_EXISTS);
        }
        return log;
    }

    @Override
    public PageResult<SwmThirdApiLogDO> getThirdApiLogPage(SwmThirdApiLogPageReqVO pageReqVO) {
        return swmThirdApiLogMapper.selectPage(pageReqVO, new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>());
    }

    @Override
    public void saveLog(SwmThirdApiLogDO apiLog) {
        swmThirdApiLogMapper.insert(apiLog);
    }

}
