package cn.iocoder.yudao.module.swm.service;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.swm.controller.admin.person.vo.SwmPersonPageReqVO;
import cn.iocoder.yudao.module.swm.controller.admin.person.vo.SwmPersonSaveReqVO;
import cn.iocoder.yudao.module.swm.dal.dataobject.SwmPersonDO;
import cn.iocoder.yudao.module.swm.dal.mysql.SwmPersonMapper;
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
        return swmPersonMapper.selectPage(pageReqVO, new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>());
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

}
