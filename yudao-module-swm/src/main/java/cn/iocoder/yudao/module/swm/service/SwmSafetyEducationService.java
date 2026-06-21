package cn.iocoder.yudao.module.swm.service;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.swm.controller.admin.safetyeducation.vo.*;
import cn.iocoder.yudao.module.swm.dal.dataobject.SwmSafetyEducationDO;
import jakarta.validation.Valid;

public interface SwmSafetyEducationService {
    String createSwmSafetyEducation(@Valid SwmSafetyEducationSaveReqVO createReqVO);
    void updateSwmSafetyEducation(@Valid SwmSafetyEducationSaveReqVO updateReqVO);
    void deleteSwmSafetyEducation(String id);
    SwmSafetyEducationDO getSwmSafetyEducation(String id);
    PageResult<SwmSafetyEducationDO> getSwmSafetyEducationPage(SwmSafetyEducationPageReqVO pageReqVO);
}
