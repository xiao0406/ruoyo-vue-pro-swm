package com.jeesite.modules.swm.web;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import com.jeesite.common.web.BaseController;
import com.jeesite.modules.constant.TdengineSuperTableConstant;
import com.jeesite.modules.swm.service.*;
import com.jeesite.modules.sys.utils.DictUtils;
import com.jeesite.modules.utils.R;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * 人员追踪控制器
 * 
 * @author Shawn
 * @date 2025-01-14
 */
@RestController
@RequestMapping(value = "${adminPath}/personTrack")
@Api(value = "人员追踪接口", tags = "人员追踪接口")
public class PersonTrackController extends BaseController {

    @Autowired
    private PersonTrackService personTrackService;

    @Autowired
    private ExternalCoordinateDataService externalCoordinateDataService;

    @Autowired
    private SwmHelmetCacheService helmetCacheService;

    @Autowired
    private SwmHelmetDeviceService swmHelmetDeviceService;

    @Autowired
    private TDengineService tdengineService;

    @Value("${tdengine.dbname}")
    private String dbname;

    private static final Logger logger = LoggerFactory.getLogger(PersonTrackController.class);

    /**
     * 获取人员追踪组织树数据
     * 
     * @param displayType 展示类型：'按车间展示' | '按班组展示' | '按人员类型展示' | '按工种展示'
     * @return 组织树节点数组
     * @author Shawn
     * @date 2025-01-14
     */
    @GetMapping("/getOrgTree")
    @ResponseBody
    @ApiOperation("获取人员追踪组织树数据")
    public Map<String, Object> getOrgTree(
            @ApiParam(value = "展示类型", required = true) @RequestParam String displayType) {

        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> treeData = new ArrayList<>();

        try {
            // 构建固定的树结构数据
            Map<String, Object> factory = new HashMap<>();
            factory.put("key", "factory-1");
            factory.put("title", "天津厂");
            factory.put("nodeType", "factory");

            List<Map<String, Object>> workshops = new ArrayList<>();

            // 一车间
            Map<String, Object> workshop1 = new HashMap<>();
            workshop1.put("key", "workshop-1");
            workshop1.put("title", "一车间");
            workshop1.put("nodeType", "workshop");

            // 二车间
            Map<String, Object> workshop2 = new HashMap<>();
            workshop2.put("key", "workshop-2");
            workshop2.put("title", "二车间");
            workshop2.put("nodeType", "workshop");

            List<Map<String, Object>> teams = new ArrayList<>();

            // 油漆一班
            Map<String, Object> team1 = new HashMap<>();
            team1.put("key", "team-2-1-1");
            team1.put("title", "油漆一班");
            team1.put("nodeType", "team");

            List<Map<String, Object>> persons = new ArrayList<>();

            // 张三
            Map<String, Object> person1 = new HashMap<>();
            person1.put("key", "person-3");
            person1.put("title", "张三");
            person1.put("nodeType", "person");
            Map<String, Object> personInfo1 = new HashMap<>();
            personInfo1.put("id", 1);
            personInfo1.put("name", "张三");
            personInfo1.put("workType", "铆焊工");
            personInfo1.put("organization", "天津厂");
            personInfo1.put("workShop", "二车间");
            personInfo1.put("teamGroup", "油漆一班");
            personInfo1.put("workHours", "8小时");
            personInfo1.put("attendanceStatus", "正常考勤");
            personInfo1.put("idCard", "110101199001011234");
            person1.put("personInfo", personInfo1);

            // 李四
            Map<String, Object> person2 = new HashMap<>();
            person2.put("key", "person-4");
            person2.put("title", "李四");
            person2.put("nodeType", "person");
            Map<String, Object> personInfo2 = new HashMap<>();
            personInfo2.put("id", 2);
            personInfo2.put("name", "李四");
            personInfo2.put("workType", "铆焊工");
            personInfo2.put("organization", "天津厂");
            personInfo2.put("workShop", "二车间");
            personInfo2.put("teamGroup", "油漆一班");
            personInfo2.put("workHours", "8小时");
            personInfo2.put("attendanceStatus", "正常考勤");
            personInfo2.put("idCard", "110101199001015678");
            person2.put("personInfo", personInfo2);

            persons.add(person1);
            persons.add(person2);
            team1.put("children", persons);

            // 油漆二班
            Map<String, Object> team2 = new HashMap<>();
            team2.put("key", "team-2-2-1");
            team2.put("title", "油漆二班");
            team2.put("nodeType", "team");

            teams.add(team1);
            teams.add(team2);
            workshop2.put("children", teams);

            // 三车间
            Map<String, Object> workshop3 = new HashMap<>();
            workshop3.put("key", "workshop-3");
            workshop3.put("title", "三车间");
            workshop3.put("nodeType", "workshop");

            workshops.add(workshop1);
            workshops.add(workshop2);
            workshops.add(workshop3);
            factory.put("children", workshops);

            treeData.add(factory);

            result.put("success", true);
            result.put("data", treeData);
            result.put("message", "获取组织树数据成功");

        } catch (Exception e) {
            logger.error("获取组织树数据失败", e);
            result.put("success", false);
            result.put("message", "获取组织树数据失败：" + e.getMessage());
        }

        return result;
    }

    /**
     * 获取人员位置数据
     * 
     * @param organizationKey 组织节点key
     * @param searchName      搜索的人员姓名
     * @param displayType     展示类型
     * @return 人员位置数组
     * @author Shawn
     * @date 2025-01-14
     */
    @GetMapping("/getPersonPositions")
    @ResponseBody
    @ApiOperation("获取人员位置数据")
    public Map<String, Object> getPersonPositions(
            @ApiParam(value = "组织节点key") @RequestParam(required = false) String organizationKey,
            @ApiParam(value = "搜索人员姓名") @RequestParam(required = false) String searchName,
            @ApiParam(value = "展示类型") @RequestParam(required = false) String displayType,
            @ApiParam(value = "人员类型") @RequestParam(required = false) List<String> personTypeList) {

        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> positions = new ArrayList<>();

        try {
            // 从数据库查询人员数据
            List<Map<String, Object>> allPositions = queryPersonsFromDatabase(searchName, organizationKey,personTypeList);

            // 根据搜索条件过滤数据
            for (Map<String, Object> position : allPositions) {
                boolean shouldInclude = true;

                // 按姓名搜索过滤
                if (searchName != null && !searchName.trim().isEmpty()) {
                    String name = (String) position.get("name");
                    if (name == null || !name.contains(searchName.trim())) {
                        shouldInclude = false;
                    }
                }

                // 按组织结构过滤（这里可以根据实际需求扩展）
                if (organizationKey != null && !organizationKey.trim().isEmpty()) {
                    // 可以根据organizationKey来过滤相应的人员
                }

                if (shouldInclude) {
                    positions.add(position);
                }
            }

            result.put("success", true);
            result.put("data", positions);
            result.put("total", positions.size());
            result.put("message", "获取人员位置数据成功");

        } catch (Exception e) {
            logger.error("获取人员位置数据失败", e);
            result.put("success", false);
            result.put("message", "获取人员位置数据失败：" + e.getMessage());
        }

        return result;
    }


    /**
     * 获取mqtt人员位置数据
     *
     * @param personPositions 人员定位参数
     * @return mqtt人员位置数据
     * @author fangxiaolong
     * @date 2026-03-12
     */
    @PostMapping("/getMqttPersonPositions")
    @ResponseBody
    @ApiOperation("获取mqtt人员位置数据")
    public Map<String, Object> getMqttPersonPositions(@RequestBody PersonPositions personPositions) {
        String searchName = personPositions.getSearchName();
        String organizationKey = personPositions.getOrganizationKey();
        List<String> personTypeList = personPositions.getPersonTypeList();
        String displayType = personPositions.getDisplayType();

        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> positions = new ArrayList<>();

        try {
            // 从数据库查询人员数据
            List<Map<String, Object>> allPositions = personTrackService.queryMqttPersonsFromDatabase(searchName, organizationKey,personTypeList);

            // 根据搜索条件过滤数据
            for (Map<String, Object> position : allPositions) {
                boolean shouldInclude = true;

                // 按姓名搜索过滤
                if (searchName != null && !searchName.trim().isEmpty()) {
                    String name = (String) position.get("name");
                    if (name == null || !name.contains(searchName.trim())) {
                        shouldInclude = false;
                    }
                }

                // 按组织结构过滤（这里可以根据实际需求扩展）
                if (organizationKey != null && !organizationKey.trim().isEmpty()) {
                    // 可以根据organizationKey来过滤相应的人员
                }

                if (shouldInclude) {
                    positions.add(position);
                }
            }

            result.put("success", true);
            result.put("data", positions);
            result.put("total", positions.size());
            result.put("message", "获取人员位置数据成功");

        } catch (Exception e) {
            logger.error("获取人员位置数据失败", e);
            result.put("success", false);
            result.put("message", "获取人员位置数据失败：" + e.getMessage());
        }

        return result;
    }

    @Data
    private static class PersonPositions {
        private String organizationKey;
        private String searchName;
        private String displayType;
        private List<String> personTypeList;
    }

    /**
     * 获取人员位置数据
     *
     * @param organizationKey 组织节点key
     * @param searchName      搜索的人员姓名
     * @param displayType     展示类型
     * @return 人员位置数组
     * @author Shawn
     * @date 2025-01-14
     */
    @PostMapping("/getPersonPositionsNew")
    @ResponseBody
    @ApiOperation("获取人员位置数据")
    public Map<String, Object> getPersonPositionsNew(@RequestBody PersonPositions personPositions) {
        String searchName = personPositions.getSearchName();
        String organizationKey = personPositions.getOrganizationKey();
        List<String> personTypeList = personPositions.getPersonTypeList();
        String displayType = personPositions.getDisplayType();

        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> positions = new ArrayList<>();

        try {
            // 从数据库查询人员数据
            List<Map<String, Object>> allPositions = queryPersonsFromDatabase(searchName, organizationKey,personTypeList);

            // 根据搜索条件过滤数据
            for (Map<String, Object> position : allPositions) {
                boolean shouldInclude = true;

                // 按姓名搜索过滤
                if (searchName != null && !searchName.trim().isEmpty()) {
                    String name = (String) position.get("name");
                    if (name == null || !name.contains(searchName.trim())) {
                        shouldInclude = false;
                    }
                }

                // 按组织结构过滤（这里可以根据实际需求扩展）
                if (organizationKey != null && !organizationKey.trim().isEmpty()) {
                    // 可以根据organizationKey来过滤相应的人员
                }

                if (shouldInclude) {
                    positions.add(position);
                }
            }

            result.put("success", true);
            result.put("data", positions);
            result.put("total", positions.size());
            result.put("message", "获取人员位置数据成功");

        } catch (Exception e) {
            logger.error("获取人员位置数据失败", e);
            result.put("success", false);
            result.put("message", "获取人员位置数据失败：" + e.getMessage());
        }

        return result;
    }

    /**
     * 从数据库查询人员数据
     * 
     * @param searchName      搜索人员姓名
     * @param organizationKey 组织节点key
     * @return 人员位置数组
     * @author Shawn
     * @date 2025-01-14
     */
    private List<Map<String, Object>> queryPersonsFromDatabase(String searchName, String organizationKey,List<String> personTypeList) {
        // 使用Service层处理查询逻辑
        return personTrackService.queryPersonsFromDatabase(searchName, organizationKey,personTypeList);
    }

    /**
     * 搜索人员
     * 
     * @param name 人员姓名
     * @return 人员位置数组
     * @author Shawn
     * @date 2025-01-14
     */
    @GetMapping("/searchPerson")
    @ResponseBody
    @ApiOperation("搜索人员")
    public Map<String, Object> searchPerson(
            @ApiParam(value = "人员姓名", required = true) @RequestParam String name) {

        // 直接调用getPersonPositions方法，使用数据库查询
        return getPersonPositions(null, name, null,null);
    }

    /**
     * 获取人员轨迹数据
     * 
     * @param personId  人员ID
     * @param idCard    身份证号码
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @param startTime 开始时间（秒）
     * @param endTime   结束时间（秒）
     * @return 轨迹数据
     * @author Shawn
     * @date 2025-01-14
     */
    @GetMapping("/getPersonTrajectory")
    @ResponseBody
    @ApiOperation("获取人员轨迹数据")
    public Map<String, Object> getPersonTrajectory(
            @ApiParam(value = "人员ID") @RequestParam(required = false) String personId,
            @ApiParam(value = "身份证号码", required = true) @RequestParam String idCard,
            @ApiParam(value = "开始日期") @RequestParam(required = false) String startDate,
            @ApiParam(value = "结束日期") @RequestParam(required = false) String endDate,
            @ApiParam(value = "开始时间（秒）") @RequestParam(required = false) Integer startTime,
            @ApiParam(value = "结束时间（秒）") @RequestParam(required = false) Integer endTime) {

        Map<String, Object> result = new HashMap<>();

        try {
            // 根据身份证号码查询人员信息
            Map<String, Object> personInfo = getPersonByIdCard(idCard);

            if (personInfo == null) {
                result.put("success", false);
                result.put("message", "未找到身份证号为 " + idCard + " 的人员信息");
                return result;
            }

            String personName = (String) personInfo.get("name");
            String workType = (String) personInfo.get("workType");
            String organization = (String) personInfo.get("organization");
            String workShop = (String) personInfo.get("workShop");
            String teamGroup = (String) personInfo.get("teamGroup");

            // 获取轨迹点（仅从时序数据库获取真实数据）
            List<Map<String, Object>> trajectoryPoints = getTrajectoryPoints(
                    personId != null ? personId : (String) personInfo.get("id"),
                    personName,
                    workType,
                    organization,
                    workShop,
                    teamGroup,
                    idCard,
                    startDate,
                    endDate,
                    startTime,
                    endTime);

            // 构建时间线事件数据 - 返回空列表，让前端从区域围栏数据获取
            List<Map<String, Object>> timelineEvents = new ArrayList<>();

            Map<String, Object> data = new HashMap<>();
            // 轨迹点个数
            logger.info("轨迹点个数: {}", trajectoryPoints.size());
            data.put("trajectoryPoints", trajectoryPoints);
            data.put("timelineEvents", timelineEvents);

            result.put("success", true);
            result.put("data", data);
            result.put("message", "获取人员轨迹数据成功");

        } catch (Exception e) {
            logger.error("获取人员轨迹数据失败", e);
            result.put("success", false);
            result.put("message", "获取人员轨迹数据失败：" + e.getMessage());
        }

        return result;
    }

    /**
     * 获取人员轨迹数据（精确到秒）
     *
     * @param personId  人员ID
     * @param idCard    身份证号码
     * @param startDate 开始时间 (格式: yyyy-MM-dd HH:mm:ss)
     * @param endDate   结束时间 (格式: yyyy-MM-dd HH:mm:ss)
     * @return 轨迹数据
     * @author Shawn
     * @date 2025/06/25
     */
    @GetMapping("/getPersonTrajectoryByDateTime")
    @ResponseBody
    @ApiOperation("获取人员轨迹数据（精确到秒）")
    public Map<String, Object> getPersonTrajectoryByDateTime(
            @ApiParam(value = "人员ID") @RequestParam(required = false) String personId,
            @ApiParam(value = "身份证号码", required = true) @RequestParam String idCard,
            @ApiParam(value = "开始时间 (格式: yyyy-MM-dd HH:mm:ss)") @RequestParam(required = false) String startDate,
            @ApiParam(value = "结束时间 (格式: yyyy-MM-dd HH:mm:ss)") @RequestParam(required = false) String endDate) {

        Map<String, Object> result = new HashMap<>();

        try {
            // --- 解析时间参数 ---
            String parsedStartDate = null;
            String parsedEndDate = null;
            Integer startTime = null;
            Integer endTime = null;

            if (startDate != null && !startDate.trim().isEmpty()) {
                try {
                    LocalDateTime.parse(startDate, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                    parsedStartDate = startDate.substring(0, 10);
                    LocalTime localStartTime = LocalTime.parse(startDate.substring(11),
                            DateTimeFormatter.ofPattern("HH:mm:ss"));
                    startTime = localStartTime.toSecondOfDay();
                } catch (Exception e) {
                    logger.error("解析开始时间格式错误: {}", startDate, e);
                    result.put("success", false);
                    result.put("message", "开始时间格式错误，请使用 yyyy-MM-dd HH:mm:ss 格式。");
                    return result;
                }
            }

            if (endDate != null && !endDate.trim().isEmpty()) {
                try {
                    LocalDateTime.parse(endDate, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                    parsedEndDate = endDate.substring(0, 10);
                    LocalTime localEndTime = LocalTime.parse(endDate.substring(11),
                            DateTimeFormatter.ofPattern("HH:mm:ss"));
                    endTime = localEndTime.toSecondOfDay();
                } catch (Exception e) {
                    logger.error("解析结束时间格式错误: {}", endDate, e);
                    result.put("success", false);
                    result.put("message", "结束时间格式错误，请使用 yyyy-MM-dd HH:mm:ss 格式。");
                    return result;
                }
            }

            // --- 核心逻辑 (与 getPersonTrajectory 相同) ---
            Map<String, Object> personInfo = getPersonByIdCard(idCard);

            if (personInfo == null) {
                result.put("success", false);
                result.put("message", "未找到身份证号为 " + idCard + " 的人员信息");
                return result;
            }

            String personName = (String) personInfo.get("name");
            String workType = (String) personInfo.get("workType");
            String organization = (String) personInfo.get("organization");
            String workShop = (String) personInfo.get("workShop");
            String teamGroup = (String) personInfo.get("teamGroup");

            List<Map<String, Object>> trajectoryPoints = getTrajectoryPoints(
                    personId != null ? personId : (String) personInfo.get("id"),
                    personName,
                    workType,
                    organization,
                    workShop,
                    teamGroup,
                    idCard,
                    parsedStartDate,
                    parsedEndDate,
                    startTime,
                    endTime);

            List<Map<String, Object>> timelineEvents = new ArrayList<>();

            Map<String, Object> data = new HashMap<>();
            logger.info("轨迹点个数: {}", trajectoryPoints.size());
            data.put("trajectoryPoints", trajectoryPoints);
            data.put("timelineEvents", timelineEvents);

            result.put("success", true);
            result.put("data", data);
            result.put("message", "获取人员轨迹数据成功");

        } catch (Exception e) {
            logger.error("获取人员轨迹数据失败", e);
            result.put("success", false);
            result.put("message", "获取人员轨迹数据失败：" + e.getMessage());
        }

        return result;
    }

    /**
     * 根据身份证号码查询人员信息
     * 
     * @param idCard 身份证号码
     * @return 人员信息
     * @author Shawn
     * @date 2025-01-14
     */
    private Map<String, Object> getPersonByIdCard(String idCard) {
        // 使用Service层处理查询逻辑
        return personTrackService.getPersonByIdCard(idCard);
    }

    /**
     * 获取轨迹点（仅从时序数据库获取真实数据）
     * 
     * @param personId     人员ID
     * @param personName   人员姓名
     * @param workType     工种
     * @param organization 组织
     * @param workShop     车间
     * @param teamGroup    班组
     * @param idCard       身份证号
     * @param startDate    开始日期
     * @param endDate      结束日期
     * @param startTime    开始时间（秒数）
     * @param endTime      结束时间（秒数）
     * @return 轨迹点列表
     * @author Shawn
     * @date 2025-01-14
     */
    private List<Map<String, Object>> getTrajectoryPoints(String personId, String personName,
            String workType, String organization, String workShop, String teamGroup, String idCard,
            String startDate, String endDate, Integer startTime, Integer endTime) {

        List<Map<String, Object>> trajectoryPoints = new ArrayList<>();

        try {
            // 获取手机号
            String phoneNumber = null;
            try {
                Map<String, Object> personInfo = getPersonByIdCard(idCard);
                if (personInfo != null && personInfo.containsKey("phoneNumber")) {
                    phoneNumber = (String) personInfo.get("phoneNumber");
                }
            } catch (Exception e) {
                logger.warn("获取身份证 {} ({}) 的手机号失败", idCard, personName);
            }

            // 尝试从外部坐标数据服务获取位置数据
            try {
                // 从external_coordinate_data表获取真实轨迹数据
                if (idCard != null && !idCard.trim().isEmpty()) {
                    try {
                        R<List<Map<String, Object>>> trajectoryResult;

                        // 如果指定了时间范围参数，使用时间范围查询；否则使用当天查询
                        if (startDate != null || endDate != null || startTime != null || endTime != null) {
                            trajectoryResult = externalCoordinateDataService
                                    .getTrajectoryByIdCardAndTimeRange(idCard, startDate, endDate, startTime, endTime);
                        } else {
                            trajectoryResult = externalCoordinateDataService
                                    .getTodayTrajectoryByIdCard(idCard);
                        }
                        if (trajectoryResult.getCode() == R.SUCCESS && trajectoryResult.getData() != null) {
                            List<Map<String, Object>> realTrajectory = trajectoryResult.getData();

                            if (!realTrajectory.isEmpty()) {
                                logger.info("身份证 {} ({}) 从external_coordinate_data获取到 {} 个真实轨迹点", idCard, personName,
                                        realTrajectory.size());

                                // 将external_coordinate_data数据转换为前端需要的格式
                                for (Map<String, Object> point : realTrajectory) {
                                    Object xObj = point.get("x");
                                    Object yObj = point.get("y");
                                    Object timeObj = point.get("time");

                                    if (xObj != null && yObj != null) {
                                        try {
                                            // 转换坐标为整数
                                            int x = (int) Math.round(Double.parseDouble(xObj.toString()));
                                            int y = (int) Math.round(Double.parseDouble(yObj.toString()));

                                            Map<String, Object> trajectoryPoint = createPersonPosition(
                                                    personId != null ? personId.toString() : "0",
                                                    personName,
                                                    x,
                                                    y,
                                                    workType != null ? workType : "待分配",
                                                    organization != null ? organization : "未知单位",
                                                    workShop != null ? workShop : "未知车间",
                                                    teamGroup != null ? teamGroup : "未知班组",
                                                    "8小时",
                                                    "正常考勤",
                                                    idCard,
                                                    phoneNumber);

                                            // 添加时间信息
                                            trajectoryPoint.put("time", timeObj);
                                            trajectoryPoint.put("hasRealLocation", true);

                                            trajectoryPoints.add(trajectoryPoint);
                                        } catch (NumberFormatException e) {
                                            logger.warn("身份证 {} 坐标数据格式错误，跳过该点: x={}, y={}", idCard, xObj, yObj);
                                        }
                                    }
                                }

                                if (!trajectoryPoints.isEmpty()) {
                                    // 从字典获取抽样间隔时间，默认1分钟
                                    int sampleInterval = 1;
                                    try {
                                        String intervalStr = DictUtils.getDictLabel("sample_trajectory_points", "time", "1");
                                        sampleInterval = Integer.parseInt(intervalStr);
                                        logger.info("从字典获取轨迹抽样间隔: {} 分钟", sampleInterval);
                                    } catch (Exception e) {
                                        logger.warn("获取轨迹抽样间隔失败，使用默认值: {} 分钟", sampleInterval);
                                    }
                                    
                                    // 对轨迹点进行间隔抽样，减少数据量
                                    List<Map<String, Object>> sampledPoints = sampleTrajectoryPoints(trajectoryPoints,
                                            sampleInterval);
                                    logger.info("身份证 {} ({}) 原始轨迹点: {} 个，{}分钟间隔抽样后: {} 个",
                                            idCard, personName, trajectoryPoints.size(), sampleInterval, sampledPoints.size());
                                    return sampledPoints;
                                }
                            }
                        }

                        logger.info("身份证 {} ({}) 从external_coordinate_data未找到轨迹数据", idCard, personName);
                    } catch (Exception e) {
                        logger.error("从external_coordinate_data获取身份证 {} ({}) 轨迹数据异常", idCard, personName, e);
                    }
                }

                // 如果没有获取到真实数据，返回空列表
                logger.info("身份证 {} ({}) 无轨迹数据", idCard, personName);
                return trajectoryPoints;
            } catch (Exception e) {
                logger.error("获取轨迹点失败", e);
                return trajectoryPoints;
            }
        } catch (Exception e) {
            logger.error("获取轨迹点失败", e);
            return trajectoryPoints;
        }
    }

    /**
     * 创建人员位置对象
     */
    private Map<String, Object> createPersonPosition(String id, String name, int x, int y,
            String workType, String organization, String workShop,
            String teamGroup, String workHours, String attendanceStatus, String idCard, String phoneNumber) {

        Map<String, Object> position = new HashMap<>();
        position.put("x", x);
        position.put("y", y);
        position.put("id", id);
        position.put("name", name);
        position.put("workType", workType);
        position.put("organization", organization);
        position.put("workShop", workShop);
        position.put("teamGroup", teamGroup);
        position.put("workHours", workHours);
        position.put("attendanceStatus", attendanceStatus);
        position.put("idCard", idCard);
        position.put("phoneNumber", phoneNumber);

        return position;
    }

    /**
     * 创建人员位置信息对象（兼容旧版本）
     */
    private Map<String, Object> createPersonPosition(String id, String name, int x, int y,
            String workType, String organization, String workShop,
            String teamGroup, String workHours, String attendanceStatus, String idCard) {

        return createPersonPosition(id, name, x, y, workType, organization, workShop,
                teamGroup, workHours, attendanceStatus, idCard, null);
    }

    /**
     * 创建时间线事件对象
     */
    private Map<String, Object> createTimelineEvent(String time, String name, String content, int type) {
        Map<String, Object> event = new HashMap<>();
        event.put("time", time);
        event.put("name", name);
        event.put("content", content);
        event.put("type", type); // 1-正常 2-报警 3-警告

        return event;
    }

    /**
     * 对轨迹点进行时间间隔抽样，减少数据量
     * 
     * @param trajectoryPoints 原始轨迹点列表
     * @param intervalMinutes  抽样间隔（分钟）
     * @return 抽样后的轨迹点列表
     * @author Shawn
     * @date 2025/06/25
     */
    private List<Map<String, Object>> sampleTrajectoryPoints(List<Map<String, Object>> trajectoryPoints,
            int intervalMinutes) {
        if (trajectoryPoints == null || trajectoryPoints.isEmpty()) {
            return new ArrayList<>();
        }

        List<Map<String, Object>> sampledPoints = new ArrayList<>();

        try {
            // 按时间排序轨迹点（使用LocalDateTime进行准确排序）
            DateTimeFormatter sortFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

            trajectoryPoints.sort((a, b) -> {
                try {
                    String timeA = (String) a.get("time");
                    String timeB = (String) b.get("time");

                    if (timeA == null || timeB == null) {
                        return timeA == null ? (timeB == null ? 0 : 1) : -1;
                    }

                    // 处理时间格式，统一转换为标准格式
                    String normalizedTimeA = normalizeTimeString(timeA);
                    String normalizedTimeB = normalizeTimeString(timeB);

                    LocalDateTime dateTimeA = LocalDateTime.parse(normalizedTimeA, sortFormatter);
                    LocalDateTime dateTimeB = LocalDateTime.parse(normalizedTimeB, sortFormatter);

                    return dateTimeA.compareTo(dateTimeB);
                } catch (Exception e) {
                    // 如果解析失败，降级使用字符串比较
                    String timeA = (String) a.get("time");
                    String timeB = (String) b.get("time");
                    if (timeA == null || timeB == null) {
                        return timeA == null ? (timeB == null ? 0 : 1) : -1;
                    }
                    return timeA.compareTo(timeB);
                }
            });

            // 使用TreeMap按分钟分组，自动保持时间顺序
            TreeMap<LocalDateTime, Map<String, Object>> minuteGroups = new TreeMap<>();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

            // 先按分钟分组，同一分钟内的点会被覆盖，最终保留最后一个点
            for (Map<String, Object> point : trajectoryPoints) {
                String timeStr = (String) point.get("time");
                if (timeStr == null || timeStr.trim().isEmpty()) {
                    continue;
                }

                try {
                    // 处理不同的时间格式
                    LocalDateTime currentTime;
                    if (timeStr.contains("T")) {
                        // 格式：2025-06-13T10:27:27 或 2025-06-13T10:27:27.000
                        timeStr = timeStr.replace("T", " ");
                        if (timeStr.contains(".")) {
                            timeStr = timeStr.substring(0, timeStr.indexOf("."));
                        }
                    }
                    currentTime = LocalDateTime.parse(timeStr, formatter);

                    // 将时间截断到分钟级别作为分组键
                    LocalDateTime minuteKey = currentTime.truncatedTo(ChronoUnit.MINUTES);
                    
                    // 在同一分钟内，后面的点会覆盖前面的点，实现取最后一个点的效果
                    minuteGroups.put(minuteKey, point);
                    
                } catch (Exception e) {
                    logger.warn("解析时间失败，跳过该轨迹点: {}", timeStr, e);
                }
            }

            // 按间隔抽样分组后的数据
            LocalDateTime lastSampledMinute = null;
            for (Map.Entry<LocalDateTime, Map<String, Object>> entry : minuteGroups.entrySet()) {
                LocalDateTime currentMinute = entry.getKey();
                
                if (lastSampledMinute == null || 
                    ChronoUnit.MINUTES.between(lastSampledMinute, currentMinute) >= intervalMinutes) {
                    sampledPoints.add(entry.getValue());
                    lastSampledMinute = currentMinute;
                }
            }

            // 如果抽样后没有点，至少保留第一个和最后一个点
            if (sampledPoints.isEmpty() && !trajectoryPoints.isEmpty()) {
                sampledPoints.add(trajectoryPoints.get(0));
                if (trajectoryPoints.size() > 1) {
                    sampledPoints.add(trajectoryPoints.get(trajectoryPoints.size() - 1));
                }
            }

        } catch (Exception e) {
            logger.error("轨迹点抽样失败，返回原始数据", e);
            return trajectoryPoints;
        }

        return sampledPoints;
    }

    /**
     * 统一时间字符串格式
     * 
     * @param timeStr 原始时间字符串
     * @return 标准化后的时间字符串（yyyy-MM-dd HH:mm:ss格式）
     * @author Shawn
     * @date 2025/06/25
     */
    private String normalizeTimeString(String timeStr) {
        if (timeStr == null || timeStr.trim().isEmpty()) {
            return timeStr;
        }

        try {
            // 处理不同的时间格式
            if (timeStr.contains("T")) {
                // 格式：2025-06-13T10:27:27 或 2025-06-13T10:27:27.000
                timeStr = timeStr.replace("T", " ");
                if (timeStr.contains(".")) {
                    timeStr = timeStr.substring(0, timeStr.indexOf("."));
                }
            }

            // 确保时间格式为 yyyy-MM-dd HH:mm:ss
            if (timeStr.length() == 19 && timeStr.matches("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}")) {
                return timeStr;
            }

            // 如果格式不正确，返回原始字符串
            return timeStr;
        } catch (Exception e) {
            logger.warn("时间格式转换失败，使用原始格式: {}", timeStr);
            return timeStr;
        }
    }

    /**
     * 根据身份证查询区域围栏数据
     * 通过身份证号从Redis缓存中查找设备ID，然后匹配area_fence_data表中的设备ID后8位，
     * 查询该表并按area_name分组找出最早的记录
     * 
     * @param idCard    身份证号
     * @param startDate 开始日期 (可选，格式：yyyy-MM-dd)
     * @param endDate   结束日期 (可选，格式：yyyy-MM-dd)
     * @param startTime 开始时间（秒，可选）
     * @param endTime   结束时间（秒，可选）
     * @return 区域围栏数据
     * @author Shawn
     * @date 2025-01-15
     */
    @GetMapping("/getAreaFenceDataByIdCard")
    @ResponseBody
    @ApiOperation("根据身份证查询区域围栏数据")
    public Map<String, Object> getAreaFenceDataByIdCard(
            @ApiParam(value = "身份证号", required = true) @RequestParam String idCard,
            @ApiParam(value = "开始日期") @RequestParam(required = false) String startDate,
            @ApiParam(value = "结束日期") @RequestParam(required = false) String endDate,
            @ApiParam(value = "开始时间（秒）") @RequestParam(required = false) Integer startTime,
            @ApiParam(value = "结束时间（秒）") @RequestParam(required = false) Integer endTime) {

        Map<String, Object> result = new HashMap<>();

        try {
            logger.info("根据身份证查询区域围栏数据，身份证号: {}, 开始日期: {}, 结束日期: {}, 开始时间: {}, 结束时间: {}",
                    idCard, startDate, endDate, startTime, endTime);

            // 1. 根据身份证从Redis缓存中查找设备ID
            String deviceId = helmetCacheService.getAssignedDeviceFromCache(idCard, swmHelmetDeviceService);

            if (deviceId == null || deviceId.trim().isEmpty()) {
                result.put("success", false);
                result.put("message", "未找到身份证号 " + idCard + " 对应的设备ID");
                return result;
            }

            logger.info("身份证 {} 对应的设备ID: {}", idCard, deviceId);

            // 2. 查询area_fence_data表，使用完整的device_id进行匹配
            List<Map<String, Object>> areaFenceData = queryAreaFenceDataByDeviceId(deviceId, startDate, endDate,
                    startTime, endTime);

            result.put("success", true);
            result.put("data", areaFenceData);
            result.put("deviceId", deviceId);
            result.put("total", areaFenceData.size());
            result.put("message", "查询区域围栏数据成功");

        } catch (Exception e) {
            logger.error("根据身份证查询区域围栏数据失败，身份证号: {}", idCard, e);
            result.put("success", false);
            result.put("message", "查询区域围栏数据失败：" + e.getMessage());
        }

        return result;
    }

    /**
     * 提取设备ID的后8位数字
     * 从类似 "866652022415351" 的字符串中提取后8位 "22415351"
     * 
     * @param deviceId 设备ID
     * @return 后8位数字字符串，如果格式不正确则返回null
     */
    private String getLastEightDigits(String deviceId) {
        if (deviceId == null || deviceId.trim().isEmpty()) {
            return null;
        }

        // 移除所有非数字字符
        String digitsOnly = deviceId.replaceAll("[^0-9]", "");

        if (digitsOnly.length() < 8) {
            logger.warn("设备ID {} 提取的数字位数不足8位: {}", deviceId, digitsOnly);
            return null;
        }

        // 返回后8位
        return digitsOnly.substring(digitsOnly.length() - 8);
    }

    /**
     * 查询area_fence_data表，根据完整设备ID匹配并按area_name分组找出最早记录
     * 
     * @param deviceId      完整的设备ID
     * @param startDate     开始日期 (可选，格式：yyyy-MM-dd)
     * @param endDate       结束日期 (可选，格式：yyyy-MM-dd)
     * @param startTime     开始时间（秒，可选）
     * @param endTime       结束时间（秒，可选）
     * @return 区域围栏数据列表
     */
    private List<Map<String, Object>> queryAreaFenceDataByDeviceId(String deviceId, String startDate,
            String endDate, Integer startTime, Integer endTime) {
        List<Map<String, Object>> resultList = new ArrayList<>();

        try {
            // 构建SQL查询语句
            // device_id格式为 B0:8E:22:31:03:39，直接进行完全匹配
            StringBuilder sqlBuilder = new StringBuilder();
            sqlBuilder.append("SELECT time, area_name FROM ").append(dbname).append(".").append(TdengineSuperTableConstant.AREA_FENCE_DATA);
            sqlBuilder.append(" WHERE device_id = '").append(deviceId).append("' ");

            // 构建时间条件
            String timeCondition = buildTimeCondition(startDate, endDate, startTime, endTime);
            if (timeCondition != null && !timeCondition.trim().isEmpty()) {
                sqlBuilder.append("AND ").append(timeCondition).append(" ");
            } else {
                // 默认查询当天
                sqlBuilder.append("AND time >= TODAY() AND time < TODAY() + 1d ");
            }

            sqlBuilder.append("ORDER BY time ASC");

            String sql = sqlBuilder.toString();
            logger.info("查询area_fence_data的SQL: {}", sql);

            // 执行查询
            R<JSONObject> queryResult = tdengineService.executeTDengineSQL(sql);

            logger.info("TDengine查询结果 - 状态码: {}, 消息: {}", queryResult.getCode(), queryResult.getMsg());

            if (queryResult.getCode() == R.SUCCESS && queryResult.getData() != null) {
                JSONObject data = queryResult.getData();
                JSONArray rows = data.getJSONArray("data");
                JSONArray columnMeta = data.getJSONArray("column_meta");

                logger.info("TDengine返回数据结构 - rows: {}, columnMeta: {}",
                        rows != null ? rows.size() : "null",
                        columnMeta != null ? columnMeta.size() : "null");

                if (rows != null && rows.size() > 0) {
                    logger.info("查询到 {} 条区域围栏数据", rows.size());

                    // 使用Map来存储每个区域的最早记录
                    Map<String, Map<String, Object>> areaFirstRecordMap = new LinkedHashMap<>();

                    // 解析查询结果，由于已经按时间排序，第一次出现的区域就是最早的
                    for (int i = 0; i < rows.size(); i++) {
                        JSONArray row = rows.getJSONArray(i);
                        if (row != null && row.size() >= 2) {
                            String time = String.valueOf(row.get(0));
                            String areaName = String.valueOf(row.get(1));

                            // 转换UTC时间为北京时间
                            String beijingTime = convertUtcToBeijingTime(time);

                            // 如果这个区域还没有记录，则添加（因为已按时间排序，这就是最早的）
                            if (!areaFirstRecordMap.containsKey(areaName)) {
                                Map<String, Object> record = new HashMap<>();
                                record.put("time", beijingTime);
                                record.put("area_name", areaName);
                                areaFirstRecordMap.put(areaName, record);
                            }
                        }
                    }

//                    // 将结果转换为List，并按时间排序
                    resultList = new ArrayList<>(areaFirstRecordMap.values());
//                    resultList.sort((a, b) -> {
//                        String timeA = (String) a.get("time");
//                        String timeB = (String) b.get("time");
//                        return timeA.compareTo(timeB);
//                    });
                    resultList.sort(Comparator.comparing(o -> (String) o.get("time"), Comparator.reverseOrder()));

                    logger.info("处理后得到 {} 个区域的最早记录", resultList.size());
                } else {
                    logger.info("未找到匹配的区域围栏数据，设备ID: {}", deviceId);
                }
            } else {
                logger.error("查询area_fence_data失败: {}", queryResult.getMsg());
            }

        } catch (Exception e) {
            logger.error("查询area_fence_data异常，设备ID: {}", deviceId, e);
        }

        return resultList;
    }

    /**
     * 构建时间查询条件
     * 
     * @param startDate 开始日期 (格式：yyyy-MM-dd)
     * @param endDate   结束日期 (格式：yyyy-MM-dd)
     * @param startTime 开始时间（秒）
     * @param endTime   结束时间（秒）
     * @return 时间条件SQL片段
     */
    private String buildTimeCondition(String startDate, String endDate, Integer startTime, Integer endTime) {
        try {
            // 如果没有提供日期，返回null使用默认条件
            if (startDate == null || startDate.trim().isEmpty()) {
                return null;
            }

            // 如果没有提供结束日期，使用开始日期作为结束日期
            if (endDate == null || endDate.trim().isEmpty()) {
                endDate = startDate;
            }

            // 将秒数转换为时分秒格式
            String startTimeStr = "00:00:00";
            String endTimeStr = "23:59:59";

            if (startTime != null && startTime >= 0) {
                int hours = startTime / 3600;
                int minutes = (startTime % 3600) / 60;
                int seconds = startTime % 60;
                startTimeStr = String.format("%02d:%02d:%02d", hours, minutes, seconds);
            }

            if (endTime != null && endTime >= 0) {
                int hours = endTime / 3600;
                int minutes = (endTime % 3600) / 60;
                int seconds = endTime % 60;
                endTimeStr = String.format("%02d:%02d:%02d", hours, minutes, seconds);
            }

            // 构建时间条件
            String startDateTime = startDate + " " + startTimeStr;
            String endDateTime = endDate + " " + endTimeStr;

            return String.format("time >= '%s' AND time <= '%s'", startDateTime, endDateTime);

        } catch (Exception e) {
            logger.error("构建时间条件失败", e);
            return null;
        }
    }

    /**
     * 根据身份证查询区域围栏数据（精确到秒）
     *
     * @param idCard    身份证号
     * @param startDate 开始时间 (格式: yyyy-MM-dd HH:mm:ss)
     * @param endDate   结束时间 (格式: yyyy-MM-dd HH:mm:ss)
     * @return 区域围栏数据
     * @author Shawn
     * @date 2025/06/25
     */
    @GetMapping("/getAreaFenceDataByIdCardByDateTime")
    @ResponseBody
    @ApiOperation("根据身份证查询区域围栏数据（精确到秒）")
    public Map<String, Object> getAreaFenceDataByIdCardByDateTime(
            @ApiParam(value = "身份证号", required = true) @RequestParam String idCard,
            @ApiParam(value = "开始时间 (格式: yyyy-MM-dd HH:mm:ss)") @RequestParam(required = false) String startDate,
            @ApiParam(value = "结束时间 (格式: yyyy-MM-dd HH:mm:ss)") @RequestParam(required = false) String endDate) {

        Map<String, Object> result = new HashMap<>();

        try {
            // --- 解析时间参数 ---
            String parsedStartDate = null;
            String parsedEndDate = null;
            Integer startTime = null;
            Integer endTime = null;

            if (startDate != null && !startDate.trim().isEmpty()) {
                try {
                    LocalDateTime.parse(startDate, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                    parsedStartDate = startDate.substring(0, 10);
                    LocalTime localStartTime = LocalTime.parse(startDate.substring(11),
                            DateTimeFormatter.ofPattern("HH:mm:ss"));
                    startTime = localStartTime.toSecondOfDay();
                } catch (Exception e) {
                    logger.error("解析开始时间格式错误: {}", startDate, e);
                    result.put("success", false);
                    result.put("message", "开始时间格式错误，请使用 yyyy-MM-dd HH:mm:ss 格式。");
                    return result;
                }
            }

            if (endDate != null && !endDate.trim().isEmpty()) {
                try {
                    LocalDateTime.parse(endDate, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                    parsedEndDate = endDate.substring(0, 10);
                    LocalTime localEndTime = LocalTime.parse(endDate.substring(11),
                            DateTimeFormatter.ofPattern("HH:mm:ss"));
                    endTime = localEndTime.toSecondOfDay();
                } catch (Exception e) {
                    logger.error("解析结束时间格式错误: {}", endDate, e);
                    result.put("success", false);
                    result.put("message", "结束时间格式错误，请使用 yyyy-MM-dd HH:mm:ss 格式。");
                    return result;
                }
            }

            logger.info("根据身份证查询区域围栏数据，身份证号: {}, 开始日期: {}, 结束日期: {}, 开始时间: {}, 结束时间: {}",
                    idCard, parsedStartDate, parsedEndDate, startTime, endTime);

            // 1. 根据身份证从Redis缓存中查找设备ID
            String deviceId = helmetCacheService.getAssignedDeviceFromCache(idCard, swmHelmetDeviceService);

            if (deviceId == null || deviceId.trim().isEmpty()) {
                result.put("success", false);
                result.put("message", "未找到身份证号 " + idCard + " 对应的设备ID");
                return result;
            }

            logger.info("身份证 {} 对应的设备ID: {}", idCard, deviceId);

            // 2. 查询area_fence_data表，使用完整的device_id进行匹配
            List<Map<String, Object>> areaFenceData = queryAreaFenceDataByDeviceId(deviceId, parsedStartDate,
                    parsedEndDate,
                    startTime, endTime);

            result.put("success", true);
            result.put("data", areaFenceData);
            result.put("deviceId", deviceId);
            result.put("total", areaFenceData.size());
            result.put("message", "查询区域围栏数据成功");

        } catch (Exception e) {
            logger.error("根据身份证查询区域围栏数据失败，身份证号: {}", idCard, e);
            result.put("success", false);
            result.put("message", "查询区域围栏数据失败：" + e.getMessage());
        }

        return result;
    }

    /**
     * 将UTC时间转换为北京时间
     *
     * @param utcTimeStr UTC时间字符串
     * @return 北京时间字符串
     */
    private String convertUtcToBeijingTime(String utcTimeStr) {
        try {
            if (StringUtils.isBlank(utcTimeStr)) {
                return utcTimeStr;
            }

            // 处理不同的时间格式
            String normalizedTime = utcTimeStr;

            // 如果包含T，替换为空格
            if (normalizedTime.contains("T")) {
                normalizedTime = normalizedTime.replace("T", " ");
            }

            // 去掉毫秒部分和时区信息
            if (normalizedTime.contains(".")) {
                normalizedTime = normalizedTime.substring(0, normalizedTime.indexOf("."));
            }
            if (normalizedTime.contains("Z")) {
                normalizedTime = normalizedTime.replace("Z", "");
            }
            if (normalizedTime.contains("+")) {
                normalizedTime = normalizedTime.substring(0, normalizedTime.indexOf("+"));
            }

            // 解析UTC时间
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            LocalDateTime utcDateTime = LocalDateTime.parse(normalizedTime, formatter);

            // 转换为UTC时区的ZonedDateTime
            ZonedDateTime utcZoned = utcDateTime.atZone(ZoneId.of("UTC"));

            // 转换为北京时间
            ZonedDateTime beijingZoned = utcZoned.withZoneSameInstant(ZoneId.of("Asia/Shanghai"));

            // 格式化为字符串
            return beijingZoned.format(formatter);

        } catch (Exception e) {
            logger.warn("时间转换失败，使用原始时间: {}", utcTimeStr, e);
            return utcTimeStr;
        }
    }


    /**
     * 获取 MQTT 设备位置轨迹数据（精确到秒）
     *
     * @param syncId    人员 ID（可选）
     * @param deviceId  设备 ID（可选）
     * @param idCard    身份证号（可选）
     * @param name      人员名称（可选）
     * @param floorId   楼层 ID（可选）
     * @param startDate 开始时间 (格式：yyyy-MM-dd HH:mm:ss)
     * @param endDate   结束时间 (格式：yyyy-MM-dd HH:mm:ss)
     * @return 轨迹数据
     * @author Shawn
     * @date 2026/03/11
     */
    @GetMapping("/getMqttDevicePositionTrajectory")
    @ResponseBody
    @ApiOperation("获取 MQTT 设备位置轨迹数据")
    public Map<String, Object> getMqttDevicePositionTrajectory(
            @ApiParam(value = "人员 ID") @RequestParam(required = false) String syncId,
            @ApiParam(value = "设备 ID") @RequestParam(required = false) String deviceId,
            @ApiParam(value = "身份证号码") @RequestParam(required = false) String idCard,
            @ApiParam(value = "人员名称") @RequestParam(required = false) String name,
            @ApiParam(value = "楼层 ID") @RequestParam(required = false) String floorId,
            @ApiParam(value = "开始时间 (格式：yyyy-MM-dd HH:mm:ss)") @RequestParam(required = false) String startDate,
            @ApiParam(value = "结束时间 (格式：yyyy-MM-dd HH:mm:ss)") @RequestParam(required = false) String endDate) {

        Map<String, Object> result = new HashMap<>();

        try {
            // --- 解析时间参数 ---
            String parsedStartDate = null;
            String parsedEndDate = null;
            Integer startTime = null;
            Integer endTime = null;

            if (startDate != null && !startDate.trim().isEmpty()) {
                try {
                    LocalDateTime.parse(startDate, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                    parsedStartDate = startDate.substring(0, 10);
                    LocalTime localStartTime = LocalTime.parse(startDate.substring(11),
                            DateTimeFormatter.ofPattern("HH:mm:ss"));
                    startTime = localStartTime.toSecondOfDay();
                } catch (Exception e) {
                    logger.error("解析开始时间格式错误：{}", startDate, e);
                    result.put("success", false);
                    result.put("message", "开始时间格式错误，请使用 yyyy-MM-dd HH:mm:ss 格式。");
                    return result;
                }
            }

            if (endDate != null && !endDate.trim().isEmpty()) {
                try {
                    LocalDateTime.parse(endDate, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                    parsedEndDate = endDate.substring(0, 10);
                    LocalTime localEndTime = LocalTime.parse(endDate.substring(11),
                            DateTimeFormatter.ofPattern("HH:mm:ss"));
                    endTime = localEndTime.toSecondOfDay();
                } catch (Exception e) {
                    logger.error("解析结束时间格式错误：{}", endDate, e);
                    result.put("success", false);
                    result.put("message", "结束时间格式错误，请使用 yyyy-MM-dd HH:mm:ss 格式。");
                    return result;
                }
            }

            // --- 验证查询条件 ---
            if (StringUtils.isBlank(syncId) && StringUtils.isBlank(deviceId) &&
                    StringUtils.isBlank(idCard) && StringUtils.isBlank(name) &&
                    StringUtils.isBlank(floorId)) {
                result.put("success", false);
                result.put("message", "至少需要一个查询条件（syncId、deviceId、idCard、name、floorId）");
                return result;
            }

            // --- 查询轨迹数据 ---
            List<Map<String, Object>> trajectoryPoints = getMqttTrajectoryPoints(
                    syncId,
                    deviceId,
                    idCard,
                    name,
                    floorId,
                    parsedStartDate,
                    parsedEndDate,
                    startTime,
                    endTime);

            List<Map<String, Object>> timelineEvents = new ArrayList<>();

            Map<String, Object> data = new HashMap<>();
            logger.info("MQTT 轨迹点个数：{}", trajectoryPoints.size());
            data.put("trajectoryPoints", trajectoryPoints);
            data.put("timelineEvents", timelineEvents);

            result.put("success", true);
            result.put("data", data);
            result.put("message", "获取 MQTT 设备位置轨迹数据成功");

        } catch (Exception e) {
            logger.error("获取 MQTT 设备位置轨迹数据失败", e);
            result.put("success", false);
            result.put("message", "获取 MQTT 设备位置轨迹数据失败：" + e.getMessage());
        }

        return result;
    }

    /**
     * 获取 MQTT 轨迹点
     *
     * @param syncId     人员 ID
     * @param deviceId   设备 ID
     * @param idCard     身份证号
     * @param name       人员名称
     * @param floorId    楼层 ID
     * @param startDate  开始日期
     * @param endDate    结束日期
     * @param startTime  开始时间（秒数）
     * @param endTime    结束时间（秒数）
     * @return 轨迹点列表
     * @author Shawn
     * @date 2026/03/11
     */
    private List<Map<String, Object>> getMqttTrajectoryPoints(String syncId, String deviceId,
                                                              String idCard, String name, String floorId,
                                                              String startDate, String endDate, Integer startTime, Integer endTime) {

        List<Map<String, Object>> trajectoryPoints = new ArrayList<>();

        try {
            // 构建 SQL 查询条件
            StringBuilder sqlBuilder = new StringBuilder();
            sqlBuilder.append("SELECT time, lng, lat , name, ");
            sqlBuilder.append("phone_number, floor_id, id, sync_id, device_id, id_card ");
            sqlBuilder.append("FROM ").append(dbname).append(".").append(TdengineSuperTableConstant.MQTT_DEVICE_POSITION);

            List<String> conditions = new ArrayList<>();

            // 添加查询条件
            if (StringUtils.isNotBlank(syncId)) {
                conditions.add("sync_id = '" + syncId.trim().replace("'", "''") + "'");
            }
            if (StringUtils.isNotBlank(deviceId)) {
                conditions.add("device_id = '" + deviceId.trim().replace("'", "''") + "'");
            }
            if (StringUtils.isNotBlank(idCard)) {
                conditions.add("id_card = '" + idCard.trim().replace("'", "''") + "'");
            }
            if (StringUtils.isNotBlank(name)) {
                conditions.add("name = '" + name.trim().replace("'", "''") + "'");
            }
            if (StringUtils.isNotBlank(floorId)) {
                conditions.add("floor_id = '" + floorId.trim().replace("'", "''") + "'");
            }

            // 添加时间条件
            String timeCondition = buildTimeCondition(startDate, endDate, startTime, endTime);
            if (timeCondition != null && !timeCondition.trim().isEmpty()) {
                conditions.add(timeCondition);
            } else {
                // 默认查询当天
                conditions.add("time >= TODAY() AND time < TODAY() + 1d");
            }

            // 拼接 WHERE 子句
            if (!conditions.isEmpty()) {
                sqlBuilder.append(" WHERE ").append(String.join(" AND ", conditions)).append(" ");
            }

            sqlBuilder.append("ORDER BY time DESC");

            String sql = sqlBuilder.toString();
            logger.info("查询 mqtt_device_position 的 SQL: {}", sql);

            // 执行查询
            R<JSONObject> queryResult = tdengineService.executeTDengineSQL(sql);

            if (queryResult.getCode() == R.SUCCESS && queryResult.getData() != null) {
                JSONObject data = queryResult.getData();
                JSONArray rows = data.getJSONArray("data");

                if (rows != null && rows.size() > 0) {
                    logger.info("查询到 {} 条 MQTT 轨迹数据", rows.size());

                    // 解析查询结果
                    for (int i = 0; i < rows.size(); i++) {
                        JSONArray row = rows.getJSONArray(i);
                        if (row != null) {
                            Map<String, Object> point = new HashMap<>();

                            // time (索引 0)
                            String timeStr = String.valueOf(row.get(0));
                            String beijingTime = convertUtcToBeijingTime(timeStr);
                            point.put("time", beijingTime);

                            // lng (索引 1)
                            Object lngObj = row.get(1);
                            if (lngObj != null && !"null".equals(String.valueOf(lngObj))) {
                                try {
                                    point.put("lng", Double.parseDouble(String.valueOf(lngObj)));
                                } catch (NumberFormatException e) {
                                    point.put("lng", null);
                                }
                            } else {
                                point.put("lng", null);
                            }

                            // lat (索引 2)
                            Object latObj = row.get(2);
                            if (latObj != null && !"null".equals(String.valueOf(latObj))) {
                                try {
                                    point.put("lat", Double.parseDouble(String.valueOf(latObj)));
                                } catch (NumberFormatException e) {
                                    point.put("lat", null);
                                }
                            } else {
                                point.put("lat", null);
                            }

                            // name (索引 3)
                            point.put("name", String.valueOf(row.get(3)));

                            // phoneNumber (索引 4)
                            point.put("phoneNumber", String.valueOf(row.get(4)));

                            // floorId (索引 5)
                            point.put("floorId", String.valueOf(row.get(5)));

                            // id (索引 6)
                            Object idObj = row.get(6);
                            if (idObj != null) {
                                point.put("id", idObj);
                            } else {
                                point.put("id", null);
                            }

                            // syncId (索引 7)
                            point.put("syncId", String.valueOf(row.get(7)));

                            // deviceId (索引 8)
                            point.put("deviceId", String.valueOf(row.get(8)));

                            // idCard (索引 9)
                            point.put("idCard", String.valueOf(row.get(9)));

                            trajectoryPoints.add(point);
                        }
                    }

                    // 对轨迹点进行间隔抽样，减少数据量
                    int sampleInterval = 1;
                    try {
                        String intervalStr = DictUtils.getDictLabel("sample_trajectory_points", "time", "1");
                        sampleInterval = Integer.parseInt(intervalStr);
                        logger.info("从字典获取 MQTT 轨迹抽样间隔：{} 分钟", sampleInterval);
                    } catch (Exception e) {
                        logger.warn("获取轨迹抽样间隔失败，使用默认值：{} 分钟", sampleInterval);
                    }

                    List<Map<String, Object>> sampledPoints = sampleTrajectoryPoints(trajectoryPoints, sampleInterval);
                    logger.info("MQTT 原始轨迹点：{} 个，{}分钟间隔抽样后：{} 个",
                            trajectoryPoints.size(), sampleInterval, sampledPoints.size());
                    return sampledPoints;
                } else {
                    logger.info("未找到匹配的 MQTT 轨迹数据");
                }
            } else {
                logger.error("查询 mqtt_device_position 失败：{}", queryResult.getMsg());
            }

        } catch (Exception e) {
            logger.error("获取 MQTT 轨迹点失败", e);
        }

        return trajectoryPoints;
    }

}