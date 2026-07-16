package cn.iocoder.yudao.module.swm.service;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.swm.controller.admin.beaconcolor.vo.SwmBeaconColorConfigPageReqVO;
import cn.iocoder.yudao.module.swm.controller.admin.beaconcolor.vo.SwmBeaconColorConfigSaveReqVO;
import cn.iocoder.yudao.module.swm.dal.dataobject.SwmBeaconColorConfigDO;
import cn.iocoder.yudao.module.swm.dal.mysql.SwmBeaconColorConfigMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.swm.enums.ErrorCodeConstants.BEACON_STATION_NOT_EXISTS;

/**
 * 信标颜色配置 Service 实现类
 */
@Service
@Validated
@Slf4j
public class SwmBeaconColorConfigServiceImpl implements SwmBeaconColorConfigService {

    @Resource
    private SwmBeaconColorConfigMapper swmBeaconColorConfigMapper;

    @Override
    public String createBeaconColorConfig(SwmBeaconColorConfigSaveReqVO createReqVO) {
        // 插入信标颜色配置
        SwmBeaconColorConfigDO beaconColorConfig = BeanUtils.toBean(createReqVO, SwmBeaconColorConfigDO.class);
        swmBeaconColorConfigMapper.insert(beaconColorConfig);
        return beaconColorConfig.getId();
    }

    @Override
    public void updateBeaconColorConfig(SwmBeaconColorConfigSaveReqVO updateReqVO) {
        // 校验存在
        validateBeaconColorConfigExists(updateReqVO.getId());

        // 更新信标颜色配置
        SwmBeaconColorConfigDO updateObj = BeanUtils.toBean(updateReqVO, SwmBeaconColorConfigDO.class);
        swmBeaconColorConfigMapper.updateById(updateObj);
    }

    @Override
    public void deleteBeaconColorConfig(String id) {
        // 校验存在
        validateBeaconColorConfigExists(id);

        // 删除信标颜色配置
        swmBeaconColorConfigMapper.deleteById(id);
    }

    @Override
    public SwmBeaconColorConfigDO getBeaconColorConfig(String id) {
        return swmBeaconColorConfigMapper.selectById(id);
    }

    @Override
    public PageResult<SwmBeaconColorConfigDO> getBeaconColorConfigPage(SwmBeaconColorConfigPageReqVO pageReqVO) {
        return swmBeaconColorConfigMapper.selectPage(pageReqVO,
                new cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX<SwmBeaconColorConfigDO>()
                        .likeIfPresent(SwmBeaconColorConfigDO::getName, pageReqVO.getName())
                        .eqIfPresent(SwmBeaconColorConfigDO::getColor, pageReqVO.getColor())
                        .orderByDesc(SwmBeaconColorConfigDO::getCreateTime));
    }

    private void validateBeaconColorConfigExists(String id) {
        if (swmBeaconColorConfigMapper.selectById(id) == null) {
            throw exception(BEACON_STATION_NOT_EXISTS);
        }
    }

}
