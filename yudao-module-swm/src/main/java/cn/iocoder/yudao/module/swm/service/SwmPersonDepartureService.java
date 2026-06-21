package cn.iocoder.yudao.module.swm.service;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.swm.controller.admin.persondeparture.vo.SwmPersonDeparturePageReqVO;
import cn.iocoder.yudao.module.swm.controller.admin.persondeparture.vo.SwmPersonDepartureSaveReqVO;
import cn.iocoder.yudao.module.swm.dal.dataobject.SwmPersonDepartureDO;
import jakarta.validation.Valid;

/**
 * 人员退场 Service 接口
 */
public interface SwmPersonDepartureService {

    /**
     * 创建人员退场
     *
     * @param createReqVO 创建信息
     * @return 人员退场编号
     */
    String createPersonDeparture(@Valid SwmPersonDepartureSaveReqVO createReqVO);

    /**
     * 更新人员退场
     *
     * @param updateReqVO 更新信息
     */
    void updatePersonDeparture(@Valid SwmPersonDepartureSaveReqVO updateReqVO);

    /**
     * 删除人员退场
     *
     * @param id 人员退场编号
     */
    void deletePersonDeparture(String id);

    /**
     * 获得人员退场
     *
     * @param id 人员退场编号
     * @return 人员退场
     */
    SwmPersonDepartureDO getPersonDeparture(String id);

    /**
     * 获得人员退场分页
     *
     * @param pageReqVO 分页查询
     * @return 人员退场分页
     */
    PageResult<SwmPersonDepartureDO> getPersonDeparturePage(SwmPersonDeparturePageReqVO pageReqVO);

}
