package com.jeesite.modules.swm.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jeesite.common.entity.Page;
import com.jeesite.common.service.CrudService;
import com.jeesite.modules.swm.entity.SwmArea;
import com.jeesite.modules.swm.dao.SwmAreaDao;

import java.util.List;

/**
 * 区域管理Service
 * 
 * @author Shawn
 * @version 2025-06-22
 */
@Service
@Transactional(readOnly = true)
public class SwmAreaService extends CrudService<SwmAreaDao, SwmArea> {

    /**
     * 获取单条数据
     * 
     * @param swmArea
     * @return
     */
    @Override
    public SwmArea get(SwmArea swmArea) {
        return super.get(swmArea);
    }

    /**
     * 查询分页数据
     * 
     * @param swmArea 查询条件
     * @return
     */
    @Override
    public Page<SwmArea> findPage(SwmArea swmArea) {
        return super.findPage(swmArea);
    }

    /**
     * 查询列表数据
     * 
     * @param swmArea
     * @return
     */
    @Override
    public List<SwmArea> findList(SwmArea swmArea) {
        return super.findList(swmArea);
    }

    /**
     * 保存数据（插入或更新）
     * 
     * @param swmArea
     */
    @Override
    @Transactional(readOnly = false)
    public void save(SwmArea swmArea) {
        super.save(swmArea);
    }

    /**
     * 更新状态
     * 
     * @param swmArea
     */
    @Override
    @Transactional(readOnly = false)
    public void updateStatus(SwmArea swmArea) {
        super.updateStatus(swmArea);
    }

    /**
     * 删除数据
     * 
     * @param swmArea
     */
    @Override
    @Transactional(readOnly = false)
    public void delete(SwmArea swmArea) {
        super.delete(swmArea);
    }
}