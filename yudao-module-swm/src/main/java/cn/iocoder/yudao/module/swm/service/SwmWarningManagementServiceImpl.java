package cn.iocoder.yudao.module.swm.service;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.swm.controller.admin.warning.vo.SwmWarningManagementPageReqVO;
import cn.iocoder.yudao.module.swm.controller.admin.warning.vo.SwmWarningManagementSaveReqVO;
import cn.iocoder.yudao.module.swm.dal.dataobject.SwmWarningManagementDO;
import cn.iocoder.yudao.module.swm.dal.mysql.SwmWarningManagementMapper;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.swm.enums.ErrorCodeConstants.WARNING_NOT_EXISTS;

/**
 * 预警管理 Service 实现类
 */
@Service
@Validated
@Slf4j
public class SwmWarningManagementServiceImpl implements SwmWarningManagementService {

    @Resource
    private SwmWarningManagementMapper swmWarningManagementMapper;

    @Override
    public String createWarningManagement(SwmWarningManagementSaveReqVO createReqVO) {
        // 插入预警记录
        SwmWarningManagementDO warningManagement = BeanUtils.toBean(createReqVO, SwmWarningManagementDO.class);
        swmWarningManagementMapper.insert(warningManagement);
        return warningManagement.getId();
    }

    @Override
    public void updateWarningManagement(SwmWarningManagementSaveReqVO updateReqVO) {
        // 校验存在
        validateWarningManagementExists(updateReqVO.getId());

        // 更新预警记录
        SwmWarningManagementDO updateObj = BeanUtils.toBean(updateReqVO, SwmWarningManagementDO.class);
        swmWarningManagementMapper.updateById(updateObj);
    }

    @Override
    public void deleteWarningManagement(String id) {
        // 校验存在
        validateWarningManagementExists(id);

        // 删除预警记录
        swmWarningManagementMapper.deleteById(id);
    }

    @Override
    public SwmWarningManagementDO getWarningManagement(String id) {
        return swmWarningManagementMapper.selectById(id);
    }

    @Override
    public PageResult<SwmWarningManagementDO> getWarningManagementPage(SwmWarningManagementPageReqVO pageReqVO) {
        return swmWarningManagementMapper.selectPage(pageReqVO,
                new LambdaQueryWrapperX<SwmWarningManagementDO>()
                        .likeIfPresent(SwmWarningManagementDO::getPersonName, pageReqVO.getPersonName())
                        .eqIfPresent(SwmWarningManagementDO::getWarningType, pageReqVO.getWarningType())
                        .eqIfPresent(SwmWarningManagementDO::getHandleStatus, pageReqVO.getHandleStatus())
                        .eqIfPresent(SwmWarningManagementDO::getType, pageReqVO.getType())
                        .likeIfPresent(SwmWarningManagementDO::getArea, pageReqVO.getArea())
                        .likeIfPresent(SwmWarningManagementDO::getHandler, pageReqVO.getHandler())
                        .betweenIfPresent(SwmWarningManagementDO::getAlarmTime,
                                pageReqVO.getBeginAlarmTime(), pageReqVO.getEndAlarmTime())
                        .orderByDesc(SwmWarningManagementDO::getAlarmTime));
    }

    private void validateWarningManagementExists(String id) {
        if (swmWarningManagementMapper.selectById(id) == null) {
            throw exception(WARNING_NOT_EXISTS);
        }
    }

}
