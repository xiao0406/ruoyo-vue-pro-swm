package cn.iocoder.yudao.module.swm.service;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.swm.controller.admin.personSchedule.vo.SwmPersonSchedulePageReqVO;
import cn.iocoder.yudao.module.swm.controller.admin.personSchedule.vo.SwmPersonScheduleSaveReqVO;
import cn.iocoder.yudao.module.swm.dal.dataobject.SwmPersonScheduleDO;
import cn.iocoder.yudao.module.swm.dal.mysql.SwmPersonScheduleMapper;
import jakarta.annotation.Resource;
import java.util.List;
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
        return swmPersonScheduleMapper.selectPage(pageReqVO, new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>());
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
    public List<SwmPersonScheduleDO> findListByCorpCode(SwmPersonScheduleDO query) {
        // In yudao, corpCode is replaced by tenantId - just return all matching records for current tenant
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
