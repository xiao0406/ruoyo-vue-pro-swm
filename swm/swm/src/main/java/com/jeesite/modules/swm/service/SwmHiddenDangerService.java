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

    /**
     * 获取单条数据
     * 
     * @param swmHiddenDanger
     * @return
     */
    @Override
    public SwmHiddenDanger get(SwmHiddenDanger swmHiddenDanger) {
        return super.get(swmHiddenDanger);
    }

    /**
     * 查询分页数据
     * 
     * @param swmHiddenDanger 查询条件
     * @return
     */
    @Override
    public Page<SwmHiddenDanger> findPage(SwmHiddenDanger swmHiddenDanger) {
        return super.findPage(swmHiddenDanger);
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
    public List<SwmHiddenDanger> findList(SwmHiddenDanger swmHiddenDanger) {
        return super.findList(swmHiddenDanger);
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