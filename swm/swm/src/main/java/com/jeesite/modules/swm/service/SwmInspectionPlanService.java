package com.jeesite.modules.swm.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;

import com.jeesite.common.entity.Page;
import com.jeesite.common.lang.StringUtils;
import com.jeesite.common.service.CrudService;
import com.jeesite.modules.swm.entity.SwmInspectionPlan;
import com.jeesite.modules.swm.dao.SwmInspectionPlanDao;
import com.jeesite.modules.sys.utils.DictUtils;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 巡检计划Service
 * 
 * @author Shawn
 * @version 2025-05-21
 */
@Service
@Transactional(readOnly = true)
public class SwmInspectionPlanService extends CrudService<SwmInspectionPlanDao, SwmInspectionPlan> {

    @Autowired
    private SwmInspectionPlanDao swmInspectionPlanDao;

    /**
     * 获取单条数据
     * 
     * @param swmInspectionPlan
     * @return
     */
    @Override
    public SwmInspectionPlan get(SwmInspectionPlan swmInspectionPlan) {
        SwmInspectionPlan entity = super.get(swmInspectionPlan);
        if (entity != null) {
            // 设置字典文本
            if (StringUtils.isNotBlank(entity.getInspectionType())) {
                entity.setInspectionTypeText(DictUtils.getDictLabel(entity.getInspectionType(), "inspection_type", ""));
            }
        }
        return entity;
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
        Page<SwmInspectionPlan> page = super.findPage(swmInspectionPlan);
        // 设置字典文本
        for (SwmInspectionPlan entity : page.getList()) {
            if (StringUtils.isNotBlank(entity.getInspectionType())) {
                entity.setInspectionTypeText(DictUtils.getDictLabel(entity.getInspectionType(), "inspection_type", ""));
            }
        }
        return page;
    }

    /**
     * 保存数据（插入或更新）
     * 
     * @param swmInspectionPlan
     */
    @Override
    @Transactional(readOnly = false)
    public void save(SwmInspectionPlan swmInspectionPlan) {
        // 新增时自动生成巡检计划编号
        if (swmInspectionPlan.getIsNewRecord()) {
            swmInspectionPlan.setPlanCode(generatePlanCode());
        }
        super.save(swmInspectionPlan);
    }

    /**
     * 生成巡检计划编号
     * 格式：ZJGG-XJJH-202507070001
     * 
     * @return 巡检计划编号
     */
    private String generatePlanCode() {
        // 获取当前日期
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
        String dateStr = dateFormat.format(new Date());
        
        // 查询当日已有的最大序号
        String prefix = "ZJGG-XJJH-" + dateStr;
        String maxCode = swmInspectionPlanDao.getMaxPlanCodeByPrefix(prefix);
        
        int nextSeq = 1;
        if (StringUtils.isNotBlank(maxCode)) {
            // 提取序号部分（最后4位）
            String seqStr = maxCode.substring(maxCode.length() - 4);
            try {
                nextSeq = Integer.parseInt(seqStr) + 1;
            } catch (NumberFormatException e) {
                nextSeq = 1;
            }
        }
        
        // 生成完整编号
        return String.format("%s%04d", prefix, nextSeq);
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