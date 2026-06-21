package cn.iocoder.yudao.module.iot.service;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.iot.controller.admin.file.vo.*;
import cn.iocoder.yudao.module.iot.dal.dataobject.IotFileUploadDO;
import jakarta.validation.Valid;

public interface IotFileUploadService {
    String createIotFileUpload(@Valid IotFileUploadSaveReqVO createReqVO);
    void updateIotFileUpload(@Valid IotFileUploadSaveReqVO updateReqVO);
    void deleteIotFileUpload(String id);
    IotFileUploadDO getIotFileUpload(String id);
    PageResult<IotFileUploadDO> getIotFileUploadPage(IotFileUploadPageReqVO pageReqVO);

    /**
     * 根据对象名称查询文件记录
     */
    IotFileUploadDO findByObjectName(String objectName);
}
