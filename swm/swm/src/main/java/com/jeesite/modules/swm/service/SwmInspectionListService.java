package com.jeesite.modules.swm.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jeesite.common.entity.Page;
import com.jeesite.common.service.CrudService;
import com.jeesite.modules.swm.dao.SwmInspectionListDao;
import com.jeesite.modules.swm.entity.SwmInspectionList;
import com.jeesite.common.lang.StringUtils;
import com.jeesite.modules.sys.utils.DictUtils;

/**
 * 巡检列表Service
 * 
 * @author Shawn
 * @version 2025-05-21
 */
@Service
@Transactional(readOnly = true)
public class SwmInspectionListService extends CrudService<SwmInspectionListDao, SwmInspectionList> {

    @Autowired
    private SwmInspectionListDao swmInspectionListDao;

    /**
     * 获取单条数据
     */
    @Override
    public SwmInspectionList get(String id) {
        SwmInspectionList entity = super.get(id);
        if (entity != null) {
            // 设置字典文本
            if (StringUtils.isNotBlank(entity.getInspectionType())) {
                entity.setInspectionTypeText(DictUtils.getDictLabel(entity.getInspectionType(), "inspection_type", ""));
            }
            if (StringUtils.isNotBlank(entity.getInspectionListStatus())) {
                entity.setInspectionListStatusText(
                        DictUtils.getDictLabel(entity.getInspectionListStatus(), "inspection_list_status_enum", ""));
            }
        }
        return entity;
    }

    /**
     * 查询分页数据
     */
    @Override
    public Page<SwmInspectionList> findPage(SwmInspectionList swmInspectionList) {
        Page<SwmInspectionList> page = super.findPage(swmInspectionList);
        // 设置字典文本
        for (SwmInspectionList entity : page.getList()) {
            if (StringUtils.isNotBlank(entity.getInspectionType())) {
                entity.setInspectionTypeText(DictUtils.getDictLabel(entity.getInspectionType(), "inspection_type", ""));
            }
            if (StringUtils.isNotBlank(entity.getInspectionListStatus())) {
                entity.setInspectionListStatusText(
                        DictUtils.getDictLabel(entity.getInspectionListStatus(), "inspection_list_status_enum", ""));
            }
        }
        return page;
    }

    /**
     * 查询分页数据（带分页参数）
     */
    public Page<SwmInspectionList> findPage(Page<SwmInspectionList> page, SwmInspectionList swmInspectionList) {
        swmInspectionList.setPage(page);
        return this.findPage(swmInspectionList);
    }

    /**
     * 保存数据
     */
    @Override
    @Transactional(readOnly = false)
    public void save(SwmInspectionList swmInspectionList) {
        super.save(swmInspectionList);
    }

    /**
     * 删除数据
     */
    @Override
    @Transactional(readOnly = false)
    public void delete(SwmInspectionList swmInspectionList) {
        super.delete(swmInspectionList);
    }
}