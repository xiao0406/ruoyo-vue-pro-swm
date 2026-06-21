package cn.iocoder.yudao.module.swm.service.impl;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.swm.controller.admin.safetyeducation.vo.*;
import cn.iocoder.yudao.module.swm.dal.dataobject.SwmSafetyEducationDO;
import cn.iocoder.yudao.module.swm.dal.mysql.SwmSafetyEducationMapper;
import cn.iocoder.yudao.module.swm.enums.ErrorCodeConstants;
import cn.iocoder.yudao.module.swm.service.SwmSafetyEducationService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;

@Service
@Validated
@Slf4j
public class SwmSafetyEducationServiceImpl implements SwmSafetyEducationService {
    @Resource
    private SwmSafetyEducationMapper mapper;

    @Override
    public String createSwmSafetyEducation(SwmSafetyEducationSaveReqVO reqVO) {
        SwmSafetyEducationDO edu = BeanUtils.toBean(reqVO, SwmSafetyEducationDO.class);
        mapper.insert(edu);
        return edu.getId();
    }

    @Override
    public void updateSwmSafetyEducation(SwmSafetyEducationSaveReqVO reqVO) {
        validateExists(reqVO.getId());
        mapper.updateById(BeanUtils.toBean(reqVO, SwmSafetyEducationDO.class));
    }

    @Override
    public void deleteSwmSafetyEducation(String id) {
        validateExists(id);
        mapper.deleteById(id);
    }

    @Override
    public SwmSafetyEducationDO getSwmSafetyEducation(String id) {
        return mapper.selectById(id);
    }

    @Override
    public PageResult<SwmSafetyEducationDO> getSwmSafetyEducationPage(SwmSafetyEducationPageReqVO pageReqVO) {
        return mapper.selectPage(pageReqVO, new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>());
    }

    private void validateExists(String id) {
        if (mapper.selectById(id) == null) throw exception(ErrorCodeConstants.SAFETY_EDUCATION_NOT_EXISTS);
    }
}
