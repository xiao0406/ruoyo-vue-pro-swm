package cn.iocoder.yudao.module.swm.service;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.swm.controller.admin.scheduleTime.vo.SwmScheduleTimePageReqVO;
import cn.iocoder.yudao.module.swm.controller.admin.scheduleTime.vo.SwmScheduleTimeSaveReqVO;
import cn.iocoder.yudao.module.swm.dal.dataobject.SwmScheduleTimeDO;
import jakarta.validation.Valid;

import java.util.List;

/**
 * 排班时间 Service 接口
 */
public interface SwmScheduleTimeService {

    String createScheduleTime(@Valid SwmScheduleTimeSaveReqVO createReqVO);
    void updateScheduleTime(@Valid SwmScheduleTimeSaveReqVO updateReqVO);
    void deleteScheduleTime(String id);
    SwmScheduleTimeDO getScheduleTime(String id);
    PageResult<SwmScheduleTimeDO> getScheduleTimePage(SwmScheduleTimePageReqVO pageReqVO);

    /**
     * 根据条件查询排班时间列表
     */
    List<SwmScheduleTimeDO> findList(SwmScheduleTimeDO query);

    /**
     * 查询排班时间列表（单条，兼容旧接口名称）
     */
    List<SwmScheduleTimeDO> findListSingle(SwmScheduleTimeDO query);

}
