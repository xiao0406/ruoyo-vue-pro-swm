package cn.iocoder.yudao.module.swm.service;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.swm.controller.admin.person.vo.SwmPersonPageReqVO;
import cn.iocoder.yudao.module.swm.controller.admin.person.vo.SwmPersonSaveReqVO;
import cn.iocoder.yudao.module.swm.dal.dataobject.SwmPersonDO;
import cn.iocoder.yudao.module.swm.dal.mysql.SwmPersonMapper;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import jakarta.annotation.Resource;
import java.util.Collections;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.swm.enums.ErrorCodeConstants.PERSON_NOT_EXISTS;

/**
 * 人员管理 Service 实现类
 */
@Service
@Validated
@Slf4j
public class SwmPersonServiceImpl implements SwmPersonService {

    @Resource
    private SwmPersonMapper swmPersonMapper;

    @Override
    public String createPerson(SwmPersonSaveReqVO createReqVO) {
        // 插入人员
        SwmPersonDO person = BeanUtils.toBean(createReqVO, SwmPersonDO.class);
        fillCreateDefaults(person);
        swmPersonMapper.insert(person);
        return person.getId();
    }

    @Override
    public void updatePerson(SwmPersonSaveReqVO updateReqVO) {
        // 校验存在
        validatePersonExists(updateReqVO.getId());

        // 更新人员
        SwmPersonDO updateObj = BeanUtils.toBean(updateReqVO, SwmPersonDO.class);
        swmPersonMapper.updateById(updateObj);
    }

    @Override
    public void deletePerson(String id) {
        // 校验存在
        validatePersonExists(id);

        // 删除人员
        swmPersonMapper.deleteById(id);
    }

    @Override
    public SwmPersonDO getPerson(String id) {
        return swmPersonMapper.selectById(id);
    }

    @Override
    public PageResult<SwmPersonDO> getPersonPage(SwmPersonPageReqVO pageReqVO) {
        return swmPersonMapper.selectPage(pageReqVO,
                new LambdaQueryWrapperX<SwmPersonDO>()
                        .likeIfPresent(SwmPersonDO::getName, pageReqVO.getName())
                        .likeIfPresent(SwmPersonDO::getPersonNumber, pageReqVO.getPersonNumber())
                        .eqIfPresent(SwmPersonDO::getPersonType, pageReqVO.getPersonType())
                        .eqIfPresent(SwmPersonDO::getCompany, pageReqVO.getCompany())
                        .eqIfPresent(SwmPersonDO::getDepartment, pageReqVO.getDepartment())
                        .eqIfPresent(SwmPersonDO::getProdLine, pageReqVO.getProdLine())
                        .eqIfPresent(SwmPersonDO::getTeam, pageReqVO.getTeam())
                        .eqIfPresent(SwmPersonDO::getJobType, pageReqVO.getJobType())
                        .eqIfPresent(SwmPersonDO::getPersonnelStatus, pageReqVO.getPersonnelStatus())
                        .likeIfPresent(SwmPersonDO::getIdentityCard, pageReqVO.getIdentityCard())
                        .likeIfPresent(SwmPersonDO::getPhoneNumber, pageReqVO.getPhoneNumber())
                        .eqIfPresent(SwmPersonDO::getSafetyEducation, pageReqVO.getSafetyEducation())
                        .eqIfPresent(SwmPersonDO::getIsExternalPersonnel, pageReqVO.getIsExternalPersonnel())
                        .likeIfPresent(SwmPersonDO::getSafetyHelmetId, pageReqVO.getSafetyHelmetId())
                        .orderByDesc(SwmPersonDO::getCreateTime));
    }

    @Override
    public List<SwmPersonDO> findList(SwmPersonDO query) {
        // Build lambda query using available fields
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SwmPersonDO> wrapper =
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        if (query.getPersonnelStatus() != null) {
            wrapper.eq(SwmPersonDO::getPersonnelStatus, query.getPersonnelStatus());
        }
        if (query.getStatus() != null) {
            wrapper.eq(SwmPersonDO::getStatus, query.getStatus());
        }
        if (query.getIdentityCard() != null) {
            wrapper.eq(SwmPersonDO::getIdentityCard, query.getIdentityCard());
        }
        if (query.getCompany() != null) {
            wrapper.eq(SwmPersonDO::getCompany, query.getCompany());
        }
        return swmPersonMapper.selectList(wrapper);
    }

    @Override
    public SwmPersonDO getByIdentityCard(String identityCard) {
        return swmPersonMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SwmPersonDO>()
                        .eq(SwmPersonDO::getIdentityCard, identityCard)
                        .last("LIMIT 1"));
    }

    @Override
    public boolean clearSafetyHelmet(String id) {
        if (id == null || id.isBlank()) {
            return false;
        }
        return swmPersonMapper.update(null, new LambdaUpdateWrapper<SwmPersonDO>()
                .eq(SwmPersonDO::getId, id)
                .set(SwmPersonDO::getSafetyHelmetId, null)) > 0;
    }

    @Override
    public List<SwmPersonDO> findListByJobTypeList(List<String> jobTypeList) {
        if (jobTypeList == null || jobTypeList.isEmpty()) {
            return Collections.emptyList();
        }
        return swmPersonMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SwmPersonDO>()
                        .in(SwmPersonDO::getJobType, jobTypeList)
                        .eq(SwmPersonDO::getPersonnelStatus, "1") // 在职状态
                        .eq(SwmPersonDO::getStatus, "0")); // 未删除
    }

    private void validatePersonExists(String id) {
        if (swmPersonMapper.selectById(id) == null) {
            throw exception(PERSON_NOT_EXISTS);
        }
    }

    @Override
    public List<SwmPersonDO> findByDepartmentCondition(String departmentCondition) {
        if (isBlank(departmentCondition)) {
            return Collections.emptyList();
        }
        return swmPersonMapper.selectList(new LambdaQueryWrapperX<SwmPersonDO>()
                .eq(SwmPersonDO::getPersonnelStatus, "1")
                .and(wrapper -> wrapper
                        .eq(SwmPersonDO::getCompany, departmentCondition)
                        .or().eq(SwmPersonDO::getDepartment, departmentCondition)
                        .or().eq(SwmPersonDO::getProdLine, departmentCondition)
                        .or().eq(SwmPersonDO::getTeam, departmentCondition)
                        .or().eq(SwmPersonDO::getJobType, departmentCondition)
                        .or().eq(SwmPersonDO::getIdentityCard, departmentCondition))
                .orderByDesc(SwmPersonDO::getUpdateTime));
    }

    @Override
    public List<SwmPersonDO> search(String keyword, String searchType) {
        if (isBlank(keyword)) {
            return Collections.emptyList();
        }
        LambdaQueryWrapperX<SwmPersonDO> wrapper = new LambdaQueryWrapperX<SwmPersonDO>()
                .eq(SwmPersonDO::getPersonnelStatus, "1");
        if ("name".equals(searchType)) {
            wrapper.like(SwmPersonDO::getName, keyword);
        } else if ("idCard".equals(searchType)) {
            wrapper.like(SwmPersonDO::getIdentityCard, keyword);
        } else if ("phone".equals(searchType)) {
            wrapper.like(SwmPersonDO::getPhoneNumber, keyword);
        } else {
            wrapper.and(query -> query.like(SwmPersonDO::getName, keyword)
                    .or().like(SwmPersonDO::getIdentityCard, keyword)
                    .or().like(SwmPersonDO::getPhoneNumber, keyword));
        }
        return swmPersonMapper.selectList(wrapper.orderByDesc(SwmPersonDO::getUpdateTime));
    }

    @Override
    public void completeSafetyEducation(List<String> personIds) {
        if (personIds == null || personIds.isEmpty()) {
            return;
        }
        swmPersonMapper.update(null, new LambdaUpdateWrapper<SwmPersonDO>()
                .in(SwmPersonDO::getId, personIds)
                .set(SwmPersonDO::getSafetyEducation, "1"));
    }

    /**
     * JeeSite 迁移过来的页面存在部分字段不强制录入的情况，这里统一补齐 RuoYi 入库所需默认值。
     */
    private void fillCreateDefaults(SwmPersonDO person) {
        if (isBlank(person.getPersonNumber())) {
            person.setPersonNumber("P" + System.currentTimeMillis());
        }
        if (isBlank(person.getStatus())) {
            person.setStatus("0");
        }
        if (isBlank(person.getPersonnelStatus())) {
            person.setPersonnelStatus("1");
        }
        if (isBlank(person.getSafetyEducation())) {
            person.setSafetyEducation("0");
        }
        if (isBlank(person.getIsExternalPersonnel())) {
            person.setIsExternalPersonnel("1");
        }
        if (isBlank(person.getPersonType())) {
            person.setPersonType("0");
        }
        if (isBlank(person.getGender())) {
            person.setGender("男");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

}
