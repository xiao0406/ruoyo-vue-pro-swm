package com.jeesite.modules.swm.service;

import com.jeesite.common.service.CrudService;
import com.jeesite.modules.swm.dao.PersonTrackDao;
import com.jeesite.modules.swm.entity.PersonTrackInfo;
import com.jeesite.modules.swm.service.impl.HelmetRundeCaReportLocationTdEnginServiceImpl;

import com.jeesite.modules.utils.R;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

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
    private HelmetRundeCaReportLocationTdEnginServiceImpl helmetLocationService;

    /**
     * 从数据库查询人员数据并转换为位置信息
     * 
     * @param searchName      搜索人员姓名
     * @param organizationKey 组织节点key
     * @return 人员位置数组
     * @author Shawn
     * @date 2025-01-14
     */
    public List<Map<String, Object>> queryPersonsFromDatabase(String searchName, String organizationKey) {
        List<Map<String, Object>> positions = new ArrayList<>();

        try {
            // 使用MyBatis查询数据
            List<PersonTrackInfo> dbResults = personTrackDao.findPersonTrackInfo(searchName, organizationKey);

            if (dbResults.isEmpty()) {
                logger.info("未查询到人员数据，searchName: {}, organizationKey: {}", searchName, organizationKey);
                return positions;
            }

            // 收集所有身份证号，用于批量查询坐标
            List<String> idCardList = new ArrayList<>();
            for (PersonTrackInfo person : dbResults) {
                String identityCard = person.getIdentityCard();
                if (identityCard != null && !identityCard.trim().isEmpty()) {
                    idCardList.add(identityCard);
                }
            }

            // 批量查询TDengine中的坐标数据
            Map<String, Map<String, Object>> locationMap = new HashMap<>();
            if (!idCardList.isEmpty()) {
                try {
                    R<Map<String, Map<String, Object>>> locationResult = helmetLocationService
                            .getLatestLocationsByIdCards(idCardList);
                    if (locationResult.getCode() == R.SUCCESS) {
                        locationMap = locationResult.getData();
                        logger.info("从TDengine查询到 {} 个身份证的坐标数据", locationMap.size());
                    } else {
                        logger.warn("查询TDengine坐标数据失败: {}", locationResult.getMsg());
                    }
                } catch (Exception e) {
                    logger.error("查询TDengine坐标数据异常", e);
                }
            }

            // 转换数据库结果为人员位置数据
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

                Integer personId = 0;
                if (id != null) {
                    try {
                        personId = Integer.parseInt(id);
                    } catch (NumberFormatException e) {
                        personId = 0; // 默认值
                    }
                }

                // 检查是否在TDengine中找到了坐标数据
                if (identityCard != null && !identityCard.trim().isEmpty() && locationMap.containsKey(identityCard)) {
                    Map<String, Object> locationInfo = locationMap.get(identityCard);
                    Object xObj = locationInfo.get("x");
                    Object yObj = locationInfo.get("y");

                    if (xObj != null && yObj != null) {
                        try {
                            // 将TDengine中的坐标转换为整数
                            int x = (int) Math.round(Double.parseDouble(xObj.toString()));
                            int y = (int) Math.round(Double.parseDouble(yObj.toString()));

                            Map<String, Object> position = createPersonPosition(
                                    personId,
                                    name,
                                    x,
                                    y,
                                    workType,
                                    organization,
                                    workShop,
                                    teamGroup,
                                    "8小时", // 工作时长默认值
                                    "正常考勤", // 考勤状态默认值
                                    identityCard,
                                    true); // 标记为真实位置

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
    private Map<String, Object> createPersonPosition(Integer id, String name, int x, int y, String workType,
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
    private Map<String, Object> createPersonPosition(Integer id, String name, int x, int y, String workType,
            String organization, String workShop, String teamGroup,
            String workHours, String attendance, String identityCard) {
        return createPersonPosition(id, name, x, y, workType, organization, workShop, teamGroup,
                workHours, attendance, identityCard, false);
    }
}