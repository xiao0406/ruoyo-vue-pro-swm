package cn.iocoder.yudao.module.swm.service;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.swm.controller.admin.dictdata.vo.SwmDictDataPageReqVO;
import cn.iocoder.yudao.module.swm.controller.admin.dictdata.vo.SwmDictDataSaveReqVO;
import cn.iocoder.yudao.module.swm.dal.dataobject.SwmDictDataDO;
import cn.iocoder.yudao.module.swm.dal.mysql.SwmDictDataMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.swm.enums.ErrorCodeConstants.COMMON_OPTIONS_NOT_EXISTS;

/**
 * 字典数据 Service 实现类
 */
@Service
@Validated
@Slf4j
public class SwmDictDataServiceImpl implements SwmDictDataService {

    @Resource
    private SwmDictDataMapper swmDictDataMapper;

    @Override
    public String createDictData(SwmDictDataSaveReqVO createReqVO) {
        // 插入字典数据
        SwmDictDataDO dictData = BeanUtils.toBean(createReqVO, SwmDictDataDO.class);
        swmDictDataMapper.insert(dictData);
        return dictData.getId();
    }

    @Override
    public void updateDictData(SwmDictDataSaveReqVO updateReqVO) {
        // 校验存在
        validateDictDataExists(updateReqVO.getId());

        // 更新字典数据
        SwmDictDataDO updateObj = BeanUtils.toBean(updateReqVO, SwmDictDataDO.class);
        swmDictDataMapper.updateById(updateObj);
    }

    @Override
    public void deleteDictData(String id) {
        // 校验存在
        validateDictDataExists(id);

        // 删除字典数据
        swmDictDataMapper.deleteById(id);
    }

    @Override
    public SwmDictDataDO getDictData(String id) {
        return swmDictDataMapper.selectById(id);
    }

    @Override
    public PageResult<SwmDictDataDO> getDictDataPage(SwmDictDataPageReqVO pageReqVO) {
        return swmDictDataMapper.selectPage(pageReqVO, new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>());
    }

    private void validateDictDataExists(String id) {
        if (swmDictDataMapper.selectById(id) == null) {
            throw exception(COMMON_OPTIONS_NOT_EXISTS);
        }
    }

}
