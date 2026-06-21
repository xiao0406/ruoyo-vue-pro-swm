package cn.iocoder.yudao.module.swm.service;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.swm.controller.admin.alarm_detail.vo.SwmAlarmConfigDetailPageReqVO;
import cn.iocoder.yudao.module.swm.controller.admin.alarm_detail.vo.SwmAlarmConfigDetailSaveReqVO;
import cn.iocoder.yudao.module.swm.dal.dataobject.SwmAlarmConfigDetailDO;
import jakarta.validation.Valid;

public interface SwmAlarmConfigDetailService {

    /**
     * 创建告警配置详情
     *
     * @param createReqVO 创建参数
     * @return 告警配置详情编号
     */
    String createAlarmConfigDetail(@Valid SwmAlarmConfigDetailSaveReqVO createReqVO);

    /**
     * 更新告警配置详情
     *
     * @param updateReqVO 更新参数
     */
    void updateAlarmConfigDetail(@Valid SwmAlarmConfigDetailSaveReqVO updateReqVO);

    /**
     * 删除告警配置详情
     *
     * @param id 告警配置详情编号
     */
    void deleteAlarmConfigDetail(String id);

    /**
     * 获得告警配置详情
     *
     * @param id 告警配置详情编号
     * @return 告警配置详情
     */
    SwmAlarmConfigDetailDO getAlarmConfigDetail(String id);

    /**
     * 获得告警配置详情分页
     *
     * @param pageReqVO 分页查询
     * @return 告警配置详情分页
     */
    PageResult<SwmAlarmConfigDetailDO> getAlarmConfigDetailPage(SwmAlarmConfigDetailPageReqVO pageReqVO);

}
