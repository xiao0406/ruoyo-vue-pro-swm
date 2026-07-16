package cn.iocoder.yudao.module.swm.controller.admin.personSchedule;

import cn.idev.excel.annotation.ExcelProperty;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.module.swm.controller.admin.personSchedule.vo.SwmPersonSchedulePageReqVO;
import cn.iocoder.yudao.module.swm.controller.admin.personSchedule.vo.SwmPersonScheduleRespVO;
import cn.iocoder.yudao.module.swm.controller.admin.personSchedule.vo.SwmPersonScheduleSaveReqVO;
import cn.iocoder.yudao.module.swm.dal.dataobject.SwmPersonScheduleDO;
import cn.iocoder.yudao.module.swm.dal.dataobject.SwmPersonDO;
import cn.iocoder.yudao.module.swm.dal.dataobject.dto.SwmBatchUpdateClassesDTO;
import cn.iocoder.yudao.module.swm.dal.mysql.SwmDailyAttendanceMapper;
import cn.iocoder.yudao.module.swm.dal.mysql.SwmPersonScheduleMapper;
import cn.iocoder.yudao.module.swm.service.SwmPersonService;
import cn.iocoder.yudao.module.swm.service.SwmPersonScheduleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.Data;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 人员排班")
@RestController
@RequestMapping("/swm/person-schedule")
@Validated
public class SwmPersonScheduleController {

    @Resource
    private SwmPersonScheduleService personScheduleService;
    @Resource
    private SwmPersonScheduleMapper personScheduleMapper;
    @Resource
    private SwmDailyAttendanceMapper dailyAttendanceMapper;
    @Resource
    private SwmPersonService personService;

    @PostMapping("/create")
    @Operation(summary = "创建人员排班")
    @PreAuthorize("@ss.hasPermission('swm:person-schedule:create')")
    public CommonResult<String> createSwmPersonSchedule(@Valid @RequestBody SwmPersonScheduleSaveReqVO createReqVO) {
        String id = personScheduleService.createPersonSchedule(createReqVO);
        return success(id);
    }

    @PutMapping("/update")
    @Operation(summary = "更新人员排班")
    @PreAuthorize("@ss.hasPermission('swm:person-schedule:update')")
    public CommonResult<Boolean> updateSwmPersonSchedule(@Valid @RequestBody SwmPersonScheduleSaveReqVO updateReqVO) {
        personScheduleService.updatePersonSchedule(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除人员排班")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('swm:person-schedule:delete')")
    public CommonResult<Boolean> deleteSwmPersonSchedule(@RequestParam("id") String id) {
        personScheduleService.deletePersonSchedule(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获取人员排班")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('swm:person-schedule:query')")
    public CommonResult<SwmPersonScheduleRespVO> getSwmPersonSchedule(@RequestParam("id") String id) {
        SwmPersonScheduleDO personSchedule = personScheduleService.getPersonSchedule(id);
        return success(BeanUtils.toBean(personSchedule, SwmPersonScheduleRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "分页查询人员排班")
    @PreAuthorize("@ss.hasPermission('swm:person-schedule:query')")
    public CommonResult<PageResult<SwmPersonScheduleRespVO>> getSwmPersonSchedulePage(@Valid SwmPersonSchedulePageReqVO pageReqVO) {
        PageResult<SwmPersonScheduleDO> pageResult = personScheduleService.getPersonSchedulePage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, SwmPersonScheduleRespVO.class));
    }

    @PostMapping("/batchSave")
    @Operation(summary = "Legacy batch save schedules")
    @PreAuthorize("@ss.hasPermission('swm:person-schedule:create')")
    public CommonResult<Boolean> batchSave(@RequestBody List<SwmPersonScheduleSaveReqVO> list) {
        if (list != null) {
            list.forEach(personScheduleService::createPersonSchedule);
        }
        return success(true);
    }

    @PostMapping("/batchUpdateClasses")
    @Operation(summary = "Legacy batch update classes")
    @PreAuthorize("@ss.hasPermission('swm:person-schedule:update')")
    public CommonResult<Boolean> batchUpdateClasses(@RequestBody SwmBatchUpdateClassesDTO dto) {
        personScheduleMapper.batchUpdateClasses(dto);
        return success(true);
    }

    @GetMapping("/getPersonIdList")
    @Operation(summary = "Legacy get selected schedule ids")
    @PreAuthorize("@ss.hasPermission('swm:person-schedule:query')")
    public CommonResult<List<String>> getPersonIdList(SwmPersonSchedulePageReqVO reqVO) {
        List<SwmPersonScheduleDO> list = personScheduleService.findList(BeanUtils.toBean(reqVO, SwmPersonScheduleDO.class));
        return success(list.stream().map(SwmPersonScheduleDO::getId).collect(Collectors.toList()));
    }

    @GetMapping("/getWorkGroups")
    @Operation(summary = "Legacy work group options")
    @PreAuthorize("@ss.hasPermission('swm:person-schedule:query')")
    public CommonResult<List<Map<String, Object>>> getWorkGroups() {
        return success(personScheduleMapper.findWorkGroupList());
    }

    @GetMapping("/monthStats")
    @Operation(summary = "Legacy month schedule stats")
    @PreAuthorize("@ss.hasPermission('swm:person-schedule:query')")
    public CommonResult<Map<String, Object>> getMonthStats(@RequestParam("month") String month) {
        Map<String, Object> result = new HashMap<>();
        result.put("month", month);
        result.put("totalPerson", personScheduleMapper.countDistinctPersonByYearAndMonth(month));
        result.put("earlyShiftCount", 0);
        result.put("middleShiftCount", 0);
        result.put("nightShiftCount", 0);
        return success(result);
    }

    @GetMapping("/import-template")
    @Operation(summary = "下载班次调整导入模板")
    public void importTemplate(HttpServletResponse response) throws IOException {
        ExcelUtils.write(response, "班次调整导入模板.xlsx", "班次调整",
                PersonScheduleImportExcelVO.class, List.of());
    }

    @PostMapping("/import-excel")
    @Operation(summary = "导入班次调整")
    @PreAuthorize("@ss.hasPermission('swm:person-schedule:update')")
    public CommonResult<Map<String, Object>> importExcel(@RequestParam("file") MultipartFile file) throws Exception {
        List<PersonScheduleImportExcelVO> rows = ExcelUtils.read(file, PersonScheduleImportExcelVO.class);
        int successCount = 0;
        List<String> errors = new ArrayList<>();
        for (int index = 0; index < rows.size(); index++) {
            PersonScheduleImportExcelVO row = rows.get(index);
            try {
                String idCard = resolveIdentityCard(row);
                String classes = normalizeClasses(row.getClasses());
                int updated = personScheduleMapper.updateClassesByIdentityCard(idCard, classes);
                if (updated == 0) {
                    throw new IllegalArgumentException("未找到该人员的有效排班记录");
                }
                dailyAttendanceMapper.updateTodayClasses(idCard, classes);
                successCount++;
            } catch (Exception ex) {
                errors.add("第 " + (index + 2) + " 行：" + ex.getMessage());
            }
        }
        Map<String, Object> result = new HashMap<>();
        result.put("success", errors.isEmpty());
        result.put("successCount", successCount);
        result.put("failedCount", errors.size());
        result.put("errors", errors);
        result.put("message", errors.isEmpty() ? "班次调整成功" : "部分班次调整失败");
        return success(result);
    }

    private String resolveIdentityCard(PersonScheduleImportExcelVO row) {
        if (row.getIdCard() != null && !row.getIdCard().isBlank()) {
            String idCard = row.getIdCard().trim();
            if (personService.getByIdentityCard(idCard) == null) {
                throw new IllegalArgumentException("身份证号未匹配到在职人员");
            }
            return idCard;
        }
        if (row.getPersonName() == null || row.getPersonName().isBlank()) {
            throw new IllegalArgumentException("姓名和身份证号不能同时为空");
        }
        String personName = row.getPersonName().trim();
        List<SwmPersonDO> matches = personService.search(personName, "name").stream()
                .filter(person -> personName.equals(person.getName()))
                .toList();
        if (matches.size() != 1 || matches.get(0).getIdentityCard() == null
                || matches.get(0).getIdentityCard().isBlank()) {
            throw new IllegalArgumentException(matches.isEmpty() ? "姓名未匹配到在职人员" : "姓名不唯一，请填写身份证号");
        }
        return matches.get(0).getIdentityCard();
    }

    private String normalizeClasses(String classes) {
        if (classes == null || classes.isBlank()) {
            throw new IllegalArgumentException("班次不能为空");
        }
        return switch (classes.trim()) {
            case "1", "白班", "早班" -> "1";
            case "3", "夜班", "晚班" -> "3";
            default -> throw new IllegalArgumentException("班次仅支持白班/早班(1)或夜班/晚班(3)");
        };
    }

    @Data
    public static class PersonScheduleImportExcelVO {
        @ExcelProperty("姓名")
        private String personName;
        @ExcelProperty("身份证号码")
        private String idCard;
        @ExcelProperty("班次")
        private String classes;
    }

}
