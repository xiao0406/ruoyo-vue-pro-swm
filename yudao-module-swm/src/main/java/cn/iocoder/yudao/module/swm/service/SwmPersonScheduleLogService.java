package cn.iocoder.yudao.module.swm.service;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.swm.controller.admin.schedulelog.vo.SwmPersonScheduleLogPageReqVO;
import cn.iocoder.yudao.module.swm.dal.dataobject.SwmPersonScheduleLogDO;

/**
 * 排班日志 Service 接口
 */
public interface SwmPersonScheduleLogService {

    /**
     * 获得排班日志
     *
     * @param id 排班日志编号
     * @return 排班日志
     */
    SwmPersonScheduleLogDO getPersonScheduleLog(String id);

    /**
     * 获得排班日志分页
     *
     * @param pageReqVO 分页查询
     * @return 排班日志分页
     */
    PageResult<SwmPersonScheduleLogDO> getPersonScheduleLogPage(SwmPersonScheduleLogPageReqVO pageReqVO);

}
