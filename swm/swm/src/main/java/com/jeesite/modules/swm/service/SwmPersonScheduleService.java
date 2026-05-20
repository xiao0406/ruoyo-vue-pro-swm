package com.jeesite.modules.swm.service;

import cn.hutool.core.collection.CollectionUtil;
import com.jeesite.common.entity.Page;
import com.jeesite.common.idgen.IdGen;
import com.jeesite.common.service.CrudService;
import com.jeesite.common.utils.excel.ExcelImport;
import com.jeesite.modules.annotation.SavePersonScheduleLog;
import com.jeesite.modules.entity.SwmPersonScheduleExport;
import com.jeesite.modules.swm.dao.SwmPersonScheduleDao;
import com.jeesite.modules.swm.entity.SwmPersonSchedule;
import com.jeesite.modules.swm.entity.SwmPersonScheduleLog;
import com.jeesite.modules.swm.entity.dto.SwmPersonScheduleDto;
import com.jeesite.modules.sys.entity.DictData;
import com.jeesite.modules.sys.entity.User;
import com.jeesite.modules.sys.utils.CorpUtils;
import com.jeesite.modules.sys.utils.DictUtils;
import com.jeesite.modules.sys.utils.UserUtils;
import com.jeesite.modules.utils.BatchOperationsUtil;
import com.xxl.job.core.context.XxlJobHelper;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

    @Resource
    private SwmPersonScheduleDao swmPersonScheduleDao;

    @Resource
    private SwmPersonScheduleLogService swmPersonScheduleLogService;
    @Resource
    private SwmDailyAttendanceService swmDailyAttendanceService;

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
    @SavePersonScheduleLog(remarkPrefix = "单个修改人员班次：目标班次")
    public void save(SwmPersonSchedule swmPersonSchedule) {
        swmPersonSchedule.setCorpCode(CorpUtils.getCurrentCorpCode());
        swmPersonSchedule.setCorpName(CorpUtils.getCurrentCorpName());
        super.save(swmPersonSchedule);
    }

    /**
     * 批量修改班次（事务控制：修改+存日志原子性）
     */
    @Transactional(readOnly = false)
    @SavePersonScheduleLog(remarkPrefix = "批量修改人员班次：目标班次") // 添加自定义注解，触发 AOP 日志记录
    public int batchUpdateClasses(SwmPersonScheduleDto dto) {

        // 2. 执行批量修改（原有逻辑）
        int affectRows = swmPersonScheduleDao.batchUpdateClasses(dto);

        // 3.同步调整日考勤数据
        //3.1 先通过人员id查询出身份证编码
        List<String> idCards = swmPersonScheduleDao.findIdCardsByIds(dto.getIds());
        // 3.2调整班次
        swmDailyAttendanceService.updateClasses(idCards, dto.getClasses());

        return affectRows;
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
        // 打印身份证号原始信息（关键！）
        XxlJobHelper.log("【DEBUG】身份证号原始值：[{}]，长度：{}",
                idCard, idCard == null ? 0 : idCard.length());
        // 打印ASCII码（排查不可见字符）
        if (idCard != null) {
            StringBuilder ascii = new StringBuilder();
            for (char c : idCard.toCharArray()) {
                ascii.append((int)c).append(",");
            }
            XxlJobHelper.log("【DEBUG】身份证号ASCII码：{}", ascii.toString());
        }
        List<SwmPersonSchedule> result = dao.findByIdCardAndMonth(idCard, null);
        XxlJobHelper.log("【DEBUG】DAO返回值是否为null：{}", result == null);
        XxlJobHelper.log("【DEBUG】DAO返回列表是否为空：{}", result.isEmpty());
        return result;
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

    @Transactional(readOnly = false)
    public Integer importData(MultipartFile file) {
        ExcelImport excelImport = null;
        Integer count = 0;
        try {
            excelImport = new ExcelImport(file, 2, 0);
            List<SwmPersonScheduleExport> list = excelImport.getDataList(SwmPersonScheduleExport.class);
            //白班身份证集合
            List<String> dayShiftIdCards = new ArrayList<>();
            //夜班身份证集合
            List<String> nightShiftIdCards = new ArrayList<>();

            if (CollectionUtil.isNotEmpty(list)){
                List<String> personNames = new ArrayList<>();
                for (SwmPersonScheduleExport export : list) {
                    if (StringUtils.isEmpty(export.getIdCard())){
                        personNames.add(export.getPersonName());
                    }
                }
                List<SwmPersonScheduleExport> personIds = this.dao.findPersonIdsByNames(personNames);
                Map<String, String> personIdcardMap = personIds.stream()
                        .collect(Collectors.toMap(
                                SwmPersonScheduleExport::getPersonName,
                                SwmPersonScheduleExport::getIdCard,
                                (existing, replacement) -> existing)); // 如果有重复键，保留已存在的


                for (SwmPersonScheduleExport export : list) {
//                    export.setMonth( month);
                    if (StringUtils.isEmpty(export.getIdCard())){
                        export.setIdCard(personIdcardMap.get(export.getPersonName()));
                    }
                    if ("1".equals(export.getClasses())){
                        dayShiftIdCards.add(export.getIdCard());
                    }else if ("3".equals(export.getClasses())){
                        nightShiftIdCards.add(export.getIdCard());
                    }
                }
                List<List<SwmPersonScheduleExport>> lists = BatchOperationsUtil.batchCutting(list, 100);
                for (List<SwmPersonScheduleExport> list1 : lists) {
                    this.dao.updateBatch(list1);
                }
                count = list.size();
            }

            //新增逻辑：调整班次之后，同步修改当天的日考勤数据
            //调整白班班次
            swmDailyAttendanceService.updateClasses(dayShiftIdCards,"1");
            //调整夜班班次
            swmDailyAttendanceService.updateClasses(nightShiftIdCards,"3");

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return count;
    }

    public List<SwmPersonSchedule> findListByCorpCode(SwmPersonSchedule swmPersonSchedule) {
        return this.dao.findListByCorpCode(swmPersonSchedule);

    }
}
