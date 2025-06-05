package com.jeesite.modules.swm.web;

import com.jeesite.common.web.BaseController;
import com.jeesite.modules.swm.service.PersonTrackService;
import com.jeesite.modules.swm.service.impl.HelmetRundeCaReportLocationTdEnginServiceImpl;
import com.jeesite.modules.utils.R;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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
    private HelmetRundeCaReportLocationTdEnginServiceImpl helmetLocationService;

    private Random random = new Random();

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
            @ApiParam(value = "展示类型") @RequestParam(required = false) String displayType) {

        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> positions = new ArrayList<>();

        try {
            // 从数据库查询人员数据
            List<Map<String, Object>> allPositions = queryPersonsFromDatabase(searchName, organizationKey);

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
    private List<Map<String, Object>> queryPersonsFromDatabase(String searchName, String organizationKey) {
        // 使用Service层处理查询逻辑
        return personTrackService.queryPersonsFromDatabase(searchName, organizationKey);
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
        return getPersonPositions(null, name, null);
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
            @ApiParam(value = "人员ID") @RequestParam(required = false) Integer personId,
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

            // 生成轨迹点（优先使用TDengine真实数据，备用随机数据）
            List<Map<String, Object>> trajectoryPoints = generateRandomTrajectoryPoints(
                    personId != null ? personId : (Integer) personInfo.get("id"),
                    personName,
                    workType,
                    organization,
                    workShop,
                    teamGroup,
                    idCard);

            // 构建时间线事件数据
            List<Map<String, Object>> timelineEvents = Arrays.asList(
                    createTimelineEvent("2025-01-01 09:52:34", personName, "进入厂区", 1),
                    createTimelineEvent("2025-01-01 10:15:20", personName, "开始工作", 1),
                    createTimelineEvent("2025-01-01 12:00:00", personName, "午休时间", 1),
                    createTimelineEvent("2025-01-01 13:30:15", personName, "继续工作", 1),
                    createTimelineEvent("2025-01-01 15:45:56", personName, "接近危险源", 3),
                    createTimelineEvent("2025-01-01 17:00:00", personName, "结束工作", 1),
                    createTimelineEvent("2025-01-01 17:30:00", personName, "离开厂区", 1));

            Map<String, Object> data = new HashMap<>();
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
     * 生成轨迹点（优先使用TDengine真实数据，备用随机数据）
     * 
     * @param personId     人员ID
     * @param personName   人员姓名
     * @param workType     工种
     * @param organization 组织
     * @param workShop     车间
     * @param teamGroup    班组
     * @param idCard       身份证号
     * @return 轨迹点列表
     * @author Shawn
     * @date 2025-01-14
     */
    private List<Map<String, Object>> generateRandomTrajectoryPoints(Integer personId, String personName,
            String workType, String organization, String workShop, String teamGroup, String idCard) {

        List<Map<String, Object>> trajectoryPoints = new ArrayList<>();

        // 首先尝试从TDengine获取真实轨迹数据
        if (idCard != null && !idCard.trim().isEmpty()) {
            try {
                R<List<Map<String, Object>>> trajectoryResult = helmetLocationService
                        .getTodayTrajectoryByIdCard(idCard);
                if (trajectoryResult.getCode() == R.SUCCESS && trajectoryResult.getData() != null) {
                    List<Map<String, Object>> realTrajectory = trajectoryResult.getData();

                    if (!realTrajectory.isEmpty()) {
                        logger.info("身份证 {} ({}) 获取到 {} 个真实轨迹点", idCard, personName, realTrajectory.size());

                        // 将TDengine数据转换为前端需要的格式
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
                                            personId,
                                            personName,
                                            x,
                                            y,
                                            workType != null ? workType : "待分配",
                                            organization != null ? organization : "未知单位",
                                            workShop != null ? workShop : "未知车间",
                                            teamGroup != null ? teamGroup : "未知班组",
                                            "8小时",
                                            "正常考勤",
                                            idCard);

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
                            logger.info("身份证 {} ({}) 成功转换 {} 个轨迹点", idCard, personName, trajectoryPoints.size());
                            return trajectoryPoints;
                        }
                    }
                }

                logger.info("身份证 {} ({}) 未找到轨迹数据，使用随机轨迹", idCard, personName);
            } catch (Exception e) {
                logger.error("获取身份证 {} ({}) 轨迹数据异常，使用随机轨迹", idCard, personName, e);
            }
        }

        // 如果没有获取到真实数据，使用随机轨迹作为备用方案
        logger.info("为身份证 {} ({}) 生成随机轨迹数据", idCard, personName);
        return generateRandomTrajectoryPointsBackup(personId, personName, workType, organization, workShop, teamGroup,
                idCard);
    }

    /**
     * 生成随机轨迹点（备用方案）
     * 
     * @param personId     人员ID
     * @param personName   人员姓名
     * @param workType     工种
     * @param organization 组织
     * @param workShop     车间
     * @param teamGroup    班组
     * @param idCard       身份证号
     * @return 轨迹点列表
     * @author Shawn
     * @date 2025-01-14
     */
    private List<Map<String, Object>> generateRandomTrajectoryPointsBackup(Integer personId, String personName,
            String workType, String organization, String workShop, String teamGroup, String idCard) {

        List<Map<String, Object>> trajectoryPoints = new ArrayList<>();

        // 生成8个随机轨迹点，模拟一天的移动路径
        int startX = random.nextInt(500) + 100; // 起始点
        int startY = random.nextInt(300) + 100;

        for (int i = 0; i < 8; i++) {
            // 每个点在前一个点的附近随机生成，模拟连续移动
            int deltaX = random.nextInt(400) - 200; // -200到200的随机偏移
            int deltaY = random.nextInt(400) - 200;

            int x = Math.max(50, Math.min(2500, startX + deltaX));
            int y = Math.max(50, Math.min(1100, startY + deltaY));

            // 更新起始点为当前点，用于下一个点的生成
            startX = x;
            startY = y;

            Map<String, Object> trajectoryPoint = createPersonPosition(
                    personId,
                    personName,
                    x,
                    y,
                    workType != null ? workType : "待分配",
                    organization != null ? organization : "未知单位",
                    workShop != null ? workShop : "未知车间",
                    teamGroup != null ? teamGroup : "未知班组",
                    "8小时",
                    "正常考勤",
                    idCard);

            // 标记为随机位置
            trajectoryPoint.put("hasRealLocation", false);

            trajectoryPoints.add(trajectoryPoint);
        }

        return trajectoryPoints;
    }

    /**
     * 创建人员位置对象
     */
    private Map<String, Object> createPersonPosition(Integer id, String name, int x, int y,
            String workType, String organization, String workShop,
            String teamGroup, String workHours, String attendanceStatus, String idCard) {

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

        return position;
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
}