/**
 * 危险源信息Controller
 * @author Shawn
 * @version 2025-05-20
 */
package com.jeesite.modules.swm.web;

import com.jeesite.common.config.Global;
import com.jeesite.common.entity.Page;
import com.jeesite.common.web.BaseController;
import com.jeesite.modules.swm.entity.SwmHazardSource;
import com.jeesite.modules.swm.entity.SwmInspectionPlan;
import com.jeesite.modules.swm.entity.SwmPerson;
import com.jeesite.modules.swm.entity.SwmVoiceTemplate;
import com.jeesite.modules.swm.service.SwmHazardSourceService;
import com.jeesite.modules.swm.service.SwmInspectionPlanService;
import com.jeesite.modules.swm.service.SwmPersonService;
import com.jeesite.modules.swm.service.SwmVoiceTemplateService;
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
        swmHazardSource.setPage(new Page<>(request, response));
        Page<SwmHazardSource> page = swmHazardSourceService.findPage(swmHazardSource);

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

            // 语音模板名称
            if (item.getVoiceTemplateId() != null && !item.getVoiceTemplateId().isEmpty()) {
                SwmVoiceTemplate voiceTemplate = swmVoiceTemplateService.get(item.getVoiceTemplateId());
                if (voiceTemplate != null) {
                    item.setVoiceTemplateText(voiceTemplate.getTemplateName());
                }
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

            result.putAll(data);
        }
        return result;
    }

    /**
     * 保存危险源信息
     */
    @PostMapping(value = "save")
    @ResponseBody
    @ApiOperation(value = "保存危险源")
    public String save(@Validated SwmHazardSource swmHazardSource) {
        if (swmHazardSource.getResponsiblePersonId() != null) {
            SwmPerson swmPerson = swmPersonService.get(swmHazardSource.getResponsiblePersonId());
            if (swmPerson == null) {
                // return renderResult(Global.FALSE, text("巡检负责人不存在！"));
            } else {
                swmHazardSource.setResponsiblePerson(swmPerson.getName());
            }
        }
        swmHazardSourceService.save(swmHazardSource);
        // 如果设置为加入巡检需要生成巡检计划
        if ("1".equals(swmHazardSource.getIsPatrolIncluded())) {
            // 判断必要字段是否为空
            if (swmHazardSource.getFirstInspectionTime() == null || swmHazardSource.getFrequencyDays() == null
                    || swmHazardSource.getResponsiblePersonId() == null) {
                return renderResult(Global.FALSE, text("加入巡检的危险源巡检负责人、巡检频次、首检时间不能为空"));
            }
            SwmInspectionPlan swmInspectionPlan = new SwmInspectionPlan();
            swmInspectionPlan.setPlanName(swmHazardSource.getHazardName());
            swmInspectionPlan.setFrequencyDays(swmHazardSource.getFrequencyDays());
            // 危险源巡检
            swmInspectionPlan.setInspectionType("3");
            swmInspectionPlan.setResponsiblePersonId(swmHazardSource.getResponsiblePersonId());
            swmInspectionPlan.setResponsiblePerson(swmHazardSource.getResponsiblePerson());
            swmInspectionPlan.setFirstInspectionTime(swmHazardSource.getFirstInspectionTime());
            swmInspectionPlanService.save(swmInspectionPlan);
        }
        return renderResult(Global.TRUE, text("保存危险源信息成功！"));
    }

    /**
     * 删除危险源信息
     */
    @RequestMapping(value = "delete")
    @ResponseBody
    @ApiOperation(value = "删除危险源")
    public String delete(SwmHazardSource swmHazardSource) {
        swmHazardSourceService.delete(swmHazardSource);
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
     * 热力图-危险源总数趋势
     * @param beginDate 开始时间
     * @param endDate 结束时间
     * @return
     */
    @GetMapping("hazardSourceStatistics")
    @ResponseBody
    @ApiOperation(value = "热力图-危险源总数趋势")
    public Map<String, Object> hazardSourceStatistics(@RequestParam(required = false) String beginDate, @RequestParam(required = false) String endDate) {

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
        result.put("categoryTop10", getHazardSourceCategoryTop10(beginDate, endDate,valueToLabelMap));

        // 3. 危险源类别分布饼图
        result.put("categoryDistribution",getHazardSourceCategoryDistribution(beginDate, endDate, valueToLabelMap));

        // 4. 危险源类别趋势折线图
        result.put("categoryTrend", getHazardSourceCategoryTrend(beginDate, endDate,valueToLabelMap));

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
                Arrays.asList(SwmHazardSource.HazardSourceStatusEnum.WAIT, SwmHazardSource.HazardSourceStatusEnum.IN_PROGRESS));

        // 3. 已关闭危险源数量（总数 - 未关闭的）
        int closedCount = rangeCount - unclosedCount;

        // 4. 危险源整改率（保留2位小数）
        double rectificationRate = rangeCount > 0 ?
                Math.round(closedCount * 10000.0 / rangeCount) / 100.0 : 0;

        // 5. 未制定巡检计划的数量
        int noInspectionPlanCount = swmHazardSourceService.countNoInspectionPlan(
                dateRange.getBeginDate(),
                dateRange.getEndDate());

        // 返回结果
        result.put("totalCount", totalCount); // 危险源总数累计
        result.put("rangeCount", totalCount); // 危险源总数
        result.put("unclosedCount", unclosedCount); //未关闭的
        result.put("closedCount", closedCount); //已整改的
        result.put("rectificationRate", rectificationRate);//危险源整改率
        result.put("noInspectionPlanCount", noInspectionPlanCount);//未制定巡检计划的数量

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
                dateRange.getEndDate()
        );

        // 转换为按日期索引的Map
        Map<String, Integer> countMap = dbResults.stream()
                .collect(Collectors.toMap(
                        item -> (String) item.get("date"),
                        item -> ((Number) item.get("count")).intValue()
                ));

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
    private List<Map<String, Object>> getHazardSourceCategoryTop10(String beginDate,String endDate,Map<String, String> valueToLabelMap) {
        List<Map<String, Object>> topCategories = swmHazardSourceService.findTopCategories(beginDate, endDate, 10);
        return transformedCategoryDict(valueToLabelMap, topCategories);
    }

    /**
     * 获取字典值转换后的数据
     * @param valueToLabelMap 字典值转换
     * @param categoryCounts 类别数量
     * @return
     */
    private static List<Map<String, Object>> transformedCategoryDict(Map<String, String> valueToLabelMap, List<Map<String, Object>> categoryCounts) {
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
    private List<Map<String, Object>> getHazardSourceCategoryDistribution(String beginDate,String endDate,Map<String, String> valueToLabelMap) {
        List<Map<String, Object>> categoryDistribution = swmHazardSourceService.findCategoryDistribution(beginDate, endDate);
        return transformedCategoryDict(valueToLabelMap, categoryDistribution);
    }

    /**
     * 获取危险源类别趋势数据
     */
    private Map<String, List<Map<String, Object>>> getHazardSourceCategoryTrend(String beginDateStr, String endDateStr,Map<String, String> valueToLabelMap) {
        // 解析日期参数
        DateRange dateRange = parseDateRange(beginDateStr, endDateStr);

        // 一次性查询所有类别的趋势数据
        List<Map<String, Object>> allData = transformedCategoryDict(valueToLabelMap, swmHazardSourceService.countCategoryTrendByDateRange(dateRange.getBeginDate(),dateRange.getEndDate()));

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
                                Collectors.toList()
                        )
                ));

        // 生成完整日期列表
        List<String> dateList = getDateList(dateRange.getBeginDate(), dateRange.getEndDate());

        // 补全缺失日期数据（使用传统方式创建Map）
        groupedData.forEach((category, data) -> {
            Map<String, Integer> dateCountMap = data.stream()
                    .collect(Collectors.toMap(
                            item -> (String) item.get("date"),
                            item -> ((Number) item.get("count")).intValue()
                    ));

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

}
