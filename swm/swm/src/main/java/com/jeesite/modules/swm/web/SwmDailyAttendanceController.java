package com.jeesite.modules.swm.web;

import com.jeesite.common.config.Global;
import com.jeesite.common.entity.Page;
import com.jeesite.common.web.BaseController;
import com.jeesite.modules.swm.entity.SwmAttendanceSummary;
import com.jeesite.modules.swm.entity.SwmDailyAttendance;
import com.jeesite.modules.swm.service.SwmAttendanceSummaryService;
import com.jeesite.modules.swm.service.SwmDailyAttendanceService;
import com.jeesite.modules.job.task.AttendanceTask;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.math.BigDecimal;
import java.text.ParseException;
import java.util.Calendar;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ser.std.DateSerializer;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.jeesite.common.lang.StringUtils;
import com.jeesite.modules.utils.R;
import com.jeesite.modules.swm.service.AreaFenceDataService;

/**
 * 日考勤统计表Controller
 * 
 * @author zwf
 * @version 2025-05-20
 */
@Controller
@RequestMapping(value = "${adminPath}/swmDailyAttendance")
@Api(tags = "日考勤统计表管理")
public class SwmDailyAttendanceController extends BaseController {

    @Autowired
    private SwmDailyAttendanceService swmDailyAttendanceService;

    @Autowired
    private SwmAttendanceSummaryService swmAttendanceSummaryService;

    @Autowired
    private AttendanceTask attendanceTask;

    @Autowired
    private AreaFenceDataService areaFenceDataService;

    // 自定义ObjectMapper，用于处理时间字段的序列化
    private final ObjectMapper objectMapper;

    // 构造函数初始化objectMapper
    public SwmDailyAttendanceController() {
        this.objectMapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();

        // 为Date类型注册时间格式化序列化器
        module.addSerializer(Date.class, new JsonSerializer<Date>() {
            @Override
            public void serialize(Date date, JsonGenerator jsonGenerator, SerializerProvider serializerProvider)
                    throws java.io.IOException {
                if (date == null) {
                    jsonGenerator.writeNull();
                    return;
                }

                // 获取字段名，用于判断是否是时间字段
                String fieldName = jsonGenerator.getOutputContext().getCurrentName();

                // 为打卡时间字段使用仅时间的格式
                if ("clockInTime".equals(fieldName) || "clockOutTime".equals(fieldName)) {
                    SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
                    jsonGenerator.writeString(timeFormat.format(date));
                } else {
                    // 其他日期字段使用标准格式
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    jsonGenerator.writeString(dateFormat.format(date));
                }
            }
        });

        objectMapper.registerModule(module);
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    // 将对象转换为Map，处理日期格式
    private Map<String, Object> convertToMap(Object object) {
        try {
            // 将对象转换为JSON字符串，再转回Map
            String json = objectMapper.writeValueAsString(object);
            return objectMapper.readValue(json, Map.class);
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyMap();
        }
    }

    /**
     * 获取数据
     */
    @ModelAttribute
    public SwmDailyAttendance get(String id, boolean isNewRecord) {
        return swmDailyAttendanceService.get(id, isNewRecord);
    }

    /**
     * 查询列表
     */
    @RequestMapping(value = { "list", "" })
    public String list(SwmDailyAttendance swmDailyAttendance, Model model) {
        model.addAttribute("swmDailyAttendance", swmDailyAttendance);
        return "modules/swm/swmDailyAttendanceList";
    }

    /**
     * 查询列表数据
     */
    @RequestMapping(value = "listData")
    @ResponseBody
    public Page<Map<String, Object>> listData(SwmDailyAttendance swmDailyAttendance, HttpServletRequest request,
            HttpServletResponse response) {
        if (swmDailyAttendance.getAttendanceDate() == null &&
                swmDailyAttendance.getBeginAttendanceDate() == null &&
                swmDailyAttendance.getEndAttendanceDate() == null) {

            Date today = new Date();

            swmDailyAttendance.setAttendanceDate(today);
        }

        swmDailyAttendance.setPage(new Page<>(request, response));
        Page<SwmDailyAttendance> originalPage = swmDailyAttendanceService.findPage(swmDailyAttendance);

        // 创建新的分页对象，用于存储格式化后的数据
        Page<Map<String, Object>> formattedPage = new Page<>(request, response);
        formattedPage.setCount(originalPage.getCount());
        formattedPage.setPageNo(originalPage.getPageNo());
        formattedPage.setPageSize(originalPage.getPageSize());

        // 处理日期格式并计算怠工时长
        List<Map<String, Object>> formattedList = new ArrayList<>();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        for (SwmDailyAttendance record : originalPage.getList()) {
            Map<String, Object> recordMap = convertToMap(record);

            // 计算怠工时长
            try {
                String idCard = swmDailyAttendanceService.getIdCardByEmployeeId(record.getEmployeeId());
                if (idCard != null && record.getAttendanceDate() != null) {
                    String dateStr = dateFormat.format(record.getAttendanceDate());
                    double calculatedIdleHours = swmDailyAttendanceService.calculateIdleTimeByIdCard(idCard, dateStr);
                    recordMap.put("idleHours", calculatedIdleHours); // 怠工时长字段。
                    logger.debug("员工ID {} 身份证号 {} 在 {} 的怠工时长: {} 小时",
                            record.getEmployeeId(), idCard, dateStr, calculatedIdleHours);
                }
            } catch (Exception e) {
                logger.error("计算员工 {} 怠工时长失败", record.getEmployeeId(), e);
                recordMap.put("calculatedIdleHours", 0.0);
            }

            formattedList.add(recordMap);
        }

        formattedPage.setList(formattedList);

        return formattedPage;
    }

    /**
     * 查看编辑表单
     */
    @RequestMapping(value = "form")
    public String form(SwmDailyAttendance swmDailyAttendance, Model model) {
        model.addAttribute("swmDailyAttendance", swmDailyAttendance);
        return "modules/swm/swmDailyAttendanceForm";
    }

    /**
     * 保存数据
     */
    @PostMapping(value = "save")
    @ResponseBody
    @ApiOperation("保存数据")
    public String save(@Validated SwmDailyAttendance swmDailyAttendance, HttpServletRequest request) {
        logger.info("接收到的考勤数据: {}", swmDailyAttendance.toString());

        try {
            // 处理上班打卡时间
            if (request.getParameter("clockInTime") != null && !request.getParameter("clockInTime").isEmpty()) {
                String clockInTimeStr = request.getParameter("clockInTime");
                logger.info("接收到的上班打卡时间: {}", clockInTimeStr);

                try {
                    // 先尝试解析完整的日期时间格式
                    SimpleDateFormat fullDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    swmDailyAttendance.setClockInTime(fullDateFormat.parse(clockInTimeStr));
                } catch (Exception e1) {
                    try {
                        // 如果失败，尝试解析只有时间部分的格式
                        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
                        Date timeOnly = timeFormat.parse(clockInTimeStr);

                        // 合并日期部分和时间部分
                        Calendar calendar = Calendar.getInstance();
                        calendar.setTime(swmDailyAttendance.getAttendanceDate()); // 使用考勤日期的日期部分
                        Calendar timeCal = Calendar.getInstance();
                        timeCal.setTime(timeOnly);

                        calendar.set(Calendar.HOUR_OF_DAY, timeCal.get(Calendar.HOUR_OF_DAY));
                        calendar.set(Calendar.MINUTE, timeCal.get(Calendar.MINUTE));
                        calendar.set(Calendar.SECOND, timeCal.get(Calendar.SECOND));

                        swmDailyAttendance.setClockInTime(calendar.getTime());
                    } catch (Exception e2) {
                        logger.error("解析上班打卡时间失败: {}", e2.getMessage());
                    }
                }
            }

            // 处理下班打卡时间
            if (request.getParameter("clockOutTime") != null && !request.getParameter("clockOutTime").isEmpty()) {
                String clockOutTimeStr = request.getParameter("clockOutTime");
                logger.info("接收到的下班打卡时间: {}", clockOutTimeStr);

                try {
                    // 先尝试解析完整的日期时间格式
                    SimpleDateFormat fullDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    swmDailyAttendance.setClockOutTime(fullDateFormat.parse(clockOutTimeStr));
                } catch (Exception e1) {
                    try {
                        // 如果失败，尝试解析只有时间部分的格式
                        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
                        Date timeOnly = timeFormat.parse(clockOutTimeStr);

                        // 合并日期部分和时间部分
                        Calendar calendar = Calendar.getInstance();
                        calendar.setTime(swmDailyAttendance.getAttendanceDate()); // 使用考勤日期的日期部分
                        Calendar timeCal = Calendar.getInstance();
                        timeCal.setTime(timeOnly);

                        calendar.set(Calendar.HOUR_OF_DAY, timeCal.get(Calendar.HOUR_OF_DAY));
                        calendar.set(Calendar.MINUTE, timeCal.get(Calendar.MINUTE));
                        calendar.set(Calendar.SECOND, timeCal.get(Calendar.SECOND));

                        swmDailyAttendance.setClockOutTime(calendar.getTime());
                    } catch (Exception e2) {
                        logger.error("解析下班打卡时间失败: {}", e2.getMessage());
                    }
                }
            }

            // 确保打卡时间已经设置
            logger.info("处理后的上班打卡时间: {}, 下班打卡时间: {}",
                    swmDailyAttendance.getClockInTime(),
                    swmDailyAttendance.getClockOutTime());

            // 保存数据
            swmDailyAttendanceService.save(swmDailyAttendance);

            logger.info("保存成功，ID: {}", swmDailyAttendance.getId());
            return renderResult(Global.TRUE, text("保存日考勤统计表成功！"));
        } catch (Exception e) {
            logger.error("保存考勤记录失败: {}", e.getMessage(), e);
            return renderResult(Global.FALSE, text("保存日考勤统计表失败！") + e.getMessage());
        }
    }

    /**
     * 删除数据
     */
    @RequestMapping(value = "delete")
    @ResponseBody
    @ApiOperation("删除数据")
    public String delete(SwmDailyAttendance swmDailyAttendance) {
        swmDailyAttendanceService.delete(swmDailyAttendance);
        return renderResult(Global.TRUE, text("删除日考勤统计表成功！"));
    }

    /**
     * 根据员工姓名和日期查询考勤记录
     */
    @GetMapping(value = "findByEmployeeAndDate")
    @ResponseBody
    @ApiOperation("根据员工姓名和日期查询考勤记录")
    public Map<String, Object> findByEmployeeAndDate(String employeeName,
            @DateTimeFormat(pattern = "yyyy-MM-dd") Date attendanceDate) {
        Map<String, Object> result = new HashMap<>();
        if (employeeName != null && !employeeName.isEmpty() && attendanceDate != null) {
            SwmDailyAttendance record = swmDailyAttendanceService.findByEmployeeAndDate(employeeName, attendanceDate);
            if (record != null) {
                // 使用自定义方法处理日期格式
                Map<String, Object> formattedRecord = convertToMap(record);
                result.put("record", formattedRecord);
                result.put("success", true);
            } else {
                result.put("record", null);
                result.put("success", true);
                result.put("message", "未找到考勤记录");
            }
        } else {
            result.put("success", false);
            result.put("message", "员工姓名和考勤日期不能为空");
        }
        return result;
    }

    /**
     * 根据员工姓名和日期范围查询考勤记录
     */
    @GetMapping(value = "findByEmployeeAndDateRange")
    @ResponseBody
    @ApiOperation("根据员工姓名和日期范围查询考勤记录")
    public Map<String, Object> findByEmployeeAndDateRange(String employeeName,
            @DateTimeFormat(pattern = "yyyy-MM-dd") Date beginDate,
            @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate) {
        Map<String, Object> result = new HashMap<>();
        if (employeeName != null && !employeeName.isEmpty() && beginDate != null && endDate != null) {
            List<SwmDailyAttendance> recordList = swmDailyAttendanceService.findByEmployeeAndDateRange(employeeName,
                    beginDate, endDate);

            // 处理日期格式
            List<Map<String, Object>> formattedList = new ArrayList<>();
            for (SwmDailyAttendance record : recordList) {
                formattedList.add(convertToMap(record));
            }

            result.put("list", formattedList);
            result.put("success", true);
        } else {
            result.put("success", false);
            result.put("message", "员工姓名和日期范围不能为空");
        }
        return result;
    }

    /**
     * 根据日期查询所有考勤记录
     */
    @GetMapping(value = "findByDate")
    @ResponseBody
    @ApiOperation("根据日期查询所有考勤记录")
    public Map<String, Object> findByDate(@DateTimeFormat(pattern = "yyyy-MM-dd") Date attendanceDate) {
        Map<String, Object> result = new HashMap<>();
        if (attendanceDate != null) {
            List<SwmDailyAttendance> recordList = swmDailyAttendanceService.findByDate(attendanceDate);

            // 处理日期格式
            List<Map<String, Object>> formattedList = new ArrayList<>();
            for (SwmDailyAttendance record : recordList) {
                formattedList.add(convertToMap(record));
            }

            result.put("list", formattedList);
            result.put("success", true);
        } else {
            result.put("success", false);
            result.put("message", "考勤日期不能为空");
        }
        return result;
    }

    /**
     * 计算月统计数据并保存
     */
    @PostMapping(value = "calculateAndSaveMonthly")
    @ResponseBody
    @ApiOperation("计算月统计数据并保存")
    public String calculateAndSaveMonthly(String employeeName, int year, int month) {
        if (employeeName == null || employeeName.isEmpty()) {
            return renderResult(Global.FALSE, text("员工姓名不能为空！"));
        }

        SwmAttendanceSummary summary = swmDailyAttendanceService.calculateMonthlyStats(employeeName, year, month);

        SwmAttendanceSummary existing = swmAttendanceSummaryService.findByEmployeeAndMonth(employeeName,
                String.format("%04d-%02d", year, month));

        if (existing != null) {
            summary.setId(existing.getId());
            summary.setIsNewRecord(false);

            if (existing.getDepartment() != null) {
                summary.setDepartment(existing.getDepartment());
            }
            if (existing.getWorkProcess() != null) {
                summary.setWorkProcess(existing.getWorkProcess());
            }
            if (existing.getTeam() != null) {
                summary.setTeam(existing.getTeam());
            }
            if (existing.getJobType() != null) {
                summary.setJobType(existing.getJobType());
            }
            if (existing.getWorkShift() != null) {
                summary.setWorkShift(existing.getWorkShift());
            }
        }

        swmAttendanceSummaryService.save(summary);

        return renderResult(Global.TRUE, text("计算并保存月统计数据成功！"));
    }

    /**
     * 获取员工月度考勤图表数据
     * 根据员工ID和月份查询日考勤数据，返回适合图表展示的格式
     */
    @GetMapping(value = "getMonthlyChartData")
    @ResponseBody
    @ApiOperation("获取员工月度考勤图表数据")
    public Map<String, Object> getMonthlyChartData(String employeeId, String month) {
        Map<String, Object> result = new HashMap<>();

        // 验证参数
        if (employeeId == null || employeeId.isEmpty() || month == null || month.isEmpty()) {
            result.put("code", 400);
            result.put("success", false);
            result.put("message", "员工ID和月份不能为空");
            return result;
        }

        // 解析月份格式 (YYYY-MM)
        int year, monthOfYear;
        try {
            String[] parts = month.split("-");
            if (parts.length != 2) {
                throw new ParseException("月份格式不正确", 0);
            }
            year = Integer.parseInt(parts[0]);
            monthOfYear = Integer.parseInt(parts[1]);

            if (monthOfYear < 1 || monthOfYear > 12) {
                throw new ParseException("月份必须在1-12之间", 0);
            }
        } catch (Exception e) {
            result.put("code", 400);
            result.put("success", false);
            result.put("message", "月份格式不正确，应为YYYY-MM");
            return result;
        }

        // 调用Service层方法获取图表数据
        Map<String, Object> data = swmDailyAttendanceService.getMonthlyChartData(employeeId, year, monthOfYear);

        // 构建返回结果
        result.put("code", 200);
        result.put("success", true);
        result.put("data", data);

        return result;
    }

    /**
     * 获取员工月度考勤时长数据
     * 根据员工ID和月份查询日考勤数据，返回应考勤时长和实际考勤时长
     */
    @GetMapping(value = "getMonthlyAttendanceData")
    @ResponseBody
    @ApiOperation("获取员工月度考勤时长数据")
    public Map<String, Object> getMonthlyAttendanceData(String employeeId, String month) {
        Map<String, Object> result = new HashMap<>();

        // 验证参数
        if (employeeId == null || employeeId.isEmpty() || month == null || month.isEmpty()) {
            result.put("code", 400);
            result.put("success", false);
            result.put("message", "员工ID和月份不能为空");
            return result;
        }

        // 解析月份格式 (YYYY-MM)
        int year, monthOfYear;
        try {
            String[] parts = month.split("-");
            if (parts.length != 2) {
                throw new ParseException("月份格式不正确", 0);
            }
            year = Integer.parseInt(parts[0]);
            monthOfYear = Integer.parseInt(parts[1]);

            if (monthOfYear < 1 || monthOfYear > 12) {
                throw new ParseException("月份必须在1-12之间", 0);
            }
        } catch (Exception e) {
            result.put("code", 400);
            result.put("success", false);
            result.put("message", "月份格式不正确，应为YYYY-MM");
            return result;
        }

        // 调用Service层方法获取考勤时长数据
        Map<String, Object> data = swmDailyAttendanceService.getMonthlyAttendanceData(employeeId, year, monthOfYear);

        // 构建返回结果
        result.put("code", 200);
        result.put("success", true);
        result.put("data", data);

        return result;
    }

    /**
     * 根据员工ID和日期查询单条考勤记录
     */
    @GetMapping(value = "getAttendanceByEmployeeAndDate")
    @ResponseBody
    @ApiOperation("根据员工ID和日期查询单条考勤记录")
    public Map<String, Object> getAttendanceByEmployeeAndDate(String employeeId,
            @DateTimeFormat(pattern = "yyyy-MM-dd") Date date) {
        Map<String, Object> result = new HashMap<>();
        if (employeeId != null && !employeeId.isEmpty() && date != null) {
            SwmDailyAttendance record = swmDailyAttendanceService.findByEmployeeIdAndDate(employeeId, date);
            if (record != null) {
                // 使用自定义方法处理日期格式
                result = convertToMap(record);
                result.put("success", true);
            } else {
                result.put("data", null);
                result.put("success", true);
                result.put("message", "未找到考勤记录");
            }
        } else {
            result.put("success", false);
            result.put("message", "员工ID和日期不能为空");
        }
        return result;
    }

    /**
     * 根据ID获取单条考勤记录
     */
    @GetMapping(value = "get")
    @ResponseBody
    @ApiOperation("根据ID获取单条考勤记录")
    public Map<String, Object> get(String id) {
        Map<String, Object> result = new HashMap<>();

        if (id != null && !id.isEmpty()) {
            SwmDailyAttendance record = swmDailyAttendanceService.get(id);
            if (record != null) {
                // 将对象转换为Map，处理日期格式
                result = convertToMap(record);
                result.put("success", true);
            } else {
                result.put("success", false);
                result.put("message", "未找到指定的考勤记录");
            }
        } else {
            result.put("success", false);
            result.put("message", "ID不能为空");
        }

        return result;
    }

    /**
     * 手动创建每日考勤数据
     * 用于测试或手动触发创建当日考勤记录
     */
    @PostMapping(value = "createDailyAttendance")
    @ResponseBody
    @ApiOperation("创建每日考勤数据")
    public String createDailyAttendance() {
        try {
            logger.info("开始手动创建每日考勤数据");

            // 调用定时任务中的方法
            attendanceTask.createDailyAttendance();

            logger.info("手动创建每日考勤数据完成");
            return renderResult(Global.TRUE, "创建每日考勤数据成功！");
        } catch (Exception e) {
            logger.error("创建每日考勤数据失败: {}", e.getMessage(), e);
            return renderResult(Global.FALSE, "创建每日考勤数据失败！" + e.getMessage());
        }
    }

}