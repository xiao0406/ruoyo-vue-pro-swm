package cn.iocoder.yudao.module.swm.service;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.swm.controller.admin.area.vo.SwmAreaPageReqVO;
import cn.iocoder.yudao.module.swm.controller.admin.area.vo.SwmAreaSaveReqVO;
import cn.iocoder.yudao.module.swm.dal.dataobject.SwmAreaDO;
import jakarta.validation.Valid;

import java.util.List;

/**
 * 区域管理 Service 接口
 */
public interface SwmAreaService {

    String createArea(@Valid SwmAreaSaveReqVO createReqVO);
    void updateArea(@Valid SwmAreaSaveReqVO updateReqVO);
    void deleteArea(String id);
    SwmAreaDO getArea(String id);
    PageResult<SwmAreaDO> getAreaPage(SwmAreaPageReqVO pageReqVO);

    /**
     * 根据条件查询区域列表
     */
    List<SwmAreaDO> findList(SwmAreaDO query);

    /**
     * 根据 ID 集合批量查询区域列表
     */
    List<SwmAreaDO> findListByIds(java.util.Collection<String> ids);

}
