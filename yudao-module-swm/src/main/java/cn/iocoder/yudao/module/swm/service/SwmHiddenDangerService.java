package cn.iocoder.yudao.module.swm.service;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.swm.controller.admin.hidden_danger.vo.SwmHiddenDangerPageReqVO;
import cn.iocoder.yudao.module.swm.controller.admin.hidden_danger.vo.SwmHiddenDangerSaveReqVO;
import cn.iocoder.yudao.module.swm.dal.dataobject.SwmHiddenDangerDO;
import jakarta.validation.Valid;

/**
 * 隐患排查 Service 接口
 */
public interface SwmHiddenDangerService {

    /**
     * 创建隐患排查
     *
     * @param createReqVO 创建信息
     * @return 隐患编号
     */
    String createHiddenDanger(@Valid SwmHiddenDangerSaveReqVO createReqVO);

    /**
     * 更新隐患排查
     *
     * @param updateReqVO 更新信息
     */
    void updateHiddenDanger(@Valid SwmHiddenDangerSaveReqVO updateReqVO);

    /**
     * 删除隐患排查
     *
     * @param id 隐患编号
     */
    void deleteHiddenDanger(String id);

    /**
     * 获得隐患排查
     *
     * @param id 隐患编号
     * @return 隐患排查
     */
    SwmHiddenDangerDO getHiddenDanger(String id);

    /**
     * 获得隐患排查分页
     *
     * @param pageReqVO 分页查询
     * @return 隐患排查分页
     */
    PageResult<SwmHiddenDangerDO> getHiddenDangerPage(SwmHiddenDangerPageReqVO pageReqVO);

}
