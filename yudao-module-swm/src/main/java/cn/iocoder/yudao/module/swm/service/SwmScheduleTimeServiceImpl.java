package cn.iocoder.yudao.module.swm.service;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.swm.controller.admin.scheduleTime.vo.SwmScheduleTimePageReqVO;
import cn.iocoder.yudao.module.swm.controller.admin.scheduleTime.vo.SwmScheduleTimeSaveReqVO;
import cn.iocoder.yudao.module.swm.dal.dataobject.SwmScheduleTimeDO;
import cn.iocoder.yudao.module.swm.dal.mysql.SwmScheduleTimeMapper;
import jakarta.annotation.Resource;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.swm.enums.ErrorCodeConstants.PERSON_SCHEDULE_NOT_EXISTS;

/**
 * 排班时间 Service 实现类
 */
@Service
@Validated
@Slf4j
public class SwmScheduleTimeServiceImpl implements SwmScheduleTimeService {

    @Resource
    private SwmScheduleTimeMapper swmScheduleTimeMapper;

    @Override
    public String createScheduleTime(SwmScheduleTimeSaveReqVO createReqVO) {
        // 插入排班时间
        SwmScheduleTimeDO scheduleTime = BeanUtils.toBean(createReqVO, SwmScheduleTimeDO.class);
        swmScheduleTimeMapper.insert(scheduleTime);
        return scheduleTime.getId();
    }

    @Override
    public void updateScheduleTime(SwmScheduleTimeSaveReqVO updateReqVO) {
        // 校验存在
        validateScheduleTimeExists(updateReqVO.getId());

        // 更新排班时间
        SwmScheduleTimeDO updateObj = BeanUtils.toBean(updateReqVO, SwmScheduleTimeDO.class);
        swmScheduleTimeMapper.updateById(updateObj);
    }

    @Override
    public void deleteScheduleTime(String id) {
        // 校验存在
        validateScheduleTimeExists(id);

        // 删除排班时间
        swmScheduleTimeMapper.deleteById(id);
    }

    @Override
    public SwmScheduleTimeDO getScheduleTime(String id) {
        return swmScheduleTimeMapper.selectById(id);
    }

    @Override
    public PageResult<SwmScheduleTimeDO> getScheduleTimePage(SwmScheduleTimePageReqVO pageReqVO) {
        return swmScheduleTimeMapper.selectPage(pageReqVO, new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>());
    }

    @Override
    public List<SwmScheduleTimeDO> findList(SwmScheduleTimeDO query) {
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SwmScheduleTimeDO> wrapper =
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        if (query.getShiftType() != null) {
            wrapper.eq(SwmScheduleTimeDO::getShiftType, query.getShiftType());
        }
        return swmScheduleTimeMapper.selectList(wrapper);
    }

    @Override
    public List<SwmScheduleTimeDO> findListSingle(SwmScheduleTimeDO query) {
        return findList(query);
    }

    private void validateScheduleTimeExists(String id) {
        if (swmScheduleTimeMapper.selectById(id) == null) {
            throw exception(PERSON_SCHEDULE_NOT_EXISTS);
        }
    }

}
