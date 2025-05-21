package com.jeesite.modules.swm.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jeesite.common.entity.Page;
import com.jeesite.common.service.CrudService;
import com.jeesite.modules.swm.entity.SwmInspectionPlan;
import com.jeesite.modules.swm.dao.SwmInspectionPlanDao;

/**
 * 巡检计划Service
 * 
 * @author Shawn
 * @version 2025-05-21
 */
@Service
@Transactional(readOnly = true)
public class SwmInspectionPlanService extends CrudService<SwmInspectionPlanDao, SwmInspectionPlan> {

    /**
     * 获取单条数据
     * 
     * @param swmInspectionPlan
     * @return
     */
    @Override
    public SwmInspectionPlan get(SwmInspectionPlan swmInspectionPlan) {
        return super.get(swmInspectionPlan);
    }

    /**
     * 查询分页数据
     * 
     * @param swmInspectionPlan      查询条件
     * @param swmInspectionPlan.page 分页对象
     * @return
     */
    @Override
    public Page<SwmInspectionPlan> findPage(SwmInspectionPlan swmInspectionPlan) {
        return super.findPage(swmInspectionPlan);
    }

    /**
     * 保存数据（插入或更新）
     * 
     * @param swmInspectionPlan
     */
    @Override
    @Transactional(readOnly = false)
    public void save(SwmInspectionPlan swmInspectionPlan) {
        super.save(swmInspectionPlan);
    }

    /**
     * 更新状态
     * 
     * @param swmInspectionPlan
     */
    @Override
    @Transactional(readOnly = false)
    public void updateStatus(SwmInspectionPlan swmInspectionPlan) {
        super.updateStatus(swmInspectionPlan);
    }

    /**
     * 删除数据
     * 
     * @param swmInspectionPlan
     */
    @Override
    @Transactional(readOnly = false)
    public void delete(SwmInspectionPlan swmInspectionPlan) {
        super.delete(swmInspectionPlan);
    }

}