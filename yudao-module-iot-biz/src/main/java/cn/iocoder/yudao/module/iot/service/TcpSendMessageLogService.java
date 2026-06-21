package cn.iocoder.yudao.module.iot.service;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.iot.controller.admin.tcp.vo.*;
import cn.iocoder.yudao.module.iot.dal.dataobject.TcpSendMessageLogDO;

public interface TcpSendMessageLogService {
    TcpSendMessageLogDO getTcpSendMessageLog(String id);
    PageResult<TcpSendMessageLogDO> getTcpSendMessageLogPage(TcpSendMessageLogPageReqVO pageReqVO);
}
