package com.jeesite.modules.swm.web;

import com.jeesite.common.config.Global;
import com.jeesite.common.entity.Page;
import com.jeesite.common.web.BaseController;
import com.jeesite.modules.swm.entity.SwmAttendanceSummary;
import com.jeesite.modules.swm.service.SwmAttendanceSummaryService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 考勤月统计表Controller
 * @author  zwf
 * @version 2025-05-20
 */
@Controller
@RequestMapping(value = "${adminPath}/swmAttendanceSummary")
@Api(tags = "考勤月统计表管理")
public class SwmAttendanceSummaryController extends BaseController {

    @Autowired
    private SwmAttendanceSummaryService swmAttendanceSummaryService;
    
    /**
     * 获取数据
     */
    @ModelAttribute
    public SwmAttendanceSummary get(String id, boolean isNewRecord) {
        return swmAttendanceSummaryService.get(id, isNewRecord);
    }
    
    /**
     * 查询列表
     */
    @RequestMapping(value = {"list", ""})
    public String list(SwmAttendanceSummary swmAttendanceSummary, Model model) {
        model.addAttribute("swmAttendanceSummary", swmAttendanceSummary);
        return "modules/swm/swmAttendanceSummaryList";
    }
    
    /**
     * 查询列表数据
     */
    @RequestMapping(value = "listData")
    @ResponseBody
    public Page<SwmAttendanceSummary> listData(SwmAttendanceSummary swmAttendanceSummary, HttpServletRequest request, HttpServletResponse response) {
        // 处理月份格式转换
        if (StringUtils.isNotBlank(swmAttendanceSummary.getMonth())) {
            String monthStr = swmAttendanceSummary.getMonth();
            
            // 检查是否是"yyyy年MM月"格式
            Pattern pattern = Pattern.compile("(\\d{4})年(\\d{1,2})月");
            Matcher matcher = pattern.matcher(monthStr);
            
            if (matcher.matches()) {
                // 提取年和月
                String year = matcher.group(1);
                String month = matcher.group(2);
                // 格式化月份，保证是两位数
                if (month.length() == 1) {
                    month = "0" + month;
                }
                // 转换为"yyyy-MM"格式
                String formattedMonth = year + "-" + month;
                swmAttendanceSummary.setMonth(formattedMonth);
            }
        } else {
            // 如果没有指定月份，默认使用当前月份
            Date now = new Date();
            SimpleDateFormat monthFormat = new SimpleDateFormat("yyyy-MM");
            String currentMonth = monthFormat.format(now);
            swmAttendanceSummary.setMonth(currentMonth);
        }
        
        // 清除status条件，让查询包括所有状态的记录
        swmAttendanceSummary.setStatus(null);
        
        // 设置分页参数
        swmAttendanceSummary.setPage(new Page<>(request, response));
        // 查询数据
        Page<SwmAttendanceSummary> page = swmAttendanceSummaryService.findPage(swmAttendanceSummary);
        return page;
    }

    /**
     * 查看编辑表单
     */
    @RequestMapping(value = "form")
    public String form(SwmAttendanceSummary swmAttendanceSummary, Model model) {
        model.addAttribute("swmAttendanceSummary", swmAttendanceSummary);
        return "modules/swm/swmAttendanceSummaryForm";
    }

    /**
     * 保存数据
     */
    @PostMapping(value = "save")
    @ResponseBody
    @ApiOperation("保存数据")
    public String save(@Validated SwmAttendanceSummary swmAttendanceSummary) {
        swmAttendanceSummaryService.save(swmAttendanceSummary);
        return renderResult(Global.TRUE, text("保存考勤月统计表成功！"));
    }
    
    /**
     * 删除数据
     */
    @RequestMapping(value = "delete")
    @ResponseBody
    @ApiOperation("删除数据")
    public String delete(SwmAttendanceSummary swmAttendanceSummary) {
        swmAttendanceSummaryService.delete(swmAttendanceSummary);
        return renderResult(Global.TRUE, text("删除考勤月统计表成功！"));
    }
    
    /**
     * 根据月份查询考勤统计记录
     */
    @GetMapping(value = "findByMonth")
    @ResponseBody
    @ApiOperation("根据月份查询考勤统计记录")
    public Map<String, Object> findByMonth(String month) {
        Map<String, Object> result = new HashMap<>();
        if (month != null && !month.isEmpty()) {
            List<SwmAttendanceSummary> recordList = swmAttendanceSummaryService.findByMonth(month);
            result.put("list", recordList);
            result.put("success", true);
        } else {
            result.put("success", false);
            result.put("message", "月份参数不能为空");
        }
        return result;
    }
    
    /**
     * 根据员工和月份查询考勤统计记录
     */
    @GetMapping(value = "findByEmployeeAndMonth")
    @ResponseBody
    @ApiOperation("根据员工和月份查询考勤统计记录")
    public Map<String, Object> findByEmployeeAndMonth(String employeeName, String month) {
        Map<String, Object> result = new HashMap<>();
        if (employeeName != null && !employeeName.isEmpty() && month != null && !month.isEmpty()) {
            SwmAttendanceSummary record = swmAttendanceSummaryService.findByEmployeeAndMonth(employeeName, month);
            result.put("record", record);
            result.put("success", true);
        } else {
            result.put("success", false);
            result.put("message", "员工姓名和月份参数不能为空");
        }
        return result;
    }

    /**
     * 根据ID获取考勤月统计记录
     */
    @GetMapping(value = "getDataById")
    @ResponseBody
    @ApiOperation("根据ID获取考勤月统计记录")
    public Map<String, Object> getDataById(String id, String month) {
        Map<String, Object> result = new HashMap<>();
        
        if (StringUtils.isNotBlank(id)) {
            SwmAttendanceSummary attendanceSummary = swmAttendanceSummaryService.get(id);
            
            if (attendanceSummary != null) {
                result.put("data", attendanceSummary);
                result.put("success", true);
                result.put("message", "获取数据成功");
            } else {
                result.put("success", false);
                result.put("message", "未找到ID为 " + id + " 的记录");
            }
        } else {
            result.put("success", false);
            result.put("message", "ID参数不能为空");
        }
        
        return result;
    }
} 