package cn.iocoder.yudao.module.swm.service;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.swm.controller.admin.danger_disposal.vo.SwmDangerDisposalPageReqVO;
import cn.iocoder.yudao.module.swm.controller.admin.danger_disposal.vo.SwmDangerDisposalSaveReqVO;
import cn.iocoder.yudao.module.swm.dal.dataobject.SwmDangerDisposalDO;
import cn.iocoder.yudao.module.swm.dal.mysql.SwmDangerDisposalMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.swm.enums.ErrorCodeConstants.DANGER_DISPOSAL_NOT_EXISTS;

/**
 * 隐患处置 Service 实现类
 */
@Service
@Validated
@Slf4j
public class SwmDangerDisposalServiceImpl implements SwmDangerDisposalService {

    @Resource
    private SwmDangerDisposalMapper swmDangerDisposalMapper;

    @Override
    public String createDangerDisposal(SwmDangerDisposalSaveReqVO createReqVO) {
        // 插入隐患处置记录
        SwmDangerDisposalDO dangerDisposal = BeanUtils.toBean(createReqVO, SwmDangerDisposalDO.class);
        swmDangerDisposalMapper.insert(dangerDisposal);
        return dangerDisposal.getId();
    }

    @Override
    public void updateDangerDisposal(SwmDangerDisposalSaveReqVO updateReqVO) {
        // 校验存在
        validateDangerDisposalExists(updateReqVO.getId());

        // 更新隐患处置记录
        SwmDangerDisposalDO updateObj = BeanUtils.toBean(updateReqVO, SwmDangerDisposalDO.class);
        swmDangerDisposalMapper.updateById(updateObj);
    }

    @Override
    public void deleteDangerDisposal(String id) {
        // 校验存在
        validateDangerDisposalExists(id);

        // 删除隐患处置记录
        swmDangerDisposalMapper.deleteById(id);
    }

    @Override
    public SwmDangerDisposalDO getDangerDisposal(String id) {
        return swmDangerDisposalMapper.selectById(id);
    }

    @Override
    public PageResult<SwmDangerDisposalDO> getDangerDisposalPage(SwmDangerDisposalPageReqVO pageReqVO) {
        return swmDangerDisposalMapper.selectPage(pageReqVO,
                new cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX<SwmDangerDisposalDO>()
                        .likeIfPresent(SwmDangerDisposalDO::getDangerName, pageReqVO.getDangerName())
                        .likeIfPresent(SwmDangerDisposalDO::getLocation, pageReqVO.getLocation())
                        .eqIfPresent(SwmDangerDisposalDO::getDisposalStatus, pageReqVO.getDisposalStatus())
                        .orderByDesc(SwmDangerDisposalDO::getDisposalTime));
    }

    private void validateDangerDisposalExists(String id) {
        if (swmDangerDisposalMapper.selectById(id) == null) {
            throw exception(DANGER_DISPOSAL_NOT_EXISTS);
        }
    }

}
