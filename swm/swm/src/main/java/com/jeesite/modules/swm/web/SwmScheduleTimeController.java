package com.jeesite.modules.swm.web;

import com.jeesite.common.config.Global;
import com.jeesite.common.entity.Page;
import com.jeesite.common.web.BaseController;
import com.jeesite.modules.swm.entity.SwmScheduleTime;
import com.jeesite.modules.swm.service.SwmScheduleTimeService;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 排班时间管理Controller
 * 
 * @author zwf
 * @version 2025-05-15
 */
@Controller
@RequestMapping(value = "${adminPath}/scheduleTime")
@Api(value = "排班时间管理接口", tags = "排班时间管理接口")
public class SwmScheduleTimeController extends BaseController {

    @Autowired
    private SwmScheduleTimeService swmScheduleTimeService;

    /**
     * 获取数据
     */
    @ModelAttribute
    public SwmScheduleTime get(String id, boolean isNewRecord) {
        return swmScheduleTimeService.get(id, isNewRecord);
    }

    /**
     * 查询列表
     */
    @RequestMapping(value = { "list", "" })
    @ApiOperation("查询列表")
    public String list(SwmScheduleTime swmScheduleTime, Model model) {
        model.addAttribute("swmScheduleTime", swmScheduleTime);
        return "modules/swm/scheduleTimeList";
    }

    /**
     * 查询列表数据
     */
    @RequestMapping(value = "listData")
    @ResponseBody
    @ApiOperation("查询列表数据")
    public Map<String, Object> listData(SwmScheduleTime swmScheduleTime, HttpServletRequest request,
            HttpServletResponse response) {
        Page<SwmScheduleTime> page = swmScheduleTimeService.findPage(new Page<>(request, response), swmScheduleTime);

        // 构建包含额外字段的响应数据
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> enhancedList = new ArrayList<>();

        // 处理每个对象，添加枚举的文本显示
        for (SwmScheduleTime schedule : page.getList()) {
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
            scheduleMap.put("shiftType", schedule.getShiftType());
            scheduleMap.put("startTime", schedule.getStartTime());
            scheduleMap.put("endTime", schedule.getEndTime());
            scheduleMap.put("restTime", schedule.getRestTime());
            scheduleMap.put("restDays", schedule.getRestDays());
            scheduleMap.put("isNewRecord", schedule.getIsNewRecord());
            scheduleMap.put("noonEndTime", schedule.getNoonEndTime());
            scheduleMap.put("afterStartTime", schedule.getAfterStartTime());

            // 添加枚举文本显示值
            scheduleMap.put("shiftTypeText", schedule.getShiftTypeText());
            scheduleMap.put("restDaysText", schedule.getRestDaysText());

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
    public Map<String, Object> form(SwmScheduleTime swmScheduleTime) {
        Map<String, Object> result = new HashMap<>();
        if (swmScheduleTime != null) {
            Map<String, Object> scheduleData = new HashMap<>();

            // 复制基本属性
            scheduleData.put("id", swmScheduleTime.getId());
            scheduleData.put("shiftType", swmScheduleTime.getShiftType());
            scheduleData.put("startTime", swmScheduleTime.getStartTime());
            scheduleData.put("endTime", swmScheduleTime.getEndTime());
            scheduleData.put("restTime", swmScheduleTime.getRestTime());
            scheduleData.put("restDays", swmScheduleTime.getRestDays());
            scheduleData.put("remarks", swmScheduleTime.getRemarks());

            // 处理枚举值
            scheduleData.put("shiftTypeText", swmScheduleTime.getShiftTypeText());
            scheduleData.put("restDaysText", swmScheduleTime.getRestDaysText());

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
    public String save(@Validated SwmScheduleTime swmScheduleTime) {
        swmScheduleTimeService.save(swmScheduleTime);
        return renderResult(Global.TRUE, text("保存排班时间成功！"));
    }

    /**
     * 删除数据
     */
    @PostMapping(value = "delete")
    @ResponseBody
    @ApiOperation("删除数据")
    public String delete(SwmScheduleTime swmScheduleTime) {
        swmScheduleTimeService.delete(swmScheduleTime);
        return renderResult(Global.TRUE, text("删除排班时间成功！"));
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
            SwmScheduleTime swmScheduleTime = swmScheduleTimeService.get(id);
            if (swmScheduleTime != null) {
                swmScheduleTimeService.delete(swmScheduleTime);
            }
        }
        return renderResult(Global.TRUE, text("批量删除排班时间成功！"));
    }

    /**
     * 保存所有排班时间数据
     */
    @PostMapping(value = "saveAll")
    @ResponseBody
    @ApiOperation("保存所有排班时间数据")
    public String saveAll(@RequestBody List<SwmScheduleTime> scheduleTimes) {
        swmScheduleTimeService.saveAll(scheduleTimes);
        return renderResult(Global.TRUE, text("保存排班时间成功！"));
    }

    /**
     * 获取班次类型选项
     */
    @GetMapping(value = "enumOptions")
    @ResponseBody
    @ApiOperation("获取班次类型选项")
    public Map<String, Object> getEnumOptions() {
        Map<String, Object> result = new HashMap<>();

        // 班次类型选项
        Map<String, String> shiftTypeOptions = new HashMap<>();
        shiftTypeOptions.put(SwmScheduleTime.ShiftTypeEnum.MORNING, "早班");
        shiftTypeOptions.put(SwmScheduleTime.ShiftTypeEnum.MIDDLE, "中班");
        shiftTypeOptions.put(SwmScheduleTime.ShiftTypeEnum.NIGHT, "晚班");
        result.put("shiftTypeOptions", shiftTypeOptions);

        return result;
    }
}