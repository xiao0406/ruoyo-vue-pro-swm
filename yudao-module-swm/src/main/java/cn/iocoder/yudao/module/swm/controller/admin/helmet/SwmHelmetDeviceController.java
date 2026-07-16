package cn.iocoder.yudao.module.swm.controller.admin.helmet;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.module.swm.controller.admin.helmet.vo.SwmHelmetDevicePageReqVO;
import cn.iocoder.yudao.module.swm.controller.admin.helmet.vo.SwmHelmetDeviceRespVO;
import cn.iocoder.yudao.module.swm.controller.admin.helmet.vo.SwmHelmetDeviceSaveReqVO;
import cn.iocoder.yudao.module.swm.dal.dataobject.SwmHelmetDeviceDO;
import cn.iocoder.yudao.module.swm.service.SwmHelmetDeviceService;
import cn.idev.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 安全帽设备")
@RestController
@RequestMapping("/swm/helmet-device")
@Validated
public class SwmHelmetDeviceController {

    @Resource
    private SwmHelmetDeviceService helmetDeviceService;

    @PostMapping("/create")
    @Operation(summary = "创建安全帽设备")
    @PreAuthorize("@ss.hasPermission('swm:helmet-device:create')")
    public CommonResult<String> createHelmetDevice(@Valid @RequestBody SwmHelmetDeviceSaveReqVO createReqVO) {
        String id = helmetDeviceService.createHelmetDevice(createReqVO);
        return success(id);
    }

    @PutMapping("/update")
    @Operation(summary = "更新安全帽设备")
    @PreAuthorize("@ss.hasPermission('swm:helmet-device:update')")
    public CommonResult<Boolean> updateHelmetDevice(@Valid @RequestBody SwmHelmetDeviceSaveReqVO updateReqVO) {
        helmetDeviceService.updateHelmetDevice(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除安全帽设备")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('swm:helmet-device:delete')")
    public CommonResult<Boolean> deleteHelmetDevice(@RequestParam("id") String id) {
        helmetDeviceService.deleteHelmetDevice(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获取安全帽设备")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('swm:helmet-device:query')")
    public CommonResult<SwmHelmetDeviceRespVO> getHelmetDevice(@RequestParam("id") String id) {
        SwmHelmetDeviceDO helmetDevice = helmetDeviceService.getHelmetDevice(id);
        return success(BeanUtils.toBean(helmetDevice, SwmHelmetDeviceRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "分页查询安全帽设备")
    @PreAuthorize("@ss.hasPermission('swm:helmet-device:query')")
    public CommonResult<PageResult<SwmHelmetDeviceRespVO>> getHelmetDevicePage(@Valid SwmHelmetDevicePageReqVO pageReqVO) {
        PageResult<SwmHelmetDeviceDO> pageResult = helmetDeviceService.getHelmetDevicePage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, SwmHelmetDeviceRespVO.class));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出安全帽设备")
    @PreAuthorize("@ss.hasPermission('swm:helmet-device:export')")
    public void exportHelmetDevice(@Valid SwmHelmetDevicePageReqVO reqVO,
                                   HttpServletResponse response) throws IOException {
        reqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<SwmHelmetDeviceRespVO> list = BeanUtils.toBean(
                helmetDeviceService.getHelmetDevicePage(reqVO).getList(), SwmHelmetDeviceRespVO.class);
        ExcelUtils.write(response, "安全帽设备.xls", "安全帽设备", SwmHelmetDeviceRespVO.class, list);
    }

    @GetMapping("/import-template")
    @Operation(summary = "下载安全帽设备导入模板")
    public void importTemplate(HttpServletResponse response) throws IOException {
        ExcelUtils.write(response, "安全帽设备导入模板.xlsx", "安全帽设备", HelmetImportExcelVO.class, List.of());
    }

    @PostMapping("/import-excel")
    @Operation(summary = "导入安全帽设备")
    @PreAuthorize("@ss.hasPermission('swm:helmet-device:create')")
    public CommonResult<Map<String, Object>> importHelmetDevice(@RequestParam("file") MultipartFile file) throws Exception {
        List<HelmetImportExcelVO> rows = ExcelUtils.read(file, HelmetImportExcelVO.class);
        int successCount = 0;
        List<String> errors = new ArrayList<>();
        for (int index = 0; index < rows.size(); index++) {
            HelmetImportExcelVO row = rows.get(index);
            try {
                if (row.getDeviceId() == null || row.getDeviceId().isBlank()) {
                    errors.add("第 " + (index + 2) + " 行设备编码为空");
                    continue;
                }
                SwmHelmetDeviceSaveReqVO reqVO = BeanUtils.toBean(row, SwmHelmetDeviceSaveReqVO.class);
                SwmHelmetDeviceDO existing = helmetDeviceService.getByDeviceId(row.getDeviceId());
                if (existing == null) {
                    helmetDeviceService.createHelmetDevice(reqVO);
                } else {
                    reqVO.setId(existing.getId());
                    helmetDeviceService.updateHelmetDevice(reqVO);
                }
                successCount++;
            } catch (Exception ex) {
                errors.add("第 " + (index + 2) + " 行：" + ex.getMessage());
            }
        }
        Map<String, Object> result = new HashMap<>();
        result.put("successCount", successCount);
        result.put("failedCount", errors.size());
        result.put("errors", errors);
        return success(result);
    }

    @Data
    public static class HelmetImportExcelVO {
        @ExcelProperty("设备编码")
        private String deviceId;
        @ExcelProperty("头盔类型(1:便携式 2:头箍式)")
        private String helmetType;
        @ExcelProperty("MAC地址")
        private String macAddress;
        @ExcelProperty("设备厂家（默认给0）")
        private String deviceSource;
    }

    @GetMapping("/getByDeviceId")
    @Operation(summary = "Get helmet device by deviceId")
    @PreAuthorize("@ss.hasPermission('swm:helmet-device:query')")
    public CommonResult<SwmHelmetDeviceRespVO> getByDeviceId(@RequestParam("deviceId") String deviceId) {
        SwmHelmetDeviceDO helmetDevice = helmetDeviceService.getByDeviceId(deviceId);
        return success(BeanUtils.toBean(helmetDevice, SwmHelmetDeviceRespVO.class));
    }

    @GetMapping("/findAvailableHelmets")
    @Operation(summary = "Find available helmet devices")
    @PreAuthorize("@ss.hasPermission('swm:helmet-device:query')")
    public CommonResult<List<SwmHelmetDeviceRespVO>> findAvailableHelmets(
            @RequestParam(value = "keyword", required = false) String keyword) {
        List<SwmHelmetDeviceDO> list = helmetDeviceService.findAvailableHelmets(keyword);
        return success(BeanUtils.toBean(list, SwmHelmetDeviceRespVO.class));
    }

    @PostMapping("/deleteAll")
    @Operation(summary = "兼容旧前端批量删除安全帽设备")
    @PreAuthorize("@ss.hasPermission('swm:helmet-device:delete')")
    public CommonResult<Boolean> deleteAllHelmetDevices(@RequestParam("ids") String ids) {
        if (ids != null && !ids.isBlank()) {
            Arrays.stream(ids.split(","))
                    .map(String::trim)
                    .filter(id -> !id.isEmpty())
                    .forEach(helmetDeviceService::deleteHelmetDevice);
        }
        return success(true);
    }

    @PostMapping("/updateBattery")
    @Operation(summary = "兼容旧前端更新安全帽电量")
    @PreAuthorize("@ss.hasPermission('swm:helmet-device:update')")
    public CommonResult<Boolean> updateBattery(@RequestParam("deviceId") String deviceId,
                                               @RequestParam("batteryLevel") Integer batteryLevel) {
        return success(true);
    }

    @PostMapping("/saveDeviceConfig")
    @Operation(summary = "兼容旧前端保存安全帽配置")
    @PreAuthorize("@ss.hasPermission('swm:helmet-device:update')")
    public CommonResult<Boolean> saveDeviceConfig(@RequestBody Map<String, Object> data) {
        return success(true);
    }

    @GetMapping("/getDeviceConfig")
    @Operation(summary = "兼容旧前端获取安全帽配置")
    @PreAuthorize("@ss.hasPermission('swm:helmet-device:query')")
    public CommonResult<Map<String, Object>> getDeviceConfig(@RequestParam("deviceId") String deviceId) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("data", new HashMap<>());
        return success(result);
    }

    @PostMapping("/assignPerson")
    @Operation(summary = "Assign person to helmet device")
    @PreAuthorize("@ss.hasPermission('swm:helmet-device:update')")
    public CommonResult<Boolean> assignPerson(@RequestParam("deviceId") String deviceId,
                                              @RequestParam(value = "personId", required = false) String personId,
                                              @RequestParam(value = "personName", required = false) String personName,
                                              @RequestParam(value = "binder", required = false) String binder) {
        SwmHelmetDeviceDO device = helmetDeviceService.getByDeviceId(deviceId);
        if (device == null) {
            return success(false);
        }
        return success(helmetDeviceService.assignPerson(deviceId, personId, personName, binder));
    }

    @PostMapping("/unassignPerson")
    @Operation(summary = "Unassign person from helmet device")
    @PreAuthorize("@ss.hasPermission('swm:helmet-device:update')")
    public CommonResult<Boolean> unassignPerson(@RequestParam("deviceId") String deviceId) {
        SwmHelmetDeviceDO device = helmetDeviceService.getByDeviceId(deviceId);
        if (device == null) {
            return success(false);
        }
        return success(helmetDeviceService.unassignPerson(deviceId));
    }

    @PostMapping("/batchChargeReminder")
    @Operation(summary = "兼容旧前端批量充电提醒")
    @PreAuthorize("@ss.hasPermission('swm:helmet-device:update')")
    public CommonResult<Boolean> batchChargeReminder(@RequestBody Map<String, Object> data) {
        return success(true);
    }

    @GetMapping({"/findByAssignedPerson", "/findByWorkshop", "/findByProcess", "/findByTeam"})
    @Operation(summary = "兼容旧前端按条件查询安全帽")
    @PreAuthorize("@ss.hasPermission('swm:helmet-device:query')")
    public CommonResult<List<SwmHelmetDeviceRespVO>> findHelmetsByLegacyCondition(SwmHelmetDevicePageReqVO reqVO) {
        reqVO.setPageSize(200);
        PageResult<SwmHelmetDeviceDO> pageResult = helmetDeviceService.getHelmetDevicePage(reqVO);
        return success(BeanUtils.toBean(pageResult.getList(), SwmHelmetDeviceRespVO.class));
    }

    @GetMapping("/getHelmetUsageRecords")
    @Operation(summary = "兼容旧前端安全帽使用记录")
    @PreAuthorize("@ss.hasPermission('swm:helmet-device:query')")
    public CommonResult<Map<String, Object>> getHelmetUsageRecords(@RequestParam("deviceId") String deviceId) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("data", List.of());
        result.put("list", List.of());
        result.put("total", 0);
        return success(result);
    }

}
