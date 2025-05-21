/**
 * 一键召回记录表服务实现类
 * @author auto
 * @date 2024-05-30
 */
package com.jeesite.modules.swm.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jeesite.common.entity.Page;
import com.jeesite.common.service.CrudService;
import com.jeesite.modules.swm.dao.SwmOneClickRecallDao;
import com.jeesite.modules.swm.entity.SwmOneClickRecall;
import com.jeesite.modules.swm.service.SwmOneClickRecallService;

/**
 * 一键召回记录表服务实现类
 * 
 * @author auto
 */
@Service
@Transactional(readOnly = true)
public class SwmOneClickRecallServiceImpl extends CrudService<SwmOneClickRecallDao, SwmOneClickRecall> implements SwmOneClickRecallService {

    @Autowired
    private SwmOneClickRecallDao swmOneClickRecallDao;

    @Override
    public SwmOneClickRecall get(String id) {
        return super.get(id);
    }
    
    @Override
    public SwmOneClickRecall get(SwmOneClickRecall oneClickRecall) {
        return super.get(oneClickRecall);
    }

    /**
     * 查询分页数据
     */
    @Override
    public Page<SwmOneClickRecall> findPage(SwmOneClickRecall oneClickRecall) {
        return super.findPage(oneClickRecall);
    }

    /**
     * 查询分页数据（带页面参数）
     */
    @Override
    public Page<SwmOneClickRecall> findPage(Page<SwmOneClickRecall> page, SwmOneClickRecall oneClickRecall) {
        // 设置分页参数
        oneClickRecall.setPage(page);
        // 执行查询
        return this.findPage(oneClickRecall);
    }

    @Override
    public List<SwmOneClickRecall> findList(SwmOneClickRecall oneClickRecall) {
        return super.findList(oneClickRecall);
    }

    @Override
    @Transactional(readOnly = false)
    public void save(SwmOneClickRecall oneClickRecall) {
        super.save(oneClickRecall);
    }

    @Override
    @Transactional(readOnly = false)
    public void delete(SwmOneClickRecall oneClickRecall) {
        super.delete(oneClickRecall);
    }
    
    @Override
    @Transactional(readOnly = false)
    public void deleteAll(String[] ids) {
        for (String id : ids) {
            SwmOneClickRecall oneClickRecall = new SwmOneClickRecall(id);
            this.delete(oneClickRecall);
        }
    }
} 