/**
 * 危险源信息Controller
 * @author Shawn
 * @version 2025-05-20
 */
package com.jeesite.modules.swm.web;

import com.jeesite.common.config.Global;
import com.jeesite.common.entity.Page;
import com.jeesite.common.web.BaseController;
import com.jeesite.modules.config.TenantContext;
import com.jeesite.modules.entity.SwmBeaconStation;
import com.jeesite.modules.entity.SwmHazardSource;
import com.jeesite.modules.swm.cache.SwmHazardSourceCache;
import com.jeesite.modules.swm.entity.*;
import com.jeesite.modules.swm.service.*;
import com.jeesite.modules.sys.entity.DictData;
import com.jeesite.modules.sys.utils.DictUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 危险源信息Controller
 */
@Controller
@RequestMapping(value = "${adminPath}/swmHazardSource")
@Api(value = "危险源管理接口")
public class SwmHazardSourceController extends BaseController {

    @Autowired
    private SwmHazardSourceService swmHazardSourceService;
    @Autowired
    private SwmInspectionPlanService swmInspectionPlanService;
    @Autowired
    private SwmPersonService swmPersonService;
    @Autowired
    private SwmVoiceTemplateService swmVoiceTemplateService;
    @Autowired
    private SwmBeaconStationService swmBeaconStationService; // 注入信标服务

    @Autowired
    private SwmHelmetDeviceService swmHelmetDeviceService;

    @Autowired
    private SwmHazardSourceCache hazardSourceCache;

    /**
     * 获取数据
     */
    @ModelAttribute
    public SwmHazardSource get(String id, boolean isNewRecord) {
        return swmHazardSourceService.get(id, isNewRecord);
    }

    /**
     * 查询列表
     */
    @RequestMapping(value = { "list", "" })
    public String list(SwmHazardSource swmHazardSource, Model model) {
        model.addAttribute("swmHazardSource", swmHazardSource);
        return "modules/swm/swmHazardSourceList";
    }

    /**
     * 查询列表数据
     */
    @RequestMapping(value = "listData")
    @ResponseBody
    @ApiOperation(value = "获取危险源列表")
    public Page<SwmHazardSource> listData(SwmHazardSource swmHazardSource, HttpServletRequest request,
            HttpServletResponse response) {
        // 处理多信标搜索
        if (swmHazardSource.getBeaconIdentifier() != null && swmHazardSource.getBeaconIdentifier().contains(",")) {
            // 将逗号分隔的搜索条件转换为列表
            String[] searchTerms = swmHazardSource.getBeaconIdentifier().split(",");
            List<String> searchList = new ArrayList<>();
            for (String term : searchTerms) {
                String trimmed = term.trim();
                if (!trimmed.isEmpty()) {
                    searchList.add(trimmed);
                }
            }
            swmHazardSource.setBeaconIdentifierSearchList(searchList);
            // 清空单个搜索条件，避免重复查询
            swmHazardSource.setBeaconIdentifier(null);
        }

        swmHazardSource.setPage(new Page<>(request, response));
        Page<SwmHazardSource> page = swmHazardSourceService.findPage(swmHazardSource);

        // 收集所有需要查询的ID
        Set<String> voiceTemplateIds = new HashSet<>();
        Set<String> beaconIds = new HashSet<>();

        // 第一次遍历，收集所有ID
        for (SwmHazardSource item : page.getList()) {
            // 收集语音模板ID
            if (item.getVoiceTemplateId() != null && !item.getVoiceTemplateId().isEmpty()) {
                voiceTemplateIds.add(item.getVoiceTemplateId());
            }

            // 收集信标ID
            if (item.getBeaconIdentifier() != null && !item.getBeaconIdentifier().isEmpty()) {
                String[] ids = item.getBeaconIdentifier().split(",");
                for (String id : ids) {
                    if (!id.trim().isEmpty()) {
                        beaconIds.add(id.trim());
                    }
                }
            }
        }

        // 批量查询语音模板
        Map<String, SwmVoiceTemplate> voiceTemplateMap = new HashMap<>();
        if (!voiceTemplateIds.isEmpty()) {
            List<SwmVoiceTemplate> voiceTemplates = swmVoiceTemplateService
                    .findByIds(new ArrayList<>(voiceTemplateIds));
            for (SwmVoiceTemplate template : voiceTemplates) {
                voiceTemplateMap.put(template.getId(), template);
            }
        }

        // 批量查询信标
        Map<String, Map<String, Object>> beaconMap = new HashMap<>();
        if (!beaconIds.isEmpty()) {
            List<SwmBeaconStation> beacons = swmBeaconStationService.findByBeaconIds(new ArrayList<>(beaconIds));
            for (SwmBeaconStation beacon : beacons) {
                Map<String, Object> beaconInfo = new HashMap<>();
                beaconInfo.put("id", beacon.getId());
                beaconInfo.put("beaconId", beacon.getBeaconId());

                // 如果设备名称为空，则使用MAC地址作为设备名称
                String deviceName = beacon.getDeviceName();
                if (deviceName == null || deviceName.trim().isEmpty() || "null".equals(deviceName)) {
                    deviceName = beacon.getBeaconId();
                }
                beaconInfo.put("deviceName", deviceName);
                beaconInfo.put("location", beacon.getLocation());
                beaconMap.put(beacon.getBeaconId(), beaconInfo);
            }
        }

        // 手动处理字典数据
        for (SwmHazardSource item : page.getList()) {
            // 危险源类别
            if ("0".equals(item.getHazardCategory())) {
                item.setHazardCategoryText("气站");
            } else if ("1".equals(item.getHazardCategory())) {
                item.setHazardCategoryText("吊钩");
            } else if ("2".equals(item.getHazardCategory())) {
                item.setHazardCategoryText("油漆库");
            } else if ("3".equals(item.getHazardCategory())) {
                item.setHazardCategoryText("空压机房");
            } else if ("4".equals(item.getHazardCategory())) {
                item.setHazardCategoryText("机械伤害风险");
            } else if ("5".equals(item.getHazardCategory())) {
                item.setHazardCategoryText("化学品泄漏风险");
            } else if ("99".equals(item.getHazardCategory())) {
                item.setHazardCategoryText("其他风险");
            }

            // 是否加入巡检
            if ("0".equals(item.getIsPatrolIncluded())) {
                item.setIsPatrolIncludedText("否");
            } else if ("1".equals(item.getIsPatrolIncluded())) {
                item.setIsPatrolIncludedText("是");
            }

            // 危险源状态
            if ("0".equals(item.getHazardStatus())) {
                item.setHazardStatusText("待处理");
            } else if ("1".equals(item.getHazardStatus())) {
                item.setHazardStatusText("处理中");
            } else if ("2".equals(item.getHazardStatus())) {
                item.setHazardStatusText("已关闭");
            } else if ("3".equals(item.getHazardStatus())) {
                item.setHazardStatusText("已忽略");
            }

            // 语音模板名称 - 从Map中获取
            if (item.getVoiceTemplateId() != null && !item.getVoiceTemplateId().isEmpty()) {
                SwmVoiceTemplate voiceTemplate = voiceTemplateMap.get(item.getVoiceTemplateId());
                if (voiceTemplate != null) {
                    item.setVoiceTemplateText(voiceTemplate.getTemplateName());
                }
            }

            // 处理多个信标显示 - 从Map中获取
            if (item.getBeaconIdentifier() != null && !item.getBeaconIdentifier().isEmpty()) {
                // 将逗号分隔的beaconIdentifier拆分为数组
                String[] ids = item.getBeaconIdentifier().split(",");
                StringBuilder beaconText = new StringBuilder();

                // 查询每个信标的名称
                for (int i = 0; i < ids.length; i++) {
                    String beaconId = ids[i].trim();
                    if (beaconId.isEmpty())
                        continue;

                    // 从Map中获取信标信息
                    Map<String, Object> beacon = beaconMap.get(beaconId);
                    if (beacon != null) {
                        if (beaconText.length() > 0) {
                            beaconText.append(", ");
                        }
                        // 使用之前缓存的deviceName
                        String deviceName = (String) beacon.get("deviceName");
                        beaconText.append(deviceName);
                    } else {
                        // 如果没有找到信标信息，直接使用MAC地址
                        if (beaconText.length() > 0) {
                            beaconText.append(", ");
                        }
                        beaconText.append(beaconId);
                    }
                }

                item.setBeaconIdentifierText(beaconText.toString());
            } else {
                // 如果没有信标ID，设置为"-"
                item.setBeaconIdentifierText("-");
            }

            // 处理草稿状态，如果isDraft为null，默认设为0
            if (item.getIsDraft() == null) {
                item.setIsDraft("0");
            }
        }

        return page;
    }

    /**
     * 查看编辑表单
     */
    @RequestMapping(value = "form")
    @ResponseBody
    @ApiOperation(value = "获取危险源详情")
    public Map<String, Object> form(SwmHazardSource swmHazardSource) {
        Map<String, Object> result = new HashMap<>();
        if (swmHazardSource != null) {
            Map<String, Object> data = new HashMap<>();
            // 复制基本属性
            data.put("id", swmHazardSource.getId());
            data.put("hazardName", swmHazardSource.getHazardName());
            data.put("hazardCategory", swmHazardSource.getHazardCategory());
            data.put("location", swmHazardSource.getLocation());
            data.put("beaconIdentifier", swmHazardSource.getBeaconIdentifier());
            data.put("isPatrolIncluded", swmHazardSource.getIsPatrolIncluded());
            data.put("patrolRecordSummary", swmHazardSource.getPatrolRecordSummary());
            data.put("registrationTime", swmHazardSource.getRegistrationTime());
            data.put("hazardStatus", swmHazardSource.getHazardStatus());
            data.put("voiceTemplateId", swmHazardSource.getVoiceTemplateId());
            data.put("beaconTag", swmHazardSource.getBeaconTag());
            data.put("remarks", swmHazardSource.getRemarks());
            data.put("frequencyDays", swmHazardSource.getFrequencyDays());
            data.put("responsiblePersonId", swmHazardSource.getResponsiblePersonId());
            data.put("firstInspectionTime", swmHazardSource.getFirstInspectionTime());
            data.put("isDraft", swmHazardSource.getIsDraft());

            // 手动设置字典文本
            // 危险源类别
            if ("0".equals(swmHazardSource.getHazardCategory())) {
                data.put("hazardCategoryText", "火灾风险");
            } else if ("1".equals(swmHazardSource.getHazardCategory())) {
                data.put("hazardCategoryText", "高处坠落风险");
            } else if ("2".equals(swmHazardSource.getHazardCategory())) {
                data.put("hazardCategoryText", "物体打击风险");
            } else if ("3".equals(swmHazardSource.getHazardCategory())) {
                data.put("hazardCategoryText", "触电风险");
            } else if ("4".equals(swmHazardSource.getHazardCategory())) {
                data.put("hazardCategoryText", "机械伤害风险");
            } else if ("5".equals(swmHazardSource.getHazardCategory())) {
                data.put("hazardCategoryText", "化学品泄漏风险");
            } else if ("99".equals(swmHazardSource.getHazardCategory())) {
                data.put("hazardCategoryText", "其他风险");
            }

            // 是否加入巡检
            if ("0".equals(swmHazardSource.getIsPatrolIncluded())) {
                data.put("isPatrolIncludedText", "否");
            } else if ("1".equals(swmHazardSource.getIsPatrolIncluded())) {
                data.put("isPatrolIncludedText", "是");
            }

            // 危险源状态
            if ("0".equals(swmHazardSource.getHazardStatus())) {
                data.put("hazardStatusText", "待处理");
            } else if ("1".equals(swmHazardSource.getHazardStatus())) {
                data.put("hazardStatusText", "处理中");
            } else if ("2".equals(swmHazardSource.getHazardStatus())) {
                data.put("hazardStatusText", "已关闭");
            } else if ("3".equals(swmHazardSource.getHazardStatus())) {
                data.put("hazardStatusText", "已忽略");
            }

            // 语音模板名称
            if (swmHazardSource.getVoiceTemplateId() != null && !swmHazardSource.getVoiceTemplateId().isEmpty()) {
                SwmVoiceTemplate voiceTemplate = swmVoiceTemplateService.get(swmHazardSource.getVoiceTemplateId());
                if (voiceTemplate != null) {
                    data.put("voiceTemplateText", voiceTemplate.getTemplateName());
                }
            }

            //白名单人员
            data.put("filterIdentityCard", swmHazardSource.getFilterIdentityCard());
            data.put("filterPersonnel", swmHazardSource.getFilterPersonnel());

            result.putAll(data);
        }
        return result;
    }

    /**
     * 保存危险源
     */
    @PostMapping(value = "save")
    @ResponseBody
    @ApiOperation(value = "保存危险源")
    public String save(@Validated SwmHazardSource swmHazardSource) {
        // 如果不是暂存，则默认设置isDraft为0
        if (swmHazardSource.getIsDraft() == null) {
            swmHazardSource.setIsDraft("0");
        }

        if (swmHazardSource.getResponsiblePersonId() != null) {
            SwmPerson swmPerson = swmPersonService.get(swmHazardSource.getResponsiblePersonId());
            if (swmPerson == null) {
                // return renderResult(Global.FALSE, text("巡检负责人不存在！"));
            } else {
                swmHazardSource.setResponsiblePerson(swmPerson.getName());
            }
        }

        // 保存危险源信息
        swmHazardSourceService.save(swmHazardSource);

        // 判断这个危险源是否有报警标识，存入redis,永不过期
        String filterIdentityCard = swmHazardSource.getFilterIdentityCard();
        if (StringUtils.isNotBlank(filterIdentityCard)) {
            //插入缓存
            hazardSourceCache.insertHazardSourceCache(swmHazardSource.getId(), filterIdentityCard);
        }

        // 危险源反向缓存，用mac地址作为key，value为危险源信息，用于iot服务的危险源报警
        hazardSourceCache.insertHazardInfoCache(swmHazardSource, TenantContext.get());

        // 如果设置为加入巡检且不是草稿状态
        if ("1".equals(swmHazardSource.getIsPatrolIncluded()) && !"1".equals(swmHazardSource.getIsDraft())) {
            // 判断必要字段是否为空
            if (swmHazardSource.getFirstInspectionTime() == null || swmHazardSource.getFrequencyDays() == null
                    || swmHazardSource.getResponsiblePersonId() == null) {
                return renderResult(Global.TRUE, text("保存成功，但无法生成巡检计划，请补充巡检相关信息！"));
            }

            // 先查询是否已存在该危险源关联的巡检计划
            try {
                SwmInspectionPlan queryPlan = new SwmInspectionPlan();
                queryPlan.setHazardSourceId(swmHazardSource.getId());
                List<SwmInspectionPlan> existingPlans = swmInspectionPlanService.findList(queryPlan);

                SwmInspectionPlan swmInspectionPlan;
                boolean isNewPlan = true;

                if (existingPlans != null && !existingPlans.isEmpty()) {
                    // 如果已存在计划，则更新第一个找到的计划
                    swmInspectionPlan = existingPlans.get(0);
                    isNewPlan = false;
                } else {
                    // 不存在计划，创建新的
                    swmInspectionPlan = new SwmInspectionPlan();
                }

                // 设置或更新计划信息
                swmInspectionPlan.setPlanName(swmHazardSource.getHazardName());
                swmInspectionPlan.setFrequencyDays(swmHazardSource.getFrequencyDays());
                // 危险源巡检
                swmInspectionPlan.setInspectionType("3");
                swmInspectionPlan.setHazardSourceId(swmHazardSource.getId());
                swmInspectionPlan.setHazardSourceName(swmHazardSource.getHazardName());
                swmInspectionPlan.setFirstInspectionTime(swmHazardSource.getFirstInspectionTime());
                swmInspectionPlan.setResponsiblePersonId(swmHazardSource.getResponsiblePersonId());
                swmInspectionPlan.setResponsiblePerson(swmHazardSource.getResponsiblePerson());
                swmInspectionPlan.setPlanStatus(SwmInspectionPlan.PlanStatusEnum.OPEN); // 默认为开启状态

                swmInspectionPlanService.save(swmInspectionPlan);

                if (isNewPlan) {
                    return renderResult(Global.TRUE, text("保存危险源并生成巡检计划成功"));
                } else {
                    return renderResult(Global.TRUE, text("保存危险源并更新巡检计划成功"));
                }
            } catch (Exception e) {
                logger.error("生成或更新巡检计划失败", e);
                return renderResult(Global.TRUE, text("保存危险源成功，但生成或更新巡检计划失败：" + e.getMessage()));
            }
        }

        return renderResult(Global.TRUE, text("保存危险源成功"));
    }

    /**
     * 删除危险源信息
     */
    @RequestMapping(value = "delete")
    @ResponseBody
    @ApiOperation(value = "删除危险源")
    public String delete(SwmHazardSource swmHazardSource) {
        swmHazardSourceService.delete(swmHazardSource);

        //删除缓存
        hazardSourceCache.deleteHazardSourceCache(swmHazardSource, TenantContext.get());
        // 危险源反向缓存，用mac地址作为key，value为危险源信息，用于iot服务的危险源报警
        hazardSourceCache.deleteHazardInfoCache(swmHazardSource, TenantContext.get());
        return renderResult(Global.TRUE, text("删除危险源信息成功！"));
    }

    /**
     * 根据MAC地址查询危险源
     */
    @GetMapping("findByBeacon")
    @ResponseBody
    @ApiOperation(value = "根据MAC地址查询危险源")
    public SwmHazardSource findByBeacon(String beaconIdentifier) {
        SwmHazardSource hazardSource = new SwmHazardSource();
        hazardSource.setBeaconIdentifier(beaconIdentifier);
        return swmHazardSourceService.findList(hazardSource).isEmpty() ? null
                : swmHazardSourceService.findList(hazardSource).get(0);
    }

    /**
     * 暂存危险源
     */
    @PostMapping(value = "tempSave", consumes = "application/json")
    @ResponseBody
    @ApiOperation(value = "暂存危险源")
    public String tempSave(@RequestBody SwmHazardSource swmHazardSource) {
        // 标记为暂存状态
        swmHazardSource.setHazardStatus(SwmHazardSource.HazardSourceStatusEnum.WAIT);
        swmHazardSource.setIsDraft("1"); // 标记为草稿状态

        // 保存数据
        swmHazardSourceService.save(swmHazardSource);

        return renderResult(Global.TRUE, text("危险源暂存成功"));
    }

    /**
     * 热力图-危险源总数趋势
     * 
     * @param beginDate 开始时间
     * @param endDate   结束时间
     * @return
     */
    @GetMapping("hazardSourceStatistics")
    @ResponseBody
    @ApiOperation(value = "热力图-危险源总数趋势")
    public Map<String, Object> hazardSourceStatistics(@RequestParam(required = false) String beginDate,
            @RequestParam(required = false) String endDate) {

        Map<String, Object> result = new HashMap<>();
        List<DictData> hazardCategoryList = DictUtils.getDictList("hazard_category_enum");
        // 创建字典值(dictValue)到标签(dictLabelRaw)的映射
        Map<String, String> valueToLabelMap = hazardCategoryList.stream()
                .collect(Collectors.toMap(
                        DictData::getDictValue,
                        DictData::getDictLabelRaw,
                        (existing, replacement) -> existing)); // 如果有重复键，保留已存在的

        // 1. 危险源总数趋势折线图
        result.put("totalTrend", getHazardSourceTotalTrend(beginDate, endDate));

        // 2. 危险源类别TOP 10 柱状图
        result.put("categoryTop10", getHazardSourceCategoryTop10(beginDate, endDate, valueToLabelMap));

        // 3. 危险源类别分布饼图
        result.put("categoryDistribution", getHazardSourceCategoryDistribution(beginDate, endDate, valueToLabelMap));

        // 4. 危险源类别趋势折线图
        result.put("categoryTrend", getHazardSourceCategoryTrend(beginDate, endDate, valueToLabelMap));

        return result;
    }

    @GetMapping("hazardSourceCounts")
    @ResponseBody
    @ApiOperation(value = "热力图-危险源数量统计-(中间4个数量)")
    public Map<String, Object> getHazardSourceCounts(
            @RequestParam(required = false) String beginDate,
            @RequestParam(required = false) String endDate) {

        // 处理日期范围（默认近30天）
        DateRange dateRange = parseDateRange(beginDate, endDate);

        Map<String, Object> result = new HashMap<>();
        // 危险源累计总数
        int totalCount = swmHazardSourceService.countAll();

        // 1. 危险源总数
        int rangeCount = swmHazardSourceService.countByDateRange(
                dateRange.getBeginDate(),
                dateRange.getEndDate(),
                null); // 不限制状态

        // 2. 未关闭危险源数量（待处理+处理中）
        int unclosedCount = swmHazardSourceService.countByDateRange(
                dateRange.getBeginDate(),
                dateRange.getEndDate(),
                Arrays.asList(SwmHazardSource.HazardSourceStatusEnum.WAIT,
                        SwmHazardSource.HazardSourceStatusEnum.IN_PROGRESS));

        // 3. 已关闭危险源数量（总数 - 未关闭的）
        int closedCount = rangeCount - unclosedCount;

        // 4. 危险源整改率（保留2位小数）
        double rectificationRate = rangeCount > 0 ? Math.round(closedCount * 10000.0 / rangeCount) / 100.0 : 0;

        // 5. 未制定巡检计划的数量
        int noInspectionPlanCount = swmHazardSourceService.countNoInspectionPlan(
                dateRange.getBeginDate(),
                dateRange.getEndDate());

        // 返回结果
        result.put("totalCount", totalCount); // 危险源总数累计
        result.put("rangeCount", totalCount); // 危险源总数
        result.put("unclosedCount", unclosedCount); // 未关闭的
        result.put("closedCount", closedCount); // 已整改的
        result.put("rectificationRate", rectificationRate);// 危险源整改率
        result.put("noInspectionPlanCount", noInspectionPlanCount);// 未制定巡检计划的数量

        return result;
    }

    /**
     * 获取危险源总数趋势数据
     */
    private List<Map<String, Object>> getHazardSourceTotalTrend(String beginDateStr, String endDateStr) {
        // 解析日期参数
        DateRange dateRange = parseDateRange(beginDateStr, endDateStr);

        // 获取日期列表
        List<String> dateList = getDateList(dateRange.getBeginDate(), dateRange.getEndDate());

        // 一次性查询所有数据
        List<Map<String, Object>> dbResults = swmHazardSourceService.countByDateRangeGroupByDay(
                dateRange.getBeginDate(),
                dateRange.getEndDate());

        // 转换为按日期索引的Map
        Map<String, Integer> countMap = dbResults.stream()
                .collect(Collectors.toMap(
                        item -> (String) item.get("date"),
                        item -> ((Number) item.get("count")).intValue()));

        // 构建返回结果 - 使用显式类型声明
        List<Map<String, Object>> result = new ArrayList<>();
        for (String date : dateList) {
            Map<String, Object> item = new HashMap<>();
            item.put("date", date);
            item.put("count", countMap.getOrDefault(date, 0));
            result.add(item);
        }

        return result;
    }

    /**
     * 获取危险源类别TOP 10数据
     */
    private List<Map<String, Object>> getHazardSourceCategoryTop10(String beginDate, String endDate,
            Map<String, String> valueToLabelMap) {
        List<Map<String, Object>> topCategories = swmHazardSourceService.findTopCategories(beginDate, endDate, 10);
        return transformedCategoryDict(valueToLabelMap, topCategories);
    }

    /**
     * 获取字典值转换后的数据
     * 
     * @param valueToLabelMap 字典值转换
     * @param categoryCounts  类别数量
     * @return
     */
    private static List<Map<String, Object>> transformedCategoryDict(Map<String, String> valueToLabelMap,
            List<Map<String, Object>> categoryCounts) {
        // 转换categoryCounts中的category值
        List<Map<String, Object>> transformedCategoryCounts = categoryCounts.stream()
                .map(originalMap -> {
                    Map<String, Object> newMap = new HashMap<>(originalMap);
                    if (originalMap.containsKey("category")) {
                        String dictValue = (String) originalMap.get("category");
                        String dictLabel = valueToLabelMap.getOrDefault(dictValue, dictValue);
                        newMap.put("category", dictLabel);
                    }
                    return newMap;
                })
                .collect(Collectors.toList());
        return transformedCategoryCounts;
    }

    /**
     * 获取危险源类别分布数据
     */
    private List<Map<String, Object>> getHazardSourceCategoryDistribution(String beginDate, String endDate,
            Map<String, String> valueToLabelMap) {
        List<Map<String, Object>> categoryDistribution = swmHazardSourceService.findCategoryDistribution(beginDate,
                endDate);
        return transformedCategoryDict(valueToLabelMap, categoryDistribution);
    }

    /**
     * 获取危险源类别趋势数据
     */
    private Map<String, List<Map<String, Object>>> getHazardSourceCategoryTrend(String beginDateStr, String endDateStr,
            Map<String, String> valueToLabelMap) {
        // 解析日期参数
        DateRange dateRange = parseDateRange(beginDateStr, endDateStr);

        // 一次性查询所有类别的趋势数据
        List<Map<String, Object>> allData = transformedCategoryDict(valueToLabelMap,
                swmHazardSourceService.countCategoryTrendByDateRange(dateRange.getBeginDate(), dateRange.getEndDate()));

        // 按类别分组（使用传统方式创建Map）
        Map<String, List<Map<String, Object>>> groupedData = allData.stream()
                .collect(Collectors.groupingBy(
                        item -> (String) item.get("category"),
                        Collectors.mapping(
                                item -> {
                                    Map<String, Object> map = new HashMap<>();
                                    map.put("date", item.get("date"));
                                    map.put("count", item.get("count"));
                                    return map;
                                },
                                Collectors.toList())));

        // 生成完整日期列表
        List<String> dateList = getDateList(dateRange.getBeginDate(), dateRange.getEndDate());

        // 补全缺失日期数据（使用传统方式创建Map）
        groupedData.forEach((category, data) -> {
            Map<String, Integer> dateCountMap = data.stream()
                    .collect(Collectors.toMap(
                            item -> (String) item.get("date"),
                            item -> ((Number) item.get("count")).intValue()));

            List<Map<String, Object>> completeData = dateList.stream()
                    .map(date -> {
                        Map<String, Object> map = new HashMap<>();
                        map.put("date", date);
                        map.put("count", dateCountMap.getOrDefault(date, 0));
                        return map;
                    })
                    .collect(Collectors.toList());

            groupedData.put(category, completeData);
        });

        return groupedData;
    }

    /**
     * 解析日期范围
     */
    private DateRange parseDateRange(String beginDateStr, String endDateStr) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date beginDate = null;
        Date endDate = null;

        try {
            if (StringUtils.isNotBlank(beginDateStr)) {
                beginDate = sdf.parse(beginDateStr);
            }
            if (StringUtils.isNotBlank(endDateStr)) {
                endDate = sdf.parse(endDateStr);
            }
        } catch (ParseException e) {
            throw new IllegalArgumentException("日期格式不正确，请使用yyyy-MM-dd格式");
        }

        // 默认近30天
        if (beginDate == null || endDate == null) {
            Calendar cal = Calendar.getInstance();
            endDate = cal.getTime();
            cal.add(Calendar.DAY_OF_MONTH, -30);
            beginDate = cal.getTime();
        }

        return new DateRange(beginDate, endDate);
    }

    /**
     * 生成日期列表
     */
    private List<String> getDateList(Date beginDate, Date endDate) {
        List<String> dateList = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

        Calendar cal = Calendar.getInstance();
        cal.setTime(beginDate);

        while (!cal.getTime().after(endDate)) {
            dateList.add(sdf.format(cal.getTime()));
            cal.add(Calendar.DAY_OF_MONTH, 1);
        }

        return dateList;
    }

    /**
     * 日期范围内部类
     */
    private static class DateRange {
        private final Date beginDate;
        private final Date endDate;

        public DateRange(Date beginDate, Date endDate) {
            this.beginDate = beginDate;
            this.endDate = endDate;
        }

        public Date getBeginDate() {
            return beginDate;
        }

        public Date getEndDate() {
            return endDate;
        }
    }

    @GetMapping("inspectionRecords")
    @ResponseBody
    @ApiOperation(value = "获取危险源的巡检记录")
    public Map<String, Object> getInspectionRecords(@RequestParam String hazardSourceId) {
        Map<String, Object> result = new HashMap<>();

        // 先获取危险源信息
        SwmHazardSource hazardSource = swmHazardSourceService.get(hazardSourceId);
        if (hazardSource == null) {
            result.put("success", false);
            result.put("message", "危险源不存在");
            result.put("records", new ArrayList<>());
            return result;
        }

        // 组装所属信标显示文本（beaconIdentifierText）
        if (hazardSource.getBeaconIdentifier() != null && !hazardSource.getBeaconIdentifier().isEmpty()) {
            String[] ids = hazardSource.getBeaconIdentifier().split(",");
            StringBuilder beaconText = new StringBuilder();
            for (int i = 0; i < ids.length; i++) {
                String beaconId = ids[i].trim();
                if (beaconId.isEmpty())
                    continue;
                SwmBeaconStation beacon = swmBeaconStationService.getByBeaconId(beaconId);
                String deviceName = beacon != null && beacon.getDeviceName() != null
                        && !beacon.getDeviceName().trim().isEmpty() && !"null".equals(beacon.getDeviceName())
                                ? beacon.getDeviceName()
                                : beaconId;
                if (beaconText.length() > 0)
                    beaconText.append(", ");
                beaconText.append(deviceName);
            }
            hazardSource.setBeaconIdentifierText(beaconText.toString());
        } else {
            hazardSource.setBeaconIdentifierText("-");
        }

        // 1. 根据危险源ID查询相关的巡检计划
        SwmInspectionPlan queryPlan = new SwmInspectionPlan();
        queryPlan.setHazardSourceId(hazardSourceId);
        // 不再限制其他条件，只查询与该危险源相关的所有计划
        List<SwmInspectionPlan> planList = swmInspectionPlanService.findList(queryPlan);

        if (planList.isEmpty()) {
            result.put("success", true);
            result.put("message", "没有找到相关的巡检计划");
            result.put("records", new ArrayList<>());
            result.put("hazardSource", hazardSource);
            return result;
        }

        // 2. 收集所有计划的ID
        List<String> planIds = planList.stream()
                .map(SwmInspectionPlan::getId)
                .collect(Collectors.toList());

        // 3. 根据计划ID查询巡检记录
        List<Map<String, Object>> recordsList = swmInspectionPlanService.findInspectionListByPlanIds(planIds);

        result.put("success", true);
        result.put("message", "获取巡检记录成功");
        result.put("records", recordsList);
        result.put("hazardSource", hazardSource);

        return result;
    }

    /**
     * 获取未加入巡检的危险源列表
     */
    @GetMapping("notPatrolledList")
    @ResponseBody
    @ApiOperation(value = "获取未加入巡检的危险源列表")
    public List<Map<String, Object>> getNotPatrolledList() {
        // 创建查询条件：未加入巡检的危险源
        SwmHazardSource query = new SwmHazardSource();
        query.setIsPatrolIncluded("0"); // 0表示未加入巡检
        query.setStatus("0"); // 只查询正常状态的记录

        // 查询符合条件的危险源列表
        List<SwmHazardSource> hazardList = swmHazardSourceService.findList(query);

        // 转换为前端需要的格式
        List<Map<String, Object>> result = new ArrayList<>();
        for (SwmHazardSource hazard : hazardList) {
            Map<String, Object> item = new HashMap<>();
            item.put("value", hazard.getId());
            item.put("label", hazard.getHazardName());
            item.put("location", hazard.getLocation());
            item.put("hazardCategory", hazard.getHazardCategory());

            // 添加危险源类别文本
            if ("0".equals(hazard.getHazardCategory())) {
                item.put("hazardCategoryText", "气站");
            } else if ("1".equals(hazard.getHazardCategory())) {
                item.put("hazardCategoryText", "吊钩");
            } else if ("2".equals(hazard.getHazardCategory())) {
                item.put("hazardCategoryText", "油漆库");
            } else if ("3".equals(hazard.getHazardCategory())) {
                item.put("hazardCategoryText", "空压机房");
            } else if ("4".equals(hazard.getHazardCategory())) {
                item.put("hazardCategoryText", "机械伤害风险");
            } else if ("5".equals(hazard.getHazardCategory())) {
                item.put("hazardCategoryText", "化学品泄漏风险");
            } else if ("99".equals(hazard.getHazardCategory())) {
                item.put("hazardCategoryText", "其他风险");
            }

            result.add(item);
        }

        return result;
    }

}
