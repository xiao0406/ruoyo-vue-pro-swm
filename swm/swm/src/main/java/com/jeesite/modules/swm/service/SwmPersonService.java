/**
 * @author Shawn
 * @date 2025-05-13
 */
package com.jeesite.modules.swm.service;

import com.google.common.collect.Lists;
import com.jeesite.common.entity.Page;
import com.jeesite.common.mybatis.mapper.query.QueryType;
import com.jeesite.common.service.CrudService;
import com.jeesite.modules.swm.dao.SwmPersonDao;
import com.jeesite.modules.swm.entity.SwmPerson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * 人员登记表service
 *
 * @author Shawn
 */
@Service
@Transactional(readOnly = true)
public class SwmPersonService extends CrudService<SwmPersonDao, SwmPerson> {

    private static final Logger logger = LoggerFactory.getLogger(SwmPersonService.class);

    @Autowired
    private ApplicationContext applicationContext;

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

        // 更新缓存（避免循环依赖）
        try {
            SwmPersonCacheService cacheService = applicationContext.getBean(SwmPersonCacheService.class);
            if (cacheService != null) {
                cacheService.updatePersonCache(swmPerson);
            }
        } catch (Exception e) {
            // 忽略缓存更新异常，不影响主要业务
            logger.debug("更新人员缓存失败", e);
        }
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
        // 先从缓存中移除（避免循环依赖）
        try {
            SwmPersonCacheService cacheService = applicationContext.getBean(SwmPersonCacheService.class);
            if (cacheService != null) {
                cacheService.removePersonFromCache(swmPerson);
            }
        } catch (Exception e) {
            // 忽略缓存更新异常，不影响主要业务
            logger.debug("从缓存中移除人员失败", e);
        }

        super.delete(swmPerson);
    }

    /**
     * 根据身份证号码查询离职人员
     *
     * @param identityCard 身份证号码
     * @return 离职人员列表
     */
    public List<SwmPerson> findDepartedByIdentityCard(String identityCard) {
        if (identityCard == null || identityCard.trim().isEmpty()) {
            return Lists.newArrayList();
        }

        SwmPerson swmPerson = new SwmPerson();
        swmPerson.setIdentityCard(identityCard);
        swmPerson.setPersonnelStatus(SwmPerson.PersonStatusEnum.INACTIVE); // 只查询离职人员

        return dao.findDepartedByIdentityCard(swmPerson);
    }

    /**
     * 根据身份证号码查询人员
     *
     * @param identityCard 身份证号码
     * @return 人员信息，如果不存在则返回null
     */
    public SwmPerson getByIdentityCard(String identityCard) {
        if (identityCard == null || identityCard.trim().isEmpty()) {
            return null;
        }

        SwmPerson swmPerson = new SwmPerson();
        swmPerson.setIdentityCard(identityCard);

        List<SwmPerson> list = dao.findList(swmPerson);
        return list.isEmpty() ? null : list.get(0);
    }

    /**
     * 通过员工ID查询员工
     * @param employeeIds 员工ID列表
     * @return
     */
    public List<SwmPerson> findListByIds(Set<String> employeeIds) {
        if (employeeIds == null || employeeIds.isEmpty()) {
            return Collections.emptyList();
        }
        return dao.findListByIds(employeeIds);
    }
}
