package cn.iocoder.yudao.module.iot.service.impl;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.iot.controller.admin.tcp.vo.*;
import cn.iocoder.yudao.module.iot.dal.dataobject.TcpSendMessageLogDO;
import cn.iocoder.yudao.module.iot.dal.mysql.TcpSendMessageLogMapper;
import cn.iocoder.yudao.module.iot.enums.ErrorCodeConstants;
import cn.iocoder.yudao.module.iot.service.TcpSendMessageLogService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;

@Service
@Validated
public class TcpSendMessageLogServiceImpl implements TcpSendMessageLogService {

    @Resource
    private TcpSendMessageLogMapper tcpSendMessageLogMapper;

    @Override
    public TcpSendMessageLogDO getTcpSendMessageLog(String id) {
        return tcpSendMessageLogMapper.selectById(id);
    }

    @Override
    public PageResult<TcpSendMessageLogDO> getTcpSendMessageLogPage(TcpSendMessageLogPageReqVO pageReqVO) {
        return tcpSendMessageLogMapper.selectPage(pageReqVO);
    }
}
