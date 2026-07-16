package cn.iocoder.yudao.module.swm.service;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.swm.controller.admin.worktype.vo.SwmWorkTypePageReqVO;
import cn.iocoder.yudao.module.swm.controller.admin.worktype.vo.SwmWorkTypeSaveReqVO;
import cn.iocoder.yudao.module.swm.dal.dataobject.SwmWorkTypeDO;
import cn.iocoder.yudao.module.swm.dal.mysql.SwmWorkTypeMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.swm.enums.ErrorCodeConstants.WORK_TYPE_NOT_EXISTS;

/**
 * 工种管理 Service 实现类
 */
@Service
@Validated
@Slf4j
public class SwmWorkTypeServiceImpl implements SwmWorkTypeService {

    @Resource
    private SwmWorkTypeMapper swmWorkTypeMapper;

    @Override
    public String createWorkType(SwmWorkTypeSaveReqVO createReqVO) {
        // 插入工种
        SwmWorkTypeDO workType = BeanUtils.toBean(createReqVO, SwmWorkTypeDO.class);
        swmWorkTypeMapper.insert(workType);
        return workType.getId();
    }

    @Override
    public void updateWorkType(SwmWorkTypeSaveReqVO updateReqVO) {
        // 校验存在
        validateWorkTypeExists(updateReqVO.getId());

        // 更新工种
        SwmWorkTypeDO updateObj = BeanUtils.toBean(updateReqVO, SwmWorkTypeDO.class);
        swmWorkTypeMapper.updateById(updateObj);
    }

    @Override
    public void deleteWorkType(String id) {
        // 校验存在
        validateWorkTypeExists(id);

        // 删除工种
        swmWorkTypeMapper.deleteById(id);
    }

    @Override
    public SwmWorkTypeDO getWorkType(String id) {
        return swmWorkTypeMapper.selectById(id);
    }

    @Override
    public PageResult<SwmWorkTypeDO> getWorkTypePage(SwmWorkTypePageReqVO pageReqVO) {
        return swmWorkTypeMapper.selectPage(pageReqVO,
                new cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX<SwmWorkTypeDO>()
                        .likeIfPresent(SwmWorkTypeDO::getWorkType, pageReqVO.getWorkType())
                        .likeIfPresent(SwmWorkTypeDO::getWorkTypeCode, pageReqVO.getWorkTypeCode())
                        .orderByDesc(SwmWorkTypeDO::getCreateTime));
    }

    @Override
    public List<SwmWorkTypeDO> getActiveWorkTypes() {
        return swmWorkTypeMapper.selectList(new LambdaQueryWrapperX<SwmWorkTypeDO>()
                .orderByAsc(SwmWorkTypeDO::getWorkType));
    }

    private void validateWorkTypeExists(String id) {
        if (swmWorkTypeMapper.selectById(id) == null) {
            throw exception(WORK_TYPE_NOT_EXISTS);
        }
    }

}
