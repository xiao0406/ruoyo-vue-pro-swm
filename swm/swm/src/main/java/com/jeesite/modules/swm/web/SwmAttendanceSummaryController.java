package com.jeesite.modules.swm.web;

import cn.hutool.core.date.DateUtil;
import com.jeesite.common.config.Global;
import com.jeesite.common.entity.Page;
import com.jeesite.common.web.BaseController;
import com.jeesite.modules.swm.entity.SwmAttendanceSummary;
import com.jeesite.modules.swm.entity.SwmDailyAttendance;
import com.jeesite.modules.swm.entity.SwmPerson;
import com.jeesite.modules.swm.entity.SwmPersonSchedule;
import com.jeesite.modules.swm.service.SwmAttendanceSummaryService;
import com.jeesite.modules.swm.service.SwmDailyAttendanceService;
import com.jeesite.modules.swm.service.SwmPersonScheduleService;
import com.jeesite.modules.swm.service.SwmPersonService;
import com.jeesite.modules.swm.service.AreaFenceDataService;
import com.jeesite.modules.swm.entity.AttendanceCheckResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Calendar;

/**
 * 考勤月统计表Controller
 *
 * @author zwf
 * @version 2025-05-20
 */
@Controller
@RequestMapping(value = "${adminPath}/swmAttendanceSummary")
@Api(tags = "考勤月统计表管理")
public class SwmAttendanceSummaryController extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(SwmAttendanceSummaryController.class);

    @Autowired
    private SwmAttendanceSummaryService swmAttendanceSummaryService;
    @Autowired
    private SwmPersonService swmPersonService;
    @Autowired
    private SwmPersonScheduleService swmPersonScheduleService;
    @Autowired
    private SwmDailyAttendanceService swmDailyAttendanceService;
    @Autowired
    private AreaFenceDataService areaFenceDataService;

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
    @RequestMapping(value = { "list", "" })
    public String list(SwmAttendanceSummary swmAttendanceSummary, Model model) {
        model.addAttribute("swmAttendanceSummary", swmAttendanceSummary);
        return "modules/swm/swmAttendanceSummaryList";
    }

    /**
     * 查询列表数据
     */
    @RequestMapping(value = "listData")
    @ResponseBody
    public Page<SwmAttendanceSummary> listData(SwmAttendanceSummary swmAttendanceSummary, HttpServletRequest request,
            HttpServletResponse response) {
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

    /**
     * 格式化考勤记录中的时间字段
     * 解决数据库时间字段显示1970-01-01的问题
     * 
     * @param attendance 考勤记录对象
     * @return 格式化后的Map对象
     * @author: Shawn
     * @date: 2025/06/23
     */
    private Map<String, Object> formatAttendanceTime(SwmDailyAttendance attendance) {
        Map<String, Object> result = new HashMap<>();

        if (attendance == null) {
            return result;
        }

        // 复制所有基本字段
        result.put("id", attendance.getId());
        result.put("employeeId", attendance.getEmployeeId());
        result.put("employeeName", attendance.getEmployeeName());
        result.put("workTimeRange", attendance.getWorkTimeRange());
        result.put("scheduledHours", attendance.getScheduledHours());
        result.put("actualHours", attendance.getActualHours());
        result.put("idleHours", attendance.getIdleHours());
        result.put("effectiveWorkHours", attendance.getEffectiveWorkHours());
        result.put("dailyEfficiency", attendance.getDailyEfficiency());
        result.put("dailyAchievementRate", attendance.getDailyAchievementRate());
        result.put("attendanceNormal", attendance.getAttendanceNormal());
        result.put("currentPosition", attendance.getCurrentPosition());
        result.put("status", attendance.getStatus());
        result.put("createBy", attendance.getCreateBy());
        result.put("updateBy", attendance.getUpdateBy());
        result.put("remarks", attendance.getRemarks());
        result.put("attendanceStatus", attendance.getAttendanceStatus());

        // 格式化日期字段
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        SimpleDateFormat dateOnlyFormat = new SimpleDateFormat("yyyy-MM-dd");

        if (attendance.getAttendanceDate() != null) {
            result.put("attendanceDate", dateOnlyFormat.format(attendance.getAttendanceDate()));
        }

        if (attendance.getCreateDate() != null) {
            result.put("createDate", dateFormat.format(attendance.getCreateDate()));
        }

        if (attendance.getUpdateDate() != null) {
            result.put("updateDate", dateFormat.format(attendance.getUpdateDate()));
        }

        // 特殊处理时间字段 - 只提取时分秒部分
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");

        if (attendance.getClockInTime() != null) {
            result.put("clockInTime", timeFormat.format(attendance.getClockInTime()));
        }

        if (attendance.getClockOutTime() != null) {
            result.put("clockOutTime", timeFormat.format(attendance.getClockOutTime()));
        }

        return result;
    }

    /**
     * 轨迹信息-个人考勤记录明细
     *
     * @param employeeId 员工ID
     * @param month      月份（可选，格式：yyyy-MM）
     */
    @GetMapping(value = "attendanceDetails")
    @ResponseBody
    @ApiOperation("轨迹信息-个人考勤记录明细")
    public Map<String, Object> attendanceDetails(String employeeId, String month) {
        Map<String, Object> result = new HashMap<>();

        // 如果没有传入月份参数，使用当前月份（保持向后兼容）
        String queryMonth = StringUtils.isNotBlank(month) ? month : DateUtil.format(new Date(), "yyyy-MM");

        // 解析月份获取年和月
        int year;
        int monthValue;
        try {
            if (StringUtils.isNotBlank(month)) {
                String[] parts = month.split("-");
                year = Integer.parseInt(parts[0]);
                monthValue = Integer.parseInt(parts[1]);
            } else {
                year = DateUtil.year(new Date());
                monthValue = DateUtil.month(new Date()) + 1;
            }
        } catch (Exception e) {
            // 如果解析失败，使用当前年月
            year = DateUtil.year(new Date());
            monthValue = DateUtil.month(new Date()) + 1;
            queryMonth = DateUtil.format(new Date(), "yyyy-MM");
        }

        // 使用findList方法获取包含关联表翻译后的完整人员信息
        SwmPerson queryPerson = new SwmPerson();
        queryPerson.setId(employeeId);
        List<SwmPerson> personList = swmPersonService.findList(queryPerson);
        SwmPerson swmPerson = personList.isEmpty() ? null : personList.get(0);

        if (swmPerson != null) {
            SwmPersonSchedule queryPersonSchedule = new SwmPersonSchedule();
            queryPersonSchedule.setIdCard(swmPerson.getIdentityCard());
            queryPersonSchedule.setMonth(queryMonth);

            SwmPersonSchedule swmPersonSchedule = swmPersonScheduleService.getByEntity(queryPersonSchedule);
            if (swmPersonSchedule != null) {
                swmPerson.setClasses(swmPersonSchedule.getClasses());
            }
            result.put("person", swmPerson);

            // 查询日考勤 - 使用格式化方法处理时间字段
            // 注意：日考勤仍然查询当天的数据
            SwmDailyAttendance dailyAttendance = swmDailyAttendanceService.findByEmployeeIdAndDate(employeeId,
                    DateUtil.date());
            result.put("dailyAttendance", formatAttendanceTime(dailyAttendance));

            // 查询月考勤 - 使用身份证号实时计算统计数据
            SwmAttendanceSummary attendanceSummary = null;
            if (swmPerson.getIdentityCard() != null && !swmPerson.getIdentityCard().trim().isEmpty()) {
                // 使用新的计算方法，包含应出勤天数等实时统计指标
                attendanceSummary = swmAttendanceSummaryService
                        .calculateAttendanceSummaryByIdentityCard(swmPerson.getIdentityCard(), queryMonth);
            }
            result.put("attendanceSummary", attendanceSummary);

            // 考勤时间和功效统计 - 使用传入的月份
            result.put("attendanceChartData", swmDailyAttendanceService.getMonthlyAttendanceData(employeeId,
                    year, monthValue));
            result.put("efficiencyChartData", swmDailyAttendanceService.getMonthlyChartData(employeeId,
                    year, monthValue));
        }

        return result;
    }

    /**
     * 根据身份证查询考勤情况
     *
     * @param idCard    身份证号
     * @param checkDate 检查日期（可选，默认当天，格式：yyyy-MM-dd）
     * @return 考勤检查结果
     *         通过人员id 查询找个人，以早班为例08:02 - 16:00： 1 是否迟到（08:05之前 厂内是否有该人员的坐标 ） 2
     *         是否早退（15:55之后 厂内是否有该人员的坐标 ） 3 是否旷工 （当天有无坐标） 3 怠工时长 （根据工作区域计算 -
     *         这个需要特定算法）
     */
    @GetMapping(value = "checkAttendanceByIdCard")
    @ResponseBody
    @ApiOperation("根据身份证查询考勤情况")
    public Map<String, Object> checkAttendanceByIdCard(String idCard, String checkDate) {
        Map<String, Object> result = new HashMap<>();

        try {
            if (StringUtils.isBlank(idCard)) {
                result.put("success", false);
                result.put("message", "身份证号不能为空");
                return result;
            }

            // 调用服务层进行考勤检查
            AttendanceCheckResult checkResult = areaFenceDataService.checkAttendanceByIdCard(idCard, checkDate);

            result.put("success", true);
            result.put("data", checkResult);
            result.put("message", "查询成功");

        } catch (Exception e) {
            logger.error("查询考勤情况失败：idCard={}, checkDate={}", idCard, checkDate, e);
            result.put("success", false);
            result.put("message", "查询失败：" + e.getMessage());
        }

        return result;
    }
}
