package com.jeesite.modules.swm.service;

import com.jeesite.common.entity.Page;
import com.jeesite.common.service.CrudService;
import com.jeesite.modules.swm.dao.SwmPersonnelBoardDao;
import com.jeesite.modules.swm.entity.SwmPersonnelBoard;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 人员看板表Service
 * 
 * @author zwf
 * @version 2025-05-15
 */
@Service
@Transactional(readOnly = true)
public class SwmPersonnelBoardService extends CrudService<SwmPersonnelBoardDao, SwmPersonnelBoard> {

    /**
     * 获取单条数据
     * @param swmPersonnelBoard
     * @return
     */
    @Override
    public SwmPersonnelBoard get(SwmPersonnelBoard swmPersonnelBoard) {
        return super.get(swmPersonnelBoard);
    }

    /**
     * 查询分页数据
     * @param swmPersonnelBoard
     * @return
     */
    public Page<SwmPersonnelBoard> findPage(SwmPersonnelBoard swmPersonnelBoard) {
        return super.findPage(swmPersonnelBoard);
    }

    /**
     * 查询分页数据（带页面参数）
     * @param page 分页对象
     * @param swmPersonnelBoard
     * @return
     */
    public Page<SwmPersonnelBoard> findPage(Page<SwmPersonnelBoard> page, SwmPersonnelBoard swmPersonnelBoard) {
        // 设置分页参数
        swmPersonnelBoard.setPage(page);
        // 执行查询
        return this.findPage(swmPersonnelBoard);
    }

    /**
     * 查询所有数据
     * @param swmPersonnelBoard
     * @return
     */
    public List<SwmPersonnelBoard> findList(SwmPersonnelBoard swmPersonnelBoard) {
        return super.findList(swmPersonnelBoard);
    }

    /**
     * 保存数据（插入或更新）
     * @param swmPersonnelBoard
     */
    @Override
    @Transactional(readOnly = false)
    public void save(SwmPersonnelBoard swmPersonnelBoard) {
        super.save(swmPersonnelBoard);
    }

    /**
     * 更新状态
     * @param swmPersonnelBoard
     */
    @Override
    @Transactional(readOnly = false)
    public void updateStatus(SwmPersonnelBoard swmPersonnelBoard) {
        super.updateStatus(swmPersonnelBoard);
    }

    /**
     * 删除数据
     * @param swmPersonnelBoard
     */
    @Override
    @Transactional(readOnly = false)
    public void delete(SwmPersonnelBoard swmPersonnelBoard) {
        super.delete(swmPersonnelBoard);
    }

    /**
     * 批量更新工作状态
     * @param ids 需要更新的ID列表
     * @param workStatus 新的工作状态
     */
    @Transactional(readOnly = false)
    public void batchUpdateWorkStatus(List<String> ids, String workStatus) {
        if (ids != null && !ids.isEmpty()) {
            for (String id : ids) {
                SwmPersonnelBoard entity = new SwmPersonnelBoard(id);
                entity.setWorkStatus(workStatus);
                super.update(entity);
            }
        }
    }

    /**
     * 批量更新安全帽状态
     * @param ids 需要更新的ID列表
     * @param helmetStatus 新的安全帽状态
     */
    @Transactional(readOnly = false)
    public void batchUpdateHelmetStatus(List<String> ids, String helmetStatus) {
        if (ids != null && !ids.isEmpty()) {
            for (String id : ids) {
                SwmPersonnelBoard entity = new SwmPersonnelBoard(id);
                entity.setHelmetStatus(helmetStatus);
                super.update(entity);
            }
        }
    }

    /**
     * 批量更新人员状态
     * @param ids 需要更新的ID列表
     * @param personnelStatus 新的人员状态
     */
    @Transactional(readOnly = false)
    public void batchUpdatePersonnelStatus(List<String> ids, String personnelStatus) {
        if (ids != null && !ids.isEmpty()) {
            for (String id : ids) {
                SwmPersonnelBoard entity = new SwmPersonnelBoard(id);
                entity.setPersonnelStatus(personnelStatus);
                super.update(entity);
            }
        }
    }
} 