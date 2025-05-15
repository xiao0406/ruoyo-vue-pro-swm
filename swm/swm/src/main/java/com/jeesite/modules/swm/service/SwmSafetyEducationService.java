package com.jeesite.modules.swm.service;

import com.jeesite.common.entity.Page;
import com.jeesite.common.service.CrudService;
import com.jeesite.modules.swm.dao.SwmSafetyEducationDao;
import com.jeesite.modules.swm.entity.SwmSafetyEducation;
import io.seata.spring.annotation.GlobalTransactional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 安全教育Service
 * @author generated
 * @version 2024-05-14
 */
@Service
@Transactional(readOnly = true)
public class SwmSafetyEducationService extends CrudService<SwmSafetyEducationDao, SwmSafetyEducation> {
    
    /**
     * 获取单条数据
     * @param swmSafetyEducation
     * @return
     */
    @Override
    public SwmSafetyEducation get(SwmSafetyEducation swmSafetyEducation) {
        return super.get(swmSafetyEducation);
    }
    
    /**
     * 查询分页数据
     * @param swmSafetyEducation 查询条件
     * @return
     */
    @Override
    public Page<SwmSafetyEducation> findPage(SwmSafetyEducation swmSafetyEducation) {
        return super.findPage(swmSafetyEducation);
    }
    
    /**
     * 查询所有记录，不带默认的状态过滤
     * @return 所有记录列表
     */
    public List<SwmSafetyEducation> findAllWithoutStatusFilter() {
        return dao.findAllWithoutStatusFilter();
    }
    
    /**
     * 保存数据（插入或更新）
     * @param swmSafetyEducation
     */
    @Override
    @GlobalTransactional
    @Transactional(readOnly = false)
    public void save(SwmSafetyEducation swmSafetyEducation) {
        super.save(swmSafetyEducation);
    }
    
    /**
     * 更新状态
     * @param swmSafetyEducation
     */
    @Override
    @GlobalTransactional
    @Transactional(readOnly = false)
    public void updateStatus(SwmSafetyEducation swmSafetyEducation) {
        super.updateStatus(swmSafetyEducation);
    }
    
    /**
     * 删除数据
     * @param swmSafetyEducation
     */
    @Override
    @GlobalTransactional
    @Transactional(readOnly = false)
    public void delete(SwmSafetyEducation swmSafetyEducation) {
        super.delete(swmSafetyEducation);
    }
} 