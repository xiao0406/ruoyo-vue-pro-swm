package cn.iocoder.yudao.module.swm.service;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.swm.controller.admin.warning.vo.SwmWarningManagementPageReqVO;
import cn.iocoder.yudao.module.swm.controller.admin.warning.vo.SwmWarningManagementSaveReqVO;
import cn.iocoder.yudao.module.swm.dal.dataobject.SwmWarningManagementDO;
import jakarta.validation.Valid;

/**
 * 预警管理 Service 接口
 */
public interface SwmWarningManagementService {

    /**
     * 创建预警记录
     *
     * @param createReqVO 创建信息
     * @return 预警编号
     */
    String createWarningManagement(@Valid SwmWarningManagementSaveReqVO createReqVO);

    /**
     * 更新预警记录
     *
     * @param updateReqVO 更新信息
     */
    void updateWarningManagement(@Valid SwmWarningManagementSaveReqVO updateReqVO);

    /**
     * 删除预警记录
     *
     * @param id 预警编号
     */
    void deleteWarningManagement(String id);

    /**
     * 获得预警记录
     *
     * @param id 预警编号
     * @return 预警记录
     */
    SwmWarningManagementDO getWarningManagement(String id);

    /**
     * 获得预警记录分页
     *
     * @param pageReqVO 分页查询
     * @return 预警记录分页
     */
    PageResult<SwmWarningManagementDO> getWarningManagementPage(SwmWarningManagementPageReqVO pageReqVO);

}
