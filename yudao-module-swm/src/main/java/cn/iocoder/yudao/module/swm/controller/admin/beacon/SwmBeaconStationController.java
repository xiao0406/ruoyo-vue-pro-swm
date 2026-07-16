package cn.iocoder.yudao.module.swm.controller.admin.beacon;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.swm.controller.admin.beacon.vo.SwmBeaconStationPageReqVO;
import cn.iocoder.yudao.module.swm.controller.admin.beacon.vo.SwmBeaconStationRespVO;
import cn.iocoder.yudao.module.swm.controller.admin.beacon.vo.SwmBeaconStationSaveReqVO;
import cn.iocoder.yudao.module.swm.dal.dataobject.SwmBeaconStationDO;
import cn.iocoder.yudao.module.swm.service.SwmBeaconStationService;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 信标站点")
@RestController
@RequestMapping("/swm/beacon-station")
@Validated
public class SwmBeaconStationController {

    @Resource
    private SwmBeaconStationService beaconStationService;

    @PostMapping("/create")
    @Operation(summary = "创建信标站点")
    @PreAuthorize("@ss.hasPermission('swm:beacon-station:create')")
    public CommonResult<String> createBeaconStation(@Valid @RequestBody SwmBeaconStationSaveReqVO createReqVO) {
        String id = beaconStationService.createBeaconStation(createReqVO);
        return success(id);
    }

    @PutMapping("/update")
    @Operation(summary = "更新信标站点")
    @PreAuthorize("@ss.hasPermission('swm:beacon-station:update')")
    public CommonResult<Boolean> updateBeaconStation(@Valid @RequestBody SwmBeaconStationSaveReqVO updateReqVO) {
        beaconStationService.updateBeaconStation(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除信标站点")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('swm:beacon-station:delete')")
    public CommonResult<Boolean> deleteBeaconStation(@RequestParam("id") String id) {
        beaconStationService.deleteBeaconStation(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获取信标站点")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('swm:beacon-station:query')")
    public CommonResult<SwmBeaconStationRespVO> getBeaconStation(@RequestParam("id") String id) {
        SwmBeaconStationDO beaconStation = beaconStationService.getBeaconStation(id);
        return success(BeanUtils.toBean(beaconStation, SwmBeaconStationRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "分页查询信标站点")
    @PreAuthorize("@ss.hasPermission('swm:beacon-station:query')")
    public CommonResult<PageResult<SwmBeaconStationRespVO>> getBeaconStationPage(@Valid SwmBeaconStationPageReqVO pageReqVO) {
        PageResult<SwmBeaconStationDO> pageResult = beaconStationService.getBeaconStationPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, SwmBeaconStationRespVO.class));
    }

    @GetMapping("/list-all")
    @Operation(summary = "获取全部信标站点")
    @PreAuthorize("@ss.hasPermission('swm:beacon-station:query')")
    public CommonResult<List<SwmBeaconStationRespVO>> getBeaconStationList(@Valid SwmBeaconStationPageReqVO reqVO) {
        List<SwmBeaconStationDO> list = beaconStationService.getBeaconStationList(reqVO);
        return success(BeanUtils.toBean(list, SwmBeaconStationRespVO.class));
    }

    @GetMapping("/import-template")
    @Operation(summary = "下载信标导入模板")
    public void importTemplate(HttpServletResponse response) throws IOException {
        ExcelUtils.write(response, "信标导入模板.xlsx", "信标", BeaconImportExcelVO.class, List.of());
    }

    @PostMapping("/import-excel")
    @Operation(summary = "导入信标")
    @PreAuthorize("@ss.hasPermission('swm:beacon-station:create')")
    public CommonResult<Map<String, Object>> importBeacon(@RequestParam("file") MultipartFile file) throws Exception {
        List<BeaconImportExcelVO> rows = ExcelUtils.read(file, BeaconImportExcelVO.class);
        List<SwmBeaconStationDO> existingList = beaconStationService.getBeaconStationList(new SwmBeaconStationPageReqVO());
        int successCount = 0;
        List<String> errors = new ArrayList<>();
        for (int index = 0; index < rows.size(); index++) {
            BeaconImportExcelVO row = rows.get(index);
            try {
                if (row.getBeaconId() == null || row.getBeaconId().isBlank()) {
                    errors.add("第 " + (index + 2) + " 行MAC地址为空");
                    continue;
                }
                SwmBeaconStationSaveReqVO reqVO = BeanUtils.toBean(row, SwmBeaconStationSaveReqVO.class);
                SwmBeaconStationDO existing = existingList.stream()
                        .filter(item -> row.getBeaconId().equalsIgnoreCase(item.getBeaconId()))
                        .findFirst().orElse(null);
                if (existing == null) {
                    beaconStationService.createBeaconStation(reqVO);
                } else {
                    reqVO.setId(existing.getId());
                    beaconStationService.updateBeaconStation(reqVO);
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
    public static class BeaconImportExcelVO {
        @ExcelProperty("MAC地址") private String beaconId;
        @ExcelProperty("所在位置") private String location;
        @ExcelProperty("设备名称") private String deviceName;
        @ExcelProperty("所属区域") private String area;
        @ExcelProperty("图纸像素X坐标") private Double pixelX;
        @ExcelProperty("图纸像素Y坐标") private Double pixelY;
        @ExcelProperty("信标类型") private String beaconType;
        @ExcelProperty("信标状态（在线、离线）") private String beaconStatus;
        @ExcelProperty("major") private String major;
        @ExcelProperty("minor") private String minor;
        @ExcelProperty("所在建筑") private String building;
        @ExcelProperty("所在楼层") private String floor;
    }

    @GetMapping("/location-for-map")
    @Operation(summary = "Get beacon coordinates for algorithm map")
    @PreAuthorize("@ss.hasPermission('swm:beacon-station:query')")
    public CommonResult<Map<String, Map<String, Object>>> getBeaconLocationForMap() {
        SwmBeaconStationPageReqVO reqVO = new SwmBeaconStationPageReqVO();
        reqVO.setBeaconType("1");
        Map<String, Map<String, Object>> result = new LinkedHashMap<>();
        for (SwmBeaconStationDO beacon : beaconStationService.getBeaconStationList(reqVO)) {
            String beaconId = formatMacAddress(beacon.getBeaconId());
            if (beaconId == null) {
                continue;
            }
            Map<String, Object> coordinate = new LinkedHashMap<>();
            coordinate.put("location", beacon.getLocation());
            coordinate.put("x", beacon.getPixelX());
            coordinate.put("y", beacon.getPixelY());
            result.put(beaconId, coordinate);
        }
        return success(result);
    }

    private String formatMacAddress(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String normalized = value.replaceAll("[^0-9A-Fa-f]", "").toLowerCase();
        if (normalized.length() != 12) {
            return value.toLowerCase();
        }
        return normalized.replaceAll("(.{2})(?!$)", "$1:");
    }

}
