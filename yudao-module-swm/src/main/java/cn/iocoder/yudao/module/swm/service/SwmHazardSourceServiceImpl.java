package cn.iocoder.yudao.module.swm.service;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.swm.controller.admin.hazard.vo.SwmHazardSourcePageReqVO;
import cn.iocoder.yudao.module.swm.controller.admin.hazard.vo.SwmHazardSourceSaveReqVO;
import cn.iocoder.yudao.module.swm.dal.dataobject.SwmHazardSourceDO;
import cn.iocoder.yudao.module.swm.dal.mysql.SwmHazardSourceMapper;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.swm.enums.ErrorCodeConstants.HAZARD_SOURCE_NOT_EXISTS;

@Service
@Validated
@Slf4j
public class SwmHazardSourceServiceImpl implements SwmHazardSourceService {

    @Resource
    private SwmHazardSourceMapper hazardSourceMapper;

    @Override
    public String createHazardSource(SwmHazardSourceSaveReqVO createReqVO) {
        SwmHazardSourceDO hazardSource = BeanUtils.toBean(createReqVO, SwmHazardSourceDO.class);
        hazardSourceMapper.insert(hazardSource);
        return hazardSource.getId();
    }

    @Override
    public void updateHazardSource(SwmHazardSourceSaveReqVO updateReqVO) {
        validateHazardSourceExists(updateReqVO.getId());
        SwmHazardSourceDO updateObj = BeanUtils.toBean(updateReqVO, SwmHazardSourceDO.class);
        hazardSourceMapper.updateById(updateObj);
    }

    @Override
    public void deleteHazardSource(String id) {
        validateHazardSourceExists(id);
        hazardSourceMapper.deleteById(id);
    }

    @Override
    public SwmHazardSourceDO getHazardSource(String id) {
        return hazardSourceMapper.selectById(id);
    }

    @Override
    public PageResult<SwmHazardSourceDO> getHazardSourcePage(SwmHazardSourcePageReqVO pageReqVO) {
        return hazardSourceMapper.selectPage(pageReqVO,
                new LambdaQueryWrapperX<SwmHazardSourceDO>()
                        .likeIfPresent(SwmHazardSourceDO::getHazardName, pageReqVO.getHazardName())
                        .eqIfPresent(SwmHazardSourceDO::getHazardCategory, pageReqVO.getHazardCategory())
                        .eqIfPresent(SwmHazardSourceDO::getHazardStatus, pageReqVO.getHazardStatus())
                        .likeIfPresent(SwmHazardSourceDO::getLocation, pageReqVO.getLocation())
                        .likeIfPresent(SwmHazardSourceDO::getResponsiblePerson, pageReqVO.getResponsiblePerson())
                        .orderByDesc(SwmHazardSourceDO::getCreateTime));
    }

    private void validateHazardSourceExists(String id) {
        if (hazardSourceMapper.selectById(id) == null) {
            throw exception(HAZARD_SOURCE_NOT_EXISTS);
        }
    }
}
