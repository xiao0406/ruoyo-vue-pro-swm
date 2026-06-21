package cn.iocoder.yudao.module.iot.service.impl;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.iot.controller.admin.file.vo.*;
import cn.iocoder.yudao.module.iot.dal.dataobject.IotFileUploadDO;
import cn.iocoder.yudao.module.iot.dal.mysql.IotFileUploadMapper;
import cn.iocoder.yudao.module.iot.enums.ErrorCodeConstants;
import cn.iocoder.yudao.module.iot.service.IotFileUploadService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;

@Service
@Validated
@Slf4j
public class IotFileUploadServiceImpl implements IotFileUploadService {

    @Resource
    private IotFileUploadMapper iotFileUploadMapper;

    @Override
    public String createIotFileUpload(IotFileUploadSaveReqVO reqVO) {
        IotFileUploadDO upload = BeanUtils.toBean(reqVO, IotFileUploadDO.class);
        iotFileUploadMapper.insert(upload);
        return upload.getId();
    }

    @Override
    public void updateIotFileUpload(IotFileUploadSaveReqVO reqVO) {
        validateExists(reqVO.getId());
        IotFileUploadDO upload = BeanUtils.toBean(reqVO, IotFileUploadDO.class);
        iotFileUploadMapper.updateById(upload);
    }

    @Override
    public void deleteIotFileUpload(String id) {
        validateExists(id);
        iotFileUploadMapper.deleteById(id);
    }

    @Override
    public IotFileUploadDO getIotFileUpload(String id) {
        return iotFileUploadMapper.selectById(id);
    }

    @Override
    public PageResult<IotFileUploadDO> getIotFileUploadPage(IotFileUploadPageReqVO pageReqVO) {
        return iotFileUploadMapper.selectPage(pageReqVO);
    }

    private void validateExists(String id) {
        if (iotFileUploadMapper.selectById(id) == null) {
            throw exception(ErrorCodeConstants.IOT_FILE_UPLOAD_NOT_EXISTS);
        }
    }

    @Override
    public IotFileUploadDO findByObjectName(String objectName) {
        return iotFileUploadMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<IotFileUploadDO>()
                        .eq(IotFileUploadDO::getFileName, objectName)
                        .last("LIMIT 1"));
    }
}
