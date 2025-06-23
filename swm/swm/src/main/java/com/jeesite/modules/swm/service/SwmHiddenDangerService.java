/**
 * 隐患信息Service
 * @author Shawn
 * @date 2025-05-21
 */
package com.jeesite.modules.swm.service;

import com.jeesite.common.entity.Page;
import com.jeesite.common.service.CrudService;
import com.jeesite.modules.swm.dao.SwmHiddenDangerDao;
import com.jeesite.modules.swm.entity.SwmHiddenDanger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 隐患信息Service
 * 
 * @author Shawn
 * @date 2023-11-16
 */
@Service
@Transactional(readOnly = true)
public class SwmHiddenDangerService extends CrudService<SwmHiddenDangerDao, SwmHiddenDanger> {

    @Autowired
    private SwmHiddenDangerDao swmHiddenDangerDao;

    /**
     * 获取单条数据
     * 
     * @param swmHiddenDanger
     * @return
     */
    @Override
    public SwmHiddenDanger get(SwmHiddenDanger swmHiddenDanger) {
        SwmHiddenDanger entity = super.get(swmHiddenDanger);
        // 如果找到记录，并且有关联的巡检计划ID，但没有巡检计划名称，则重新查询
        if (entity != null && entity.getInspectionPlanId() != null && entity.getInspectionPlanName() == null) {
            List<SwmHiddenDanger> list = swmHiddenDangerDao.findListWithPlanName(entity);
            if (list != null && !list.isEmpty()) {
                return list.get(0);
            }
        }
        return entity;
    }

    /**
     * 查询分页数据
     * 
     * @param swmHiddenDanger 查询条件
     * @return
     */
    @Override
    public Page<SwmHiddenDanger> findPage(SwmHiddenDanger swmHiddenDanger) {
        Page<SwmHiddenDanger> page = swmHiddenDanger.getPage();
        
        // 设置默认排序
        if (page.getOrderBy() == null || page.getOrderBy().isEmpty()) {
            page.setOrderBy("a.create_date DESC");
        }
        
        // 执行分页查询
        page.setCount(swmHiddenDangerDao.findCount(swmHiddenDanger));
        if (page.getCount() > 0) {
            page.setList(swmHiddenDangerDao.findListWithPlanName(swmHiddenDanger));
        }
        
        return page;
    }

    /**
     * 查询分页数据（带分页参数）
     * 
     * @param page            分页参数
     * @param swmHiddenDanger 查询条件
     * @return
     */
    public Page<SwmHiddenDanger> findPage(Page<SwmHiddenDanger> page, SwmHiddenDanger swmHiddenDanger) {
        swmHiddenDanger.setPage(page);
        return this.findPage(swmHiddenDanger);
    }

    /**
     * 查询所有数据
     * 
     * @param swmHiddenDanger 查询条件
     * @return
     */
    @Override
    public List<SwmHiddenDanger> findList(SwmHiddenDanger swmHiddenDanger) {
        return swmHiddenDangerDao.findListWithPlanName(swmHiddenDanger);
    }

    /**
     * 保存数据（插入或更新）
     * 
     * @param swmHiddenDanger
     */
    @Override
    @Transactional(readOnly = false)
    public void save(SwmHiddenDanger swmHiddenDanger) {
        super.save(swmHiddenDanger);
    }

    /**
     * 更新状态
     * 
     * @param swmHiddenDanger
     */
    @Override
    @Transactional(readOnly = false)
    public void updateStatus(SwmHiddenDanger swmHiddenDanger) {
        super.updateStatus(swmHiddenDanger);
    }

    /**
     * 删除数据
     * 
     * @param swmHiddenDanger
     */
    @Override
    @Transactional(readOnly = false)
    public void delete(SwmHiddenDanger swmHiddenDanger) {
        super.delete(swmHiddenDanger);
    }
}