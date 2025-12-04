package com.jeesite.modules.swm.service;

import com.jeesite.common.entity.Page;
import com.jeesite.common.service.CrudService;
import com.jeesite.modules.swm.dao.SwmPersonScheduleDao;
import com.jeesite.modules.swm.entity.SwmPersonSchedule;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 人员排班Service
 *
 * @author zwf
 * @version 2025-05-15
 */
@Service
@Transactional(readOnly = true)
public class SwmPersonScheduleService extends CrudService<SwmPersonScheduleDao, SwmPersonSchedule> {

    private static final Logger logger = LoggerFactory.getLogger(SwmPersonScheduleService.class);

    /**
     * 获取单条数据
     * 
     * @param swmPersonSchedule
     * @return
     */
    @Override
    public SwmPersonSchedule get(SwmPersonSchedule swmPersonSchedule) {
        return super.get(swmPersonSchedule);
    }

    /**
     * 查询分页数据
     * 
     * @param swmPersonSchedule
     * @return
     */
    public Page<SwmPersonSchedule> findPage(SwmPersonSchedule swmPersonSchedule) {
        return super.findPage(swmPersonSchedule);
    }

    /**
     * 查询分页数据（带页面参数）
     * 
     * @param page              分页对象
     * @param swmPersonSchedule
     * @return
     */
    public Page<SwmPersonSchedule> findPage(Page<SwmPersonSchedule> page, SwmPersonSchedule swmPersonSchedule) {
        // 设置分页参数
        swmPersonSchedule.setPage(page);
        // 执行查询
        return this.findPage(swmPersonSchedule);
    }

    /**
     * 查询所有数据
     * 
     * @param swmPersonSchedule
     * @return
     */
    public List<SwmPersonSchedule> findList(SwmPersonSchedule swmPersonSchedule) {
        return super.findList(swmPersonSchedule);
    }

    /**
     * 保存数据（插入或更新）
     * 
     * @param swmPersonSchedule
     */
    @Override
    @Transactional(readOnly = false)
    public void save(SwmPersonSchedule swmPersonSchedule) {
        super.save(swmPersonSchedule);
    }

    /**
     * 批量保存数据
     * 
     * @param scheduleList 排班列表
     */
    @Transactional(readOnly = false)
    public void batchSave(List<SwmPersonSchedule> scheduleList) {
        if (scheduleList != null && !scheduleList.isEmpty()) {
            for (SwmPersonSchedule schedule : scheduleList) {
                // 如果传入了身份证号，可以使用它进行相关处理
                if (schedule.getIdCard() != null && !schedule.getIdCard().isEmpty()) {
                    // 检查该人员在当月是否已有排班
                    List<SwmPersonSchedule> existingSchedules = dao.findByIdCardAndMonth(schedule.getIdCard(),
                            schedule.getMonth());

                    // 如果已有排班，且当前不是修改操作（没有ID），则跳过
                    if (!existingSchedules.isEmpty() && (schedule.getId() == null || schedule.getId().isEmpty())) {
                        logger.info("人员 {} (身份证: {}) 在 {} 月已有排班，跳过添加新排班",
                                schedule.getPersonName(), schedule.getIdCard(), schedule.getMonth());
                        continue;
                    }

                    // 使用前端传入的personId设置到employeeId字段
                    // Author: Shawn
                    // Date: 2025/01/27
                    if (schedule.getEmployeeId() == null || schedule.getEmployeeId().isEmpty()) {
                        String personId = schedule.getPersonId();
                        if (personId != null && !personId.isEmpty()) {
                            schedule.setEmployeeId(personId);
                            logger.debug("为排班记录设置employeeId: 身份证号={}, personId={}",
                                    schedule.getIdCard(), personId);
                        }
                    }
                }

                this.save(schedule);
            }
        }
    }

    /**
     * 删除数据
     * 
     * @param swmPersonSchedule
     */
    @Override
    @Transactional(readOnly = false)
    public void delete(SwmPersonSchedule swmPersonSchedule) {
        super.delete(swmPersonSchedule);
    }

    /**
     * 根据身份证号查询排班记录
     * 
     * @param idCard 身份证号码
     * @return 排班记录列表
     */
    public List<SwmPersonSchedule> findByIdCard(String idCard) {
        return dao.findByIdCard(idCard);
    }

    /**
     * 根据身份证号和月份查询排班记录
     * 
     * @param idCard 身份证号码
     * @param month  月份
     * @return 排班记录列表
     */
    public List<SwmPersonSchedule> findByIdCardAndMonth(String idCard, String month) {
        return dao.findByIdCardAndMonth(idCard, month);
    }

    /**
     * 根据身份证号获取班组名称
     * 
     * @param idCard 身份证号码
     * @return 班组名称
     */
    public String getWorkGroupNameByIdCard(String idCard) {
        return dao.getWorkGroupNameByIdCard(idCard);
    }

    /**
     * 根据实体对象查询数据
     * 
     * @param entity
     * @return
     */
    public SwmPersonSchedule getByEntity(SwmPersonSchedule entity) {
        return dao.getByEntity(entity);
    }

    /**
     * 获取所有班组列表
     * 
     * @return 班组列表，包含id和名称
     */
    public List<Map<String, Object>> findWorkGroupList() {
        return dao.findWorkGroupList();
    }

    /**
     * 批量获取多个人员的班组信息
     * 
     * @param idCards 身份证号码列表
     * @return 包含身份证号和班组名称的对象列表
     */
    public List<Map<String, Object>> batchGetWorkGroupNameByIdCards(List<String> idCards) {
        if (idCards == null || idCards.isEmpty()) {
            return Collections.emptyList();
        }
        return dao.batchGetWorkGroupNameByIdCards(idCards);
    }

    /**
     * 根据年份和月份获取不重复身份证的排班人数
     * 
     * @param yearMonth 年月格式，例如："2025-06"
     * @return 排班人数
     */
    public int countDistinctPersonByYearAndMonth(String yearMonth) {
        return dao.countDistinctPersonByYearAndMonth(yearMonth);
    }

    /**
     * 批量插入数据
     * @param list
     */
    public void insertBatch(List<SwmPersonSchedule> list) {
        dao.insertBatch(list);
    }

    public List<SwmPersonSchedule> scheduleList(SwmPersonSchedule swmPersonSchedule) {
        return dao.scheduleList(swmPersonSchedule);
    }
}
