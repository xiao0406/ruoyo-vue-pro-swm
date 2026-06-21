package cn.iocoder.yudao.module.swm.service;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.swm.controller.admin.alarm.vo.SwmAlarmConfigPageReqVO;
import cn.iocoder.yudao.module.swm.controller.admin.alarm.vo.SwmAlarmConfigSaveReqVO;
import cn.iocoder.yudao.module.swm.dal.dataobject.SwmAlarmConfigDO;
import jakarta.validation.Valid;

/**
 * 告警配置 Service 接口
 */
public interface SwmAlarmConfigService {

    /**
     * 创建告警配置
     *
     * @param createReqVO 创建信息
     * @return 配置编号
     */
    String createAlarmConfig(@Valid SwmAlarmConfigSaveReqVO createReqVO);

    /**
     * 更新告警配置
     *
     * @param updateReqVO 更新信息
     */
    void updateAlarmConfig(@Valid SwmAlarmConfigSaveReqVO updateReqVO);

    /**
     * 删除告警配置
     *
     * @param id 配置编号
     */
    void deleteAlarmConfig(String id);

    /**
     * 获得告警配置
     *
     * @param id 配置编号
     * @return 告警配置
     */
    SwmAlarmConfigDO getAlarmConfig(String id);

    /**
     * 获得告警配置分页
     *
     * @param pageReqVO 分页查询
     * @return 告警配置分页
     */
    PageResult<SwmAlarmConfigDO> getAlarmConfigPage(SwmAlarmConfigPageReqVO pageReqVO);

}
