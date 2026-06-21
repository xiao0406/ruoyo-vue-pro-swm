package cn.iocoder.yudao.module.swm.service;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.swm.controller.admin.personworkarea.vo.SwmPersonWorkAreaPageReqVO;
import cn.iocoder.yudao.module.swm.controller.admin.personworkarea.vo.SwmPersonWorkAreaSaveReqVO;
import cn.iocoder.yudao.module.swm.dal.dataobject.SwmPersonWorkAreaDO;
import jakarta.validation.Valid;

import java.util.List;

/**
 * 人员工作区域 Service 接口
 */
public interface SwmPersonWorkAreaService {

    String createPersonWorkArea(@Valid SwmPersonWorkAreaSaveReqVO createReqVO);
    void updatePersonWorkArea(@Valid SwmPersonWorkAreaSaveReqVO updateReqVO);
    void deletePersonWorkArea(String id);
    SwmPersonWorkAreaDO getPersonWorkArea(String id);
    PageResult<SwmPersonWorkAreaDO> getPersonWorkAreaPage(SwmPersonWorkAreaPageReqVO pageReqVO);

    /**
     * 根据身份证号查询有效的人员工作区域绑定
     */
    List<SwmPersonWorkAreaDO> findActiveByIdentityCard(String identityCard);

}
