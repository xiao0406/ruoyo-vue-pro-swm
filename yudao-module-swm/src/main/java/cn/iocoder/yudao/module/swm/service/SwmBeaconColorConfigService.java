package cn.iocoder.yudao.module.swm.service;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.swm.controller.admin.beaconcolor.vo.SwmBeaconColorConfigPageReqVO;
import cn.iocoder.yudao.module.swm.controller.admin.beaconcolor.vo.SwmBeaconColorConfigSaveReqVO;
import cn.iocoder.yudao.module.swm.dal.dataobject.SwmBeaconColorConfigDO;
import jakarta.validation.Valid;

/**
 * 信标颜色配置 Service 接口
 */
public interface SwmBeaconColorConfigService {

    /**
     * 创建信标颜色配置
     *
     * @param createReqVO 创建信息
     * @return 信标颜色配置编号
     */
    String createBeaconColorConfig(@Valid SwmBeaconColorConfigSaveReqVO createReqVO);

    /**
     * 更新信标颜色配置
     *
     * @param updateReqVO 更新信息
     */
    void updateBeaconColorConfig(@Valid SwmBeaconColorConfigSaveReqVO updateReqVO);

    /**
     * 删除信标颜色配置
     *
     * @param id 信标颜色配置编号
     */
    void deleteBeaconColorConfig(String id);

    /**
     * 获得信标颜色配置
     *
     * @param id 信标颜色配置编号
     * @return 信标颜色配置
     */
    SwmBeaconColorConfigDO getBeaconColorConfig(String id);

    /**
     * 获得信标颜色配置分页
     *
     * @param pageReqVO 分页查询
     * @return 信标颜色配置分页
     */
    PageResult<SwmBeaconColorConfigDO> getBeaconColorConfigPage(SwmBeaconColorConfigPageReqVO pageReqVO);

}
