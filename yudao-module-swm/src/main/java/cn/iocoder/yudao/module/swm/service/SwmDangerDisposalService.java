package cn.iocoder.yudao.module.swm.service;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.swm.controller.admin.danger_disposal.vo.SwmDangerDisposalPageReqVO;
import cn.iocoder.yudao.module.swm.controller.admin.danger_disposal.vo.SwmDangerDisposalSaveReqVO;
import cn.iocoder.yudao.module.swm.dal.dataobject.SwmDangerDisposalDO;
import jakarta.validation.Valid;

/**
 * 隐患处置 Service 接口
 */
public interface SwmDangerDisposalService {

    /**
     * 创建隐患处置记录
     *
     * @param createReqVO 创建信息
     * @return 隐患处置编号
     */
    String createDangerDisposal(@Valid SwmDangerDisposalSaveReqVO createReqVO);

    /**
     * 更新隐患处置记录
     *
     * @param updateReqVO 更新信息
     */
    void updateDangerDisposal(@Valid SwmDangerDisposalSaveReqVO updateReqVO);

    /**
     * 删除隐患处置记录
     *
     * @param id 隐患处置编号
     */
    void deleteDangerDisposal(String id);

    /**
     * 获得隐患处置记录
     *
     * @param id 隐患处置编号
     * @return 隐患处置记录
     */
    SwmDangerDisposalDO getDangerDisposal(String id);

    /**
     * 获得隐患处置记录分页
     *
     * @param pageReqVO 分页查询
     * @return 隐患处置记录分页
     */
    PageResult<SwmDangerDisposalDO> getDangerDisposalPage(SwmDangerDisposalPageReqVO pageReqVO);

}
