package com.jeesite.modules.swm.service;

import com.jeesite.common.entity.Page;
import com.jeesite.common.service.CrudService;
import com.jeesite.modules.swm.dao.SwmScheduleTimeDao;
import com.jeesite.modules.swm.entity.SwmScheduleTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 排班时间管理Service
 * 
 * @author zwf
 * @version 2025-05-15
 */
@Service
@Transactional(readOnly = true)
public class SwmScheduleTimeService extends CrudService<SwmScheduleTimeDao, SwmScheduleTime> {

    /**
     * 获取单条数据
     * 
     * @param swmScheduleTime
     * @return
     */
    @Override
    public SwmScheduleTime get(SwmScheduleTime swmScheduleTime) {
        return super.get(swmScheduleTime);
    }

    /**
     * 查询分页数据
     * 
     * @param swmScheduleTime
     * @return
     */
    public Page<SwmScheduleTime> findPage(SwmScheduleTime swmScheduleTime) {
        return super.findPage(swmScheduleTime);
    }

    /**
     * 查询分页数据（带页面参数）
     * 
     * @param page            分页对象
     * @param swmScheduleTime
     * @return
     */
    public Page<SwmScheduleTime> findPage(Page<SwmScheduleTime> page, SwmScheduleTime swmScheduleTime) {
        // 设置分页参数
        swmScheduleTime.setPage(page);
        // 执行查询
        return this.findPage(swmScheduleTime);
    }

    /**
     * 查询所有数据
     * 
     * @param swmScheduleTime
     * @return
     */
    public List<SwmScheduleTime> findList(SwmScheduleTime swmScheduleTime) {
        return super.findList(swmScheduleTime);
    }

    /**
     * 保存数据（插入或更新）
     * 
     * @param swmScheduleTime
     */
    @Override
    @Transactional(readOnly = false)
    public void save(SwmScheduleTime swmScheduleTime) {
        super.save(swmScheduleTime);
    }

    /**
     * 保存所有数据
     * 
     * @param scheduleTimes
     */
    @Transactional(readOnly = false)
    public void saveAll(List<SwmScheduleTime> scheduleTimes) {
        for (SwmScheduleTime swmScheduleTime : scheduleTimes) {
            // 只保存有时间设置的项
            if (swmScheduleTime.getStartTime() != null && !swmScheduleTime.getStartTime().isEmpty() &&
                    swmScheduleTime.getEndTime() != null && !swmScheduleTime.getEndTime().isEmpty()) {
                super.save(swmScheduleTime);
            }
        }
    }

    /**
     * 删除数据
     * 
     * @param swmScheduleTime
     */
    @Override
    @Transactional(readOnly = false)
    public void delete(SwmScheduleTime swmScheduleTime) {
        super.delete(swmScheduleTime);
    }

    public List<SwmScheduleTime> findListSingle(SwmScheduleTime scheduleTimeQuery) {
        return dao.findListSingle(scheduleTimeQuery);
    }
}