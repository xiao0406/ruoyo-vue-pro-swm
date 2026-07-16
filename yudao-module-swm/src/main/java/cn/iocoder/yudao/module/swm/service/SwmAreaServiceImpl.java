package cn.iocoder.yudao.module.swm.service;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.swm.controller.admin.area.vo.SwmAreaPageReqVO;
import cn.iocoder.yudao.module.swm.controller.admin.area.vo.SwmAreaSaveReqVO;
import cn.iocoder.yudao.module.swm.dal.dataobject.SwmAreaDO;
import cn.iocoder.yudao.module.swm.dal.mysql.SwmAreaMapper;
import jakarta.annotation.Resource;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.swm.enums.ErrorCodeConstants.AREA_NOT_EXISTS;

/**
 * 区域管理 Service 实现类
 */
@Service
@Validated
@Slf4j
public class SwmAreaServiceImpl implements SwmAreaService {

    @Resource
    private SwmAreaMapper swmAreaMapper;

    @Override
    public String createArea(SwmAreaSaveReqVO createReqVO) {
        // 插入区域
        SwmAreaDO area = BeanUtils.toBean(createReqVO, SwmAreaDO.class);
        swmAreaMapper.insert(area);
        return area.getId();
    }

    @Override
    public void updateArea(SwmAreaSaveReqVO updateReqVO) {
        // 校验存在
        validateAreaExists(updateReqVO.getId());

        // 更新区域
        SwmAreaDO updateObj = BeanUtils.toBean(updateReqVO, SwmAreaDO.class);
        swmAreaMapper.updateById(updateObj);
    }

    @Override
    public void deleteArea(String id) {
        // 校验存在
        validateAreaExists(id);

        // 删除区域
        swmAreaMapper.deleteById(id);
    }

    @Override
    public SwmAreaDO getArea(String id) {
        return swmAreaMapper.selectById(id);
    }

    @Override
    public PageResult<SwmAreaDO> getAreaPage(SwmAreaPageReqVO pageReqVO) {
        return swmAreaMapper.selectPage(pageReqVO,
                new cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX<SwmAreaDO>()
                        .likeIfPresent(SwmAreaDO::getAreaName, pageReqVO.getAreaName())
                        .eqIfPresent(SwmAreaDO::getAreaType, pageReqVO.getAreaType())
                        .eqIfPresent(SwmAreaDO::getWorkShop, pageReqVO.getWorkShop())
                        .eqIfPresent(SwmAreaDO::getIsScreenShow, pageReqVO.getIsScreenShow())
                        .orderByDesc(SwmAreaDO::getCreateTime));
    }

    @Override
    public List<SwmAreaDO> findList(SwmAreaDO query) {
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SwmAreaDO> wrapper =
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        if (query.getStatus() != null) {
            wrapper.eq(SwmAreaDO::getStatus, query.getStatus());
        }
        if (query.getAreaType() != null) {
            wrapper.eq(SwmAreaDO::getAreaType, query.getAreaType());
        }
        return swmAreaMapper.selectList(wrapper);
    }

    @Override
    public List<SwmAreaDO> findListByIds(java.util.Collection<String> ids) {
        if (ids == null || ids.isEmpty()) {
            return java.util.Collections.emptyList();
        }
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SwmAreaDO> wrapper =
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        wrapper.in(SwmAreaDO::getId, ids);
        return swmAreaMapper.selectList(wrapper);
    }

    private void validateAreaExists(String id) {
        if (swmAreaMapper.selectById(id) == null) {
            throw exception(AREA_NOT_EXISTS);
        }
    }

}
