package com.jeesite.modules.swm.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jeesite.common.entity.Page;
import com.jeesite.common.service.CrudService;
import com.jeesite.modules.swm.entity.SwmWorkType;
import com.jeesite.modules.swm.dao.SwmWorkTypeDao;

/**
 * 工种表Service
 * 
 * @author Shawn
 * @version 2025-07-04
 */
@Service
@Transactional(readOnly = true)
public class SwmWorkTypeService extends CrudService<SwmWorkTypeDao, SwmWorkType> {

    /**
     * 获取单条数据
     * 
     * @param swmWorkType
     * @return
     */
    @Override
    public SwmWorkType get(SwmWorkType swmWorkType) {
        return super.get(swmWorkType);
    }

    /**
     * 查询分页数据
     * 
     * @param swmWorkType
     * @return
     */
    @Override
    public Page<SwmWorkType> findPage(SwmWorkType swmWorkType) {
        return super.findPage(swmWorkType);
    }

    /**
     * 查询列表数据
     * 
     * @param swmWorkType
     * @return
     */
    @Override
    public List<SwmWorkType> findList(SwmWorkType swmWorkType) {
        return super.findList(swmWorkType);
    }

    /**
     * 保存数据（插入或更新）
     * 
     * @param swmWorkType
     */
    @Override
    @Transactional(readOnly = false)
    public void save(SwmWorkType swmWorkType) {
        super.save(swmWorkType);
    }

    /**
     * 更新状态
     * 
     * @param swmWorkType
     */
    @Override
    @Transactional(readOnly = false)
    public void updateStatus(SwmWorkType swmWorkType) {
        super.updateStatus(swmWorkType);
    }

    /**
     * 删除数据
     * 
     * @param swmWorkType
     */
    @Override
    @Transactional(readOnly = false)
    public void delete(SwmWorkType swmWorkType) {
        super.delete(swmWorkType);
    }

    /**
     * 获取有效的工种列表（状态为正常）
     * 
     * @return
     */
    public List<SwmWorkType> findActiveList() {
        SwmWorkType swmWorkType = new SwmWorkType();
        swmWorkType.setStatus(SwmWorkType.STATUS_NORMAL);
        return findList(swmWorkType);
    }
}
