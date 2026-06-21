package cn.iocoder.yudao.module.swm.service;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.swm.controller.admin.personnelboard.vo.SwmPersonnelBoardPageReqVO;
import cn.iocoder.yudao.module.swm.controller.admin.personnelboard.vo.SwmPersonnelBoardSaveReqVO;
import cn.iocoder.yudao.module.swm.dal.dataobject.SwmPersonnelBoardDO;
import jakarta.validation.Valid;

/**
 * 人员看板 Service 接口
 */
public interface SwmPersonnelBoardService {

    /**
     * 创建人员看板
     *
     * @param createReqVO 创建信息
     * @return 人员看板编号
     */
    String createPersonnelBoard(@Valid SwmPersonnelBoardSaveReqVO createReqVO);

    /**
     * 更新人员看板
     *
     * @param updateReqVO 更新信息
     */
    void updatePersonnelBoard(@Valid SwmPersonnelBoardSaveReqVO updateReqVO);

    /**
     * 删除人员看板
     *
     * @param id 人员看板编号
     */
    void deletePersonnelBoard(String id);

    /**
     * 获得人员看板
     *
     * @param id 人员看板编号
     * @return 人员看板
     */
    SwmPersonnelBoardDO getPersonnelBoard(String id);

    /**
     * 获得人员看板分页
     *
     * @param pageReqVO 分页查询
     * @return 人员看板分页
     */
    PageResult<SwmPersonnelBoardDO> getPersonnelBoardPage(SwmPersonnelBoardPageReqVO pageReqVO);

}
