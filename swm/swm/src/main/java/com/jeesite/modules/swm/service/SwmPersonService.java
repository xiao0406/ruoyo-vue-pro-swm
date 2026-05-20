/**
 * @author Shawn
 * @date 2025-05-13
 */
package com.jeesite.modules.swm.service;

import cn.hutool.core.collection.CollectionUtil;
import com.alibaba.nacos.common.utils.CollectionUtils;
import com.google.common.collect.Lists;
import com.jeesite.common.entity.Page;
import com.jeesite.common.mybatis.mapper.query.QueryType;
import com.jeesite.common.service.CrudService;
import com.jeesite.common.utils.excel.ExcelImport;
import com.jeesite.common.utils.excel.annotation.ExcelField;
import com.jeesite.common.utils.excel.annotation.ExcelFields;
import com.jeesite.modules.cache.service.RedisService;
import com.jeesite.modules.entity.AiDto;
import com.jeesite.modules.constant.SwmRedisConstant;
import com.jeesite.modules.fms.entity.FmsPositionArchive;
import com.jeesite.modules.fms.entity.FmsProdLine;
import com.jeesite.modules.fms.entity.FmsWorkGroup;
import com.jeesite.modules.swm.dao.SwmPersonDao;
import com.jeesite.modules.swm.entity.PersonnelOrganizationQueryParam;
import com.jeesite.modules.swm.entity.SwmPerson;
import com.jeesite.modules.swm.excel.SwmPersonSwitcWorkshopImport;
import com.jeesite.modules.swm.web.SwmDashboardNewController;
import com.jeesite.modules.utils.BatchOperationsUtil;
import com.jeesite.modules.sys.utils.CorpUtils;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

import org.springframework.web.multipart.MultipartFile;

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
    @Autowired
    private RedisService redisService;

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

        //查询设备在线数量
        String corpCode = CorpUtils.getCurrentCorpCode();
        Set<Object> deviceIds = redisService.sGet(corpCode + SwmRedisConstant.RedisIotKey.ONLINE_DEVICES_KEY);
        Set<String> todayOnSiteIdCards = new HashSet<>();
        if (deviceIds != null) {
            for (Object deviceId : deviceIds) {
                String currentPerson = (String) redisService.hget(SwmRedisConstant.RedisGlobalKey.DEVICE_PERSON_MAP, String.valueOf(deviceId));
                if (currentPerson != null){
                    todayOnSiteIdCards.add(currentPerson);
                }
            }
        }
        swmPerson.setTodayOnSiteIdCards(new ArrayList<>(todayOnSiteIdCards));
        Page<SwmPerson> result = this.findPage(swmPerson);
        if(CollectionUtils.isNotEmpty(result.getList())){
            for (SwmPerson person : result.getList()) {
                if (todayOnSiteIdCards.contains(person.getIdentityCard())){
                    person.setPowerOnStatus("0");
                }else {
                    person.setPowerOnStatus("1");
                }
            }
        }
        return result;
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
        if (list.isEmpty()) {
            return null;
        }
        return list.stream()
                .filter(person -> StringUtils.equals("0", StringUtils.trimToEmpty(person.getStatus())))
                .findFirst()
                .orElse(null);
    }

    /**
     * 通过员工ID查询员工
     *
     * @param employeeIds 员工ID列表
     * @return
     */
    public List<SwmPerson> findListByIds(Set<String> employeeIds) {
        if (employeeIds == null || employeeIds.isEmpty()) {
            return Collections.emptyList();
        }
        return dao.findListByIds(employeeIds);
    }

    /**
     * 根据部门条件查询在职人员
     *
     * @param departmentCondition 部门条件参数，可以是车间ID、班组ID、产线ID、组织编码或身份证号
     * @return 符合条件的在职人员列表
     */
    public List<SwmPerson> findPersonsByDepartmentCondition(String departmentCondition) {
        if (departmentCondition == null || departmentCondition.trim().isEmpty()) {
            return Collections.emptyList();
        }
        return dao.findPersonsByDepartmentCondition(departmentCondition);
    }

    /**
     * 根据关键词搜索人员（支持姓名、身份证、电话多字段搜索）
     *
     * @param keyword 搜索关键词
     * @return 符合条件的在职人员列表
     */
    public List<SwmPerson> searchPersonsByKeyword(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return Collections.emptyList();
        }
        return dao.searchPersonsByKeyword(keyword.trim());
    }

    /**
     * 根据身份证号列表查询人员
     *
     * @param idCards 身份证号列表
     * @return 人员列表
     */
    public List<SwmPerson> findByIdCards(List<String> idCards) {
        if (idCards == null || idCards.isEmpty()) {
            return Collections.emptyList();
        }
        return dao.findByIdCards(idCards);
    }

    /**
     * 统计不重复身份证的人员数量
     *
     * @return 不重复身份证的人员数量
     */
    public int countDistinctByIdentityCard() {
        return dao.countDistinctByIdentityCard();
    }
    
    /**
     * 查询所有在职人员
     * 条件：personnel_status='1' and status='0'
     * 
     * @return 在职人员列表
     * @author Shawn
     * @date 2025-01-24
     */
    public List<SwmPerson> findActivePersons() {
        SwmPerson query = new SwmPerson();
        query.setPersonnelStatus(SwmPerson.PersonStatusEnum.ACTIVE); // '1' - 在职
        query.setStatus("0"); // 正常状态
        return findList(query);
    }
    
    /**
     * 查询所有离职人员
     * 条件：personnel_status='0' and status='0'
     * 
     * @return 离职人员列表
     * @author Shawn
     * @date 2025-01-24
     */
    public List<SwmPerson> findInactivePersons() {
        SwmPerson query = new SwmPerson();
        query.setPersonnelStatus(SwmPerson.PersonStatusEnum.INACTIVE); // '0' - 离职
        query.setStatus("0"); // 正常状态
        return findList(query);
    }

    /**
     * 通过组织和工种查询人员
     */
    public List<SwmPerson> listByOrgAndWorkType(PersonnelOrganizationQueryParam query) {
        return dao.listByOrgAndWorkType(query);
    };

    /**
     * 根据身份证号列表查询在职人员
     * 
     * @param identityCards 身份证号列表
     * @return 在职人员列表
     * @author Shawn
     * @date 2025-01-15
     */
    public List<SwmPerson> findActivePersonsByIdentityCards(List<String> identityCards) {
        if (identityCards == null || identityCards.isEmpty()) {
            return Collections.emptyList();
        }
        return dao.findActivePersonsByIdentityCards(identityCards);
    }

    /**
     * 根据手机号列表查询在职人员
     * 
     * @param phoneNumbers 手机号列表
     * @return 在职人员列表
     * @author Shawn
     * @date 2025-01-15
     */
    public List<SwmPerson> findActivePersonsByPhoneNumbers(List<String> phoneNumbers) {
        if (phoneNumbers == null || phoneNumbers.isEmpty()) {
            return Collections.emptyList();
        }
        return dao.findActivePersonsByPhoneNumbers(phoneNumbers);
    }

    public Page<SwmPerson> findByIdCardsPage(SwmPerson swmPersonPage) {
        SwmPerson swmPerson = new SwmPerson();
        Page<SwmPerson> page = swmPersonPage.getPage();
        List<String> idCards = swmPersonPage.getIdCards();
        if (idCards == null || idCards.isEmpty()) {
            return page;
        }

//        List<SwmPerson> byIdCards = dao.findByIdCards(idCards);
        // 从DAO获取分页数据
        List<SwmPerson> list = dao.findByIdCardsPage(swmPersonPage);
        // 获取总记录数
        swmPerson.setIdCards(swmPersonPage.getIdCards());
        Long count = dao.findByIdCardsCount(swmPerson);

        // 设置分页结果
        page.setList(list);
        page.setCount(count);
        return page;
    }

    public List<AiDto.RiskStatistics> findTableName() {
        return dao.findTableName();
    }

    public List<AiDto.Trajectory> findPersonList() {
        return  dao.findPersonList();
    }

    public Page<SwmDashboardNewController.Person> findTodayAttendance(SwmDashboardNewController.Person vo) {
        Page<SwmDashboardNewController.Person> page = vo.getPage();
        List<SwmDashboardNewController.Person> list = dao.findTodayAttendance(vo);
        page.setList(list);
        return page;
    }

    public List<SwmPerson> peronsList(SwmPerson swmPerson) {
        return  dao.peronsList(swmPerson);
    }

    public Page<SwmDashboardNewController.Person> findManageTodayList(SwmDashboardNewController.Person vo) {
        Page<SwmDashboardNewController.Person> page = vo.getPage();
        List<SwmDashboardNewController.Person> list = dao.findManageTodayList(vo);
        page.setList(list);
        return page;
    }

    @Transactional(readOnly = false)
    public Integer importData(MultipartFile file) {

        ExcelImport excelImport;
        int count = 0;

        try {
            excelImport = new ExcelImport(file, 2, 0);
            List<SwmPersonSwitcWorkshopImport> list = excelImport.getDataList(SwmPersonSwitcWorkshopImport.class);

            if (CollectionUtil.isEmpty(list)) {
                return 0;
            }

            // 1. 收集Excel数据

            Set<String> departments = list.stream().map(SwmPersonSwitcWorkshopImport::getDepartment)
                    .filter(StringUtils::isNotBlank).collect(Collectors.toSet());

            Set<String> productionLines = list.stream().map(SwmPersonSwitcWorkshopImport::getProdLine)
                    .filter(StringUtils::isNotBlank).collect(Collectors.toSet());

            Set<String> teams = list.stream().map(SwmPersonSwitcWorkshopImport::getTeam)
                    .filter(StringUtils::isNotBlank).collect(Collectors.toSet());

            // 2. 查询组织架构
            String corpCode = CorpUtils.getCurrentCorpCode();
            List<FmsPositionArchive> dbDepartments = this.dao.selectByNames(new ArrayList<>(departments),corpCode);

            List<FmsProdLine> dbLines = this.dao.selectProdLineByNames(new ArrayList<>(productionLines),corpCode);

            List<FmsWorkGroup> dbTeams = this.dao.selectWorkGroupByNames(new ArrayList<>(teams),corpCode);

            // 3. 构建Map
            // 车间
            Map<String, FmsPositionArchive> departmentMap =
                    dbDepartments.stream().collect(Collectors.toMap(
                            FmsPositionArchive::getPositionName,
                            e -> e,
                            (a, b) -> a
                    ));

            // 产线 key = 车间ID_产线名
            Map<String, FmsProdLine> lineMap =
                    dbLines.stream().collect(Collectors.toMap(
                            e -> e.getWorkShopId() + "_" + e.getProdLineName(),
                            e -> e,
                            (a, b) -> a
                    ));

            // 班组 key = 产线ID_班组名
            Map<String, FmsWorkGroup> teamMap =
                    dbTeams.stream().collect(Collectors.toMap(
                            e -> e.getProdLineId() + "_" + e.getWorkGroupName(),
                            e -> e,
                            (a, b) -> a
                    ));

            // 4. 收集人员查询条件
            Set<String> idCards = new HashSet<>();
            Set<String> personNames = new HashSet<>();

            int rowNum = 2;
            for (SwmPersonSwitcWorkshopImport item : list) {
                String departmentName = item.getDepartment();
                String lineName = item.getProdLine();
                String teamName = item.getTeam();

                if (StringUtils.isBlank(departmentName)) {
                    throw new RuntimeException("第" + rowNum + "行：车间不能为空");
                }

                // ---------- 车间 ----------
                FmsPositionArchive department = departmentMap.get(departmentName);
                if (department == null) {
                    throw new RuntimeException("第" + rowNum + "行：车间不存在：" + departmentName);
                }

                // ---------- 产线 ----------
                String lineKey = department.getId() + "_" + lineName;
                FmsProdLine prodLine = lineMap.get(lineKey);

                if (prodLine == null) {
                    throw new RuntimeException(
                            "第" + rowNum + "行：车间【" + departmentName + "】下不存在产线：" + lineName);
                }

                // ---------- 班组 ----------
                String teamKey = prodLine.getId() + "_" + teamName;
                FmsWorkGroup team = teamMap.get(teamKey);

                if (team == null) {
                    throw new RuntimeException(
                            "第" + rowNum + "行：产线【" + lineName + "】下不存在班组：" + teamName);
                }

                String name = item.getName();
                String idCard = item.getIdentityCard();

                if (StringUtils.isNotBlank(idCard)) {
                    idCards.add(idCard);
                } else {
                    personNames.add(name);
                }

                rowNum++;
            }

            // 5. 查询人员
            List<SwmPerson> byIdCards = new ArrayList<>();
            if (!idCards.isEmpty()){
                byIdCards = this.dao.findByIdCards(new ArrayList<>(idCards));
            }


            Map<String, SwmPerson> idCardMap = byIdCards.stream().collect(Collectors.toMap(
                            SwmPerson::getIdentityCard,
                            e -> e,
                            (a, b) -> a
                    ));

            List<SwmPerson> byNames = new ArrayList<>();
            if (!personNames.isEmpty()){
                byNames = this.dao.findListByPersonNames(new ArrayList<>(personNames));
            }

            Map<String, SwmPerson> nameMap = byNames.stream().collect(Collectors.toMap(
                            SwmPerson::getName,
                            e -> e,
                            (a, b) -> a
                    ));

            // 6. 处理更新

            List<SwmPerson> updatePersons = new ArrayList<>();

            rowNum = 2;
            for (SwmPersonSwitcWorkshopImport item : list) {

                String departmentName = item.getDepartment();
                String lineName = item.getProdLine();
                String teamName = item.getTeam();

                FmsPositionArchive department = departmentMap.get(departmentName);
                String lineKey = department.getId() + "_" + lineName;
                FmsProdLine prodLine = lineMap.get(lineKey);

                String teamKey = prodLine.getId() + "_" + teamName;
                FmsWorkGroup team = teamMap.get(teamKey);

                String name = item.getName();
                String idCard = item.getIdentityCard();

                SwmPerson person = null;

                // 优先身份证
                if (StringUtils.isNotBlank(idCard)) {
                    person = idCardMap.get(idCard);
                }

                // 没身份证用姓名
                if (person == null && StringUtils.isNotBlank(name)) {
                    person = nameMap.get(name);
                }

                if (person == null) {
                    logger.error("未找到人员：" + name);
                    continue;
                }

                // 填充ID
                person.setDepartment(department.getId());
                person.setProdLine(prodLine.getId());
                person.setTeam(team.getId());

                updatePersons.add(person);

                rowNum++;
            }

            // 7. 批量更新
            if (!updatePersons.isEmpty()) {
                List<List<SwmPerson>> lists = BatchOperationsUtil.batchCutting(updatePersons, 100);
                for (List<SwmPerson> updateList : lists) {
                    this.dao.updateTeamBatch(updateList);
                }
            }

            count = updatePersons.size();

        } catch (Exception e) {
            throw new RuntimeException("导入失败：" + e.getMessage(), e);
        }

        return count;
    }

    public List<SwmPerson> findListByJobTypeList(List<String> jobtypeList) {
        return this.dao.findListByJobTypeList(jobtypeList);
    }

    public void updateBatch(SwmPerson swmPerson) {
        List<String> ids = swmPerson.getIds();
        Set< String> idSet = new HashSet<>(ids);
        List<SwmPerson> personList = this.dao.findListByIds(idSet);
        for (SwmPerson person : personList) {
            person.setCompany(swmPerson.getCompany());
            person.setDepartment(swmPerson.getDepartment());
            person.setProdLine(swmPerson.getProdLine());
            person.setTeam(swmPerson.getTeam());
            super.update(swmPerson);
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
    }
    @Transactional(readOnly = false)
    public Integer importData1(MultipartFile file) {
        ExcelImport excelImport;
        int count = 0;

        try {
            excelImport = new ExcelImport(file, 2, 0);
            List<SwmPersonSwitcWorkshopImport1> list = excelImport.getDataList(SwmPersonSwitcWorkshopImport1.class);

            if (CollectionUtil.isEmpty(list)) {
                return 0;
            }

            List<List<SwmPersonSwitcWorkshopImport1>> lists = BatchOperationsUtil.batchCutting(list, 100);
            for (List<SwmPersonSwitcWorkshopImport1> updateList : lists) {
                this.dao.updateBatchUrgentPerson(updateList);
            }

            count = lists.size();

        } catch (Exception e) {
            throw new RuntimeException("导入失败：" + e.getMessage(), e);
        }

        return count;
    }


    @Data
    public static  class SwmPersonSwitcWorkshopImport1 {

        @ExcelFields({
                @ExcelField(title = "姓名", attrName = "name", align = ExcelField.Align.CENTER, sort = 10),
                @ExcelField(title = "手机号码", attrName = "phoneNumber", align = ExcelField.Align.CENTER, sort = 20),
                @ExcelField(title = "紧急联系人", attrName = "urgentPerson", align = ExcelField.Align.CENTER, sort = 30),
                @ExcelField(title = "紧急联系人电话", attrName = "urgentPhoneNumber", align = ExcelField.Align.CENTER, sort = 40),
                @ExcelField(title = "血型", attrName = "bloodType", align = ExcelField.Align.CENTER, sort = 50),
                @ExcelField(title = "所属班组", attrName = "team", align = ExcelField.Align.CENTER, sort = 60),
        })

        public SwmPersonSwitcWorkshopImport1() {
        }

        private String name; // 姓名
        private String phoneNumber; // 手机号码
        private String urgentPerson; // 紧急联系人
        private String urgentPhoneNumber; // 紧急联系人电话
        private String bloodType; // 血型
        private String team; // 所属班组
    }
}
