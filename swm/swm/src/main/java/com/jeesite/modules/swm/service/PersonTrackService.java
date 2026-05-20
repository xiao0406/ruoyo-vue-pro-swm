package com.jeesite.modules.swm.service;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import com.alibaba.nacos.common.utils.CollectionUtils;
import com.jeesite.common.service.CrudService;
import com.jeesite.modules.config.TenantContext;
import com.jeesite.modules.constant.TdengineSuperTableConstant;
import com.jeesite.modules.enums.CorpDbEnum;
import com.jeesite.modules.swm.dao.PersonTrackDao;
import com.jeesite.modules.swm.entity.PersonTrackInfo;
import com.jeesite.modules.swm.entity.SwmDailyAttendance;
import com.jeesite.modules.sys.utils.DictUtils;
import com.jeesite.modules.utils.R;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * 人员追踪服务类
 * 
 * @author Shawn
 * @date 2025-01-14
 */
@Service
public class PersonTrackService extends CrudService<PersonTrackDao, PersonTrackInfo> {

    private static final Logger logger = LoggerFactory.getLogger(PersonTrackService.class);

    private final Random random = new Random();

    @Autowired
    private PersonTrackDao personTrackDao;

    @Autowired
    private ExternalCoordinateDataService externalCoordinateDataService;

    @Autowired
    private SwmDailyAttendanceService swmDailyAttendanceService;

    @Autowired
    private SwmPersonCacheService swmPersonCacheService;
    @Qualifier("swmExecutor")
    @Autowired
    private ThreadPoolTaskExecutor swmExecutor;
    @Resource
    private SwmSafetyPersonTrainingService swmSafetyPersonTrainingService;
    @Autowired
    private SwmHelmetDeviceService swmHelmetDeviceService;
    @Autowired
    private TDengineService tdengineService;

    @Value("${tdengine.dbname}")
    private String dbname;

    /**
     * 从数据库查询人员数据并转换为位置信息
     * 
     * @param searchName      搜索人员姓名
     * @param organizationKey 组织节点key
     * @return 人员位置数组
     * @author Shawn
     * @date 2025-01-14
     */
    public List<Map<String, Object>> queryPersonsFromDatabase(String searchName, String organizationKey,List<String> personTypeList) {
        List<Map<String, Object>> positions = new ArrayList<>();

//        if (CollectionUtils.isEmpty(personTypeList)){
//            logger.info("未查询到人员数据，searchName: {}, organizationKey: {}", searchName, organizationKey);
//            return positions;
//        }

        try {
            // 使用MyBatis查询数据
            List<PersonTrackInfo> dbResults = personTrackDao.findPersonTrackInfo(searchName, organizationKey,personTypeList);

            if (dbResults.isEmpty()) {
                logger.info("未查询到人员数据，searchName: {}, organizationKey: {}", searchName, organizationKey);
                return positions;
            }

            // 收集所有身份证号，用于批量查询坐标
            List<String> idCardList = new ArrayList<>();
            // 收集所有需要查询颜色的key值 2025/06/24 Shawn 添加
            Set<String> colorKeys = new HashSet<>();

            for (PersonTrackInfo person : dbResults) {
                String identityCard = person.getIdentityCard();
                if (identityCard != null && !identityCard.trim().isEmpty()) {
                    idCardList.add(identityCard);
                }

                // 收集颜色查询的key值
                if (person.getPersonType() != null && !person.getPersonType().trim().isEmpty()) {
                    colorKeys.add(person.getPersonType());
                }
                if (person.getWorkType() != null && !person.getWorkType().trim().isEmpty()) {
                    colorKeys.add(person.getWorkType());
                }
                if (person.getWorkerArchiveId() != null && !person.getWorkerArchiveId().trim().isEmpty()) {
                    colorKeys.add(person.getWorkerArchiveId());
                }
                if (person.getOfficeCode() != null && !person.getOfficeCode().trim().isEmpty()) {
                    colorKeys.add(person.getOfficeCode());
                }
                if (person.getPositionArchiveId() != null && !person.getPositionArchiveId().trim().isEmpty()) {
                    colorKeys.add(person.getPositionArchiveId());
                }
                if (person.getWorkGroupId() != null && !person.getWorkGroupId().trim().isEmpty()) {
                    colorKeys.add(person.getWorkGroupId());
                }
                if (person.getProdLineId() != null && !person.getProdLineId().trim().isEmpty()) {
                    colorKeys.add(person.getProdLineId());
                }
            }

            // 批量查询颜色信息 2025/06/24 Shawn 添加
            Map<String, String> colorMap = new HashMap<>();
            if (!colorKeys.isEmpty()) {
                try {
                    List<Map<String, Object>> colorResults = personTrackDao.getColorsByKeys(new ArrayList<>(colorKeys));
                    for (Map<String, Object> colorResult : colorResults) {
                        String key = (String) colorResult.get("key");
                        String color = (String) colorResult.get("color");
                        if (key != null && color != null) {
                            colorMap.put(key, color);
                        }
                    }
                    logger.info("从swm_helmet_subitem查询到 {} 个颜色配置", colorMap.size());
                } catch (Exception e) {
                    logger.error("查询颜色配置异常", e);
                }
            }

            // 批量查询external_coordinate_data表中的坐标数据
            Map<String, Map<String, Object>> locationMap = new HashMap<>();
            if (!idCardList.isEmpty()) {
                try {
                    R<Map<String, Map<String, Object>>> locationResult = externalCoordinateDataService
                            .getLatestLocationsByIdCards(idCardList);
                    if (locationResult.getCode() == R.SUCCESS) {
                        locationMap = locationResult.getData();
                        logger.info("从external_coordinate_data查询到 {} 个身份证的坐标数据", locationMap.size());
                    } else {
                        logger.warn("查询external_coordinate_data坐标数据失败: {}", locationResult.getMsg());
                    }
                } catch (Exception e) {
                    logger.error("查询external_coordinate_data坐标数据异常", e);
                }
            }

            // 转换数据库结果为人员位置数据
            List<String> identityCards = dbResults.stream().map(PersonTrackInfo::getIdentityCard).collect(Collectors.toList());
//            //工作时长map
//            Map<String, String> workHoursMap = getWorkHoursFromAttendanceBatch(identityCards);
//            //考勤Map
//            Map<String, String> attendanceStatusMap = getAttendanceStatusFromAttendanceBatch(identityCards);

            // 并行执行工时查询
            CompletableFuture<Map<String, String>> workHoursFuture = CompletableFuture.supplyAsync(
                    () -> getWorkHoursFromAttendanceBatch(identityCards), swmExecutor
            );

            // 并行执行考勤状态查询
            CompletableFuture<Map<String, String>> attendanceFuture = CompletableFuture.supplyAsync(
                    () -> getAttendanceStatusFromAttendanceBatch(identityCards), swmExecutor
            );

            // 等待两个任务都完成
            CompletableFuture<Void> allDone = CompletableFuture.allOf(workHoursFuture, attendanceFuture);
            allDone.join();
            Map<String, String> workHoursMap = workHoursFuture.get();
            Map<String, String> attendanceStatusMap = attendanceFuture.get();


            //查询人员是否完成安全教育视频情况，每个月看一次
            Date date = new Date();
            DateTime startMonth = DateUtil.beginOfMonth(date);
            DateTime endMonth = DateUtil.endOfMonth(date);
            Set<String> safetyStrList = swmSafetyPersonTrainingService.findListByIdCard(identityCards,startMonth,endMonth);

            //查询电量
//            identityCards
            Map<String,Integer> batteryMap = this.getBatteryLevelsByIdCards(identityCards);

            for (PersonTrackInfo person : dbResults) {
                String name = person.getName();
                String workType = person.getJobType();
                String organization = person.getOrganization();
                String workShop = person.getWorkShop();
                String teamGroup = person.getTeamGroup();
                String identityCard = person.getIdentityCard();
                String id = person.getId();

                // 如果某些字段为空，设置默认值
//                if (workType == null)
//                    workType = "待分配";
//                if (organization == null)
//                    organization = "未知单位";
//                if (workShop == null)
//                    workShop = "未知车间";
//                if (teamGroup == null)
//                    teamGroup = "未知班组";
//                if (identityCard == null)
//                    identityCard = "未登记";

                String personId = id; // 直接使用字符串ID，不转换为整数

                // 检查是否在external_coordinate_data表中找到了坐标数据
                if (identityCard != null && !identityCard.trim().isEmpty() && locationMap.containsKey(identityCard)) {
                    Map<String, Object> locationInfo = locationMap.get(identityCard);
                    Object xObj = locationInfo.get("x");
                    Object yObj = locationInfo.get("y");

                    if (xObj != null && yObj != null) {
                        try {
                            // 将external_coordinate_data中的坐标转换为整数
                            int x = (int) Math.round(Double.parseDouble(xObj.toString()));
                            int y = (int) Math.round(Double.parseDouble(yObj.toString()));

                            // 获取真实的考勤数据 2025/07/07 Shawn 修改
//                            String workHours = getWorkHoursFromAttendance(identityCard);
//                            String attendanceStatus = getAttendanceStatusFromAttendance(identityCard);
                            String workHours = workHoursMap.get(identityCard);
                            String attendanceStatus = attendanceStatusMap.get(identityCard);
                            if (workHours == null){
                                workHours = "0.0";
                            }
                            if (attendanceStatus == null){
                                attendanceStatus = "未知状态";
                            }

                            // 创建人员位置信息，包含颜色信息 2025/06/24 Shawn 修改
                            Map<String, Object> position = createPersonPositionWithColors(
                                    personId,
                                    name,
                                    x,
                                    y,
                                    workType,
                                    organization,
                                    workShop,
                                    teamGroup,
                                    workHours, // 从考勤记录获取工作时长
                                    attendanceStatus, // 从考勤记录获取考勤状态
                                    identityCard,
                                    true, // 标记为真实位置
                                    person, // 传入完整的person对象
                                    colorMap, // 传入颜色映射
                                    safetyStrList,batteryMap);// 传入是否安全培训状态


                            positions.add(position);
                            logger.info("添加身份证 {} ({}) 的真实坐标: x={}, y={}", identityCard, name, x, y);
                        } catch (NumberFormatException e) {
                            logger.warn("身份证 {} ({}) 的坐标数据格式错误，跳过该人员", identityCard, name);
                        }
                    } else {
                        logger.warn("身份证 {} ({}) 的坐标数据为空，跳过该人员", identityCard, name);
                    }
                } else {
                    // 没有找到坐标数据，跳过该人员，不返回位置信息
                    logger.info("身份证 {} ({}) 未找到坐标数据，跳过该人员", identityCard, name);
                }
            }

        } catch (Exception e) {
            logger.error("查询数据库人员数据失败", e);
            // 如果数据库查询失败，返回空列表而不是测试数据
            positions = new ArrayList<>();
        }

        logger.info("最终返回 {} 个有效位置信息", positions.size());
        return positions;
    }

    /**
     * 从数据库查询mqtt人员数据并转换为位置信息
     *
     * @param searchName      搜索人员姓名
     * @param organizationKey 组织节点key
     * @return 人员位置数组
     * @author fangxiaolong
     * @date 2026-03-12
     */
    public List<Map<String, Object>> queryMqttPersonsFromDatabase(String searchName, String organizationKey,List<String> personTypeList) {
        List<Map<String, Object>> positions = new ArrayList<>();

        try {
            // 使用 MyBatis 查询数据
            List<PersonTrackInfo> dbResults = personTrackDao.findPersonTrackInfo(searchName, organizationKey,personTypeList);

            if (dbResults.isEmpty()) {
                logger.info("【mqtt定位数据】 -未查询到人员数据，searchName: {}, organizationKey: {}", searchName, organizationKey);
                return positions;
            }

            // 收集所有身份证号，用于批量查询坐标
            List<String> idCardList = new ArrayList<>();
            // 收集所有需要查询颜色的 key 值 2025/06/24 Shawn 添加
            Set<String> colorKeys = new HashSet<>();

            for (PersonTrackInfo person : dbResults) {
                String identityCard = person.getIdentityCard();
                if (identityCard != null && !identityCard.trim().isEmpty()) {
                    idCardList.add(identityCard);
                }

                // 收集颜色查询的 key 值
                if (person.getPersonType() != null && !person.getPersonType().trim().isEmpty()) {
                    colorKeys.add(person.getPersonType());
                }
                if (person.getWorkType() != null && !person.getWorkType().trim().isEmpty()) {
                    colorKeys.add(person.getWorkType());
                }
                if (person.getWorkerArchiveId() != null && !person.getWorkerArchiveId().trim().isEmpty()) {
                    colorKeys.add(person.getWorkerArchiveId());
                }
                if (person.getOfficeCode() != null && !person.getOfficeCode().trim().isEmpty()) {
                    colorKeys.add(person.getOfficeCode());
                }
                if (person.getPositionArchiveId() != null && !person.getPositionArchiveId().trim().isEmpty()) {
                    colorKeys.add(person.getPositionArchiveId());
                }
                if (person.getWorkGroupId() != null && !person.getWorkGroupId().trim().isEmpty()) {
                    colorKeys.add(person.getWorkGroupId());
                }
                if (person.getProdLineId() != null && !person.getProdLineId().trim().isEmpty()) {
                    colorKeys.add(person.getProdLineId());
                }
            }

            // 批量查询颜色信息 2025/06/24 Shawn 添加
            Map<String, String> colorMap = new HashMap<>();
            if (!colorKeys.isEmpty()) {
                try {
                    List<Map<String, Object>> colorResults = personTrackDao.getColorsByKeys(new ArrayList<>(colorKeys));
                    for (Map<String, Object> colorResult : colorResults) {
                        String key = (String) colorResult.get("key");
                        String color = (String) colorResult.get("color");
                        if (key != null && color != null) {
                            colorMap.put(key, color);
                        }
                    }
                    logger.info("【mqtt定位数据】 -从 swm_helmet_subitem 查询到 {} 个颜色配置", colorMap.size());
                } catch (Exception e) {
                    logger.error("【mqtt定位数据】 -查询颜色配置异常", e);
                }
            }

            // 批量查询 mqtt_device_position 表中的坐标数据（替代 external_coordinate_data）
            Map<String, Map<String, Object>> locationMap = new HashMap<>();
            if (!idCardList.isEmpty()) {
                try {
                    R<Map<String, Map<String, Object>>> locationResult = getMqttLatestLocationsByIdCards(idCardList);
                    if (locationResult != null && locationResult.getCode() == R.SUCCESS) {
                        locationMap = locationResult.getData();
                        logger.info("【mqtt定位数据】 -从 mqtt_device_position 查询到 {} 个身份证的坐标数据", locationMap.size());
                    } else {
                        logger.warn("【mqtt定位数据】 -查询 mqtt_device_position 坐标数据失败：{}", locationResult != null ? locationResult.getMsg() : "null");
                    }
                } catch (Exception e) {
                    logger.error("【mqtt定位数据】 -查询 mqtt_device_position 坐标数据异常", e);
                }
            }

            // 转换数据库结果为人员位置数据
            List<String> identityCards = dbResults.stream().map(PersonTrackInfo::getIdentityCard).collect(Collectors.toList());
            // 并行执行工时查询
            CompletableFuture<Map<String, String>> workHoursFuture = CompletableFuture.supplyAsync(
                    () -> getWorkHoursFromAttendanceBatch(identityCards), swmExecutor
            );

            // 并行执行考勤状态查询
            CompletableFuture<Map<String, String>> attendanceFuture = CompletableFuture.supplyAsync(
                    () -> getAttendanceStatusFromAttendanceBatch(identityCards), swmExecutor
            );

            // 等待两个任务都完成
            CompletableFuture<Void> allDone = CompletableFuture.allOf(workHoursFuture, attendanceFuture);
            allDone.join();
            Map<String, String> workHoursMap = workHoursFuture.get();
            Map<String, String> attendanceStatusMap = attendanceFuture.get();


            for (PersonTrackInfo person : dbResults) {
                String name = person.getName();
                String workType = person.getWorkType();
                String organization = person.getOrganization();
                String workShop = person.getWorkShop();
                String teamGroup = person.getTeamGroup();
                String identityCard = person.getIdentityCard();
                String id = person.getId();

                // 如果某些字段为空，设置默认值
                if (workType == null)
                    workType = "待分配";
                if (organization == null)
                    organization = "未知单位";
                if (workShop == null)
                    workShop = "未知车间";
                if (teamGroup == null)
                    teamGroup = "未知班组";
                if (identityCard == null)
                    identityCard = "未登记";

                String personId = id; // 直接使用字符串 ID，不转换为整数

                // 检查是否在 mqtt_device_position 表中找到了坐标数据
                if (identityCard != null && !identityCard.trim().isEmpty() && locationMap.containsKey(identityCard)) {
                    Map<String, Object> locationInfo = locationMap.get(identityCard);
                    String lng = (String)locationInfo.get("lng");
                    String lat = (String)locationInfo.get("lat");
                    String floorId = (String)locationInfo.get("floorId");

                    if (lng != null && lat != null && floorId != null) {
                        try {
                            String workHours = workHoursMap.get(identityCard);
                            String attendanceStatus = attendanceStatusMap.get(identityCard);
                            if (workHours == null){
                                workHours = "0.0";
                            }
                            if (attendanceStatus == null){
                                attendanceStatus = "未知状态";
                            }

                            // 创建人员位置信息，包含颜色信息 2025/06/24 Shawn 修改
                            Map<String, Object> position = createMqttPersonPositionWithColors(
                                    personId,
                                    name,
                                    lng,  // 使用经度
                                    lat,  // 使用纬度
                                    floorId,
                                    workType,
                                    organization,
                                    workShop,
                                    teamGroup,
                                    workHours, // 从考勤记录获取工作时长
                                    attendanceStatus, // 从考勤记录获取考勤状态
                                    identityCard,
                                    true, // 标记为真实位置
                                    person, // 传入完整的 person 对象
                                    colorMap); // 传入颜色映射

                            positions.add(position);
                            logger.info("【mqtt定位数据】 -添加身份证 {} ({}) 的真实坐标：lng={}, lat={}", identityCard, name, lng, lat);
                        } catch (NumberFormatException e) {
                            logger.warn("【mqtt定位数据】 -身份证 {} ({}) 的经纬度数据格式错误，跳过该人员", identityCard, name);
                        }
                    } else {
                        logger.warn("【mqtt定位数据】 -身份证 {} ({}) 的经纬度数据为空，跳过该人员", identityCard, name);
                    }
                } else {
                    // 没有找到坐标数据，跳过该人员，不返回位置信息
                    logger.info("【mqtt定位数据】 -身份证 {} ({}) 未找到 mqtt_device_position 坐标数据，跳过该人员", identityCard, name);
                }
            }

        } catch (Exception e) {
            logger.error("【mqtt定位数据】 -查询数据库人员数据失败", e);
            // 如果数据库查询失败，返回空列表而不是测试数据
            positions = new ArrayList<>();
        }

        logger.info("【mqtt定位数据】 -最终返回 {} 个有效位置信息", positions.size());
        return positions;
    }

    /**
     * 根据身份证号列表查询最新电池电量
     * @param identityCards 身份证号列表（非空）
     * @return key=身份证号，value=最新电池电量（null表示无数据）
     */
    private Map<String, Integer> getBatteryLevelsByIdCards(List<String> identityCards) {
        Map<String, Integer> batteryMap = new HashMap<>();

        // 1. 边界条件校验：空列表直接返回空Map
        if (CollectionUtils.isEmpty(identityCards)) {
            return Collections.emptyMap();
        }

        // 2. 获取租户对应的数据库名
//        String corpCode = TenantContext.get();
//        String dbname = CorpDbEnum.getDbNameByCorpCode(corpCode);

        // 3. 构建IN查询条件（防SQL注入 + 空值过滤）
        StringBuilder idCardCondition = new StringBuilder();
        idCardCondition.append("id_card in (");
        for (int i = 0; i < identityCards.size(); i++) {
            idCardCondition.append("'").append(identityCards.get(i)).append("'");
            if (i < identityCards.size() - 1) {
                idCardCondition.append(",");
            }
        }
        idCardCondition.append(")");

        // 4. 构建TDengine查询SQL（优化语法 + 防注入）
        String sql = String.format(
                "SELECT LAST_ROW(id_card) AS id_card, LAST_ROW(bat_l) AS latest_battery " +
                        "FROM %s.%s " +
                        "WHERE %s " +
                        "PARTITION BY id_card",
                dbname,TdengineSuperTableConstant.HELMET_RUNDE_CA_REPORT_LOCATION, idCardCondition.toString()
        );

        R<JSONObject> result = tdengineService.executeTDengineSQL(sql);

        if (result != null && result.getCode() == R.SUCCESS && result.getData() != null) {
            JSONObject obj = result.getData();
            JSONArray dataArray = obj.getJSONArray("data");

            if (dataArray != null && dataArray.size() > 0) {
                for (int i = 0; i < dataArray.size(); i++) {
                    JSONArray row = dataArray.getJSONArray(i);
                    String idCard = row.getStr(0);
                    Integer latestBattery = row.getInt(1);
                    batteryMap.put(idCard, latestBattery);
                }
            }
        }
        return batteryMap;

    }

    /**
     * 生成随机坐标
     * 
     * @return 坐标数组 [x, y]
     * @author Shawn
     * @date 2025-01-14
     */
    public int[] generateRandomCoordinates() {
        int x = random.nextInt(2500) + 50; // 50-2550范围
        int y = random.nextInt(1100) + 50; // 50-1150范围
        return new int[] { x, y };
    }

    /**
     * 根据身份证号码查询人员信息
     * 
     * @param identityCard 身份证号码
     * @return 人员信息Map
     * @author Shawn
     * @date 2025-01-14
     */
    public Map<String, Object> getPersonByIdCard(String identityCard) {
        try {
            // 使用MyBatis查询数据
            PersonTrackInfo person = personTrackDao.getPersonByIdCard(identityCard);

            if (person != null) {
                Map<String, Object> result = new HashMap<>();
                result.put("id", person.getId());
                result.put("name", person.getName());
                result.put("personType", person.getPersonType());
                result.put("gender", person.getGender());
                result.put("organization", person.getOrganization() != null ? person.getOrganization() : "未知单位");
                result.put("workShop", person.getWorkShop() != null ? person.getWorkShop() : "未知车间");
                result.put("prodLine", person.getProdLine());
                result.put("teamGroup", person.getTeamGroup() != null ? person.getTeamGroup() : "未知班组");
                result.put("workType", person.getWorkType() != null ? person.getWorkType() : "待分配");
                result.put("identityCard", person.getIdentityCard());
                result.put("phoneNumber", person.getPhoneNumber());

                return result;
            }

        } catch (Exception e) {
            logger.error("根据身份证号码查询人员信息失败", e);
        }

        return null;
    }

    /**
     * 创建人员位置信息对象
     * 
     * @param id              人员ID
     * @param name            姓名
     * @param x               X坐标
     * @param y               Y坐标
     * @param workType        工种
     * @param organization    单位
     * @param workShop        车间
     * @param teamGroup       班组
     * @param workHours       工作时长
     * @param attendance      考勤状态
     * @param identityCard    身份证号
     * @param hasRealLocation 是否为真实位置
     * @return 人员位置信息Map
     * @author Shawn
     * @date 2025-01-14
     */
    private Map<String, Object> createPersonPosition(String id, String name, int x, int y, String workType,
            String organization, String workShop, String teamGroup,
            String workHours, String attendance, String identityCard, boolean hasRealLocation) {
        Map<String, Object> position = new HashMap<>();
        position.put("id", id);
        position.put("name", name);
        position.put("x", x);
        position.put("y", y);
        position.put("workType", workType);
        position.put("organization", organization);
        position.put("workShop", workShop);
        position.put("teamGroup", teamGroup);
        position.put("workHours", workHours);
        position.put("attendance", attendance);
        position.put("identityCard", identityCard);
        position.put("hasRealLocation", hasRealLocation);
        return position;
    }

    private Map<String, Object> createMqttPersonPosition(String id, String name, String x, String y, String floorId, String workType,
                                                     String organization, String workShop, String teamGroup,
                                                     String workHours, String attendance, String identityCard, boolean hasRealLocation) {
        Map<String, Object> position = new HashMap<>();
        position.put("id", id);
        position.put("name", name);
        position.put("x", x);
        position.put("y", y);
        position.put("floorId", floorId != null ? floorId : "");
        position.put("workType", workType);
        position.put("organization", organization);
        position.put("workShop", workShop);
        position.put("teamGroup", teamGroup);
        position.put("workHours", workHours);
        position.put("attendance", attendance);
        position.put("identityCard", identityCard);
        position.put("hasRealLocation", hasRealLocation);
        return position;
    }

    /**
     * 创建人员位置信息对象（兼容旧版本）
     * 
     * @param id           人员ID
     * @param name         姓名
     * @param x            X坐标
     * @param y            Y坐标
     * @param workType     工种
     * @param organization 单位
     * @param workShop     车间
     * @param teamGroup    班组
     * @param workHours    工作时长
     * @param attendance   考勤状态
     * @param identityCard 身份证号
     * @return 人员位置信息Map
     * @author Shawn
     * @date 2025-01-14
     */
    private Map<String, Object> createPersonPosition(String id, String name, int x, int y, String workType,
            String organization, String workShop, String teamGroup,
            String workHours, String attendance, String identityCard) {
        return createPersonPosition(id, name, x, y, workType, organization, workShop, teamGroup,
                workHours, attendance, identityCard, false);
    }

    /**
     * 创建人员位置信息对象，包含颜色信息
     * 
     * @param id              人员ID
     * @param name            姓名
     * @param x               X坐标
     * @param y               Y坐标
     * @param workType        工种
     * @param organization    单位
     * @param workShop        车间
     * @param teamGroup       班组
     * @param workHours       工作时长
     * @param attendance      考勤状态
     * @param identityCard    身份证号
     * @param hasRealLocation 是否为真实位置
     * @param person          完整的person对象
     * @param colorMap        颜色映射
     * @return 人员位置信息Map
     * @author Shawn
     * @date 2025/06/24
     */
    private Map<String, Object> createPersonPositionWithColors(String id, String name, int x, int y, String workType,
                                                               String organization, String workShop, String teamGroup,
                                                               String workHours, String attendance, String identityCard, boolean hasRealLocation,
                                                               PersonTrackInfo person, Map<String, String> colorMap,Set<String> safetyStrList,Map<String,Integer> batteryMap) {

        // 创建基础的人员位置信息
        Map<String, Object> position = createPersonPosition(id, name, x, y, workType, organization, workShop, teamGroup,
                workHours, attendance, identityCard, hasRealLocation);

        // 添加颜色信息 2025/06/24 Shawn 添加
        position.put("personTypeColor", colorMap.get(person.getPersonType()));
        position.put("workTypeColor", colorMap.get(person.getWorkType()));
        position.put("workerArchiveColor", colorMap.get(person.getWorkerArchiveId()));
        position.put("officeCodeColor", colorMap.get(person.getOfficeCode()));
        position.put("positionArchiveColor", colorMap.get(person.getPositionArchiveId()));
        position.put("workGroupColor", colorMap.get(person.getWorkGroupId()));
        position.put("prodLineColor", colorMap.get(person.getProdLineId()));

        // 添加ID字段，便于前端使用
        position.put("workerArchiveId", person.getWorkerArchiveId());
        position.put("officeCode", person.getOfficeCode());
        position.put("positionArchiveId", person.getPositionArchiveId());
        position.put("workGroupId", person.getWorkGroupId());
        position.put("prodLineId", person.getProdLineId());
        position.put("personNumber",person.getPersonNumber());
        position.put("age", person.getAge());
        position.put("urgentPerson", person.getUrgentPerson());
        position.put("urgentPhoneNumber", person.getUrgentPhoneNumber());

        // 添加手机号字段
        position.put("phoneNumber", person.getPhoneNumber());
        position.put("gender", person.getGender());
        position.put("bloodType", person.getBloodType());

        //判断是否进行安全检查
        position.put("safety", "未受教育");
        if (safetyStrList.contains(person.getIdentityCard())){
            position.put("safety", "已受教育");
        }

        //人员登记里的入场安全教育
//        education_status_enum
        String dictLabel = DictUtils.getDictLabel("education_status_enum", person.getSafetyEducation(), "");
        position.put("safetyEducation", dictLabel);
        position.put("powerOnStatus","在线");

        position.put("battery", batteryMap.get(person.getIdentityCard()));
        position.put("personType", person.getPersonType());
        return position;
    }

    private Map<String, Object> createMqttPersonPositionWithColors(String id, String name, String x, String y, String floorId, String workType,
                                                               String organization, String workShop, String teamGroup,
                                                               String workHours, String attendance, String identityCard, boolean hasRealLocation,
                                                               PersonTrackInfo person, Map<String, String> colorMap) {

        // 创建基础的人员位置信息
        Map<String, Object> position = createMqttPersonPosition(id, name, x, y,floorId, workType, organization, workShop, teamGroup,
                workHours, attendance, identityCard, hasRealLocation);

        // 添加颜色信息 2025/06/24 Shawn 添加
        position.put("personTypeColor", colorMap.get(person.getPersonType()));
        position.put("workTypeColor", colorMap.get(person.getWorkType()));
        position.put("workerArchiveColor", colorMap.get(person.getWorkerArchiveId()));
        position.put("officeCodeColor", colorMap.get(person.getOfficeCode()));
        position.put("positionArchiveColor", colorMap.get(person.getPositionArchiveId()));
        position.put("workGroupColor", colorMap.get(person.getWorkGroupId()));
        position.put("prodLineColor", colorMap.get(person.getProdLineId()));

        // 添加ID字段，便于前端使用
        position.put("workerArchiveId", person.getWorkerArchiveId());
        position.put("officeCode", person.getOfficeCode());
        position.put("positionArchiveId", person.getPositionArchiveId());
        position.put("workGroupId", person.getWorkGroupId());
        position.put("prodLineId", person.getProdLineId());
        position.put("personNumber",person.getPersonNumber());
        position.put("age", person.getAge());
        position.put("urgentPerson", person.getUrgentPerson());
        position.put("urgentPhoneNumber", person.getUrgentPhoneNumber());

        // 添加手机号字段
        position.put("phoneNumber", person.getPhoneNumber());
        position.put("gender", person.getGender());
        return position;
    }

    /**
     * 根据身份证号获取工作时长
     *
     * @param identityCard 身份证号
     * @return 格式化的工作时长，如"6.5小时"
     * @author Shawn
     * @date 2025/07/07
     */
    private String getWorkHoursFromAttendance(String identityCard) {
        try {
            if (identityCard == null || identityCard.trim().isEmpty()) {
                return "0小时";
            }

            // 1. 通过身份证获取员工信息
            Map<String, Object> personInfo = swmPersonCacheService.getActivePersonByIdentityCard(identityCard);
            if (personInfo == null) {
                logger.debug("身份证 {} 未找到对应的员工信息", identityCard);
                return "0小时";
            }

            String employeeId = (String) personInfo.get("id");
            if (employeeId == null) {
                logger.debug("身份证 {} 对应的员工ID为空", identityCard);
                return "0小时";
            }

            // 2. 查询当日考勤记录
            Date today = new Date();
            SwmDailyAttendance attendance = swmDailyAttendanceService.findByEmployeeIdAndDate(employeeId, today);

            if (attendance == null) {
                logger.debug("员工ID {} 当日无考勤记录", employeeId);
                return "0小时";
            }

            // 3. 获取实际工作时长
            BigDecimal effectiveWorkHours = attendance.getEffectiveWorkHours();
            if (effectiveWorkHours == null) {
                return "0小时";
            }

            // 4. 格式化返回
            double hours = effectiveWorkHours.doubleValue();
            if (hours == 0) {
                return "0小时";
            } else if (hours == (int) hours) {
                // 整数小时
                return String.format("%.0f小时", hours);
            } else {
                // 带小数的小时
                return String.format("%.1f小时", hours);
            }

        } catch (Exception e) {
            logger.error("获取身份证 {} 的工作时长失败", identityCard, e);
            return "0小时";
        }
    }

    /**
     * 根据身份证号获取工作时长（批量方法）
     *
     * @param identityCards 身份证号
     * @return 格式化的工作时长，如"6.5小时"
     * @author Shawn
     * @date 2025/07/07
     */
    private Map<String, String> getWorkHoursFromAttendanceBatch(List<String> identityCards) {
        Map<String, String> result = new HashMap<>();
        if (identityCards == null || identityCards.isEmpty()) {
            return result;
        }

        try {
            // 1. 批量获取人员信息
            Map<String, Map<String, Object>> personInfos = swmPersonCacheService.getActivePersonByIdentityCardBatch(identityCards);
            List<String> employeeIds = new ArrayList<>();
            Map<String, String> identityToEmployeeId = new HashMap<>();

            for (String identityCard : identityCards) {
                Map<String, Object> personInfo = personInfos.get(identityCard);
                if (personInfo == null || personInfo.get("id") == null) {
                    result.put(identityCard, "0小时");
                } else {
                    String employeeId = (String) personInfo.get("id");
                    employeeIds.add(employeeId);
                    identityToEmployeeId.put(employeeId, identityCard);
                }
            }

            if (employeeIds.isEmpty()) {
                return result;
            }

            // 2. 批量查询考勤记录
            Date today = new Date();
            List<SwmDailyAttendance> attendances = swmDailyAttendanceService.findByEmployeeIdAndDateBatch(employeeIds, today);

            // 3. 填充工作时长
            for (SwmDailyAttendance attendance : attendances) {
                if (attendance == null || attendance.getEmployeeId() == null) {
                    continue;
                }
                String identityCard = identityToEmployeeId.get(attendance.getEmployeeId());
                if (identityCard == null) {
                    continue;
                }

                BigDecimal effectiveWorkHours = attendance.getEffectiveWorkHours();
                double hours = effectiveWorkHours != null ? effectiveWorkHours.doubleValue() : 0;

                if (hours == 0) {
                    result.put(identityCard, "0小时");
                } else if (hours == (int) hours) {
                    result.put(identityCard, String.format("%.0f小时", hours));
                } else {
                    result.put(identityCard, String.format("%.1f小时", hours));
                }
            }

            // 4. 如果某些员工没有考勤记录，默认 0 小时
            for (String identityCard : identityCards) {
                result.putIfAbsent(identityCard, "0小时");
            }

        } catch (Exception e) {
            logger.error("获取身份证 {} 的工作时长失败", identityCards, e);
        }

        return result;
    }

    /**
     * 根据身份证号获取考勤状态
     *
     * @param identityCard 身份证号
     * @return 考勤状态描述，如"正常考勤"或"异常考勤"
     * @author Shawn
     * @date 2025/07/07
     */
    private String getAttendanceStatusFromAttendance(String identityCard) {
        try {
            if (identityCard == null || identityCard.trim().isEmpty()) {
                return "未知状态";
            }

            // 1. 通过身份证获取员工信息
            Map<String, Object> personInfo = swmPersonCacheService.getActivePersonByIdentityCard(identityCard);
            if (personInfo == null) {
                logger.debug("身份证 {} 未找到对应的员工信息", identityCard);
                return "未知状态";
            }

            String employeeId = (String) personInfo.get("id");
            if (employeeId == null) {
                logger.debug("身份证 {} 对应的员工ID为空", identityCard);
                return "未知状态";
            }

            // 2. 查询当日考勤记录
            Date today = new Date();
            SwmDailyAttendance attendance = swmDailyAttendanceService.findByEmployeeIdAndDate(employeeId, today);

            if (attendance == null) {
                logger.debug("员工ID {} 当日无考勤记录", employeeId);
                return "无考勤记录";
            }

            // 3. 获取考勤状态
            String attendanceNormal = attendance.getAttendanceNormal();
            if (attendanceNormal == null) {
                return "正常考勤";
            }

            // 4. 根据状态码返回描述
            switch (attendanceNormal) {
                case "0":
                    return "正常考勤";
                case "1":
                    return "异常考勤";
                default:
                    return "正常考勤";
            }

        } catch (Exception e) {
            logger.error("获取身份证 {} 的考勤状态失败", identityCard, e);
            return "查询失败";
        }
    }

    /**
     * 根据身份证号获取考勤状态
     *
     * @param identityCards 身份证号
     * @return 考勤状态描述，如"正常考勤"或"异常考勤"
     * @author Shawn
     * @date 2025/07/07
     */
    private Map<String,String> getAttendanceStatusFromAttendanceBatch(List<String> identityCards) {
        Map<String, String> result = new HashMap<>();
        if (identityCards == null || identityCards.isEmpty()) {
            return result;
        }

        try {
            // 1. 批量获取人员信息
            Map<String, Map<String, Object>> personInfos = swmPersonCacheService.getActivePersonByIdentityCardBatch(identityCards);
            List<String> employeeIds = new ArrayList<>();

            for (String identityCard : identityCards) {
                Map<String, Object> personInfo = personInfos.get(identityCard);
                if (personInfo == null || personInfo.get("id") == null) {
                    result.put(identityCard, "未知状态");
                } else {
                    employeeIds.add((String) personInfo.get("id"));
                }
            }

            if (employeeIds.isEmpty()) {
                return result;
            }

            // 2. 批量查询考勤记录
            Date today = new Date();
            List<SwmDailyAttendance> attendances = swmDailyAttendanceService.findByEmployeeIdAndDateBatch(employeeIds, today);

            // 3. 填充考勤状态
            Map<String, SwmDailyAttendance> attendanceMap = attendances.stream()
                    .filter(Objects::nonNull)
                    .collect(Collectors.toMap(SwmDailyAttendance::getIdentityCard, a -> a));

            for (String identityCard : identityCards) {
                SwmDailyAttendance attendance = attendanceMap.get(identityCard);
                if (attendance == null) {
                    result.put(identityCard, "无考勤记录");
                } else {
                    String attendanceNormal = attendance.getAttendanceNormal();
                    //07:00 - 18:00
                    String workTimeRange = attendance.getWorkTimeRange();
                    //07:09:54
                    Date clockInTime = attendance.getClockInTime();
                    //切割时间段，然后判断是否大于上班时间，大于说明迟早，小于是正常，为空则为未出勤
                    if (clockInTime == null) {
                        result.put(identityCard, "未出勤");
                    } else {
                        // 解析时间段
                        String[] split = workTimeRange.split("-");
                        String startTimeStr = split[0].trim(); // 07:00
                        LocalTime workStartTime = LocalTime.parse(startTimeStr, DateTimeFormatter.ofPattern("HH:mm"));
                        LocalTime clockInLocalTime = clockInTime.toInstant()
                                .atZone(ZoneId.systemDefault())
                                .toLocalTime();
                        // 判断
                        if (clockInLocalTime.isAfter(workStartTime)) {
                            result.put(identityCard, "迟到");
                        } else {
                            result.put(identityCard, "正常考勤");
                        }
                    }

//                    if ("1".equals(attendanceNormal)) {
//                        result.put(identityCard, "正常考勤");
//                    }else if ("2".equals(attendanceNormal)){
//                        result.put(identityCard, "休息日");
//                    } else if ("3".equals(attendanceNormal)){
//                        result.put(identityCard, "未出勤");
//                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    /**
     * 根据身份证列表从 mqtt_device_position 表查询当前日期最后一条记录的 lng,lat 坐标
     *
     * @param idCardList 身份证号列表
     * @return Map<身份证号，Map<坐标信息>>
     * @author Shawn
     * @date 2026/03/11
     */
    private R<Map<String, Map<String, Object>>> getMqttLatestLocationsByIdCards(List<String> idCardList) {
        logger.info("【mqtt定位数据】 -从 mqtt_device_position 表根据身份证列表查询当前日期最后一条记录坐标，身份证数量：{}",
                idCardList != null ? idCardList.size() : 0);

        if (idCardList == null || idCardList.isEmpty()) {
            return R.fail("【mqtt定位数据】 -身份证列表不能为空");
        }

        try {
            // 获取当前日期的开始和结束时间
            Date now = new Date();
            Date sevenDaysAgo = DateUtil.offsetDay(now, -7);
            String startTime = DateUtil.formatDateTime(sevenDaysAgo);
            String endTime = DateUtil.formatDateTime(now);

            // 构建身份证号的 IN 查询条件
            StringBuilder idCardCondition = new StringBuilder();
            idCardCondition.append("id_card in (");
            for (int i = 0; i < idCardList.size(); i++) {
                idCardCondition.append("'").append(idCardList.get(i)).append("'");
                if (i < idCardList.size() - 1) {
                    idCardCondition.append(",");
                }
            }
            idCardCondition.append(")");

            // 使用 LAST_ROW 函数配合 PARTITION BY 进行批量查询 mqtt_device_position 表
            String sql = String.format(
                    "select LAST_ROW(id_card) as id_card, LAST_ROW(lng) as lng, " +
                            "LAST_ROW(lat) as lat, LAST_ROW(floor_id) as floor_id, LAST_ROW(time) as time from %s.%s " +
                            "where %s and time >= '%s' and time <= '%s' " +
                            "partition by id_card",
//                    "plb", TdengineSuperTableConstant.MQTT_DEVICE_POSITION,
                    dbname, TdengineSuperTableConstant.MQTT_DEVICE_POSITION,
                    idCardCondition.toString(), startTime, endTime);

            logger.info("【mqtt定位数据】 -查询 mqtt_device_position 身份证坐标 SQL: {}", sql);
            R<JSONObject> result = tdengineService.executeTDengineSQL(sql);

            if (result.getCode() == R.SUCCESS && result.getData() != null) {
                JSONObject data = result.getData();
                JSONArray rows = data.getJSONArray("data");

                Map<String, Map<String, Object>> locationMap = new HashMap<>();

                if (rows != null && rows.size() > 0) {
                    // 解析查询结果（字段顺序与 SELECT 一致）
                    for (int i = 0; i < rows.size(); i++) {
                        JSONArray row = rows.getJSONArray(i);
                        if (row != null && row.size() >= 5) {
                            // id_card (索引 0)
                            String idCard = String.valueOf(row.get(0));

                            // lng (索引 1)
                            Object lngObj = row.get(1);

                            // lat (索引 2)
                            Object latObj = row.get(2);

                            // floor_id (索引 3)
                            Object floorIdObj = row.get(3);

                            // time (索引 4)
                            Object timeObj = row.get(4);

                            logger.debug("【mqtt定位数据】 -mqtt_device_position 原始数据 - idCard: {}, lng: {}, lat: {}, floorId: {}, time: {}",
                                    idCard, lngObj, latObj, floorIdObj, timeObj);

                            if (idCard != null && !idCard.trim().isEmpty()) {
                                Map<String, Object> locationInfo = new HashMap<>();
                                locationInfo.put("lng", lngObj);
                                locationInfo.put("lat", latObj);
                                locationInfo.put("floorId", floorIdObj);
                                locationInfo.put("time", timeObj);
                                locationInfo.put("idCard", idCard);

                                locationMap.put(idCard, locationInfo);
                                logger.info("【mqtt定位数据】 -从 mqtt_device_position 找到身份证 {} 的坐标：lng={}, lat={}, floorId={}, time={}",
                                        idCard, lngObj, latObj, floorIdObj, timeObj);
                            }
                        }
                    }

                    logger.info("【mqtt定位数据】 -从 mqtt_device_position 查询完成，找到 {} 个身份证的坐标信息", locationMap.size());
                    return R.ok(locationMap);
                } else {
                    logger.info("【mqtt定位数据】 -未找到匹配的 mqtt_device_position 数据");
                    return R.fail("【mqtt定位数据】 -未找到坐标数据");
                }
            }

            return R.fail("【mqtt定位数据】 -查询失败：" + result.getMsg());
        } catch (Exception e) {
            logger.error("【mqtt定位数据】 -从 mqtt_device_position 根据身份证列表查询坐标失败", e);
            return R.fail("【mqtt定位数据】 -查询失败：" + e.getMessage());
        }
    }
}