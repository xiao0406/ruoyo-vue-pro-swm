/**
 * 隐患处置信息Service
 * @author Shawn
 * @date 2025-05-21
 */
package com.jeesite.modules.swm.service;

import com.jeesite.common.entity.Page;
import com.jeesite.common.service.CrudService;
import com.jeesite.modules.swm.dao.SwmDangerDisposalDao;
import com.jeesite.modules.swm.entity.SwmDangerDisposal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 隐患处置信息Service
 * 
 * @author Shawn
 * @date 2025-05-21
 */
@Service
@Transactional(readOnly = true)
public class SwmDangerDisposalService extends CrudService<SwmDangerDisposalDao, SwmDangerDisposal> {

    /**
     * 获取单条数据
     * 
     * @param swmDangerDisposal
     * @return
     */
    @Override
    public SwmDangerDisposal get(SwmDangerDisposal swmDangerDisposal) {
        return super.get(swmDangerDisposal);
    }

    /**
     * 查询分页数据
     * 
     * @param swmDangerDisposal 查询条件
     * @return
     */
    @Override
    public Page<SwmDangerDisposal> findPage(SwmDangerDisposal swmDangerDisposal) {
        return super.findPage(swmDangerDisposal);
    }

    /**
     * 查询分页数据（带分页参数）
     * 
     * @param page              分页参数
     * @param swmDangerDisposal 查询条件
     * @return
     */
    public Page<SwmDangerDisposal> findPage(Page<SwmDangerDisposal> page, SwmDangerDisposal swmDangerDisposal) {
        swmDangerDisposal.setPage(page);
        return this.findPage(swmDangerDisposal);
    }

    /**
     * 查询所有数据
     * 
     * @param swmDangerDisposal 查询条件
     * @return
     */
    public List<SwmDangerDisposal> findList(SwmDangerDisposal swmDangerDisposal) {
        return super.findList(swmDangerDisposal);
    }

    /**
     * 保存数据（插入或更新）
     * 
     * @param swmDangerDisposal
     */
    @Override
    @Transactional(readOnly = false)
    public void save(SwmDangerDisposal swmDangerDisposal) {
        super.save(swmDangerDisposal);
    }

    /**
     * 删除数据
     * 
     * @param swmDangerDisposal
     */
    @Override
    @Transactional(readOnly = false)
    public void delete(SwmDangerDisposal swmDangerDisposal) {
        super.delete(swmDangerDisposal);
    }

    /**
     * 根据隐患ID查询其所有处置记录
     * 
     * @param hiddenDangerId 隐患ID
     * @return 处置记录列表
     */
    public List<SwmDangerDisposal> findByHiddenDangerId(String hiddenDangerId) {
        SwmDangerDisposal swmDangerDisposal = new SwmDangerDisposal();
        swmDangerDisposal.setHiddenDangerId(hiddenDangerId);
        return findList(swmDangerDisposal);
    }
}