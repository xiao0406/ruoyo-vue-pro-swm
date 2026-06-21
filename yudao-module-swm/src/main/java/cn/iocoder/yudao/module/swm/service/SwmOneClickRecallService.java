package cn.iocoder.yudao.module.swm.service;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.swm.controller.admin.one_click_recall.vo.SwmOneClickRecallPageReqVO;
import cn.iocoder.yudao.module.swm.controller.admin.one_click_recall.vo.SwmOneClickRecallSaveReqVO;
import cn.iocoder.yudao.module.swm.dal.dataobject.SwmOneClickRecallDO;
import jakarta.validation.Valid;

/**
 * 一键召回 Service 接口
 */
public interface SwmOneClickRecallService {

    String createOneClickRecall(@Valid SwmOneClickRecallSaveReqVO createReqVO);

    void updateOneClickRecall(@Valid SwmOneClickRecallSaveReqVO updateReqVO);

    void deleteOneClickRecall(String id);

    SwmOneClickRecallDO getOneClickRecall(String id);

    PageResult<SwmOneClickRecallDO> getOneClickRecallPage(SwmOneClickRecallPageReqVO pageReqVO);

}
