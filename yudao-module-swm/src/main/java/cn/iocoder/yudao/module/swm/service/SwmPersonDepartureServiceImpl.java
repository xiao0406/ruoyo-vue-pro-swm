package cn.iocoder.yudao.module.swm.service;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.swm.controller.admin.persondeparture.vo.SwmPersonDeparturePageReqVO;
import cn.iocoder.yudao.module.swm.controller.admin.persondeparture.vo.SwmPersonDepartureSaveReqVO;
import cn.iocoder.yudao.module.swm.dal.dataobject.SwmPersonDepartureDO;
import cn.iocoder.yudao.module.swm.dal.mysql.SwmPersonDepartureMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.swm.enums.ErrorCodeConstants.PERSON_NOT_EXISTS;

/**
 * 人员退场 Service 实现类
 */
@Service
@Validated
@Slf4j
public class SwmPersonDepartureServiceImpl implements SwmPersonDepartureService {

    @Resource
    private SwmPersonDepartureMapper swmPersonDepartureMapper;

    @Override
    public String createPersonDeparture(SwmPersonDepartureSaveReqVO createReqVO) {
        // 插入人员退场
        SwmPersonDepartureDO personDeparture = BeanUtils.toBean(createReqVO, SwmPersonDepartureDO.class);
        swmPersonDepartureMapper.insert(personDeparture);
        return personDeparture.getId();
    }

    @Override
    public void updatePersonDeparture(SwmPersonDepartureSaveReqVO updateReqVO) {
        // 校验存在
        validatePersonDepartureExists(updateReqVO.getId());

        // 更新人员退场
        SwmPersonDepartureDO updateObj = BeanUtils.toBean(updateReqVO, SwmPersonDepartureDO.class);
        swmPersonDepartureMapper.updateById(updateObj);
    }

    @Override
    public void deletePersonDeparture(String id) {
        // 校验存在
        validatePersonDepartureExists(id);

        // 删除人员退场
        swmPersonDepartureMapper.deleteById(id);
    }

    @Override
    public SwmPersonDepartureDO getPersonDeparture(String id) {
        return swmPersonDepartureMapper.selectById(id);
    }

    @Override
    public PageResult<SwmPersonDepartureDO> getPersonDeparturePage(SwmPersonDeparturePageReqVO pageReqVO) {
        return swmPersonDepartureMapper.selectPage(pageReqVO, new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>());
    }

    private void validatePersonDepartureExists(String id) {
        if (swmPersonDepartureMapper.selectById(id) == null) {
            throw exception(PERSON_NOT_EXISTS);
        }
    }

}
