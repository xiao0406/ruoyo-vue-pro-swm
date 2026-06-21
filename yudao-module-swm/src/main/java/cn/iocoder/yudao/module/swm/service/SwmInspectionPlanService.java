package cn.iocoder.yudao.module.swm.service;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.swm.controller.admin.inspectionplan.vo.*;
import cn.iocoder.yudao.module.swm.dal.dataobject.SwmInspectionPlanDO;
import jakarta.validation.Valid;

import java.util.List;

public interface SwmInspectionPlanService {
    String createSwmInspectionPlan(@Valid SwmInspectionPlanSaveReqVO createReqVO);
    void updateSwmInspectionPlan(@Valid SwmInspectionPlanSaveReqVO updateReqVO);
    void deleteSwmInspectionPlan(String id);
    SwmInspectionPlanDO getSwmInspectionPlan(String id);
    PageResult<SwmInspectionPlanDO> getSwmInspectionPlanPage(SwmInspectionPlanPageReqVO pageReqVO);

    /**
     * 根据条件查询巡检计划列表
     */
    List<SwmInspectionPlanDO> findList(SwmInspectionPlanDO queryPlan);
}
