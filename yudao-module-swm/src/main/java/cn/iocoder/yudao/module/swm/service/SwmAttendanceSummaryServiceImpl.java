package cn.iocoder.yudao.module.swm.service;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.swm.controller.admin.attendanceSummary.vo.SwmAttendanceSummaryPageReqVO;
import cn.iocoder.yudao.module.swm.controller.admin.attendanceSummary.vo.SwmAttendanceSummarySaveReqVO;
import cn.iocoder.yudao.module.swm.dal.dataobject.SwmAttendanceSummaryDO;
import cn.iocoder.yudao.module.swm.dal.mysql.SwmAttendanceSummaryMapper;
import jakarta.annotation.Resource;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.swm.enums.ErrorCodeConstants.ATTENDANCE_NOT_EXISTS;

/**
 * 考勤汇总 Service 实现类
 */
@Service
@Validated
@Slf4j
public class SwmAttendanceSummaryServiceImpl implements SwmAttendanceSummaryService {

    @Resource
    private SwmAttendanceSummaryMapper swmAttendanceSummaryMapper;

    @Override
    public String createAttendanceSummary(SwmAttendanceSummarySaveReqVO createReqVO) {
        // 插入考勤汇总
        SwmAttendanceSummaryDO attendanceSummary = BeanUtils.toBean(createReqVO, SwmAttendanceSummaryDO.class);
        swmAttendanceSummaryMapper.insert(attendanceSummary);
        return attendanceSummary.getId();
    }

    @Override
    public void updateAttendanceSummary(SwmAttendanceSummarySaveReqVO updateReqVO) {
        // 校验存在
        validateAttendanceSummaryExists(updateReqVO.getId());

        // 更新考勤汇总
        SwmAttendanceSummaryDO updateObj = BeanUtils.toBean(updateReqVO, SwmAttendanceSummaryDO.class);
        swmAttendanceSummaryMapper.updateById(updateObj);
    }

    @Override
    public void deleteAttendanceSummary(String id) {
        // 校验存在
        validateAttendanceSummaryExists(id);

        // 删除考勤汇总
        swmAttendanceSummaryMapper.deleteById(id);
    }

    @Override
    public SwmAttendanceSummaryDO getAttendanceSummary(String id) {
        return swmAttendanceSummaryMapper.selectById(id);
    }

    @Override
    public PageResult<SwmAttendanceSummaryDO> getAttendanceSummaryPage(SwmAttendanceSummaryPageReqVO pageReqVO) {
        return swmAttendanceSummaryMapper.selectPage(pageReqVO, new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>());
    }

    @Override
    public SwmAttendanceSummaryDO findByEmployeeIdAndMonth(String employeeId, String month) {
        return swmAttendanceSummaryMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SwmAttendanceSummaryDO>()
                        .eq(SwmAttendanceSummaryDO::getEmployeeId, employeeId)
                        .eq(SwmAttendanceSummaryDO::getMonth, month)
                        .last("LIMIT 1"));
    }

    @Override
    public List<SwmAttendanceSummaryDO> findByIdentityCardAndMonth(String identityCard, String month) {
        return swmAttendanceSummaryMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SwmAttendanceSummaryDO>()
                        .eq(SwmAttendanceSummaryDO::getIdentityCard, identityCard)
                        .eq(SwmAttendanceSummaryDO::getMonth, month));
    }

    @Override
    public void save(SwmAttendanceSummaryDO summary) {
        swmAttendanceSummaryMapper.insert(summary);
    }

    @Override
    public void update(SwmAttendanceSummaryDO summary) {
        swmAttendanceSummaryMapper.updateById(summary);
    }

    private void validateAttendanceSummaryExists(String id) {
        if (swmAttendanceSummaryMapper.selectById(id) == null) {
            throw exception(ATTENDANCE_NOT_EXISTS);
        }
    }

}
