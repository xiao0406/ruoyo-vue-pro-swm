package cn.iocoder.yudao.module.swm.service;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.swm.controller.admin.alarm_light.vo.SwmAlarmLightPageReqVO;
import cn.iocoder.yudao.module.swm.controller.admin.alarm_light.vo.SwmAlarmLightSaveReqVO;
import cn.iocoder.yudao.module.swm.dal.dataobject.SwmAlarmLightDO;
import jakarta.validation.Valid;

/**
 * 报警灯 Service 接口
 */
public interface SwmAlarmLightService {

    /**
     * 创建报警灯
     *
     * @param createReqVO 创建信息
     * @return 报警灯编号
     */
    String createAlarmLight(@Valid SwmAlarmLightSaveReqVO createReqVO);

    /**
     * 更新报警灯
     *
     * @param updateReqVO 更新信息
     */
    void updateAlarmLight(@Valid SwmAlarmLightSaveReqVO updateReqVO);

    /**
     * 删除报警灯
     *
     * @param id 报警灯编号
     */
    void deleteAlarmLight(String id);

    /**
     * 获得报警灯
     *
     * @param id 报警灯编号
     * @return 报警灯
     */
    SwmAlarmLightDO getAlarmLight(String id);

    /**
     * 获得报警灯分页
     *
     * @param pageReqVO 分页查询
     * @return 报警灯分页
     */
    PageResult<SwmAlarmLightDO> getAlarmLightPage(SwmAlarmLightPageReqVO pageReqVO);

}
