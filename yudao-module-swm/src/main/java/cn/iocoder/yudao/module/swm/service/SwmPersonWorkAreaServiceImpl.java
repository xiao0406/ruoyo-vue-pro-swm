package cn.iocoder.yudao.module.swm.service;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.swm.controller.admin.personworkarea.vo.SwmPersonWorkAreaPageReqVO;
import cn.iocoder.yudao.module.swm.controller.admin.personworkarea.vo.SwmPersonWorkAreaSaveReqVO;
import cn.iocoder.yudao.module.swm.dal.dataobject.SwmPersonWorkAreaDO;
import cn.iocoder.yudao.module.swm.dal.mysql.SwmPersonWorkAreaMapper;
import jakarta.annotation.Resource;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.swm.enums.ErrorCodeConstants.PERSON_NOT_EXISTS;

/**
 * 人员工作区域 Service 实现类
 */
@Service
@Validated
@Slf4j
public class SwmPersonWorkAreaServiceImpl implements SwmPersonWorkAreaService {

    @Resource
    private SwmPersonWorkAreaMapper swmPersonWorkAreaMapper;

    @Override
    public String createPersonWorkArea(SwmPersonWorkAreaSaveReqVO createReqVO) {
        // 插入人员工作区域
        SwmPersonWorkAreaDO personWorkArea = BeanUtils.toBean(createReqVO, SwmPersonWorkAreaDO.class);
        swmPersonWorkAreaMapper.insert(personWorkArea);
        return personWorkArea.getId();
    }

    @Override
    public void updatePersonWorkArea(SwmPersonWorkAreaSaveReqVO updateReqVO) {
        // 校验存在
        validatePersonWorkAreaExists(updateReqVO.getId());

        // 更新人员工作区域
        SwmPersonWorkAreaDO updateObj = BeanUtils.toBean(updateReqVO, SwmPersonWorkAreaDO.class);
        swmPersonWorkAreaMapper.updateById(updateObj);
    }

    @Override
    public void deletePersonWorkArea(String id) {
        // 校验存在
        validatePersonWorkAreaExists(id);

        // 删除人员工作区域
        swmPersonWorkAreaMapper.deleteById(id);
    }

    @Override
    public SwmPersonWorkAreaDO getPersonWorkArea(String id) {
        return swmPersonWorkAreaMapper.selectById(id);
    }

    @Override
    public PageResult<SwmPersonWorkAreaDO> getPersonWorkAreaPage(SwmPersonWorkAreaPageReqVO pageReqVO) {
        return swmPersonWorkAreaMapper.selectPage(pageReqVO,
                new cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX<SwmPersonWorkAreaDO>()
                        .likeIfPresent(SwmPersonWorkAreaDO::getIdentityCard, pageReqVO.getIdentityCard())
                        .likeIfPresent(SwmPersonWorkAreaDO::getPersonName, pageReqVO.getPersonName())
                        .eqIfPresent(SwmPersonWorkAreaDO::getAreaId, pageReqVO.getAreaId())
                        .orderByDesc(SwmPersonWorkAreaDO::getCreateTime));
    }

    @Override
    public List<SwmPersonWorkAreaDO> findActiveByIdentityCard(String identityCard) {
        return swmPersonWorkAreaMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SwmPersonWorkAreaDO>()
                        .eq(SwmPersonWorkAreaDO::getIdentityCard, identityCard)
                        .eq(SwmPersonWorkAreaDO::getStatus, "0"));
    }

    private void validatePersonWorkAreaExists(String id) {
        if (swmPersonWorkAreaMapper.selectById(id) == null) {
            throw exception(PERSON_NOT_EXISTS);
        }
    }

}
