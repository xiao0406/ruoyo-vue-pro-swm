package cn.iocoder.yudao.module.swm.service;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.swm.controller.admin.alarm_detail.vo.SwmAlarmConfigDetailPageReqVO;
import cn.iocoder.yudao.module.swm.controller.admin.alarm_detail.vo.SwmAlarmConfigDetailSaveReqVO;
import cn.iocoder.yudao.module.swm.dal.dataobject.SwmAlarmConfigDetailDO;
import cn.iocoder.yudao.module.swm.dal.mysql.SwmAlarmConfigDetailMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.swm.enums.ErrorCodeConstants.ALARM_CONFIG_NOT_EXISTS;

@Service
@Validated
@Slf4j
public class SwmAlarmConfigDetailServiceImpl implements SwmAlarmConfigDetailService {

    @Resource
    private SwmAlarmConfigDetailMapper swmAlarmConfigDetailMapper;

    @Override
    public String createAlarmConfigDetail(SwmAlarmConfigDetailSaveReqVO createReqVO) {
        SwmAlarmConfigDetailDO alarmConfigDetail = BeanUtils.toBean(createReqVO, SwmAlarmConfigDetailDO.class);
        swmAlarmConfigDetailMapper.insert(alarmConfigDetail);
        return alarmConfigDetail.getId();
    }

    @Override
    public void updateAlarmConfigDetail(SwmAlarmConfigDetailSaveReqVO updateReqVO) {
        validateAlarmConfigDetailExists(updateReqVO.getId());
        SwmAlarmConfigDetailDO alarmConfigDetail = BeanUtils.toBean(updateReqVO, SwmAlarmConfigDetailDO.class);
        swmAlarmConfigDetailMapper.updateById(alarmConfigDetail);
    }

    @Override
    public void deleteAlarmConfigDetail(String id) {
        validateAlarmConfigDetailExists(id);
        swmAlarmConfigDetailMapper.deleteById(id);
    }

    @Override
    public SwmAlarmConfigDetailDO getAlarmConfigDetail(String id) {
        return swmAlarmConfigDetailMapper.selectById(id);
    }

    @Override
    public PageResult<SwmAlarmConfigDetailDO> getAlarmConfigDetailPage(SwmAlarmConfigDetailPageReqVO pageReqVO) {
        return swmAlarmConfigDetailMapper.selectPage(pageReqVO,
                new cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX<SwmAlarmConfigDetailDO>()
                        .eqIfPresent(SwmAlarmConfigDetailDO::getMainId, pageReqVO.getMainId())
                        .orderByDesc(SwmAlarmConfigDetailDO::getCreateTime));
    }

    private void validateAlarmConfigDetailExists(String id) {
        if (swmAlarmConfigDetailMapper.selectById(id) == null) {
            throw exception(ALARM_CONFIG_NOT_EXISTS);
        }
    }

}
