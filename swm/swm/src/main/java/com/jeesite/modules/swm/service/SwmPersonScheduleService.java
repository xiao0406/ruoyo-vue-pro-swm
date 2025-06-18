package com.jeesite.modules.swm.service;

import com.jeesite.common.entity.Page;
import com.jeesite.common.service.CrudService;
import com.jeesite.modules.swm.dao.SwmPersonScheduleDao;
import com.jeesite.modules.swm.entity.SwmPersonSchedule;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

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
     * @param swmPersonSchedule
     * @return
     */
    @Override
    public SwmPersonSchedule get(SwmPersonSchedule swmPersonSchedule) {
        return super.get(swmPersonSchedule);
    }

    /**
     * 查询分页数据
     * @param swmPersonSchedule
     * @return
     */
    public Page<SwmPersonSchedule> findPage(SwmPersonSchedule swmPersonSchedule) {
        return super.findPage(swmPersonSchedule);
    }

    /**
     * 查询分页数据（带页面参数）
     * @param page 分页对象
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
     * @param swmPersonSchedule
     * @return
     */
    public List<SwmPersonSchedule> findList(SwmPersonSchedule swmPersonSchedule) {
        return super.findList(swmPersonSchedule);
    }

    /**
     * 保存数据（插入或更新）
     * @param swmPersonSchedule
     */
    @Override
    @Transactional(readOnly = false)
    public void save(SwmPersonSchedule swmPersonSchedule) {
        super.save(swmPersonSchedule);
    }

    /**
     * 批量保存数据
     * @param scheduleList 排班列表
     */
    @Transactional(readOnly = false)
    public void batchSave(List<SwmPersonSchedule> scheduleList) {
        if (scheduleList != null && !scheduleList.isEmpty()) {
            for (SwmPersonSchedule schedule : scheduleList) {
                // 如果传入了身份证号，可以使用它进行相关处理
                if (schedule.getIdCard() != null && !schedule.getIdCard().isEmpty()) {
                    // 检查该人员在当月是否已有排班
                    List<SwmPersonSchedule> existingSchedules = dao.findByIdCardAndMonth(schedule.getIdCard(), schedule.getMonth());

                    // 如果已有排班，且当前不是修改操作（没有ID），则跳过
                    if (!existingSchedules.isEmpty() && (schedule.getId() == null || schedule.getId().isEmpty())) {
                        logger.info("人员 {} (身份证: {}) 在 {} 月已有排班，跳过添加新排班",
                                schedule.getPersonName(), schedule.getIdCard(), schedule.getMonth());
                        continue;
                    }
                }

                this.save(schedule);
            }
        }
    }

    /**
     * 删除数据
     * @param swmPersonSchedule
     */
    @Override
    @Transactional(readOnly = false)
    public void delete(SwmPersonSchedule swmPersonSchedule) {
        super.delete(swmPersonSchedule);
    }

    /**
     * 根据身份证号查询排班记录
     * @param idCard 身份证号码
     * @return 排班记录列表
     */
    public List<SwmPersonSchedule> findByIdCard(String idCard) {
        return dao.findByIdCard(idCard);
    }

    /**
     * 根据身份证号和月份查询排班记录
     * @param idCard 身份证号码
     * @param month 月份
     * @return 排班记录列表
     */
    public List<SwmPersonSchedule> findByIdCardAndMonth(String idCard, String month) {
        return dao.findByIdCardAndMonth(idCard, month);
    }

    /**
     * 根据身份证号获取班组名称
     * @param idCard 身份证号码
     * @return 班组名称
     */
    public String getWorkGroupNameByIdCard(String idCard) {
        return dao.getWorkGroupNameByIdCard(idCard);
    }

    /**
     * 根据实体对象查询数据
     * @param entity
     * @return
     */
    public SwmPersonSchedule getByEntity(SwmPersonSchedule entity) {
        return dao.getByEntity(entity);
    }
}
