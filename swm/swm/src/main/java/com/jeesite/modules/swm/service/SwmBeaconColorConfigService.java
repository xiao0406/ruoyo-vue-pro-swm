package com.jeesite.modules.swm.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jeesite.common.entity.Page;
import com.jeesite.common.service.CrudService;
import com.jeesite.modules.swm.dao.SwmBeaconColorConfigDao;
import com.jeesite.modules.swm.entity.SwmBeaconColorConfig;

/**
 * 信标颜色配置表Service
 * 
 * @author zwf
 * @version 2025-05-23
 */
@Service
@Transactional(readOnly = true)
public class SwmBeaconColorConfigService extends CrudService<SwmBeaconColorConfigDao, SwmBeaconColorConfig> {
    
    /**
     * 获取单条数据
     * @param swmBeaconColorConfig 查询条件
     * @return 信标颜色配置信息
     */
    @Override
    public SwmBeaconColorConfig get(SwmBeaconColorConfig swmBeaconColorConfig) {
        return super.get(swmBeaconColorConfig);
    }
    
    /**
     * 查询分页数据
     * @param swmBeaconColorConfig 查询条件
     * @return 分页数据
     */
    public Page<SwmBeaconColorConfig> findPage(SwmBeaconColorConfig swmBeaconColorConfig) {
        return super.findPage(swmBeaconColorConfig);
    }
    
    /**
     * 查询列表数据
     * @param swmBeaconColorConfig 查询条件
     * @return 列表数据
     */
    @Override
    public List<SwmBeaconColorConfig> findList(SwmBeaconColorConfig swmBeaconColorConfig) {
        return super.findList(swmBeaconColorConfig);
    }
    
    /**
     * 保存数据（插入或更新）
     * @param swmBeaconColorConfig 实体对象
     */
    @Override
    @Transactional(readOnly = false)
    public void save(SwmBeaconColorConfig swmBeaconColorConfig) {
        super.save(swmBeaconColorConfig);
    }
    
    /**
     * 删除数据
     * @param swmBeaconColorConfig 实体对象
     */
    @Override
    @Transactional(readOnly = false)
    public void delete(SwmBeaconColorConfig swmBeaconColorConfig) {
        super.delete(swmBeaconColorConfig);
    }
} 