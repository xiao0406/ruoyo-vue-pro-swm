package cn.iocoder.yudao.module.swm.service;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.swm.controller.admin.dicttype.vo.SwmDictTypePageReqVO;
import cn.iocoder.yudao.module.swm.controller.admin.dicttype.vo.SwmDictTypeSaveReqVO;
import cn.iocoder.yudao.module.swm.dal.dataobject.SwmDictTypeDO;
import cn.iocoder.yudao.module.swm.dal.mysql.SwmDictTypeMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.swm.enums.ErrorCodeConstants.COMMON_OPTIONS_NOT_EXISTS;

/**
 * 字典类型 Service 实现类
 */
@Service
@Validated
@Slf4j
public class SwmDictTypeServiceImpl implements SwmDictTypeService {

    @Resource
    private SwmDictTypeMapper swmDictTypeMapper;

    @Override
    public String createDictType(SwmDictTypeSaveReqVO createReqVO) {
        // 插入字典类型
        SwmDictTypeDO dictType = BeanUtils.toBean(createReqVO, SwmDictTypeDO.class);
        swmDictTypeMapper.insert(dictType);
        return dictType.getId();
    }

    @Override
    public void updateDictType(SwmDictTypeSaveReqVO updateReqVO) {
        // 校验存在
        validateDictTypeExists(updateReqVO.getId());

        // 更新字典类型
        SwmDictTypeDO updateObj = BeanUtils.toBean(updateReqVO, SwmDictTypeDO.class);
        swmDictTypeMapper.updateById(updateObj);
    }

    @Override
    public void deleteDictType(String id) {
        // 校验存在
        validateDictTypeExists(id);

        // 删除字典类型
        swmDictTypeMapper.deleteById(id);
    }

    @Override
    public SwmDictTypeDO getDictType(String id) {
        return swmDictTypeMapper.selectById(id);
    }

    @Override
    public PageResult<SwmDictTypeDO> getDictTypePage(SwmDictTypePageReqVO pageReqVO) {
        return swmDictTypeMapper.selectPage(pageReqVO,
                new cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX<SwmDictTypeDO>()
                        .likeIfPresent(SwmDictTypeDO::getDictName, pageReqVO.getDictName())
                        .likeIfPresent(SwmDictTypeDO::getDictType, pageReqVO.getDictType())
                        .eqIfPresent(SwmDictTypeDO::getIsSys, pageReqVO.getIsSys())
                        .orderByDesc(SwmDictTypeDO::getCreateTime));
    }

    private void validateDictTypeExists(String id) {
        if (swmDictTypeMapper.selectById(id) == null) {
            throw exception(COMMON_OPTIONS_NOT_EXISTS);
        }
    }

}
