package cn.iocoder.yudao.module.swm.service;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.swm.controller.admin.beacon.vo.SwmBeaconStationPageReqVO;
import cn.iocoder.yudao.module.swm.controller.admin.beacon.vo.SwmBeaconStationSaveReqVO;
import cn.iocoder.yudao.module.swm.dal.dataobject.SwmBeaconStationDO;
import jakarta.validation.Valid;

/**
 * 信标站点 Service 接口
 */
public interface SwmBeaconStationService {

    /**
     * 创建信标站点
     *
     * @param createReqVO 创建信息
     * @return 站点编号
     */
    String createBeaconStation(@Valid SwmBeaconStationSaveReqVO createReqVO);

    /**
     * 更新信标站点
     *
     * @param updateReqVO 更新信息
     */
    void updateBeaconStation(@Valid SwmBeaconStationSaveReqVO updateReqVO);

    /**
     * 删除信标站点
     *
     * @param id 站点编号
     */
    void deleteBeaconStation(String id);

    /**
     * 获得信标站点
     *
     * @param id 站点编号
     * @return 信标站点
     */
    SwmBeaconStationDO getBeaconStation(String id);

    /**
     * 获得信标站点分页
     *
     * @param pageReqVO 分页查询
     * @return 信标站点分页
     */
    PageResult<SwmBeaconStationDO> getBeaconStationPage(SwmBeaconStationPageReqVO pageReqVO);

}
