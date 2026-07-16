package cn.iocoder.yudao.module.swm.service;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.swm.controller.admin.attendanceSummary.vo.SwmAttendanceSummaryPageReqVO;
import cn.iocoder.yudao.module.swm.controller.admin.attendanceSummary.vo.SwmAttendanceSummarySaveReqVO;
import cn.iocoder.yudao.module.swm.dal.dataobject.SwmAttendanceSummaryDO;
import cn.iocoder.yudao.module.swm.dal.dataobject.SwmPersonDO;
import cn.iocoder.yudao.module.swm.dal.mysql.SwmAttendanceSummaryMapper;
import cn.iocoder.yudao.module.swm.dal.mysql.SwmPersonMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import jakarta.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
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
    @Resource
    private SwmPersonMapper swmPersonMapper;

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
        PageResult<SwmAttendanceSummaryDO> pageResult = swmAttendanceSummaryMapper.selectPage(pageReqVO,
                new LambdaQueryWrapperX<SwmAttendanceSummaryDO>()
                        .eqIfPresent(SwmAttendanceSummaryDO::getEmployeeId, pageReqVO.getEmployeeId())
                        .likeIfPresent(SwmAttendanceSummaryDO::getEmployeeName, pageReqVO.getEmployeeName())
                        .likeIfPresent(SwmAttendanceSummaryDO::getIdentityCard, pageReqVO.getIdentityCard())
                        .eqIfPresent(SwmAttendanceSummaryDO::getDepartment, pageReqVO.getDepartment())
                        .eqIfPresent(SwmAttendanceSummaryDO::getTeam, pageReqVO.getTeam())
                        .eqIfPresent(SwmAttendanceSummaryDO::getJobType, pageReqVO.getJobType())
                        .eqIfPresent(SwmAttendanceSummaryDO::getMonth, pageReqVO.getMonth())
                        .orderByDesc(SwmAttendanceSummaryDO::getMonth));
        fillPersonInfo(pageResult.getList());
        return pageResult;
    }

    /**
     * 月考勤列表展示人员手机号，汇总表没有该字段，按身份证号从人员表回填。
     */
    private void fillPersonInfo(List<SwmAttendanceSummaryDO> summaryList) {
        if (summaryList == null || summaryList.isEmpty()) {
            return;
        }
        List<String> idCards = summaryList.stream()
                .map(SwmAttendanceSummaryDO::getIdentityCard)
                .filter(idCard -> idCard != null && !idCard.isBlank())
                .distinct()
                .collect(Collectors.toList());
        if (idCards.isEmpty()) {
            return;
        }
        Map<String, SwmPersonDO> personMap = swmPersonMapper.selectList(
                        new LambdaQueryWrapper<SwmPersonDO>().in(SwmPersonDO::getIdentityCard, idCards))
                .stream()
                .collect(Collectors.toMap(SwmPersonDO::getIdentityCard, Function.identity(), (first, second) -> first));
        summaryList.forEach(summary -> {
            SwmPersonDO person = personMap.get(summary.getIdentityCard());
            if (person != null) {
                summary.setPhoneNumber(person.getPhoneNumber());
            }
        });
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
