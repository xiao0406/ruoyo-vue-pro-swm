package cn.iocoder.yudao.module.swm.service;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.swm.controller.admin.beacon.vo.SwmBeaconStationPageReqVO;
import cn.iocoder.yudao.module.swm.controller.admin.beacon.vo.SwmBeaconStationSaveReqVO;
import cn.iocoder.yudao.module.swm.dal.dataobject.SwmBeaconStationDO;
import cn.iocoder.yudao.module.swm.dal.mysql.SwmBeaconStationMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.swm.enums.ErrorCodeConstants.BEACON_STATION_NOT_EXISTS;

/**
 * 信标站点 Service 实现类
 */
@Service
@Validated
@Slf4j
public class SwmBeaconStationServiceImpl implements SwmBeaconStationService {

    @Resource
    private SwmBeaconStationMapper swmBeaconStationMapper;

    @Override
    public String createBeaconStation(SwmBeaconStationSaveReqVO createReqVO) {
        // 插入信标站点
        SwmBeaconStationDO beaconStation = BeanUtils.toBean(createReqVO, SwmBeaconStationDO.class);
        swmBeaconStationMapper.insert(beaconStation);
        return beaconStation.getId();
    }

    @Override
    public void updateBeaconStation(SwmBeaconStationSaveReqVO updateReqVO) {
        // 校验存在
        validateBeaconStationExists(updateReqVO.getId());

        // 更新信标站点
        SwmBeaconStationDO updateObj = BeanUtils.toBean(updateReqVO, SwmBeaconStationDO.class);
        swmBeaconStationMapper.updateById(updateObj);
    }

    @Override
    public void deleteBeaconStation(String id) {
        // 校验存在
        validateBeaconStationExists(id);

        // 删除信标站点
        swmBeaconStationMapper.deleteById(id);
    }

    @Override
    public SwmBeaconStationDO getBeaconStation(String id) {
        return swmBeaconStationMapper.selectById(id);
    }

    @Override
    public PageResult<SwmBeaconStationDO> getBeaconStationPage(SwmBeaconStationPageReqVO pageReqVO) {
        return swmBeaconStationMapper.selectPage(pageReqVO, new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>());
    }

    private void validateBeaconStationExists(String id) {
        if (swmBeaconStationMapper.selectById(id) == null) {
            throw exception(BEACON_STATION_NOT_EXISTS);
        }
    }

}
