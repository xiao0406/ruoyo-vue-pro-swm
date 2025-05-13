/**
 * @author Shawn
 * @date 2025-05-13
 */
package com.jeesite.modules.swm.service;

import com.jeesite.common.entity.Page;
import com.jeesite.common.service.CrudService;
import com.jeesite.modules.swm.dao.SwmPersonDepartureDao;
import com.jeesite.modules.swm.entity.SwmPerson;
import com.jeesite.modules.swm.entity.SwmPersonDeparture;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 离职登记表service
 * 
 * @author Shawn
 */
@Service
@Transactional(readOnly = true)
public class SwmPersonDepartureService extends CrudService<SwmPersonDepartureDao, SwmPersonDeparture> {

    /**
     * 获取单条数据
     * 
     * @param swmPersonDeparture
     * @return
     */
    @Override
    public SwmPersonDeparture get(SwmPersonDeparture swmPersonDeparture) {
        return super.get(swmPersonDeparture);
    }

    /**
     * 查询分页数据
     * 
     * @param swmPersonDeparture 查询条件
     * @return
     */
    @Override
    public Page<SwmPersonDeparture> findPage(SwmPersonDeparture swmPersonDeparture) {
        return super.findPage(swmPersonDeparture);
    }

    /**
     * 查询分页数据（带分页参数）
     * 
     * @param page               分页参数
     * @param swmPersonDeparture 查询条件
     * @return
     */
    public Page<SwmPersonDeparture> findPage(Page<SwmPersonDeparture> page, SwmPersonDeparture swmPersonDeparture) {
        swmPersonDeparture.setPage(page);
        return this.findPage(swmPersonDeparture);
    }

    /**
     * 保存数据（插入或更新）
     * 
     * @param swmPersonDeparture
     */
    @Override
    @Transactional(readOnly = false)
    public void save(SwmPersonDeparture swmPersonDeparture) {
        super.save(swmPersonDeparture);
    }

    /**
     * 基于人员信息创建离职记录
     * 
     * @param swmPerson 人员信息
     * @return 创建的离职记录
     */
    @Transactional(readOnly = false)
    public SwmPersonDeparture createFromPerson(SwmPerson swmPerson) {
        if (swmPerson == null) {
            return null;
        }

        // 创建新的离职记录
        SwmPersonDeparture departure = new SwmPersonDeparture();

        // 复制基本信息
        departure.setName(swmPerson.getName());
        departure.setPersonType(swmPerson.getPersonType());
        departure.setGender(swmPerson.getGender());
        departure.setCompany(swmPerson.getCompany());
        departure.setDepartment(swmPerson.getDepartment());
        departure.setWorkProcess(swmPerson.getWorkProcess());
        departure.setTeam(swmPerson.getTeam());
        departure.setJobType(swmPerson.getJobType());
        departure.setSafetyHelmetId(swmPerson.getSafetyHelmetId());
        departure.setSafetyEducation(swmPerson.getSafetyEducation());
        departure.setIdentityCard(swmPerson.getIdentityCard());
        departure.setPhoneNumber(swmPerson.getPhoneNumber());

        // 设置离职相关信息
        departure.setPersonnelStatus(swmPerson.getPersonnelStatus());
        departure.setHelmetReturned(swmPerson.getHelmetReturned());
        departure.setDepartureType(swmPerson.getDepartureType());
        departure.setDepartureReason(swmPerson.getDepartureReason());
        departure.setDepartureDate(swmPerson.getDepartureDate());
        departure.setRemarks(swmPerson.getRemarks());

        // 保存离职记录
        this.save(departure);

        return departure;
    }

    /**
     * 更新状态
     * 
     * @param swmPersonDeparture
     */
    @Override
    @Transactional(readOnly = false)
    public void updateStatus(SwmPersonDeparture swmPersonDeparture) {
        super.updateStatus(swmPersonDeparture);
    }

    /**
     * 删除数据
     * 
     * @param swmPersonDeparture
     */
    @Override
    @Transactional(readOnly = false)
    public void delete(SwmPersonDeparture swmPersonDeparture) {
        super.delete(swmPersonDeparture);
    }
}