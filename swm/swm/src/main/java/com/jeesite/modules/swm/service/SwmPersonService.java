/**
 * @author Shawn
 * @date 2025-05-13
 */
package com.jeesite.modules.swm.service;

import com.google.common.collect.Lists;
import com.jeesite.common.entity.DataEntity;
import com.jeesite.common.entity.Page;
import com.jeesite.common.mybatis.mapper.query.QueryType;
import com.jeesite.common.service.CrudService;
import com.jeesite.modules.swm.dao.SwmPersonDao;
import com.jeesite.modules.swm.entity.SwmPerson;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 人员登记表service
 * 
 * @author Shawn
 */
@Service
@Transactional(readOnly = true)
public class SwmPersonService extends CrudService<SwmPersonDao, SwmPerson> {

    /**
     * 获取单条数据
     * 
     * @param swmPerson
     * @return
     */
    @Override
    public SwmPerson get(SwmPerson swmPerson) {
        return super.get(swmPerson);
    }

    /**
     * 查询分页数据
     * 
     * @param swmPerson 查询条件
     * @return
     */
    @Override
    public Page<SwmPerson> findPage(SwmPerson swmPerson) {
        return super.findPage(swmPerson);
    }

    /**
     * 查询分页数据（带分页参数）
     * 
     * @param page      分页参数
     * @param swmPerson 查询条件
     * @return
     */
    public Page<SwmPerson> findPage(Page<SwmPerson> page, SwmPerson swmPerson) {
        swmPerson.setPage(page);
        // 设置状态条件为在职或离职
        swmPerson.getSqlMap().getWhere().and("personnel_status", QueryType.IN,
                Lists.newArrayList(SwmPerson.PersonStatusEnum.ACTIVE, SwmPerson.PersonStatusEnum.INACTIVE));
        return this.findPage(swmPerson);
    }

    /**
     * 保存数据（插入或更新）
     * 
     * @param swmPerson
     */
    @Override
    @Transactional(readOnly = false)
    public void save(SwmPerson swmPerson) {
        super.save(swmPerson);
    }

    /**
     * 更新状态
     * 
     * @param swmPerson
     */
    @Override
    @Transactional(readOnly = false)
    public void updateStatus(SwmPerson swmPerson) {
        super.updateStatus(swmPerson);
    }

    /**
     * 删除数据
     * 
     * @param swmPerson
     */
    @Override
    @Transactional(readOnly = false)
    public void delete(SwmPerson swmPerson) {
        super.delete(swmPerson);
    }

}