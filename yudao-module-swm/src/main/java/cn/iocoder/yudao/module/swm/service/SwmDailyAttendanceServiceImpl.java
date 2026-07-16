package cn.iocoder.yudao.module.swm.service;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.swm.controller.admin.attendance.vo.SwmDailyAttendancePageReqVO;
import cn.iocoder.yudao.module.swm.controller.admin.attendance.vo.SwmDailyAttendanceSaveReqVO;
import cn.iocoder.yudao.module.swm.dal.dataobject.SwmDailyAttendanceDO;
import cn.iocoder.yudao.module.swm.dal.dataobject.SwmPersonDO;
import cn.iocoder.yudao.module.swm.dal.mysql.SwmDailyAttendanceMapper;
import cn.iocoder.yudao.module.swm.dal.mysql.SwmOrganizationTreeMapper;
import cn.iocoder.yudao.module.swm.dal.mysql.SwmPersonMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.swm.enums.ErrorCodeConstants.ATTENDANCE_NOT_EXISTS;

/**
 * 每日考勤 Service 实现类
 */
@Service
@Validated
@Slf4j
public class SwmDailyAttendanceServiceImpl implements SwmDailyAttendanceService {

    @Resource
    private SwmDailyAttendanceMapper swmDailyAttendanceMapper;

    @Resource
    private SwmPersonMapper swmPersonMapper;
    @Resource
    private SwmOrganizationTreeMapper organizationTreeMapper;

    @Override
    public String createDailyAttendance(SwmDailyAttendanceSaveReqVO createReqVO) {
        SwmDailyAttendanceDO attendance = BeanUtils.toBean(createReqVO, SwmDailyAttendanceDO.class);
        swmDailyAttendanceMapper.insert(attendance);
        return attendance.getId();
    }

    @Override
    public void updateDailyAttendance(SwmDailyAttendanceSaveReqVO updateReqVO) {
        validateDailyAttendanceExists(updateReqVO.getId());
        SwmDailyAttendanceDO updateObj = BeanUtils.toBean(updateReqVO, SwmDailyAttendanceDO.class);
        swmDailyAttendanceMapper.updateById(updateObj);
    }

    @Override
    public void deleteDailyAttendance(String id) {
        validateDailyAttendanceExists(id);
        swmDailyAttendanceMapper.deleteById(id);
    }

    @Override
    public SwmDailyAttendanceDO getDailyAttendance(String id) {
        return swmDailyAttendanceMapper.selectById(id);
    }

    @Override
    public PageResult<SwmDailyAttendanceDO> getDailyAttendancePage(SwmDailyAttendancePageReqVO pageReqVO) {
        PageResult<SwmDailyAttendanceDO> pageResult = swmDailyAttendanceMapper.selectPage(pageReqVO,
                new LambdaQueryWrapperX<SwmDailyAttendanceDO>()
                        .eqIfPresent(SwmDailyAttendanceDO::getEmployeeId, pageReqVO.getEmployeeId())
                        .likeIfPresent(SwmDailyAttendanceDO::getEmployeeName, pageReqVO.getEmployeeName())
                        .likeIfPresent(SwmDailyAttendanceDO::getIdentityCard, pageReqVO.getIdentityCard())
                        .likeIfPresent(SwmDailyAttendanceDO::getDeviceId, pageReqVO.getDeviceId())
                        .eqIfPresent(SwmDailyAttendanceDO::getPersonType, pageReqVO.getPersonType())
                        .betweenIfPresent(SwmDailyAttendanceDO::getAttendanceDate,
                                pageReqVO.getBeginAttendanceDate(), pageReqVO.getEndAttendanceDate())
                        .eqIfPresent(SwmDailyAttendanceDO::getClasses, pageReqVO.getClasses())
                        .eqIfPresent(SwmDailyAttendanceDO::getAttendanceNormal, pageReqVO.getAttendanceNormal())
                        .orderByDesc(SwmDailyAttendanceDO::getAttendanceDate));
        fillPersonInfo(pageResult.getList());
        return pageResult;
    }

    /**
     * 迁移后的日/周考勤页面仍展示人员组织信息，这些字段不在日考勤表中。
     * 这里按身份证回填 swm_person 的人员字段，避免前端表格组织列为空。
     */
    private void fillPersonInfo(List<SwmDailyAttendanceDO> attendanceList) {
        if (attendanceList == null || attendanceList.isEmpty()) {
            return;
        }
        List<String> idCards = attendanceList.stream()
                .map(SwmDailyAttendanceDO::getIdentityCard)
                .filter(StrUtil::isNotBlank)
                .distinct()
                .collect(Collectors.toList());
        if (idCards.isEmpty()) {
            return;
        }
        Map<String, SwmPersonDO> personMap = swmPersonMapper.selectList(
                        new LambdaQueryWrapper<SwmPersonDO>().in(SwmPersonDO::getIdentityCard, idCards))
                .stream()
                .collect(Collectors.toMap(SwmPersonDO::getIdentityCard, Function.identity(), (first, second) -> first));
        attendanceList.forEach(attendance -> {
            SwmPersonDO person = personMap.get(attendance.getIdentityCard());
            if (person == null) {
                return;
            }
            attendance.setCompany(person.getCompany());
            attendance.setDepartment(person.getDepartment());
            attendance.setProdLine(person.getProdLine());
            attendance.setTeam(person.getTeam());
            attendance.setPhoneNumber(person.getPhoneNumber());
            attendance.setJobType(person.getJobType());
            attendance.setPersonType(person.getPersonType());
            attendance.setPowerOnStatus(person.getPowerOnStatus());
        });
        fillOrganizationNames(attendanceList);
    }

    private void fillOrganizationNames(List<SwmDailyAttendanceDO> attendanceList) {
        List<String> ids = attendanceList.stream()
                .flatMap(item -> java.util.stream.Stream.of(item.getCompany(), item.getDepartment(), item.getProdLine(), item.getTeam()))
                .filter(StrUtil::isNotBlank)
                .distinct()
                .collect(Collectors.toList());
        if (ids.isEmpty()) {
            return;
        }
        Map<String, String> nameMap = organizationTreeMapper.getOrganizationNames(ids).stream()
                .filter(item -> item.get("value") != null && item.get("label") != null)
                .collect(Collectors.toMap(
                        item -> String.valueOf(item.get("value")),
                        item -> String.valueOf(item.get("label")),
                        (first, second) -> first));
        attendanceList.forEach(item -> {
            item.setCompany(nameMap.getOrDefault(item.getCompany(), item.getCompany()));
            item.setDepartment(nameMap.getOrDefault(item.getDepartment(), item.getDepartment()));
            item.setProdLine(nameMap.getOrDefault(item.getProdLine(), item.getProdLine()));
            item.setTeam(nameMap.getOrDefault(item.getTeam(), item.getTeam()));
        });
    }

    @Override
    public List<SwmDailyAttendanceDO> findList(SwmDailyAttendanceDO query) {
        LambdaQueryWrapper<SwmDailyAttendanceDO> wrapper = new LambdaQueryWrapper<>();
        if (query.getEmployeeId() != null) {
            wrapper.eq(SwmDailyAttendanceDO::getEmployeeId, query.getEmployeeId());
        }
        if (query.getIdentityCard() != null) {
            wrapper.eq(SwmDailyAttendanceDO::getIdentityCard, query.getIdentityCard());
        }
        if (query.getAttendanceDate() != null) {
            wrapper.eq(SwmDailyAttendanceDO::getAttendanceDate, query.getAttendanceDate());
        }
        if (query.getBeginAttendanceDate() != null) {
            wrapper.ge(SwmDailyAttendanceDO::getAttendanceDate, query.getBeginAttendanceDate());
        }
        if (query.getEndAttendanceDate() != null) {
            wrapper.le(SwmDailyAttendanceDO::getAttendanceDate, query.getEndAttendanceDate());
        }
        return swmDailyAttendanceMapper.selectList(wrapper);
    }

    @Override
    public void save(SwmDailyAttendanceDO attendance) {
        swmDailyAttendanceMapper.insert(attendance);
    }

    @Override
    public void update(SwmDailyAttendanceDO attendance) {
        swmDailyAttendanceMapper.updateById(attendance);
    }

    @Override
    public void saveBatch(List<SwmDailyAttendanceDO> list) {
        swmDailyAttendanceMapper.insertBatch(list);
    }

    @Override
    public void updateBatch(List<SwmDailyAttendanceDO> list) {
        swmDailyAttendanceMapper.updateBatch(list);
    }

    @Override
    public SwmDailyAttendanceDO findByEmployeeIdAndDate(String employeeId, LocalDate attendanceDate) {
        return swmDailyAttendanceMapper.selectOne(
                new LambdaQueryWrapper<SwmDailyAttendanceDO>()
                        .eq(SwmDailyAttendanceDO::getEmployeeId, employeeId)
                        .eq(SwmDailyAttendanceDO::getAttendanceDate, attendanceDate)
                        .last("LIMIT 1"));
    }

    @Override
    public SwmDailyAttendanceDO findByIdentityCardAndDate(String identityCard, LocalDate attendanceDate) {
        return swmDailyAttendanceMapper.selectOne(
                new LambdaQueryWrapper<SwmDailyAttendanceDO>()
                        .eq(SwmDailyAttendanceDO::getIdentityCard, identityCard)
                        .eq(SwmDailyAttendanceDO::getAttendanceDate, attendanceDate)
                        .last("LIMIT 1"));
    }

    @Override
    public List<SwmDailyAttendanceDO> findClockInCardList(String date, Integer random) {
        LocalDate ld = LocalDate.parse(date);
        return swmDailyAttendanceMapper.selectList(
                new LambdaQueryWrapper<SwmDailyAttendanceDO>()
                        .eq(SwmDailyAttendanceDO::getAttendanceDate, ld));
    }

    @Override
    public List<SwmDailyAttendanceDO> findClockOutCardList(String yestDay, String nowDate, Integer random) {
        LocalDate start = LocalDate.parse(yestDay);
        LocalDate end = LocalDate.parse(nowDate);
        return swmDailyAttendanceMapper.selectList(
                new LambdaQueryWrapper<SwmDailyAttendanceDO>()
                        .ge(SwmDailyAttendanceDO::getAttendanceDate, start)
                        .le(SwmDailyAttendanceDO::getAttendanceDate, end));
    }

    @Override
    public String getIdCardByEmployeeId(String employeeId) {
        SwmPersonDO person = swmPersonMapper.selectById(employeeId);
        return person != null ? person.getIdentityCard() : null;
    }

    @Override
    public double calculateIdleTimeByIdCard(String idCard, String dateStr, String workTimeRange) {
        // TODO: implement actual idle time calculation based on TDengine data
        return 0.0;
    }

    @Override
    public double calculateEffectiveWorkHoursByIdCard(String idCard, String dateStr, String workTimeRange) {
        // TODO: implement actual effective work hours calculation based on TDengine data
        return 0.0;
    }

    @Override
    public List<SwmDailyAttendanceDO> findByMonth(String month) {
        // month format: yyyy-MM
        String[] parts = month.split("-");
        int year = Integer.parseInt(parts[0]);
        int mon = Integer.parseInt(parts[1]);
        LocalDate start = LocalDate.of(year, mon, 1);
        LocalDate end = start.withDayOfMonth(start.lengthOfMonth());
        return swmDailyAttendanceMapper.selectList(
                new LambdaQueryWrapper<SwmDailyAttendanceDO>()
                        .ge(SwmDailyAttendanceDO::getAttendanceDate, start)
                        .le(SwmDailyAttendanceDO::getAttendanceDate, end));
    }

    private void validateDailyAttendanceExists(String id) {
        if (swmDailyAttendanceMapper.selectById(id) == null) {
            throw exception(ATTENDANCE_NOT_EXISTS);
        }
    }

}
