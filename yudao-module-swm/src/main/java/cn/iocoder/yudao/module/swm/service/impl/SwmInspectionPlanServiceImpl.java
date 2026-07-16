package cn.iocoder.yudao.module.swm.service.impl;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.swm.controller.admin.inspectionplan.vo.*;
import cn.iocoder.yudao.module.swm.dal.dataobject.SwmInspectionPlanDO;
import cn.iocoder.yudao.module.swm.dal.mysql.SwmInspectionPlanMapper;
import cn.iocoder.yudao.module.swm.enums.ErrorCodeConstants;
import cn.iocoder.yudao.module.swm.service.SwmInspectionPlanService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;

@Service
@Validated
@Slf4j
public class SwmInspectionPlanServiceImpl implements SwmInspectionPlanService {
    @Resource
    private SwmInspectionPlanMapper mapper;

    @Override
    public String createSwmInspectionPlan(SwmInspectionPlanSaveReqVO reqVO) {
        SwmInspectionPlanDO plan = BeanUtils.toBean(reqVO, SwmInspectionPlanDO.class);
        mapper.insert(plan);
        return plan.getId();
    }

    @Override
    public void updateSwmInspectionPlan(SwmInspectionPlanSaveReqVO reqVO) {
        validateExists(reqVO.getId());
        mapper.updateById(BeanUtils.toBean(reqVO, SwmInspectionPlanDO.class));
    }

    @Override
    public void deleteSwmInspectionPlan(String id) {
        validateExists(id);
        mapper.deleteById(id);
    }

    @Override
    public SwmInspectionPlanDO getSwmInspectionPlan(String id) {
        return mapper.selectById(id);
    }

    @Override
    public PageResult<SwmInspectionPlanDO> getSwmInspectionPlanPage(SwmInspectionPlanPageReqVO pageReqVO) {
        return mapper.selectPage(pageReqVO,
                new cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX<SwmInspectionPlanDO>()
                        .orderByDesc(SwmInspectionPlanDO::getCreateTime));
    }

    private void validateExists(String id) {
        if (mapper.selectById(id) == null) throw exception(ErrorCodeConstants.INSPECTION_PLAN_NOT_EXISTS);
    }

    @Override
    public List<SwmInspectionPlanDO> findList(SwmInspectionPlanDO queryPlan) {
        LambdaQueryWrapper<SwmInspectionPlanDO> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.isNotBlank(queryPlan.getPlanStatus())) {
            wrapper.eq(SwmInspectionPlanDO::getPlanStatus, queryPlan.getPlanStatus());
        }
        return mapper.selectList(wrapper);
    }

    @Override
    public boolean updateStatus(String id, String status) {
        validateExists(id);
        SwmInspectionPlanDO update = new SwmInspectionPlanDO();
        update.setId(id);
        update.setPlanStatus(status);
        return mapper.updateById(update) > 0;
    }
}
