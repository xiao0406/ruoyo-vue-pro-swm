package cn.iocoder.yudao.module.swm.service;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.swm.controller.admin.hazard.vo.SwmHazardSourcePageReqVO;
import cn.iocoder.yudao.module.swm.controller.admin.hazard.vo.SwmHazardSourceSaveReqVO;
import cn.iocoder.yudao.module.swm.dal.dataobject.SwmHazardSourceDO;
import jakarta.validation.Valid;

public interface SwmHazardSourceService {

    String createHazardSource(@Valid SwmHazardSourceSaveReqVO createReqVO);

    void updateHazardSource(@Valid SwmHazardSourceSaveReqVO updateReqVO);

    void deleteHazardSource(String id);

    SwmHazardSourceDO getHazardSource(String id);

    PageResult<SwmHazardSourceDO> getHazardSourcePage(SwmHazardSourcePageReqVO pageReqVO);
}
