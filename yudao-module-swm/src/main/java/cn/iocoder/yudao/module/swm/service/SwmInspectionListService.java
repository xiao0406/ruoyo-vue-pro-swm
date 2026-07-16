package cn.iocoder.yudao.module.swm.service;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.swm.controller.admin.inspectionlist.vo.*;
import cn.iocoder.yudao.module.swm.dal.dataobject.SwmInspectionListDO;
import jakarta.validation.Valid;

public interface SwmInspectionListService {
    String createSwmInspectionList(@Valid SwmInspectionListSaveReqVO createReqVO);
    void updateSwmInspectionList(@Valid SwmInspectionListSaveReqVO updateReqVO);
    void deleteSwmInspectionList(String id);
    SwmInspectionListDO getSwmInspectionList(String id);
    PageResult<SwmInspectionListDO> getSwmInspectionListPage(SwmInspectionListPageReqVO pageReqVO);

    /**
     * 直接保存巡检单（用于定时任务内部创建）
     */
    void createSwmInspectionList(SwmInspectionListDO inspectionList);

    /**
     * 检查指定计划在指定日期是否已有巡检任务
     */
    boolean existsByPlanIdAndDate(String planId, String dateStr);

    /**
     * 获取指定计划的最后一次巡检任务（按创建时间倒序）
     */
    SwmInspectionListDO getLastTaskByPlanId(String planId);

    SwmInspectionListDO startTask(String id);

    SwmInspectionListDO completeTask(SwmInspectionListSaveReqVO reqVO);
}
