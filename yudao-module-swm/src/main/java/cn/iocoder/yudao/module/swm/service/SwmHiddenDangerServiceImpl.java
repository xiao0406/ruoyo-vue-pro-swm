package cn.iocoder.yudao.module.swm.service;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.swm.controller.admin.hidden_danger.vo.SwmHiddenDangerPageReqVO;
import cn.iocoder.yudao.module.swm.controller.admin.hidden_danger.vo.SwmHiddenDangerSaveReqVO;
import cn.iocoder.yudao.module.swm.dal.dataobject.SwmHiddenDangerDO;
import cn.iocoder.yudao.module.swm.dal.mysql.SwmHiddenDangerMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.swm.enums.ErrorCodeConstants.HIDDEN_DANGER_NOT_EXISTS;

/**
 * 隐患排查 Service 实现类
 */
@Service
@Validated
@Slf4j
public class SwmHiddenDangerServiceImpl implements SwmHiddenDangerService {

    @Resource
    private SwmHiddenDangerMapper swmHiddenDangerMapper;

    @Override
    public String createHiddenDanger(SwmHiddenDangerSaveReqVO createReqVO) {
        // 插入隐患排查
        SwmHiddenDangerDO hiddenDanger = BeanUtils.toBean(createReqVO, SwmHiddenDangerDO.class);
        swmHiddenDangerMapper.insert(hiddenDanger);
        return hiddenDanger.getId();
    }

    @Override
    public void updateHiddenDanger(SwmHiddenDangerSaveReqVO updateReqVO) {
        // 校验存在
        validateHiddenDangerExists(updateReqVO.getId());

        // 更新隐患排查
        SwmHiddenDangerDO updateObj = BeanUtils.toBean(updateReqVO, SwmHiddenDangerDO.class);
        swmHiddenDangerMapper.updateById(updateObj);
    }

    @Override
    public void deleteHiddenDanger(String id) {
        // 校验存在
        validateHiddenDangerExists(id);

        // 删除隐患排查
        swmHiddenDangerMapper.deleteById(id);
    }

    @Override
    public SwmHiddenDangerDO getHiddenDanger(String id) {
        return swmHiddenDangerMapper.selectById(id);
    }

    @Override
    public PageResult<SwmHiddenDangerDO> getHiddenDangerPage(SwmHiddenDangerPageReqVO pageReqVO) {
        return swmHiddenDangerMapper.selectPage(pageReqVO,
                new cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX<SwmHiddenDangerDO>()
                        .likeIfPresent(SwmHiddenDangerDO::getDangerName, pageReqVO.getDangerName())
                        .likeIfPresent(SwmHiddenDangerDO::getLocation, pageReqVO.getLocation())
                        .eqIfPresent(SwmHiddenDangerDO::getIsHandled, pageReqVO.getIsHandled())
                        .orderByDesc(SwmHiddenDangerDO::getCreateTime));
    }

    private void validateHiddenDangerExists(String id) {
        if (swmHiddenDangerMapper.selectById(id) == null) {
            throw exception(HIDDEN_DANGER_NOT_EXISTS);
        }
    }

}
