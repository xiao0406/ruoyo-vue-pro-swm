package cn.iocoder.yudao.module.swm.controller.admin.person;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.module.swm.controller.admin.person.vo.SwmPersonExcelVO;
import cn.iocoder.yudao.module.swm.controller.admin.person.vo.SwmPersonPageReqVO;
import cn.iocoder.yudao.module.swm.controller.admin.person.vo.SwmPersonRespVO;
import cn.iocoder.yudao.module.swm.controller.admin.person.vo.SwmPersonSaveReqVO;
import cn.iocoder.yudao.module.swm.controller.admin.persondeparture.vo.SwmPersonDepartureRespVO;
import cn.iocoder.yudao.module.swm.controller.admin.persondeparture.vo.SwmPersonDepartureSaveReqVO;
import cn.iocoder.yudao.module.swm.dal.dataobject.SwmPersonDO;
import cn.iocoder.yudao.module.swm.dal.dataobject.SwmPersonDepartureDO;
import cn.iocoder.yudao.module.swm.dal.dataobject.SwmSafetyEducationDO;
import cn.iocoder.yudao.module.swm.service.SwmPersonDepartureService;
import cn.iocoder.yudao.module.swm.service.SwmPersonService;
import cn.iocoder.yudao.module.swm.service.SwmSafetyEducationService;
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
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 人员管理")
@RestController
@RequestMapping("/swm/person")
@Validated
public class SwmPersonController {

    @Resource
    private SwmPersonService personService;
    @Resource
    private SwmPersonDepartureService personDepartureService;
    @Resource
    private SwmSafetyEducationService safetyEducationService;

    @PostMapping("/create")
    @Operation(summary = "创建人员管理")
    @PreAuthorize("@ss.hasPermission('swm:person:create')")
    public CommonResult<String> createPerson(@Valid @RequestBody SwmPersonSaveReqVO createReqVO) {
        String id = personService.createPerson(createReqVO);
        return success(id);
    }

    @PutMapping("/update")
    @Operation(summary = "更新人员管理")
    @PreAuthorize("@ss.hasPermission('swm:person:update')")
    public CommonResult<Boolean> updatePerson(@Valid @RequestBody SwmPersonSaveReqVO updateReqVO) {
        personService.updatePerson(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除人员管理")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('swm:person:delete')")
    public CommonResult<Boolean> deletePerson(@RequestParam("id") String id) {
        personService.deletePerson(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获取人员管理")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('swm:person:query')")
    public CommonResult<SwmPersonRespVO> getPerson(@RequestParam("id") String id) {
        SwmPersonDO person = personService.getPerson(id);
        return success(BeanUtils.toBean(person, SwmPersonRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "分页查询人员管理")
    @PreAuthorize("@ss.hasPermission('swm:person:query')")
    public CommonResult<PageResult<SwmPersonRespVO>> getPersonPage(@Valid SwmPersonPageReqVO pageReqVO) {
        PageResult<SwmPersonDO> pageResult = personService.getPersonPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, SwmPersonRespVO.class));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "Export persons")
    @PreAuthorize("@ss.hasPermission('swm:person:export')")
    public void exportPerson(SwmPersonPageReqVO reqVO, HttpServletResponse response) throws IOException {
        reqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<SwmPersonDO> list = personService.getPersonPage(reqVO).getList();
        ExcelUtils.write(response, "人员数据.xls", "人员列表", SwmPersonExcelVO.class,
                BeanUtils.toBean(list, SwmPersonExcelVO.class));
    }

    @GetMapping({"/import-template", "/import-template-enhanced"})
    @Operation(summary = "下载人员导入模板")
    public void importTemplate(HttpServletResponse response) throws IOException {
        ExcelUtils.write(response, "人员信息导入模板.xls", "人员信息", SwmPersonExcelVO.class, List.of());
    }

    @PostMapping({"/import-excel", "/import-excel-enhanced"})
    @Operation(summary = "导入人员 Excel")
    @PreAuthorize("@ss.hasPermission('swm:person:create')")
    public CommonResult<Map<String, Object>> importPerson(@RequestParam("file") MultipartFile file) throws Exception {
        List<SwmPersonExcelVO> rows = ExcelUtils.read(file, SwmPersonExcelVO.class);
        int successCount = 0;
        List<String> errors = new ArrayList<>();
        for (int index = 0; index < rows.size(); index++) {
            SwmPersonExcelVO row = rows.get(index);
            try {
                if (row.getName() == null || row.getName().isBlank()) {
                    errors.add("第 " + (index + 2) + " 行姓名为空");
                    continue;
                }
                SwmPersonDO existing = row.getIdentityCard() == null || row.getIdentityCard().isBlank()
                        ? null : personService.getByIdentityCard(row.getIdentityCard());
                SwmPersonSaveReqVO saveReqVO = BeanUtils.toBean(row, SwmPersonSaveReqVO.class);
                if (existing == null) {
                    personService.createPerson(saveReqVO);
                } else {
                    saveReqVO.setId(existing.getId());
                    personService.updatePerson(saveReqVO);
                }
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
        result.put("message", errors.isEmpty() ? "导入成功" : "部分数据导入失败");
        return success(result);
    }

    @PostMapping("/batch-save")
    @Operation(summary = "批量保存人员")
    @PreAuthorize("@ss.hasPermission('swm:person:create')")
    public CommonResult<Map<String, Object>> batchSave(@RequestBody List<SwmPersonSaveReqVO> rows) {
        int successCount = 0;
        List<String> errors = new ArrayList<>();
        for (int index = 0; index < rows.size(); index++) {
            try {
                SwmPersonSaveReqVO row = rows.get(index);
                if (row.getId() == null || row.getId().isBlank()) {
                    personService.createPerson(row);
                } else {
                    personService.updatePerson(row);
                }
                successCount++;
            } catch (Exception ex) {
                errors.add("第 " + (index + 1) + " 条：" + ex.getMessage());
            }
        }
        Map<String, Object> result = new HashMap<>();
        result.put("success", errors.isEmpty());
        result.put("successCount", successCount);
        result.put("failedCount", errors.size());
        result.put("errors", errors);
        return success(result);
    }

    @GetMapping("/enum-options")
    @Operation(summary = "获取人员状态枚举")
    public CommonResult<Map<String, Object>> getEnumOptions() {
        Map<String, Object> result = new HashMap<>();
        result.put("personnelStatus", Map.of("1", "在职", "0", "离职"));
        result.put("safetyEducation", Map.of("0", "未完成", "1", "已完成"));
        result.put("helmetReturned", Map.of("0", "未归还", "1", "已归还"));
        result.put("departureType", Map.of("0", "正常离职", "1", "异常离职"));
        return success(result);
    }

    @PostMapping("/delete-list")
    @Operation(summary = "Batch delete persons")
    @PreAuthorize("@ss.hasPermission('swm:person:delete')")
    public CommonResult<Boolean> deletePersonList(@RequestParam("ids") String ids) {
        splitIds(ids).forEach(personService::deletePerson);
        return success(true);
    }

    @GetMapping("/check-identity-card")
    @Operation(summary = "Check identity card duplicate")
    @PreAuthorize("@ss.hasPermission('swm:person:query')")
    public CommonResult<Map<String, Object>> checkIdentityCard(
            @RequestParam("identityCard") String identityCard,
            @RequestParam(value = "excludeId", required = false) String excludeId) {
        SwmPersonDO person = personService.getByIdentityCard(identityCard);
        boolean sameRecord = person != null && excludeId != null && excludeId.equals(person.getId());
        boolean exists = person != null && !sameRecord && "1".equals(person.getPersonnelStatus());
        boolean hasInactivePerson = person != null && !sameRecord && !"1".equals(person.getPersonnelStatus());

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("exists", exists);
        result.put("existingPerson", exists ? person.getName() : null);
        result.put("hasInactivePerson", hasInactivePerson);
        result.put("inactivePerson", hasInactivePerson ? person.getName() : null);
        result.put("message", exists ? "Identity card already exists" : "OK");
        return success(result);
    }

    @PostMapping("/handle-departure")
    @Operation(summary = "Handle person departure")
    @PreAuthorize("@ss.hasPermission('swm:person:update')")
    public CommonResult<Boolean> handleDeparture(@RequestBody SwmPersonSaveReqVO reqVO) {
        SwmPersonDO person = personService.getPerson(reqVO.getId());
        if (person == null) {
            return success(false);
        }

        SwmPersonDepartureSaveReqVO departure = BeanUtils.toBean(person, SwmPersonDepartureSaveReqVO.class);
        departure.setId(null);
        departure.setHelmetReturned(reqVO.getHelmetReturned());
        departure.setDepartureType(reqVO.getDepartureType());
        departure.setDepartureReason(reqVO.getDepartureReason());
        departure.setDepartureDate(reqVO.getDepartureDate() == null ? LocalDate.now() : reqVO.getDepartureDate());
        personDepartureService.createPersonDeparture(departure);

        SwmPersonSaveReqVO update = BeanUtils.toBean(person, SwmPersonSaveReqVO.class);
        update.setPersonnelStatus("0");
        update.setHelmetReturned(reqVO.getHelmetReturned());
        update.setDepartureType(reqVO.getDepartureType());
        update.setDepartureReason(reqVO.getDepartureReason());
        update.setDepartureDate(departure.getDepartureDate());
        personService.updatePerson(update);
        if ("1".equals(reqVO.getHelmetReturned())) {
            personService.clearSafetyHelmet(person.getId());
        }
        return success(true);
    }

    @GetMapping("/find-departed-by-identity-card")
    @Operation(summary = "Find departed person by identity card")
    @PreAuthorize("@ss.hasPermission('swm:person:query')")
    public CommonResult<Map<String, Object>> findDepartedByIdentityCard(@RequestParam("identityCard") String identityCard) {
        SwmPersonDO person = personService.getByIdentityCard(identityCard);
        boolean hasRecord = person != null && !"1".equals(person.getPersonnelStatus());
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("hasRecord", hasRecord);
        result.put("data", hasRecord ? List.of(BeanUtils.toBean(person, SwmPersonRespVO.class)) : List.of());
        result.put("total", hasRecord ? 1 : 0);
        return success(result);
    }

    @GetMapping("/departure-detail")
    @Operation(summary = "Get person departure detail")
    @PreAuthorize("@ss.hasPermission('swm:person:query')")
    public CommonResult<Map<String, Object>> getDepartureDetail(@RequestParam("id") String id) {
        SwmPersonDepartureDO departure = personDepartureService.getPersonDeparture(id);
        Map<String, Object> result = new HashMap<>();
        result.put("success", departure != null);
        result.put("data", BeanUtils.toBean(departure, SwmPersonDepartureRespVO.class));
        return success(result);
    }

    @PostMapping("/clear-safety-helmet")
    @Operation(summary = "Clear bound safety helmet")
    @PreAuthorize("@ss.hasPermission('swm:person:update')")
    public CommonResult<Boolean> clearSafetyHelmet(@RequestParam("id") String id) {
        SwmPersonDO person = personService.getPerson(id);
        if (person == null) {
            return success(false);
        }
        return success(personService.clearSafetyHelmet(id));
    }

    @GetMapping({"/all", "/active-cache", "/active-with-id-card-cache"})
    @Operation(summary = "List persons for copied SWM pages")
    @PreAuthorize("@ss.hasPermission('swm:person:query')")
    public CommonResult<Map<String, Object>> listPersonOptions(SwmPersonDO query) {
        List<SwmPersonDO> list = personService.findList(query);
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("data", BeanUtils.toBean(list, SwmPersonRespVO.class));
        result.put("list", BeanUtils.toBean(list, SwmPersonRespVO.class));
        result.put("total", list.size());
        return success(result);
    }

    @PostMapping("/batch-complete-safety-education")
    @Operation(summary = "Batch complete safety education")
    @PreAuthorize("@ss.hasPermission('swm:person:update')")
    public CommonResult<Map<String, Object>> batchCompleteSafetyEducation(@RequestParam("personIds") String personIds) {
        List<String> ids = splitIds(personIds);
        personService.completeSafetyEducation(ids);
        Map<String, Object> result = new HashMap<>();
        result.put("result", "success");
        result.put("message", "OK");
        result.put("successCount", ids.size());
        result.put("totalCount", ids.size());
        return success(result);
    }

    @GetMapping("/safety-education-by-identity-card")
    @Operation(summary = "List safety education records by identity card")
    @PreAuthorize("@ss.hasPermission('swm:person:query')")
    public CommonResult<Map<String, Object>> getSafetyEducationByIdentityCard(@RequestParam("identityCard") String identityCard) {
        List<SwmSafetyEducationDO> records = safetyEducationService.findByIdentityCard(identityCard);
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("data", records);
        result.put("total", records.size());
        result.put("message", "OK");
        return success(result);
    }

    @GetMapping("/find-by-department-condition")
    @Operation(summary = "根据组织条件查询在职人员")
    public CommonResult<List<SwmPersonRespVO>> findByDepartmentCondition(
            @RequestParam("departmentCondition") String departmentCondition) {
        return success(BeanUtils.toBean(personService.findByDepartmentCondition(departmentCondition),
                SwmPersonRespVO.class));
    }

    @GetMapping("/search")
    @Operation(summary = "搜索人员")
    public CommonResult<Map<String, Object>> search(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "searchType", defaultValue = "all") String searchType,
            @RequestParam(value = "pageNo", defaultValue = "1") Integer pageNo,
            @RequestParam(value = "pageSize", defaultValue = "20") Integer pageSize) {
        List<SwmPersonDO> all = personService.search(keyword, searchType);
        int safePageSize = Math.min(Math.max(pageSize, 1), 200);
        int start = Math.max(pageNo - 1, 0) * safePageSize;
        int end = Math.min(start + safePageSize, all.size());
        List<SwmPersonDO> page = start < all.size() ? all.subList(start, end) : List.of();
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("list", BeanUtils.toBean(page, SwmPersonRespVO.class));
        result.put("total", all.size());
        return success(result);
    }

    private List<String> splitIds(String ids) {
        if (ids == null || ids.isBlank()) {
            return List.of();
        }
        return Arrays.stream(ids.split(","))
                .map(String::trim)
                .filter(id -> !id.isEmpty())
                .toList();
    }

}
