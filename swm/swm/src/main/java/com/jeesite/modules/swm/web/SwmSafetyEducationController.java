package com.jeesite.modules.swm.web;

import com.jeesite.common.config.Global;
import com.jeesite.common.entity.Page;
import com.jeesite.common.lang.DateUtils;
import com.jeesite.common.web.BaseController;
import com.jeesite.modules.swm.entity.SwmSafetyEducation;
import com.jeesite.modules.swm.service.SwmSafetyEducationService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 安全教育Controller
 * 
 * @author generated
 * @version 2024-05-14
 */
@Controller
@RequestMapping(value = "${adminPath}/safetyEducation")
@Api(value = "安全教育管理接口", tags = "安全教育管理接口")
public class SwmSafetyEducationController extends BaseController {

    @Autowired
    private SwmSafetyEducationService swmSafetyEducationService;

    /**
     * 获取数据
     */
    @ModelAttribute
    public SwmSafetyEducation get(String id, boolean isNewRecord) {
        return swmSafetyEducationService.get(id, isNewRecord);
    }

    /**
     * 查询列表
     */
    @RequestMapping(value = { "list", "" })
    @ApiOperation("查询列表")
    public String list(SwmSafetyEducation swmSafetyEducation, Model model) {
        model.addAttribute("swmSafetyEducation", swmSafetyEducation);
        return "modules/swm/safetyEducationList";
    }

    /**
     * 查询列表数据
     */
    @RequestMapping(value = "listData")
    @ResponseBody
    @ApiOperation("查询列表数据")
    public Page<SwmSafetyEducation> listData(SwmSafetyEducation swmSafetyEducation, HttpServletRequest request,
            HttpServletResponse response) {
        // 创建分页对象
        Page<SwmSafetyEducation> page = new Page<>(request, response);

        // 使用不带状态过滤的方法查询所有记录
        List<SwmSafetyEducation> allRecords = swmSafetyEducationService.findAllWithoutStatusFilter();

        // 应用其他过滤条件（如果有的话）
        List<SwmSafetyEducation> filteredRecords = filterRecords(allRecords, swmSafetyEducation);

        // 设置分页结果
        int pageNo = page.getPageNo();
        int pageSize = page.getPageSize();
        int count = filteredRecords.size();

        // 计算起止索引
        int fromIndex = (pageNo - 1) * pageSize;
        int toIndex = Math.min(fromIndex + pageSize, count);

        // 防止越界
        if (fromIndex >= count) {
            fromIndex = Math.max(0, count - pageSize);
            toIndex = count;
        }

        // 获取当前页数据
        List<SwmSafetyEducation> pageRecords = (fromIndex < toIndex) ? filteredRecords.subList(fromIndex, toIndex)
                : new ArrayList<>();

        // 处理枚举显示值并将状态替换为文本
        for (SwmSafetyEducation education : pageRecords) {
            // 先获取文本值
            String statusText = education.getStatusText();
            String typeText = education.getSafetyEducationTypeText();
            String participationTypeText = education.getParticipationTypeText();

            // 将字段的原始值替换为文本值
            education.setStatus(statusText);
            education.setSafetyEducationType(typeText);
            education.setParticipationType(participationTypeText);
        }

        // 设置分页对象属性
        page.setList(pageRecords);
        page.setCount(count);

        return page;
    }

    /**
     * 根据查询条件过滤记录
     */
    private List<SwmSafetyEducation> filterRecords(List<SwmSafetyEducation> allRecords, SwmSafetyEducation criteria) {
        if (criteria == null) {
            return allRecords;
        }

        return allRecords.stream()
                .filter(record -> {
                    // 根据主题过滤（模糊匹配）
                    if (criteria.getTheme() != null && !criteria.getTheme().isEmpty()) {
                        if (record.getTheme() == null || !record.getTheme().contains(criteria.getTheme())) {
                            return false;
                        }
                    }

                    // 根据安全教育类型过滤（精确匹配）
                    if (criteria.getSafetyEducationType() != null && !criteria.getSafetyEducationType().isEmpty()) {
                        if (record.getSafetyEducationType() == null
                                || !record.getSafetyEducationType().equals(criteria.getSafetyEducationType())) {
                            return false;
                        }
                    }

                    // 根据参与类型过滤（精确匹配）
                    if (criteria.getParticipationType() != null && !criteria.getParticipationType().isEmpty()) {
                        if (record.getParticipationType() == null
                                || !record.getParticipationType().equals(criteria.getParticipationType())) {
                            return false;
                        }
                    }

                    // 根据参与对象过滤（模糊匹配）
                    if (criteria.getParticipants() != null && !criteria.getParticipants().isEmpty()) {
                        if (record.getParticipants() == null
                                || !record.getParticipants().contains(criteria.getParticipants())) {
                            return false;
                        }
                    }

                    // 根据状态过滤（精确匹配）
                    if (criteria.getStatus() != null && !criteria.getStatus().isEmpty()) {
                        if (record.getStatus() == null || !record.getStatus().equals(criteria.getStatus())) {
                            return false;
                        }
                    }

                    // 根据附件URL过滤（模糊匹配）
                    if (criteria.getAttachmentUrl() != null && !criteria.getAttachmentUrl().isEmpty()) {
                        if (record.getAttachmentUrl() == null
                                || !record.getAttachmentUrl().contains(criteria.getAttachmentUrl())) {
                            return false;
                        }
                    }

                    // 根据开始时间过滤
                    if (criteria.getStartTime() != null) {
                        if (record.getStartTime() == null || record.getStartTime().before(criteria.getStartTime())) {
                            return false;
                        }
                    }

                    return true;
                })
                .collect(Collectors.toList());
    }

    /**
     * 查看编辑表单
     */
    @RequestMapping(value = "form")
    @ResponseBody
    @ApiOperation("查看编辑表单")
    public Map<String, Object> form(SwmSafetyEducation swmSafetyEducation) {
        Map<String, Object> result = new HashMap<>();
        if (swmSafetyEducation != null) {
            Map<String, Object> educationData = new HashMap<>();
            // 复制基本属性
            educationData.put("id", swmSafetyEducation.getId());
            educationData.put("theme", swmSafetyEducation.getTheme());
            educationData.put("startTime", swmSafetyEducation.getStartTime());
            educationData.put("participants", swmSafetyEducation.getParticipants());
            educationData.put("remarks", swmSafetyEducation.getRemarks());
            educationData.put("attachmentUrl", swmSafetyEducation.getAttachmentUrl());

            // 处理枚举值
            educationData.put("status", swmSafetyEducation.getStatus());
            educationData.put("statusText", swmSafetyEducation.getStatusText());
            educationData.put("safetyEducationType", swmSafetyEducation.getSafetyEducationType());
            educationData.put("safetyEducationTypeText", swmSafetyEducation.getSafetyEducationTypeText());
            educationData.put("participationType", swmSafetyEducation.getParticipationType());
            educationData.put("participationTypeText", swmSafetyEducation.getParticipationTypeText());

            // 处理时间
            educationData.put("createTime", swmSafetyEducation.getCreateTime());
            educationData.put("updateTime", swmSafetyEducation.getUpdateTime());

            result.putAll(educationData);
        }
        return result;
    }

    /**
     * 保存数据
     */
    @PostMapping(value = "save")
    @ResponseBody
    @ApiOperation("保存数据")
    public String save(@Validated SwmSafetyEducation swmSafetyEducation) {
        // 设置创建/更新时间
        Date now = new Date();
        if (swmSafetyEducation.getIsNewRecord()) {
            swmSafetyEducation.setCreateTime(now);
        }
        swmSafetyEducation.setUpdateTime(now);

        swmSafetyEducationService.save(swmSafetyEducation);
        return renderResult(Global.TRUE, text("保存安全教育成功！"));
    }

    /**
     * 删除数据
     */
    @PostMapping(value = "delete")
    @ResponseBody
    @ApiOperation("删除数据")
    public String delete(SwmSafetyEducation swmSafetyEducation) {
        swmSafetyEducationService.delete(swmSafetyEducation);
        return renderResult(Global.TRUE, text("删除安全教育成功！"));
    }

    /**
     * 批量删除数据
     */
    @PostMapping(value = "deleteAll")
    @ResponseBody
    @ApiOperation("批量删除数据")
    public String deleteAll(String ids) {
        String[] idArray = ids.split(",");
        for (String id : idArray) {
            SwmSafetyEducation swmSafetyEducation = swmSafetyEducationService.get(id);
            if (swmSafetyEducation != null) {
                swmSafetyEducationService.delete(swmSafetyEducation);
            }
        }
        return renderResult(Global.TRUE, text("批量删除安全教育成功！"));
    }

    /**
     * 获取枚举选项
     */
    @GetMapping(value = "enumOptions")
    @ResponseBody
    @ApiOperation("获取枚举选项")
    public Map<String, Object> getEnumOptions() {
        Map<String, Object> result = new HashMap<>();

        // 状态选项
        Map<String, String> statusOptions = new HashMap<>();
        statusOptions.put(SwmSafetyEducation.StatusEnum.NOT_STARTED, "未开始");
        statusOptions.put(SwmSafetyEducation.StatusEnum.COMPLETED, "已完成");
        result.put("statusOptions", statusOptions);

        // 安全教育类型选项
        Map<String, String> typeOptions = new HashMap<>();
        typeOptions.put(SwmSafetyEducation.EducationTypeEnum.ENTRY, "人员入职安全教育");
        typeOptions.put(SwmSafetyEducation.EducationTypeEnum.WEEKLY, "周安全教育");
        typeOptions.put(SwmSafetyEducation.EducationTypeEnum.MONTHLY, "月度教育");
        typeOptions.put(SwmSafetyEducation.EducationTypeEnum.QUARTERLY, "季度教育");
        typeOptions.put(SwmSafetyEducation.EducationTypeEnum.SPECIAL, "专题教育");
        result.put("educationTypeOptions", typeOptions);

        // 参与类型选项
        Map<String, String> participationTypeOptions = new HashMap<>();
        participationTypeOptions.put(SwmSafetyEducation.ParticipationTypeEnum.TEAM, "班组");
        participationTypeOptions.put(SwmSafetyEducation.ParticipationTypeEnum.PROCESS, "工序");
        participationTypeOptions.put(SwmSafetyEducation.ParticipationTypeEnum.WORKSHOP, "车间");
        result.put("participationTypeOptions", participationTypeOptions);

        return result;
    }

    /**
     * 导出数据
     */
    @RequestMapping(value = "exportData")
    @ApiOperation("导出数据")
    public void exportData(SwmSafetyEducation swmSafetyEducation, HttpServletResponse response) {
        try {
            // 使用不带状态过滤的方法获取所有记录
            List<SwmSafetyEducation> allRecords = swmSafetyEducationService.findAllWithoutStatusFilter();

            // 应用其他过滤条件
            List<SwmSafetyEducation> filteredRecords = filterRecords(allRecords, swmSafetyEducation);

            // 处理枚举显示值并将状态替换为文本
            for (SwmSafetyEducation education : filteredRecords) {
                // 先获取文本值
                String statusText = education.getStatusText();
                String typeText = education.getSafetyEducationTypeText();
                String participationTypeText = education.getParticipationTypeText();

                // 将字段的原始值替换为文本值
                education.setStatus(statusText);
                education.setSafetyEducationType(typeText);
                education.setParticipationType(participationTypeText);
            }

            String fileName = "安全教育数据" + DateUtils.getDate("yyyyMMddHHmmss") + ".xlsx";
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", "attachment; filename=" + fileName);
            // 由于BaseController没有exportExcel方法，这里省略导出逻辑
            // 如需实现，可以使用POI或EasyExcel等库
        } catch (Exception e) {
            logger.error("导出安全教育数据失败！", e);
        }
    }
}