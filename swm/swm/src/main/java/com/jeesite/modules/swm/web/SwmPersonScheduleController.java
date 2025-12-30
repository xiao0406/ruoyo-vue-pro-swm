package com.jeesite.modules.swm.web;

import com.jeesite.common.config.Global;
import com.jeesite.common.entity.Page;
import com.jeesite.common.utils.excel.ExcelExport;
import com.jeesite.common.web.BaseController;
import com.jeesite.modules.entity.SwmBeaconStationExport;
import com.jeesite.modules.entity.SwmPersonScheduleExport;
import com.jeesite.modules.swm.entity.SwmPersonSchedule;
import com.jeesite.modules.swm.entity.SwmScheduleTime;
import com.jeesite.modules.swm.service.SwmPersonScheduleService;
import com.jeesite.modules.swm.service.SwmScheduleTimeService;
import com.jeesite.modules.sys.utils.ExcelExportUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 人员排班Controller
 * 
 * @author zwf
 * @version 2025-05-15
 */
@Controller
@RequestMapping(value = "${adminPath}/personSchedule")
@Api(value = "人员排班管理接口", tags = "人员排班管理接口")
public class SwmPersonScheduleController extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(SwmPersonScheduleController.class);

    @Autowired
    private SwmPersonScheduleService swmPersonScheduleService;

    @Autowired
    private SwmScheduleTimeService swmScheduleTimeService;

    /**
     * 获取数据
     */
    @ModelAttribute
    public SwmPersonSchedule get(String id, boolean isNewRecord) {
        return swmPersonScheduleService.get(id, isNewRecord);
    }

    /**
     * 查询列表
     */
    @RequestMapping(value = { "list", "" })
    @ApiOperation("查询列表")
    public String list(SwmPersonSchedule swmPersonSchedule, Model model) {
        model.addAttribute("swmPersonSchedule", swmPersonSchedule);
        return "modules/swm/personScheduleList";
    }

    /**
     * 查询列表数据
     */
    @RequestMapping(value = "listData")
    @ResponseBody
    @ApiOperation("查询列表数据")
    public Map<String, Object> listData(SwmPersonSchedule swmPersonSchedule, HttpServletRequest request,
            HttpServletResponse response) {

        // 从请求参数中获取四级查询条件 Author: Shawn Date: 2025/01/27
        String organization = request.getParameter("organization");
        String workshop = request.getParameter("workshop");
        String process = request.getParameter("process");
        String workGroupName = request.getParameter("workGroupName");

        // 设置到查询对象
        swmPersonSchedule.setOrganization(organization);
        swmPersonSchedule.setWorkshop(workshop);
        swmPersonSchedule.setProcess(process);
        swmPersonSchedule.setWorkGroupName(workGroupName);

        Page<SwmPersonSchedule> page = swmPersonScheduleService.findPage(new Page<>(request, response),
                swmPersonSchedule);

        // 构建包含额外字段的响应数据
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> enhancedList = new ArrayList<>();

        // 处理每个对象，添加枚举的文本显示（组织架构信息已通过SQL查询获得）
        for (SwmPersonSchedule schedule : page.getList()) {
            Map<String, Object> scheduleMap = new HashMap<>();

            // 复制基本属性
            scheduleMap.put("id", schedule.getId());
            scheduleMap.put("createBy", schedule.getCreateBy());
            scheduleMap.put("createDate", schedule.getCreateDate());
            scheduleMap.put("updateBy", schedule.getUpdateBy());
            scheduleMap.put("updateDate", schedule.getUpdateDate());
            scheduleMap.put("remarks", schedule.getRemarks());
            scheduleMap.put("status", schedule.getStatus());

            // 复制业务属性
            scheduleMap.put("personName", schedule.getPersonName());
            scheduleMap.put("month", schedule.getMonth());
            scheduleMap.put("classes", schedule.getClasses());
            scheduleMap.put("idCard", schedule.getIdCard());
            scheduleMap.put("employeeId", schedule.getEmployeeId());
            scheduleMap.put("isNewRecord", schedule.getIsNewRecord());

            // 添加枚举文本显示值
            scheduleMap.put("classesText", schedule.getClassesText());

            // 添加组织架构信息（直接从SQL查询结果获取）Author: Shawn Date: 2025/01/27
            scheduleMap.put("organization", schedule.getOrganization());
            scheduleMap.put("workshop", schedule.getWorkshop());
            scheduleMap.put("process", schedule.getProcess());
            scheduleMap.put("workGroupName", schedule.getWorkGroupName());

            // 添加到列表
            enhancedList.add(scheduleMap);
        }

        // 构建分页结果
        result.put("list", enhancedList);
        result.put("count", page.getCount());
        result.put("pageNo", page.getPageNo());
        result.put("pageSize", page.getPageSize());

        return result;
    }

    /**
     * 查看编辑表单
     */
    @RequestMapping(value = "form")
    @ResponseBody
    @ApiOperation("查看编辑表单")
    public Map<String, Object> form(SwmPersonSchedule swmPersonSchedule) {
        Map<String, Object> result = new HashMap<>();
        if (swmPersonSchedule != null) {
            Map<String, Object> scheduleData = new HashMap<>();

            // 复制基本属性
            scheduleData.put("id", swmPersonSchedule.getId());
            scheduleData.put("personName", swmPersonSchedule.getPersonName());
            scheduleData.put("month", swmPersonSchedule.getMonth());
            scheduleData.put("classes", swmPersonSchedule.getClasses());
            scheduleData.put("idCard", swmPersonSchedule.getIdCard());
            scheduleData.put("employeeId", swmPersonSchedule.getEmployeeId());
            scheduleData.put("remarks", swmPersonSchedule.getRemarks());

            // 处理枚举值
            scheduleData.put("classesText", swmPersonSchedule.getClassesText());

            // 添加班组名称 - 使用批量查询方法
            if (swmPersonSchedule.getIdCard() != null && !swmPersonSchedule.getIdCard().isEmpty()) {
                List<String> idCards = Collections.singletonList(swmPersonSchedule.getIdCard());
                List<Map<String, Object>> workGroupList = swmPersonScheduleService
                        .batchGetWorkGroupNameByIdCards(idCards);

                String workGroupName = "";
                if (!workGroupList.isEmpty()) {
                    Map<String, Object> item = workGroupList.get(0);
                    workGroupName = (String) item.get("value");
                }

                scheduleData.put("workGroupName", workGroupName != null ? workGroupName : "");
            } else {
                scheduleData.put("workGroupName", "");
            }

            result.putAll(scheduleData);
        }
        return result;
    }

    /**
     * 保存数据
     */
    @PostMapping(value = "save")
    @ResponseBody
    @ApiOperation("保存数据")
    public String save(@Validated SwmPersonSchedule swmPersonSchedule) {
        swmPersonScheduleService.save(swmPersonSchedule);
        return renderResult(Global.TRUE, text("保存人员排班成功！"));
    }

    /**
     * 批量保存数据
     */
    @PostMapping(value = "batchSave")
    @ResponseBody
    @ApiOperation("批量保存数据")
    public String batchSave(@RequestBody List<SwmPersonSchedule> scheduleList) {
        // 打印接收到的数据，用于调试
        if (scheduleList != null && !scheduleList.isEmpty()) {
            for (SwmPersonSchedule schedule : scheduleList) {
                // 记录日志，用于调试，生产环境可以移除
                logger.info("接收到排班数据: 姓名={}, 月份={}, 班次={}, 身份证号={}",
                        schedule.getPersonName(), schedule.getMonth(),
                        schedule.getClasses(), schedule.getIdCard());
            }
        }

        swmPersonScheduleService.batchSave(scheduleList);
        return renderResult(Global.TRUE, text("批量保存人员排班成功！"));
    }

    /**
     * 删除数据
     */
    @PostMapping(value = "delete")
    @ResponseBody
    @ApiOperation("删除数据")
    public String delete(SwmPersonSchedule swmPersonSchedule) {
        swmPersonScheduleService.delete(swmPersonSchedule);
        return renderResult(Global.TRUE, text("删除人员排班成功！"));
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
            SwmPersonSchedule swmPersonSchedule = swmPersonScheduleService.get(id);
            if (swmPersonSchedule != null) {
                swmPersonScheduleService.delete(swmPersonSchedule);
            }
        }
        return renderResult(Global.TRUE, text("批量删除人员排班成功！"));
    }

    /**
     * 获取班次类型选项
     */
    @GetMapping(value = "getShiftOptions")
    @ResponseBody
    @ApiOperation("获取班次类型选项")
    public Map<String, Object> getShiftOptions() {
        Map<String, Object> result = new HashMap<>();

        // 查询所有班次时间设置
        SwmScheduleTime query = new SwmScheduleTime();
        List<SwmScheduleTime> scheduleTimeList = swmScheduleTimeService.findList(query);

        // 转换为选项格式
        Map<String, Map<String, String>> shiftOptions = new HashMap<>();
        for (SwmScheduleTime scheduleTime : scheduleTimeList) {
            Map<String, String> option = new HashMap<>();
            option.put("startTime", scheduleTime.getStartTime());
            option.put("endTime", scheduleTime.getEndTime());
            option.put("text", scheduleTime.getShiftTypeText());

            shiftOptions.put(scheduleTime.getShiftType(), option);
        }

        result.put("shiftOptions", shiftOptions);

        return result;
    }

    /**
     * 根据身份证号查询排班记录
     */
    @GetMapping(value = "findByIdCard")
    @ResponseBody
    @ApiOperation("根据身份证号查询排班记录")
    public Map<String, Object> findByIdCard(@RequestParam("idCard") String idCard) {
        Map<String, Object> result = new HashMap<>();

        if (idCard == null || idCard.isEmpty()) {
            result.put("success", false);
            result.put("message", "身份证号不能为空");
            return result;
        }

        List<SwmPersonSchedule> scheduleList = swmPersonScheduleService.findByIdCard(idCard);
        List<Map<String, Object>> enhancedList = new ArrayList<>();

        // 获取班组名称
        String workGroupName = swmPersonScheduleService.getWorkGroupNameByIdCard(idCard);

        // 处理每个对象，添加枚举的文本显示
        for (SwmPersonSchedule schedule : scheduleList) {
            Map<String, Object> scheduleMap = new HashMap<>();

            // 复制基本属性
            scheduleMap.put("id", schedule.getId());
            scheduleMap.put("personName", schedule.getPersonName());
            scheduleMap.put("month", schedule.getMonth());
            scheduleMap.put("classes", schedule.getClasses());
            scheduleMap.put("idCard", schedule.getIdCard());
            scheduleMap.put("employeeId", schedule.getEmployeeId());
            scheduleMap.put("createDate", schedule.getCreateDate());
            scheduleMap.put("updateDate", schedule.getUpdateDate());
            scheduleMap.put("remarks", schedule.getRemarks());

            // 添加枚举文本显示值
            scheduleMap.put("classesText", schedule.getClassesText());

            // 添加班组名称
            scheduleMap.put("workGroupName", workGroupName);

            // 添加到列表
            enhancedList.add(scheduleMap);
        }

        result.put("success", true);
        result.put("list", enhancedList);
        result.put("count", enhancedList.size());

        return result;
    }

    /**
     * 获取班组下拉列表数据
     */
    @GetMapping(value = "getWorkGroups")
    @ResponseBody
    @ApiOperation("获取班组下拉列表")
    public List<Map<String, Object>> getWorkGroupList() {
        try {
            List<Map<String, Object>> workGroups = swmPersonScheduleService.findWorkGroupList();
            List<Map<String, Object>> result = new ArrayList<>();

            // 转换为前端需要的格式
            for (Map<String, Object> item : workGroups) {
                Map<String, Object> workGroup = new HashMap<>();
                workGroup.put("label", item.get("name"));
                workGroup.put("value", item.get("name"));
                result.add(workGroup);
            }

            return result;
        } catch (Exception e) {
            logger.error("获取班组列表失败", e);
            return Collections.emptyList();
        }
    }

    @ApiOperation("模板下载")
    @RequestMapping("/export")
    @ResponseBody
    public String export() throws IOException {
        String name;
        List<SwmPersonScheduleExport> list = new ArrayList<>();
        String fileName = "班次管理导入模板.xlsx";
        try (ExcelExport ee = new ExcelExport("班次管理设置", SwmPersonScheduleExport.class)) {
            name = ExcelExportUtil.uploadOss(ee.setDataList(list), fileName);
        }
        return renderResult(Global.TRUE, text("成功！"), name);
    }

    @ApiOperation("班次切换白夜班")
    @RequestMapping("/importData")
    @ResponseBody
    public String importData(MultipartFile file) {
        Integer count = swmPersonScheduleService.importData(file);
        return renderResult(Global.TRUE, text("数据全部导入成功,共" + count + "条。"));
    }
}