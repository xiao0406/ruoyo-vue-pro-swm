package cn.iocoder.yudao.module.swm.service;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.swm.controller.admin.alarm.vo.SwmAlarmConfigPageReqVO;
import cn.iocoder.yudao.module.swm.controller.admin.alarm.vo.SwmAlarmConfigSaveReqVO;
import cn.iocoder.yudao.module.swm.dal.dataobject.SwmAlarmConfigDO;
import cn.iocoder.yudao.module.swm.dal.mysql.SwmAlarmConfigMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.swm.enums.ErrorCodeConstants.ALARM_CONFIG_NOT_EXISTS;

/**
 * 告警配置 Service 实现类
 */
@Service
@Validated
@Slf4j
public class SwmAlarmConfigServiceImpl implements SwmAlarmConfigService {

    @Resource
    private SwmAlarmConfigMapper swmAlarmConfigMapper;

    @Override
    public String createAlarmConfig(SwmAlarmConfigSaveReqVO createReqVO) {
        // 插入告警配置
        SwmAlarmConfigDO alarmConfig = BeanUtils.toBean(createReqVO, SwmAlarmConfigDO.class);
        swmAlarmConfigMapper.insert(alarmConfig);
        return alarmConfig.getId();
    }

    @Override
    public void updateAlarmConfig(SwmAlarmConfigSaveReqVO updateReqVO) {
        // 校验存在
        validateAlarmConfigExists(updateReqVO.getId());

        // 更新告警配置
        SwmAlarmConfigDO updateObj = BeanUtils.toBean(updateReqVO, SwmAlarmConfigDO.class);
        swmAlarmConfigMapper.updateById(updateObj);
    }

    @Override
    public void deleteAlarmConfig(String id) {
        // 校验存在
        validateAlarmConfigExists(id);

        // 删除告警配置
        swmAlarmConfigMapper.deleteById(id);
    }

    @Override
    public SwmAlarmConfigDO getAlarmConfig(String id) {
        return swmAlarmConfigMapper.selectById(id);
    }

    @Override
    public PageResult<SwmAlarmConfigDO> getAlarmConfigPage(SwmAlarmConfigPageReqVO pageReqVO) {
        return swmAlarmConfigMapper.selectPage(pageReqVO,
                new cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX<SwmAlarmConfigDO>()
                        .likeIfPresent(SwmAlarmConfigDO::getAlarmName, pageReqVO.getAlarmName())
                        .likeIfPresent(SwmAlarmConfigDO::getAlarmKey, pageReqVO.getAlarmKey())
                        .eqIfPresent(SwmAlarmConfigDO::getEnableAlarm, pageReqVO.getEnableAlarm())
                        .orderByDesc(SwmAlarmConfigDO::getCreateTime));
    }

    private void validateAlarmConfigExists(String id) {
        if (swmAlarmConfigMapper.selectById(id) == null) {
            throw exception(ALARM_CONFIG_NOT_EXISTS);
        }
    }

}
