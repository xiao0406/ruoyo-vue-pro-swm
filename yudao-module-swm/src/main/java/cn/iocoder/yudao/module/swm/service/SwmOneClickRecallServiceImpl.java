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
        return swmOneClickRecallMapper.selectPage(pageReqVO, new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>());
    }

    private void validateOneClickRecallExists(String id) {
        if (swmOneClickRecallMapper.selectById(id) == null) {
            throw exception(ONE_CLICK_RECALL_NOT_EXISTS);
        }
    }

}
