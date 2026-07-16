package cn.iocoder.yudao.module.swm.service;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.swm.controller.admin.handle_record.vo.SwmHandleRecordPageReqVO;
import cn.iocoder.yudao.module.swm.controller.admin.handle_record.vo.SwmHandleRecordSaveReqVO;
import cn.iocoder.yudao.module.swm.dal.dataobject.SwmHandleRecordDO;
import cn.iocoder.yudao.module.swm.dal.mysql.SwmHandleRecordMapper;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.swm.dal.dataobject.SwmWarningManagementDO;
import cn.iocoder.yudao.module.swm.dal.mysql.SwmWarningManagementMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.swm.enums.ErrorCodeConstants.WARNING_NOT_EXISTS;

/**
 * 处置记录 Service 实现类
 */
@Service
@Validated
@Slf4j
public class SwmHandleRecordServiceImpl implements SwmHandleRecordService {

    @Resource
    private SwmHandleRecordMapper swmHandleRecordMapper;

    @Resource
    private SwmWarningManagementMapper swmWarningManagementMapper;

    @Override
    public String createHandleRecord(SwmHandleRecordSaveReqVO createReqVO) {
        SwmHandleRecordDO handleRecord = BeanUtils.toBean(createReqVO, SwmHandleRecordDO.class);
        if (handleRecord.getHandleStatus() == null || handleRecord.getHandleStatus().isBlank()) {
            handleRecord.setHandleStatus("0");
        }
        if ((handleRecord.getWarningRecord() == null || handleRecord.getWarningRecord().isBlank())
                && handleRecord.getRecordName() != null) {
            handleRecord.setWarningRecord(handleRecord.getRecordName());
        }
        swmHandleRecordMapper.insert(handleRecord);
        syncWarning(handleRecord);
        return handleRecord.getId();
    }

    @Override
    public void updateHandleRecord(SwmHandleRecordSaveReqVO updateReqVO) {
        validateHandleRecordExists(updateReqVO.getId());
        SwmHandleRecordDO updateObj = BeanUtils.toBean(updateReqVO, SwmHandleRecordDO.class);
        swmHandleRecordMapper.updateById(updateObj);
        syncWarning(updateObj);
    }

    @Override
    public void deleteHandleRecord(String id) {
        validateHandleRecordExists(id);
        swmHandleRecordMapper.deleteById(id);
    }

    @Override
    public SwmHandleRecordDO getHandleRecord(String id) {
        return swmHandleRecordMapper.selectById(id);
    }

    @Override
    public PageResult<SwmHandleRecordDO> getHandleRecordPage(SwmHandleRecordPageReqVO pageReqVO) {
        return swmHandleRecordMapper.selectPage(pageReqVO,
                new LambdaQueryWrapperX<SwmHandleRecordDO>()
                        .likeIfPresent(SwmHandleRecordDO::getRecordName, pageReqVO.getRecordName())
                        .eqIfPresent(SwmHandleRecordDO::getWarningId, pageReqVO.getWarningId())
                        .eqIfPresent(SwmHandleRecordDO::getHandleStatus, pageReqVO.getHandleStatus())
                        .orderByDesc(SwmHandleRecordDO::getCreateTime));
    }

    /** Keep the warning row consistent with the disposal record, as the JeeSite save flow did. */
    private void syncWarning(SwmHandleRecordDO handleRecord) {
        if (handleRecord.getWarningId() == null || handleRecord.getWarningId().isBlank()) {
            return;
        }
        SwmWarningManagementDO warning = new SwmWarningManagementDO();
        warning.setId(handleRecord.getWarningId());
        warning.setHandler(handleRecord.getHandler());
        warning.setHandleTime(handleRecord.getHandleTime());
        warning.setHandleProcess(handleRecord.getHandleProcess());
        warning.setHandleStatus(handleRecord.getHandleStatus());
        warning.setAttachment(handleRecord.getAttachment());
        swmWarningManagementMapper.updateById(warning);
    }

    private void validateHandleRecordExists(String id) {
        if (swmHandleRecordMapper.selectById(id) == null) {
            throw exception(WARNING_NOT_EXISTS);
        }
    }

}
