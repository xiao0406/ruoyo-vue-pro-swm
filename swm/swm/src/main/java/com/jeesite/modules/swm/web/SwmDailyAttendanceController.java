package com.jeesite.modules.swm.web;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.jeesite.common.config.Global;
import com.jeesite.common.entity.Page;
import com.jeesite.common.lang.DateUtils;
import com.jeesite.common.lang.StringUtils;
import com.jeesite.common.mybatis.mapper.query.QueryType;
import com.jeesite.common.utils.excel.ExcelExport;
import com.jeesite.common.web.BaseController;
import com.jeesite.modules.cache.service.RedisService;
import com.jeesite.modules.job.task.AttendanceTask;
import com.jeesite.modules.constant.SwmRedisConstant;
import com.jeesite.modules.swm.entity.*;
import com.jeesite.modules.swm.job.FmsMonthPlanProlongTask;
import com.jeesite.modules.swm.service.AreaFenceDataService;
import com.jeesite.modules.swm.service.SwmAttendanceSummaryService;
import com.jeesite.modules.swm.service.SwmDailyAttendanceService;
import com.jeesite.modules.sys.utils.CorpUtils;
import com.jeesite.modules.util.MinioUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

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

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory
            .getLogger(SwmDailyAttendanceController.class);

    @Autowired
    private SwmDailyAttendanceService swmDailyAttendanceService;

    @Autowired
    private SwmAttendanceSummaryService swmAttendanceSummaryService;

    @Autowired
    private MinioUtils minioUtils;

    @Autowired
    private AttendanceTask attendanceTask;

    @Autowired
    private FmsMonthPlanProlongTask fmsMonthPlanProlongTask;

    @Autowired
    private AreaFenceDataService areaFenceDataService;
    @Autowired
    private RedisService redisService;

    // 自定义ObjectMapper，用于处理时间字段的序列化
    private final ObjectMapper objectMapper;

    private static final String SESSION_CORP_CODE = "corpCode";

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
                } else if ("clockInDate".equals(fieldName) || "clockOutDate".equals(fieldName)) {
                    // 完整的打卡日期时间字段使用完整格式
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    jsonGenerator.writeString(dateFormat.format(date));
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
            Map<String, Object> result = objectMapper.readValue(json, Map.class);

            // 如果是SwmDailyAttendance对象，获取实时位置并设置currentPosition
            if (object instanceof SwmDailyAttendance) {
                SwmDailyAttendance attendance = (SwmDailyAttendance) object;
                String currentPosition = getRealTimePosition(attendance.getEmployeeId());
                result.put("currentPosition", currentPosition);
            }

            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyMap();
        }
    }

    /**
     * 获取员工的实时位置
     *
     * @param employeeId 员工ID
     * @return "0"-工作区, "1"-休息区, "3"-未知
     */
    private String getRealTimePosition(String employeeId) {
        if (StringUtils.isBlank(employeeId)) {
            return "3";
        }
        try {
            // 1. 获取员工身份证号
            String idCard = swmDailyAttendanceService.getIdCardByEmployeeId(employeeId);
            if (idCard == null) {
                logger.warn("无法根据员工ID {} 找到身份证号", employeeId);
                return "3";
            }
            // 2. 调用服务查询实时位置
            return areaFenceDataService.getCurrentLocationByIdCard(idCard);
        } catch (Exception e) {
            logger.error("获取员工 {} 实时位置失败", employeeId, e);
            return "3";
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

    @RequestMapping(value = "listData")
    @ResponseBody
    public Page<Map<String, Object>> listData(SwmDailyAttendance swmDailyAttendance, HttpServletRequest request,
                                              HttpServletResponse response) {

        String corpCode = CorpUtils.getCurrentCorpCode();

        // 1. 日期默认值处理（原有逻辑保留）
        if (swmDailyAttendance.getAttendanceDate() == null &&
                swmDailyAttendance.getBeginAttendanceDate() == null &&
                swmDailyAttendance.getEndAttendanceDate() == null) {
            Date today = new Date();
            swmDailyAttendance.setAttendanceDate(today);
        }

        // 2. 获取在线人员身份证集合（原有逻辑保留，新增日志打印）

        String onlineDevicesKey2 = corpCode+SwmRedisConstant.RedisIotKey.ONLINE_DEVICES_KEY;
        Set<Object> deviceIds = redisService.sGet(onlineDevicesKey2);
        Set<String> todayOnSiteIdCards = new HashSet<>();

        // ===== 新增：打印deviceIds的核心日志 =====
        // 1. 打印deviceIds的基础信息（是否为空、元素数量）
        logger.info("从Redis获取的在线设备ID集合(ONLINE_DEVICES_KEY)：{}，集合大小：{}",
                (deviceIds == null ? "null" : deviceIds),
                (deviceIds == null ? 0 : deviceIds.size()));

        if (deviceIds != null && !deviceIds.isEmpty()) { // 新增：判空避免无效遍历
            for (Object deviceId : deviceIds) {
                String currentPerson = (String) redisService.hget(SwmRedisConstant.RedisGlobalKey.DEVICE_PERSON_MAP, String.valueOf(deviceId));

                // 2. 打印每个deviceId对应的身份证（便于排查单个设备的映射问题）
                logger.info("设备ID：{}，映射的身份证号：{}", deviceId, currentPerson);

                if (StringUtils.isNotEmpty(currentPerson)){ // 新增：判空避免空字符串加入
                    todayOnSiteIdCards.add(currentPerson);
                }
            }
        }

        // 3. 打印最终收集到的身份证集合（验证结果）
        logger.info("最终收集到的在线人员身份证集合：{}，集合大小：{}",
                todayOnSiteIdCards, todayOnSiteIdCards.size());

        // 3. 在线/离线筛选（核心修复：处理空集合+表别名+逻辑兜底）
        String powerOnStatus = swmDailyAttendance.getPowerOnStatus();
        if (StringUtils.isNotEmpty(powerOnStatus)) {
            List<String> idCardList = new ArrayList<>(todayOnSiteIdCards);
            // 关键：表别名建议通过常量/配置获取，避免硬编码
            String identityCardColumn = "a.identity_card";

            if ("0".equals(powerOnStatus)) {
                // 在线：空集合时不拼接条件（或根据业务返回空）
                if (!idCardList.isEmpty()) {
                    swmDailyAttendance.getSqlMap().getWhere().and(identityCardColumn, QueryType.IN, idCardList);
                } else {
                    // 业务兜底：在线但无人员，直接返回空结果（避免SQL错误）
                    Page<Map<String, Object>> emptyPage = new Page<>(request, response);
                    emptyPage.setCount(0);
                    return emptyPage;
                }
            } else {
                // 离线：空集合时拼接 1=1（或根据业务调整），非空时拼接NOT IN
                if (!idCardList.isEmpty()) {
                    swmDailyAttendance.getSqlMap().getWhere().and(identityCardColumn, QueryType.NOT_IN, idCardList);
                } else {
                    // 兜底：无在线人员 → 所有人员都是离线，不拼接筛选条件（或拼接 1=1）
                    // swmDailyAttendance.getSqlMap().getWhere().and("1", QueryType.EQ, "1");
                }
            }
            logger.info("在线人员身份证集合：{}，筛选条件：powerOnStatus={}", todayOnSiteIdCards, powerOnStatus);
        }

        // 4. 执行分页查询（确保sqlMap的条件被带入）
        swmDailyAttendance.setPage(new Page<>(request, response));
        Page<SwmDailyAttendance> originalPage = swmDailyAttendanceService.findPage(swmDailyAttendance);

        // 5. 格式化结果（原有逻辑保留，新增powerOnStatus赋值）
        Page<Map<String, Object>> formattedPage = new Page<>(request, response);
        formattedPage.setCount(originalPage.getCount());
        formattedPage.setPageNo(originalPage.getPageNo());
        formattedPage.setPageSize(originalPage.getPageSize());

        List<Map<String, Object>> formattedList = new ArrayList<>();
        for (SwmDailyAttendance record : originalPage.getList()) {
            // 赋值在线状态（前端展示用）
            record.setPowerOnStatus(todayOnSiteIdCards.contains(record.getIdentityCard()) ? "0" : "1");
            formattedList.add(convertToMap(record));
        }
        formattedPage.setList(formattedList);

        return formattedPage;
    }

//    /**
//     * 查询列表数据
//     */
//    @RequestMapping(value = "listData")
//    @ResponseBody
//    public Page<Map<String, Object>> listData(SwmDailyAttendance swmDailyAttendance, HttpServletRequest request,
//            HttpServletResponse response) {
//        if (swmDailyAttendance.getAttendanceDate() == null &&
//                swmDailyAttendance.getBeginAttendanceDate() == null &&
//                swmDailyAttendance.getEndAttendanceDate() == null) {
//
//            Date today = new Date();
//
//            swmDailyAttendance.setAttendanceDate(today);
//        }
//
//        Set<Object> deviceIds = redisService.sGet(SwmRedisConstant.RedisIotKey.ONLINE_DEVICES_KEY);
//        Set<String> todayOnSiteIdCards = new HashSet<>();
//        if (deviceIds != null) {
//            for (Object deviceId : deviceIds) {
//                String currentPerson = (String) redisService.hget(SwmRedisConstant.RedisGlobalKey.DEVICE_PERSON_MAP, String.valueOf(deviceId));
//                if (currentPerson != null){
//                    todayOnSiteIdCards.add(currentPerson);
//                }
//            }
//        }
//
//        //在线
//        if (StringUtils.isNotEmpty(swmDailyAttendance.getPowerOnStatus())){
//            if ("0".equals(swmDailyAttendance.getPowerOnStatus())){
//                swmDailyAttendance.getSqlMap().getWhere().and("a.identity_card", QueryType.IN, new ArrayList<>(todayOnSiteIdCards));
//            }else {
//                swmDailyAttendance.getSqlMap().getWhere().and("a.identity_card", QueryType.NOT_IN,  new ArrayList<>(todayOnSiteIdCards));
//            }
//        }
//        swmDailyAttendance.setPage(new Page<>(request, response));
//        Page<SwmDailyAttendance> originalPage = swmDailyAttendanceService.findPage(swmDailyAttendance);
//
//        // 创建新的分页对象，用于存储格式化后的数据
//        Page<Map<String, Object>> formattedPage = new Page<>(request, response);
//        formattedPage.setCount(originalPage.getCount());
//        formattedPage.setPageNo(originalPage.getPageNo());
//        formattedPage.setPageSize(originalPage.getPageSize());
//
//        // 处理日期格式并计算怠工时长
//        List<Map<String, Object>> formattedList = new ArrayList<>();
//
//        for (SwmDailyAttendance record : originalPage.getList()) {
//            if(todayOnSiteIdCards.contains(record.getIdentityCard())){
//                record.setPowerOnStatus("0");
//            }else {
//                record.setPowerOnStatus("1");
//            }
//            formattedList.add(convertToMap(record));
//        }
//
//        formattedPage.setList(formattedList);
//
//        return formattedPage;
//    }

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
                    Date fullDateTime = fullDateFormat.parse(clockInTimeStr);
                    swmDailyAttendance.setClockInTime(fullDateTime);
                    // 同时设置完整的打卡日期时间
                    swmDailyAttendance.setClockInDate(fullDateTime);
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
                        // 同时设置完整的打卡日期时间
                        swmDailyAttendance.setClockInDate(calendar.getTime());
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
                    Date fullDateTime = fullDateFormat.parse(clockOutTimeStr);
                    swmDailyAttendance.setClockOutTime(fullDateTime);
                    // 同时设置完整的打卡日期时间
                    swmDailyAttendance.setClockOutDate(fullDateTime);
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
                        // 同时设置完整的打卡日期时间
                        swmDailyAttendance.setClockOutDate(calendar.getTime());
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

    /**
     * 手动计算怠工时长
     * 用于测试或手动触发怠工时长计算定时任务
     * 
     * @author: Shawn
     * @date: 2025/6/20
     */
    @PostMapping(value = "calculateIdleHours")
    @ResponseBody
    @ApiOperation("计算怠工时长")
    public String calculateIdleHours() {
        try {
            logger.info("开始手动计算怠工时长");

            // 调用定时任务中的方法
            attendanceTask.calculateIdleHours();

            logger.info("手动计算怠工时长完成");
            return renderResult(Global.TRUE, "计算怠工时长成功！");
        } catch (Exception e) {
            logger.error("计算怠工时长失败: {}", e.getMessage(), e);
            return renderResult(Global.FALSE, "计算怠工时长失败！" + e.getMessage());
        }
    }

    /**
     * 手动执行月考勤统计任务
     * 用于测试或手动触发月考勤统计定时任务
     * 
     * @author: Shawn
     * @date: 2025/01/27
     */
    @PostMapping(value = "calculateMonthlyAttendance")
    @ResponseBody
    @ApiOperation("执行月考勤统计任务")
    public String calculateMonthlyAttendance() {
        try {
            logger.info("开始手动执行月考勤统计任务");

            // 调用定时任务中的方法
            attendanceTask.calculateMonthlyAttendance();

            logger.info("手动执行月考勤统计任务完成");
            return renderResult(Global.TRUE, "月考勤统计任务执行成功！");
        } catch (Exception e) {
            logger.error("月考勤统计任务执行失败: {}", e.getMessage(), e);
            return renderResult(Global.FALSE, "月考勤统计任务执行失败！" + e.getMessage());
        }
    }

    /**
     * 手动触发月度计划顺延任务
     * 用于测试或手动触发
     * 
     * @author: Shawn
     * @date: 2025/06/30
     */
    @PostMapping(value = "runFmsMonthPlanProlongTask")
    @ResponseBody
    @ApiOperation("手动触发月度计划顺延任务")
    public String runFmsMonthPlanProlongTask() {
        try {
            logger.info("开始手动触发月度计划顺延任务");
            fmsMonthPlanProlongTask.execute();
            logger.info("手动触发月度计划顺延任务完成");
            return renderResult(Global.TRUE, "月度计划顺延任务执行成功！");
        } catch (Exception e) {
            logger.error("月度计划顺延任务执行失败: {}", e.getMessage(), e);
            return renderResult(Global.FALSE, "月度计划顺延任务执行失败！" + e.getMessage());
        }
    }

    /**
     * 导出日考勤记录
     */
    @RequestMapping(value = "exportData")
    @ResponseBody
    @ApiOperation("导出日考勤记录")
    public String exportData(SwmDailyAttendance swmDailyAttendance, HttpServletRequest request,
            HttpServletResponse response) throws IOException {
        // 验证日期参数，确保只导出一天数据
        if (swmDailyAttendance.getAttendanceDate() == null) {
            return renderResult(Global.FALSE, text("请选择考勤日期！"));
        }

        try {

            String corpCode = CorpUtils.getCurrentCorpCode();
            Set<Object> deviceIds = redisService.sGet(corpCode + SwmRedisConstant.RedisIotKey.ONLINE_DEVICES_KEY);
            Set<String> todayOnSiteIdCards = new HashSet<>();
            if (deviceIds != null) {
                for (Object deviceId : deviceIds) {
                    String currentPerson = (String) redisService.hget(SwmRedisConstant.RedisGlobalKey.DEVICE_PERSON_MAP, String.valueOf(deviceId));
                    if (currentPerson != null){
                        todayOnSiteIdCards.add(currentPerson);
                    }
                }
            }

            // 3. 在线/离线筛选（核心修复：处理空集合+表别名+逻辑兜底）
            String powerOnStatus = swmDailyAttendance.getPowerOnStatus();
            if (StringUtils.isNotEmpty(powerOnStatus)) {
                List<String> idCardList = new ArrayList<>(todayOnSiteIdCards);
                // 关键：表别名建议通过常量/配置获取，避免硬编码
                String identityCardColumn = "a.identity_card";

                if ("0".equals(powerOnStatus)) {
                    // 在线：空集合时不拼接条件（或根据业务返回空）
                    if (!idCardList.isEmpty()) {
                        swmDailyAttendance.getSqlMap().getWhere().and(identityCardColumn, QueryType.IN, idCardList);
                    }
                } else {
                    // 离线：空集合时拼接 1=1（或根据业务调整），非空时拼接NOT IN
                    if (!idCardList.isEmpty()) {
                        swmDailyAttendance.getSqlMap().getWhere().and(identityCardColumn, QueryType.NOT_IN, idCardList);
                    }
                }
            }


            // 获取所有符合条件的数据（不分页）
            List<SwmDailyAttendance> list = swmDailyAttendanceService.findExportList(swmDailyAttendance);
            for (SwmDailyAttendance attendance : list) {
                if (todayOnSiteIdCards.contains(attendance.getIdentityCard())) {
                    attendance.setPowerOnStatus("开机");
                } else {
                    attendance.setPowerOnStatus("关机");
                }
            }

            if (list.isEmpty()) {
                return renderResult(Global.FALSE, text("没有符合条件的数据可以导出！"));
            }

            // 转换为导出实体
            List<SwmDailyAttendanceExportEntity> exportList = swmDailyAttendanceService.convertToExportList(list);

            // 生成文件名
            String fileName = "日考勤记录_" + DateUtils.formatDate(swmDailyAttendance.getAttendanceDate(), "yyyyMMdd") + "_"
                    + DateUtils.getDate("yyyyMMddHHmmss") + ".xlsx";

            // 使用ExcelExport生成Excel文件
            byte[] excelData;
            try (ExcelExport ee = new ExcelExport("日考勤记录", SwmDailyAttendanceExportEntity.class);
                    ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                ee.setDataList(exportList);
                ee.getWorkbook().write(baos);
                excelData = baos.toByteArray();
            }

            // 创建MockMultipartFile用于上传到MinIO
            MockMultipartFile mockFile = new MockMultipartFile(
                    "file",
                    fileName,
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                    excelData);

            // 构建MinIO存储路径
            String objectName = "daily-attendance/"
                    + DateUtils.formatDate(swmDailyAttendance.getAttendanceDate(), "yyyyMMdd") + "/" + fileName;
            logger.info("Excel文件将存储在MinIO路径: {}", objectName);

            // 使用MinioUtils上传文件到MinIO
            Map<String, Object> uploadResult = minioUtils.upload(mockFile, objectName);
            logger.info("MinIO上传成功，返回结果: {}", uploadResult);

            // 生成预览URL，使用相对路径，并添加fileName参数
            String previewUrl = "fileUpload/preview?objectName=" + objectName + "&fileName=" + fileName;
            logger.info("生成预览URL: {}", previewUrl);

            return renderResult(Global.TRUE, text("导出成功！"), previewUrl);
        } catch (Exception e) {
            logger.error("导出日考勤记录失败: {}", e.getMessage(), e);
            return renderResult(Global.FALSE, text("导出失败！") + e.getMessage());
        }
    }

    /**
     * 导出月考勤记录
     */
    @RequestMapping(value = "exportMonthlyData")
    @ResponseBody
    @ApiOperation("导出月考勤记录")
    public String exportMonthlyData(SwmMonthlyAttendance swmMonthlyAttendance, HttpServletRequest request,
                             HttpServletResponse response) throws IOException {

        // 验证日期参数，确保只导出一天数据
        if (swmMonthlyAttendance.getCurrentMonth() == null) {
            return renderResult(Global.FALSE, text("请选择考勤月份！"));
        }

        try {
            // 获取所有符合条件的数据（不分页）
            List<SwmMonthlyAttendance> list = swmDailyAttendanceService.findExportListByMonth(swmMonthlyAttendance);

            if (list.isEmpty()) {
                return renderResult(Global.FALSE, text("没有符合条件的数据可以导出！"));
            }

            // 转换为导出实体
            List<SwmMonthlyAttendanceExportEntity> exportList = swmDailyAttendanceService.monthlyConvertToExportList(list);

            // 生成文件名
            String fileName = "月考勤记录_" + DateUtils.formatDate(swmMonthlyAttendance.getCurrentMonth(), "yyyyMM") + "_"
                    + DateUtils.getDate("yyyyMMddHHmmss") + ".xlsx";

            // 使用ExcelExport生成Excel文件
            byte[] excelData;
            try (ExcelExport ee = new ExcelExport("月考勤记录", SwmMonthlyAttendanceExportEntity.class);
                 ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                ee.setDataList(exportList);
                ee.getWorkbook().write(baos);
                excelData = baos.toByteArray();
            }

            // 创建MockMultipartFile用于上传到MinIO
            MockMultipartFile mockFile = new MockMultipartFile(
                    "file",
                    fileName,
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                    excelData);

            // 构建MinIO存储路径
            String objectName = "monthly-attendance/"
                    + DateUtils.formatDate(swmMonthlyAttendance.getCurrentMonth(), "yyyyMM") + "/" + fileName;
            logger.info("Excel文件将存储在MinIO路径: {}", objectName);

            // 使用MinioUtils上传文件到MinIO
            Map<String, Object> uploadResult = minioUtils.upload(mockFile, objectName);
            logger.info("MinIO上传成功，返回结果: {}", uploadResult);

            // 生成预览URL，使用相对路径，并添加fileName参数
            String previewUrl = "fileUpload/preview?objectName=" + objectName + "&fileName=" + fileName;
            logger.info("生成预览URL: {}", previewUrl);

            return renderResult(Global.TRUE, text("导出成功！"), previewUrl);
        } catch (Exception e) {
            logger.error("导出月考勤记录失败: {}", e.getMessage(), e);
            return renderResult(Global.FALSE, text("导出失败！") + e.getMessage());
        }
    }

    /**
     * 月考核查询列表数据
     */
    @RequestMapping(value = "monthlyListData")
    @ResponseBody
    public Page<Map<String, Object>> monthlyListData(SwmMonthlyAttendance swmMonthlyAttendance, HttpServletRequest request,
                                              HttpServletResponse response) {
        if (swmMonthlyAttendance.getCurrentMonth() == null) {

            Date today = new Date();

            swmMonthlyAttendance.setCurrentMonth(today);
        }

        swmMonthlyAttendance.setPage(new Page<>(request, response));

        Page<SwmMonthlyAttendance> originalPage = swmDailyAttendanceService.findMonthlyByPage(swmMonthlyAttendance);

        // 创建新的分页对象，用于存储格式化后的数据
        Page<Map<String, Object>> formattedPage = new Page<>(request, response);
        formattedPage.setCount(originalPage.getCount());
        formattedPage.setPageNo(originalPage.getPageNo());
        formattedPage.setPageSize(originalPage.getPageSize());

        // 处理日期格式并计算怠工时长
        List<Map<String, Object>> formattedList = new ArrayList<>();

        for (SwmMonthlyAttendance record : originalPage.getList()) {
            formattedList.add(convertToMap(record));
        }

        formattedPage.setList(formattedList);

        return formattedPage;
    }
}