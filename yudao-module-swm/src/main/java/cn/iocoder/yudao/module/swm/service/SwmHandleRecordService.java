package cn.iocoder.yudao.module.swm.service;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.swm.controller.admin.handle_record.vo.SwmHandleRecordPageReqVO;
import cn.iocoder.yudao.module.swm.controller.admin.handle_record.vo.SwmHandleRecordSaveReqVO;
import cn.iocoder.yudao.module.swm.dal.dataobject.SwmHandleRecordDO;
import jakarta.validation.Valid;

/**
 * 处置记录 Service 接口
 */
public interface SwmHandleRecordService {

    String createHandleRecord(@Valid SwmHandleRecordSaveReqVO createReqVO);

    void updateHandleRecord(@Valid SwmHandleRecordSaveReqVO updateReqVO);

    void deleteHandleRecord(String id);

    SwmHandleRecordDO getHandleRecord(String id);

    PageResult<SwmHandleRecordDO> getHandleRecordPage(SwmHandleRecordPageReqVO pageReqVO);

}
