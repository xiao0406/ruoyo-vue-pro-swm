package cn.iocoder.yudao.module.swm.service;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.swm.controller.admin.joblog.vo.SwmJobLogPageReqVO;
import cn.iocoder.yudao.module.swm.dal.dataobject.SwmJobLogDO;

/**
 * 任务日志 Service 接口
 */
public interface SwmJobLogService {

    SwmJobLogDO getJobLog(String id);
    PageResult<SwmJobLogDO> getJobLogPage(SwmJobLogPageReqVO pageReqVO);

    /**
     * 保存任务日志（新增或更新）
     */
    void save(SwmJobLogDO jobLog);

}
