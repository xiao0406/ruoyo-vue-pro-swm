package com.jeesite.modules.swm.service;

import com.jeesite.common.entity.Page;
import com.jeesite.common.service.CrudService;
import com.jeesite.modules.swm.dao.SwmPersonScheduleDao;
import com.jeesite.modules.swm.entity.SwmPersonSchedule;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 人员排班Service
 * 
 * @author zwf
 * @version 2025-05-15
 */
@Service
@Transactional(readOnly = true)
public class SwmPersonScheduleService extends CrudService<SwmPersonScheduleDao, SwmPersonSchedule> {

    /**
     * 获取单条数据
     * @param swmPersonSchedule
     * @return
     */
    @Override
    public SwmPersonSchedule get(SwmPersonSchedule swmPersonSchedule) {
        return super.get(swmPersonSchedule);
    }

    /**
     * 查询分页数据
     * @param swmPersonSchedule
     * @return
     */
    public Page<SwmPersonSchedule> findPage(SwmPersonSchedule swmPersonSchedule) {
        return super.findPage(swmPersonSchedule);
    }

    /**
     * 查询分页数据（带页面参数）
     * @param page 分页对象
     * @param swmPersonSchedule
     * @return
     */
    public Page<SwmPersonSchedule> findPage(Page<SwmPersonSchedule> page, SwmPersonSchedule swmPersonSchedule) {
        // 设置分页参数
        swmPersonSchedule.setPage(page);
        // 执行查询
        return this.findPage(swmPersonSchedule);
    }

    /**
     * 查询所有数据
     * @param swmPersonSchedule
     * @return
     */
    public List<SwmPersonSchedule> findList(SwmPersonSchedule swmPersonSchedule) {
        return super.findList(swmPersonSchedule);
    }

    /**
     * 保存数据（插入或更新）
     * @param swmPersonSchedule
     */
    @Override
    @Transactional(readOnly = false)
    public void save(SwmPersonSchedule swmPersonSchedule) {
        super.save(swmPersonSchedule);
    }

    /**
     * 批量保存数据
     * @param scheduleList 排班列表
     */
    @Transactional(readOnly = false)
    public void batchSave(List<SwmPersonSchedule> scheduleList) {
        if (scheduleList != null && !scheduleList.isEmpty()) {
            for (SwmPersonSchedule schedule : scheduleList) {
                this.save(schedule);
            }
        }
    }

    /**
     * 删除数据
     * @param swmPersonSchedule
     */
    @Override
    @Transactional(readOnly = false)
    public void delete(SwmPersonSchedule swmPersonSchedule) {
        super.delete(swmPersonSchedule);
    }
} 