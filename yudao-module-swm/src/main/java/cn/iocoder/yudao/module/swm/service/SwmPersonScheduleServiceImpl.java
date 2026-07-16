package cn.iocoder.yudao.module.swm.service;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.swm.controller.admin.personSchedule.vo.SwmPersonSchedulePageReqVO;
import cn.iocoder.yudao.module.swm.controller.admin.personSchedule.vo.SwmPersonScheduleSaveReqVO;
import cn.iocoder.yudao.module.swm.dal.dataobject.SwmPersonDO;
import cn.iocoder.yudao.module.swm.dal.dataobject.SwmPersonScheduleDO;
import cn.iocoder.yudao.module.swm.dal.mysql.SwmOrganizationTreeMapper;
import cn.iocoder.yudao.module.swm.dal.mysql.SwmPersonMapper;
import cn.iocoder.yudao.module.swm.dal.mysql.SwmPersonScheduleMapper;
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
import static cn.iocoder.yudao.module.swm.enums.ErrorCodeConstants.PERSON_SCHEDULE_NOT_EXISTS;

/**
 * 人员排班 Service 实现类
 */
@Service
@Validated
@Slf4j
public class SwmPersonScheduleServiceImpl implements SwmPersonScheduleService {

    @Resource
    private SwmPersonScheduleMapper swmPersonScheduleMapper;
    @Resource
    private SwmPersonMapper swmPersonMapper;
    @Resource
    private SwmOrganizationTreeMapper organizationTreeMapper;

    @Override
    public String createPersonSchedule(SwmPersonScheduleSaveReqVO createReqVO) {
        // 插入人员排班
        SwmPersonScheduleDO personSchedule = BeanUtils.toBean(createReqVO, SwmPersonScheduleDO.class);
        swmPersonScheduleMapper.insert(personSchedule);
        return personSchedule.getId();
    }

    @Override
    public void updatePersonSchedule(SwmPersonScheduleSaveReqVO updateReqVO) {
        // 校验存在
        validatePersonScheduleExists(updateReqVO.getId());

        // 更新人员排班
        SwmPersonScheduleDO updateObj = BeanUtils.toBean(updateReqVO, SwmPersonScheduleDO.class);
        swmPersonScheduleMapper.updateById(updateObj);
    }

    @Override
    public void deletePersonSchedule(String id) {
        // 校验存在
        validatePersonScheduleExists(id);

        // 删除人员排班
        swmPersonScheduleMapper.deleteById(id);
    }

    @Override
    public SwmPersonScheduleDO getPersonSchedule(String id) {
        return swmPersonScheduleMapper.selectById(id);
    }

    @Override
    public PageResult<SwmPersonScheduleDO> getPersonSchedulePage(SwmPersonSchedulePageReqVO pageReqVO) {
        PageResult<SwmPersonScheduleDO> pageResult = swmPersonScheduleMapper.selectPage(pageReqVO,
                new LambdaQueryWrapperX<SwmPersonScheduleDO>()
                        .likeIfPresent(SwmPersonScheduleDO::getPersonName, pageReqVO.getPersonName())
                        .eqIfPresent(SwmPersonScheduleDO::getMonth, pageReqVO.getMonth())
                        .eqIfPresent(SwmPersonScheduleDO::getClasses, pageReqVO.getClasses())
                        .likeIfPresent(SwmPersonScheduleDO::getIdCard, pageReqVO.getIdCard())
                        .eqIfPresent(SwmPersonScheduleDO::getEmployeeId, pageReqVO.getEmployeeId())
                        .orderByDesc(SwmPersonScheduleDO::getCreateTime));
        fillPersonAndOrganizationInfo(pageResult.getList());
        return pageResult;
    }

    private void fillPersonAndOrganizationInfo(List<SwmPersonScheduleDO> list) {
        if (list == null || list.isEmpty()) {
            return;
        }
        List<String> idCards = list.stream()
                .map(SwmPersonScheduleDO::getIdCard)
                .filter(value -> value != null && !value.isBlank())
                .distinct()
                .collect(Collectors.toList());
        if (idCards.isEmpty()) {
            return;
        }
        Map<String, SwmPersonDO> personMap = swmPersonMapper.selectList(
                        new LambdaQueryWrapper<SwmPersonDO>().in(SwmPersonDO::getIdentityCard, idCards))
                .stream()
                .collect(Collectors.toMap(SwmPersonDO::getIdentityCard, Function.identity(), (first, second) -> first));
        list.forEach(item -> {
            SwmPersonDO person = personMap.get(item.getIdCard());
            if (person == null) {
                return;
            }
            item.setOrganization(person.getCompany());
            item.setWorkshop(person.getDepartment());
            item.setProcess(person.getProdLine());
            item.setWorkGroupName(person.getTeam());
            item.setPersonId(person.getId());
            item.setPersonType(person.getPersonType());
            item.setDeviceId(person.getSafetyHelmetId());
        });
        fillOrganizationNames(list);
    }

    private void fillOrganizationNames(List<SwmPersonScheduleDO> list) {
        List<String> ids = list.stream()
                .flatMap(item -> java.util.stream.Stream.of(item.getOrganization(), item.getWorkshop(), item.getProcess(), item.getWorkGroupName()))
                .filter(value -> value != null && !value.isBlank())
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
        list.forEach(item -> {
            item.setOrganization(nameMap.getOrDefault(item.getOrganization(), item.getOrganization()));
            item.setWorkshop(nameMap.getOrDefault(item.getWorkshop(), item.getWorkshop()));
            item.setProcess(nameMap.getOrDefault(item.getProcess(), item.getProcess()));
            item.setWorkGroupName(nameMap.getOrDefault(item.getWorkGroupName(), item.getWorkGroupName()));
        });
    }

    @Override
    public List<SwmPersonScheduleDO> findList(SwmPersonScheduleDO query) {
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SwmPersonScheduleDO> wrapper =
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        if (query.getIdCard() != null) {
            wrapper.eq(SwmPersonScheduleDO::getIdCard, query.getIdCard());
        }
        if (query.getMonth() != null) {
            wrapper.eq(SwmPersonScheduleDO::getMonth, query.getMonth());
        }
        if (query.getClasses() != null) {
            wrapper.eq(SwmPersonScheduleDO::getClasses, query.getClasses());
        }
        return swmPersonScheduleMapper.selectList(wrapper);
    }

    @Override
    public List<SwmPersonScheduleDO> findListByTenantId(SwmPersonScheduleDO query) {
        return findList(query);
    }

    @Override
    public List<SwmPersonScheduleDO> findByIdCardAndMonth(String idCard, String month) {
        SwmPersonScheduleDO query = new SwmPersonScheduleDO();
        query.setIdCard(idCard);
        query.setMonth(month);
        return findList(query);
    }

    @Override
    public void insertBatch(List<SwmPersonScheduleDO> list) {
        swmPersonScheduleMapper.insertBatch(list);
    }

    private void validatePersonScheduleExists(String id) {
        if (swmPersonScheduleMapper.selectById(id) == null) {
            throw exception(PERSON_SCHEDULE_NOT_EXISTS);
        }
    }

}
