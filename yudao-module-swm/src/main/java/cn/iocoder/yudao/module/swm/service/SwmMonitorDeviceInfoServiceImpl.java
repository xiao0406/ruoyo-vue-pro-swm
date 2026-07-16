package cn.iocoder.yudao.module.swm.service;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.swm.controller.admin.monitordevice.vo.SwmMonitorDeviceInfoPageReqVO;
import cn.iocoder.yudao.module.swm.controller.admin.monitordevice.vo.SwmMonitorDeviceInfoSaveReqVO;
import cn.iocoder.yudao.module.swm.dal.dataobject.SwmMonitorDeviceInfoDO;
import cn.iocoder.yudao.module.swm.dal.mysql.SwmMonitorDeviceInfoMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.swm.enums.ErrorCodeConstants.PERSON_NOT_EXISTS;

/**
 * 监控设备 Service 实现类
 */
@Service
@Validated
@Slf4j
public class SwmMonitorDeviceInfoServiceImpl implements SwmMonitorDeviceInfoService {

    @Resource
    private SwmMonitorDeviceInfoMapper swmMonitorDeviceInfoMapper;

    @Override
    public String createMonitorDeviceInfo(SwmMonitorDeviceInfoSaveReqVO createReqVO) {
        // 插入监控设备
        SwmMonitorDeviceInfoDO monitorDeviceInfo = BeanUtils.toBean(createReqVO, SwmMonitorDeviceInfoDO.class);
        swmMonitorDeviceInfoMapper.insert(monitorDeviceInfo);
        return monitorDeviceInfo.getId();
    }

    @Override
    public void updateMonitorDeviceInfo(SwmMonitorDeviceInfoSaveReqVO updateReqVO) {
        // 校验存在
        validateMonitorDeviceInfoExists(updateReqVO.getId());

        // 更新监控设备
        SwmMonitorDeviceInfoDO updateObj = BeanUtils.toBean(updateReqVO, SwmMonitorDeviceInfoDO.class);
        swmMonitorDeviceInfoMapper.updateById(updateObj);
    }

    @Override
    public void deleteMonitorDeviceInfo(String id) {
        // 校验存在
        validateMonitorDeviceInfoExists(id);

        // 删除监控设备
        swmMonitorDeviceInfoMapper.deleteById(id);
    }

    @Override
    public SwmMonitorDeviceInfoDO getMonitorDeviceInfo(String id) {
        return swmMonitorDeviceInfoMapper.selectById(id);
    }

    @Override
    public PageResult<SwmMonitorDeviceInfoDO> getMonitorDeviceInfoPage(SwmMonitorDeviceInfoPageReqVO pageReqVO) {
        return swmMonitorDeviceInfoMapper.selectPage(pageReqVO,
                new cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX<SwmMonitorDeviceInfoDO>()
                        .eqIfPresent(SwmMonitorDeviceInfoDO::getRecType, pageReqVO.getRecType())
                        .eqIfPresent(SwmMonitorDeviceInfoDO::getParentId, pageReqVO.getParentId())
                        .likeIfPresent(SwmMonitorDeviceInfoDO::getName, pageReqVO.getName())
                        .likeIfPresent(SwmMonitorDeviceInfoDO::getCode, pageReqVO.getCode())
                        .eqIfPresent(SwmMonitorDeviceInfoDO::getDeviceType, pageReqVO.getDeviceType())
                        .likeIfPresent(SwmMonitorDeviceInfoDO::getIp, pageReqVO.getIp())
                        .orderByDesc(SwmMonitorDeviceInfoDO::getCreateTime));
    }

    private void validateMonitorDeviceInfoExists(String id) {
        if (swmMonitorDeviceInfoMapper.selectById(id) == null) {
            throw exception(PERSON_NOT_EXISTS);
        }
    }

}
