package com.jeesite.modules.swm.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jeesite.common.entity.Page;
import com.jeesite.common.service.CrudService;
import com.jeesite.modules.swm.dao.SwmSiteMapManagementDao;
import com.jeesite.modules.swm.entity.SwmSiteMapManagement;

/**
 * 场地底图管理表Service
 * 
 * @author zwf
 * @version 2025-05-30
 */
@Service
@Transactional(readOnly = true)
public class SwmSiteMapManagementService extends CrudService<SwmSiteMapManagementDao, SwmSiteMapManagement> {
    
    /**
     * 获取单条数据
     * @param swmSiteMapManagement 查询条件
     * @return 场地底图管理信息
     */
    @Override
    public SwmSiteMapManagement get(SwmSiteMapManagement swmSiteMapManagement) {
        return super.get(swmSiteMapManagement);
    }
    
    /**
     * 查询分页数据
     * @param swmSiteMapManagement 查询条件
     * @return 分页数据
     */
    public Page<SwmSiteMapManagement> findPage(SwmSiteMapManagement swmSiteMapManagement) {
        return super.findPage(swmSiteMapManagement);
    }
    
    /**
     * 查询列表数据
     * @param swmSiteMapManagement 查询条件
     * @return 列表数据
     */
    @Override
    public List<SwmSiteMapManagement> findList(SwmSiteMapManagement swmSiteMapManagement) {
        return super.findList(swmSiteMapManagement);
    }
    
    /**
     * 保存数据（插入或更新）
     * @param swmSiteMapManagement 实体对象
     */
    @Override
    @Transactional(readOnly = false)
    public void save(SwmSiteMapManagement swmSiteMapManagement) {
        // 新增时，默认设置为禁用状态
        if (swmSiteMapManagement.getIsNewRecord()) {
            swmSiteMapManagement.setStatus("1"); // 1表示禁用
        }
        super.save(swmSiteMapManagement);
    }
    
    /**
     * 删除数据
     * @param swmSiteMapManagement 实体对象
     */
    @Override
    @Transactional(readOnly = false)
    public void delete(SwmSiteMapManagement swmSiteMapManagement) {
        // 执行物理删除而不是逻辑删除
        dao.physicalDelete(swmSiteMapManagement);
    }
    
    /**
     * 启用/禁用底图
     * @param id 底图ID
     * @param status 状态（0-启用，1-禁用）
     */
    @Transactional(readOnly = false)
    public void changeStatus(String id, String status) {
        SwmSiteMapManagement map = get(id);
        if (map == null) {
            return;
        }
        
        // 如果是启用操作
        if ("0".equals(status)) {
            // 禁用所有其他底图（不论项目）
            disableAllOtherMaps(id);
        }
        
        // 更新当前底图状态
        map.setStatus(status);
        // 设置更新时间和更新人
        map.preUpdate();
        
        // 直接通过DAO更新状态，确保状态字段被更新
        dao.updateMapStatus(map);
    }
    
    /**
     * 禁用所有其他底图（不论项目）
     * @param currentMapId 当前底图ID（不会被禁用）
     */
    @Transactional(readOnly = false)
    private void disableAllOtherMaps(String currentMapId) {
        // 查询所有底图
        SwmSiteMapManagement query = new SwmSiteMapManagement();
        List<SwmSiteMapManagement> maps = findList(query);
        
        // 禁用除当前底图外的所有底图
        for (SwmSiteMapManagement map : maps) {
            if (!map.getId().equals(currentMapId)) {
                map.setStatus("1"); // 设置为禁用
                map.preUpdate(); // 设置更新时间和更新人
                dao.updateMapStatus(map);
            }
        }
    }
} 