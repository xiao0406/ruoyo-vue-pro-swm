/**
 * 危险源信息Service
 * @author Shawn
 * @version 2024-05-20
 */
package com.jeesite.modules.swm.service;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jeesite.common.entity.Page;
import com.jeesite.common.service.CrudService;
import com.jeesite.modules.swm.entity.SwmHazardSource;
import com.jeesite.modules.swm.dao.SwmHazardSourceDao;

/**
 * 危险源信息Service
 */
@Service
@Transactional(readOnly = true)
public class SwmHazardSourceService extends CrudService<SwmHazardSourceDao, SwmHazardSource> {

    /**
     * 获取单条数据
     * 
     * @param swmHazardSource
     * @return
     */
    @Override
    public SwmHazardSource get(SwmHazardSource swmHazardSource) {
        return super.get(swmHazardSource);
    }

    /**
     * 查询分页数据
     * 
     * @param swmHazardSource
     * @return
     */
    public Page<SwmHazardSource> findPage(SwmHazardSource swmHazardSource) {
        return super.findPage(swmHazardSource);
    }

    /**
     * 保存数据（插入或更新）
     * 
     * @param swmHazardSource
     */
    @Override
    @Transactional(readOnly = false)
    public void save(SwmHazardSource swmHazardSource) {
        super.save(swmHazardSource);
    }

    /**
     * 更新状态
     * 
     * @param swmHazardSource
     */
    @Override
    @Transactional(readOnly = false)
    public void updateStatus(SwmHazardSource swmHazardSource) {
        super.updateStatus(swmHazardSource);
    }

    /**
     * 删除数据
     * 
     * @param swmHazardSource
     */
    @Override
    @Transactional(readOnly = false)
    public void delete(SwmHazardSource swmHazardSource) {
        super.delete(swmHazardSource);
    }

}