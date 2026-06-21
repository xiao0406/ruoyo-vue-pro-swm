package cn.iocoder.yudao.module.swm.service;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.swm.controller.admin.dictdata.vo.SwmDictDataPageReqVO;
import cn.iocoder.yudao.module.swm.controller.admin.dictdata.vo.SwmDictDataSaveReqVO;
import cn.iocoder.yudao.module.swm.dal.dataobject.SwmDictDataDO;
import jakarta.validation.Valid;

/**
 * 字典数据 Service 接口
 */
public interface SwmDictDataService {

    /**
     * 创建字典数据
     *
     * @param createReqVO 创建信息
     * @return 字典数据编号
     */
    String createDictData(@Valid SwmDictDataSaveReqVO createReqVO);

    /**
     * 更新字典数据
     *
     * @param updateReqVO 更新信息
     */
    void updateDictData(@Valid SwmDictDataSaveReqVO updateReqVO);

    /**
     * 删除字典数据
     *
     * @param id 字典数据编号
     */
    void deleteDictData(String id);

    /**
     * 获得字典数据
     *
     * @param id 字典数据编号
     * @return 字典数据
     */
    SwmDictDataDO getDictData(String id);

    /**
     * 获得字典数据分页
     *
     * @param pageReqVO 分页查询
     * @return 字典数据分页
     */
    PageResult<SwmDictDataDO> getDictDataPage(SwmDictDataPageReqVO pageReqVO);

}
