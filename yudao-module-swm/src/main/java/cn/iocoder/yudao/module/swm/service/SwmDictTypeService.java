package cn.iocoder.yudao.module.swm.service;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.swm.controller.admin.dicttype.vo.SwmDictTypePageReqVO;
import cn.iocoder.yudao.module.swm.controller.admin.dicttype.vo.SwmDictTypeSaveReqVO;
import cn.iocoder.yudao.module.swm.dal.dataobject.SwmDictTypeDO;
import jakarta.validation.Valid;

/**
 * 字典类型 Service 接口
 */
public interface SwmDictTypeService {

    /**
     * 创建字典类型
     *
     * @param createReqVO 创建信息
     * @return 字典类型编号
     */
    String createDictType(@Valid SwmDictTypeSaveReqVO createReqVO);

    /**
     * 更新字典类型
     *
     * @param updateReqVO 更新信息
     */
    void updateDictType(@Valid SwmDictTypeSaveReqVO updateReqVO);

    /**
     * 删除字典类型
     *
     * @param id 字典类型编号
     */
    void deleteDictType(String id);

    /**
     * 获得字典类型
     *
     * @param id 字典类型编号
     * @return 字典类型
     */
    SwmDictTypeDO getDictType(String id);

    /**
     * 获得字典类型分页
     *
     * @param pageReqVO 分页查询
     * @return 字典类型分页
     */
    PageResult<SwmDictTypeDO> getDictTypePage(SwmDictTypePageReqVO pageReqVO);

}
