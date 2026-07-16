package cn.iocoder.yudao.module.swm.service;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.swm.controller.admin.one_click_recall.vo.SwmOneClickRecallPageReqVO;
import cn.iocoder.yudao.module.swm.controller.admin.one_click_recall.vo.SwmOneClickRecallSaveReqVO;
import cn.iocoder.yudao.module.swm.dal.dataobject.SwmOneClickRecallDO;
import cn.iocoder.yudao.module.swm.dal.mysql.SwmOneClickRecallMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;
import java.util.UUID;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.swm.enums.ErrorCodeConstants.ONE_CLICK_RECALL_NOT_EXISTS;

/**
 * 一键召回 Service 实现类
 */
@Service
@Validated
@Slf4j
public class SwmOneClickRecallServiceImpl implements SwmOneClickRecallService {

    @Resource
    private SwmOneClickRecallMapper swmOneClickRecallMapper;

    @Override
    public String createOneClickRecall(SwmOneClickRecallSaveReqVO createReqVO) {
        SwmOneClickRecallDO oneClickRecall = BeanUtils.toBean(createReqVO, SwmOneClickRecallDO.class);
        // JeeSite 原表使用 varchar 主键，迁移到 MyBatis Plus 后需要在业务层补齐主键和默认值。
        oneClickRecall.setId(UUID.randomUUID().toString().replace("-", ""));
        oneClickRecall.setRecallTime(LocalDateTime.now());
        oneClickRecall.setRecallSuccessCount(oneClickRecall.getRecallSuccessCount() == null ? 0 : oneClickRecall.getRecallSuccessCount());
        oneClickRecall.setRecallFailCount(oneClickRecall.getRecallFailCount() == null ? 0 : oneClickRecall.getRecallFailCount());
        oneClickRecall.setRecallResult(oneClickRecall.getRecallResult() == null ? "0" : oneClickRecall.getRecallResult());
        oneClickRecall.setStatus("0");
        swmOneClickRecallMapper.insert(oneClickRecall);
        return oneClickRecall.getId();
    }

    @Override
    public void updateOneClickRecall(SwmOneClickRecallSaveReqVO updateReqVO) {
        validateOneClickRecallExists(updateReqVO.getId());
        SwmOneClickRecallDO updateObj = BeanUtils.toBean(updateReqVO, SwmOneClickRecallDO.class);
        swmOneClickRecallMapper.updateById(updateObj);
    }

    @Override
    public void deleteOneClickRecall(String id) {
        validateOneClickRecallExists(id);
        swmOneClickRecallMapper.deleteById(id);
    }

    @Override
    public SwmOneClickRecallDO getOneClickRecall(String id) {
        return swmOneClickRecallMapper.selectById(id);
    }

    @Override
    public PageResult<SwmOneClickRecallDO> getOneClickRecallPage(SwmOneClickRecallPageReqVO pageReqVO) {
        return swmOneClickRecallMapper.selectPage(pageReqVO,
                new cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX<SwmOneClickRecallDO>()
                        .likeIfPresent(SwmOneClickRecallDO::getTemplateName, pageReqVO.getTemplateName())
                        .likeIfPresent(SwmOneClickRecallDO::getEvacuationPlan, pageReqVO.getEvacuationPlan())
                        .eqIfPresent(SwmOneClickRecallDO::getRecallResult, pageReqVO.getRecallResult())
                        .orderByDesc(SwmOneClickRecallDO::getRecallTime));
    }

    private void validateOneClickRecallExists(String id) {
        if (swmOneClickRecallMapper.selectById(id) == null) {
            throw exception(ONE_CLICK_RECALL_NOT_EXISTS);
        }
    }

}
