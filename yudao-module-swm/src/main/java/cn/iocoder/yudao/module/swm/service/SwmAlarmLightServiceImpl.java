package cn.iocoder.yudao.module.swm.service;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.swm.controller.admin.alarm_light.vo.SwmAlarmLightPageReqVO;
import cn.iocoder.yudao.module.swm.controller.admin.alarm_light.vo.SwmAlarmLightSaveReqVO;
import cn.iocoder.yudao.module.swm.dal.dataobject.SwmAlarmLightDO;
import cn.iocoder.yudao.module.swm.dal.mysql.SwmAlarmLightMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.swm.enums.ErrorCodeConstants.ALARM_LIGHT_NOT_EXISTS;

/**
 * 报警灯 Service 实现类
 */
@Service
@Validated
@Slf4j
public class SwmAlarmLightServiceImpl implements SwmAlarmLightService {

    @Resource
    private SwmAlarmLightMapper swmAlarmLightMapper;

    @Override
    public String createAlarmLight(SwmAlarmLightSaveReqVO createReqVO) {
        // 插入报警灯
        SwmAlarmLightDO alarmLight = BeanUtils.toBean(createReqVO, SwmAlarmLightDO.class);
        swmAlarmLightMapper.insert(alarmLight);
        return alarmLight.getId();
    }

    @Override
    public void updateAlarmLight(SwmAlarmLightSaveReqVO updateReqVO) {
        // 校验存在
        validateAlarmLightExists(updateReqVO.getId());

        // 更新报警灯
        SwmAlarmLightDO updateObj = BeanUtils.toBean(updateReqVO, SwmAlarmLightDO.class);
        swmAlarmLightMapper.updateById(updateObj);
    }

    @Override
    public void deleteAlarmLight(String id) {
        // 校验存在
        validateAlarmLightExists(id);

        // 删除报警灯
        swmAlarmLightMapper.deleteById(id);
    }

    @Override
    public SwmAlarmLightDO getAlarmLight(String id) {
        return swmAlarmLightMapper.selectById(id);
    }

    @Override
    public PageResult<SwmAlarmLightDO> getAlarmLightPage(SwmAlarmLightPageReqVO pageReqVO) {
        return swmAlarmLightMapper.selectPage(pageReqVO, new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>());
    }

    private void validateAlarmLightExists(String id) {
        if (swmAlarmLightMapper.selectById(id) == null) {
            throw exception(ALARM_LIGHT_NOT_EXISTS);
        }
    }

}
