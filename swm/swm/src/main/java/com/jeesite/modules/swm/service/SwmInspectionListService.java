package com.jeesite.modules.swm.service;

import com.jeesite.common.entity.Page;
import com.jeesite.common.lang.StringUtils;
import com.jeesite.common.service.CrudService;
import com.jeesite.modules.swm.dao.SwmInspectionListDao;
import com.jeesite.modules.swm.entity.SwmInspectionList;
import com.jeesite.modules.sys.utils.DictUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * 巡检列表Service
 *
 * @author Shawn
 * @version 2025-05-21
 */
@Service
@Transactional(readOnly = true)
public class SwmInspectionListService extends CrudService<SwmInspectionListDao, SwmInspectionList> {

    private static final Logger logger = LoggerFactory.getLogger(SwmInspectionListService.class);

    @Autowired
    private SwmInspectionListDao swmInspectionListDao;

    /**
     * 获取单条数据
     */
    @Override
    public SwmInspectionList get(String id) {
        SwmInspectionList entity = super.get(id);
        if (entity != null) {
            // 设置字典文本
            if (StringUtils.isNotBlank(entity.getInspectionType())) {
                entity.setInspectionTypeText(DictUtils.getDictLabel(entity.getInspectionType(), "inspection_type", ""));
            }
            if (StringUtils.isNotBlank(entity.getInspectionListStatus())) {
                entity.setInspectionListStatusText(
                        DictUtils.getDictLabel(entity.getInspectionListStatus(), "inspection_list_status_enum", ""));
            }
        }
        return entity;
    }

    /**
     * 查询分页数据
     */
    @Override
    public Page<SwmInspectionList> findPage(SwmInspectionList swmInspectionList) {
        Page<SwmInspectionList> page = super.findPage(swmInspectionList);
        // 设置字典文本
        for (SwmInspectionList entity : page.getList()) {
            if (StringUtils.isNotBlank(entity.getInspectionType())) {
                entity.setInspectionTypeText(DictUtils.getDictLabel(entity.getInspectionType(), "inspection_type", ""));
            }
            if (StringUtils.isNotBlank(entity.getInspectionListStatus())) {
                entity.setInspectionListStatusText(
                        DictUtils.getDictLabel(entity.getInspectionListStatus(), "inspection_list_status_enum", ""));
            }
        }
        return page;
    }

    /**
     * 查询分页数据（带分页参数）
     */
    public Page<SwmInspectionList> findPage(Page<SwmInspectionList> page, SwmInspectionList swmInspectionList) {
        swmInspectionList.setPage(page);
        return this.findPage(swmInspectionList);
    }

    /**
     * 保存数据
     */
    @Override
    @Transactional(readOnly = false)
    public void save(SwmInspectionList swmInspectionList) {
        super.save(swmInspectionList);
    }

    /**
     * 删除数据
     */
    @Override
    @Transactional(readOnly = false)
    public void delete(SwmInspectionList swmInspectionList) {
        super.delete(swmInspectionList);
    }

    /**
     * 获取计划对应的最后一个任务
     * @param planId
     * @return
     */
    public SwmInspectionList getLastTaskByPlanId(String planId) {
        return dao.getLastTaskByPlanId(planId);
    }

    /**
     * 获取最新的巡检记录
     * @param limit 查询条数
     * @return
     */
    public List<SwmInspectionList> latestInspectionRecord(int limit) {
       return dao.latestInspectionRecord(limit);
    }

    /**
     * 开始巡检任务
     * 将状态从"待处理"改为"进行中"
     * 
     * @param id 巡检任务ID
     * @return 更新后的巡检任务对象
     */
    @Transactional(readOnly = false)
    public SwmInspectionList startInspectionTask(String id) {
        SwmInspectionList inspectionList = this.get(id);
        if (inspectionList == null) {
            logger.warn("开始巡检任务失败：任务不存在，ID: {}", id);
            return null;
        }
        
        if (!SwmInspectionList.InspectionListStatusEnum.WAIT.equals(inspectionList.getInspectionListStatus())) {
            logger.warn("开始巡检任务失败：状态不是待处理，ID: {}, 当前状态: {}", 
                    id, inspectionList.getInspectionListStatus());
            return null;
        }
        
        inspectionList.setInspectionListStatus(SwmInspectionList.InspectionListStatusEnum.IN_PROGRESS);
        this.save(inspectionList);
        logger.info("巡检任务已成功开始，ID: {}, 计划: {}", id, inspectionList.getPlanName());
        
        return inspectionList;
    }
    
    /**
     * 完成巡检任务
     * 将状态从"进行中"改为"已完成"，并设置结束时间
     * 
     * @param inspectionList 包含更新信息的巡检任务对象
     * @return 更新后的巡检任务对象
     */
    @Transactional(readOnly = false)
    public SwmInspectionList completeInspectionTask(SwmInspectionList inspectionList) {
        if (inspectionList == null || StringUtils.isBlank(inspectionList.getId())) {
            logger.warn("完成巡检任务失败：任务ID为空");
            return null;
        }
        
        String id = inspectionList.getId();
        SwmInspectionList entity = this.get(id);
        if (entity == null) {
            logger.warn("完成巡检任务失败：任务不存在，ID: {}", id);
            return null;
        }
        
        if (!SwmInspectionList.InspectionListStatusEnum.IN_PROGRESS.equals(entity.getInspectionListStatus())) {
            logger.warn("完成巡检任务失败：状态不是进行中，ID: {}, 当前状态: {}", 
                    id, entity.getInspectionListStatus());
            return null;
        }
        
        // 更新状态和结束时间
        entity.setInspectionListStatus(SwmInspectionList.InspectionListStatusEnum.COMPLETED);
        entity.setEndTime(new Date());
        
        // 更新备注和附件
        if (StringUtils.isNotBlank(inspectionList.getRemarks())) {
            entity.setRemarks(inspectionList.getRemarks());
        }
        
        if (StringUtils.isNotBlank(inspectionList.getAttachmentPath())) {
            entity.setAttachmentPath(inspectionList.getAttachmentPath());
        }
        
        this.save(entity);
        logger.info("巡检任务已成功完成，ID: {}, 计划: {}, 结束时间: {}", 
                id, entity.getPlanName(), entity.getEndTime());
        
        return entity;
    }
    
    /**
     * 检查指定计划在指定日期是否已有任务（用于防重复生成）
     * @param planId 计划ID
     * @param dateStr 日期字符串，格式：yyyy-MM-dd
     * @return true表示已存在任务，false表示不存在
     */
    public boolean existsByPlanIdAndDate(String planId, String dateStr) {
        int count = dao.countByPlanIdAndDate(planId, dateStr);
        return count > 0;
    }
}
